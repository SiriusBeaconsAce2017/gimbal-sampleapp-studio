package com.com.parking.beans;

public class LotAndDistance {
    private Lot lot;
    private double distance;

    public LotAndDistance() {
    }

    public LotAndDistance(Lot lot, double distance) {
        setLot(lot);
        setDistance(distance);
    }

    public Lot getLot() {
        return lot;
    }

    public void setLot(Lot lot) {
        this.lot = lot;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

}
