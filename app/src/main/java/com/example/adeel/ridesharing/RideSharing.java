package com.example.adeel.ridesharing;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Afraz on 3/7/2019.
 */

public class RideSharing extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
