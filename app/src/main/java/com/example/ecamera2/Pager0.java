package com.example.ecamera2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;


public class Pager0 extends RelativeLayout {
    public Pager0(Context context) {//pageNumber是由ＭainActivity.java那邊傳入頁碼

        super(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.my_pager0, null);//連接頁面


        addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        //將元件放入ViewPager
    }

}
