package com.example.adeel.ridesharing;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
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

import java.util.ArrayList;

import static com.example.adeel.ridesharing.FindRideFragment.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class AcceptedRequestsFragment extends Fragment {
    private ArrayList<Accept> acceptArrayList;
    private ListView acceptList;
    private AcceptRequestAdapter acceptRequestAdapter;
    private DatabaseReference databaseReferenceActive,databaseReferencePendingList;
    private FirebaseAuth mAuth;
    private Button track_Button,call_Button,chat_Button;
    private String postId = "";
    private TextView noPostMsg;
    private ProgressDialog loadingDialog;
    ValueEventListener getTokenForCancelValueEventListener;
    private  ValueEventListener activePostValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.hasChildren()) {
                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    postId = snapshot.getKey();
                    Log.v(TAG,"POSTID: "+postId);
                    databaseReferencePendingList = FirebaseDatabase.getInstance().getReference().child("Requests").child(postId).child("Accepted");
                    databaseReferencePendingList.keepSynced(true);
                    databaseReferencePendingList.addValueEventListener(acceptlistListener);
                    databaseReferenceActive.removeEventListener(activePostValueEventListener);
                }
            }
            else{
                Log.v(TAG,"No Active Post");
                noPostMsg.setVisibility(View.VISIBLE);
                acceptList.setVisibility(View.GONE);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    private  ValueEventListener acceptlistListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
            acceptArrayList.clear();
            loadingDialog.show();
            noPostMsg.setVisibility(View.GONE);
            acceptList.setVisibility(View.VISIBLE);
            if(dataSnapshot.hasChildren()) {
                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.v(TAG,dataSnapshot+"");
                    View.OnClickListener trackEvent = new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getActivity(),MapsActivity.class);
                            intent.putExtra("driverId",snapshot.getKey() );
                            intent.putExtra("driver","true");
                            startActivity(intent);

                        }
                    };

                    View.OnClickListener chatkEvent = new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Intent intent = new Intent(getActivity(),ChatActivity.class);
                            intent.putExtra("driverId",snapshot.getKey() );
                            intent.putExtra("driverName",snapshot.child("name").getValue().toString());
                            startActivity(intent);
                        }
                    };



                    View.OnClickListener callEvent = new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Intent.ACTION_CALL);
                            String phone = "tel:"+snapshot.child("contact").getValue().toString();
                            intent.setData(Uri.parse(phone));
                            if (ActivityCompat.checkSelfPermission(getActivity(),
                                    Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                                startActivity(intent);
                            }
                        }
                    };
                    try {
                        acceptArrayList.add(new Accept(snapshot.child("name").getValue().toString(), snapshot.child("seats").getValue().toString(), snapshot.child("location").getValue().toString(),snapshot.getKey()  , trackEvent, callEvent,chatkEvent));
                    }catch (Exception e){
                        Log.v(TAG,e.getMessage());
                    }
                }
                acceptRequestAdapter.notifyDataSetChanged();
            }else{
//                Toast.makeText(getActivity(), "No Pending Requests", Toast.LENGTH_SHORT).show();
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
        View view = inflater.inflate(R.layout.fragment_accepted_requests, container, false);
        mAuth = FirebaseAuth.getInstance();

        acceptArrayList = new ArrayList<>();
        acceptList = view.findViewById(R.id.acceptrequest_list);
        acceptRequestAdapter = new AcceptRequestAdapter(getActivity(),acceptArrayList);
        acceptList.setAdapter(acceptRequestAdapter);
        noPostMsg = view.findViewById(R.id.noPostMsg);
        PostHelpingMethod postHelpingMethod = new PostHelpingMethod(getActivity());
        loadingDialog = postHelpingMethod.createProgressDialog("Loading...","Please Wait.");
        databaseReferenceActive  = FirebaseDatabase.getInstance().getReference().child("Posts").child("Active").child(mAuth.getUid());
        databaseReferenceActive.keepSynced(true);
        databaseReferenceActive.addValueEventListener(activePostValueEventListener);


        return view;
    }

}
