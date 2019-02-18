package com.example.adeel.ridesharing;


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

    private ArrayList<Request> requestArrayList;
    private ListView requestList;
    private PendingRequestAdapter pendingRequestAdapter;
    private DatabaseReference databaseReferenceActive,databaseReferencePendingList,databaseReferenceUser;
    private FirebaseAuth mAuth;
    private boolean check = false;
    private  ValueEventListener penndinglistListener, refUserListener;
    private ValueEventListener getPendingList = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            requestArrayList.clear();
            if(dataSnapshot.hasChildren()){
                for (final DataSnapshot snapshot:dataSnapshot.getChildren()) {
                    String postId = snapshot.getKey();
                    databaseReferencePendingList = FirebaseDatabase.getInstance().getReference().child("Requests").child(postId).child("Pending");
                    penndinglistListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChildren()){

                                check = true;
                                for (final DataSnapshot snapshot1:dataSnapshot.getChildren()) {
                                    databaseReferenceUser = FirebaseDatabase.getInstance().getReference().child("Users").child(snapshot1.getKey());
                                    refUserListener = new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                                            Log.v("Info",dataSnapshot+"");
                                            View.OnClickListener cancelEvent = new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    Toast.makeText(getActivity(), "Canceled "+dataSnapshot, Toast.LENGTH_SHORT).show();
                                                }
                                            };

                                            View.OnClickListener acceptEvent = new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    Toast.makeText(getActivity(), "Accepted "+dataSnapshot, Toast.LENGTH_SHORT).show();
                                                }
                                            };
                                            try {
                                                requestArrayList.add(new Request(dataSnapshot.child("name").getValue().toString(),snapshot1.child("seats").getValue().toString(),snapshot1.child("location").getValue().toString(), cancelEvent,acceptEvent));
                                                pendingRequestAdapter.notifyDataSetChanged();
                                            }catch (Exception e){

                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    };
                                    databaseReferenceUser.addValueEventListener(refUserListener);
                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    };

                    databaseReferencePendingList.addValueEventListener(penndinglistListener);
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

    @Override
    public void onDetach() {
        super.onDetach();
        databaseReferenceActive.removeEventListener(getPendingList);
        databaseReferencePendingList.removeEventListener(penndinglistListener);
        databaseReferenceUser.removeEventListener(refUserListener);
    }
}
