package com.example.felix.gps_app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
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

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session;
import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxFileSystem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
    final static private Session.AccessType ACCESS_TYPE = Session.AccessType.DROPBOX;

    private AppKeyPair appKeyPair;
    private AndroidAuthSession session;
    private SharedPreferences sharedPreferences;
    private String key;
    private String secret;

    private DbxAccountManager dbxAccountManager;
    private DbxFileSystem dbxFileSystem;


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
        appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);

        sharedPreferences = getSharedPreferences(DROPBOX_NAME, 0);
        key = sharedPreferences.getString(APP_KEY, null);
        secret = sharedPreferences.getString(APP_SECRET, null);


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

        //UploadFileToDropbox upload = new UploadFileToDropbox(this, dropboxAPI, FILE_DIR);
        //upload.execute();
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



    private class UploadFileToDropbox extends AsyncTask<Void, Void, Boolean>
    {
        private DropboxAPI<?> dropbox;
        private String path;
        private Context context;

        public UploadFileToDropbox(Context context, DropboxAPI<?> dropbox, String path)
        {
            this.context = context.getApplicationContext();
            this.dropbox = dropbox;
            this.path = path;
        }

        @Override
        protected Boolean doInBackground(Void... voids)
        {
            Looper.prepare();
            /*
            final File tempDir = context.getCacheDir();
            File tempFile;
            FileWriter fileWriter;


            try
            {
                tempFile = File.createTempFile("file", ".txt", tempDir);
                fileWriter = new FileWriter(tempFile, true);
                fileWriter.write("\n" + textLatitude.getText().toString() + "     " + textLongitude.getText().toString() + "    " + textAddress.getText().toString() +  "     " + calendar.get(Calendar.DAY_OF_MONTH) + "." +  calendar.get(Calendar.MONTH) + "." + calendar.get(Calendar.YEAR) + " " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + "\n");
                fileWriter.close();

                FileInputStream fileInputStream = new FileInputStream(tempFile);
                dropbox.putFile(path + "GPS-Koordinaten.txt", fileInputStream, tempFile.length(), null, null);
                tempFile.delete();
                System.out.println("###### DATEI WIRD HOCHGELADEN ########");
                return true;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch(DropboxUnlinkedException e)
            {
                Toast.makeText(getApplicationContext(), "Problem bei Authentifizierung!", Toast.LENGTH_SHORT);
            }
            catch (DropboxException e)
            {
                e.printStackTrace();
            }
            */

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            /*
            if (result) {
                Toast.makeText(context, "File Uploaded Sucesfully!",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "Failed to upload file", Toast.LENGTH_LONG)
                        .show();
            }
            */
        }
    }
}
