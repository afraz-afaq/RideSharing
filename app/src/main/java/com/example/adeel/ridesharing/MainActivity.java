package com.example.adeel.ridesharing;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;

public class MainActivity extends AppCompatActivity  implements CarBottomSheet.GetDeleteStatus{

    private static final String TAG = MainActivity.class.getSimpleName();
    DrawerLayout mDrawerlayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    static Toolbar mtoolbar;
    static NavigationView mNavigationView;
    private View headerView;
    private PreferencesClass preferencesClass;
    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        preferencesClass = new PreferencesClass(this);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_menu);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        drawerSetup();

        if (savedInstanceState==null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
            mNavigationView.setCheckedItem(R.id.nav_home);
            mtoolbar.setTitle(R.string.home);
        }

        headerView = mNavigationView.inflateHeaderView(R.layout.drawer_header);
        TextView textViewName = (TextView) headerView.findViewById(R.id.nav_name);
        textViewName.setText(preferencesClass.getUSER_NAME());

        TextView textViewEmail = (TextView) headerView.findViewById(R.id.nav_email);
        textViewEmail.setText(preferencesClass.getUSER_EMAIL());

        setHeaderImage();

        profileChecker();

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                new HomeFragment()).commit();
                        mNavigationView.setCheckedItem(R.id.nav_home);
                        mtoolbar.setTitle(R.string.home);
                        break;
                    case R.id.nav_profile:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                new ProfileFragment()).commit();
                        mNavigationView.setCheckedItem(R.id.nav_profile);
                        mtoolbar.setTitle(R.string.profile);
                        break;
                    case R.id.nav_logout:
                        preferencesClass.clearUser();
                        FirebaseAuth mAuth  = FirebaseAuth.getInstance();
                        Intent logout = new Intent(MainActivity.this,LoginActivity.class);
                        mAuth.signOut();
                        startActivity(logout);
                        finish();
                        break;
                    case R.id.nav_findRide:
                        ConnectivityManager connMgr = (ConnectivityManager)
                                getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                        if (!(networkInfo != null && networkInfo.isConnected())) {
                            //check = true;
                            createDialog();

                        } else {
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                    new FindRideFragment()).commit();
                            mNavigationView.setCheckedItem(R.id.nav_findRide);
                            mtoolbar.setTitle(R.string.find_ride);
                        }
                        break;
                    case R.id.nav_offferRide:
                        ConnectivityManager conMgr = (ConnectivityManager)
                                getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo networkIfo = conMgr.getActiveNetworkInfo();
                        if (!(networkIfo != null && networkIfo.isConnected())) {
                            //check = true;
                            createDialog();

                        } else {

                            checkCars();
                        }
                        break;
                    case R.id.nav_myBookings:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                new MyBookingFragment()).commit();
                        mNavigationView.setCheckedItem(R.id.nav_myBookings);
                        mtoolbar.setTitle(R.string.my_bookings);
                        break;
                    case R.id.nav_myCars:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                new MyVehicles()).commit();
                        mNavigationView.setCheckedItem(R.id.nav_myCars);
                        mtoolbar.setTitle(R.string.my_vehicles);
                        break;
                }
                mDrawerlayout.closeDrawer(GravityCompat.START);

                return false;
            }
        });
    }

    private void createDialog() {

        final Dialog dialogNet = new Dialog(MainActivity.this);
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


    private void drawerSetup() {
        mDrawerlayout = (DrawerLayout) findViewById(R.id.drawerlayout);
        mtoolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mtoolbar);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerlayout, mtoolbar, R.string.home, R.string.home);
        mDrawerlayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if(mDrawerlayout.isDrawerOpen(GravityCompat.START)){
            mDrawerlayout.closeDrawer(GravityCompat.START);
        }else {

            super.onBackPressed();
        }
    }

    private void setHeaderImage(){
        String sImage = preferencesClass.getUserImage();
        if(sImage != null){
            CircularImageView imageView = (CircularImageView) headerView.findViewById(R.id.imageHeader);
            imageView.setImageBitmap(preferencesClass.decodeBase64(sImage));
        }
        else{
            String path = mAuth.getUid();
            mStorageRef.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {

                    CircularImageView imageView = (CircularImageView) headerView.findViewById(R.id.imageHeader);
                    PostHelpingMethod helpingMethod = new PostHelpingMethod(MainActivity.this);
                    String imageURL = uri.toString();
                    GlideApp.with(getApplicationContext()).load(imageURL).apply(new RequestOptions()
                            .placeholder(R.color.colorPrimary)
                            .dontAnimate().skipMemoryCache(true))
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    Bitmap icon = ((BitmapDrawable)resource).getBitmap();
                                    String img = preferencesClass.encodeToBase64(icon,Bitmap.CompressFormat.JPEG, 100);
                                    preferencesClass.setUserImage(img);
                                    return false;
                                }
                            }).into(imageView);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getBaseContext(),exception.toString(),Toast.LENGTH_LONG).show();
                }
            });

        }
    }

    private void profileChecker(){
        Intent intent = getIntent();
        if(intent.hasExtra("FromProfile")){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ProfileFragment()).commit();
            mNavigationView.setCheckedItem(R.id.nav_profile);
            mtoolbar.setTitle(R.string.profile);
        }

    }
    private void checkCars(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getUid()).child("Cars");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new OfferRideFragment()).commit();
                    mNavigationView.setCheckedItem(R.id.nav_offferRide);
                    mtoolbar.setTitle(R.string.offer_ride);
                }
                else{
                    PostHelpingMethod postHelpingMethod = new PostHelpingMethod(MainActivity.this);
                    postHelpingMethod.snackbarMessage("No cars available to post a ride",findViewById(R.id.fragment_container));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onDeleteStatusPassed(String deleteStatus) {
        MyVehicles myVehicles = new MyVehicles();
        Bundle bundle = new Bundle();
        bundle.putString("deleteStatus",deleteStatus);
        myVehicles.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                myVehicles).commit();
    }
}
