package com.riderrapp.riderr;

public class rideData {
    String date;
    String time;
    String place;
    String offeredBy;
    String rideId;
    public rideData(String place, String date, String time, String offeredBy, String rideId) {
        this.place = place;
        this.date = date;
        this.time = time;
        this.offeredBy = offeredBy;
        this.rideId = rideId;
    }
}
