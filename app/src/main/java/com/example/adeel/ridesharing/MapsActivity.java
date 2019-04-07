package com.example.adeel.ridesharing;

import android.Manifest;
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
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    public static String TAG = "USER MAP";
    MarkerOptions markerOptions;
    Marker mCurrLocationMarker;
    MarkerOptions markerOtherOptions;
    Marker mOtherLocationMarker;
    Location mLastLocation;
    private FusedLocationProviderClient mLocationProviderClient;
    LocationRequest mLocationRequest;
    DatabaseReference trackRef,trackOtherRef;
    FirebaseAuth mAuth;
    PreferencesClass preferencesClass;
    PostHelpingMethod postHelpingMethod;
    String otherName;
    boolean firstTime = true;

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
                trackRef.child("lng").setValue(location.getLongitude());
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                moveCamera(latLng,"Me");
            }
        }
    };

    ValueEventListener otherTrackValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.exists()) {
                LatLng latLng = new LatLng(Double.parseDouble(dataSnapshot.child("lat").getValue().toString()), Double.parseDouble(dataSnapshot.child("lng").getValue().toString()));
                if (mOtherLocationMarker != null)
                    mOtherLocationMarker.remove();

                moveCamera(latLng, otherName);
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
        String receiver_id = getIntent().getStringExtra("driverId");
        if(getIntent().getStringExtra("driver").equals("true"))
            otherName = "To Pick";
        else
            otherName = "Driver";
        preferencesClass = new PreferencesClass(this);
        postHelpingMethod = new PostHelpingMethod(this);
        mAuth = FirebaseAuth.getInstance();
        trackRef = FirebaseDatabase.getInstance().getReference().child("Track").child(mAuth.getUid());
        trackOtherRef = FirebaseDatabase.getInstance().getReference().child("Track").child(receiver_id);
        trackOtherRef.addValueEventListener(otherTrackValueEventListener);
        mLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
    }

    private void initPoint(){
        LatLng latLng = new LatLng(24.8607,67.0011);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,18f));
    }
    private void moveCamera(LatLng latLng, String title){
        if(title.equals("Me")) {

            float zoom = mMap.getCameraPosition().zoom;
            markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            Bitmap bitmap = preferencesClass.decodeBase64(preferencesClass.getUserImage());
            markerOptions.icon(getMarkerIconFromDrawable(bitmap));
            markerOptions.title(title);
            mCurrLocationMarker = mMap.addMarker(markerOptions);
//            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            if(firstTime) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
                firstTime = false;
            }
        }
        else{
                float zoom = mMap.getCameraPosition().zoom;
                markerOtherOptions = new MarkerOptions();
                markerOtherOptions.position(latLng);
                Drawable otherDrawable;
                if (getIntent().getStringExtra("driver").equals("true"))
                    otherDrawable = getResources().getDrawable(R.drawable.ic_person_pin_circle_black_24dp);
                else
                    otherDrawable = getResources().getDrawable(R.drawable.ic_drive_eta_black_24dp);
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

    @Override
    protected void onStop() {
        super.onStop();
        trackOtherRef.removeEventListener(otherTrackValueEventListener);
        mLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }
}
