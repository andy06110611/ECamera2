package com.example.ecamera2;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.BlackLevelPattern;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.Arrays;
import java.util.Timer;

/**************************************
 editor:品祥
 修改資訊:方形等等，以及mask部分與surfaceview相關
 drawInpreview()等函式
 基本上只有在按一次按鈕後會顯示黑色框框
**************************************/
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /*********************分數變數*******************/
    private double horizonScore = 0.0;
    private double rotScore = 0.0;
    public static boolean recommandExist  = false;
    /*********************分數變數*******************/

    /*********************點*******************/
    private double[] horizonPoint;
    private Point RotPoint = new Point();
    private Point RotcheckPoint = new Point();
    /*********************點*******************/

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
    private SurfaceView rectSurfaceview;
    private SurfaceHolder rectSurfaceHolder;

    private ImageView iv_show;      //顯示已拍好的照片
    private CameraManager mCameraManager;     //攝像頭管理器
    private Handler childHandler, mainHandler;
    private String mCameraID;       //攝像Id 0 為後  1 為前
    private ImageReader mImageReader;
    private CameraCaptureSession mCameraCaptureSession;
    private CameraDevice mCameraDevice;
    private Button b_re;    //回拍照畫面按鈕

    private int counter = 0;

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
    /*********************方形(canvas)*******************/
    Bitmap rectBitmap ;//= Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888);
    Canvas rectCanvas ;//= new Canvas(rectBitmap);  // 畫布;
    Paint rectPaint ;//= new Paint();
    /*********************方形(canvas)*******************/

    private String CV_TAG = "OpenCV";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DisplayMetrics monitorSize = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(monitorSize);
        getsize(monitorSize);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  //強制豎屏
        iniLoadOpenCV();
        initVIew();
    }

    private void iniLoadOpenCV() {
        boolean success = OpenCVLoader.initDebug();
        if (success) {
            Log.i(CV_TAG, "OpenCV Libraries loaded...");
        } else {
            Toast.makeText(this.getApplicationContext(), "WARNING: Could not load OpenCV Libraries!", Toast.LENGTH_LONG).show();
        }
    }

    private void initVIew() {

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
        p.setAntiAlias(true);                      // bSurfaceView設置白色設置畫筆的鋸齒效果。 true是去除。
        p.setColor(Color.WHITE);                 // bSurfaceView設置白色
        p.setStyle(Paint.Style.STROKE);
        p1.setAntiAlias(true);
        p1.setColor(Color.GRAY);                 // iSurfaceView設置灰色

        mSurfaceHolder.setKeepScreenOn(true);    // mSurfaceView添加回调
        bSurfaceHolder.setKeepScreenOn(true);   // bSurfaceView添加回调
        iSurfaceHolder.setKeepScreenOn(true);    // iSurfaceView添加回调

        bSurfaceView.setZOrderMediaOverlay(true);
        bSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        drawInPreview();
        bSurfaceHolder.addCallback(new SurfaceHolder.Callback() {


            @Override
            public void surfaceCreated(SurfaceHolder holder) { //SurfaceView創建

                canvas = holder.lockCanvas();
                // 1.鎖住畫布
                canvas.drawCircle(bSurfaceView.getWidth() / 2, bSurfaceView.getHeight() / 2, 100f, p);
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
                canvas1.drawRect(0, 210, 210, 0, p1);
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
        mImageReader = ImageReader.newInstance(1080, 1920, ImageFormat.JPEG, 1);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() { //可以在这里处理拍照得到的临时照片 例如，写入本地
            @Override
            public void onImageAvailable(ImageReader reader) {
                mCameraDevice.close();
                mSurfaceView.setVisibility(View.GONE);
                rectSurfaceview.setVisibility(View.GONE);
                bSurfaceView.setVisibility(View.GONE);
                iSurfaceView.setVisibility(View.GONE);
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

                try {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                    Bitmap bMapRotate = null;
                    Configuration config = getResources().getConfiguration();

                    if (config.orientation == 1) {

                        Matrix matrix = new Matrix();

                        matrix.reset();

//                        matrix.postRotate(90);

                        bMapRotate = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),    //轉90度過後的

                                matrix, true);

                    }
                    /*********************轉90度*******************/

                    /**********************判斷構圖分數********************/

//                  do horizontal composition
//                    horizontal Horizontal_position = new horizontal();
//                    horizonScore = Horizontal_position.horizontal_composition(bMapRotate);

//                  do Rule of Third
                    RoThird Rot1 = new RoThird();
                    rotScore = Rot1.rotMain(bMapRotate);
                    RotPoint = Rot1.Center;
                    recommandExist = true;
                    /*********************存照片*******************/

                    String root = Environment.getExternalStorageDirectory().toString();
                    //File myDir = new File(root ,System.currentTimeMillis()+".jpg");
                    byte[] bytes1 = bundle.getByteArray("bitmapByteArray");
                    long n = System.currentTimeMillis();
                    File file = new File(root + "/" + n + ".jpg");
                    OutputStream os = new FileOutputStream(file);
                    os.write(bytes1);
                    os.close();
                    /*********************存照片*******************/

                    /*********************畫推薦畫面***********************/
                    //水平的畫圖推薦
//                    bMapRotate = Horizontal_position.recommend(bMapRotate);
                    //三分構圖法的畫圖推薦
                    bMapRotate = Rot1.recommend(bMapRotate);
                    //水平的畫圖推薦
//                    Horizontal_position.calVertex();
//                    Imgproc.polylines(temMat, Horizontal_position.vertex, true, new Scalar(0, 255, 255), 10);
//                    Imgproc.line(temMat, new Point(horizonPoint[0], horizonPoint[1]), new Point(horizonPoint[2], horizonPoint[3]), new Scalar(255, 255, 255), 10);
//                    Imgproc.rectangle(temMat, new Point(horizonPoint[0], horizonPoint[1]), new Point(horizonPoint[0] + (temMat.width()/8), horizonPoint[1] + temMat.height()/8), new Scalar(0, 0, 255), 10);

                    //三分構圖法的畫圖推薦
//                    Imgproc.rectangle(temMat, new Point(Rot1.Center.x + (temMat.width()/ 16), Rot1.Center.y + (temMat.width()/ 16)), new Point(Rot1.Center.x - (temMat.width()/ 16), Rot1.Center.y - (temMat.width()/ 16)), new Scalar(0, 255, 255), 10);
//                    Imgproc.rectangle(temMat, new Point(Rot1.checkPoint.x + (temMat.width()/ 16), Rot1.checkPoint.y + (temMat.width()/ 16)), new Point(Rot1.checkPoint.x - (temMat.width()/ 16), Rot1.checkPoint.y - (temMat.width()/ 16)), new Scalar(0, 0, 255), 10);
//                    Utils.matToBitmap(temMat, bMapRotate);


                    /*********************畫推薦畫面***********************/

                    /*****************畫面變更*******************/
                    iv_show.setImageBitmap(bMapRotate);
                    showDetail();
                    /*****************畫面變更*******************/

                    /**************廣播****************/
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri uri = Uri.fromFile(file);
                    intent.setData(uri);
                    Context context = getApplicationContext();
                    context.sendBroadcast(intent);
                    /**************廣播****************/

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    image.close();
                }
                image.close();
            }

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
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.surfaceView_button:
                takePicture();    //拍照
                break;
            case R.id.surfaceView_img:
                onPick();     //開啟相簿
                break;
        }
    }

    public void re_btn(View v) {
        mSurfaceView.setVisibility(View.VISIBLE);
        bSurfaceView.setVisibility(View.VISIBLE);
        iSurfaceView.setVisibility(View.VISIBLE);
//        if(recommandExist == false){
//            recommandExist = true;
//        }
        rectSurfaceview.setVisibility(View.VISIBLE);

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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case 101:
                    imgUri = data.getData();
                    break;
            }

            getImg();

        } else {
            Log.d(TAG, "Did not take a picture");
        }
    }

    void getImg() {
        BitmapFactory.Options option = new BitmapFactory.Options();
        Bitmap bmp = null;
        Mat mat = new Mat();

        try {
            bmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(imgUri), null, option/*null*/);
        } catch (IOException e) {
            Log.d(TAG, "霈????潛??航炊");
            return;
        }

        Bitmap bmp32 = bmp.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, mat);
        displayImage(mat);
    }

    private void displayImage(Mat image) {

        mSurfaceView.setVisibility(View.GONE);
        bSurfaceView.setVisibility(View.GONE);
        iSurfaceView.setVisibility(View.GONE);
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
        if (config.orientation == 1) {

            Matrix matrix = new Matrix();

            matrix.reset();

//            matrix.postRotate(90);

            bMapRotate = Bitmap.createBitmap(bitmap1, 0, 0, bitmap1.getWidth(), bitmap1.getHeight(),

                    matrix, true);

        }

        imv.setImageBitmap(bMapRotate);
    }

    private void showDetail() {
        new AlertDialog.Builder(this)
                .setTitle("Score of your photo")
                .setMessage("Horizontal Composition: " + horizonScore + "\n" + "Rule of Third :" + rotScore)
                .show();
    }

    private void drawInPreview(){
        rectCanvas = new Canvas(rectBitmap);  // 畫布
        rectPaint = new Paint();
//        System.out.println("23");
        rectSurfaceview = (SurfaceView) findViewById(R.id.rectSurfaceView);
        rectSurfaceHolder = rectSurfaceview.getHolder();
        rectSurfaceHolder.setKeepScreenOn(true);
        rectSurfaceview.setZOrderMediaOverlay(true);
        rectSurfaceview.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        System.out.println("before created");
        rectSurfaceHolder.addCallback(new rectSuf(getApplicationContext()){
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if(RotPoint.x != 0)
                {
                    rectCanvas = holder.lockCanvas();
                    // 1.鎖住畫布
                    rectCanvas.drawRect((float) (RotPoint.x + (rectBitmap.getWidth() / 16)), (float) (RotPoint.y + (rectBitmap.getHeight() / 16)), (float) (RotPoint.x - (rectBitmap.getWidth() / 16)), (float) (RotPoint.y - (rectBitmap.getHeight() / 16)), rectPaint);
                    // 2.在畫布上貼圖
                    holder.unlockCanvasAndPost(rectCanvas);
                    // 3.解鎖並PO出畫布
                    System.out.println(counter);
                }
//                if(recommandExist == true){
////                    Handler handler = new Handler();
////                    handler.postDelayed(new Runnable(){
////                        @Override
////                        public void run() {
////                        }}, 100);
//                }
            }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });
    }

    private void getsize(DisplayMetrics monitorSize){
        /*********************方形(canvas)*******************/
        rectBitmap = Bitmap.createBitmap(monitorSize.widthPixels, monitorSize.heightPixels, Bitmap.Config.ARGB_8888);
    }


}

class rectSuf extends  SurfaceView implements SurfaceHolder.Callback, Runnable{

    private SurfaceHolder holder;
    MyThread myThread;
    Paint mypaint;



    public rectSuf(Context context) {
        super(context);

        holder = getHolder();
        holder.addCallback(this);

        myThread = new MyThread();
        mypaint = new Paint();
        mypaint.setColor(Color.RED);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        myThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public void run() {

    }

    class MyThread extends Thread{
        @Override
        public void run(){
            while(MainActivity.recommandExist){
                try{
                    draw();
                }catch (Exception e){

                }
            }
        }

        private void draw(){
            Canvas canvas = holder.lockCanvas();
            canvas.drawRect(0, 0, 100, 100, mypaint);
            holder.unlockCanvasAndPost(canvas);
        }
    }
}