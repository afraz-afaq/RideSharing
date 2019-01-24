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

public class OfferedFragment extends Fragment {

    private Spinner mOffredOptions;
    private int iOffredOptions=0;

    public OfferedFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_offered, container, false);

        mOffredOptions=rootView.findViewById(R.id.spinner_offeredoptions);
        spinnerOffredOptions();
        return rootView;
    }

    private void spinnerOffredOptions(){

        ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(getActivity(),R.array.bookings_options,android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        mOffredOptions.setAdapter(arrayAdapter);

        mOffredOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selection = (String) adapterView.getItemAtPosition(i);
                if (!TextUtils.isEmpty(selection)){
                    if (selection.equals(getString(R.string.show_all))){
                        iOffredOptions=R.string.show_all;
                    }
                    else if (selection.equals(getString(R.string.past_bookings))){
                        iOffredOptions=R.string.past_bookings;
                    }
                    else if (selection.equals(getString(R.string.today_bookings))){
                        iOffredOptions=R.string.today_bookings;
                    }
                    else if (selection.equals(getString(R.string.canceled_bookings))){
                        iOffredOptions=R.string.canceled_bookings;
                    }
                    else {
                        iOffredOptions=R.string.complete_bookings;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                iOffredOptions=R.string.show_all;

            }
        });

    }

}
