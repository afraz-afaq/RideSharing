package com.example.adeel.ridesharing;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

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

    private ArrayList<Request> requestArrayList;
    private ListView requestList;
    private PendingRequestAdapter pendingRequestAdapter;
    private DatabaseReference databaseReferencePendingList;
    private DatabaseReference databaseReferenceActive;
    private FirebaseAuth mAuth;
    private ValueEventListener getPendingList = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.hasChildren()){
                for (DataSnapshot snapshot:dataSnapshot.getChildren()) {
                    String postId = snapshot.getKey();
                    databaseReferencePendingList = FirebaseDatabase.getInstance().getReference().child("Requests").child(postId).child("Pending");
                    databaseReferencePendingList.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
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
        databaseReferenceActive  = FirebaseDatabase.getInstance().getReference().child("Posts").child("Active").child(mAuth.getUid());
        requestArrayList = new ArrayList<>();
        requestList = v.findViewById(R.id.pendingrequest_list);
        pendingRequestAdapter = new PendingRequestAdapter(getActivity(),requestArrayList);
        requestList.setAdapter(pendingRequestAdapter);

        populatePendingList();

        return v;
    }

    private void populatePendingList(){

        databaseReferenceActive.addValueEventListener(getPendingList);
    }

}
