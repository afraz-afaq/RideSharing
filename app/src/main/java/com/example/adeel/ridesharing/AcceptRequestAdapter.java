package com.example.adeel.ridesharing;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class AcceptRequestAdapter extends ArrayAdapter<Accept> {
    private StorageReference mStorageRef;
    private Activity context;
    public AcceptRequestAdapter(Activity context, ArrayList<Accept> accepts) {
        super(context, 0, accepts);
        this.context = context;

    }

    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        final View listViewItem = inflater.inflate(R.layout.acceptedrequest_items, null, true);


        TextView name = listViewItem.findViewById(R.id.person_name);
        TextView loc = listViewItem.findViewById(R.id.person_loc);
        TextView seats = listViewItem.findViewById(R.id.person_seats);
        Button track = listViewItem.findViewById(R.id.track_button);
        Button chat = listViewItem.findViewById(R.id.caht_button);
        Button call = listViewItem.findViewById(R.id.call_button);
        final CircleImageView imageView = listViewItem.findViewById(R.id.person_image);
        //TextView mComfortLevel = (TextView) listViewItem.findViewById(R.id.textView_comfortLevel);
        Accept accept = getItem(position);
        name.setText(accept.getmName());
        loc.setText(accept.getmLoc());
        seats.setText(accept.getmSeats());
        call.setOnClickListener(accept.getCallOnClickListener());
        track.setOnClickListener(accept.getTrackOnClickListener());
        chat.setOnClickListener(accept.getChatOnClickListener());
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mStorageRef.child(accept.getmImage()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(final Uri uri) {
                //GlideApp.with(getContext()).load(uri.toString()).placeholder(R.drawable.avatar).into(imageView);
                Picasso.with(getContext()).load(uri.toString()).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.avatar).into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(getContext()).load(uri.toString()).placeholder(R.drawable.avatar).into(imageView);

                    }
                });

            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar sb = Snackbar.make(listViewItem, "Network Error "+e.getMessage(), Snackbar.LENGTH_LONG);
                sb.show();
            }
        });

        return listViewItem;
    }

}
