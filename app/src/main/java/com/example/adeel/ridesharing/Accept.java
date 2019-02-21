package com.example.adeel.ridesharing;

import android.view.View;

public class Accept {
    String mName, mSeats, mLoc, mImage;
    View.OnClickListener trackOnClickListener, callOnClickListener,chatOnClickListener;

    public Accept(String mName, String mSeats, String mLoc, String mImage, View.OnClickListener trackOnClickListener, View.OnClickListener callOnClickListener, View.OnClickListener chatOnClickListener) {
        this.mName = mName;
        this.mSeats = mSeats;
        this.mLoc = mLoc;
        this.mImage = mImage;
        this.trackOnClickListener = trackOnClickListener;
        this.callOnClickListener = callOnClickListener;
        this.chatOnClickListener = chatOnClickListener;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmSeats() {
        return mSeats;
    }

    public void setmSeats(String mSeats) {
        this.mSeats = mSeats;
    }

    public String getmLoc() {
        return mLoc;
    }

    public void setmLoc(String mLoc) {
        this.mLoc = mLoc;
    }

    public String getmImage() {
        return mImage;
    }

    public void setmImage(String mImage) {
        this.mImage = mImage;
    }

    public View.OnClickListener getTrackOnClickListener() {
        return trackOnClickListener;
    }

    public void setTrackOnClickListener(View.OnClickListener trackOnClickListener) {
        this.trackOnClickListener = trackOnClickListener;
    }

    public View.OnClickListener getCallOnClickListener() {
        return callOnClickListener;
    }

    public void setCallOnClickListener(View.OnClickListener callOnClickListener) {
        this.callOnClickListener = callOnClickListener;
    }

    public View.OnClickListener getChatOnClickListener() {
        return chatOnClickListener;
    }

    public void setChatOnClickListener(View.OnClickListener chatOnClickListener) {
        this.chatOnClickListener = chatOnClickListener;
    }
}
