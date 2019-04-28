package com.example.adeel.ridesharing;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener {

    private GoogleMap mMap;
    private Button buttonFind;
    public static String TAG = "USER MAP";
    MarkerOptions markerOptions;
    Marker mCurrLocationMarker;
    MarkerOptions markerOtherOptions;
    Marker mOtherLocationMarker;
    Location mLastLocation;
    private FusedLocationProviderClient mLocationProviderClient;
    LocationRequest mLocationRequest;
    DatabaseReference trackRef, trackOtherRef;
    FirebaseAuth mAuth;
    PreferencesClass preferencesClass;
    PostHelpingMethod postHelpingMethod;
    String otherName;
    boolean firstTime = true, active = false, firstTimeLatLng = true;
    AlertDialog.Builder noActiveUserAlert;
    ValueEventListener valueEventListenerToken;
    LatLng otherUser, currentUser;
    DatabaseReference databaseReferenceTrack;
    ValueEventListener trackValueEventListener;
    AlertDialog trackDialog;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorPrimaryDark, R.color.colorPrimary, R.color.primary_dark_material_light};
    String[] perms = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                mLastLocation = location;
                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker.remove();
                }
                //Place current location marker
                trackRef.child("lat").setValue(location.getLatitude());
                trackRef.child("lng").setValue(location.getLongitude()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (firstTimeLatLng) {
                            FirebaseDatabase.getInstance().getReference().child("Track").child(mAuth.getUid()).child("active").setValue("true");
                            firstTimeLatLng = false;
                        }
                    }
                });
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                moveCamera(latLng, "Me");
                currentUser = latLng;
                if (otherUser != null)
                    getRouteToMarker();
            }
        }
    };

    ValueEventListener otherTrackValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (mMap != null) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("lat").exists() && dataSnapshot.child("lng").exists()) {
                        LatLng latLng = new LatLng(Double.parseDouble(dataSnapshot.child("lat").getValue().toString()), Double.parseDouble(dataSnapshot.child("lng").getValue().toString()));
                        if (mOtherLocationMarker != null)
                            mOtherLocationMarker.remove();
                        moveCamera(latLng, otherName);
                        otherUser = latLng;
                        if (currentUser != null)
                            getRouteToMarker();
//                        try {
//                            String distance = String.valueOf(postHelpingMethod.distanceBetween(latLng, currentUser));
//                            buttonFind.setText("User Online (" + postHelpingMethod.decimalPlacer(Double.valueOf(distance), 2) + " Meters Away)");
//                        } catch (Exception e) {
//                            buttonFind.setText("User Online");
//                        }
                    }
//                    } else {
//                        if(!firstTimeLatLng)
//                            noActiveUserAlert.show();
//                        buttonFind.setText("User Offline");
//                        active = false;
//                    }
                } else {
                    buttonFind.setText("User Offline");
                    trackDialog.show();
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        polylines = new ArrayList<>();
        final String receiver_id = getIntent().getStringExtra("driverId");
        buttonFind = findViewById(R.id.findBtn);
        preferencesClass = new PreferencesClass(this);
        postHelpingMethod = new PostHelpingMethod(this);

        noActiveUserAlert = new AlertDialog.Builder(MapsActivity.this);
        noActiveUserAlert.setMessage("User is not active. Request for Tracking?");
        noActiveUserAlert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final DatabaseReference dtoken = FirebaseDatabase.getInstance().getReference().child("Users").child(receiver_id).child("token");
                valueEventListenerToken = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (getIntent().getStringExtra("driver").equals("true"))
                            postHelpingMethod.sendNotification("Tracking Request", "Driver wants to track you", dataSnapshot.getValue().toString());
                        else
                            postHelpingMethod.sendNotification("Tracking Request", "User wants to track you", dataSnapshot.getValue().toString());

                        dtoken.removeEventListener(valueEventListenerToken);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                };
                dtoken.addValueEventListener(valueEventListenerToken);
            }
        });

        noActiveUserAlert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        trackDialog = noActiveUserAlert.create();
        if (getIntent().getStringExtra("driver").equals("true"))
            otherName = "To Pick";
        else
            otherName = "Driver";
        mAuth = FirebaseAuth.getInstance();

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        initPoint();
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1500); // two minute interval
        mLocationRequest.setFastestInterval(1500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);


        FirebaseDatabase.getInstance().getReference().child("Track").child(mAuth.getUid()).child("active").setValue("true");


    }

    private void initPoint() {
        LatLng latLng = new LatLng(24.8607, 67.0011);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f));
        if (currentUser != null)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentUser, 18f));
    }

    private void moveCamera(LatLng latLng, String title) {
        if (title.equals("Me")) {

            float zoom = mMap.getCameraPosition().zoom;
            markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            Bitmap bitmap = preferencesClass.decodeBase64(preferencesClass.getUserImage());
            markerOptions.icon(getMarkerIconFromDrawable(bitmap));
            markerOptions.title(title);
            mCurrLocationMarker = mMap.addMarker(markerOptions);
//            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            if (firstTime) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
                firstTime = false;
            }
        } else {
            float zoom = mMap.getCameraPosition().zoom;
            markerOtherOptions = new MarkerOptions();
            markerOtherOptions.position(latLng);
            Drawable otherDrawable;
            if (getIntent().getStringExtra("driver").equals("true"))
                otherDrawable = getResources().getDrawable(R.drawable.ic_person_pin_circle_black_48dp);
            else
                otherDrawable = getResources().getDrawable(R.drawable.ic_directions_car_black_32dp);
            BitmapDescriptor markerIcon = getMarkerIconFromDrawable(otherDrawable);
            markerOtherOptions.icon(markerIcon);
            markerOtherOptions.title(title);
            mOtherLocationMarker = mMap.addMarker(markerOtherOptions);
//            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            if (firstTime) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
            }
        }
    }

    private BitmapDescriptor getMarkerIconFromDrawable(Bitmap bitmapImage) {
        Bitmap bitmap = Bitmap.createScaledBitmap(bitmapImage, 50, 50, false);
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);

        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return BitmapDescriptorFactory.fromBitmap(output);
    }

    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    Routing routing;

    @Override
    protected void onStart() {
        super.onStart();
        final LocationManager manager = (LocationManager) getSystemService(this.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this);
            alertDialogBuilder.setPositiveButton("enable",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent viewIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(viewIntent);
                        }
                    });
            alertDialogBuilder.setMessage("Please enable your GPS.");
            android.app.AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }
        else {
            trackRef = FirebaseDatabase.getInstance().getReference().child("Track").child(mAuth.getUid());
//        trackOtherRef = FirebaseDatabase.getInstance().getReference().child("Track").child(receiver_id);
//        trackOtherRef.addValueEventListener(otherTrackValueEventListener);
            mLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            buttonFind.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (active) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(otherUser, mMap.getCameraPosition().zoom));
                    }
                }
            });
            trackOtherRef = FirebaseDatabase.getInstance().getReference().child("Track").child(getIntent().getStringExtra("driverId"));
            databaseReferenceTrack = FirebaseDatabase.getInstance().getReference().child("Track").child(getIntent().getStringExtra("driverId")).child("active");
            trackValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.getValue().equals("true")) {
                            trackDialog.dismiss();
                            trackOtherRef.addValueEventListener(otherTrackValueEventListener);
                            active = true;
                        } else {
                            trackOtherRef.removeEventListener(otherTrackValueEventListener);
                            trackDialog.show();
                            buttonFind.setText("User Offline");
                            active = false;
                        }
                    } else {
                        trackDialog.show();
                        active = false;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            databaseReferenceTrack.addValueEventListener(trackValueEventListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (routing != null)
            routing.cancel(true);
        if (databaseReferenceTrack != null) {
            databaseReferenceTrack.removeEventListener(trackValueEventListener);
            trackOtherRef.removeEventListener(otherTrackValueEventListener);
        }
        //mLocationProviderClient.removeLocationUpdates(mLocationCallback);
        FirebaseDatabase.getInstance().getReference().child("Track").child(mAuth.getUid()).child("active").setValue("false");
    }


    @Override
    public void onRoutingFailure(RouteException e) {
        if (e != null) {
            //Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        if (polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i < route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

//            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
            if (active) {
                try {
                    buttonFind.setText("User Online (" + route.get(i).getDistanceValue() + " Meters Away)");
                } catch (Exception e) {
                    buttonFind.setText("User Online");
                }
            }
        }
    }

    @Override
    public void onRoutingCancelled() {

    }

    private void getRouteToMarker() {
        routing = new Routing.Builder()
                .key("AIzaSyAQjza9vSMtTjbNtdbDrbev6cp9_mbt8Fk")
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(currentUser, otherUser)
                .build();
        routing.execute();
    }

    private void erasePolyLines() {
        for (Polyline line : polylines) {
            line.remove();
        }
        polylines.clear();
    }

}
