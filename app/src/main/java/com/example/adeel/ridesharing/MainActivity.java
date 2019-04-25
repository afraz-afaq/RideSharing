package com.example.adeel.ridesharing;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.text.DecimalFormat;


public class MainActivity extends AppCompatActivity implements CarBottomSheet.GetDeleteStatus {

    private static final String TAG = MainActivity.class.getSimpleName();
    DrawerLayout mDrawerlayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    static Toolbar mtoolbar;
    static NavigationView mNavigationView;
    private View headerView;
    private PostHelpingMethod postHelpingMethod;
    private PreferencesClass preferencesClass;
    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    ValueEventListener valueEventListener, checkRatingValueEventListener;
    private DatabaseReference mUserDatabase;
    private String postStatus = "true";
    BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("request")) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new RequestFragment()).commit();
            } else if (intent.hasExtra("accepted")) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new BookedFragment()).commit();
            } else if (intent.hasExtra("canceled")) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FindRideFragment()).commit();
            } else if (intent.hasExtra("requestc")) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FindRideFragment()).commit();
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        startService(new Intent(getBaseContext(), BadgeCount.class));

        IntentFilter filter = new IntentFilter("android.intent.CLOSE_ACTIVITY");
        registerReceiver(mReceiver, filter);
        postHelpingMethod = new PostHelpingMethod(MainActivity.this);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Ratings").child("Users").child(mAuth.getUid()).child("status");
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String status = dataSnapshot.getValue().toString();
                if (status.equals("false")) {
                    postStatus = "false";
                    showRatingDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        FirebaseMessaging.getInstance().subscribeToTopic("pushNotifications");
        preferencesClass = new PreferencesClass(this);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_menu);
        drawerSetup();

        if (savedInstanceState == null) {
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

        final TextView ratingView = headerView.findViewById(R.id.nav_rating);
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Ratings").child("Users").child(mAuth.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String rate = dataSnapshot.child("rating").getValue().toString();
                    if (rate.equals("0")) {
                        ratingView.setText("-");
                    } else {
                        ratingView.setText(rate);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        setHeaderImage(headerView);

        profileChecker();
        final TextView request_count = (TextView) MenuItemCompat.getActionView(mNavigationView.getMenu().
                findItem(R.id.nav_request));
        request_count.setGravity(Gravity.CENTER_VERTICAL);
        request_count.setTypeface(null, Typeface.BOLD);
        request_count.setTextColor(Color.RED);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Posts").child("Active").child(mAuth.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String key = snapshot.getKey();
                        FirebaseDatabase.getInstance().getReference().child("Requests").child(key).child("Pending").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    request_count.setText(dataSnapshot.getChildrenCount() + "");
                                } else {
                                    request_count.setText("");
                                }
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
                    case R.id.nav_request:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                new RequestFragment()).commit();
                        mNavigationView.setCheckedItem(R.id.nav_request);
                        mtoolbar.setTitle("Requests");
                        break;
                    case R.id.nav_logout:
                        preferencesClass.clearUser();
                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        Intent logout = new Intent(MainActivity.this, LoginActivity.class);
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
                            if (preferencesClass.getUserPostStatus().equals("false")) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setMessage("Please complete or cancel your current ride!").setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case DialogInterface.BUTTON_POSITIVE:
                                                dialog.dismiss();

                                        }
                                    }
                                }).show();
                            } else {
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                        new FindRideFragment()).commit();
                                mNavigationView.setCheckedItem(R.id.nav_findRide);
                                mtoolbar.setTitle(R.string.find_ride);
                            }
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
                            if (preferencesClass.getUserPostStatus().equals("false")) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setMessage("Please complete or cancel your current ride!").setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case DialogInterface.BUTTON_POSITIVE:
                                                dialog.dismiss();

                                        }
                                    }
                                }).show();
                            } else {

                                //checkCars();
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                        new OfferRideFragment()).commit();
                                mNavigationView.setCheckedItem(R.id.nav_offferRide);
                                mtoolbar.setTitle(R.string.offer_ride);
                            }
                        }
                        break;
                    case R.id.nav_myBookings:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                new MyBookingFragment(), "bookings").commit();
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

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null && !firebaseUser.isEmailVerified()) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getUid()).child("poststatus").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    preferencesClass.setUserPostStatus(dataSnapshot.getValue().toString());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void showRatingDialog() {
        final Dialog dialogRating = new Dialog(MainActivity.this);
        dialogRating.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogRating.setContentView(R.layout.rating_dialog);
        ImageView buttonClose = dialogRating.findViewById(R.id.imageView_close);
        RatingBar ratingBar = dialogRating.findViewById(R.id.ratingBar);

        dialogRating.show();
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, final float rating, boolean fromUser) {

                final DatabaseReference mUserDatabase;
                mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Ratings").child("Users").child(mAuth.getUid()).child("drivertorate");
                checkRatingValueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String driver = dataSnapshot.getValue().toString();
                        if (!driver.isEmpty()) {
                            mUserDatabase.removeEventListener(checkRatingValueEventListener);


                            final DatabaseReference driverDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Ratings").child("Users").child(driver);
                            valueEventListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshotDriver) {
                                    if (postStatus.equals("false")) {
                                        driverDatabaseReference.removeEventListener(valueEventListener);
                                        postStatus = "true";
                                        Double over_rating = 0.0, count = 0.0;
                                        over_rating = Double.parseDouble(dataSnapshotDriver.child("rating").getValue().toString());
                                        count = Double.parseDouble(dataSnapshotDriver.child("count").getValue().toString());
                                        Double result = ((over_rating * count) + rating) / (count + 1);
                                        DecimalFormat df = new DecimalFormat("#.#");
                                        result = Double.parseDouble(df.format(result));
                                        driverDatabaseReference.child("count").setValue(++count);
                                        driverDatabaseReference.child("rating").setValue(result);
                                        dialogRating.dismiss();
                                        mUserDatabase.setValue("");
                                        FirebaseDatabase.getInstance().getReference().child("Ratings").child("Users").child(mAuth.getUid()).child("status").setValue("true");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            };
                            driverDatabaseReference.addValueEventListener(valueEventListener);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }

                };

                mUserDatabase.addValueEventListener(checkRatingValueEventListener);


            }
        });


        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogRating.cancel();
                FirebaseDatabase.getInstance().getReference().child("Ratings").child("Users").child(mAuth.getUid()).child("status").setValue("true");
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
        mtoolbar = (Toolbar) findViewById(R.id.toolbar_forgot);
        setSupportActionBar(mtoolbar);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerlayout, mtoolbar, R.string.home, R.string.home);
        mDrawerlayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

    }

    @Override
    public void onBackPressed() {
        if (mDrawerlayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerlayout.closeDrawer(GravityCompat.START);
        } else {
            YesNoDialog();
        }

    }

    private void YesNoDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        finish();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:

                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Do you want to exit App?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private void setHeaderImage(final View view) {
        String sImage = preferencesClass.getUserImage();
        if (sImage != null) {
            CircularImageView imageView = (CircularImageView) headerView.findViewById(R.id.imageHeader);
            imageView.setImageBitmap(preferencesClass.decodeBase64(sImage));
        } else {
            String path = mAuth.getUid();
            mStorageRef.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {

                    CircularImageView imageView = (CircularImageView) headerView.findViewById(R.id.imageHeader);
                    PostHelpingMethod helpingMethod = new PostHelpingMethod(MainActivity.this);
                    String imageURL = uri.toString();
                    GlideApp.with(getApplicationContext()).load(imageURL).placeholder(R.drawable.avatar).apply(new RequestOptions()
                            .placeholder(R.color.colorPrimary)
                            .dontAnimate().skipMemoryCache(true))
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    Bitmap icon = ((BitmapDrawable) resource).getBitmap();
                                    String img = preferencesClass.encodeToBase64(icon, Bitmap.CompressFormat.JPEG, 100);
                                    preferencesClass.setUserImage(img);
                                    return false;
                                }
                            }).into(imageView);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    postHelpingMethod.snackbarMessage(exception.toString(), view);
                }
            });

        }
    }

    private void profileChecker() {
        Intent intent = getIntent();
        if (intent.hasExtra("FromProfile")) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ProfileFragment()).commit();
            mNavigationView.setCheckedItem(R.id.nav_profile);
            mtoolbar.setTitle(R.string.profile);
        }

    }


    @Override
    public void onDeleteStatusPassed(String deleteStatus) {
        MyVehicles myVehicles = new MyVehicles();
        Bundle bundle = new Bundle();
        bundle.putString("deleteStatus", deleteStatus);
        myVehicles.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                myVehicles).commit();
    }

}
