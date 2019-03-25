package com.example.adeel.ridesharing;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.firebase.geofire.core.GeoHashQuery;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.internal.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static com.example.adeel.ridesharing.MainActivity.mNavigationView;
import static com.example.adeel.ridesharing.MainActivity.mtoolbar;
import static java.lang.Math.round;

public class OfferRideFragment extends Fragment {

    private final String TAG = "PostRide2";
    private Animation fromTop,fromBottom;
    private Address mOriginAddress, mDestinationAddress;
    private String mVehicle, mDateTime,mDepartTIme, mTime, mSeats;
    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mAuth;
    private ArrayAdapter mSeatsAdapter, mCarAdapter, mTimeAdapter;
    private ArrayList mCarsList;
    private double mDisitance, mDuration, mFare, mPerKM, mPerMin, mBase;
    private boolean isCar;
    private PreferencesClass preferencesClass;
    private PostHelpingMethod postHelpingMethod;
    //private LatLngBounds mLatLngBounds = new LatLngBounds(new LatLng(-40, -168), new LatLng(71, 136));
    private LatLngBounds mLatLngBounds = new LatLngBounds(new LatLng(24, 67), new LatLng(25, 68));

    private Button mSubmit,mButton_Publish,mButtonCar,mButtonBike;
    private Spinner mSeatsSpinnner, mCarSpinnner, mTimeSpinner;
    private LinearLayout mLayout1,mLayout2,mLayoutVia;
    private AutoCompleteTextView mDepart,mPickUpText;
    private Switch mRouteSwitch;
    private Dialog mdialog;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private EditText mFurtherDetails;
    private View rootView;
    ValueEventListener ratingValueEventListener;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_offerride, container, false);
        postHelpingMethod = new PostHelpingMethod(getActivity());
        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getUid());

        preferencesClass = new PreferencesClass(getActivity());
        mLayout1=rootView.findViewById(R.id.layout_offer1);
        mLayout2=rootView.findViewById(R.id.layout_offer2);
        mLayoutVia=rootView.findViewById(R.id.layout_offerVehicle);
        mLayout1.setVisibility(View.GONE);
        mLayout2.setVisibility(View.GONE);

        mButtonCar = rootView.findViewById(R.id.button_viaCar);
        mButtonBike = rootView.findViewById(R.id.button_viaBike);
        mButton_Publish = rootView.findViewById(R.id.button_publish);
        mFurtherDetails = rootView.findViewById(R.id.message);
        mDepart = rootView.findViewById(R.id.depart);
        mPickUpText = rootView.findViewById(R.id.pickup);
        mRouteSwitch = rootView.findViewById(R.id.routeSwitch);

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(getContext(), Places.getGeoDataClient(getContext()), mLatLngBounds, null);
        mDepart.setAdapter(mPlaceAutocompleteAdapter);
        mPickUpText.setAdapter(mPlaceAutocompleteAdapter);

        mSeatsAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.seats, android.R.layout.simple_spinner_dropdown_item);
        mTimeAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.time, android.R.layout.simple_list_item_1);

        mCarSpinnner = rootView.findViewById(R.id.spinner_selectCar);
        mSeatsSpinnner = rootView.findViewById(R.id.spinner_seat);
        mTimeSpinner = rootView.findViewById(R.id.spinnerTime);

        mTimeSpinner.setAdapter(mTimeAdapter);
        mSeatsSpinnner.setAdapter(mSeatsAdapter);

        mButtonCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCars();
            }
        });

        mButtonBike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSeatsSpinnner.setEnabled(false);
                checkBikes();
            }
        });

        mTimeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mTime = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mSeatsSpinnner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mSeats = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mCarsList = new ArrayList();
        //populateVehicleList();
        mCarAdapter = new ArrayAdapter(getActivity(),android.R.layout.simple_list_item_1,mCarsList);
        mCarSpinnner.setAdapter(mCarAdapter);

        mSubmit = rootView.findViewById(R.id.button_submit);

        mDepart.setEnabled(false);
        mPickUpText.setAdapter(mPlaceAutocompleteAdapter);
        mDepart.setText("Bahria University Karachi Campus, National Stadium Rd, Karachi, Karachi");
        mPickUpText.getText().clear();
        mPickUpText.setEnabled(true);

        if(TextUtils.isEmpty(mPickUpText.getText().toString())){
            mDepart.setFocusable(true);
            mLayout1.setVisibility(View.VISIBLE);

        }

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    if(TextUtils.isEmpty(mPickUpText.getText().toString()))
                    {
                        Toast.makeText(getActivity(), "Please Enter your location", Toast.LENGTH_SHORT).show();
                        mPickUpText.setFocusable(true);
                        mLayout1.setVisibility(View.VISIBLE);
                    }

                else if(TextUtils.isEmpty(mDepart.getText().toString())){
                    Toast.makeText(getActivity(), "Please Enter your destination", Toast.LENGTH_SHORT).show();
                    mDepart.setFocusable(true);
                    mLayout1.setVisibility(View.VISIBLE);

                }
                else if(mTime.equals("Mins before leaving")){
                    Toast.makeText(getActivity(), "Please select time in leaving", Toast.LENGTH_SHORT).show();
                    mTimeSpinner.setFocusable(true);
                }
               else{
                        final ProgressDialog progressDialog = postHelpingMethod.createProgressDialog("Fetching","Please wait fetching locations");
                        final String depart = mDepart.getText().toString();
                        final String pickup = mPickUpText.getText().toString();
                        progressDialog.show();
                        new AsyncTask<String,Void,String>(){

                            @Override
                            protected String doInBackground(String... strings) {

                                mOriginAddress =  postHelpingMethod.geoLocateSearch(pickup,TAG);
                                mDestinationAddress =  postHelpingMethod.geoLocateSearch(depart,TAG);
                                return "";
                            }

                            @Override
                            protected void onPostExecute(String s) {

                                if(mOriginAddress == null || mDestinationAddress == null){
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setMessage("Location fetching error please try again.")
                                            .setTitle("Alert");
                                    builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.dismiss();
                                        }
                                    });

                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                }
                                else {
                                    mLayout1.setVisibility(View.GONE);
                                    mLayout2.setVisibility(View.VISIBLE);

                                }
                                progressDialog.dismiss();
                            }
                        }.execute();



                }


            }
        });

        mRouteSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    mPickUpText.setEnabled(false);
                    mPickUpText.setAdapter(null);
                    mPickUpText.setText("Bahria University Karachi Campus, National Stadium Rd, Karachi, Karachi");
                    mDepart.getText().clear();
                    mDepart.setEnabled(true);
                } else {
                    setDestination();
                }
            }
        });
        
        

        //new  fragment start

        mCarSpinnner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mVehicle = adapterView.getItemAtPosition(i).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mButton_Publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager connMgr = (ConnectivityManager)
                        getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (!(networkInfo != null && networkInfo.isConnected())) {
                    //check = true;
                    createDialog();

                } else {
                    createConfirmPasswordDialog();
                }
            }
        });


        return rootView;
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

    private void createConfirmPasswordDialog(){

        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.confirmpasswordlayout);
        Button verifyBtn = dialog.findViewById(R.id.button_verify);
        final EditText confirm = dialog.findViewById(R.id.editText_password);
        ImageView buttonClose = dialog.findViewById(R.id.imageView_close);
        dialog.show();
        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(confirm.getText().toString()))
                    postHelpingMethod.snackbarMessage("Please enter to verify password",view);
                else if(confirm.getText().toString().trim().equals(preferencesClass.getUSER_Password())) {
                    mdialog = postHelpingMethod.createDialog("Publishing...");
                    mdialog.show();
                    dialog.cancel();
                    getTimeFromServer();
                }
                else
                    postHelpingMethod.snackbarMessage("Verification failed",view);
            }
        });

        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
    }

    private void getTimeFromServer(){
        String URLTime = "http://api.geonames.org/timezoneJSON?formatted=true&lat=24.86&lng=67.00&username=zauya&style=full";
       // Toast.makeText(getActivity(),"In method",Toast.LENGTH_LONG).show();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, URLTime, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            mDateTime = response.getString("time");

                               Date date = stringToDate(mDateTime);
//                            Toast.makeText(getActivity(),date.toString(),Toast.LENGTH_SHORT).show();
//                            Toast.makeText(getActivity(),formatDate(date)+"",Toast.LENGTH_SHORT).show();
//                            Toast.makeText(getActivity(),formatDate(new Date(date.getTime() + (Integer.parseInt(mTime) * 60000)))+"",Toast.LENGTH_SHORT).show();
                            mDateTime = formatDate(date);
                            mDepartTIme = formatDate(new Date(date.getTime() + (Integer.parseInt(mTime) * 60000)));
                            getParsedData(new LatLng(mOriginAddress.getLatitude(),mOriginAddress.getLongitude()),new LatLng(mDestinationAddress.getLatitude(),mDestinationAddress.getLongitude()));
                        } catch (JSONException e) {
                            Log.e(TAG, "Problem parsing the JSON results", e);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Volley Error: ", error);
                    }
                });
        VolleySingleton.getInstance(getContext()).getRequestQueue().add(jsonObjectRequest);
        //publishPost();
    }
    private void getParsedData(LatLng origin, LatLng destination) {
        String originLatLng = origin.latitude+","+origin.longitude;
        String destinationLatLng = destination.latitude+","+destination.longitude;
        String URL = "https://api.myjson.com/bins/k1nd0";//"https://maps.googleapis.com/maps/api/directions/json?origin="+originLatLng+"&destination="+destinationLatLng+"&key=AIzaSyAQjza9vSMtTjbNtdbDrbev6cp9_mbt8Fk";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray routesArray = response.getJSONArray("routes");
                    JSONObject zeroRouteObject = routesArray.getJSONObject(0);
                    JSONArray legsArray = zeroRouteObject.getJSONArray("legs");
                    JSONObject zeroObject = legsArray.getJSONObject(0);
                    JSONObject distanceObject = zeroObject.getJSONObject("distance");
                    JSONObject durationObject = zeroObject.getJSONObject("duration");

                    Double distance = distanceObject.getDouble("value");
                    Double duration = durationObject.getDouble("value");
                    DecimalFormat df = new DecimalFormat("#.##");

                    mDisitance = Double.parseDouble(df.format(distance/1000));
                    mDuration = round(duration/60);
                    mFare = postHelpingMethod.getFare(mDisitance, mDuration,mPerKM,mPerMin,mBase);
                    //Toast.makeText(getActivity(),mDisitance+"  "+mDuration+"  "+mFare,Toast.LENGTH_LONG).show();
                    publishPost();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Volley Error: ", error);
            }
        });

        VolleySingleton.getInstance(getContext()).getRequestQueue().add(jsonObjectRequest);
    }
    private void publishPost(){
        String key = FirebaseDatabase.getInstance().getReference().child("Posts").child("Active").child(mAuth.getUid()).push().getKey();
        final DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("Posts").child("Active").child(mAuth.getUid()).child(key);

        final DatabaseReference driverRatingDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Ratings").child("Users").child(mAuth.getUid()).child("rating");
        ratingValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshotRating) {
                driverRatingDatabaseReference.removeEventListener(ratingValueEventListener);

                HashMap<String,Object> hashMap =  new HashMap<>();
                hashMap.put("rating",dataSnapshotRating.getValue().toString());
                hashMap.put("isCar",Boolean.toString(isCar));
                hashMap.put("contact",preferencesClass.getUserContact());
                hashMap.put("vehicle",mVehicle.split("\\s+")[0]);
                hashMap.put("date",mDateTime);
                hashMap.put("departTime",mDepartTIme);
                hashMap.put("distance",String.valueOf(mDisitance));
                hashMap.put("duration",String.valueOf(mDuration));
                hashMap.put("name",preferencesClass.getUSER_NAME());
                hashMap.put("regno",preferencesClass.getUserRegno());
                hashMap.put("seats",mSeats);
                hashMap.put("onway","false");
                hashMap.put("fare",String.valueOf(mFare));
                if(TextUtils.isEmpty(mFurtherDetails.getText().toString()))
                    hashMap.put("extraDetails","no");
                else
                    hashMap.put("extraDetails",mFurtherDetails.getText().toString());

                final HashMap<String,String> origin = new HashMap<>();
                origin.put("lat",String.valueOf(mOriginAddress.getLatitude()));
                origin.put("lng",String.valueOf(mOriginAddress.getLongitude()));
                origin.put("name",mPickUpText.getText().toString());

                final HashMap<String,String> destination = new HashMap<>();
                destination.put("lat",String.valueOf(mDestinationAddress.getLatitude()));
                destination.put("lng",String.valueOf(mDestinationAddress.getLongitude()));
                destination.put("name",mDepart.getText().toString());

                hashMap.put("Origin",origin);
                hashMap.put("Destination",destination);

                postRef.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){

                            Toast.makeText(getActivity(), "Ride Posted Successfully!", Toast.LENGTH_SHORT).show();
                            FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getUid()).child("poststatus").setValue("false");
                            mdialog.cancel();
                            getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                    new MyBookingFragment()).commit();
                            mNavigationView.setCheckedItem(R.id.nav_myBookings);
                            mtoolbar.setTitle(R.string.my_bookings);

                        }
                        else{
                            Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        driverRatingDatabaseReference.addValueEventListener(ratingValueEventListener);
    }
    public Date stringToDate(String mDateTime) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return dateFormat.parse(mDateTime);
    }

    public String formatDate(Date date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return dateFormat.format(date);
    }
    private void populateVehicleList(){
        final DatabaseReference carRef;
        if(isCar)
            carRef = mDatabaseReference.child("Cars");
        else
            carRef = mDatabaseReference.child("Bikes");
        carRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(mCarsList!=null)
                    mCarsList.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    //Cars car = new Cars(snapshot.getKey(),snapshot.child("name").getValue().toString(),snapshot.child("color").getValue().toString());
                    mCarsList.add(snapshot.getKey()+" "+snapshot.child("name").getValue().toString());
                }

                mCarAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getRates(){
        DatabaseReference databaseReference;
        if(isCar)
            databaseReference = FirebaseDatabase.getInstance().getReference().child("Price").child("Car");
        else
            databaseReference = FirebaseDatabase.getInstance().getReference().child("Price").child("Bike");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mBase = Double.parseDouble(dataSnapshot.child("base").getValue().toString());
                mPerKM = Double.parseDouble(dataSnapshot.child("perkm").getValue().toString());
                mPerMin = Double.parseDouble(dataSnapshot.child("permin").getValue().toString());
                Log.d(TAG,mBase+" "+mPerKM+" "+mPerMin);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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


    private void checkCars(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getUid()).child("Cars");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    isCar = true;
                    populateVehicleList();
                    getRates();
                    mLayoutVia.setVisibility(View.GONE);
                    mLayout1.setVisibility(View.VISIBLE);
                }
                else{
                    postHelpingMethod.snackbarMessage("No cars available to post a ride",rootView);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkBikes(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getUid()).child("Bikes");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    isCar = false;
                    populateVehicleList();
                    getRates();
                    mLayoutVia.setVisibility(View.GONE);
                    mLayout1.setVisibility(View.VISIBLE);
                }
                else{
                    postHelpingMethod.snackbarMessage("No bikes available to post a ride",rootView);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


//    private void setupAnim() {
//        fromBottom = AnimationUtils.loadAnimation(getActivity(), R.anim.from_bottom);
//        mLayout1.setAnimation(fromBottom);
//        fromTop = AnimationUtils.loadAnimation(getActivity(), R.anim.fall_down);
//        mLayout2.setAnimation(fromTop);
//
//    }

}
