package com.example.adeel.ridesharing;

import android.view.View;

import java.util.ArrayList;

/**
 * Simple POJO model for example
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class FindRideItem {

    private String price;
    private String fromAddress;
    private String toAddress;
    private String seats;
    private String distance;
    private String time;
    private String carName;
    private String carColor;
    private String carReg;
    private String driverName;
    private String driverUid;
    private String postId;
    private String image;

    private View.OnClickListener requestBtnClickListener;

    public FindRideItem() {
    }

    public FindRideItem(String driverUid,String postId,String price, String fromAddress, String toAddress, String seats, String distance, String time, String carName, String carColor, String carReg, String driverName, String image) {
        this.price = price;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.seats = seats;
        this.distance = distance;
        this.time = time;
        this.carName = carName;
        this.carColor = carColor;
        this.carReg = carReg;
        this.driverName = driverName;
        this.driverUid = driverUid;
        this.postId = postId;
        this.image = image;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public String getSeats() {
        return seats;
    }

    public void setSeats(String seats) {
        this.seats = seats;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCarName() {
        return carName;
    }

    public void setCarName(String carName) {
        this.carName = carName;
    }

    public String getCarColor() {
        return carColor;
    }

    public void setCarColor(String carColor) {
        this.carColor = carColor;
    }

    public String getCarReg() {
        return carReg;
    }

    public void setCarReg(String carReg) {
        this.carReg = carReg;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverUid() {
        return driverUid;
    }

    public void setDriverUid(String driverUid) {
        this.driverUid = driverUid;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public View.OnClickListener getRequestBtnClickListener() {
        return requestBtnClickListener;
    }

    public void setRequestBtnClickListener(View.OnClickListener requestBtnClickListener) {
        this.requestBtnClickListener = requestBtnClickListener;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FindRideItem item = (FindRideItem) o;

        if (!seats.equals(item.seats)) return false;
        if (price != null ? !price.equals(item.price) : item.price != null) return false;
        if (fromAddress != null ? !fromAddress.equals(item.fromAddress) : item.fromAddress != null)
            return false;
        if (toAddress != null ? !toAddress.equals(item.toAddress) : item.toAddress != null)
            return false;
        if (time != null ? !time.equals(item.time) : item.time != null) return false;
        return !(time != null ? !time.equals(item.time) : item.time != null);

    }

    @Override
    public int hashCode() {
        int result = price != null ? price.hashCode() : 0;
        result = 31 * result + (price != null ? price.hashCode() : 0);
        result = 31 * result + (fromAddress != null ? fromAddress.hashCode() : 0);
        result = 31 * result + (toAddress != null ? toAddress.hashCode() : 0);
        result = 31 * result + (seats != null ? seats.hashCode() : 0);;
        result = 31 * result + (time != null ? time.hashCode() : 0);
        return result;
    }

    /**
     * @return List of elements prepared for tests
     */
    public static ArrayList<FindRideItem> getTestingList() {
        ArrayList<FindRideItem> items = new ArrayList<>();
        items.add(new FindRideItem("32432dfsd","3452342","$14", "Nipa Chorangi", "Bahria University", "3","2.5km", "10min", "Mehran", "Black", "RRR-098","Farhan","987423abc3274"));
        items.add(new FindRideItem("32432dfsd","3452342","$14", "Nipa Chorangi", "Bahria University", "3","2.5km", "10min", "Mehran", "Black", "RRR-098","Farhan","987423abc3274"));
        return items;

    }

}
