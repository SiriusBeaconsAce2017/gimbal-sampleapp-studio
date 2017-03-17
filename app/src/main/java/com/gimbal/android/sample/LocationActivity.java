package com.gimbal.android.sample;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.parking.utilities.FontManager;

public class LocationActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener {

    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 0;
    GoogleApiClient mGoogleApiClient;

    TextView locationMarker;
    TextView userLocation;
    Button book;

    double latitude;
    double longitude;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        locationMarker = (TextView) findViewById(R.id.location_marker);
        locationMarker.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        userLocation = (TextView) findViewById(R.id.user_location);
        book = (Button) findViewById(R.id.book);

        book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LocationActivity.this, MapsActivity.class);
                intent.putExtra("Latitude",latitude);
                intent.putExtra("Longitude",longitude);
                startActivity(intent);
            }
        });
        if(checkPlayServices()){
            buildGoogleApiClient();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private boolean checkPlayServices() {
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        int resultCode = availability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            return false;
        }
        return true;
    }

    private void displayLocation() {
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION )
                != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this,
                                                new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                                                MY_PERMISSION_ACCESS_COARSE_LOCATION );
        }
        Location mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
            String text = LocationAddress.getAddressFromLocation(latitude, longitude, this);
            userLocation.setText("You're at " + text);
        } else {
            userLocation.setText("Couldn't get the location. Make sure location is enabled on the device");
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();
    }
}
