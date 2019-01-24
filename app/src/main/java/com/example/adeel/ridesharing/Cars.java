package com.example.adeel.ridesharing;

public class Cars {
    private String mCarColor,mCarName,mCarRegNo;

    public Cars() {
    }

    public Cars(String mCarColor, String mCarName, String mCarRegNo) {
        this.mCarColor = mCarColor;
        this.mCarName = mCarName;
        this.mCarRegNo = mCarRegNo;
    }

    @Override
    public String toString() {
        return "Cars{" +
                "mCarColor='" + mCarColor + '\'' +
                ", mCarName='" + mCarName + '\'' +
                ", mCarRegNo='" + mCarRegNo + '\'' +
                '}';
    }

    public String getmCarColor() {
        return mCarColor;
    }

    public void setmCarColor(String mCarColor) {
        this.mCarColor = mCarColor;
    }

    public String getmCarName() {
        return mCarName;
    }

    public void setmCarName(String mCarName) {
        this.mCarName = mCarName;
    }

    public String getmCarRegNo() {
        return mCarRegNo;
    }

    public void setmCarRegNo(String mCarRegNo) {
        this.mCarRegNo = mCarRegNo;
    }
}
