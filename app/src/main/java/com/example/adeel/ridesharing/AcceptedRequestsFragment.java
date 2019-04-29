package com.example.adeel.ridesharing;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import static com.example.adeel.ridesharing.FindRideFragment.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class AcceptedRequestsFragment extends Fragment {
    private ArrayList<Accept> acceptArrayList;
    private ListView acceptList;
    private AcceptRequestAdapter acceptRequestAdapter;
    private DatabaseReference databaseReferenceActive, databaseReferencePendingList;
    private FirebaseAuth mAuth;
    private Button track_Button, call_Button, chat_Button;
    private String postId = "";
    private TextView noPostMsg;
    private View view;
    private ProgressDialog loadingDialog;
    PostHelpingMethod postHelpingMethod;
    String uID,numToCall;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final String PHONE_CALL = Manifest.permission.CALL_PHONE;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final int CALL_PERMISSION_REQUEST_CODE = 5678;
    ValueEventListener getTokenForCancelValueEventListener;
    private ValueEventListener activePostValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.hasChildren()) {
                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    postId = snapshot.getKey();
                    Log.v(TAG, "POSTID: " + postId);
                    databaseReferencePendingList = FirebaseDatabase.getInstance().getReference().child("Requests").child(postId).child("Accepted");
                    databaseReferencePendingList.keepSynced(true);
                    databaseReferencePendingList.addValueEventListener(acceptlistListener);
                    databaseReferenceActive.removeEventListener(activePostValueEventListener);
                }
            } else {
                Log.v(TAG, "No Active Post");
                noPostMsg.setVisibility(View.VISIBLE);
                acceptList.setVisibility(View.GONE);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    private ValueEventListener acceptlistListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
            acceptArrayList.clear();
            loadingDialog.show();
            noPostMsg.setVisibility(View.GONE);
            acceptList.setVisibility(View.VISIBLE);
            if (dataSnapshot.hasChildren()) {
                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.v(TAG, dataSnapshot + "");

                    View.OnClickListener trackEvent = new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            uID = snapshot.getKey();
                            final String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION};

                            if (ContextCompat.checkSelfPermission(getContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                    && ContextCompat.checkSelfPermission(getContext(), COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                                Intent intent = new Intent(getActivity(), MapsActivity.class);
                                intent.putExtra("driverId", uID);
                                intent.putExtra("driver", "true");
                                startActivity(intent);

                            } else {
                                // Permission is not granted
                                // Should we show an explanation?
                                if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                    alertDialogBuilder.setPositiveButton("Allow",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Intent intent = new Intent();
                                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                    Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                                                    intent.setData(uri);
                                                    startActivity(intent);
                                                }
                                            });
                                    alertDialogBuilder.setMessage("Location permissions are neccessary for this feature.");
                                    AlertDialog alertDialog = alertDialogBuilder.create();
                                    alertDialog.show();

                                    return;
                                } else {

                                    requestPermissions(permissions, LOCATION_PERMISSION_REQUEST_CODE);
                                }

                            }

                        }
                    };

                    View.OnClickListener chatkEvent = new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Intent intent = new Intent(getActivity(), ChatActivity.class);
                            intent.putExtra("driverId", snapshot.getKey());
                            intent.putExtra("driverName", snapshot.child("name").getValue().toString());
                            startActivity(intent);
                        }
                    };


                    View.OnClickListener callEvent = new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            numToCall = snapshot.child("contact").getValue().toString();
                            getCallPermission();
                        }
                    };
                    try {
                        acceptArrayList.add(new Accept(snapshot.child("name").getValue().toString(), snapshot.child("seats").getValue().toString(), snapshot.child("location").getValue().toString(), snapshot.getKey(), trackEvent, callEvent, chatkEvent));
                    } catch (Exception e) {
                        Log.v(TAG, e.getMessage());
                    }
                }
                acceptRequestAdapter.notifyDataSetChanged();
            } else {
                noPostMsg.setVisibility(View.VISIBLE);
                acceptList.setVisibility(View.GONE);
            }
            loadingDialog.dismiss();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            noPostMsg.setVisibility(View.VISIBLE);
            acceptList.setVisibility(View.GONE);
            loadingDialog.dismiss();
        }
    };

    public AcceptedRequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_accepted_requests, container, false);
        mAuth = FirebaseAuth.getInstance();

        acceptArrayList = new ArrayList<>();
        acceptList = view.findViewById(R.id.acceptrequest_list);
        acceptRequestAdapter = new AcceptRequestAdapter(getActivity(), acceptArrayList);
        acceptList.setAdapter(acceptRequestAdapter);
        noPostMsg = view.findViewById(R.id.noPostMsg);
        postHelpingMethod = new PostHelpingMethod(getActivity());
        loadingDialog = postHelpingMethod.createProgressDialog("Loading...", "Please Wait.");
        databaseReferenceActive = FirebaseDatabase.getInstance().getReference().child("Posts").child("Active").child(mAuth.getUid());
        databaseReferenceActive.keepSynced(true);
        databaseReferenceActive.addValueEventListener(activePostValueEventListener);


        return view;
    }
    private void getCallPermission() {
        String[] permissions = {Manifest.permission.CALL_PHONE};
        if (ContextCompat.checkSelfPermission(getContext(), PHONE_CALL) == PackageManager.PERMISSION_GRANTED) {

            Intent intent = new Intent(Intent.ACTION_CALL);
            String phone = "tel:" + numToCall;
            intent.setData(Uri.parse(phone));
            startActivity(intent);

        } else {
            // Permission is not granted
            // Should we show an explanation?
            if (!shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE)) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setPositiveButton("Allow",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        });
                alertDialogBuilder.setMessage("Phone calling permission is neccessary for this feature.");
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                return;
            } else {

                requestPermissions(permissions, CALL_PERMISSION_REQUEST_CODE);
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            postHelpingMethod.snackbarMessage("Location Premissions are Required!", view);
                            return;
                        }
                    }
                    Intent intent = new Intent(getActivity(), MapsActivity.class);
                    intent.putExtra("driverId", uID);
                    intent.putExtra("driver", "true");
                    startActivity(intent);
                    break;
                }
            }
            case CALL_PERMISSION_REQUEST_CODE: {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    postHelpingMethod.snackbarMessage("Calling Permission is Required!", view);
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_CALL);
                String phone = "tel:" + numToCall;
                intent.setData(Uri.parse(phone));
                startActivity(intent);
                break;
            }
        }
    }

}
