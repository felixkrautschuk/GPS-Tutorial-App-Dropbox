package com.example.felix.gps_app;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;


public class MainActivity extends Activity
{
    GoogleMap map;
    private TextView textLatitude;
    private TextView textLongitude;
    private TextView textAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textLatitude = (TextView) findViewById(R.id.TextViewLatValue);
        textLongitude = (TextView) findViewById(R.id.TextViewLonValue);
        textAddress = (TextView) findViewById(R.id.TextViewAddValue);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();



        //Aktiviere mobiles Internet
        try
        {
            setWlanAndMobileDataEnabled(getApplicationContext(), true);
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchMethodException e)
        {
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();
        }

        new LocationAsyncTask().execute();
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

    private void setWlanAndMobileDataEnabled(Context context, boolean enabled) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException
    {
        final ConnectivityManager conman = (ConnectivityManager)  context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final Class conmanClass = Class.forName(conman.getClass().getName());
        final Field connectivityManagerField = conmanClass.getDeclaredField("mService");
        connectivityManagerField.setAccessible(true);
        final Object connectivityManager = connectivityManagerField.get(conman);
        final Class connectivityManagerClass =  Class.forName(connectivityManager.getClass().getName());
        final Method setMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
        setMobileDataEnabledMethod.setAccessible(true);

        setMobileDataEnabledMethod.invoke(connectivityManager, enabled);

        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(enabled);
    }

    private class LocationAsyncTask extends AsyncTask<Void, Void, Void> implements LocationListener
    {
        LocationManager locationManager;
        Location location;


        @Override
        protected Void doInBackground(Void... voids)
        {
            Looper.prepare();
            locationManager  = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 20000, 0, this);
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);


            return null;
        }

        @Override
        protected void onPostExecute(Void Void)
        {
            super.onPostExecute(Void);
            if(location != null)
            {
                onLocationChanged(location);
            }
        }

        @Override
        public void onLocationChanged(Location location)
        {
            Geocoder geocoder;
            List<Address> listAddresses = null;

            //Standord markieren
            if(map != null)
            {
                drawMarker(location);
            }

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
                textAddress.setText("suche Adresse...");
            }
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
    }
}

class PositionAsyncTask extends AsyncTask<Void, Void, Void> implements LocationListener
{

    @Override
    protected Void doInBackground(Void... voids)
    {
        return null;
    }

    @Override
    public void onLocationChanged(Location location)
    {

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
}
