package com.com.parking.beans;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Lot {

    private String lotID;
    private String lotName;
    private double latitude;
    private double longitude;

    public Lot() {
    }

    public Lot(String lotID, String lotName, double latitude, double longitude) {
        setLotID(lotID);
        setLotName(lotName);
        setLatitude(latitude);
        setLongitude(longitude);
    }

    public String getLotID() {
        return lotID;
    }

    public void setLotID(String lotID) {
        this.lotID = lotID;
    }

    public String getLotName() {
        return lotName;
    }

    public void setLotName(String lotName) {
        this.lotName = lotName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

}
