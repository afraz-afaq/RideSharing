package com.example.adeel.ridesharing;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.xml.sax.Parser;

import java.util.ArrayList;

import fcm.androidtoandroid.FirebasePush;
import fcm.androidtoandroid.connection.PushNotificationTask;
import fcm.androidtoandroid.model.Notification;

public class OfferedFragment extends Fragment {

    private String TAG = "OFFERED_FRAG";
    private Spinner mOffredOptions;
    private int iOffredOptions = 0;
    private TextView from, to, distance, time, seats, price;
    TabLayout tabLayout;
    private Button start, cancel;
    private ListView offeredListView;
    private CardView activePost;
    private String postID;
    private FirebaseAuth mAuth;
    private PostHelpingMethod postHelpingMethod;
    private ArrayList<HistoryPost> historyPosts;
    private HistoryAdapter historyPostArrayAdapter;
    DatabaseReference databaseReferenceActive, databaseReference, databaseReferenceNotification, databaseReferenceToken;
    ValueEventListener notificationToToken;
    DatabaseReference getPostData;

    ValueEventListener showActivePost = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.hasChildren()) {

                activePost.setVisibility(View.VISIBLE);
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    postID = snapshot.getKey();
                    from.setText(snapshot.child("Origin").child("name").getValue().toString());
                    to.setText(snapshot.child("Destination").child("name").getValue().toString());
                    seats.setText(snapshot.child("seats").getValue().toString());
                    distance.setText(snapshot.child("distance").getValue().toString() + "KM");
                    time.setText(snapshot.child("departTime").getValue().toString().split("\\s+")[1]);
                    price.setText(snapshot.child("fare").getValue().toString() + "Rs");
                    if (snapshot.child("onway").getValue().toString().equals("false")) {
                        start.setText("START");
                    } else {
                        start.setText("COMPLETED");
                    }
                }
            } else {
                Toast.makeText(getActivity(), "No Active Post", Toast.LENGTH_LONG).show();

                activePost.setVisibility(View.GONE);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };


    ValueEventListener cancelPost = new ValueEventListener() {

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.hasChildren()) {
                Log.v(TAG, "Post Data: " + dataSnapshot.toString());
                FirebaseDatabase.getInstance().getReference().child("Posts").child("Canceled").child(mAuth.getUid()).child(dataSnapshot.getKey()).setValue(dataSnapshot.getValue());
                databaseReference.removeValue();
                databaseReference.removeEventListener(cancelPost);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    ValueEventListener sendNotifications = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot categoriesDataSnapshot) {
            if (categoriesDataSnapshot.hasChildren()) {
                for (DataSnapshot categories : categoriesDataSnapshot.getChildren()) {
                    if (categories.getKey().equals("Pending") || categories.getKey().equals("Active")) {
                        for (final DataSnapshot userID : categories.getChildren()) {
                            Log.v(TAG, userID.toString());
                            databaseReferenceToken = FirebaseDatabase.getInstance().getReference().child("Users").child(userID.getKey()).child("token");
                            notificationToToken = new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    postHelpingMethod.sendNotification("Ride Canceled", "Please find another one.", dataSnapshot.getValue().toString());
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            };
                            databaseReferenceToken.addValueEventListener(notificationToToken);
                            databaseReferenceNotification.removeEventListener(sendNotifications);
                            FirebaseDatabase.getInstance().getReference().child("Find").child(userID.getKey()).child("Pending").removeValue();
                            FirebaseDatabase.getInstance().getReference().child("Find").child(userID.getKey()).child("Active").removeValue();
                        }
                    }
                }

            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    ValueEventListener sendNotificationstostart = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot categoriesDataSnapshot) {
            if (categoriesDataSnapshot.hasChildren()) {
                for (DataSnapshot categories : categoriesDataSnapshot.getChildren()) {
                    if (categories.getKey().equals("Pending") || categories.getKey().equals("Active")) {
                        for (final DataSnapshot userID : categories.getChildren()) {
                            Log.v(TAG, userID.toString());
                            databaseReferenceToken = FirebaseDatabase.getInstance().getReference().child("Users").child(userID.getKey()).child("token");
                            notificationToToken = new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    postHelpingMethod.sendNotification("Ride Started", "Please be on your current location", dataSnapshot.getValue().toString());
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            };
                            databaseReferenceToken.addValueEventListener(notificationToToken);
                            getPostData.removeEventListener(sendNotificationstostart);/*
                            FirebaseDatabase.getInstance().getReference().child("Find").child(userID.getKey()).child("Pending").removeValue();
                            FirebaseDatabase.getInstance().getReference().child("Find").child(userID.getKey()).child("Active").removeValue();*/
                        }
                    }
                }

            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    databaseReference = FirebaseDatabase.getInstance().getReference().child("Posts").child("Active").child(mAuth.getUid()).child(postID);
                    databaseReference.addValueEventListener(cancelPost);
                    databaseReferenceNotification = FirebaseDatabase.getInstance().getReference().child("Requests").child(postID);
                    databaseReferenceNotification.addValueEventListener(sendNotifications);

                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    dialog.dismiss();
                    break;
            }
        }
    };

    public OfferedFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_offered, container, false);
        from = rootView.findViewById(R.id.title_from_address);
        to = rootView.findViewById(R.id.title_to_address);
        distance = rootView.findViewById(R.id.title_pledge);
        time = rootView.findViewById(R.id.title_weight);
        seats = rootView.findViewById(R.id.title_requests_count);
        price = rootView.findViewById(R.id.title_price);
        start = rootView.findViewById(R.id.startRide);
        cancel = rootView.findViewById(R.id.cancelRide);
        tabLayout = new MyBookingFragment().mTabLayout;
        offeredListView = rootView.findViewById(R.id.listView_offered);
        activePost = rootView.findViewById(R.id.activePost);
        mOffredOptions = rootView.findViewById(R.id.spinner_offeredoptions);
        postHelpingMethod = new PostHelpingMethod(getActivity());
        mAuth = FirebaseAuth.getInstance();
        historyPosts = new ArrayList<HistoryPost>();
        historyPostArrayAdapter = new HistoryAdapter(getActivity(), historyPosts);
        offeredListView.setAdapter(historyPostArrayAdapter);


        databaseReferenceActive = FirebaseDatabase.getInstance().getReference().child("Posts").child("Active").child(mAuth.getUid());

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Are you sure you want to cancel this ride?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (start.getText().toString().equals("START")) {
                    Toast.makeText(getActivity(), "Notify All", Toast.LENGTH_SHORT).show();
                    getPostData = FirebaseDatabase.getInstance().getReference().child("Posts").child("Active").child(mAuth.getUid()).child(postID).child("onway");
                    getPostData.setValue("true").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            DatabaseReference ad = FirebaseDatabase.getInstance().getReference().child("Posts").child("Active").child(mAuth.getUid()).child(postID);
                            databaseReference.addValueEventListener(cancelPost);
                            databaseReferenceNotification = FirebaseDatabase.getInstance().getReference().child("Requests").child(postID);
                            databaseReferenceNotification.addValueEventListener(sendNotificationstostart);
                        }
                    });

                    start.setText("COMPLETED");
                } else {
                    Toast.makeText(getActivity(), "Ride Completed", Toast.LENGTH_SHORT).show();
                }

            }
        });
        spinnerOffredOptions();

        return rootView;
    }

    private void spinnerOffredOptions() {

        ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.bookings_options_driver, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        mOffredOptions.setAdapter(arrayAdapter);

        mOffredOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selection = (String) adapterView.getItemAtPosition(i);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.past_bookings))) {
                        iOffredOptions = R.string.past_bookings;
                        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                            @Override
                            public void onTabSelected(TabLayout.Tab tab) {
                                if(tab.getPosition() == 1)
                                    showActive();
                            }

                            @Override
                            public void onTabUnselected(TabLayout.Tab tab) {
                                databaseReferenceActive.removeEventListener(showActivePost);
                                databaseReferenceNotification.removeEventListener(sendNotifications);
                                databaseReference.removeEventListener(cancelPost);
                                databaseReferenceToken.removeEventListener(notificationToToken);
                                databaseReference.removeEventListener(populateValueEventListener);
                            }

                            @Override
                            public void onTabReselected(TabLayout.Tab tab) {

                            }
                        });
                    } else if (selection.equals(getString(R.string.canceled_bookings))) {
                        iOffredOptions = R.string.canceled_bookings;
                        activePost.setVisibility(View.GONE);
                        offeredListView.setVisibility(View.VISIBLE);
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Posts").child("Canceled").child(mAuth.getUid());
                        populateList(databaseReference);
                    } else {
                        iOffredOptions = R.string.complete_bookings;
                        activePost.setVisibility(View.GONE);
                        offeredListView.setVisibility(View.VISIBLE);
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Posts").child("Completed").child(mAuth.getUid());
                        populateList(databaseReference);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                iOffredOptions = R.string.show_all;

            }
        });

    }

    private void showActive() {
        offeredListView.setVisibility(View.GONE);

        databaseReferenceActive.addValueEventListener(showActivePost);

    }

    ValueEventListener populateValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.hasChildren()) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    HistoryPost historyPost = new HistoryPost(snapshot.child("Origin").child("name").getValue().toString(), snapshot.child("Destination").child("name").getValue().toString(), snapshot.child("fare").getValue().toString() + "Rs", snapshot.child("isCar").getValue().toString().equals("true") ? "Car" : "Bike", snapshot.child("vehicle").getValue().toString(), snapshot.child("departTime").getValue().toString());
                    historyPosts.add(historyPost);
                }
            } else {
                Toast.makeText(getActivity(), "No Posts Available", Toast.LENGTH_SHORT).show();
            }
            historyPostArrayAdapter.notifyDataSetChanged();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    private void populateList(DatabaseReference databaseReference) {
        historyPosts.clear();
        databaseReference.addValueEventListener(populateValueEventListener);

    }


    @Override
    public void onDetach() {
        super.onDetach();
        databaseReferenceActive.removeEventListener(showActivePost);
        databaseReferenceNotification.removeEventListener(sendNotifications);
        databaseReference.removeEventListener(cancelPost);
        databaseReferenceToken.removeEventListener(notificationToToken);
        databaseReference.removeEventListener(populateValueEventListener);
    }

}
