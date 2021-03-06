package com.example.adeel.ridesharing;

/**
 * Created by Afraz on 2/11/2019.
 */

public class HistoryPost {

    String from;
    String to;
    String price;
    String via;
    String vehicle;
    String date;
    String driver;
    String regno;

    public HistoryPost(String from, String to, String price, String via, String vehicle, String date, String driver, String regno) {
        this.from = from;
        this.to = to;
        this.price = price;
        this.via = via;
        this.vehicle = vehicle;
        this.date = date;
        this.driver = driver;
        this.regno = regno;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getVia() {
        return via;
    }

    public void setVia(String via) {
        this.via = via;
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getRegno() {
        return regno;
    }

    public void setRegno(String regno) {
        this.regno = regno;
    }
}
