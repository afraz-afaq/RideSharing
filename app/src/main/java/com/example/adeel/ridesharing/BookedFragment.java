package com.example.adeel.ridesharing;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class BookedFragment extends Fragment {

    private Spinner mBookedOptions;
    private int iBookedOptions=0;

    public BookedFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_booked, container, false);

        mBookedOptions=rootView.findViewById(R.id.spinner_bookedoptions);
        spinnerBookedOptions();
        return rootView;
    }

    private void spinnerBookedOptions(){

        ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(getActivity(),R.array.bookings_options,android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        mBookedOptions.setAdapter(arrayAdapter);

        mBookedOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selection = (String) adapterView.getItemAtPosition(i);
                if (!TextUtils.isEmpty(selection)){
                    if (selection.equals(getString(R.string.past_bookings))){
                        iBookedOptions=R.string.past_bookings;
                    }
                    else if (selection.equals(getString(R.string.today_bookings))){
                        iBookedOptions=R.string.today_bookings;
                    }
                    else if (selection.equals(getString(R.string.canceled_bookings))){
                        iBookedOptions=R.string.canceled_bookings;
                    }
                    else {
                        iBookedOptions=R.string.complete_bookings;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                iBookedOptions=R.string.show_all;

            }
        });

    }

}
