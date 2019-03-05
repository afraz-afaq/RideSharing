package com.example.adeel.ridesharing;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ramotion.foldingcell.FoldingCell;

import java.util.HashSet;
import java.util.List;

/**
 * Simple example of ListAdapter for using with Folding Cell
 * Adapter holds indexes of unfolded elements for correct work with default reusable views behavior
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class FindRideCellAdapter extends ArrayAdapter<FindRideItem> {

    private HashSet<Integer> unfoldedIndexes = new HashSet<>();
    private View.OnClickListener defaultRequestBtnClickListener;

    public FindRideCellAdapter(Context context, List<FindRideItem> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // get item for selected view
        FindRideItem item = getItem(position);
        // if cell is exists - reuse it, if not - create the new one from resource
        FoldingCell cell = (FoldingCell) convertView;
        ViewHolder viewHolder;
        if (cell == null) {
            viewHolder = new ViewHolder();
            LayoutInflater vi = LayoutInflater.from(getContext());
            cell = (FoldingCell) vi.inflate(R.layout.findride_cells, parent, false);
            // binding view parts to view holder
            viewHolder.price = cell.findViewById(R.id.title_price);
            viewHolder.fromAddress = cell.findViewById(R.id.title_from_address);
            viewHolder.toAddress = cell.findViewById(R.id.title_to_address);
            viewHolder.seats = cell.findViewById(R.id.title_requests_count);
            viewHolder.time = cell.findViewById(R.id.title_weight);
            viewHolder.distance = cell.findViewById(R.id.title_pledge);

            viewHolder.price2 = cell.findViewById(R.id.price_mock);
            viewHolder.seats2 = cell.findViewById(R.id.head_image_left_text);
            viewHolder.distance2 = cell.findViewById(R.id.head_image_center_text);
            viewHolder.time2 = cell.findViewById(R.id.head_image_right_text);
            viewHolder.driverName = cell.findViewById(R.id.content_name_view);
            viewHolder.fromAddress2 = cell.findViewById(R.id.content_from_address_1);
            viewHolder.toAddress2 = cell.findViewById(R.id.content_to_address_1);
            viewHolder.carName = cell.findViewById(R.id.content_delivery_time);
            viewHolder.carColor = cell.findViewById(R.id.content_delivery_date);
            viewHolder.carReg = cell.findViewById(R.id.content_deadline_time);
            viewHolder.rating = cell.findViewById(R.id.content_rating_stars);
            viewHolder.imageView = cell.findViewById(R.id.content_avatar);

            viewHolder.contentRequestBtn = cell.findViewById(R.id.content_request_btn);
            cell.setTag(viewHolder);
        } else {
            // for existing cell set valid valid state(without animation)
            if (unfoldedIndexes.contains(position)) {
                cell.unfold(true);
            } else {
                cell.fold(true);
            }
            viewHolder = (ViewHolder) cell.getTag();
        }

        if (null == item)
            return cell;

        // bind data from selected element to view through view holder
        viewHolder.price.setText("₨. "+item.getPrice());
        viewHolder.price2.setText("₨. "+item.getPrice());
        viewHolder.fromAddress.setText(item.getFromAddress());
        viewHolder.toAddress.setText(item.getToAddress());
        viewHolder.fromAddress2.setText(item.getFromAddress());
        viewHolder.toAddress2.setText(item.getToAddress());
        viewHolder.seats.setText(item.getSeats());
        viewHolder.seats2.setText(item.getSeats());
        viewHolder.distance.setText(item.getDistance());
        viewHolder.distance2.setText(item.getDistance());
        viewHolder.time.setText(item.getTime().split("\\s+")[1]);
        viewHolder.time2.setText(item.getTime().split("\\s+")[1]);
        viewHolder.driverName.setText(item.getDriverName());
        viewHolder.carName.setText(item.getCarName());
        viewHolder.carColor.setText(item.getCarColor());
        viewHolder.carReg.setText(item.getCarReg());
        viewHolder.rating.setText(item.getRating());
        Log.v("Tesing","-> "+item.getImage());
        GlideApp.with(getContext()).load(item.getImage()).into(viewHolder.imageView);


        // set custom btn handler for list item from that item
        if (item.getRequestBtnClickListener() != null) {
            viewHolder.contentRequestBtn.setOnClickListener(item.getRequestBtnClickListener());
        } else {
            // (optionally) add "default" handler if no handler found in item
            viewHolder.contentRequestBtn.setOnClickListener(defaultRequestBtnClickListener);
        }

        return cell;
    }

    // simple methods for register cell state changes
    public void registerToggle(int position) {
        if (unfoldedIndexes.contains(position))
            registerFold(position);
        else
            registerUnfold(position);
    }

    public void registerFold(int position) {
        unfoldedIndexes.remove(position);
    }

    public void registerUnfold(int position) {
        unfoldedIndexes.add(position);
    }

    public View.OnClickListener getDefaultRequestBtnClickListener() {
        return defaultRequestBtnClickListener;
    }

    public void setDefaultRequestBtnClickListener(View.OnClickListener defaultRequestBtnClickListener) {
        this.defaultRequestBtnClickListener = defaultRequestBtnClickListener;
    }

    // View lookup cache
    private static class ViewHolder {
        TextView price;
        TextView contentRequestBtn;
        TextView seats;
        TextView fromAddress;
        TextView toAddress;
        TextView distance;
        TextView time;
        TextView price2;
        TextView seats2;
        TextView distance2;
        TextView driverName;
        TextView fromAddress2;
        TextView toAddress2;
        TextView carName;
        TextView carColor;
        TextView carReg;
        TextView time2;
        TextView rating;
        ImageView imageView;

    }
}
