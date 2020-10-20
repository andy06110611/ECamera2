package com.example.ecamera2;


import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;


import androidx.appcompat.app.AlertDialog;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

public class Pager1 extends RelativeLayout{
    String[] items={"強度平衡", "水平構圖","三分構圖","消失點構圖"};
    boolean[] selection={false, false, false, false};
    private Context context1;
    View view;

    public Pager1(Context context) {
        super(context);
        context1 = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.my_pager1, null);//連接頁面
        ImageButton select_composition = view.findViewById(R.id.btn_selectMode);//取得頁面元件
        addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        //將元件放入ViewPager




    }

    boolean ib;
    boolean horizontal;
    boolean third;
    boolean vp;
    private int checkRecommend = 0;
    private Bitmap recommendImg;
    private ImageView img_recommend;

    public  String changeType(double a){
        int score = (int)a;
        String score_string = Integer.toString(score);
        return  score_string;
    }

    public void selectMode(Bitmap bitmap, Mat img,boolean check[],Context context){
        ib = check[0];
        horizontal = check[1];
        third = check[2];
        vp = check[3];

        final IB i = new IB();
        final horizontal h = new horizontal();
        final RoThird r = new RoThird();
        final vanishpoint v = new vanishpoint();

        int highestComposition = 0;
        double highest;

        Double IBscore = 0.0;
        Double VPscore = 0.0;
        Double Hscore = 0.0;
        Double Rscore = 0.0;

        String SIBscore = null;
        String SVPscore = null;
        String SHscore = null;
        String SRscore = null;

        if(vp && horizontal && third){
            if(ib){
                IBscore = i.getIBscore(bitmap);
                SIBscore = changeType(IBscore);
            }
            Rscore = r.rotMain(bitmap);
            VPscore = v.vanishpoint(img);
            Hscore = h.horizontal_composition(bitmap);


            if(Hscore < VPscore){

                if(Rscore < VPscore){
                    highestComposition = 2;  //設消失點構圖分數最高
                    highest = VPscore;
                }
                else{
                    highestComposition = 1;  //設三分構圖分數最高
                    highest = Rscore;
                }
            }else{
                if(Rscore < Hscore){
                    highestComposition = 3;  //設水平構圖分數最高
                    highest = Hscore;
                }
                else{
                    highestComposition = 1;  //設三分構圖分數最高
                    highest = Rscore;
                }

            }

            SRscore = changeType(Rscore);
            SVPscore = changeType(VPscore);
            SHscore = changeType(Hscore);

            if(ib){
                new AlertDialog.Builder(context)
                        .setTitle("分數")
                        .setMessage("強度平衡"+SIBscore+"\n"+
                                "三分構圖"+SRscore+"\n"+
                                "消失點構圖"+SVPscore+"\n"+
                                "水平構圖"+SHscore)
                        .show();
            }else{
                new AlertDialog.Builder(context)
                        .setTitle("分數")
                        .setMessage("三分構圖"+SRscore+"\n"+
                                "消失點構圖"+SVPscore+"\n"+
                                "水平構圖"+SHscore)
                        .show();
            }


            selectRecommend(highestComposition,highest,bitmap,img,context);

        }
        else if(vp && horizontal ){
            if(ib){
                IBscore = i.getIBscore(bitmap);
                SIBscore = changeType(IBscore);
            }
            VPscore = v.vanishpoint(img);
            Hscore = h.horizontal_composition(bitmap);

            highestComposition = 2;   //消失點構圖最高分
            highest = VPscore;
            if(VPscore < Hscore){
                highestComposition = 3;  //水平構圖最高分
                highest = Hscore;
            }

            SVPscore = changeType(VPscore);
            SHscore = changeType(Hscore);

            if(ib){
                new AlertDialog.Builder(context)
                        .setTitle("分數")
                        .setMessage("強度平衡"+SIBscore+"\n"+
                                "消失點構圖"+SVPscore+"\n"+
                                "水平構圖"+SHscore)
                        .show();
            }else{
                new AlertDialog.Builder(context)
                        .setTitle("分數")
                        .setMessage("消失點構圖"+SVPscore+"\n"+
                                "水平構圖"+SHscore)
                        .show();
            }


            selectRecommend(highestComposition,highest,bitmap,img,context);

        }else if(vp && third){
            if(ib){
                IBscore = i.getIBscore(bitmap);
                SIBscore = changeType(IBscore);
            }
            Rscore = r.rotMain(bitmap);
            VPscore = v.vanishpoint(img);

            highestComposition = 1;   //三分構圖最高分
            highest = Rscore;
            if(Rscore < VPscore){
                highestComposition = 2;  //消失點構圖最高分
                highest = VPscore;
            }

            SRscore = changeType(Rscore);
            SVPscore = changeType(VPscore);

            if(ib){
                new AlertDialog.Builder(context)
                        .setTitle("分數")
                        .setMessage("強度平衡"+SIBscore+"\n"+
                                "三分構圖"+SRscore+"\n"+
                                "消失點構圖"+SVPscore)
                        .show();

            }else{
                new AlertDialog.Builder(context)
                        .setTitle("分數")
                        .setMessage("三分構圖"+SRscore+"\n"+
                                "消失點構圖"+SVPscore)
                        .show();
            }


            selectRecommend(highestComposition,highest,bitmap,img,context);

        }else if(horizontal && third){
            if(ib){
                IBscore = i.getIBscore(bitmap);
                SIBscore = changeType(IBscore);
            }
            Rscore = r.rotMain(bitmap);
            Hscore = h.horizontal_composition(bitmap);

            highestComposition = 1;   //三分構圖最高分
            highest = Rscore;
            if(Rscore < Hscore){
                highestComposition = 3;  //水平構圖最高分
                highest = Hscore;
            }

            SRscore = changeType(Rscore);
            SHscore = changeType(Hscore);

            if(ib){
                new AlertDialog.Builder(context)
                        .setTitle("分數")
                        .setMessage("強度平衡"+SIBscore+"\n"+
                                "三分構圖"+SRscore+"\n"+
                                "水平構圖"+SHscore)
                        .show();
            }else{
                new AlertDialog.Builder(context)
                        .setTitle("分數")
                        .setMessage("三分構圖"+SRscore+"\n"+
                                "水平構圖"+SHscore)
                        .show();
            }


            selectRecommend(highestComposition,highest,bitmap,img,context);



        }else if(third){
            if(ib){
                IBscore = i.getIBscore(bitmap);
                SIBscore = changeType(IBscore);
            }
            Rscore = r.rotMain(bitmap);

            highest = Rscore;
            highestComposition = 1;

            SRscore = changeType(Rscore);

            if(ib){
                new AlertDialog.Builder(context)
                        .setTitle("分數")
                        .setMessage("強度平衡"+SIBscore+"\n"+
                                "三分構圖"+SRscore)
                        .show();
            }else{
                new AlertDialog.Builder(context)
                        .setTitle("分數")
                        .setMessage("三分構圖"+SRscore)
                        .show();
            }



            selectRecommend(highestComposition,highest,bitmap,img,context);


        }else if(horizontal){
            if(ib){
                IBscore = i.getIBscore(bitmap);
                SIBscore = changeType(IBscore);
            }
            Hscore = h.horizontal_composition(bitmap);

            highest = Hscore;
            highestComposition = 3;

            SHscore = changeType(Hscore);

            if(ib){
                new AlertDialog.Builder(context)
                        .setTitle("分數")
                        .setMessage("強度平衡"+SIBscore+"\n"+
                                "水平構圖"+SHscore)
                        .show();
            }else{
                new AlertDialog.Builder(context)
                        .setTitle("分數")
                        .setMessage("水平構圖"+SHscore)
                        .show();
            }


            selectRecommend(highestComposition,highest,bitmap,img,context);


        }else if(vp){
            if(ib){
                IBscore = i.getIBscore(bitmap);
                SIBscore = changeType(IBscore);
            }
            VPscore = v.vanishpoint(img);

            highest = VPscore;
            highestComposition = 2;

            SVPscore = changeType(VPscore);

            if(ib){
                new AlertDialog.Builder(context)
                        .setTitle("分數")
                        .setMessage("強度平衡"+SIBscore+"\n"+
                                "消失點構圖"+SVPscore)
                        .show();
            }else{
                new AlertDialog.Builder(context)
                        .setTitle("分數")
                        .setMessage("消失點構圖"+SVPscore)
                        .show();
            }



            selectRecommend(highestComposition,highest,bitmap,img,context);
        }else if(ib){
            IBscore = i.getIBscore(bitmap);
            SIBscore = changeType(IBscore);
            new AlertDialog.Builder(context)
                    .setTitle("分數")
                    .setMessage("強度平衡"+SIBscore)
                    .show();
        }



    }

    public void selectRecommend(int a,double score,Bitmap bitmap1,Mat img1,Context context){

        final horizontal h = new horizontal();
        final RoThird r = new RoThird();
        final vanishpoint v = new vanishpoint();

        if(score>=95){
            checkRecommend = 0;
            new AlertDialog.Builder(context)
                    .setTitle("恭喜!")
                    .setMessage("這是一張符合構圖的照片")
                    .show();
        }else{

            if(a == 1){
                if(checkRecommend == 0){
                    r.rotMain(bitmap1);
                    recommendImg = r.recommend(bitmap1); //三分構圖
                }
            }else if(a == 2){
                if(checkRecommend == 0){
                    v.vanishpoint(img1);
                    Mat z = new Mat();
                    z = v.draw_rec(img1);
                    Utils.matToBitmap(z, bitmap1);
                    recommendImg = bitmap1;
                }

            }else if(a == 3){

                if(checkRecommend == 0){
                    h.horizontal_composition(bitmap1);
                    recommendImg = h.recommend(bitmap1); //水平構圖
                }
            }
            checkRecommend = 1;
        }
    }
}
