package com.example.adeel.ridesharing;

import android.widget.Button;

public class Request {
    String mName,mSeats,mLoc;
    Button mConfirm,mCancel;

    public Request(String mName, String mSeats, String mLoc, Button mConfirm, Button mCancel) {
        this.mName = mName;
        this.mSeats = mSeats;
        this.mLoc = mLoc;
        this.mConfirm = mConfirm;
        this.mCancel = mCancel;
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

    public Button getmConfirm() {
        return mConfirm;
    }

    public void setmConfirm(Button mConfirm) {
        this.mConfirm = mConfirm;
    }

    public Button getmCancel() {
        return mCancel;
    }

    public void setmCancel(Button mCancel) {
        this.mCancel = mCancel;
    }
}
