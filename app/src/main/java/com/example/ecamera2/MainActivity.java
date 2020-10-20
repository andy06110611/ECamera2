package com.example.ecamera2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button btn;
    private SurfaceView  mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new drawRect(getApplicationContext()));

//        mSurfaceView = (SurfaceView)findViewById(R.id.rectSurfaceView);
//
//        mSurfaceHolder = mSurfaceView.getHolder();
//        mSurfaceHolder.setKeepScreenOn(true);
//
//        drawRect test1 = new drawRect(getApplicationContext());
//
    }
}