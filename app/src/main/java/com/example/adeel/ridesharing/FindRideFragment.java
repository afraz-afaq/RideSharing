package com.example.adeel.ridesharing;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.solver.widgets.Snapshot;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
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
import com.google.android.gms.tasks.Tasks;
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

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

public class FindRideFragment extends Fragment {

    public static String TAG = "FIND RIDE";
    LatLng latLng;
    LatLng latLngUser;
    DatabaseReference driverRatingDatabaseReference;
    ValueEventListener ratingValueEventListener;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Posts").child("Active");
    Timer timer = new Timer();
    private Spinner mDriver, mSeats;
    private ProgressDialog progressDialog;
    private int iDriver = 0, iSeats = 0;
    private Switch mRouteSwitch;
    private Button mFindRide;
    private LinearLayout mLayout1, mLayout2;
    private AutoCompleteTextView mDepart, mPickUpText;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private boolean flag = false;
    private View rootView;
    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private FindRideCellAdapter adapter;
    private ArrayList<FindRideItem> findRideItems;
    private PostHelpingMethod postHelpingMethod;
    private PreferencesClass preferencesClass;
    private ValueEventListener notify;
    private TextView noPostMsg;
    ImageView searchIcon;
    ValueEventListener valueEventListener;
    ValueEventListener findRideListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
            findRideItems.clear();
            if (dataSnapshot.hasChildren()) {

                progressDialog.show();
                //Address address;
                final String depart = mDepart.getText().toString();
                final String pickup = mPickUpText.getText().toString();


                new AsyncTask<String, Void, Address>() {

                    @Override
                    protected Address doInBackground(String... strings) {
                        Address Add;
                        if (flag) {

                            Add = postHelpingMethod.geoLocateSearch(depart, TAG);

                        } else {

                            Add = postHelpingMethod.geoLocateSearch(pickup, TAG);
                        }

                        return Add;
                    }

                    @Override
                    protected void onPostExecute(Address address1) {
                        if (address1 == null) {
                            progressDialog.dismiss();
                            databaseReference.removeEventListener(findRideListener);
                            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
                            builder.setMessage("Location fetching error please try again.")
                                    .setTitle("Alert");
                            builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    mPickUpText.setFocusable(true);
                                    mLayout2.setVisibility(View.GONE);
                                    noPostMsg.setVisibility(View.GONE);
                                    mLayout1.setVisibility(View.VISIBLE);
                                    dialog.dismiss();
                                }
                            });

                            android.app.AlertDialog dialog = builder.create();
                            dialog.setCancelable(false);
                            dialog.setCanceledOnTouchOutside(false);
                            dialog.show();
                        } else {

                            latLngUser = new LatLng(address1.getLatitude(), address1.getLongitude());
                            int i = 1;
                            for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                final String uId = snapshot.getKey();
                                for (final DataSnapshot snapshotposts : snapshot.getChildren()) {
                                    final String postId = snapshotposts.getKey();

                                    if (flag) {
                                        latLng = new LatLng(getDouble(snapshotposts.child("Destination").child("lat")), getDouble(snapshotposts.child("Destination").child("lng")));

                                    } else {
                                        latLng = new LatLng(getDouble(snapshotposts.child("Origin").child("lat")), getDouble(snapshotposts.child("Origin").child("lng")));
                                    }

                                    String dateTime = snapshotposts.child("departTime").getValue().toString();

                                    Date datePost = new Date();
                                    Date curDate = new Date();

                                    String strDateFormat = "yyyy-MM-dd HH:mm";
                                    DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
                                    String curDateString = dateFormat.format(curDate);

                                    try {
                                        datePost = new OfferRideFragment().stringToDate(new OfferRideFragment().formatDate(new OfferRideFragment().stringToDate(dateTime)));
                                        curDate = new OfferRideFragment().stringToDate(curDateString);
                                        Log.v(TAG, "Current: " + curDate + " PostDate: " + datePost + (datePost.compareTo(curDate) < 0 ? "No Ride" : "Ride hai"));
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                    if (postHelpingMethod.withInRange(latLng, latLngUser) && iSeats <= Integer.parseInt(snapShotToString(snapshotposts, "seats")) && !(datePost.compareTo(curDate) < 0) && snapShotToString(snapshotposts, "onway").equals("false")) {


                                        driverRatingDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Ratings").child("Users").child(uId).child("rating");
                                        driverRatingDatabaseReference.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshotRating) {
                                                Log.v("RATE", dataSnapshotRating.getValue().toString());
                                                final DatabaseReference databaseReference;
                                                if (snapShotToString(snapshotposts, "isCar").equals("true"))
                                                    databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uId).child("Cars").child(snapShotToString(snapshotposts, "vehicle"));
                                                else
                                                    databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uId).child("Bikes").child(snapShotToString(snapshotposts, "vehicle"));
                                                Log.v(TAG, "-> " + snapshotposts);
                                                databaseReference.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                                                        Log.v(TAG, "2-> " + dataSnapshot);
                                                                String fare = snapShotToString(snapshotposts, "fare");
                                                                DecimalFormat df = new DecimalFormat();
                                                                df.setMaximumFractionDigits(1);
                                                                FindRideItem findRideItem = new FindRideItem(uId, postId, df.format(Double.parseDouble(fare)), snapShotToString(snapshotposts.child("Origin"), "name"), snapShotToString(snapshotposts.child("Destination"), "name"), snapShotToString(snapshotposts, "seats"), snapShotToString(snapshotposts, "distance"), snapShotToString(snapshotposts, "departTime"),
                                                                        snapShotToString(dataSnapshot, "name"), snapShotToString(dataSnapshot, "color"), snapShotToString(snapshotposts, "vehicle"), snapShotToString(snapshotposts, "name"), snapShotToString(snapshotposts, "rating"), uId, snapShotToString(snapshotposts, "isCar"), snapShotToString(snapshotposts, "extraDetails"));
                                                                findRideItems.add(findRideItem);
                                                                final int index = findRideItems.size() - 1;
                                                                findRideItems.get(index).setRequestBtnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {
                                                                        final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(DialogInterface dialog, int which) {
                                                                                switch (which) {
                                                                                    case DialogInterface.BUTTON_POSITIVE:
                                                                                        //Yes button clicked
                                                                                        final DatabaseReference notifyDriver = FirebaseDatabase.getInstance().getReference().child("Users").child(findRideItems.get(index).getDriverUid()).child("token");
                                                                                        notify = new ValueEventListener() {
                                                                                            @Override
                                                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                                postHelpingMethod.sendNotification("New Request", preferencesClass.getUSER_NAME() + "(" + preferencesClass.getUserRegno() + ") wants to ride with you!", dataSnapshot.getValue().toString());
                                                                                                FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getUid()).child("poststatus").setValue("false");
                                                                                                notifyDriver.removeEventListener(notify);
                                                                                                getFragmentManager().beginTransaction().replace(R.id.fragment_container, new MyBookingFragment()).commit();
                                                                                            }

                                                                                            @Override
                                                                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                                                Toast.makeText(getActivity(), databaseError.getMessage().toString(), Toast.LENGTH_LONG).show();
                                                                                            }
                                                                                        };
                                                                                        notifyDriver.addValueEventListener(notify);
                                                                                        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference().child("Requests").child(findRideItems.get(index).getPostId()).child("Pending").child(mAuth.getUid());
                                                                                        databaseReference1.child("seats").setValue(iSeats);
                                                                                        databaseReference1.child("contact").setValue(preferencesClass.getUserContact());
                                                                                        databaseReference1.child("name").setValue(preferencesClass.getUSER_NAME());
                                                                                        if (flag)
                                                                                            databaseReference1.child("location").setValue(findRideItems.get(index).getToAddress());
                                                                                        else
                                                                                            databaseReference1.child("location").setValue(findRideItems.get(index).getFromAddress());

                                                                                        DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference().child("Find").child(mAuth.getUid()).child("Pending").child(findRideItems.get(index).getPostId());
                                                                                        HashMap<String, String> hashMap = new HashMap<String, String>();
                                                                                        hashMap.put("driver", findRideItems.get(index).getDriverUid());
                                                                                        databaseReference2.setValue(hashMap);
                                                                                        break;

                                                                                    case DialogInterface.BUTTON_NEGATIVE:
                                                                                        //No button clicked
                                                                                        dialog.dismiss();
                                                                                        break;
                                                                                }
                                                                            }
                                                                        };
                                                                        final DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference().child("Posts").child("Active").child(uId).child(postId);
                                                                        valueEventListener = new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                if(dataSnapshot.hasChildren()){
                                                                                    if(dataSnapshot.child("onway").getValue().toString().equals("false")) {
                                                                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                                                        builder.setMessage("Request this ride?").setPositiveButton("Yes", dialogClickListener)
                                                                                                .setNegativeButton("No", dialogClickListener).show();
                                                                                    }
                                                                                    else{
                                                                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                                                        builder.setMessage("This ride is no longer available!").setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                                                                            @Override
                                                                                            public void onClick(DialogInterface dialog, int which) {
                                                                                                dialog.dismiss();
                                                                                            }
                                                                                        })
                                                                                                .show();
                                                                                    }
                                                                                }
                                                                                else{
                                                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                                                    builder.setMessage("This ride is no longer available!").setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                                                                        @Override
                                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                                            dialog.dismiss();
                                                                                        }
                                                                                    })
                                                                                            .show();
                                                                                }

                                                                                databaseReference1.removeEventListener(valueEventListener);
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                                                builder.setMessage("Theres Something Wrong. "+databaseError.getMessage()).setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                                                                    @Override
                                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                                        dialog.dismiss();
                                                                                    }
                                                                                })
                                                                                        .show();
                                                                            }
                                                                        };
                                                                        databaseReference1.addValueEventListener(valueEventListener);
                                                                    }
                                                                });
                                                                progressDialog.dismiss();
                                                                noPostMsg.setVisibility(View.GONE);
                                                                mLayout2.setVisibility(View.VISIBLE);
                                                                adapter.notifyDataSetChanged();

                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }

                                                });

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

                                    }
                                    else {
                                        if (i == dataSnapshot.getChildrenCount()) {
                                            if (findRideItems.isEmpty()) {
                                                progressDialog.dismiss();
                                                noPostMsg.setVisibility(View.VISIBLE);
                                            }

                                        }

                                    }
                                }
                                i++;
                            }

                            databaseReference.removeEventListener(findRideListener);
                        }

                    }
                }.execute();

            } else {
                adapter.notifyDataSetChanged();
                progressDialog.cancel();
                noPostMsg.setVisibility(View.VISIBLE);
                mLayout1.setVisibility(View.GONE);
                mLayout2.setVisibility(View.GONE);
                databaseReference.removeEventListener(findRideListener);
            }


        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }

    };
    private LatLngBounds mLatLngBounds = new LatLngBounds(new LatLng(24, 67), new LatLng(25, 68));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_findride, container, false);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        findRideItems = new ArrayList<>();
        postHelpingMethod = new PostHelpingMethod(getActivity());
        preferencesClass = new PreferencesClass(getActivity());
        mDriver = rootView.findViewById(R.id.spinner_driver);
        mSeats = rootView.findViewById(R.id.spinner_seat);
        noPostMsg = rootView.findViewById(R.id.noPostMsg);
        progressDialog = postHelpingMethod.createProgressDialog("Searching...", "Finding nearby rides for you");

        spinnerDriver();
        spinnerSeat();

        mDepart = rootView.findViewById(R.id.editText_destination);
        mPickUpText = rootView.findViewById(R.id.editText_location);

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
                if (check) {

                    mLayout2.setVisibility(View.VISIBLE);
                    mLayout1.setVisibility(View.GONE);
                    noPostMsg.setVisibility(View.GONE);
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
                        iSeats = 1;
                    } else if (selection.equals(getString(R.string.sno2))) {
                        iSeats = 2;
                    } else if (selection.equals(getString(R.string.sno3))) {
                        iSeats = 3;
                    } else {
                        iSeats = 4;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                iSeats = R.string.sno1;

            }
        });

    }

    private void populateFindList() {
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        searchIcon = toolbar.findViewById(R.id.refreshSearch);
        searchIcon.setVisibility(View.VISIBLE);
        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.addValueEventListener(findRideListener);
            }
        });

        TimerTask timerTask = new TimerTask() {
            public void run() {

                databaseReference.addValueEventListener(findRideListener);
            }
        };
        timer.schedule(timerTask, 0, 90000);
    }

    private double getDouble(DataSnapshot snapshot) {
        return Double.parseDouble(snapshot.getValue().toString());
    }

    private String snapShotToString(DataSnapshot snapshot, String value) {
        return snapshot.child(value).getValue().toString();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(searchIcon != null)
            searchIcon.setVisibility(View.GONE);
        timer.cancel();
        databaseReference.removeEventListener(findRideListener);
        Log.v("Bye", "Bye Bye");
    }
}
