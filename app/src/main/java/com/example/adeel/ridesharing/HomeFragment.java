package com.example.adeel.ridesharing;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import static com.example.adeel.ridesharing.MainActivity.mNavigationView;
import static com.example.adeel.ridesharing.MainActivity.mtoolbar;

public class HomeFragment extends Fragment {
    private Button mFindride,mOfferride;
    PreferencesClass preferencesClass;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        mFindride = rootView.findViewById(R.id.button_findRide);
        mOfferride = rootView.findViewById(R.id.button_offerRide);
        preferencesClass = new PreferencesClass(getActivity());
        mFindride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (preferencesClass.getUserPostStatus().equals("false")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Please complete or cancel your current ride!").setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    dialog.dismiss();

                            }
                        }
                    }).show();
                } else {
                    getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new FindRideFragment()).commit();
                    mNavigationView.setCheckedItem(R.id.nav_findRide);
                    mtoolbar.setTitle(R.string.find_ride);
                }
            }
        });
        mOfferride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (preferencesClass.getUserPostStatus().equals("false")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Please complete or cancel your current ride!").setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    dialog.dismiss();

                            }
                        }
                    }).show();
                } else {

                    //checkCars();
                    getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new OfferRideFragment()).commit();
                    mNavigationView.setCheckedItem(R.id.nav_offferRide);
                    mtoolbar.setTitle(R.string.offer_ride);
                }
            }
        });



        return rootView;
    }
}
