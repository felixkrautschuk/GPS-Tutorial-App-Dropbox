package com.example.felix.gps_app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session;
import com.dropbox.client2.session.TokenPair;
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

//App-Key:      664a1cwr6syv2y4
//App-Secret:   fpvv0h58e1cbenn

//Access-Token: WJWOvpPSU4UAAAAAAAATfcOSKmC9Iw5SUZppRnRMelUhewuH9OUXP5v5FxjegxJF
//This access token can be used to access your account (felixkrautschuk@yahoo.de) via the API.

public class MainActivity extends Activity implements LocationListener
{
    LocationManager locationManager;
    Location location;
    GoogleMap map;
    Criteria criteria;
    private TextView textLatitude;
    private TextView textLongitude;
    private TextView textAddress;

    private DropboxAPI<AndroidAuthSession> dropbox;

    private final static String FILE_DIR = "/GPS/";
    private final static String DROPBOX_NAME = "dropbox_prefs";
    private final static String APP_KEY = "664a1cwr6syv2y4";
    private final static String APP_SECRET = "fpvv0h58e1cbenn";
    final static private Session.AccessType ACCESS_TYPE = Session.AccessType.DROPBOX;

    private DropboxAPI<AndroidAuthSession> dropboxAPI;
    private AppKeyPair appKeyPair;
    private AndroidAuthSession session;
    private SharedPreferences sharedPreferences;
    private String key;
    private String secret;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Textfelder
        textLatitude = (TextView) findViewById(R.id.TextViewLatValue);
        textLongitude = (TextView) findViewById(R.id.TextViewLonValue);
        textAddress = (TextView) findViewById(R.id.TextViewAddValue);


        //Google Maps Bestandteile
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        criteria = new Criteria();
        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);


        //Dropbox
        appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
        dropboxAPI = new DropboxAPI<AndroidAuthSession>(session);
        sharedPreferences = getSharedPreferences(DROPBOX_NAME, 0);
        key = sharedPreferences.getString(APP_KEY, null);
        secret = sharedPreferences.getString(APP_SECRET, null);

        dropboxAPI.getSession().startOAuth2Authentication(MainActivity.this);


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

        System.out.println("##################################################################");

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

    @Override
    protected void onResume()
    {
        super.onResume();
        if (dropboxAPI.getSession().authenticationSuccessful()) {
            try {
                dropboxAPI.getSession().finishAuthentication();

                String accessToken = dropboxAPI.getSession().getOAuth2AccessToken();

                TokenPair tokens = session.getAccessTokenPair();
                SharedPreferences prefs = getSharedPreferences(DROPBOX_NAME, 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(APP_KEY, tokens.key);
                editor.putString(APP_SECRET, tokens.secret);
                editor.commit();

            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }
}
