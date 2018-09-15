package com.maps.gi.arboteste;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button bCamera = findViewById(R.id.btnCamera);
        int gilmarViado = 24;
        String Jessica = "minha";

        bCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirCamera();
            }
        });
    }

    public final void abrirCamera(){

        Intent camera = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivity(camera);

    }

}
