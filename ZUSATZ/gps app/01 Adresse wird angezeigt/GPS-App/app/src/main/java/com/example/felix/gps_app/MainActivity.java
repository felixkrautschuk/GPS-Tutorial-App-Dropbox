package com.example.felix.gps_app;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MainActivity extends Activity implements LocationListener
{
    LocationManager locationManager;
    Location location;
    GoogleMap map;
    Criteria criteria;
    String provider;
    Intent intent;
    private TextView textLatitude;
    private TextView textLongitude;
    private TextView textAddress;
    private TextView textProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textLatitude = (TextView) findViewById(R.id.TextViewLatValue);
        textLongitude = (TextView) findViewById(R.id.TextViewLonValue);
        textAddress = (TextView) findViewById(R.id.TextViewAddValue);
        textProvider = (TextView) findViewById(R.id.TextViewProviderValue);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, true);
        location = locationManager.getLastKnownLocation(provider);
        locationManager.requestLocationUpdates(provider, 20000, 0, this);


        if(location != null)
        {
            onLocationChanged(location);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location)
    {
        Geocoder geocoder;
        List<Address> listAddresses = null;

        //Standord marlieren
        if(map != null)
        {
            drawMarker(location);
        }

        //Provider (GPS, Network,...) angeben
        System.out.println("Provider " + provider + " has been selected.");

        //Breitengrad und Längengrad ermitteln und anzeigen
        double lat = location.getLatitude();
        double
                lng = location.getLongitude();
        textLatitude.setText(String.valueOf(lat));
        textLongitude.setText(String.valueOf(lng));

        //Adresse ermitteln
        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        try
        {
            listAddresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        if(listAddresses != null && listAddresses.size() > 0)
        {
            //hole erste Adresse
            Address address = listAddresses.get(0);
            String stringAddress = String.format("%s, %s, %s", address.getMaxAddressLineIndex() > 0 ? address
                            .getAddressLine(0) : "",
                    // Locality is usually a city
                    address.getLocality(),
                    // The country of the address
                    address.getCountryName());
            textAddress.setText(stringAddress);
        }

        else
        {
            textAddress.setText("Es wurde keine passende Adresse gefunden!");
        }

        //Provider anzeigen
        textProvider.setText(provider);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle)
    {

    }

    @Override
    public void onProviderEnabled(String s)
    {

    }

    @Override
    public void onProviderDisabled(String s)
    {

    }

    private void drawMarker(Location location)
    {
        LatLng currentPosition;

        map.clear();
        currentPosition= new LatLng(location.getLatitude(), location.getLongitude());

        //Zoom zur aktuellen Position
        map.animateCamera(CameraUpdateFactory.newLatLng(currentPosition));

        //aktuelle Position markieren
        map.addMarker(new MarkerOptions().position(currentPosition).snippet("Lat: " + location.getLatitude() + "Lng: " + location.getLongitude()));
    }
}
