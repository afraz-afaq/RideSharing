package com.example.adeel.ridesharing;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyVehicles extends Fragment {


    public MyVehicles() {
        // Required empty public constructor
    }
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private BookingPagerAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_my_vehicles, container, false);
        mTabLayout = rootView.findViewById(R.id.tabLayout);
        mViewPager = rootView.findViewById(R.id.viewPager);

        mAdapter = new BookingPagerAdapter(getFragmentManager());
        mAdapter.AddFragment(new MyCarsFragment(),"Cars");
        mAdapter.AddFragment(new MyBikesFragment(),"Bikes");

        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        return rootView;
    }

}
