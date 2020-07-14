package id.ac.stiki.doleno.mangab.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import id.ac.stiki.doleno.mangab.R;

import id.ac.stiki.doleno.mangab.adapter.HistoryPagerAdapter;

public class HistoryMhsActivity extends AppCompatActivity {

    private TabLayout tlHistory;
    private ViewPager vpHistory;
    private HistoryPagerAdapter historyPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_mhs);

        tlHistory = findViewById(R.id.tlHistory);
        vpHistory = findViewById(R.id.vpHistory);

        tlHistory.addTab(tlHistory.newTab().setText("Recent"));
        tlHistory.addTab(tlHistory.newTab().setText("Summary"));
        tlHistory.setTabGravity(TabLayout.GRAVITY_FILL);

        historyPagerAdapter = new HistoryPagerAdapter(getSupportFragmentManager(), tlHistory.getTabCount());

        vpHistory.setAdapter(historyPagerAdapter);

        tlHistory.setupWithViewPager(vpHistory);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
