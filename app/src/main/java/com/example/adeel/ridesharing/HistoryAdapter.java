package com.example.adeel.ridesharing;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Afraz on 2/11/2019.
 */

public class HistoryAdapter extends ArrayAdapter<HistoryPost> {

    private Activity context;
    ArrayList<HistoryPost> posts;

    public HistoryAdapter(Activity context, ArrayList<HistoryPost> posts) {
        super(context, R.layout.history_post_layout, posts);
        this.context = context;
        this.posts = posts;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.history_post_layout, null, true);



        TextView from = (TextView) listViewItem.findViewById(R.id.title_from_address);
        TextView to = (TextView) listViewItem.findViewById(R.id.title_to_address);
        TextView price = (TextView) listViewItem.findViewById(R.id.title_price);
        TextView via = (TextView) listViewItem.findViewById(R.id.title_requests_count);
        TextView vehicle = (TextView) listViewItem.findViewById(R.id.title_pledge);
        TextView date = (TextView) listViewItem.findViewById(R.id.title_weight);
        TextView name = (TextView) listViewItem.findViewById(R.id.driverName);
        TextView regno = (TextView) listViewItem.findViewById(R.id.regno);

        //TextView mComfortLevel = (TextView) listViewItem.findViewById(R.id.textView_comfortLevel);
        HistoryPost historyPost = posts.get(position);
        from.setText(historyPost.getFrom());
        to.setText(historyPost.getTo());
        price.setText(historyPost.getPrice());
        via.setText(historyPost.getVia());
        vehicle.setText(historyPost.getVehicle());
        date.setText(historyPost.getDate());
        name.setText(historyPost.getDriver());
        regno.setText(historyPost.getRegno());

        return listViewItem;
    }

}
