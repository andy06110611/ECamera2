package com.example.ecamera2;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public int itemPosition = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  //強制豎屏
        iniLoadOpenCV();
        initVIew();

        /*********************模式切換*******************/
        ArrayList<View> mPages = new ArrayList<>();
        mPages.add(new Pager0(this));
        mPages.add(new Pager1(this));
        mPages.add(new Pager2(this));

        ViewPager viewPager = findViewById(R.id.mViewPager);
        TabLayout tab = findViewById(R.id.tab);
        PagerAdapter1 myPagerAdapter = new PagerAdapter1();
        myPagerAdapter.PagerAdapter(mPages);
        tab.setupWithViewPager(viewPager);//將TabLayout綁定給ViewPager
        viewPager.setAdapter(myPagerAdapter);//綁定適配器
        viewPager.setCurrentItem(1);//指定跳到某頁，一定得設置在setAdapter後面
        itemPosition = myPagerAdapter.currentPosition();   //未得到正確位置
        /*********************模式切換*******************/
    }


    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /*********************拍照畫面*******************/
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    /*********************拍照畫面*******************/

    /*********************拍照按鈕(canvas)*******************/
    private SurfaceView bSurfaceView;
    private SurfaceHolder bSurfaceHolder;
    /*********************拍照按鈕*******************/

    /*********************開啟相簿按鈕(canvas)*******************/
    private SurfaceView iSurfaceView;
    private SurfaceHolder iSurfaceHolder;
    /*********************開啟相簿按鈕*******************/

   // private ImageButton btn_selectMode;  //選擇構圖模式
    private ImageView iv_show;      //顯示已拍好的照片
    private CameraManager mCameraManager;     //攝像頭管理器
    private Handler childHandler, mainHandler;
    private String mCameraID;       //攝像Id 0 為後  1 為前
    private ImageReader mImageReader;
    private CameraCaptureSession mCameraCaptureSession;
    private CameraDevice mCameraDevice;
    private Button b_re;    //回拍照畫面按鈕

    /*********************相簿照片*******************/
    Uri imgUri;
    ImageView imv;   //相簿點選照片顯示
    private static String TAG = "MainActivity";
    /*********************相簿照片*********************/

    /*********************拍照畫布(canvas)*******************/
    Bitmap bitmap1 = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap1);  // 畫布
    Paint p = new Paint();
    /*********************拍照畫布(canvas)*******************/

    /*********************相簿畫布(canvas)*******************/
    Bitmap bitmap2 = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888);
    Canvas canvas1 = new Canvas(bitmap2);  // 畫布
    Paint p1 = new Paint();
    /*********************相簿畫布(canvas)*******************/

    private String CV_TAG = "OpenCV";
    private void iniLoadOpenCV() {
        boolean success = OpenCVLoader.initDebug();
        if (success) {
            Log.i(CV_TAG, "OpenCV Libraries loaded...");
        } else {
            Toast.makeText(this.getApplicationContext(), "WARNING: Could not load OpenCV Libraries!", Toast.LENGTH_LONG).show();
        }
    }

    private void initVIew() {
        //btn_selectMode = (ImageButton) findViewById(R.id.btn_selectMode);
        iv_show = (ImageView) findViewById(R.id.iv_show_camera2_activity);   //拍照完顯示
        b_re = (Button) findViewById(R.id.repreview);       //回拍照畫面按鈕
        imv = (ImageView) findViewById(R.id.imgView);         //相簿點選照片顯示

        mSurfaceView = (SurfaceView) findViewById(R.id.surface_view_camera2_activity);
        bSurfaceView = (SurfaceView) findViewById(R.id.surfaceView_button);
        iSurfaceView = (SurfaceView) findViewById(R.id.surfaceView_img);

        bSurfaceView.setOnClickListener(this);
        iSurfaceView.setOnClickListener(this);

        mSurfaceHolder = mSurfaceView.getHolder();// 取得容器
        bSurfaceHolder = bSurfaceView.getHolder();// 取得容器
        iSurfaceHolder = iSurfaceView.getHolder();// 取得容器
        p.setAntiAlias(true);					  // bSurfaceView設置白色設置畫筆的鋸齒效果。 true是去除。
        p.setColor(Color.WHITE);				 // bSurfaceView設置白色
        p1.setAntiAlias(true);
        p1.setColor(Color.GRAY);				 // iSurfaceView設置灰色
        mSurfaceHolder.setKeepScreenOn(true);    // mSurfaceView添加回调
        bSurfaceHolder.setKeepScreenOn(true);   // bSurfaceView添加回调
        iSurfaceHolder.setKeepScreenOn(true);    // iSurfaceView添加回调

        bSurfaceView.setZOrderMediaOverlay(true);
        bSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        bSurfaceHolder.addCallback(new SurfaceHolder.Callback() {


            @Override
            public void surfaceCreated(SurfaceHolder holder) { //SurfaceView創建

                canvas = holder.lockCanvas();
                // 1.鎖住畫布
                canvas.drawCircle(bSurfaceView.getWidth()/2, bSurfaceView.getHeight()/2, 100f, p);
                // 2.在畫布上貼圖
                holder.unlockCanvasAndPost(canvas);
                // 3.解鎖並PO出畫布
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) { //SurfaceView銷毀
                // 釋放Camera資源
                if (null != mCameraDevice) {
                    mCameraDevice.close();
                    MainActivity.this.mCameraDevice = null;
                }
            }
        });
        iSurfaceHolder.addCallback(new SurfaceHolder.Callback() {


            @Override
            public void surfaceCreated(SurfaceHolder holder) { //SurfaceView創建

                canvas1 = holder.lockCanvas();
                // 1.鎖住畫布
                canvas1.drawRect(0, 210, 210, 0,p1);
                // 2.在畫布上貼圖
                holder.unlockCanvasAndPost(canvas1);
                // 3.解鎖並PO出畫布
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) { //SurfaceView銷毁
                // 釋放Camera資源
                if (null != mCameraDevice) {
                    mCameraDevice.close();
                    MainActivity.this.mCameraDevice = null;
                }
            }
        });
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {


            @Override
            public void surfaceCreated(SurfaceHolder holder) { //SurfaceView創建

                initCamera2();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) { //SurfaceView銷毀
                // 釋放Camera資源
                if (null != mCameraDevice) {
                    mCameraDevice.close();
                    MainActivity.this.mCameraDevice = null;
                }
            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initCamera2() {
        HandlerThread handlerThread = new HandlerThread("Camera2");
        handlerThread.start();
        childHandler = new Handler(handlerThread.getLooper());
        mainHandler = new Handler(getMainLooper());
        mCameraID = "" + CameraCharacteristics.LENS_FACING_FRONT;//後攝像頭
        mImageReader = ImageReader.newInstance(1080, 1920, ImageFormat.JPEG,1);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() { //可以在这里处理拍照得到的临时照片 例如，写入本地
            @Override
            public void onImageAvailable(ImageReader reader) {
                mCameraDevice.close();
                mSurfaceView.setVisibility(View.GONE);
                bSurfaceView.setVisibility(View.GONE);
                iSurfaceView.setVisibility(View.GONE);
                //btn_selectMode.setVisibility(View.GONE);
                imv.setVisibility(View.GONE);
                b_re.setVisibility(View.VISIBLE);
                iv_show.setVisibility(View.VISIBLE);

                Image image = reader.acquireNextImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);//由缓冲區存入字節數组
                image.close();
                Bundle bundle = new Bundle();
                bundle.putByteArray("bitmapByteArray", bytes);
                Message msg = new Message();
                msg.setData(bundle);
                childHandler.sendMessage(msg);


                /*********************轉90度*******************/

                BitmapFactory.Options bfoOptions = new BitmapFactory.Options();
                bfoOptions.inScaled = false;
                try {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length,bfoOptions);

                    Bitmap bMapRotate = null;
                    Configuration config = getResources().getConfiguration();

                    if (config.orientation==1)

                    {

                        Matrix matrix = new Matrix();

                        matrix.reset();

                        matrix.postRotate(90);

                        bMapRotate = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),    //轉90度過後的

                                matrix, true);
                    }
                    /*********************轉90度*******************/

                    /*********************存照片and判斷構圖分數*******************/

                    String root = Environment.getExternalStorageDirectory().toString();
                    //File myDir = new File(root ,System.currentTimeMillis()+".jpg");
                    byte[] bytes1 = bundle.getByteArray("bitmapByteArray");
                    long n = System.currentTimeMillis();
                    File file = new File(root+ "/"+n+".jpg");
                    OutputStream os = new FileOutputStream(file);
                    os.write(bytes1);
                    os.close();
                    /*********************存照片and判斷構圖分數*******************/
                    Mat m = new Mat();
                    iv_show.setImageBitmap(bMapRotate);
                    Utils.bitmapToMat(bMapRotate,m,true);

                    if(itemPosition == 0){
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("YO")
                                .setMessage("1111")
                                .show();
                        Pager1 i = new Pager1(MainActivity.this);
                        i.selectMode(bMapRotate,m,selection,MainActivity.this);
                        //selectMode(bMapRotate,m);
                    }
                    /**************廣播****************/
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri uri = Uri.fromFile(file);
                    intent.setData(uri);
                    Context context = getApplicationContext();
                    context.sendBroadcast(intent);
                    /**************廣播****************/

                }catch (IOException e) { e.printStackTrace(); } finally { image.close(); } image.close(); }

            //if(!myDir.exists()) myDir.mkdirs();
            //Mat src = new Mat();

            //Utils.bitmapToMat(bitmap, src);
            //Imgcodecs.imwrite(root+"/saved_img/"+System.currentTimeMillis()+".jpg", src);



        }, mainHandler);



        //獲取摄像頭管理
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //打開摄像頭
            mCameraManager.openCamera(mCameraID, stateCallback, mainHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    /**
     * 摄像頭創建監聽
     */
    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {//打開攝像頭
            mCameraDevice = camera;
            //開啟預覽
            takePreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {//關閉摄像頭
            if (null != mCameraDevice) {
                mCameraDevice.close();
                MainActivity.this.mCameraDevice = null;
            }
        }

        @Override
        public void onError(CameraDevice camera, int error) {//發生錯誤
            Toast.makeText(MainActivity.this, "摄像頭開啟失敗", Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * 開始預覽
     */
    private void takePreview() {


        try {
            // 創建預覽需要的CaptureRequest.Builder
            final CaptureRequest.Builder previewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            // 將SurfaceView的surface作為CaptureRequest.Builder的目標
            previewRequestBuilder.addTarget(mSurfaceHolder.getSurface());
            // 創建CameraCaptureSession，該對象負責管理處理預覽請求和拍照請求
            mCameraDevice.createCaptureSession(Arrays.asList(mSurfaceHolder.getSurface(), mImageReader.getSurface()), new CameraCaptureSession.StateCallback() // ③
            {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    if (null == mCameraDevice) return;
                    // 當攝像頭已經準備好時，開始顯示預覽
                    mCameraCaptureSession = cameraCaptureSession;
                    try {
                        // 自動對焦
                        previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        // 打開閃光燈
                        previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                        // 顯示預覽
                        CaptureRequest previewRequest = previewRequestBuilder.build();
                        mCameraCaptureSession.setRepeatingRequest(previewRequest, null, childHandler);

                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(MainActivity.this, "配置失敗", Toast.LENGTH_SHORT).show();
                }
            }, childHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 點擊事件
     */

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.surfaceView_button:
                takePicture();    //拍照
                break;
            case R.id.surfaceView_img:
                onPick();     //開啟相簿
                break;

        }
    }
    public void re_btn(View v){
        mSurfaceView.setVisibility(View.VISIBLE);
        bSurfaceView.setVisibility(View.VISIBLE);
        iSurfaceView.setVisibility(View.VISIBLE);
        //btn_selectMode.setVisibility(View.VISIBLE);
        b_re.setVisibility(View.GONE);
        imv.setVisibility(View.GONE);
        iv_show.setVisibility(View.GONE);


    }
    /**
     * 拍照
     */
    private void takePicture() {
        if (mCameraDevice == null) return;
        // 創建拍照需要的CaptureRequest.Builder
        final CaptureRequest.Builder captureRequestBuilder;
        try {
            captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            // 將imageReader的surface作為CaptureRequest.Builder的目標
            captureRequestBuilder.addTarget(mImageReader.getSurface());
            // 自動對焦
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            // 自動曝光
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            // 獲取手機方向
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            // 根據設備方向計算設置照片的方向
            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            //拍照
            CaptureRequest mCaptureRequest = captureRequestBuilder.build();
            mCameraCaptureSession.capture(mCaptureRequest, null, childHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }



    public void onPick() {
        Intent it = new Intent(Intent.ACTION_GET_CONTENT);
        it.setType("image/*");
        startActivityForResult(it, 101);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK ){
            switch (requestCode){
                case 101:
                    imgUri = data.getData();
                    break;
            }

            getImg();

        }else{
            Log.d(TAG, "Did not take a picture");
        }
    }

    void getImg(){
        BitmapFactory.Options option = new BitmapFactory.Options();
        Bitmap bmp = null;
        Mat mat = new Mat();

        try{
            bmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(imgUri), null, option/*null*/);
        }catch(IOException e) {
            Log.d(TAG, "霈????潛??航炊");
            return;
        }

        Bitmap bmp32 = bmp.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, mat);
        displayImage(mat);
    }

    private void displayImage(Mat image){

        mSurfaceView.setVisibility(View.GONE);
        bSurfaceView.setVisibility(View.GONE);
        iSurfaceView.setVisibility(View.GONE);
        //btn_selectMode.setVisibility(View.GONE);
        iv_show.setVisibility(View.GONE);
        b_re.setVisibility(View.VISIBLE);
        imv.setVisibility(View.VISIBLE);

        //create a bitMap
        Bitmap bitmap1 = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.RGB_565);

        //convert to bitmap
        Utils.matToBitmap(image, bitmap1);
        Bitmap bMapRotate = null;
        Configuration config = getResources().getConfiguration();

        //轉90度
        if (config.orientation==1)

        {

            Matrix matrix = new Matrix();

            matrix.reset();

            matrix.postRotate(90);

            bMapRotate = Bitmap.createBitmap(bitmap1, 0, 0, bitmap1.getWidth(), bitmap1.getHeight(),

                    matrix, true);

        }

        imv.setImageBitmap(bMapRotate);
    }

    String[] items={"強度平衡", "水平構圖","三分構圖","消失點構圖"};
    boolean[] selection={false, false, false, false};
    public boolean[] select(View v){

        AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        dialog.setTitle("選擇構圖")

                .setMultiChoiceItems(items, selection, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {}
                })

                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                })
                .show();
        return selection;
    }
}
