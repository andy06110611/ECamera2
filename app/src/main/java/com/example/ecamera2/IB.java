package com.example.ecamera2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.TextView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

public class IB {
    private TextView tv_score;     //顯示強度構圖分數
    public double getIBscore (Bitmap bitmap){

        

        //BitmapFactory.Options bfoOptions = new BitmapFactory.Options();
        //bfoOptions.inScaled = false;

        //Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.e31);
        //float d = getResources().getDisplayMetrics().density;
        float width = bitmap.getWidth();
        float height = bitmap.getHeight();

        Mat matSrc = new Mat();
        Utils.bitmapToMat(bitmap,matSrc);
        Mat uncropped = matSrc;

        //////////////////////////左
        Rect roil = new Rect(0, 0, (int)bitmap.getWidth()/2, (int)bitmap.getHeight());
        Mat croppedl = new Mat(uncropped, roil);
        java.util.List<Mat> imagesl = new ArrayList<>();
        imagesl.add(croppedl);
        MatOfInt channels = new MatOfInt(0); // 圖像通道数，0表示只有一個通道
        MatOfInt histSize = new MatOfInt(256); // CV_8U類型的圖片範圍是0~255，共有256個灰度级
        Mat histogramOfGrayl = new Mat(); // 輸出直方圖结果，共有256行，行數的相當於對應灰度值，每一行的值相當於该灰度值所占比例
        MatOfFloat histRange = new MatOfFloat(0, 255);
        Imgproc.calcHist(imagesl, channels, new Mat(), histogramOfGrayl, histSize, histRange, false);
        //Core.normalize(histogramOfGrayl, histogramOfGrayl, 0, histogramOfGrayl.rows(), Core.NORM_MINMAX, -1, new Mat());

        //////////////////////右

        Rect roir = new Rect((int)bitmap.getWidth()/2, 0, (int)bitmap.getWidth()/2,(int)bitmap.getHeight());
        Mat croppedr = new Mat(uncropped, roir);
        java.util.List<Mat> imagesr = new ArrayList<>();
        imagesr.add(croppedr);
        Mat histogramOfGrayr = new Mat(); // 輸出直方圖结果，共有256行，行數的相當於對應灰度值，每一行的值相當於该灰度值所占比例
        Imgproc.calcHist(imagesr, channels, new Mat(), histogramOfGrayr, histSize, histRange, false);
        //Core.normalize(histogramOfGrayr, histogramOfGrayr, 0, histogramOfGrayr.rows(), Core.NORM_MINMAX, -1, new Mat());

        float diff = 0;
        for(int i= 0;i <= 255;i++){
            diff += Math.abs(histogramOfGrayl.get(i, 0)[0] - histogramOfGrayr.get(i, 0)[0]);
        }
        //(int) Math.floor(histImgCols / histSize.get(0, 0)[0])
        //diff /=  8.3;        //怪怪的
        float worst = (int)width * (int)height;
        float intensity = 100 - (diff/worst)*100;
        //int Iintensity = (int)intensity;
        //String stringIntensity = Integer.toString(Iintensity);


        return intensity;
    }
}
