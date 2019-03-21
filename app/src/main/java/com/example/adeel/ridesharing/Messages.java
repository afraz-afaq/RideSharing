package com.example.adeel.ridesharing;

public class Messages {
    private String message,reciever;
    private long time;
    private String seen;
    private String isLast = null;
    private String from,id;

    public Messages() {
    }

    public Messages(String message, String reciever, long time, String seen, String from, String id) {
        this.message = message;
        this.reciever = reciever;
        this.time = time;
        this.seen = seen;
        this.from = from;
        this.id = id;
    }

    public String getIsLast() {
        return isLast;
    }

    public void setIsLast(String isLast) {
        this.isLast = isLast;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReciever() {
        return reciever;
    }

    public void setReciever(String reciever) {
        this.reciever = reciever;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
