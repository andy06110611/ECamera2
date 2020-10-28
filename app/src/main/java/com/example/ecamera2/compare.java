package com.example.ecamera2;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;

public class compare {
    public boolean comparePic(Mat sourceMat, Mat templateMat, Point destination){
        boolean good = false;

        MatOfFloat ranges = new MatOfFloat(0f, 256f);
        MatOfInt histSize = new MatOfInt(1000);

        Imgproc.calcHist(Arrays.asList(sourceMat), new MatOfInt(0), new Mat(), sourceMat, histSize, ranges);
        Imgproc.calcHist(Arrays.asList(templateMat), new MatOfInt(0), new Mat(), templateMat, histSize, ranges);
        double res = Imgproc.compareHist(sourceMat, templateMat, Imgproc.CV_COMP_CORREL);
        if(res > 0.7){
            good = true;
        }
        return good;
    }

    public Mat pictureCut(Bitmap tem_img, Point Center){
        Mat tem = new Mat();
        Utils.bitmapToMat(tem_img, tem);
        int[] point = new int[2];
        point[0] = (int) Center.x;
        point[1] = (int) Center.y;

        int disHeight = 480;
        int disWidth = 270;

        Rect rect = new Rect(point[0] - disWidth/2, point[1] - disHeight/2, disWidth, disHeight);

        return new Mat(tem, rect);
    }
}
