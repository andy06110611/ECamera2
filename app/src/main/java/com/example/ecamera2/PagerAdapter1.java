package com.example.ecamera2;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.List;

public class PagerAdapter1 extends PagerAdapter implements ViewPager.OnPageChangeListener {
    private List<View> mPager;//管理分頁陣列
    private int childCount = 0;//取得現在分頁位置

    public PagerAdapter1() {

    }

    public void PagerAdapter(List<View> mPager){
        //請記得新增建構子喔！(ﾟAﾟ;)
        this.mPager = mPager;//分頁陣列要由MainActivity傳入}
    }

    @Override
    public int getItemPosition(@NonNull Object object) {//取得分頁位置
        if (childCount>0){
            childCount --;
            return POSITION_NONE;
        }
        return  super.getItemPosition(object);
    }

    @Override
    public int getCount() {
        return mPager.size();
    }//填入陣列長度
    /**再加入....↓*/
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        mPager.get(position).setTag(position);
        ((ViewPager) container).addView(mPager.get(position));
        return mPager.get(position);//跑回圈增加View
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return object == view;//不管他，直接照打
    }

    @Override
    public void notifyDataSetChanged() {
        childCount = getCount();//動態新增
        super.notifyDataSetChanged();
    }

    private int currentPage;


    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }



    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        //設置TabLayout上面的文字標籤內容
        if(position == 0){
            return "原始拍照";
        }
        else if(position == 1){
            return "構圖拍照";
        }
        else{
            return "人像拍照";
        }
    }
    /**再加入....↑*/

    int currentPosition = 0;
    @Override
    public void onPageSelected(int pItem) {
        // TODO Auto-generated method stub

        switch (pItem) {

            //如果是第一個頁面
            case 0:
                currentPosition = 0;
                break;
            //如果是第二個頁面
            case 1:
                currentPosition = 1;
                break;
            //如果是第三個頁面
            case 2:
                currentPosition = 3;
                break;
        }

    }
    public int currentPosition(){
        return currentPosition;
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
        // TODO Auto-generated method stub

    }
}
