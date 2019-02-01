package com.example.adeel.ridesharing;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyBikesFragment extends Fragment {

    private ListView mBikeList;
    private FirebaseAuth mAuth;
    DatabaseReference databaseBikes;
    ArrayList<Cars> bikesArrayList;
    PostHelpingMethod postHelpingMethod;
    ProgressDialog progressDialog;
    View rootView;
    RelativeLayout emptyView;
    CarBottomSheet carBottomSheet;

    public MyBikesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_my_bikes, container, false);
        mAuth = FirebaseAuth.getInstance();
        postHelpingMethod = new PostHelpingMethod(getActivity());

        emptyView = rootView.findViewById(R.id.empty_viewBike);
        databaseBikes = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getUid()).child("Bikes");
        progressDialog  = postHelpingMethod.createProgressDialog("Fetching Bikes","Please Wait....");
        mBikeList = rootView.findViewById(R.id.listView_bikes);
        bikesArrayList = new ArrayList<>();

        mBikeList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putString("uId",mAuth.getUid());
                bundle.putString("bikeReg",bikesArrayList.get(position).getmCarRegNo());
                bundle.putString("from","bike");
                carBottomSheet = new CarBottomSheet();
                carBottomSheet.setArguments(bundle);
                carBottomSheet.show(getFragmentManager(),"");
                return false;
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (!(networkInfo != null && networkInfo.isConnected())) {
            //check = true;
            createDialog();

        } else {
            populateCarsList();
        }
    }

    private void createDialog(){

        final Dialog dialogNet = new Dialog(getActivity());
        dialogNet.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogNet.setContentView(R.layout.nointernet_dialog);
        Button buttonNet = dialogNet.findViewById(R.id.button_changephone);
        ImageView buttonClose = dialogNet.findViewById(R.id.imageView_close);
        dialogNet.show();
        buttonNet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogNet.cancel();
            }
        });

        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogNet.cancel();
            }
        });
    }


    public void populateCarsList(){
        progressDialog.show();
        databaseBikes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                bikesArrayList.clear();
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        //getting artist
                        Cars cars = new Cars(postSnapshot.child("color").getValue().toString(), postSnapshot.child("name").getValue().toString(), postSnapshot.getKey());
                        bikesArrayList.add(cars);
                        //Cars cars = postSnapshot.getValue(Cars.class);
                        //adding artist to the list
                        //Cars.add(cars);
                    }
                    PreferencesClass preferencesClass = new PreferencesClass(getActivity());
                    preferencesClass.saveBikesList(bikesArrayList);
                    CarsAdapter bikesAdapter = new CarsAdapter(getActivity(), bikesArrayList);
                    //attaching adapter to the listview
                    mBikeList.setAdapter(bikesAdapter);
                    bikesAdapter.notifyDataSetChanged();

                    emptyView.setVisibility(View.GONE);
                    progressDialog.dismiss();
                }
                else{

                    progressDialog.dismiss();
                    emptyView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}
