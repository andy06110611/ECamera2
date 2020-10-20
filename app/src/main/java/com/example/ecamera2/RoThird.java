package com.example.ecamera2;
import android.graphics.Bitmap;
import android.graphics.Picture;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.opencv.imgproc.Imgproc.THRESH_BINARY;

public class RoThird {
    Mat tem_img = new Mat();
    Mat HSV_img = new Mat();
    Mat mGray = new Mat();
    Mat meanshift = new Mat();

    Double score = 0.0;
    Double shortestDis = 10000.0;

    Point Center = new Point();
    Point checkPoint = new Point();




    public Double rotMain(Bitmap tem){
        Utils.bitmapToMat(tem, tem_img);
        Imgproc.GaussianBlur(tem_img, tem_img, new Size(9, 9), 0);

        Imgproc.cvtColor(tem_img, HSV_img, Imgproc.COLOR_BGR2HSV);
        Imgproc.cvtColor(HSV_img, mGray, Imgproc.COLOR_BGR2HSV_FULL);
        //還找不到用函式的辦法
//        Imgproc.pyrMeanShiftFiltering(tem_img, tem_img, 2, 10, 4);

        mGray = backproject(HSV_img);
        Imgproc.threshold(mGray, mGray, 200, 255, THRESH_BINARY);
        findMaxrect(mGray);
        calScore();
        System.out.println(score);
        return score;
    }

    /******************分出前景後景**********************/
    private Mat backproject(Mat source){
        Mat backproj = new Mat();
        Mat Hist = new Mat();
        int h_bins = 3, s_bins = 3;

        MatOfInt HistSize = new MatOfInt(h_bins, s_bins);
        MatOfFloat histRange = new MatOfFloat(0, 179, 0, 255);
        MatOfInt Channels = new MatOfInt(0, 1);
        boolean accumulate = false;

        //計算直方圖
        Imgproc.calcHist(Arrays.asList(HSV_img), Channels, new Mat(), Hist, HistSize, histRange, false);
//        Imgproc.calcHist(hsvPlanes1, new MatOfInt(1), new Mat(), sHist, new MatOfInt(histSize), histRange2, false);

        //normalize
        Core.normalize(Hist, Hist, 0, 255, Core.NORM_MINMAX);

        //將直方圖畫回image
        Imgproc.calcBackProject(Arrays.asList(HSV_img), Channels, Hist, backproj, histRange, 1);

        //倒轉
        backproj = inverse(backproj);


        return  backproj;
    }

    /*******************倒致**********************/
    private Mat inverse(Mat input){
        Mat invertcolormatrix= new Mat(input.rows(),input.cols(), input.type(), new Scalar(255,255,255));

        Core.subtract(invertcolormatrix, input, input);
        return input;
    }

    /******************找出前景主體*********************/
    private void findMaxrect(Mat input){
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();

        //尋找輪廓
        Imgproc.findContours(input, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        //最大輪廓
        double area = Imgproc.boundingRect(contours.get(0)).area();
        int index = 0;
        for (int i = 0; i < contours.size(); i++) {
            double tempArea = Imgproc.boundingRect(contours.get(i)).area();
            if (tempArea > area) {
                area = tempArea;
                index = i;
            }
        }

        //找出最大輪廓的中心
//        Imgproc.drawContours(tem_img, contours, index, new Scalar(0,255,255), 5);
        MatOfPoint2f matOfPoint2f = new MatOfPoint2f(contours.get(index).toArray());
        RotatedRect box = Imgproc.minAreaRect(matOfPoint2f);
        Center = box.center;
    }

    private void calScore(){
        double width= tem_img.width(), height = tem_img.height();
        double degree = 0.0;
        /***************計算與圖上6個點之最短距離********************/
        for(double i = 2; i <=4 ; i= i+2){
            double tem_witgh = width / 6 * i;
            for(double j = 2; j <= 4; j++){
                double tem_height = height / 6 * j;
                Point temPoint = new Point(tem_witgh, tem_height);
                double temDis = calDistance(temPoint, Center);
                if(shortestDis > temDis){
                    shortestDis = temDis;
                    checkPoint = temPoint;
                }
            }
        }
        if(Math.abs(checkPoint.x -0 ) > Math.abs(checkPoint.x - width)){
            degree =  90 * ( 1 / checkPoint.x - 0) * shortestDis;
        }else{
            degree =  90 * ( 1 / (width - checkPoint.x)) * shortestDis;
        }
        score = Math.cos(Math.toRadians(degree)) * 100;

        BigDecimal b = new BigDecimal(score);
        score = b.setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue();
    }
    /**************計算點到點之距離********************/
    private double calDistance(Point check, Point center){
        Double xDis = Math.abs(check.x - center.x);
        Double yDis = Math.abs(check.y - center.y);
        Double Distance = Math.sqrt(xDis * xDis + yDis * yDis);

        BigDecimal b = new BigDecimal(Distance);
        Distance = b.setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue();
        return Distance;
    }


    public Bitmap recommend(Bitmap temBitmap){
        Mat temMat = new Mat();
        Utils.bitmapToMat(temBitmap, temMat);

        //drawLine
        Imgproc.rectangle(temMat, new Point(Center.x + (temMat.width()/ 16), Center.y + (temMat.height()/ 16)), new Point(Center.x - (temMat.width()/ 16), Center.y - (temMat.height()/ 16)), new Scalar(0, 255, 255), 10);
        Imgproc.rectangle(temMat, new Point(checkPoint.x + (temMat.width()/ 16), checkPoint.y + (temMat.height()/ 16)), new Point(checkPoint.x - (temMat.width()/ 16), checkPoint.y - (temMat.height()/ 16)), new Scalar(0, 0, 255), 10);

        Utils.matToBitmap(temMat, temBitmap);
        return temBitmap;
    }
}
