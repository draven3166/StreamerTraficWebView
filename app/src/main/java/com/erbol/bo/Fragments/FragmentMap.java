package com.erbol.bo.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.erbol.bo.R;
import com.erbol.bo.Utils.ConstantsUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

//import com.erbol.bo.android.common.logger.Log;

import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")//Fragment
public class FragmentMap extends Fragment implements
        LocationListener, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMarkerDragListener,GoogleMap.OnMyLocationChangeListener {
    private MapView mMapView;
    private GoogleMap mMap;
    private View mView;
    private boolean actualizar=true;
    private Button btnAceptar;
    private String provider;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    // public static final String TAG = Activity_Map.class.getSimpleName();

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private TextView txtLoc;
    private static Location mLocation;
    private LatLng posDestino;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private PolygonOptions areaPermitida;
    private final int inclinacionI = 0;
    private final int inclinacionF = 35;
    private final int mapaZomm = 17;
    //private LatLng posicionFija;
    private Location mLocAnterior;
    private double latitudeTienda;
    private double longetudeTienda;


    public static FragmentMap newInstance() {
        FragmentMap fm = new FragmentMap();
        Bundle b = new Bundle();
        fm.setArguments(b);
        return fm;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mView = inflater.inflate(R.layout.fragment_map, container, false);
        mMapView = (MapView) mView.findViewById(R.id.map_totalFMT);
        mMapView.onCreate(savedInstanceState);

        provider = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if(!provider.contains("gps")){ //if gps is disabled
            AlertDialog.Builder dialogGPS=new AlertDialog.Builder(getActivity());
            dialogGPS.setTitle("GPS Desactivado");
            dialogGPS.setMessage("¿Desea habilitar el GPS?");
            dialogGPS.setCancelable(false);
            dialogGPS.setPositiveButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog2, int id) {
                    dialog2.cancel();
                }
            });
            dialogGPS.setNegativeButton("Configurar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog2, int id) {
                    Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    getActivity().startActivity(callGPSSettingIntent);
                    dialog2.cancel();
                }
            });
            dialogGPS.show();
        }//-16.501795, -68.132617
        latitudeTienda = -16.501795;
        longetudeTienda = -68.132617;
        posDestino = new LatLng(latitudeTienda, longetudeTienda);
        txtLoc=(TextView)mView.findViewById(R.id.tvNotification);
        btnAceptar=(Button)mView.findViewById(R.id.btnGaleria);
        btnAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLocation != null) {

                }/* else {
                    Toast.makeText(getActivity(), "No se encuentra su ubicación o esta lejos de la tienda", Toast.LENGTH_LONG).show();
                }*/
            }
        });
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000);// 1 second, in milliseconds
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MapsInitializer.initialize(getActivity());
        setUpMapIfNeeded(mView);
        loadMarkerListener();
        //ubicacionTienda();
    }

    private void setUpMapIfNeeded(View inflatedView) {
        if (mMap == null) {
            mMap = ((MapView) inflatedView.findViewById(R.id.map_totalFMT)).getMap();
            if (mMap != null) {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mMap.setMyLocationEnabled(true);// boton de localizacion
                // habilitado
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                mMap.setOnMapClickListener(this);
                mMap.setOnMapLongClickListener(this);
                mMap.setOnMarkerDragListener(this);
                mMap.setOnMyLocationChangeListener(this);
                //mMap.seto(this);
                // mapa de tipo hibrido
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                    @Override
                    public boolean onMyLocationButtonClick() {
                        mLocation = mLocAnterior;
                        mMap.clear();
                        centerMap();
                        actualizar = true;
                        mos_actual(mLocation);
                        return true;
                    }
                });
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    public void loadMarkerListener() {
        if (mMap != null) {
            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                }
            });
        } else
            Toast.makeText(getActivity(), "Instale o actualice Google Maps", Toast.LENGTH_SHORT).show();
    }

    public void centerMap() {
        if (mLocation != null) {
            //ubicaDestino();
            CameraPosition camPos = new CameraPosition.Builder()
                    .target(new LatLng(mLocation.getLatitude(), mLocation.getLongitude())).zoom(mapaZomm).bearing(inclinacionI).tilt(inclinacionF)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
            LatLng myPosition = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_user));
            markerOptions.title("Ubicación Actual");
            // markerOptions.snippet(txtMiUbicacion.getText().toString());
            markerOptions.position(myPosition);
            markerOptions.draggable(true);
            mMap.addMarker(markerOptions);
        }
    }

    private void ubicaDestino() {
        CameraPosition camPos = new CameraPosition.Builder()
                .target(posDestino).zoom(mapaZomm).bearing(inclinacionI).tilt(inclinacionF).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_ubica));
        markerOptions.title("Localización del destino");
        markerOptions.position(posDestino);
        markerOptions.draggable(true);
        mMap.addMarker(markerOptions);

        areaPermitida = new PolygonOptions();
        areaPermitida.strokeColor(Color.GRAY);
        areaPermitida.fillColor(0x55CDDC2B);
        areaPermitida.strokeWidth(2);
        mMap.addPolygon(areaPermitida);
    }

    public boolean isGoogleMapsInstalled() {
        try {
            ApplicationInfo info = getActivity().getPackageManager()
                    .getApplicationInfo("com.google.android.apps.maps", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onMyLocationChange(Location arg0) {
        // TODO Auto-generated method stub
        mLocAnterior=arg0;
        if(actualizar){
            mMap.clear();
            mLocation = arg0;
            mos_actual(mLocation);
            centerMap();
        }
    }

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();
    }

    @Override
    public void onMarkerDrag(Marker arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        // TODO Auto-generated method stub
        actualizar=false;
        mLocation.setLatitude(marker.getPosition().latitude);
        mLocation.setLongitude(marker.getPosition().longitude);
        mMap.clear();
        mos_actual(mLocation);
        centerMap();
    }

    @Override
    public void onMarkerDragStart(Marker arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onMapLongClick(LatLng point) {
        // TODO Auto-generateed method stub
        actualizar=false;
        if (mLocation == null)
            mLocation = new Location("");
        mLocation.setLatitude(point.latitude);
        mLocation.setLongitude(point.longitude);
        mMap.clear();
        mos_actual(mLocation);
        centerMap();
    }

    @Override
    public void onMapClick(LatLng arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Log.i("TAG", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);
        }
        else {
            handleNewLocation(location);
        }
    }

    private void handleNewLocation(Location location) {
        Log.d("TAG", location.toString());
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        //mMap.addMarker(new MarkerOptions().position(new LatLng(currentLatitude, currentLongitude)).title("Current Location"));
        MarkerOptions options = new MarkerOptions().position(latLng).title("Estoy aquí !!!");
        mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public void mos_actual(Location loc){
        if(loc==null)
            txtLoc.setText("Aun no se encuentra su ubicación");
    }
}