package com.example.adeel.ridesharing;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Afraz on 7/23/2018.
 */

public class PreferencesClass {

    private Activity activity;
    public static String TAG = "PREFERENCES";
    public PreferencesClass(Activity activity) {
        this.activity = activity;
    }

    public void saveUser(DataSnapshot dataSnapshot){

        SharedPreferences sharedPreferences = activity.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(AppConstants.USER_NAME,dataSnapshot.child("name").getValue().toString());
        editor.putString(AppConstants.USER_EMAIL,dataSnapshot.child("email").getValue().toString());
        editor.putString(AppConstants.USER_CONTACT,dataSnapshot.child("contact").getValue().toString());
        editor.putString(AppConstants.USER_REGNO,dataSnapshot.child("regno").getValue().toString());
        editor.putString(AppConstants.USER_DEPT,dataSnapshot.child("dept").getValue().toString());
        editor.putString(AppConstants.USER_GENDER,dataSnapshot.child("gender").getValue().toString());
        editor.putString(AppConstants.USER_POSTSTATUS,dataSnapshot.child("poststatus").getValue().toString());
        //editor.putString(AppConstants.USER_IMAGE,image);
        editor.apply();

        Log.d(TAG,sharedPreferences.getString(AppConstants.USER_CONTACT, null));

        //Log.d(TAG,image+" ");

}

public void setUserImage(String image){
    SharedPreferences sharedPreferences = activity.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString(AppConstants.USER_IMAGE,image);
    editor.apply();

    Log.d(TAG,"IMAGE: "+image, null);

}
    public void setUserPassword(String password){
        SharedPreferences sharedPreferences = activity.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(AppConstants.USER_PASSWORD,password);
        editor.apply();

    }

    public void setUserPostStatus(String status){
        SharedPreferences sharedPreferences = activity.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(AppConstants.USER_POSTSTATUS,status);
        editor.apply();

    }

    public void setUserToken(String token){
        SharedPreferences sharedPreferences = activity.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(AppConstants.USER_TOKEN,token);
        editor.apply();

    }

//    public void setUserPhoneStatus(String status){
//        SharedPreferences sharedPreferences = activity.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString(AppConstants.USER_PHONESTATUS,status);
//        editor.apply();
//
//    }

    public void setUserContact(String contact){
        SharedPreferences sharedPreferences = activity.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(AppConstants.USER_CONTACT,contact);
        editor.apply();

    }


    public String getUSER_NAME() {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        String name =  sharedPreferences.getString(AppConstants.USER_NAME, null);
        Log.d(TAG,"NAME: "+name);
        return name;
    }

    public String getUserPostStatus() {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        String status =  sharedPreferences.getString(AppConstants.USER_POSTSTATUS, null);
        Log.d(TAG,"POST STATUS: "+status);
        return status;
    }

    public String getUSER_TOKEN() {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        String token =  sharedPreferences.getString(AppConstants.USER_TOKEN, null);
        Log.d(TAG,"TOKEN: "+token);
        return token;
    }

//    public String getUSER_PHONESTATUS() {
//        SharedPreferences sharedPreferences = activity.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
//        String status =  sharedPreferences.getString(AppConstants.USER_PHONESTATUS, null);
//        Log.d(TAG,"PHONESTATUS: "+status);
//        return status;
//    }

    public String getUSER_EMAIL() {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        String email = sharedPreferences.getString(AppConstants.USER_EMAIL, null);
        Log.d(TAG,"EMAIL: "+email);
        return email;
    }
    public String getUSER_Password() {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        String password = sharedPreferences.getString(AppConstants.USER_PASSWORD, null);
        Log.d(TAG,"PASWORD: "+password);
        return password;
    }


    public String getUserRegno() {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        String regno = sharedPreferences.getString(AppConstants.USER_REGNO, null);
        Log.d(TAG,"REGNO: "+regno);
        return regno;
    }

    public  String getUserContact() {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        String contact =  sharedPreferences.getString(AppConstants.USER_CONTACT, null);
        Log.d(TAG,"CONTACT: "+contact);
        return contact;
    }

    public  String getUserGender() {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(AppConstants.USER_GENDER, null);
    }

    public  String getUserDept() {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        String dept = sharedPreferences.getString(AppConstants.USER_DEPT, null);
        Log.d(TAG,"DEPT: "+dept);
        return dept;
    }

    public  String getUserImage() {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(AppConstants.USER_IMAGE, null);
    }



    public void clearUser(){

        SharedPreferences sharedPreferences = activity.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }


    public String encodeToBase64(Bitmap image, Bitmap.CompressFormat compressFormat, int quality)
    {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        String imageString =  Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
        return imageString;
    }

    public static Bitmap decodeBase64(String input)
    {
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public void saveCarsList(ArrayList<Cars> cars){
        SharedPreferences sharedPreferences = activity.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String carsString = gson.toJson(cars);
        editor.putString(AppConstants.CARS_LIST,carsString);

        editor.apply();
    }

    public void saveBikesList(ArrayList<Cars> bikes){
        SharedPreferences sharedPreferences = activity.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String bikesString = gson.toJson(bikes);
        editor.putString(AppConstants.BIKES_LIST,bikesString);

        editor.apply();
    }

    public ArrayList<Cars> getCarsList(){
        SharedPreferences sharedPreferences = activity.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String carsString = sharedPreferences.getString(AppConstants.CARS_LIST,null);
        Type type = new TypeToken<List<Cars>>(){}.getType();
        ArrayList<Cars> carsList = gson.fromJson(carsString, type);
        return carsList;
    }

    public ArrayList<Cars> getBikesList(){
        SharedPreferences sharedPreferences = activity.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String bikesString = sharedPreferences.getString(AppConstants.BIKES_LIST,null);
        Type type = new TypeToken<List<Cars>>(){}.getType();
        ArrayList<Cars> bikesList = gson.fromJson(bikesString, type);
        return bikesList;
    }
}