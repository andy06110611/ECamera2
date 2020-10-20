package com.example.ecamera2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Pager2 extends RelativeLayout {
    public Pager2(Context context) {
        super(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.my_pager2, null);//連接頁面
        TextView textView = view.findViewById(R.id.textView3);//取得頁面元件
        textView.setText("第33頁");

        addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        //將元件放入ViewPager
    }
}
