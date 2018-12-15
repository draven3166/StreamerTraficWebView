package com.erbol.bo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.erbol.bo.Adapters.ConflictAdapter;
import com.erbol.bo.Adapters.OnListenerLine;
import com.erbol.bo.DataModel.Conflicts;
import com.erbol.bo.DataModel.DBAdapter;
import com.erbol.bo.DataModel.Figures;
import com.erbol.bo.Fragments.FDialog_conflict;
import com.erbol.bo.Fragments.FDialog_statistic;
import com.erbol.bo.Utils.ConstantsUtil;
import com.erbol.bo.Utils.DataParser;
import com.erbol.bo.Utils.QuickActionBar;
import com.erbol.bo.Utils.QuickActionIcons;
import com.erbol.bo.Utils.ServiceUtil;
import com.erbol.bo.Utils.StateEthernet;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMyLocationChangeListener,
        LocationListener, TextToSpeech.OnInitListener, PlaceSelectionListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private final int inclinacionI = 0;
    private final int inclinacionF = 35;
    private final int mapaZoom = 17;
    private LatLng posDestino;

    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    private String drawMode, provider, nameDestiny;
    private TextToSpeech tts;
    private int MY_DATA_CHECK_CODE = 208;
    private PlaceAutocompleteFragment autoFragment;
    private Place mPlace;
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private LinearLayout llSearch, llNavigate;
    private boolean activeSearch, drouteing, activeConflict, activeList;
    private Button btnConfli;
    private ListView lvConfli;
    private Switch swNavigate;

    private OnListenerLine mListenerNV;
    private FDialog_conflict dlgNuevaVisita;
    private FDialog_statistic dlgGraphic;
    private ConflictAdapter mAdapter;
    private ArrayList<Conflicts> conflictos;
    private QuickActionBar qab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        activeSearch = true;
        drouteing = false;
        activeConflict = false;
        activeList = true;
        drawMode = "driving";
        nameDestiny = "Dirección desconocida";
        llSearch = (LinearLayout)findViewById(R.id.llsearch);
        llNavigate = (LinearLayout)findViewById(R.id.llnavigate);
        swNavigate = (Switch)findViewById(R.id.sw_navigate);
        btnConfli = (Button)findViewById(R.id.btn_confli);
        lvConfli = (ListView)findViewById(R.id.lv_confli);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fgt_map);
        mapFragment.getMapAsync(this);

        autoFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS).build();
        autoFragment.setFilter(typeFilter);
        autoFragment.setOnPlaceSelectedListener(this);

        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

        provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if(!provider.contains("gps")){
            AlertDialog.Builder dialogGPS=new AlertDialog.Builder(this);
            dialogGPS.setTitle("GPS Desactivado");
            dialogGPS.setMessage("¿Desea habilitar el GPS?");
            dialogGPS.setCancelable(false);
            dialogGPS.setPositiveButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dlg, int id) {
                    dlg.cancel();
                }
            });
            dialogGPS.setNegativeButton("Configurar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dlg, int id) {
                    Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(callGPSSettingIntent);
                    dlg.cancel();
                }
            });
            dialogGPS.show();
        }
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setHomeAsUpIndicator(R.mipmap.ic_nav_menu);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        navView = (NavigationView)findViewById(R.id.navview);
        navView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.menu_trafico:
                                if (menuItem.isChecked()) {
                                    menuItem.setChecked(false);
                                    mMap.setTrafficEnabled(false);
                                } else {
                                    menuItem.setChecked(true);
                                    mMap.setTrafficEnabled(true);
                                }
                                break;
                            case R.id.menu_conflic:
                                mListenerNV = new OnListenerLine() {

                                    @Override
                                    public void onListenerCanceled() {
                                        mListenerNV = null;
                                        dlgGraphic.cancel();
                                    }
                                };
                                dlgGraphic = new FDialog_statistic(MapsActivity.this, mListenerNV, 1);
                                dlgGraphic.show();
                                break;
                            case R.id.menu_report:
                                mListenerNV = new OnListenerLine() {

                                    @Override
                                    public void onListenerCanceled() {
                                        mListenerNV = null;
                                        dlgGraphic.cancel();
                                    }
                                };
                                dlgGraphic = new FDialog_statistic(MapsActivity.this, mListenerNV, 2);
                                dlgGraphic.show();
                                break;
                            case R.id.menu_driving:
                                if (menuItem.isChecked()) {
                                    menuItem.setChecked(false);
                                } else {
                                    menuItem.setChecked(true);
                                }
                                drawMode = "driving";
                                swNavigate.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.driving, 0, 0, 0);
                                mMap.clear();
                                drawingRoutes(mLastLocation, posDestino);
                                break;
                            case R.id.menu_walking:
                                if (menuItem.isChecked()) {
                                    menuItem.setChecked(false);
                                } else {
                                    menuItem.setChecked(true);
                                }
                                drawMode = "walking";
                                swNavigate.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.walking, 0, 0, 0);
                                mMap.clear();
                                drawingRoutes(mLastLocation, posDestino);
                                break;
                            case R.id.menu_cycling:
                                if (menuItem.isChecked()) {
                                    menuItem.setChecked(false);
                                } else {
                                    menuItem.setChecked(true);
                                }
                                drawMode = "cycling";
                                swNavigate.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.bycicle, 0, 0, 0);
                                mMap.clear();
                                drawingRoutes(mLastLocation, posDestino);
                                break;
                            case R.id.menu_opcion_1:
                                if (menuItem.isChecked()) {
                                    menuItem.setChecked(false);
                                } else {
                                    menuItem.setChecked(true);
                                }
                                onNormalMap();
                                break;
                            case R.id.menu_opcion_2:
                                if (menuItem.isChecked()) {
                                    menuItem.setChecked(false);
                                } else {
                                    menuItem.setChecked(true);
                                }
                                onSatelliteMap();
                                break;
                            case R.id.menu_opcion_3:
                                if (menuItem.isChecked()) {
                                    menuItem.setChecked(false);
                                } else {
                                    menuItem.setChecked(true);
                                }
                                onTerrainMap();
                                break;
                            case R.id.menu_opcion_4:
                                if (menuItem.isChecked()) {
                                    menuItem.setChecked(false);
                                } else {
                                    menuItem.setChecked(true);
                                }
                                onHybridMap();
                                break;
                        }
                        drawerLayout.closeDrawers();
                        return true;
                    }
                });
        btnConfli.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activeConflict){
                    if (activeList){
                        lvConfli.setVisibility(View.VISIBLE);
                        Animation localAnimation = AnimationUtils.loadAnimation(MapsActivity.this, R.anim.slide_left_right);
                        localAnimation.reset();
                        lvConfli.clearAnimation();
                        lvConfli.startAnimation(localAnimation);
                        activeList = !activeList;
                    }else{
                        lvConfli.setVisibility(View.GONE);
                        activeList = !activeList;
                    }
                }else{
                    Toast.makeText(MapsActivity.this, getResources().getString(R.string.non_conflict), Toast.LENGTH_SHORT).show();
                }
            }
        });

        swNavigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (posDestino!=null){
                    if (drawMode.equals("driving")){
                        swNavigate.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.driving, 0, 0, 0);
                    }else{
                        if (drawMode.equals("walking")){
                            swNavigate.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.walking, 0, 0, 0);
                        }else{
                            if (drawMode.equals("cycling")){
                                swNavigate.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.bycicle, 0, 0, 0);
                            }
                        }
                    }
                    if (swNavigate.isChecked()){
                        drouteing = true;
                    } else{
                        drouteing = false;
                    }
                }else{
                    Toast.makeText(MapsActivity.this, getResources().getString(R.string.ldestiny), Toast.LENGTH_SHORT).show();
                    swNavigate.setChecked(false);
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
                mMap.setOnMyLocationChangeListener(this);
                UiSettings uiSettings = mMap.getUiSettings();
                uiSettings.setZoomControlsEnabled(true);
                uiSettings.setMapToolbarEnabled(true);
                //uiSettings.setMyLocationButtonEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationChangeListener(this);
            UiSettings uiSettings = mMap.getUiSettings();
            uiSettings.setZoomControlsEnabled(true);
            uiSettings.setMapToolbarEnabled(true);
            //uiSettings.setMyLocationButtonEnabled(true);
        }
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            if (mLastLocation!=null){
                LatLng posOrigin = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                CameraPosition camPos = new CameraPosition.Builder()
                        .target(new LatLng(posOrigin.latitude, posOrigin.longitude)).zoom(mapaZoom).bearing(inclinacionI).tilt(inclinacionF)
                        .build();
                mMap.addMarker(new MarkerOptions().position(posOrigin).title("Estoy aqui").snippet("locName").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
            }
        }

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {

            @Override
            public boolean onMyLocationButtonClick() {
                if (mLastLocation != null) {
                    zoomLocation(mLastLocation);
                    String locName = "Sin dirección";
                    Geocoder gcd = new Geocoder(MapsActivity.this, Locale.getDefault());
                    List<Address> addresses = null;
                    try {
                        addresses = gcd.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
                        if (addresses.size() > 0) {
                            locName = "Ciudad: " + addresses.get(0).getLocality() + "\nDirección: " + addresses.get(0).getFeatureName();
                            speakLocations(getResources().getString(R.string.txt_origin) + locName);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {
                        if (btnConfli.getVisibility()==View.VISIBLE){
                            btnConfli.setVisibility(View.GONE);
                        }
                        Context context = getApplicationContext();
                        LinearLayout info = new LinearLayout(context);
                        info.setOrientation(LinearLayout.VERTICAL);
                        TextView title = new TextView(context);
                        title.setTextColor(Color.BLACK);
                        title.setGravity(Gravity.CENTER);
                        title.setTypeface(null, Typeface.BOLD);
                        title.setText(marker.getTitle());
                        TextView snippet = new TextView(context);
                        snippet.setTextColor(Color.GRAY);
                        snippet.setText(marker.getSnippet());
                        info.addView(title);
                        info.addView(snippet);
                        return info;
                    }
                });
                return false;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (btnConfli.getVisibility()==View.GONE){
                    btnConfli.setVisibility(View.VISIBLE);
                }
            }
        });
        loadDataConflict.execute();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10 * 1000);
        //mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        zoomLocation(location);
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    public void zoomLocation(Location location){
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraPosition camPos = new CameraPosition.Builder()
                .target(new LatLng(latLng.latitude, latLng.longitude)).zoom(mapaZoom).bearing(inclinacionI).tilt(inclinacionF)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                        mMap.setOnMyLocationChangeListener(this);
                    }
                } else {
                    Toast.makeText(this, "Sin permisos", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    public void onActivityResult(int requestcode, int resultcode, Intent datos) {
        if (resultcode == Activity.RESULT_OK && requestcode==151 && datos != null) {
            ArrayList<String> text = datos.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (mLastLocation != null && text.size() > 0) {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocationName(text.get(0), 1);
                    if(addresses.size() > 0) {
                        Address address = addresses.get(0);
                        mMap.clear();
                        posDestino = new LatLng(address.getLatitude(), address.getLongitude());
                        CameraPosition camPos = new CameraPosition.Builder()
                                .target(new LatLng(posDestino.latitude, posDestino.longitude)).zoom(mapaZoom).bearing(inclinacionI).tilt(inclinacionF)
                                .build();
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
                        mMap.addMarker(new MarkerOptions().position(posDestino).title(text.get(0).toString()));
                        nameDestiny = text.get(0).toString();
                        drawingRoutes(mLastLocation, posDestino);
                        speakLocations(getResources().getString(R.string.txt_destiny) + text.get(0));
                        llNavigate.setVisibility(View.VISIBLE);
                    }else{
                        Toast.makeText(MapsActivity.this, "No se pudo localizar el destino", Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{

            }
        }
        if (requestcode == MY_DATA_CHECK_CODE) {
            if (resultcode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                tts = new TextToSpeech(this, this);
            } else {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }

    public void onMapSearch() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Diga la dirección...");
        try {
            startActivityForResult(intent, 151);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "Tu dispositivo no soporta el reconocimiento de voz", Toast.LENGTH_LONG).show();
        }
    }

    private String getUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String sensor = "sensor=false&mode=" + drawMode + "&alternatives=true";
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        return url;
    }

    @Override
    public void onMyLocationChange(Location location) {
        mLastLocation = location;
        if (drouteing) {
            /*if (mCurrLocationMarker != null) {
                mCurrLocationMarker.remove();
            }*/
            mMap.clear();
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Posición actual");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            mCurrLocationMarker = mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(mapaZoom));
            drawingRoutes(mLastLocation, posDestino);
        }
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                data = ServiceUtil.downloadUrl(url[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                DataParser parser = new DataParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = result.get(i);
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.BLUE);
            }
            if (lineOptions != null) {
                mMap.addPolyline(lineOptions);
            } else {
                Toast.makeText(MapsActivity.this, "No se encontro la ruta, intente de nuevo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (swNavigate.isChecked()){
            swNavigate.setChecked(false);
            drouteing = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (swNavigate.isChecked()){
            swNavigate.setChecked(false);
            drouteing = false;
        }
    }

    @Override
    public void onInit(int status) {// Inicia TTS
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                AlertDialog.Builder dialogL = new AlertDialog.Builder(this);
                dialogL.setTitle(getResources().getString(R.string.dlg_title));
                dialogL.setMessage(getResources().getString(R.string.dlg_language));
                dialogL.setCancelable(false);
                dialogL.setPositiveButton("Instalar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogo1, int id) {
                        Intent installLanguage = new Intent();
                        installLanguage.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                        startActivity(installLanguage);
                        dialogo1.dismiss();
                    }
                });
                dialogL.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogo1, int id) {
                        dialogo1.cancel();
                    }
                });
                dialogL.show();
            }
        } else if (status == TextToSpeech.ERROR) {
            Toast.makeText(this, getResources().getString(R.string.txt_interprete), Toast.LENGTH_LONG).show();
        }
    }

    private void speakLocations(String speech) {
        if (tts!=null){
            tts.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
        } else{
            Toast.makeText(this, getResources().getString(R.string.voice_prepare), Toast.LENGTH_SHORT).show();
        }
    }

    public void onNormalMap() {
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    public void onSatelliteMap() {
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
    }

    public void onTerrainMap() {
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
    }

    public void onHybridMap() {
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }

    @Override
    public void onPlaceSelected(Place place) {
        mMap.clear();
        mPlace = place;
        if (mPlace!=null){
            posDestino = place.getLatLng();
            CameraPosition camPos = new CameraPosition.Builder()
                    .target(new LatLng(posDestino.latitude, posDestino.longitude)).zoom(mapaZoom).bearing(inclinacionI).tilt(inclinacionF)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
            mMap.addMarker(new MarkerOptions().position(posDestino).title(mPlace.getName().toString()));
            nameDestiny = mPlace.getName().toString();
            drawingRoutes(mLastLocation, posDestino);
            speakLocations(getResources().getString(R.string.txt_destiny) + place.getName());
            llNavigate.setVisibility(View.VISIBLE);
        }
    }

    public void drawingRoutes(Location porigen, LatLng pdestino) {
        if (porigen!=null && pdestino!=null){
            String url = getUrl(new LatLng(porigen.getLatitude(), porigen.getLongitude()), pdestino);
            FetchUrl fetchUrl = new FetchUrl();
            fetchUrl.execute(url);
            drawingMarkes();
        }else{
            Toast.makeText(this, "Posiciones no fijadas", Toast.LENGTH_SHORT).show();
        }
    }

    public void drawingMarkes() {
        mMap.addMarker(new MarkerOptions().position(posDestino).title(nameDestiny));
    }

    @Override
    public void onError(Status status) {
        Toast.makeText(this, "Place selection failed: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_search:
                if (activeSearch){
                    llSearch.setVisibility(View.VISIBLE);
                    Animation localAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_left_right);
                    localAnimation.reset();
                    llSearch.clearAnimation();
                    llSearch.startAnimation(localAnimation);
                    activeSearch = !activeSearch;
                }else{
                    llSearch.setVisibility(View.GONE);
                    activeSearch = !activeSearch;
                }
                return true;
            case R.id.action_micro:
                onMapSearch();
                return true;
            case R.id.action_clear:
                llNavigate.setVisibility(View.GONE);
                if (swNavigate.isChecked()){
                    swNavigate.setChecked(false);
                    drouteing = false;
                }
                mMap.clear();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    AsyncTask<Void, Void, Boolean> loadDataConflict = new AsyncTask<Void, Void, Boolean>() {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            btnConfli.setText(getResources().getString(R.string.search_conflict));
        }

        @Override
        protected Boolean doInBackground(Void... param) {
            Boolean conexion = false;
            if (StateEthernet.verificaConexion(MapsActivity.this)) {
                try {
                    DBAdapter dbrad = new DBAdapter(MapsActivity.this);
                    dbrad.open();
                    dbrad.deleteFigures();
                    conflictos = new ArrayList<Conflicts>();
                    String timeline = ServiceUtil.getDatajsonc();
                    JSONArray jsonArray = new JSONArray(timeline);
                    JSONObject jsonObject;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = (JSONObject) jsonArray.get(i);
                        Conflicts conf = new Conflicts();
                        long iconflict = jsonObject.getLong(ConstantsUtil.TAG_CONF);
                        conf.setIdc(iconflict);
                        conf.setSector(jsonObject.getString(ConstantsUtil.TAG_SECTOR));
                        conf.setCause(jsonObject.getString(ConstantsUtil.TAG_CAUSE));
                        JSONArray jsonArrayF = jsonObject.getJSONArray(ConstantsUtil.TAG_FIGURE);
                        JSONObject jsonObjectF;
                        for (int j = 0; j < jsonArrayF.length(); j++) {
                            jsonObjectF = (JSONObject) jsonArrayF.get(j);
                            dbrad.addFigures(iconflict,
                                    jsonObjectF.getString(ConstantsUtil.TAG_LATITUDC),
                                    jsonObjectF.getString(ConstantsUtil.TAG_LONGETUDC),
                                    jsonObjectF.getLong(ConstantsUtil.TAG_RADIOC),
                                    jsonObjectF.getString(ConstantsUtil.TAG_FILL),
                                    jsonObjectF.getString(ConstantsUtil.TAG_STROKE),
                                    jsonObjectF.getString(ConstantsUtil.TAG_TYPE));
                        }
                        conflictos.add(conf);
                    }
                    dbrad.close();
                    conexion = true;
                } catch (Exception e) {
                    Log.e("Error: ", Log.getStackTraceString(e));
                    conexion = false;
                }
            } else{
                conexion = false;
            }
            return conexion;
        }

        @Override
        protected void onPostExecute(Boolean results) {
            if (results){
                btnConfli.setText(getResources().getString(R.string.list_conflict));
                if (conflictos.size()>0){
                    activeConflict = true;
                    mAdapter = new ConflictAdapter(MapsActivity.this, conflictos);
                    lvConfli.setAdapter(mAdapter);
                    lvConfli.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                            final long iconf = parent.getItemIdAtPosition(position);
                            qab = new QuickActionBar(v);
                            final QuickActionIcons opcionC1 = new QuickActionIcons();
                            opcionC1.setTitle("Localización");
                            opcionC1.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    mMap.clear();
                                    DBAdapter queryFigVert = new DBAdapter(MapsActivity.this);
                                    queryFigVert.open();
                                    ArrayList<Figures> figures = new ArrayList<Figures>();
                                    figures = queryFigVert.getFigures(iconf);
                                    if (figures.size() > 0) {
                                        lvConfli.setVisibility(View.GONE);
                                        activeList = !activeList;
                                        CircleOptions circleOptions = new CircleOptions();
                                        double lat = 0, lng = 0;
                                        for (int i = 0; i < figures.size(); i++) {
                                            Figures dfig = figures.get(i);
                                            lat = Double.valueOf(dfig.getLatp());
                                            lng = Double.valueOf(dfig.getLngp());
                                            circleOptions.center(new LatLng(lat, lng));
                                            circleOptions.radius(dfig.getRadc());
                                            circleOptions.strokeColor(Color.parseColor("#" + dfig.getStroke()));
                                            circleOptions.fillColor(Integer.parseInt(0x30+dfig.getFillc(), 16));
                                            circleOptions.strokeWidth(7);
                                            circleOptions.describeContents();
                                            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title("Lugar de conflicto").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                                            mMap.addCircle(circleOptions);
                                            CameraPosition camPos = new CameraPosition.Builder()
                                                    .target(new LatLng(lat, lng)).zoom(mapaZoom).bearing(inclinacionI).tilt(inclinacionF)
                                                    .build();
                                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
                                        }
                                    } else {
                                        Toast.makeText(MapsActivity.this, getResources().getString(R.string.non_figure), Toast.LENGTH_SHORT).show();
                                    }
                                    queryFigVert.close();
                                    qab.dismiss();
                                }
                            });
                            qab.addItem(opcionC1);
                            final QuickActionIcons opcionC2 = new QuickActionIcons();
                            opcionC2.setTitle(" Detalles ");
                            opcionC2.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    mListenerNV = new OnListenerLine() {

                                        @Override
                                        public void onListenerCanceled() {
                                            mListenerNV = null;
                                            dlgNuevaVisita.cancel();
                                        }
                                    };
                                    dlgNuevaVisita = new FDialog_conflict(MapsActivity.this, mListenerNV, iconf);
                                    dlgNuevaVisita.show();
                                    qab.dismiss();
                                }
                            });
                            qab.addItem(opcionC2);
                            qab.setAnimationStyle(QuickActionBar.GROW_FROM_LEFT);
                            qab.show();
                        }
                    });
                }else{
                    btnConfli.setText(getResources().getString(R.string.non_conflict));
                    activeConflict = false;
                }
            }else{
                btnConfli.setText(getResources().getString(R.string.non_conflict));
                Toast.makeText(MapsActivity.this, getResources().getString(R.string.txt_error), Toast.LENGTH_LONG).show();
            }
        }
    };
}
//KmlLayer layer = new KmlLayer(getMap(), R.raw.kmlFile, getApplicationContext());
//KmlLayer layer = new KmlLayer(getMap(), kmlInputStream, getApplicationContext());
//layer.addLayerToMap();
