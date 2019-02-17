package com.example.adeel.ridesharing;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class PendingRequestAdapter extends ArrayAdapter<Request> {

    private Activity context;
    public PendingRequestAdapter(Activity context, ArrayList<Request> requests) {
        super(context, 0, requests);
        this.context = context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.pendingreuest_items, null, true);



        TextView name = listViewItem.findViewById(R.id.pending_name);
        TextView loc = listViewItem.findViewById(R.id.Loc);
        TextView seats = listViewItem.findViewById(R.id.textView_seats);
        Button confirm = listViewItem.findViewById(R.id.button_confirm);
        Button cancel = listViewItem.findViewById(R.id.button_cancel);

        //TextView mComfortLevel = (TextView) listViewItem.findViewById(R.id.textView_comfortLevel);
        Request req = getItem(position);
        name.setText(req.getmName());
        loc.setText(req.getmLoc());
        seats.setText(req.getmSeats());
        confirm.setOnClickListener(req.getAcceptOnClickListener());
        cancel.setOnClickListener(req.getCancelOnClickListener());

        return listViewItem;
    }



}
