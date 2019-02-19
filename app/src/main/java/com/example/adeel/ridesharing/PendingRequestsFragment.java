package com.example.adeel.ridesharing;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class PendingRequestsFragment extends Fragment {

    private final String TAG = "PENDING LIST";
    private ArrayList<Request> requestArrayList;
    private ListView requestList;
    private PendingRequestAdapter pendingRequestAdapter;
    private DatabaseReference databaseReferenceActive,databaseReferencePendingList;
    private FirebaseAuth mAuth;
    private String postId = "";
    private  ValueEventListener activePostValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.hasChildren()) {
                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    postId = snapshot.getKey();
                    Log.v(TAG,"POSTID: "+postId);
                    databaseReferencePendingList = FirebaseDatabase.getInstance().getReference().child("Requests").child(postId).child("Pending");
                    databaseReferencePendingList.addValueEventListener(penndinglistListener);
                    databaseReferenceActive.removeEventListener(activePostValueEventListener);
                }
            }
            else{
                Log.v(TAG,"No Active Post");
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    private  ValueEventListener penndinglistListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
            requestArrayList.clear();
            if(dataSnapshot.hasChildren()) {
                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.v(TAG,dataSnapshot+"");
                    View.OnClickListener cancelEvent = new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(getActivity(), "Canceled "+snapshot, Toast.LENGTH_SHORT).show();
                        }
                    };

                    View.OnClickListener acceptEvent = new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(getActivity(), "Accepted "+snapshot, Toast.LENGTH_SHORT).show();
                        }
                    };
                    try {
                        requestArrayList.add(new Request(snapshot.child("name").getValue().toString(), snapshot.child("seats").getValue().toString(), snapshot.child("location").getValue().toString(), snapshot.child("token").getValue().toString(), cancelEvent, acceptEvent));
                    }catch (Exception e){

                    }
                }
                pendingRequestAdapter.notifyDataSetChanged();
            }else{
                Toast.makeText(getActivity(), "No Pending Requests", Toast.LENGTH_SHORT).show();
            }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    public PendingRequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_pending_requests, container, false);

        mAuth=FirebaseAuth.getInstance();
        requestArrayList = new ArrayList<>();
        requestList = v.findViewById(R.id.pendingrequest_list);
        pendingRequestAdapter = new PendingRequestAdapter(getActivity(),requestArrayList);
        requestList.setAdapter(pendingRequestAdapter);


        databaseReferenceActive  = FirebaseDatabase.getInstance().getReference().child("Posts").child("Active").child(mAuth.getUid());
        databaseReferenceActive.addValueEventListener(activePostValueEventListener);

        return v;
    }


    @Override
    public void onDetach() {
        super.onDetach();

    }
}
