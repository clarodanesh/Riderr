package com.riderrapp.riderr;

public class rideData {
    String date;
    String time;
    String place;
    String offeredBy;
    public rideData(String place, String date, String time, String offeredBy) {
        this.place = place;
        this.date = date;
        this.time = time;
        this.offeredBy = offeredBy;
    }
}
