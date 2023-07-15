package com.example.ejercicio_2_1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.ejercicio_2_1.Procesos.SQLiteConexion;
import com.example.ejercicio_2_1.Procesos.Transacciones;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    VideoView videoview;
    Button btngrabar, btnguardar;
    String curpath;
    byte[] Arrayvideo;
    static final int REQUEST_VIDEO_CAPTURE = 104;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoview = (VideoView) findViewById(R.id.videoview);
        btngrabar = (Button) findViewById(R.id.btngrabar);
        btnguardar = (Button) findViewById(R.id.btnguardar);

        btnguardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Arrayvideo != null){
                    guardarvideo();
                }else{
                    if(Arrayvideo == null) {
                        Toast.makeText(getApplicationContext(),"Video Guardado!", Toast.LENGTH_LONG).show();
                        videoview.requestFocus();
                    }
                }
            }
        });

        btngrabar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                otorgarpermisos();
            }
        });

    }

    private void otorgarpermisos() {
        if(ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA)!=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},REQUEST_VIDEO_CAPTURE);
        }
        else{
            VideoIntent();
        }
    }


    private File createVideo() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String videoFileName = "MP4_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        File vid = File.createTempFile(
                videoFileName,
                ".mp4",
                storageDir
        );

        curpath = vid.getAbsolutePath();
        return vid;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_VIDEO_CAPTURE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                grabarvideo();
            }
        }
        else{
            Toast.makeText(getApplicationContext(),"Otorgar permisos de camara",Toast.LENGTH_LONG).show();
        }
    }

    private void grabarvideo() {
        Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (videoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(videoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    private void guardarvideo() {
        SQLiteConexion conexion = new SQLiteConexion(this, Transacciones.NameDataBase,null,1);
        SQLiteDatabase db = conexion.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put(Transacciones.video, Arrayvideo);
        String sql = "INSERT INTO grabarvideo(id,video) VALUES (0,'"+Arrayvideo+"')";
        try{
            Long resultado = db.insert(Transacciones.TablaVideo, Transacciones.id,valores);
            Toast.makeText(getApplicationContext(),"Video Guardado COD: "+resultado.toString(), Toast.LENGTH_LONG).show();

            db.close();
        }catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"error "+e, Toast.LENGTH_LONG).show();
        }
        limpiarPantalla();
        Arrayvideo = null;
    }

    private void limpiarPantalla() {
        videoview.setVideoURI(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            File vidFile = new File(curpath);
            Uri urivideo = Uri.fromFile(vidFile);
            videoview.setVideoURI(urivideo);
            videoview.setMediaController(new MediaController(this));
            videoview.requestFocus();
            videoview.start();
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Arrayvideo = Files.readAllBytes(vidFile.toPath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void VideoIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File videofile = null;
            try {
                videofile = createVideo();
                Toast.makeText(getApplicationContext(), "Video Creado", Toast.LENGTH_LONG).show();
                AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
                adb.setTitle("EXITO");
                adb.setMessage("Video Creado");
                adb.setPositiveButton("Aceptar", null);
                adb.show();
            } catch (IOException ex) {
                Toast.makeText(getApplicationContext(), "Error al guardar", Toast.LENGTH_LONG).show();
            }
            SystemClock.sleep(1000);
            Uri vidURI = null;
            if (videofile != null) {
                try {
                    vidURI = FileProvider.getUriForFile(this, "com.example.tarea2_1jonathand.provider", videofile);
                    Toast.makeText(getApplicationContext(), "Ruta Obtenida", Toast.LENGTH_LONG).show();
                } catch (Exception ex) {
                    Toast.makeText(getApplicationContext(), "Problemas con la ruta " + ex, Toast.LENGTH_LONG).show();
                    System.out.println(ex);
                }
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, vidURI);
                startActivityForResult(takePictureIntent, REQUEST_VIDEO_CAPTURE);
            }
        }
    }

}