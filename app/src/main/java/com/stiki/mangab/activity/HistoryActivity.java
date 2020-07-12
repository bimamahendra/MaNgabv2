package com.stiki.mangab.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.stiki.mangab.R;
import com.stiki.mangab.adapter.HistoryPagerAdapter;

public class HistoryActivity extends AppCompatActivity {

    private TabLayout tlHistory;
    private ViewPager vpHistory;
    private HistoryPagerAdapter historyPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        tlHistory = findViewById(R.id.tlHistory);
        vpHistory = findViewById(R.id.vpHistory);

        tlHistory.addTab(tlHistory.newTab().setText("Recent"));
        tlHistory.addTab(tlHistory.newTab().setText("Summary"));
        tlHistory.setTabGravity(TabLayout.GRAVITY_FILL);

        historyPagerAdapter = new HistoryPagerAdapter(getSupportFragmentManager(), tlHistory.getTabCount());

        vpHistory.setAdapter(historyPagerAdapter);

        tlHistory.setupWithViewPager(vpHistory);
    }

}
