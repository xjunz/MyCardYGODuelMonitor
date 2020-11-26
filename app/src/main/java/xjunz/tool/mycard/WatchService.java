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

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
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
    private final CompositeDisposable mDisposables = new CompositeDisposable();
    private DuelCallback mDuelCallback;
    private NotificationManager mNotificationManager;
    private List<CountDownTimer> mCountDownTimers;
    private final ObservableInt mServiceStatus = new ObservableInt(STATUS_CLOSED);
    private boolean mNotifyClientClosure = true;
    public static final int STATUS_CLOSED = -1;
    public static final int STATUS_CONNECTING = 0;
    public static final int STATUS_OPENED = 1;
    private static final int FOREGROUND_NOTIFICATION_ID = 814;
    private static final int DEF_DUEL_NOTIFICATION_ID = 1999;
    private int mDuelNotificationId = DEF_DUEL_NOTIFICATION_ID;
    private HashMap<String, Integer> mNotifiedDuels;
    private String FOREGROUND_CHANNEL_ID;
    private String PUSH_CHANNEL_ID;
    private Timer mWhiteHotTimer;
    private final TimerTask mWhiteHotChecker = new TimerTask() {
        @Override
        public void run() {
            if (App.config().pushWhiteHot.getValue()) {
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
                    notifyDuel(candidate, buildWatchNotification(getString(R.string.new_white_hot)
                            , Html.fromHtml(getResources().getString(R.string.def_notification_content, candidate.getPlayer1Rank(), candidate.getPlayer1Name(), candidate.getPlayer2Rank(), candidate.getPlayer2Name()))
                            , candidate.getId()));
                }
            }
        }
    };

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

    private int getDuelNotificationId() {
        if (App.config().notifyInSingleId.getValue()) {
            return DEF_DUEL_NOTIFICATION_ID;
        } else {
            mDuelNotificationId++;
            return mDuelNotificationId;
        }
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
                if (mDuels.contains(duel)) {
                    mCountDownTimers.remove(this);
                    Notification notification = buildWatchNotification(
                            getResources().getString(R.string.delayed_notification_title, min)
                            , Html.fromHtml(getResources().getString(R.string.def_notification_content, duel.getPlayer1Rank(), duel.getPlayer1Name(), duel.getPlayer2Rank(), duel.getPlayer2Name()))
                            , duel.getId());
                    notifyDuel(duel, notification);
                }
            }
        }.start();
        mCountDownTimers.add(timer);
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

    private Notification buildWatchNotification(CharSequence title, CharSequence content, String duelId) {
        Intent intent;
        if (!App.config().hasCompleteWatchConfig()) {
            intent = new Intent(this, WatchSetupActivity.class);
            intent.putExtra(WatchSetupActivity.EXTRA_DUEL_ID, duelId);
        } else {
            intent = Utils.buildLaunchWatchIntent(duelId);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, mDuelNotificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext());
        long timestamp = System.currentTimeMillis();
        builder.setContentTitle(title)
                .setSmallIcon(R.drawable.ic_baseline_local_fire_department_24)
                .setContentText(content)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setSubText(Utils.formatDate(getResources().getConfiguration().locale, timestamp))
                .setWhen(timestamp);
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

    private void notifyDuel(@NonNull Duel duel, Notification notification) {
        int id = getDuelNotificationId();
        mNotificationManager.notify(id, notification);
        mNotifiedDuels.put(duel.getId(), id);
    }

    private class MyCardWssClient extends WebSocketClient {
        private static final String EVENT_INIT = "init";
        private static final String EVENT_DELETE = "delete";
        private static final String EVENT_CREATE = "create";

        public MyCardWssClient(URI serverUri) {
            super(serverUri);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            mServiceStatus.set(STATUS_OPENED);
        }

        private void loadPlayer1Rank(@NonNull Duel duel, boolean shouldNotify, int time) {
            Utils.createRetrofit(App.config().duelRankLoadTimeout.getValue()).create(LoadPlayerInfoService.class).loadPlayerInfo(duel.getPlayer1Name()).enqueue(new Utils.CallbackAdapter<Player>() {
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
            Utils.createRetrofit(App.config().duelRankLoadTimeout.getValue()).create(LoadPlayerInfoService.class).loadPlayerInfo(duel.getPlayer2Name()).enqueue(new Utils.CallbackAdapter<Player>() {
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
                int index = mDuels.indexOf(duel);
                if (index >= 0) {
                    if (mDuelCallback != null) {
                        mDuelCallback.onPlayerRankGot(index);
                    }
                    if (shouldNotify) {
                        if (App.config().isDuelInPushCondition(duel)) {
                            int delay = App.config().pushDelayMin.getValue();
                            if (delay > 0) {
                                beginDelayPush(delay, duel);
                            } else {
                                Notification notification = buildWatchNotification(
                                        getResources().getString(R.string.def_notification_title)
                                        , Html.fromHtml(getResources().getString(R.string.def_notification_content, duel.getPlayer1Rank(), duel.getPlayer1Name(), duel.getPlayer2Rank(), duel.getPlayer2Name()))
                                        , duel.getId());
                                notifyDuel(duel, notification);
                            }
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
            mDisposables.add(Single.create((SingleOnSubscribe<Pair<String, Integer>>) emitter -> {
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
                                    mDuels.add(duel);
                                    loadPlayerRank(duel, false);
                                }
                                emitter.onSuccess(new Pair<>(event, null));
                                break;
                            case EVENT_DELETE:
                                if (mDuels == null) {
                                    return;
                                }
                                String id = jobj.getString("data");
                                //取消通知
                                if (mNotifiedDuels.containsKey(id)) {
                                    mNotificationManager.cancel(mNotifiedDuels.get(id));
                                    mNotifiedDuels.remove(id);
                                }
                                for (int i = 0; i < mDuels.size(); i++) {
                                    if (id.equals(mDuels.get(i).getId())) {
                                        mDuels.remove(i);
                                        emitter.onSuccess(new Pair<>(event, i));
                                        return;
                                    }
                                }
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
                                mDuels.add(duel);
                                emitter.onSuccess(new Pair<>(event, null));
                                loadPlayerRank(duel, true);
                                break;
                        }
                    }).subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(pair -> {
                                if (mDuelCallback != null) {
                                    switch (pair.first) {
                                        case EVENT_INIT:
                                            mDuelCallback.onInitList(mDuels);
                                            break;
                                        case EVENT_CREATE:
                                            mDuelCallback.onDuelCreated(mDuels.size() - 1);
                                            break;
                                        case EVENT_DELETE:
                                            mDuelCallback.onDuelDeleted(pair.second);
                                            break;
                                    }
                                }
                            })
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

    public interface DuelCallback {
        void onInitList(List<Duel> initialDuels);

        void onDuelCreated(int index);

        void onDuelDeleted(int index);

        void onPlayerRankGot(int index);
    }

    public void setDuelCallback(DuelCallback duelCallback) {
        this.mDuelCallback = duelCallback;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mDisposables.dispose();
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
}
