package com.example.adeel.ridesharing;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

    private Spinner mOffredOptions;
    private int iOffredOptions=0;
    private TextView from, to, distance, time, seats, price;
    private Button start, cancel;
    private ListView offeredListView;
    private CardView activePost;
    Boolean checkCancel = false;
    private String postID;
    private FirebaseAuth mAuth;
    private ArrayList<HistoryPost> historyPosts;
    private  HistoryAdapter historyPostArrayAdapter;
    DatabaseReference databaseReferenceActive;
    private DatabaseReference databaseReference;


    ValueEventListener showActivePost = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.hasChildren()){

                activePost.setVisibility(View.VISIBLE);
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    postID = snapshot.getKey();
                    from.setText(snapshot.child("Origin").child("name").getValue().toString());
                    to.setText(snapshot.child("Destination").child("name").getValue().toString());
                    seats.setText(snapshot.child("seats").getValue().toString());
                    distance.setText(snapshot.child("distance").getValue().toString()+"KM");
                    time.setText(snapshot.child("departTime").getValue().toString().split("\\s+")[1]);
                    price.setText(snapshot.child("fare").getValue().toString()+"Rs");
                    if(snapshot.child("onway").getValue().toString().equals("false")){
                        start.setText("START");
                    }
                    else{
                        start.setText("COMPLETED");
                    }
                }
            }
            else
            {
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
                FirebaseDatabase.getInstance().getReference().child("Posts").child("Canceled").child(mAuth.getUid()).setValue(dataSnapshot.getValue());

                final FirebasePush firebasePush = new FirebasePush("AIzaSyDARseKL-2opSy4uMzLigTdjv-Mo6AyTsQ") ;
                firebasePush.setAsyncResponse(new PushNotificationTask.AsyncResponse() {
                    @Override
                    public void onFinishPush(@NotNull String ouput) {
                        Log.e("OUTPUT", ouput);
                    }
                });
                Log.v("PENDING","post " +postID);
                firebasePush.setNotification(new Notification("Ride Canceled","Sorry but your ride has been canceled by the driver"));
                DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference().child("Requests").child(postID).child("Pending");
                databaseReference1.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChildren()){
                            for (DataSnapshot snapshot:dataSnapshot.getChildren()) {

                                Log.v("token user",snapshot.getKey());
                                DatabaseReference userRefToken = FirebaseDatabase.getInstance().getReference().child("Users").child(snapshot.getKey()).child("token");
                                Toast.makeText(getActivity(), snapshot.getKey(), Toast.LENGTH_SHORT).show();
                                userRefToken.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    String token = dataSnapshot.getValue().toString();
                                    Log.v("token",token);
                                    firebasePush.sendToToken(token);
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
            });

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    checkCancel = true;
                    new AsyncTask<Void,Void,String>(){
                        @Override
                        protected String doInBackground(Void... voids) {
                            databaseReference = FirebaseDatabase.getInstance().getReference().child("Posts").child("Active").child(mAuth.getUid());
                            databaseReference.addValueEventListener(cancelPost);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(String s) {
                            super.onPostExecute(s);
                            databaseReference.removeEventListener(cancelPost);
                            FirebaseDatabase.getInstance().getReference().child("Posts").child("Active").removeValue();
                            Toast.makeText(getActivity(), postID, Toast.LENGTH_SHORT).show();
                        }
                    }.execute();

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
        offeredListView = rootView.findViewById(R.id.listView_offered);
        activePost = rootView.findViewById(R.id.activePost);
        mOffredOptions=rootView.findViewById(R.id.spinner_offeredoptions);
        mAuth = FirebaseAuth.getInstance();
        historyPosts = new ArrayList<HistoryPost>();
        historyPostArrayAdapter = new HistoryAdapter(getActivity(),historyPosts);
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
                if(start.getText().toString().equals("START")){
                    Toast.makeText(getActivity(), "Notify All", Toast.LENGTH_SHORT).show();
                    FirebaseDatabase.getInstance().getReference().child("Posts").child("Active").child(mAuth.getUid()).child(postID).child("onway").setValue("true");
                    start.setText("COMPLETED");
                }
                else{
                    Toast.makeText(getActivity(), "Ride Completed", Toast.LENGTH_SHORT).show();
                }

            }
        });

        spinnerOffredOptions();
        return rootView;
    }

    private void spinnerOffredOptions(){

        ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(getActivity(),R.array.bookings_options_driver,android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        mOffredOptions.setAdapter(arrayAdapter);

        mOffredOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selection = (String) adapterView.getItemAtPosition(i);
                if (!TextUtils.isEmpty(selection)){
                    if (selection.equals(getString(R.string.past_bookings))){
                        iOffredOptions=R.string.past_bookings;
                        showActive();
                    }
                    else if (selection.equals(getString(R.string.canceled_bookings))){
                        iOffredOptions=R.string.canceled_bookings;
                        activePost.setVisibility(View.GONE);
                        offeredListView.setVisibility(View.VISIBLE);
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Posts").child("Canceled").child(mAuth.getUid());
                        populateList(databaseReference);
                    }
                    else {
                        iOffredOptions=R.string.complete_bookings;
                        activePost.setVisibility(View.GONE);
                        offeredListView.setVisibility(View.VISIBLE);
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Posts").child("Completed").child(mAuth.getUid());
                        populateList(databaseReference);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                iOffredOptions=R.string.show_all;

            }
        });

    }

    private void showActive(){
        offeredListView.setVisibility(View.GONE);

        databaseReferenceActive.addValueEventListener(showActivePost);

    }

    private void populateList(DatabaseReference databaseReference){
        historyPosts.clear();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        HistoryPost historyPost = new HistoryPost(snapshot.child("Origin").child("name").getValue().toString(),snapshot.child("Destination").child("name").getValue().toString(),snapshot.child("fare").getValue().toString()+"Rs",snapshot.child("isCar").getValue().toString().equals("true")?"Car":"Bike",snapshot.child("vehicle").getValue().toString(),snapshot.child("departTime").getValue().toString());
                        historyPosts.add(historyPost);
                    }
                }
                else
                {
                    
                }

                historyPostArrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onDetach() {
        super.onDetach();
        databaseReferenceActive.removeEventListener(showActivePost);

    }
}
