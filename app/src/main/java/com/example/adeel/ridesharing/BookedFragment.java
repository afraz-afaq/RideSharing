package com.example.adeel.ridesharing;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.EventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class BookedFragment extends Fragment {

    private String TAG = "BOOKED_FRAG";
    private Spinner mBookedOptions;
    private int iBookedOptions=0;
    private TextView from, to, distance, time, seats, price, name, noPostMsg;
    private CircleImageView circleImageView;
    private Button cancel,track,chat,call;
    private ListView bookedListView;
    private CardView pendingPost;
    private View rootView;
    private String postID, driverId, driverName, contact;
    private FirebaseAuth mAuth;
    private StorageReference storageReference;
    private PostHelpingMethod postHelpingMethod;
    private ArrayList<HistoryPost> historyPosts;
    private HistoryAdapter historyPostArrayAdapter;
    private DatabaseReference databaseReferencePopulateList;
    private DatabaseReference getPostData , databaseReferencePending, databaseReferenceActive, databaseReference, databaseReferenceNotification, databaseReferenceToken;
    private ValueEventListener  getPostDataValueEventListener, getFindPendingToCancelValueEventListener;
    private ProgressDialog loadingDialog;

    ValueEventListener showPendingPost = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            noPostMsg.setVisibility(View.GONE);
            bookedListView.setVisibility(View.GONE);
            circleImageView.setVisibility(View.GONE);
            track.setVisibility(View.GONE);
            chat.setVisibility(View.GONE);
            call.setVisibility(View.GONE);
            if (dataSnapshot.hasChildren()) {
                pendingPost.setVisibility(View.VISIBLE);
                cancel.setVisibility(View.VISIBLE);
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    postID = snapshot.getKey();
                    driverId = snapshot.child("driver").getValue().toString();
                    Log.v(TAG,"Driver: "+driverId);

                    getPostData = FirebaseDatabase.getInstance().getReference().child("Posts").child("Active").child(driverId).child(postID);
                    getPostData.keepSynced(true);
                    getPostDataValueEventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChildren()) {
                                from.setText(dataSnapshot.child("Origin").child("name").getValue().toString());
                                to.setText(dataSnapshot.child("Destination").child("name").getValue().toString());
                                seats.setText(dataSnapshot.child("seats").getValue().toString());
                                distance.setText(dataSnapshot.child("distance").getValue().toString() + "KM");
                                time.setText(dataSnapshot.child("departTime").getValue().toString().split("\\s+")[1]);
                                price.setText(dataSnapshot.child("fare").getValue().toString() + "Rs");
                                name.setText(dataSnapshot.child("name").getValue().toString());
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            loadingDialog.dismiss();
                            noPostMsg.setVisibility(View.VISIBLE);
                        }
                    };
                    getPostData.addValueEventListener(getPostDataValueEventListener);

                }
            } else {
//                Toast.makeText(getActivity(), "No Pending Post", Toast.LENGTH_LONG).show();
                noPostMsg.setVisibility(View.VISIBLE);
                pendingPost.setVisibility(View.GONE);
            }
            loadingDialog.dismiss();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            loadingDialog.dismiss();
            noPostMsg.setVisibility(View.VISIBLE);
        }
    };

    ValueEventListener showActivePost = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            bookedListView.setVisibility(View.GONE);
            cancel.setVisibility(View.GONE);
            noPostMsg.setVisibility(View.GONE);
            if (dataSnapshot.hasChildren()) {
                pendingPost.setVisibility(View.VISIBLE);
                circleImageView.setVisibility(View.VISIBLE);
                track.setVisibility(View.VISIBLE);
                chat.setVisibility(View.VISIBLE);
                call.setVisibility(View.VISIBLE);

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    postID = snapshot.getKey();
                    driverId = snapshot.child("driver").getValue().toString();
                    Log.v(TAG,"Driver: "+driverId);

                    storageReference.child(driverId).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(final Uri uri) {
                            //GlideApp.with(getContext()).load(uri.toString()).placeholder(R.drawable.avatar).into(circleImageView);
                            Picasso.with(getContext()).load(uri.toString()).networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(R.drawable.avatar).into(circleImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(getContext()).load(uri.toString()).placeholder(R.drawable.avatar).into(circleImageView);

                                }
                            });

                        }

                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            postHelpingMethod.snackbarMessage("Network Error "+e.getMessage(),rootView);
                        }
                    });
                    getPostData = FirebaseDatabase.getInstance().getReference().child("Posts").child("Active").child(driverId).child(postID);
                    getPostData.keepSynced(true);
                    getPostDataValueEventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChildren()) {
                                from.setText(dataSnapshot.child("Origin").child("name").getValue().toString());
                                to.setText(dataSnapshot.child("Destination").child("name").getValue().toString());
                                seats.setText(dataSnapshot.child("seats").getValue().toString());
                                distance.setText(dataSnapshot.child("distance").getValue().toString() + "KM");
                                time.setText(dataSnapshot.child("departTime").getValue().toString().split("\\s+")[1]);
                                price.setText(dataSnapshot.child("fare").getValue().toString() + "Rs");
                                name.setText(dataSnapshot.child("name").getValue().toString());
                                getPostData.removeEventListener(getPostDataValueEventListener);
                                driverName = dataSnapshot.child("name").getValue().toString();
                                contact = dataSnapshot.child("contact").getValue().toString();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            loadingDialog.dismiss();
                            noPostMsg.setVisibility(View.VISIBLE);
                        }
                    };
                    getPostData.addValueEventListener(getPostDataValueEventListener);

                }
            } else {
//                Toast.makeText(getActivity(), "No Pending Post", Toast.LENGTH_LONG).show();
                noPostMsg.setVisibility(View.VISIBLE);
                pendingPost.setVisibility(View.GONE);
            }
            loadingDialog.dismiss();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            loadingDialog.dismiss();
            noPostMsg.setVisibility(View.VISIBLE);
        }

    };



    public BookedFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_booked, container, false);
        circleImageView = rootView.findViewById(R.id.dimage);
        from = rootView.findViewById(R.id.title_from_address);
        name = rootView.findViewById(R.id.dname);
        to = rootView.findViewById(R.id.title_to_address);
        distance = rootView.findViewById(R.id.title_pledge);
        time = rootView.findViewById(R.id.title_weight);
        seats = rootView.findViewById(R.id.title_requests_count);
        price = rootView.findViewById(R.id.title_price);
        cancel = rootView.findViewById(R.id.cancelRide);
        track = rootView.findViewById(R.id.trackRide);
        chat = rootView.findViewById(R.id.chatRide);
        call = rootView.findViewById(R.id.phoneRide);
        noPostMsg = rootView.findViewById(R.id.noPostMsg);
        bookedListView = rootView.findViewById(R.id.listView_booked);
        pendingPost = rootView.findViewById(R.id.pendingPost);
        mBookedOptions=rootView.findViewById(R.id.spinner_bookedoptions);
        postHelpingMethod = new PostHelpingMethod(getActivity());
        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        historyPosts = new ArrayList<HistoryPost>();
        historyPostArrayAdapter = new HistoryAdapter(getActivity(), historyPosts);
        bookedListView.setAdapter(historyPostArrayAdapter);
        loadingDialog = postHelpingMethod.createProgressDialog("Loading...","Please Wait.");
        spinnerBookedOptions();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference getFindPendingToCancel = FirebaseDatabase.getInstance().getReference().child("Find").child(mAuth.getUid()).child("Pending").child(postID);
                getFindPendingToCancelValueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        FirebaseDatabase.getInstance().getReference().child("Find").child(mAuth.getUid()).child("Canceled").child(postID).setValue(dataSnapshot.getValue());
                        getFindPendingToCancel.removeEventListener(getFindPendingToCancelValueEventListener);
                        getFindPendingToCancel.removeValue();
                        FirebaseDatabase.getInstance().getReference().child("Requests").child(postID).child("Pending").child(mAuth.getUid()).removeValue();
                        FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getUid()).child("poststatus").setValue("true");
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                };

                getFindPendingToCancel.addValueEventListener(getFindPendingToCancelValueEventListener);
            }
        });

        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),ChatActivity.class);
                intent.putExtra("driverId",driverId);
                intent.putExtra("name",driverName);
                startActivity(intent);
            }
        });

        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                String phone = "tel:"+contact;
                intent.setData(Uri.parse(phone));
                if (ActivityCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(intent);
                }
            }
        });

        track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),MapsActivity.class);
                intent.putExtra("driverId",driverId);
                intent.putExtra("driver","false");
                startActivity(intent);
            }
        });
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
                            iBookedOptions = R.string.past_bookings;
                            showActive();
                    }
                    else if (selection.equals(getString(R.string.today_bookings))){
                        iBookedOptions=R.string.today_bookings;
                        showPending();
                    }
                    else if (selection.equals(getString(R.string.canceled_bookings))){
                        iBookedOptions=R.string.canceled_bookings;
                        databaseReferencePopulateList = FirebaseDatabase.getInstance().getReference().child("Find").child(mAuth.getUid()).child("Canceled");
                        populateList(databaseReferencePopulateList);
                    }
                    else {
                        iBookedOptions=R.string.complete_bookings;
                        databaseReferencePopulateList = FirebaseDatabase.getInstance().getReference().child("Find").child(mAuth.getUid()).child("Completed");
                        populateList(databaseReferencePopulateList);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                iBookedOptions=R.string.show_all;

            }
        });

    }

    private void showPending() {
        loadingDialog.show();
        databaseReferencePending = FirebaseDatabase.getInstance().getReference().child("Find").child(mAuth.getUid()).child("Pending");
        databaseReferencePending.addValueEventListener(showPendingPost);
    }

    private void showActive() {
        loadingDialog.show();
        databaseReferenceActive = FirebaseDatabase.getInstance().getReference().child("Find").child(mAuth.getUid()).child("Active");
        databaseReferenceActive.addValueEventListener(showActivePost);
    }



    ValueEventListener populateValueEventListener =  new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            noPostMsg.setVisibility(View.GONE);
            pendingPost.setVisibility(View.GONE);
            if (dataSnapshot.hasChildren()) {
                bookedListView.setVisibility(View.VISIBLE);
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    postID = snapshot.getKey();
                    driverId = snapshot.child("driver").getValue().toString();
                    Log.v(TAG,"Driver: "+driverId);

                    getPostData = FirebaseDatabase.getInstance().getReference().child("Posts").child("Completed").child(driverId).child(postID);
                    getPostDataValueEventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.hasChildren()) {
                                HistoryPost historyPost = new HistoryPost(snapshot.child("Origin").child("name").getValue().toString(), snapshot.child("Destination").child("name").getValue().toString(),  "Rs. "+snapshot.child("fare").getValue().toString(), snapshot.child("isCar").getValue().toString().equals("true") ? "Car" : "Bike", snapshot.child("vehicle").getValue().toString(), snapshot.child("departTime").getValue().toString(),snapshot.child("name").getValue().toString(),snapshot.child("regno").getValue().toString());
                                historyPosts.add(historyPost);
                                historyPostArrayAdapter.notifyDataSetChanged();
                            }
                            else{
                                getPostData.removeEventListener(getPostDataValueEventListener);
                                getPostData = FirebaseDatabase.getInstance().getReference().child("Posts").child("Canceled").child(driverId).child(postID);
                                getPostData.addValueEventListener(getPostDataValueEventListener);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    };
                    getPostData.addValueEventListener(getPostDataValueEventListener);
                }
            } else {
//                Toast.makeText(getActivity(), "No Posts Available", Toast.LENGTH_LONG).show();
                noPostMsg.setVisibility(View.VISIBLE);
                pendingPost.setVisibility(View.GONE);
            }

loadingDialog.dismiss();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    private void populateList(DatabaseReference databaseReference) {
        loadingDialog.show();
        historyPosts.clear();
        databaseReference.addValueEventListener(populateValueEventListener);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (databaseReferencePending != null){
            databaseReferencePending.removeEventListener(showPendingPost);
        }
        if (databaseReferenceActive != null){
            databaseReferenceActive.removeEventListener(showActivePost);
        }
        if (getPostData != null){
            getPostData.removeEventListener(getPostDataValueEventListener);
        }
        if (databaseReferencePopulateList != null){
            databaseReferencePopulateList.removeEventListener(populateValueEventListener);
        }

    }
}
