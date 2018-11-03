package com.maps.gi.arboteste;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.os.Debug;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MainActivity extends AppCompatActivity {

    ImageView foto;
    Bitmap bitmap;

    int indice;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private TextView txtNomePopular;
    private TextView txtNomeCientifico;
    private TextView txtCpa;
    private TextView txtAlturaTronco;
    private TextView txtAlturaCopa;
    private TextView txtNotaRaiz;
    private TextView txtNotaCaule;
    private TextView txtVigorCopa;
    private TextView txtNotaVitArv;
    private TextView txtDoencasPragPara;
    private TextView txtObservacoes;

    private Location location;
    private LocationManager locationManager;

    private double longitude;
    private double lat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton bCamera = findViewById(R.id.btnCamera);
        ImageButton bMapa = findViewById(R.id.btnMapa);
        ImageButton bCriar = findViewById(R.id.btnCriar);
        ImageButton bListaArvores = findViewById(R.id.btnListaArvores);
        foto = findViewById(R.id.imgArvore);
        final EditText latitude = findViewById(R.id.idNomePopular);

        longitude = 0.0;
        lat = 0.0;

        associaComponentes();

        requestPermissions();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        bListaArvores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               abrirMenu();
            }
        });

        bCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirCamera();
            }
        });
        bMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirMapa();
            }
        });

        bCriar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                obterLocalizacao();
                gravarRegistro();
                gravarCoordenada();
                //gravarImagem();

                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if(location != null){
                            //latitude.setText(location.toString());
                            //gravarCoordenada(location);
                            Log.d("LOCATION", location.toString());
                        }

                    }
                });
            }
        });


    }

    public Connection conexionCB(){
        Connection conexion=null;
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
            conexion = DriverManager.getConnection("jdbc:jtds:sqlserver://177.21.62.255;databaseName=Arbo;user=sa;password=epilef;");
        }catch (Exception e){
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        }
        return conexion;
    }

    public void gravarRegistro(){
        indice = 0;
        try {
            PreparedStatement pst = conexionCB().prepareStatement("INSERT INTO ARVORE_REGISTRO" +
                    "(NOME_POPULAR,NOME_CIENTIFICO,CPA,ALTURA_TRONCO,ALTURA_COPA,NOTA_RAIZ,NOTA_CAULE," +
                    "VIGOR_COPA,NOTA_VITALIDADE,NOTA_DOENCAS,OBS) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?)");

            pst.setString(1,txtNomePopular.getText().toString());
            pst.setString(2,txtNomeCientifico.getText().toString());
            pst.setString(3,txtCpa.getText().toString());
            pst.setString(4,txtAlturaTronco.getText().toString());
            pst.setString(5,txtAlturaCopa.getText().toString());
            pst.setString(6,txtNotaRaiz.getText().toString());
            pst.setString(7,txtNotaCaule.getText().toString());
            pst.setString(8,txtVigorCopa.getText().toString());
            pst.setString(9,txtNotaVitArv.getText().toString());
            pst.setString(10,txtDoencasPragPara.getText().toString());
            pst.setString(11,txtObservacoes.getText().toString());

            pst.executeUpdate();

            pst = conexionCB().prepareStatement("SELECT MAX(CODIGO) AS CODIGO FROM ARVORE_REGISTRO");
            ResultSet rs;
            rs = pst.executeQuery();
            rs.next();
            indice = Integer.parseInt(rs.getString("CODIGO"));
            Toast.makeText(getApplicationContext(),"Codigo Arvore: " + Integer.toString(indice),Toast.LENGTH_LONG).show();

            Toast.makeText(getApplicationContext(),"Registro gravado com sucesso!",Toast.LENGTH_LONG).show();

        }catch (SQLException e){
            Toast.makeText(getApplicationContext(),"Ocorreu um erro ao gravar o registro: " + e.toString(),Toast.LENGTH_LONG).show();
        }
    }

    public void gravarImagem(){

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
        byte imagemBytes[] = stream.toByteArray();
        try {

            PreparedStatement ps = conexionCB().prepareStatement("INSERT INTO ARVORE_FOTO VALUES (?,"+imagemBytes.toString()+")");
            ps.setInt(1, indice);
            ps.executeUpdate();

        }catch(Exception e){
            Toast.makeText(getApplicationContext(),"Erro: " + e.toString(),Toast.LENGTH_LONG).show();
        }
    }

    public void gravarCoordenada(){
        try {
            PreparedStatement pst = conexionCB().prepareStatement("INSERT INTO ARVORE_COORDENADA " +
                    "(ARVORE,LONGITUDE,LATITUDE) VALUES (?,?,?)");
            pst.setInt(1,indice);
            pst.setString(2,String.valueOf(longitude));
            pst.setString(3,String.valueOf(lat));
            pst.executeUpdate();

            Toast.makeText(getApplicationContext(),"Coordenadas gravadas com sucesso!",Toast.LENGTH_LONG).show();

        }catch (Exception e){
            Toast.makeText(getApplicationContext(),"Ocorreu um erro ao gravar as coordenadas: " + e.toString(),Toast.LENGTH_LONG).show();
        }
    }

    public final void abrirCamera(){

        Intent camera = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivityForResult(camera, 0);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        bitmap = (Bitmap)data.getExtras().get("data");
        foto.setImageBitmap(bitmap);


    }//onActivityResult

    public final void abrirMapa(){
        Intent mapa = new Intent(this, MapsActivity.class);
        startActivity(mapa);
    }

    public final void abrirMenu(){
        Intent menu = new Intent(this, MenuActivity.class);
        startActivity(menu);
    }

    public void requestPermissions(){
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1
        );
    }

    public void associaComponentes(){

        txtNomePopular=(TextView) findViewById(R.id.idNomePopular);
        txtNomeCientifico=(TextView) findViewById(R.id.idNomeCientifico);
        txtCpa=(TextView) findViewById(R.id.idCpa);
        txtAlturaTronco=(TextView) findViewById(R.id.idAlturaTronco);
        txtAlturaCopa=(TextView) findViewById(R.id.idAlturaCopa);
        txtNotaRaiz=(TextView) findViewById(R.id.idNotaRaiz);
        txtNotaCaule=(TextView) findViewById(R.id.idNotaCaule);
        txtVigorCopa=(TextView) findViewById(R.id.idVigorCopa);
        txtNotaVitArv=(TextView) findViewById(R.id.idNotaVitArv);
        txtDoencasPragPara=(TextView) findViewById(R.id.idDoencasPragPara);
        txtObservacoes=(TextView) findViewById(R.id.idObservacoes);
    }

    public void obterLocalizacao() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
        } else {
            locationManager = (LocationManager)
                    getSystemService(Context.LOCATION_SERVICE);
            location =
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        if (location != null) {
            longitude = location.getLongitude();
            lat = location.getLatitude();
            Toast.makeText(getApplicationContext(), "Longitude = " + String.valueOf(longitude) +
                    " / Latitude = " + String.valueOf(lat), Toast.LENGTH_LONG).show();
        }
    }
}
