package com.maps.gi.arboteste;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ImageButton btnCadastro = findViewById(R.id.btnCadastroArvores);
        ImageButton btnMapa = findViewById(R.id.btnAbrirMapaMenu);

        btnCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirCadastro();
            }
        });

        btnMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirMapa();
                //Marcar todos os pontos no mapa, de acordo com o banco de dados
            }
        });

    }

    public void abrirCadastro(){
        Intent cadastro = new Intent(this, MainActivity.class);
        startActivity(cadastro);
    }

    public final void abrirMapa(){
        Intent mapa = new Intent(this, MapsActivity.class);
        startActivity(mapa);
    }
}
