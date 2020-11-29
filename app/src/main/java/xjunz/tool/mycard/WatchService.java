/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.text.Html;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.Observable;
import androidx.databinding.ObservableInt;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.SSLParameters;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Flowable;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Response;
import xjunz.tool.mycard.api.Constants;
import xjunz.tool.mycard.api.LoadPlayerInfoService;
import xjunz.tool.mycard.api.bean.Duel;
import xjunz.tool.mycard.api.bean.Player;
import xjunz.tool.mycard.ui.MainActivity;
import xjunz.tool.mycard.ui.WatchSetupActivity;
import xjunz.tool.mycard.util.Utils;

public class WatchService extends Service {

    /**
     * 观战信息WSS URL
     */
    private List<Duel> mDuels;
    private WebSocketClient mClient;
    public static final int STATUS_CLOSED = -1;
    public static final int STATUS_CONNECTING = 0;
    public static final int STATUS_OPENED = 1;
    private static final int FOREGROUND_NOTIFICATION_ID = 814;
    private static final int DEF_DUEL_NOTIFICATION_ID = 1999;
    private final ObservableInt mServiceStatus = new ObservableInt(STATUS_CLOSED);
    private DuelCallback mDuelCallback;
    private NotificationManager mNotificationManager;
    private List<CountDownTimer> mCountDownTimers;
    private boolean mNotifyClientClosure = true;
    private int mDuelNotificationId = DEF_DUEL_NOTIFICATION_ID;
    private HashMap<String, Integer> mNotifiedDuels;
    private String mCurrentShowedSingleIdDuel;
    private String FOREGROUND_CHANNEL_ID;
    private String PUSH_CHANNEL_ID;
    private String mCurrentWhiteHotDuelId;
    private final TimerTask mWhiteHotChecker = new TimerTask() {
        @Override
        public void run() {
            if (App.config().pushWhiteHot.getValue() && mCurrentWhiteHotDuelId == null) {
                Duel candidate = null;
                int minRankSum = Integer.MAX_VALUE;
                for (int i = 0; i < Math.min(mDuels.size(), 5); i++) {
                    long cur = System.currentTimeMillis();
                    Duel duel = mDuels.get(i);
                    if (duel.getStartTimestamp() != 0 && cur - duel.getStartTimestamp() >= 15 * 60 * 1000) {
                        if (duel.getPlayer1Rank() != 0 && duel.getPlayer1Rank() <= 1500) {
                            if (duel.getPlayer2Rank() != 0 && duel.getPlayer2Rank() <= 1500) {
                                int curRankSum = duel.getPlayer1Rank() + duel.getPlayer2Rank();
                                if (curRankSum <= minRankSum) {
                                    minRankSum = curRankSum;
                                    candidate = duel;
                                }
                            }
                        }
                    }
                }
                if (candidate != null) {
                    mCurrentWhiteHotDuelId = candidate.getId();
                    notifyDuel(candidate, buildWatchNotification(getString(R.string.new_white_hot)
                            , Html.fromHtml(getResources().getString(R.string.def_notification_content, candidate.getPlayer1Rank(), candidate.getPlayer1Name(), candidate.getPlayer2Rank(), candidate.getPlayer2Name()))
                            , candidate));
                }
            }
        }
    };
    private Timer mWhiteHotTimer;
    private Disposable mOnMessageDisposable;
    private int mOrdinalOfAllDuels;

    @NonNull

    public ObservableInt getServiceStatus() {
        return mServiceStatus;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new WatchBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mDuelCallback = null;
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotifiedDuels = new HashMap<>();
        mCountDownTimers = new ArrayList<>();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            FOREGROUND_CHANNEL_ID = getPackageName() + ":foreground";
            PUSH_CHANNEL_ID = getPackageName() + ":push";
            NotificationChannel foregroundChannel = new NotificationChannel(FOREGROUND_CHANNEL_ID, getString(getApplicationInfo().labelRes), NotificationManager.IMPORTANCE_HIGH);
            foregroundChannel.setShowBadge(false);
            mNotificationManager.createNotificationChannel(foregroundChannel);
            NotificationChannel pushChannel = new NotificationChannel(PUSH_CHANNEL_ID, getString(getApplicationInfo().labelRes), NotificationManager.IMPORTANCE_HIGH);
            pushChannel.setShowBadge(true);
            mNotificationManager.createNotificationChannel(pushChannel);
        }
        mServiceStatus.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                switch (mServiceStatus.get()) {
                    case STATUS_CLOSED:
                        if (mNotifyClientClosure) {
                            mNotificationManager.notify(FOREGROUND_NOTIFICATION_ID, buildForegroundNotification(getText(R.string.monitoring_service_stopped)));
                        }
                        break;
                    case STATUS_CONNECTING:
                        break;
                    case STATUS_OPENED:
                        mNotificationManager.notify(FOREGROUND_NOTIFICATION_ID, buildForegroundNotification(getText(R.string.monitoring_focused_duels)));
                        break;
                }
            }
        });
        startForeground(FOREGROUND_NOTIFICATION_ID, buildForegroundNotification(getText(R.string.starting_monitoring_service)));
    }

    private void beginDelayPush(int min, Duel duel) {
        Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@io.reactivex.annotations.NonNull CompletableEmitter emitter) throws Exception {
                //timer要在main thread中创建[Handler.java:227]
                CountDownTimer timer = new CountDownTimer(min * 60 * 1000, 100) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        if (!mDuels.contains(duel)) {
                            cancel();
                            mCountDownTimers.remove(this);
                        }
                    }

                    @Override
                    public void onFinish() {
                        mCountDownTimers.remove(this);
                        if (mDuels.contains(duel)) {
                            String whiteListedPlayer = App.config().enableWhitelist.getValue() ? duel.getPlayerName(App.config().isWhitelisted(duel)) : null;
                            notifyDuel(duel, buildWatchNotification(
                                    whiteListedPlayer == null ? getString(R.string.delayed_notification_title, min) : getString(R.string.whitelisted_delayed_notification_title, whiteListedPlayer, min)
                                    , buildNotificationContent(duel), duel));
                        }
                    }
                }.start();
                mCountDownTimers.add(timer);
            }
        }).subscribeOn(AndroidSchedulers.mainThread()).subscribe();
    }

    private Notification buildForegroundNotification(CharSequence content) {
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext());
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, -1, intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setSmallIcon(R.drawable.ic_baseline_eye_24)
                .setContentText(content)
                .setDefaults(Notification.DEFAULT_ALL)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(FOREGROUND_CHANNEL_ID);
        }
        return builder.build();
    }

    private Notification buildWatchNotification(CharSequence title, CharSequence content, Duel duel) {
        Intent intent;
        if (!App.config().hasCompleteWatchConfig() || !App.isYGOMobileInstalled()) {
            intent = new Intent(this, WatchSetupActivity.class);
            intent.putExtra(WatchSetupActivity.EXTRA_DUEL_ID, duel.getId());
        } else {
            intent = Utils.buildLaunchWatchIntent(duel.getId());
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, mDuelNotificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext());
        long timestamp = System.currentTimeMillis();
        builder.setContentTitle(title)
                .setSmallIcon(R.drawable.ic_baseline_local_fire_department_24)
                .setContentText(content)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setWhen(timestamp);
        if (duel.getStartTimestamp() > 0) {
            builder.setSubText(getString(R.string.duel_started_at, Utils.formatDate(getResources().getConfiguration().locale, duel.getStartTimestamp())));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(PUSH_CHANNEL_ID);
        }
        return builder.build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mClient == null || mClient.isClosed()) {
            try {
                mClient = new MyCardWssClient(new URI(Constants.WATCH_WSS_URL));
                mClient.connect();
                mServiceStatus.set(STATUS_CONNECTING);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
            if (mDuels != null && mDuelCallback != null) {
                mDuelCallback.onInitList(mDuels);
            }
        }
        return START_STICKY;
    }

    public class WatchBinder extends Binder {
        public WatchService getService() {
            return WatchService.this;
        }
    }

    private CharSequence buildNotificationContent(@NonNull Duel duel) {
        if (duel.getPlayer1Rank() <= 0 && duel.getPlayer2Rank() > 0) {
            return Html.fromHtml(getString(R.string.player1_unranked_notification_content, duel.getPlayer1Name(), duel.getPlayer2Rank(), duel.getPlayer2Name()));
        } else if (duel.getPlayer1Rank() > 0 && duel.getPlayer2Rank() <= 0) {
            return Html.fromHtml(getString(R.string.player2_unranked_notification_content, duel.getPlayer1Rank(), duel.getPlayer1Name(), duel.getPlayer2Name()));
        } else if (duel.getPlayer2Rank() <= 0 && duel.getPlayer1Rank() <= 0) {
            return Html.fromHtml(getString(R.string.unranked_notification_content, duel.getPlayer1Name(), duel.getPlayer2Name()));
        }
        return Html.fromHtml(getString(R.string.def_notification_content, duel.getPlayer1Rank(), duel.getPlayer1Name(), duel.getPlayer2Rank(), duel.getPlayer2Name()));
    }

    private void notifyDuel(@NonNull Duel duel, Notification notification) {
        if (App.config().notifyInSingleId.getValue()) {
            mNotificationManager.notify(DEF_DUEL_NOTIFICATION_ID, notification);
            mCurrentShowedSingleIdDuel = duel.getId();
        } else {
            mDuelNotificationId++;
            mNotificationManager.notify(mDuelNotificationId, notification);
            mNotifiedDuels.put(duel.getId(), mDuelNotificationId);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOnMessageDisposable != null) {
            mOnMessageDisposable.dispose();
        }
        if (mClient != null && !mClient.isClosed()) {
            mNotifyClientClosure = false;
            mClient.close();
        }
        if (mWhiteHotTimer != null) {
            mWhiteHotTimer.cancel();
            mWhiteHotTimer.purge();
        }
        for (CountDownTimer timer : mCountDownTimers) {
            timer.cancel();
        }
    }

    public void setDuelCallback(DuelCallback duelCallback) {
        this.mDuelCallback = duelCallback;
    }

    public interface DuelCallback {
        void onInitList(List<Duel> initialDuels);

        void onDuelCreated(Duel duel);

        void onDuelDeleted(String id);

        void onPlayerRankGot(Duel duel);
    }

    private class MyCardWssClient extends WebSocketClient {
        private static final String EVENT_INIT = "init";
        private static final String EVENT_DELETE = "delete";
        private static final String EVENT_CREATE = "create";
        private final LoadPlayerInfoService mLoadPlayerInfoService;

        public MyCardWssClient(URI serverUri) {
            super(serverUri);
            mLoadPlayerInfoService = Utils.createRetrofit(App.config().duelRankLoadTimeout.getValue()).create(LoadPlayerInfoService.class);
        }

        @Override
        protected void onSetSSLParameters(SSLParameters sslParameters) {
            //https://github.com/TooTallNate/Java-WebSocket/wiki/No-such-method-error-setEndpointIdentificationAlgorithm
            if (Build.VERSION.SDK_INT < 24) {
                return;
            }
            super.onSetSSLParameters(sslParameters);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            mServiceStatus.set(STATUS_OPENED);
        }

        private void loadPlayer1Rank(@NonNull Duel duel, boolean shouldNotify, int time) {
            mLoadPlayerInfoService.loadPlayerInfo(duel.getPlayer1Name()).enqueue(new Utils.CallbackAdapter<Player>() {
                @Override
                public void onResponse(@NonNull Call<Player> call, @NonNull Response<Player> response) {
                    duel.setPlayer1(response.body());
                    notifyWhenLoadComplete(duel, shouldNotify);
                }

                @Override
                public void onFailure(@NonNull Call<Player> call, @NonNull Throwable t) {
                    super.onFailure(call, t);
                    if (time > 0) {
                        //重试
                        loadPlayer1Rank(duel, shouldNotify, time - 1);
                    }
                }
            });
        }

        private void loadPlayer2Rank(@NonNull Duel duel, boolean shouldNotify, int time) {
            mLoadPlayerInfoService.loadPlayerInfo(duel.getPlayer2Name()).enqueue(new Utils.CallbackAdapter<Player>() {
                @Override
                public void onResponse(@NonNull Call<Player> call, @NonNull Response<Player> response) {
                    duel.setPlayer2(response.body());
                    notifyWhenLoadComplete(duel, shouldNotify);
                }

                @Override
                public void onFailure(@NonNull Call<Player> call, @NonNull Throwable t) {
                    super.onFailure(call, t);
                    if (time > 0) {
                        //重试
                        loadPlayer1Rank(duel, shouldNotify, time - 1);
                    }
                }
            });
        }

        private void notifyWhenLoadComplete(@NonNull Duel duel, boolean shouldNotify) {
            if (duel.getPlayer1() != null && duel.getPlayer2() != null) {
                if (mDuelCallback != null) {
                    mDuelCallback.onPlayerRankGot(duel);
                }
                if (shouldNotify) {
                    if (App.config().isDuelInPushCondition(duel)) {
                        int delay = App.config().pushDelayMin.getValue();
                        if (delay > 0) {
                            beginDelayPush(delay, duel);
                        } else {
                            Notification notification = buildWatchNotification(getString(R.string.def_notification_title)
                                    , buildNotificationContent(duel), duel);
                            notifyDuel(duel, notification);
                        }
                    }
                }
            }
        }


        private void loadPlayerRank(@NonNull Duel duel, boolean shouldNotify) {
            loadPlayer1Rank(duel, shouldNotify, App.config().duelRankLoadRetryTimes.getValue());
            loadPlayer2Rank(duel, shouldNotify, App.config().duelRankLoadRetryTimes.getValue());
        }

        @Override
        public void onMessage(String message) {
            mOnMessageDisposable = Flowable.create((FlowableOnSubscribe<Pair<String, Object>>) emitter -> {
                JSONObject jobj = new JSONObject(message);
                String event = jobj.getString("event");
                switch (event) {
                    case EVENT_INIT:
                        JSONArray jarray = new JSONArray(jobj.getString("data"));
                        mDuels = new ArrayList<>();
                        for (int i = 0; i < jarray.length(); i++) {
                            JSONObject data = jarray.getJSONObject(i);
                            Duel duel = new Duel();
                            duel.setId(data.getString("id"));
                            JSONArray players = data.getJSONArray("users");
                            duel.setPlayer1Name(players.getJSONObject(0).getString("username"));
                            duel.setPlayer2Name(players.getJSONObject(1).getString("username"));
                            duel.setOrdinal(mOrdinalOfAllDuels);
                            mOrdinalOfAllDuels++;
                            mDuels.add(duel);
                            loadPlayerRank(duel, false);
                        }
                        emitter.onNext(new Pair<>(event, null));
                        break;
                    case EVENT_DELETE:
                        if (mDuels == null) {
                            return;
                        }
                        String id = jobj.getString("data");
                        if (id.equals(mCurrentWhiteHotDuelId)) {
                            mCurrentWhiteHotDuelId = null;
                        }
                        //取消通知
                        if (mNotifiedDuels.containsKey(id)) {
                            //noinspection ConstantConditions
                            mNotificationManager.cancel(mNotifiedDuels.get(id));
                            mNotifiedDuels.remove(id);
                        }
                        if (id.equals(mCurrentShowedSingleIdDuel)) {
                            mNotificationManager.cancel(DEF_DUEL_NOTIFICATION_ID);
                        }
                        emitter.onNext(new Pair<>(EVENT_DELETE, id));
                        break;
                    case EVENT_CREATE:
                        if (mDuels == null) {
                            return;
                        }
                        //只有当新建了对局才启动白热化检测任务
                        //因为EVENT_INIT获取的对局我们不知道其开始时间
                        if (mWhiteHotTimer == null) {
                            mWhiteHotTimer = new Timer();
                            mWhiteHotTimer.schedule(mWhiteHotChecker, 0L, 5000L);
                        }
                        JSONObject data = jobj.getJSONObject("data");
                        Duel duel = new Duel();
                        duel.setStartTimestamp(System.currentTimeMillis());
                        duel.setId(data.getString("id"));
                        JSONArray players = data.getJSONArray("users");
                        duel.setPlayer1Name(players.getJSONObject(0).getString("username"));
                        duel.setPlayer2Name(players.getJSONObject(1).getString("username"));
                        duel.setOrdinal(mOrdinalOfAllDuels);
                        mOrdinalOfAllDuels++;
                        mDuels.add(duel);
                        emitter.onNext(new Pair<>(event, duel));
                        loadPlayerRank(duel, true);
                        //检查是否有白名单玩家
                        if (App.config().enableWhitelist.getValue()) {
                            String whitelisted = duel.getPlayerName(App.config().isWhitelisted(duel));
                            if (whitelisted != null) {
                                int delay = App.config().pushDelayMin.getValue();
                                if (delay > 0) {
                                    beginDelayPush(delay, duel);
                                } else {
                                    notifyDuel(duel, buildWatchNotification(getString(R.string.whitelisted_def_notification_title, whitelisted),
                                            buildNotificationContent(duel), duel));
                                }
                            }
                        }
                        break;
                }
            }, BackpressureStrategy.BUFFER).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(pair -> {
                                if (mDuelCallback != null) {
                                    switch (pair.first) {
                                        case EVENT_INIT:
                                            mDuelCallback.onInitList(mDuels);
                                            break;
                                        case EVENT_CREATE:
                                            mDuelCallback.onDuelCreated((Duel) pair.second);
                                            break;
                                        case EVENT_DELETE:
                                            mDuelCallback.onDuelDeleted((String) pair.second);
                                            break;
                                    }
                                }
                            }
                    );
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            mServiceStatus.set(STATUS_CLOSED);
            if (remote && App.config().reconnectWhenClosedRemotely.getValue()) {
                mServiceStatus.set(STATUS_CONNECTING);
                Completable.create(emitter -> reconnect()).subscribeOn(Schedulers.newThread()).subscribe();
            }
        }

        @Override
        public void onError(@NonNull Exception ex) {
            ex.printStackTrace();
        }
    }
}
