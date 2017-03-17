package com.gimbal.android.sample;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.com.parking.beans.Lot;
import com.com.parking.beans.LotAndDistance;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parking.utilities.LotAdapter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ListView nearestLots;
    private DatabaseReference mDatabase;
    LatLng currentLocation;
    ArrayList<LotAndDistance> lotsAndDistances;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        nearestLots = (ListView) findViewById(R.id.nearest_lots);

        Bundle bundle = getIntent().getExtras();
        double lat = bundle.getDouble("Latitude");
        double lon = bundle.getDouble("Longitude");
        currentLocation = new LatLng(lat, lon);

        loadListOfLots();

        LotAdapter nearbyLotsListAdapter = new LotAdapter(this, android.R.layout.simple_list_item_1, lotsAndDistances);

        sortLotsByShortestDistance();

        nearestLots.setAdapter(nearbyLotsListAdapter);
        nearbyLotsListAdapter.notifyDataSetChanged();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        CameraUpdate panToCurrentLocation = CameraUpdateFactory.newLatLng(currentLocation);
        mMap.moveCamera(panToCurrentLocation);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 400, null);
        mMap.addMarker(new MarkerOptions()
                .position(currentLocation)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                .title("Your location"));

        for(LotAndDistance lot : lotsAndDistances) {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lot.getLot().getLatitude(), lot.getLot().getLongitude()))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    .alpha(0.8f)
                    .title(lot.getLot().getLotName()));
        }
    }

    private void sortLotsByShortestDistance()
    {
        Collections.sort(lotsAndDistances, new Comparator<LotAndDistance>() {
            public int compare(LotAndDistance o1,
                               LotAndDistance o2) {
                return ((Double)o1.getDistance()).compareTo(o2.getDistance());
            }
        });

    }

    private double calculateDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        return Radius * c;
    }

    private void loadListOfLots(){
        lotsAndDistances = new ArrayList<>();
        mDatabase = FirebaseDatabase.getInstance().getReference("Parking_Lot");
        final DatabaseReference lotsData = mDatabase;
        lotsData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot lotData : dataSnapshot.getChildren()) {
                    Lot lot = lotData.getValue(Lot.class);
                    double distance = calculateDistance(currentLocation, new LatLng(lot.getLatitude(), lot.getLongitude()));
                    Toast.makeText(MapsActivity.this, lot.getLotName() + " " + distance, Toast.LENGTH_LONG).show();
                    lotsAndDistances.add(new LotAndDistance(lot, distance));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}