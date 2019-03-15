package com.example.adeel.ridesharing;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class PendingRequestsFragment extends Fragment {

    private final String TAG = "PENDING LIST";
    private ArrayList<Request> requestArrayList;
    private ListView requestList;
    private PendingRequestAdapter pendingRequestAdapter;
    private DatabaseReference databaseReferenceActive,databaseReferencePendingList;
    private ValueEventListener getFindPendingToAcceptValueEventListener;
    private FirebaseAuth mAuth;
    private Button mAccept_Button,mCancel_Button;
    private String postId = "";
    private TextView noPostMsg;
    ValueEventListener getTokenForCancelValueEventListener;
    private ProgressDialog loadingDialog;
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
                noPostMsg.setVisibility(View.VISIBLE);
                requestList.setVisibility(View.GONE);
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
            loadingDialog.show();
            noPostMsg.setVisibility(View.GONE);
            requestList.setVisibility(View.VISIBLE);
            if(dataSnapshot.hasChildren()) {
                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.v(TAG,dataSnapshot+"");
                    View.OnClickListener cancelEvent = new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final DatabaseReference getTokenForCancel = FirebaseDatabase.getInstance().getReference().child("Users").child(snapshot.getKey()).child("token");
                            getTokenForCancelValueEventListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Toast.makeText(getActivity(), "Canceled "+dataSnapshot.getValue().toString(), Toast.LENGTH_SHORT).show();

                                    getTokenForCancel.removeEventListener(getTokenForCancelValueEventListener);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            };
                            getTokenForCancel.addValueEventListener(getTokenForCancelValueEventListener);


                        }
                    };

                    View.OnClickListener acceptEvent = new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DatabaseReference Acceptedref = FirebaseDatabase.getInstance().getReference().child("Requests").child(postId).child("Accepted").child(snapshot.getKey());
                            Acceptedref.setValue(snapshot.getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        DatabaseReference removeref = FirebaseDatabase.getInstance().getReference().child("Requests").child(postId).child("Pending").child(snapshot.getKey());
                                        databaseReferencePendingList.removeEventListener(penndinglistListener);
                                        removeref.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                databaseReferencePendingList.addValueEventListener(penndinglistListener);
                                            }
                                        });


                                        final DatabaseReference getFindPendingToAccept = FirebaseDatabase.getInstance().getReference().child("Find").child(snapshot.getKey()).child("Pending").child(postId);
                                        getFindPendingToAcceptValueEventListener = new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                FirebaseDatabase.getInstance().getReference().child("Find").child(snapshot.getKey()).child("Active").child(postId).setValue(dataSnapshot.getValue());
                                                getFindPendingToAccept.removeEventListener(getFindPendingToAcceptValueEventListener);
                                                getFindPendingToAccept.removeValue();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        };
                                        getFindPendingToAccept.addValueEventListener(getFindPendingToAcceptValueEventListener);


                                    }
                                    else {
                                        String error = task.getException().toString();
                                        Toast.makeText(getActivity(), "fail "+error, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    };
                    try {
                        requestArrayList.add(new Request(snapshot.child("name").getValue().toString(), snapshot.child("seats").getValue().toString(), snapshot.child("location").getValue().toString(), cancelEvent, acceptEvent));
                    }catch (Exception e){

                    }
                }
            }else{
//                Toast.makeText(getActivity(), "No Pending Requests", Toast.LENGTH_SHORT).show();
                noPostMsg.setVisibility(View.VISIBLE);
                requestList.setVisibility(View.GONE);
            }
            loadingDialog.dismiss();
            pendingRequestAdapter.notifyDataSetChanged();
        }
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            noPostMsg.setVisibility(View.VISIBLE);
            requestList.setVisibility(View.GONE);
            loadingDialog.dismiss();
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
        noPostMsg = v.findViewById(R.id.noPostMsg);
        PostHelpingMethod postHelpingMethod = new PostHelpingMethod(getActivity());
        loadingDialog = postHelpingMethod.createProgressDialog("Loading...","Please Wait.");
        databaseReferenceActive  = FirebaseDatabase.getInstance().getReference().child("Posts").child("Active").child(mAuth.getUid());
        databaseReferenceActive.addValueEventListener(activePostValueEventListener);

        return v;
    }


    @Override
    public void onDetach() {
        super.onDetach();

    }
}
