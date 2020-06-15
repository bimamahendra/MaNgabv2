package com.stiki.mangab.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.stiki.mangab.fragment.RecentHistoryFragment;
import com.stiki.mangab.fragment.SummaryHistoryFragment;

public class HistoryPagerAdapter extends FragmentStatePagerAdapter {

    //integer to count number of tabs
    private int tabCount;

    public HistoryPagerAdapter(@NonNull FragmentManager fm, int tabCount) {
        super(fm);
        this.tabCount= tabCount;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new RecentHistoryFragment();
            case 1:
                return new SummaryHistoryFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Recent";
            case 1:
                return "Summary";
            default:
                return null;
        }
    }
}
