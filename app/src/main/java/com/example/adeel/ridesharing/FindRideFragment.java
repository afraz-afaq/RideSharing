package com.example.adeel.ridesharing;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.solver.widgets.Snapshot;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.ramotion.foldingcell.FoldingCell;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class FindRideFragment extends Fragment {

    public static String TAG = "FIND RIDE";

    private Spinner mDriver, mSeats;
    private ProgressDialog progressDialog;
    private int iDriver = 0, iSeats = 0;
    private Switch mRouteSwitch;
    private Button mFindRide;
    private LinearLayout mLayout1, mLayout2;
    private AutoCompleteTextView mDepart, mPickUpText;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private boolean flag = false;
    View rootView;
    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private FindRideCellAdapter adapter;
    ArrayList<FindRideItem> findRideItems;
    PostHelpingMethod postHelpingMethod;
    private LatLngBounds mLatLngBounds = new LatLngBounds(new LatLng(24, 67), new LatLng(25, 68));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
         rootView = inflater.inflate(R.layout.fragment_findride, container, false);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        findRideItems = new ArrayList<>();
        postHelpingMethod = new PostHelpingMethod(getActivity());
        mDriver = rootView.findViewById(R.id.spinner_driver);
        mSeats = rootView.findViewById(R.id.spinner_seat);
        progressDialog = postHelpingMethod.createProgressDialog("Searching...,", "Finding nearby rides for you");

        spinnerDriver();
        spinnerSeat();

        mDepart = rootView.findViewById(R.id.editText_destination);
        mPickUpText = rootView.findViewById(R.id.editText_location );

        mLayout1 = rootView.findViewById(R.id.layout_cardFindride);
        mLayout2 = rootView.findViewById(R.id.ListLayout_findride);

        mRouteSwitch = rootView.findViewById(R.id.findrideswitch);


        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(getContext(), Places.getGeoDataClient(getContext()), mLatLngBounds, null);
        mPickUpText.setAdapter(mPlaceAutocompleteAdapter);
        mDepart.setAdapter(mPlaceAutocompleteAdapter);

        mDepart.setEnabled(false);
        mDepart.setText("Bahria University Karachi Campus, National Stadium Rd, Karachi, Karachi");
        mPickUpText.getText().clear();
        mPickUpText.setEnabled(true);

        if (TextUtils.isEmpty(mPickUpText.getText().toString())) {
            mDepart.setFocusable(true);

        }

        mFindRide = rootView.findViewById(R.id.button_searchRide);
        mFindRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean check = true;
                if (flag) {
                    if (TextUtils.isEmpty(mDepart.getText().toString())) {
                        Toast.makeText(getActivity(), "Please Enter your desstination", Toast.LENGTH_SHORT).show();
                        mDepart.setFocusable(true);
                        mLayout1.setVisibility(View.VISIBLE);
                        check = false;
                    }

                } else if (TextUtils.isEmpty(mPickUpText.getText().toString())) {
                    Toast.makeText(getActivity(), "Please Enter your location", Toast.LENGTH_SHORT).show();
                    mPickUpText.setFocusable(true);
                    mLayout1.setVisibility(View.VISIBLE);
                    check = false;

                }
                if(check) {
                    mLayout1.setVisibility(View.GONE);
                    mLayout2.setVisibility(View.VISIBLE);
                    populateListView();
                }

            }
        });
        mRouteSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b) {
                    flag = b;
                    mPickUpText.setEnabled(false);
                    mPickUpText.setAdapter(null);
                    mPickUpText.setText("Bahria University Karachi Campus, National Stadium Rd, Karachi, Karachi");
                    mDepart.getText().clear();
                    mDepart.setEnabled(true);
                } else {
                    flag = b;
                    setDestination();
                }
            }
        });

        return rootView;
    }

    private void populateListView() {

        ListView theListView = rootView.findViewById(R.id.ListView_findRide);
        populateFindList();
         adapter = new FindRideCellAdapter(getActivity(), findRideItems);
        adapter.setDefaultRequestBtnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "DEFAULT HANDLER FOR ALL BUTTONS", Toast.LENGTH_SHORT).
                show();
            }
        });

        theListView.setAdapter(adapter);
        theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                ((FoldingCell) view).toggle(false);
                adapter.registerToggle(pos);
            }
        });
    }

    private void setDestination() {
        mDepart.setEnabled(false);
        mPickUpText.setAdapter(mPlaceAutocompleteAdapter);
        mDepart.setText("Bahria University Karachi Campus, National Stadium Rd, Karachi, Karachi");
        mPickUpText.getText().clear();
        mPickUpText.setEnabled(true);
    }

    private void spinnerDriver() {

        ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.Drivers, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        mDriver.setAdapter(arrayAdapter);

        mDriver.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selection = (String) adapterView.getItemAtPosition(i);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.driver_male))) {
                        iDriver = R.string.driver_male;
                    } else {
                        iDriver = R.string.driver_female;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                iDriver = R.string.driver_male;

            }
        });

    }

    private void spinnerSeat() {

        ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.Seats, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        mSeats.setAdapter(arrayAdapter);

        mSeats.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selection = (String) adapterView.getItemAtPosition(i);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.sno1))) {
                        iSeats = R.string.sno1;
                    } else if (selection.equals(getString(R.string.sno2))) {
                        iSeats = R.string.sno2;
                    } else if (selection.equals(getString(R.string.sno3))) {
                        iSeats = R.string.sno3;
                    } else {
                        iSeats = R.string.sno4;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                iSeats = R.string.sno1;

            }
        });

    }

    private void populateFindList(){
        progressDialog.show();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Posts").child("Active");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    final String uId = snapshot.getKey();
                    for (final DataSnapshot snapshotposts:snapshot.getChildren()) {
                        final String postId = snapshotposts.getKey();
                        LatLng latLng;
                        LatLng latLngUser;
                        if(flag) {
                            latLng = new LatLng(getDouble(snapshotposts.child("Destination").child("lat")), getDouble(snapshotposts.child("Destination").child("lng")));
                            Address userDestination = postHelpingMethod.geoLocateSearch(mDepart.getText().toString(),TAG);
                            latLngUser = new LatLng(userDestination.getLatitude(),userDestination.getLongitude());
                        }
                        else {
                            latLng = new LatLng(getDouble(snapshotposts.child("Origin").child("lat")), getDouble(snapshotposts.child("Origin").child("lng")));
                            Address userOrigin = postHelpingMethod.geoLocateSearch(mPickUpText.getText().toString(),TAG);
                            latLngUser = new LatLng(userOrigin.getLatitude(),userOrigin.getLongitude());
                        }

                        if(postHelpingMethod.withInRange(latLng,latLngUser) && Integer.parseInt(getString(iSeats))<= Integer.parseInt(snapShotToString(snapshotposts,"seats"))){
                            DatabaseReference databaseReference;
                            if(snapShotToString(snapshotposts,"isCar").equals("true"))
                                databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uId).child("Cars").child(snapShotToString(snapshotposts,"vehicle"));
                            else
                                databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uId).child("Bikes").child(snapShotToString(snapshotposts,"vehicle"));
                            Log.v(TAG,"-> "+snapshotposts);
                            databaseReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                                    Log.v(TAG,"2-> "+dataSnapshot);
                                    String path = uId;
                                    mStorageRef.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String imageURL = uri.toString();
                                            String fare = snapShotToString(snapshotposts,"fare");
                                            DecimalFormat df = new DecimalFormat();
                                            df.setMaximumFractionDigits(1);
                                            FindRideItem findRideItem = new FindRideItem(uId,postId,df.format(Double.parseDouble(fare)),snapShotToString(snapshotposts.child("Origin"),"name"),snapShotToString(snapshotposts.child("Destination"),"name"),snapShotToString(snapshotposts,"seats"),snapShotToString(snapshotposts,"distance"),snapShotToString(snapshotposts,"departTime"),
                                                    snapShotToString(dataSnapshot,"name"),snapShotToString(dataSnapshot,"color"),snapShotToString(snapshotposts,"vehicle"),snapShotToString(snapshotposts,"name"),imageURL);
                                            findRideItems.add(findRideItem);
                                            final int index = findRideItems.size()-1;
                                            findRideItems.get(index).setRequestBtnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Toast.makeText(getActivity(), findRideItems.get(index).getPostId(), Toast.LENGTH_SHORT).
                                                            show();
                                                    DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference().child("Requests").child(findRideItems.get(index).getPostId());
                                                    databaseReference1.child(mAuth.getUid()).child("status").setValue("pending");

                                                    DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference().child("Find").child("Pending").child(mAuth.getUid());
                                                    HashMap<String,String> hashMap = new HashMap<String, String>();
                                                    hashMap.put("postId",findRideItems.get(index).getPostId());
                                                    hashMap.put("driver",findRideItems.get(index).getDriverUid());
                                                    databaseReference2.setValue(hashMap);

                                                }
                                            });

                                            adapter.notifyDataSetChanged();

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            Toast.makeText(getActivity(),exception.toString(),Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }

                            });

                        }
                    }
                }

                progressDialog.cancel();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private double getDouble(DataSnapshot snapshot){
        return Double.parseDouble(snapshot.getValue().toString());
    }

    private String snapShotToString(DataSnapshot snapshot, String value){
        return snapshot.child(value).getValue().toString();
    }

}

