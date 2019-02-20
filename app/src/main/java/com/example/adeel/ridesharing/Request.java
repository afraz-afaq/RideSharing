package com.example.adeel.ridesharing;

import android.view.View;
import android.widget.Button;

public class Request {
    String mName, mSeats, mLoc;
    View.OnClickListener cancelOnClickListener, acceptOnClickListener;

    public Request(String mName, String mSeats, String mLoc, View.OnClickListener cancelOnClickListener, View.OnClickListener acceptOnClickListener) {
        this.mName = mName;
        this.mSeats = mSeats;
        this.mLoc = mLoc;
        this.cancelOnClickListener = cancelOnClickListener;
        this.acceptOnClickListener = acceptOnClickListener;
    }

    public Request(String mName, String mSeats, String mLoc, View.OnClickListener cancelOnClickListener) {
        this.mName = mName;
        this.mSeats = mSeats;
        this.mLoc = mLoc;
        this.cancelOnClickListener = cancelOnClickListener;
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

    public View.OnClickListener getCancelOnClickListener() {
        return cancelOnClickListener;
    }

    public void setCancelOnClickListener(View.OnClickListener cancelOnClickListener) {
        this.cancelOnClickListener = cancelOnClickListener;
    }

    public View.OnClickListener getAcceptOnClickListener() {
        return acceptOnClickListener;
    }

    public void setAcceptOnClickListener(View.OnClickListener acceptOnClickListener) {
        this.acceptOnClickListener = acceptOnClickListener;
    }
}
