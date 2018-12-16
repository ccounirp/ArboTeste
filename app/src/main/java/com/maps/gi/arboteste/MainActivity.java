package com.maps.gi.arboteste;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.os.Debug;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

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
    private CheckBox cbDanoCalcada;
    private CheckBox cbConflitoRede;
    private TextView txtDiametro;

    private Location location;
    private LocationManager locationManager;

    private double longitude;
    private double lat;

    private boolean loc = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
          //      WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ImageButton bCamera = findViewById(R.id.btnCamera);
        ImageButton bMapa = findViewById(R.id.btnMapa);
        ImageButton bCriar = findViewById(R.id.btnCriar);
        foto = findViewById(R.id.imgArvore);
        final EditText latitude = findViewById(R.id.idNomePopular);


        longitude = 0.0;
        lat = 0.0;

        associaComponentes();

        requestPermissions();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        bCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirCamera();
            }
        });

        bMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Ao clicar neste botão, vai obter a coordenada atual
                obterLocalizacao();
            }
        });

        bCriar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nomePopular = txtNomePopular.getText().toString();
                String nomeCientifico = txtNomeCientifico.getText().toString();

                if (nomePopular.equals("") || nomePopular == null || nomeCientifico.equals("") || nomeCientifico == null) {
                    validationCamp();
                } else {

                    if (loc) {
                        try {
                            gravarRegistro();
                            gravarCoordenada();
                            gravarImagem();
                            Toast.makeText(getApplicationContext(), "Registro gravado com sucesso!", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {

                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Clique em obter localização e tente gravar novamente", Toast.LENGTH_LONG).show();
                    }

                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                //latitude.setText(location.toString());
                                //gravarCoordenada(location);
                                Log.d("LOCATION", location.toString());
                            }

                        }
                    });
                }
            }

        });


        txtDiametro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtDiametro.setError("Toque duplo para atualizar o Diametro. Caso o CAP for mudado, toque duplo para atualizar o Diametro.");
                if(txtCpa.equals("") || txtCpa.equals(null)){
                    txtCpa.setError("Para estabelecer o Diametro automatico, deve-se informar um valor para o CAP.");
                }
                else {
                    try{
                        String capString = txtCpa.getText().toString().replace(",",".");
                        float cap = Float.parseFloat(capString);
                        float diametro = (float) (cap / 3.14);
                        String diametroString = String.valueOf(diametro);
                        txtDiametro.setText(diametroString);
                    } catch (Exception ex){
                        txtCpa.setError("Para estabelecer o Diametro automatico, deve-se informar um valor para o CAP.");
                    }

                }
            }
        });

    }

    public void validationCamp(){
        String nomePopular = txtNomePopular.getText().toString();
        String nomeCientifico = txtNomeCientifico.getText().toString();

        if(nomePopular.equals("") || nomePopular == null)
        {
            txtNomePopular.setError("O nome popular é necessário");
        }

        if(nomeCientifico.equals("") || nomeCientifico == null){
            txtNomeCientifico.setError("O nome cientifico é necessário");
        }
    }

    public Connection conexionCB(){
        Connection conexion=null;
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
            conexion = DriverManager.getConnection("jdbc:jtds:sqlserver://199.192.207.158;databaseName=Arbo;user=sa;password=@900Doletas;");
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

                pst.setString(1, txtNomePopular.getText().toString());
                pst.setString(2, txtNomeCientifico.getText().toString());
                pst.setString(3, txtCpa.getText().toString().replace(",", "."));
                pst.setString(4, txtAlturaTronco.getText().toString().replace(",", "."));
                pst.setString(5, txtAlturaCopa.getText().toString().replace(",", "."));
                pst.setString(6, txtNotaRaiz.getText().toString());
                pst.setString(7, txtNotaCaule.getText().toString());
                pst.setString(8, txtVigorCopa.getText().toString());
                pst.setString(9, txtNotaVitArv.getText().toString());
                pst.setString(10, txtDoencasPragPara.getText().toString());
                pst.setString(11, txtObservacoes.getText().toString());
                //pst.setString( 12, cbDanoCalcada
                //pst.setString( 13, cbConflitoRede
                //pst.setString( 14, txtDiametro

                pst.executeUpdate();

                pst = conexionCB().prepareStatement("SELECT MAX(CODIGO) AS CODIGO FROM ARVORE_REGISTRO");
                ResultSet rs;
                rs = pst.executeQuery();
                rs.next();
                indice = Integer.parseInt(rs.getString("CODIGO"));
                Toast.makeText(getApplicationContext(), "Codigo Arvore: " + Integer.toString(indice), Toast.LENGTH_LONG).show();

                Toast.makeText(getApplicationContext(), "Registro gravado com sucesso!", Toast.LENGTH_LONG).show();

            } catch (SQLException e) {
                Toast.makeText(getApplicationContext(), "Ocorreu um erro ao gravar o registro: " + e.toString(), Toast.LENGTH_LONG).show();
            }

    }

    public void gravarImagem(){

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
        byte imagemBytes[] = stream.toByteArray();

        try {

            PreparedStatement ps = conexionCB().prepareStatement("INSERT INTO ARVORE_FOTO VALUES (?,?)");
            ps.setInt(1, indice);
            ps.setBytes(2,imagemBytes);
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

            //Toast.makeText(getApplicationContext(),"Coordenadas gravadas com sucesso!",Toast.LENGTH_LONG).show();

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
        cbDanoCalcada= (CheckBox) findViewById(R.id.idDanoCalcada);
        cbConflitoRede= (CheckBox) findViewById(R.id.idConflitoRede);
        txtDiametro= (TextView) findViewById(R.id.idDiametro);

        txtNotaRaiz.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "5")});
        txtNotaCaule.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "5")});
        txtVigorCopa.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "5")});
        txtNotaVitArv.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "5")});
        txtDoencasPragPara.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "3")});
    }

    public void obterLocalizacao() {
        //Aplicativo checa se tem a permissão
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
        } else {
            locationManager = (LocationManager)
                    getSystemService(Context.LOCATION_SERVICE);
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if(location == null){
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        }

        if (location != null) {
            longitude = location.getLongitude();
            lat = location.getLatitude();
            Toast.makeText(getApplicationContext(), "Longitude = " + String.valueOf(longitude) +
                    " / Latitude = " + String.valueOf(lat), Toast.LENGTH_LONG).show();
        }
        loc = true;
    }
}
