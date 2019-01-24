package com.example.adeel.ridesharing;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MyBookingFragment extends Fragment {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private BookingPagerAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_mybookings, container, false);
        mTabLayout = rootView.findViewById(R.id.tabLayout);
        mViewPager = rootView.findViewById(R.id.viewPager);

        mAdapter = new BookingPagerAdapter(getFragmentManager());
        mAdapter.AddFragment(new BookedFragment(),"Booked");
        mAdapter.AddFragment(new OfferedFragment(),"Offered");

        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        return rootView;
    }

}
