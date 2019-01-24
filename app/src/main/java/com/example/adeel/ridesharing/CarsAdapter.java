/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.adeel.ridesharing;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class CarsAdapter extends ArrayAdapter<Cars> {
    private Activity context;
    List<Cars> cars;

    public CarsAdapter(Activity context, List<Cars> cars) {
        super(context, R.layout.mycarlist_item, cars);
        this.context = context;
        this.cars = cars;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.mycarlist_item, null, true);



        TextView mCarName = (TextView) listViewItem.findViewById(R.id.textView_carName);
       // TextView mNoOfSeats = (TextView) listViewItem.findViewById(R.id.textView_noOfSeats);
        TextView mRegNo = (TextView) listViewItem.findViewById(R.id.textView_regNo);
        TextView mColor = (TextView) listViewItem.findViewById(R.id.textView_color);
        //TextView mComfortLevel = (TextView) listViewItem.findViewById(R.id.textView_comfortLevel);
        Cars carss = cars.get(position);
        mRegNo.setText(carss.getmCarRegNo());
        mCarName.setText(carss.getmCarName());
        mColor.setText(carss.getmCarColor());

        return listViewItem;
    }
}
