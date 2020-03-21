package com.riderrapp.riderr;

public class rideData {
    String date;
    String time;
    String place;
    String offeredBy;
    String rideId;
    String price;

    public rideData(String place, String date, String time, String offeredBy, String rideId, String price) {
        this.place = place;
        this.date = date;
        this.time = time;
        this.offeredBy = offeredBy;
        this.rideId = rideId;
        this.price = price;
    }
}
