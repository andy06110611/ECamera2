package com.example.ecamera2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.w3c.dom.Text;

/***********************
 #author:品祥
 簡單不extend listener之使用sensor方法
 這個code是在顯示gyroscope之角速度
 註解掉的函式是用來計算整個方向移動了多少但不精確
 ***********************/
public class MainActivity extends Activity{

    private SensorManager sm;
    private Sensor sr , sr2 , sr3;
    private TextView azimuthAngle;

    private float[] accelerometerValues = new float[3];
    private float[] magneticFieldValues = new float[3];
    private float[] GYROSCOPEValues = new float[6];

    private static final String TAG = "---MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
//        sm2 = (SensorManager) getSystemService(SENSOR_SERVICE);

        sr = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sr2 = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sr3 = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        azimuthAngle = (TextView)findViewById(R.id.textView);
//        txv2 = (TextView)findViewById(R.id.textView2);
    }

    @Override
    protected void onResume(){
        super.onResume();
//        sm.registerListener(new MySensorEventListener(), sr, SensorManager.SENSOR_DELAY_NORMAL, Sensor.TYPE_ACCELEROMETER);
//        sm.registerListener(new MySensorEventListener(), sr2, SensorManager.SENSOR_DELAY_NORMAL, Sensor.TYPE_MAGNETIC_FIELD);
        sm.registerListener(new MySensorEventListener(), sr3, SensorManager.SENSOR_DELAY_NORMAL, Sensor.TYPE_GYROSCOPE);
//
//        sm2.registerListener(this, sr2, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause(){
        super.onPause();
        sm.unregisterListener(new MySensorEventListener());
//        sm2.unregisterListener(this);
    }

    private void calculaterOrientation(){
        float[] values = new float[3];
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(R, values);
        values[0] = (float)Math.toDegrees(values[0]);

        Log.i(TAG, values[0] + "");
                azimuthAngle.setText(String.format("角度: %1.2f", values[0]));
            }

    private void cal(){
        azimuthAngle.setText(String.format("角度: %1.2f", GYROSCOPEValues[2]));
    }
    class MySensorEventListener implements  SensorEventListener{
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accelerometerValues = event.values;
            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticFieldValues = event.values;
            }if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
                GYROSCOPEValues = event.values;
                cal();
            }
//            calculaterOrientation();

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy){
        }
    }
}