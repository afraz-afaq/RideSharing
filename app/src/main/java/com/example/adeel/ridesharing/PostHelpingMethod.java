package com.example.adeel.ridesharing;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import fcm.androidtoandroid.FirebasePush;
import fcm.androidtoandroid.connection.PushNotificationTask;
import fcm.androidtoandroid.model.Notification;

/**
 * Created by Afraz on 12/30/2018.
 */

public class PostHelpingMethod {

    private Activity activity;

    public PostHelpingMethod(FragmentActivity activity) {
        this.activity = activity;
    }

    public Address geoLocateSearch(String searchtxt, String TAG) {
        Address address = null;
        Geocoder geocoder = new Geocoder(activity);
        List<Address> addressList = new ArrayList<>();
        try {
            addressList = geocoder.getFromLocationName(searchtxt, 1); //to get 1 matching location address
        } catch (IOException e) {

            //Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
            Log.d(TAG, e.getMessage());
        }
        if (addressList.size() > 0)//if address is found
        {
            address = addressList.get(0);
            Log.d(TAG, "Address Found " + address);
        }
        return address;
    }


    public double getFare(double distance, double time) {
        return ((distance * 2.25) + (time * 1.55) + 40);
    }

    public double getFare(double distance, double time, double perKM, double perMin, double base) {
        return ((distance * perKM) + (time * perMin) + base);
    }

    public void sendNotification(String title, String body, String token) {
        final FirebasePush firebasePush = new FirebasePush("AIzaSyDARseKL-2opSy4uMzLigTdjv-Mo6AyTsQ") ;

        firebasePush.setAsyncResponse(new PushNotificationTask.AsyncResponse() {
            @Override
            public void onFinishPush(@NotNull String ouput) {
                Log.e("OUTPUT", ouput);
            }
        });
        firebasePush.setNotification(new Notification(title,body));

        firebasePush.sendToToken(token);
    }


    public ProgressDialog createProgressDialog(String title, String message) {
        ProgressDialog mDialog = new ProgressDialog(activity);
        mDialog.setTitle(title);
        mDialog.setMessage(message);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);
        return mDialog;
    }

    public Dialog createDialog(String title) {
        Dialog dialog;
        dialog = new Dialog(activity);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setTitle(title);
        dialog.setContentView(R.layout.progressdialoglayout);
        return dialog;
    }

    public void snackbarMessage(String msg, View v) {
        Snackbar sb = Snackbar.make(v, msg, Snackbar.LENGTH_LONG);
        View sbView = sb.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorAccent));
        sb.show();
    }

    public boolean withInRange(LatLng loc1, LatLng loc2) {
        boolean check = false;
        float[] result = new float[5];
        Location.distanceBetween(loc1.latitude, loc1.longitude, loc2.latitude, loc2.longitude, result);
        if (result[0] <= 1000)
            check = true;
        return check;
    }

    public float distanceBetween(LatLng loc1, LatLng loc2) {
        boolean check = false;
        float[] result = new float[5];
        Location.distanceBetween(loc1.latitude, loc1.longitude, loc2.latitude, loc2.longitude, result);
        return result[0];
    }

    public String decimalPlacer(double value,int placement){
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(placement);
        return df.format(value);
    }
}
