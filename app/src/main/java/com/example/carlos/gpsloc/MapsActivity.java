package com.example.carlos.gpsloc;

import android.*;
import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Location location;
    LocationManager locationManager;
    LocationListener locationListener;
    AlertDialog alert = null;
    Marker marcador,mrkDestino;
    Button btnLocalizacion;
    boolean flag = false;
    //bandera PARA LOCALIZAR

    //BANDERA PARA

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        obtenerUbicacionAcutual();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        iniciarBotones();




    }

    private void iniciarBotones(){
        btnLocalizacion = (Button)findViewById(R.id.btnLoc);
        btnLocalizacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



            }
        });

    }

    private void obtenerUbicacionAcutual() {

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            AlertNoGps();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            } else {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        } else {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                LatLng ubb = new LatLng(location.getLatitude(), location.getLongitude());

                    try {
                        if(flag == false){
                            marcador = mMap.addMarker(new MarkerOptions()
                                    .position(ubb).title("")
                                    .draggable(true)
                            );
                            flag = true;
                        }else {
                            marcador.setPosition(ubb);
                        }
                        verificarLLegada();
                       }catch (NullPointerException e) {
                          System.out.print(e.getMessage());
                    }


                try {

                }catch(NullPointerException e){

                }

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
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
    }

    public void verificarLLegada(){

        LatLng destino = new LatLng(mrkDestino.getPosition().latitude,mrkDestino.getPosition().longitude);
        LatLng inicio=   new LatLng(marcador.getPosition().latitude,marcador.getPosition().longitude);
        double res = calculaDistancia(inicio, destino);
        res = res*1000;
        String r = Double.toString(res);
        if(res <10) {
            Toast.makeText(getApplicationContext(), r + " " + "metros yego a su destino", Toast.LENGTH_LONG).show();
        }

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        LatLng ub;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            } else {
                mMap.setMyLocationEnabled(true);
            }
        } else {
               mMap.setMyLocationEnabled(true);
        }

        if(location != null) {
             ub = new LatLng(location.getLatitude(), location.getLongitude());
        }else{
            ub = new LatLng(25,-107);
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ub, 19));

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override

            public void onMapLongClick(LatLng latLng) {

                Toast.makeText(getApplicationContext(),"latitud -->>"+latLng.latitude+" "+" longitud -->"+latLng.longitude, Toast.LENGTH_LONG).show();

                try {
                    mrkDestino.remove();
                }catch (NullPointerException e){

                }

                mrkDestino= mMap.addMarker(new MarkerOptions()

                        .draggable(true)
                        .position(latLng));

            }

        });

    }

    private void AlertNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("El sistema GPS esta desactivado, ¿Desea activarlo?")
                .setCancelable(false)
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        alert = builder.create();
        alert.show();
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(alert != null)
        {
            alert.dismiss ();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            } else {
                locationManager.removeUpdates(locationListener);
            }
        } else {
            locationManager.removeUpdates(locationListener);
        }


    }
    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }



    }

    private double calculaDistancia(LatLng desde, LatLng hasta){

        // transforma grados en radianes
        double lat1 = graRad(desde.latitude);
        double long1 = graRad(desde.longitude);

        double lat2 = graRad(hasta.latitude);
        double long2 = graRad(hasta.longitude);
        // calcula la distancia


        double res= Math.acos(Math.sin(lat1)*Math.sin(lat2) +
                Math.cos(lat1)*Math.cos(lat2) *
                        Math.cos(long2-long1) )* 6371;

        return res;
    }

    private double graRad(double longitude) {

        double radianes = (longitude * Math.PI)/180;
        return radianes;

    }


}
