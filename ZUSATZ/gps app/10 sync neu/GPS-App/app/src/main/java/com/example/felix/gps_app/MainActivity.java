package com.example.felix.gps_app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Toast;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

//App-Key:      tzm81cu6g6iz3mq
//App-Secret:   88hg5tljg20tpm6

//Access-Token: WJWOvpPSU4UAAAAAAAATfcOSKmC9Iw5SUZppRnRMelUhewuH9OUXP5v5FxjegxJF
//This access token can be used to access your account (felixkrautschuk@yahoo.de) via the API.

//  WJWOvpPSU4UAAAAAAAAThUHQcfFc_Xc4MziShdJszwF5zzUyYOtwCZp8dc_0Btmw

public class MainActivity extends Activity implements LocationListener
{
    LocationManager locationManager;
    Location location;
    GoogleMap map;
    private TextView textLatitude;
    private TextView textLongitude;
    private TextView textAddress;
    Calendar calendar;
    SimpleDateFormat simpleDateFormat;
    String formattedDate;

    private final static String FILE_DIR = "/Apps/GpsAppWithDBoxTracking/";
    private final static String DROPBOX_NAME = "dropbox_prefs";
    private final static String APP_KEY = "tzm81cu6g6iz3mq";
    private final static String APP_SECRET = "88hg5tljg20tpm6";
    static final int REQUEST_LINK_TO_DBX = 0;


    private DbxAccountManager dbxAccountManager;
    private DbxFileSystem dbxFileSystem;
    DbxPath dbxPath;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Textfelder
        textLatitude = (TextView) findViewById(R.id.TextViewLatValue);
        textLongitude = (TextView) findViewById(R.id.TextViewLonValue);
        textAddress = (TextView) findViewById(R.id.TextViewAddValue);

        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        formattedDate = simpleDateFormat.format(calendar.getTime());


        //Google Maps Bestandteile
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, this);


        //Dropbox
        dbxPath = new DbxPath("/Apps/GpsAppWithDBoxTracking/GPS-Koordinaten.txt");

        super.onResume();
        dbxAccountManager = DbxAccountManager.getInstance(getApplicationContext(), APP_KEY, APP_SECRET);
        dbxAccountManager.startLink(this, REQUEST_LINK_TO_DBX);



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



        if(location != null)
        {
            super.onResume();
            onLocationChanged(location);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_LINK_TO_DBX)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                Toast.makeText(getApplicationContext(), "Succeeded!!!", Toast.LENGTH_SHORT).show();
                try
                {
                    super.onResume();

                    dbxFileSystem = DbxFileSystem.forAccount(dbxAccountManager.getLinkedAccount());
                }
                catch (DbxException.Unauthorized unauthorized)
                {
                    unauthorized.printStackTrace();
                }
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Failed!!!", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onLocationChanged(Location location)
    {
        Geocoder geocoder;
        List<Address> listAddresses = null;
        calendar = Calendar.getInstance();


        //Standord marlieren
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
}
