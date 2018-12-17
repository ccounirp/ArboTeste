package com.maps.gi.arboteste;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.sql.Connection;
import java.sql.DriverManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        requestPermissions();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {

                    LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    //mMap.addMarker(new MarkerOptions().position(myLocation).title("Nome Popular")
                    // .snippet("Nome Científico").icon(BitmapDescriptorFactory.fromResource(R.mipmap.icone_marcador)));
                    ListarArvores(mMap);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                }
            }
        });

    }

    public void requestPermissions(){
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1
        );
    }

    public Connection conexionCB(){
        Connection conexion=null;
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();

            conexion = DriverManager.getConnection("jdbc:jtds:sqlserver://177.21.62.255;databaseName=Arbo;user=sa;password=epilef;");

            //conexion = DriverManager.getConnection("jdbc:jtds:sqlserver://199.192.207.158;databaseName=Arbo;user=sa;password=@900Doletas;");

        }catch (Exception e){
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        }
        return conexion;
    }

    public void ListarArvores(GoogleMap map){

        try {

            PreparedStatement pstQuery = conexionCB().prepareStatement("SELECT ARVORE,LATITUDE,LONGITUDE FROM ARVORE_COORDENADA");
            ResultSet rs;
            rs = pstQuery.executeQuery();
            LatLng position;
            MarkerOptions marker;

            while(rs.next()){

                 position =  new LatLng(Double.parseDouble(rs.getString("LATITUDE")),
                         Double.parseDouble(rs.getString("LONGITUDE")));
                 marker = new MarkerOptions();
                 marker.position(position);
                 marker.title(rs.getString("ARVORE"));

                 marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.icone_marcador));
                 map.addMarker(marker);
                //mMap.addMarker(new MarkerOptions().position(myLocation).title("Nome Popular")
                // .snippet("Nome Científico").icon(BitmapDescriptorFactory.fromResource(R.mipmap.icone_marcador)));
            }

        }catch (Exception e){

        }
    }
}
