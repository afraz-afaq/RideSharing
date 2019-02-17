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
public class RequestFragment extends Fragment {
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private RequestPagerAdapter mAdapter;


    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.fragment_request, container, false);

        mTabLayout = mView.findViewById(R.id.reuest_tabLayout);
        mViewPager = mView.findViewById(R.id.request_viewPager);

        mAdapter = new RequestPagerAdapter(getFragmentManager());
        mAdapter.AddFragment(new PendingRequestsFragment(),"Requests");
        mAdapter.AddFragment(new AcceptedRequestsFragment(),"Accepted");

        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        return mView;
    }

}
