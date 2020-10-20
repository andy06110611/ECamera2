package com.example.ecamera2;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class vanishpoint  {

//    Mat img;                                //照片
    Mat lines;                              //記錄霍夫線

    List<Point> filter_point;               //過濾後構成直線的兩個座標
    List<Point> pt_xy;                      //紀錄兩直線的交叉座標
    List<Double[]> pt_abc;                  //紀錄直線的abc常數值
    List<Double> minD;                      //紀錄點到所有線的距離

    int minNo_pt_xy;                        //紀錄pt_xy中距離所有線距離和最短的座標
    double best_distance;                   //紀錄vanish point到7點中最短的距離
    double score = 0;                           //記錄此張照片的分數


    //    主函式
    public double vanishpoint(Mat img)
    {
//        轉灰階
        Mat gray = new Mat();
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(gray, gray, new Size(3,3),0);

//        找邊緣
        Mat edges = new Mat();
        Imgproc.Canny(gray, edges, 70,220);

//        記錄霍夫線
        lines = new Mat();
        Imgproc.HoughLinesP(edges, lines, 2, Math.PI / 180, 25, 200, 17);

//        紀錄要畫出的線
        Mat houghlines = new Mat();
        houghlines.create(edges.rows(), edges.cols(), CvType.CV_8UC1);

        img = find_xy3(img);                  //尋找適當的線座標

        if( filter_point.isEmpty())
        {
            score = 0;
//            return img;
            return score;
        }

        img = find_line_cross(img);          //尋找兩線的交叉座標，並記錄該方程式的abc常數值

        minNo_pt_xy = cal_xy_abc_mindistance();   //找出這些交叉點中，哪一個座標距離所有直線的距離和最小

        img = draw_rec(img);

        img = cal_7point(img);               //找出這些交叉點中，哪一個座標距離所有直線的距離和最小

        cal_score(img);                //計算分數

        if(score < 0)
            score = 0;
        return score;

//        return img;
    }

/***********************************************************************************************/
//尋找適當的線座標
/***********************************************************************************************/
    public Mat find_xy(Mat img)
    {
        filter_point = new ArrayList<Point>();
        int filter_point_i = 0;
        double[] points;
        double x1, y1, x2, y2;  //兩個座標值
        int theta;  //兩個座標角度

//        int row = lines.rows();
        for(int i = 0; i < lines.rows(); i++)
        {
            points = lines.get(i,0);

            x1 = points[0];
            y1 = points[1];
            x2 = points[2];
            y2 = points[3];

            Point pt1 = new Point(x1,y1);
            Point pt2 = new Point(x2, y2);
            theta = (int) azimuuthAngle(x1,y1, x2, y2);

            if( (theta > 20 && theta < 70) || (theta > 110 && theta < 160) || (theta > 200 && theta < 250) || (theta > 290 && theta < 340))
            {
//                畫出過濾後的線
                //Imgproc.line(img, pt1, pt2, new Scalar(0, 255, 0), 2);  //0909改

                filter_point.add(filter_point_i, pt1);
                filter_point_i++;
                filter_point.add(filter_point_i, pt2);
                filter_point_i++;
            }
        }
        return img;
    }

    public Mat find_xy2(Mat img)
    {
        double w, h;
        w = img.width();
        h = img.height();

        double w0, w1, w2, w3, w4, w5;
        w0 = 0;
        w1 = w / 5;
        w2 = w / 5 * 2;
        w3 = w / 5 * 3;
        w4 = w / 5 * 4;
        w5 = w;

        double h0, h1, h2, h3, h4, h5;
        h0 = 0;
        h1 = h / 5;
        h2 = h / 5 * 2;
        h3 = h / 5 * 3;
        h4 = h / 5 * 4;
        h5 = h;

        double[] points;
        double x1, y1, x2, y2;  //兩個座標值

        filter_point = new ArrayList<Point>();
        int filter_point_i = 0;
        int theta;  //兩個座標角度

        for(int i = 0; i < lines.rows(); i++)
        {
            points = lines.get(i,0);

            //設定x1一定是比較大的
            if(points[0] > points[2])
            {
                x1 = points[0];
                y1 = points[1];
                x2 = points[2];
                y2 = points[3];
            }
            else{
                x1 = points[2];
                y1 = points[3];
                x2 = points[0];
                y2 = points[1];
            }

            Point pt1 = new Point(x1,y1);
            Point pt2 = new Point(x2, y2);
            theta = (int) azimuuthAngle(x1,y1, x2, y2);

            //判定point1和point2的相對位置關係
            if(y1 > y2)//為第二、四象限
            {
                if(x1 < w3 && y1 < h3 && x2 < w2 && y2 < h2)//為第二象限
                {
                    if(theta > 200 && theta < 250)
                    {
                        //畫出過濾後的線
                        Imgproc.line(img, pt1, pt2, new Scalar(0, 1, 0), 2);

                        filter_point.add(filter_point_i, pt1);
                        filter_point_i++;
                        filter_point.add(filter_point_i, pt2);
                        filter_point_i++;
                    }
                    //此線不畫
                }
                else if(x1 > w3 && y1 > h3 && x2 > h2 && y2 > h2)//為第四象限
                {
                    if(theta > 20 && theta < 70)
                    {
                        //畫出過濾後的線
                        Imgproc.line(img, pt1, pt2, new Scalar(0, 255, 0), 2);

                        filter_point.add(filter_point_i, pt1);
                        filter_point_i++;
                        filter_point.add(filter_point_i, pt2);
                        filter_point_i++;
                    }
                    //此線不畫
                }
                //此線不畫
            }
            else{//為第一、三象限
                if(x1 >  w3 && y1 < h2 && x2 > w2 && y2 < w3)//為第一象限
                {
                    if(theta > 290 && theta <340)
                    {
                        //畫出過濾後的線
                        Imgproc.line(img, pt1, pt2, new Scalar(0, 255, 0), 2);

                        filter_point.add(filter_point_i, pt1);
                        filter_point_i++;
                        filter_point.add(filter_point_i, pt2);
                        filter_point_i++;
                    }
                    //此線不畫
                }
                else if(x1 < w3 && y1 > h2 && x2 < w2 && y2 > h3)//為第三象限
                {
                    if(theta > 290 && theta <340)
                    {
                        //畫出過濾後的線
                        Imgproc.line(img, pt1, pt2, new Scalar(0, 255, 0), 2);

                        filter_point.add(filter_point_i, pt1);
                        filter_point_i++;
                        filter_point.add(filter_point_i, pt2);
                        filter_point_i++;
                    }
                    //此線不畫
                }
            }
        }
        return img;
    }

    public Mat find_xy3(Mat img)
    {
        filter_point = new ArrayList<Point>();
            int filter_point_i = 0;
            double[] points;
            double x1, y1, x2, y2;  //兩個座標值

            for(int i = 0; i < lines.rows(); i++)
            {
                points = lines.get(i,0);

                x1 = points[0];
                y1 = points[1];
                x2 = points[2];
                y2 = points[3];

                Point pt1 = new Point(x1,y1);
                Point pt2 = new Point(x2, y2);


            //畫出過濾後的線
            //Imgproc.line(img, pt1, pt2, new Scalar(0, 255, 0), 2);  //0909改

            filter_point.add(filter_point_i, pt1);
            filter_point_i++;
            filter_point.add(filter_point_i, pt2);
            filter_point_i++;
        }

        return img;
    }


/***********************************************************************************************/
//尋找兩線的交叉座標，並記錄該方程式的abc常數值
/***********************************************************************************************/
    public Mat find_line_cross(Mat img)
    {
        int filter_point_i = 0;

        pt_xy = new ArrayList<Point>();
        int pt_xy_i = 0;

        pt_abc = new ArrayList<Double[]>();
        int pt_abc_i = 0;
        int ff = filter_point.size();

        //先找出所有線的abc
        while (filter_point_i < filter_point.size())
        {
            double x1, y1, x2, y2;

            x1 = filter_point.get(filter_point_i).x;
            y1 = filter_point.get(filter_point_i).y;
            x2 = filter_point.get(filter_point_i + 1).x;
            y2 = filter_point.get(filter_point_i + 1).y;

            pt_abc.add(pt_abc_i,find_equation(x1, y1, x2, y2));

            filter_point_i = filter_point_i + 2;
            pt_abc_i++;
        }

        //找出交叉點，並記錄
        pt_abc_i = 0;
        while (pt_abc_i < (pt_abc.size() - 1))
        {
            int pt_abc_cmp_i = pt_abc_i + 1;
            while (pt_abc_cmp_i < pt_abc.size())
            {
                double[] xy;
                xy = find_crosspoint(pt_abc.get(pt_abc_i), pt_abc.get(pt_abc_cmp_i));
                if(xy[0] != Double.NEGATIVE_INFINITY && xy[1] != Double.NEGATIVE_INFINITY && xy[0] != Double.POSITIVE_INFINITY && xy[1] != Double.POSITIVE_INFINITY)
                {
                    Point xy_p = new Point(xy[0], xy[1]);
//                    Imgproc.circle(img, xy_p, 2, new Scalar(255,0,0), 2);

                    pt_xy.add(pt_xy_i, xy_p);
                    pt_xy_i++;
                }
                pt_abc_cmp_i ++;
            }
            pt_abc_i++;
        }
        return img;
    }

/***********************************************************************************************/
//找出這些交叉點中，哪一個座標距離所有直線的距離和最小
/***********************************************************************************************/
    public int cal_xy_abc_mindistance()
    {
        minD = new ArrayList<Double>();
        int minD_i = 0;
        double tem = 0;

        int pt_xy_i = 0;
        int pt_abc_i = 0;

        double minDistance = Math.pow(2,100);                     //紀錄最短距離
        int minDistance_no = 0;                     //紀錄最短距離的座標的索引值

        while(pt_xy_i < pt_xy.size())
        {
            while (pt_abc_i < pt_abc.size())
            {
                tem += find_mindistance(pt_xy.get(pt_xy_i), pt_abc.get(pt_abc_i));
                pt_abc_i++;
            }

            minD.add(minD_i, tem);

            //和目前最短距離和比較，若新值較小，更動為最新版最短距離和
            if(minDistance > tem)
            {
                minDistance = tem;
                minDistance_no = minD_i;
            }
            minD_i++;
            tem = 0;
            pt_xy_i++;
            pt_abc_i = 0;
        }
        return minDistance_no;
    }

/***********************************************************************************************/
//劃出框框
/***********************************************************************************************/ 
    public Mat draw_rec(Mat img)
    {
        //畫出算出來的中心點
        Imgproc.circle(img, pt_xy.get(minNo_pt_xy), 5,new Scalar(0,0,0), 30);

        //畫出算出來的中心點的框框
        Point r1 = new Point();
        Point r2 = new Point();
        r1.x = pt_xy.get(minNo_pt_xy).x - img.width() / 16;
        r1.y = pt_xy.get(minNo_pt_xy).y - img.height() / 16;
        r2.x = pt_xy.get(minNo_pt_xy).x + img.width() / 16;
        r2.y = pt_xy.get(minNo_pt_xy).y + img.height() / 16;
        Imgproc.rectangle(img, r1, r2, new Scalar(0, 255, 255), 20);

        //畫出照片中心
        r1.x = img.width() / 16 * 7;
        r1.y = img.height() / 16 * 7;
        r2.x = img.width() / 16 * 9;
        r2.y = img.height() / 16 * 9;
        Imgproc.rectangle(img, r1, r2, new Scalar(0,0,255), 20);

        return img;
    }
/***********************************************************************************************/
//找出這些交叉點中，哪一個座標距離所有直線的距離和最小
/***********************************************************************************************/
    public Mat cal_7point(Mat img)
    {
        Imgproc.circle(img, pt_xy.get(minNo_pt_xy), 2, new Scalar(255,0,0), 2);

        //取的照片的長寬
        double img_w = img.size().width;
        double img_h = img.size().height;

        //取的7點的比較值
        List<Point> img_7point = new ArrayList<Point>();

        Point tem = new Point(img_w / 3, img_h / 3);
        img_7point.add(0, tem);

        tem = new Point(img_w / 2, img_h / 3);
        img_7point.add(1, tem);

        tem = new Point(img_w * 2 / 3, img_h / 3);
        img_7point.add(2, tem);

        tem = new Point(img_w / 2, img_h / 2);
        img_7point.add(3, tem);

        tem = new Point(img_w / 3, img_h * 2 / 3);
        img_7point.add(4, tem);

        tem = new Point(img_w / 2, img_h * 2 / 3);
        img_7point.add(5, tem);

        tem = new Point(img_w * 2 / 3, img_h * 2 / 3);
        img_7point.add(6, tem);

        int img_7point_i = 0;
        best_distance = 10000;

        Point p_sxsy;
        double sx, sy;

        while(img_7point_i < img_7point.size())
        {
            sx = img_7point.get(img_7point_i).x;
            sy = img_7point.get(img_7point_i).y;
            p_sxsy = new Point(sx, sy);

            //畫出7點
            //Imgproc.circle(img, p_sxsy, 5, new Scalar(0, 255, 255), 10);//0909改

            double p_to_p = point_to_point(sx,sy);
            if(best_distance > p_to_p)
            {
                best_distance = p_to_p;
            }
            img_7point_i++;
        }
        return img;
    }

    public Mat cal_7point2(Mat img)
    {
        Imgproc.circle(img, pt_xy.get(minNo_pt_xy), 2, new Scalar(255,0,0), 2);

        //取的照片的長寬
        double img_w = img.size().width;
        double img_h = img.size().height;

        //取的7點的比較值
        List<Point> img_7point = new ArrayList<Point>();

        Point tem = new Point(img_w / 2, img_h / 3);
        img_7point.add(0, tem);

        tem = new Point(img_w / 2, img_h / 2);
        img_7point.add(1, tem);

        tem = new Point(img_w / 2, img_h * 2 / 3);
        img_7point.add(2, tem);

        int img_7point_i = 0;
        best_distance = 10000;

        Point p_sxsy;
        double sx, sy;

        while(img_7point_i < img_7point.size())
        {
            sx = img_7point.get(img_7point_i).x;
            sy = img_7point.get(img_7point_i).y;
            p_sxsy = new Point(sx, sy);

            //畫出7點
//            Imgproc.circle(img, p_sxsy, 5, new Scalar(0, 255, 255), 10);

            double p_to_p = point_to_point(sx,sy);
            if(best_distance > p_to_p)
            {
                best_distance = p_to_p;
            }
            img_7point_i++;
        }
        return img;
    }

/***********************************************************************************************/
//計算vanish point和 7個點距離
    /***********************************************************************************************/
    public double point_to_point(double sx, double sy)
    {
        double p;
        double d1 = pt_xy.get(minNo_pt_xy).x;
        double d2 = pt_xy.get(minNo_pt_xy).y;

        p = Math.sqrt(Math.pow(Math.abs(d1 - sx), 2) + Math.pow(Math.abs(d2 - sy), 2));
        return p;
    }

/***********************************************************************************************/
//計算分數
    /***********************************************************************************************/
    public void cal_score(Mat img)
    {
        //取的照片的長寬
        double img_w = img.size().width;
        double img_h = img.size().height;
        double worst = Math.sqrt(Math.pow(img_w / 3, 2) + Math.pow(img_h / 3, 2));
        score = 100 - best_distance / worst * 100;
    }

/***********************************************************************************************/
//計算角度
    /***********************************************************************************************/
    public double azimuuthAngle(double x1, double y1, double x2, double y2)
    {
        double angle = 0.0;
        double dx, dy;

        dx = x2 -x1;
        dy = y2 -y1;

        if(x2 == x1)
        {
            angle = Math.PI / 2.0;
            if(y2 == y1)
                angle = 0.0;
            else if(y2 <y1)
                angle = 3.0 * Math.PI / 2.0;
        }
        else if(x2 > x1 && y2 > y1)
            angle = Math.atan(dx / dy);
        else if(x2 > x1 && y2 < y1)
            angle = Math.PI / 2 +Math.atan(-dy / dx);
        else if(x2 < x1 && y2 < y1)
            angle = Math.PI + Math.atan(dx / dy);
        else if(x2 < x1 && y2 > y1)
            angle = 3.0 * Math.PI / 2.0 + Math.atan(dy / -dx);

        return (angle * 180 / Math.PI);
    }

/***********************************************************************************************/
//    找出線的方程式
    /***********************************************************************************************/
    public Double[] find_equation(double x1, double y1, double x2, double y2)
    {
        Double[][] abc = new Double[1][3];
        abc[0][0] = -(y2 - y1);
        abc[0][1] = x2 - x1;
        abc[0][2] = abc[0][0] * x1 + abc[0][1] * y1;
        return abc[0];
    }

/***********************************************************************************************/
//    找出兩線的焦點
    /***********************************************************************************************/
    public double[] find_crosspoint(Double[] abc1, Double[] abc2)
    {
        double[] xy = new double[2];
        try{
            xy[1] = (abc1[2] * abc2[0] - abc2[2] * abc1[0]) / (abc2[0] * abc1[1] - abc1[0] * abc2[1]);
            xy[0] = (abc1[2] - abc1[1] * xy[1]) / abc1[0];
            return xy;
        }
        catch (Exception mm)
        {
            return xy;
        }
    }

/***********************************************************************************************/
//     計算點到直線的最短距離
    /***********************************************************************************************/
    public Double find_mindistance(Point xy, Double[] abc)
    {
        Double mindis = Math.abs(abc[0] * xy.x + abc[1] * xy.y + abc[2] / Math.sqrt(Math.pow(abc[0], 2) + Math.pow(abc[1], 2)));
        return mindis;
    }


}


