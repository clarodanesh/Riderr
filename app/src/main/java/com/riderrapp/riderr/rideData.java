package com.riderrapp.riderr;

//rideData class which will hold the ride data and then will be put into the ride data list
//will be used to hold data to populate foundrides activity cardviews
public class rideData {
    String date;
    String time;
    String place;
    String offeredBy;
    String rideId;
    String price;
    String drating;

    public rideData(String place, String date, String time, String offeredBy, String rideId, String price, String drating) {
        this.place = place;
        this.date = date;
        this.time = time;
        this.offeredBy = offeredBy;
        this.rideId = rideId;
        this.price = price;
        this.drating = drating;
    }
}
