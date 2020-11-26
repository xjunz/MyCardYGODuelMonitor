/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import xjunz.tool.mycard.R;
import xjunz.tool.mycard.WatchService;
import xjunz.tool.mycard.databinding.ActivityMainBinding;
import xjunz.tool.mycard.ui.fragment.RankFragment;
import xjunz.tool.mycard.ui.fragment.WatchFragment;

public class MainActivity extends AppCompatActivity {

    private WatchFragment mWatchFragment;
    private RankFragment mRankFragment;
    private ActivityMainBinding mBinding;
    private boolean mIsServiceConnected;
    private final ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WatchService.WatchBinder binder = (WatchService.WatchBinder) service;
            binder.getService().setDuelCallback(mWatchFragment);
            mBinding.setStatus(binder.getService().getServiceStatus());
            mIsServiceConnected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIsServiceConnected = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        initViews();
        initPages();
        bindService(new Intent(this, WatchService.class), mConn, BIND_AUTO_CREATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, WatchService.class));
        } else {
            startService(new Intent(this, WatchService.class));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.item_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (item.getItemId() == R.id.item_about) {
            startActivity(new Intent(this, AboutActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }


    private void initViews() {
        mViewPager = findViewById(R.id.view_pager);
        mIbWatch = findViewById(R.id.ib_watch);
        mIbRank = findViewById(R.id.ib_rank);
    }

    private ViewPager2 mViewPager;
    private ImageButton mIbWatch, mIbRank;

    private void initPages() {
        mWatchFragment = new WatchFragment();
        mRankFragment = new RankFragment();
        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mIbWatch.setSelected(position == 0);
                mIbRank.setSelected(position == 1);
                setTitle(position == 0 ? R.string.watch_list : R.string.rank_list);
            }
        });
        PageAdapter adapter = new PageAdapter(this);
        mViewPager.setAdapter(adapter);
    }

    public void gotoPage1(View view) {
        mViewPager.setCurrentItem(0, true);
    }

    public void gotoPage2(View view) {
        mViewPager.setCurrentItem(1, true);
    }


    private class PageAdapter extends FragmentStateAdapter {

        public PageAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return position == 0 ? mWatchFragment : mRankFragment;
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }


    public void switchService(View view) {
        int statusCode = mBinding.getStatus().get();
        if (statusCode == 1) {
            if (mIsServiceConnected) {
                unbindService(mConn);
            }
            stopService(new Intent(this, WatchService.class));
        } else if (statusCode == -1) {
            bindService(new Intent(this, WatchService.class), mConn, BIND_AUTO_CREATE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(this, WatchService.class));
            } else {
                startService(new Intent(this, WatchService.class));
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mIsServiceConnected) {
            unbindService(mConn);
        }
    }
}