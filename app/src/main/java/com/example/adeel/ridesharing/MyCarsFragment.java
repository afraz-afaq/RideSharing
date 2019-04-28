package com.example.adeel.ridesharing;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyCarsFragment extends Fragment {
    private ListView mCarList;
    private FirebaseAuth mAuth;
    DatabaseReference databaseCars;
    ArrayList<Cars> carsArrayList;
    PostHelpingMethod postHelpingMethod;
    ProgressDialog progressDialog;
    View rootView;
    RelativeLayout emptyView;
    CarBottomSheet carBottomSheet;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_mycars, container, false);
        mAuth = FirebaseAuth.getInstance();
        postHelpingMethod = new PostHelpingMethod(getActivity());

        emptyView = rootView.findViewById(R.id.empty_viewCar);
        databaseCars = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getUid()).child("Cars");
        databaseCars.keepSynced(true);
        progressDialog = postHelpingMethod.createProgressDialog("Fetching Vehicles", "Please Wait....");
        mCarList = rootView.findViewById(R.id.listView_cars);
        carsArrayList = new ArrayList<>();


        mCarList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ConnectivityManager connMgr = (ConnectivityManager)
                        getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (!(networkInfo != null && networkInfo.isConnected())) {
                    //check = true;
                    createDialog();

                } else {

                    Bundle bundle = new Bundle();
                    bundle.putString("uId", mAuth.getUid());
                    bundle.putString("carReg", carsArrayList.get(position).getmCarRegNo());
                    bundle.putString("from", "car");
                    carBottomSheet = new CarBottomSheet();
                    carBottomSheet.setArguments(bundle);
                    carBottomSheet.show(getFragmentManager(), "");

                }


                return false;
            }
        });


        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        populateCarsList();
    }

    private void createDialog() {

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

    public void populateCarsList() {
        progressDialog.show();
        databaseCars.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                carsArrayList.clear();
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        //getting artist
                        Cars cars = new Cars(postSnapshot.child("color").getValue().toString(), postSnapshot.child("name").getValue().toString(), postSnapshot.getKey());
                        carsArrayList.add(cars);
                        //Cars cars = postSnapshot.getValue(Cars.class);
                        //adding artist to the list
                        //Cars.add(cars);
                    }
                    PreferencesClass preferencesClass = new PreferencesClass(getActivity());
                    preferencesClass.saveBikesList(carsArrayList);
                    CarsAdapter carsAdapter = new CarsAdapter(getActivity(), carsArrayList);
                    //attaching adapter to the listview
                    mCarList.setAdapter(carsAdapter);
                    carsAdapter.notifyDataSetChanged();

                    emptyView.setVisibility(View.GONE);
                    progressDialog.dismiss();
                } else {

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
