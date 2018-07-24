package com.ranita.babyhelper;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

import io.github.lizhangqu.coreprogress.ProgressHelper;
import io.github.lizhangqu.coreprogress.ProgressUIListener;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = MainActivity.class.getName();
    private static String UPLOAD_URL;
    private Context mContext;

    private Button btnChoose;
    private Button btnUpload;
    private EditText ipEditText;
    private EditText userEditText;
    private NavigationView nvView;
    private ImageView mDrawImg;
    private DrawerLayout mDrawerLayout;
    private DragRectView mDragRectView;
    private RelativeLayout mDrawAreaLayout;
    private ProgressBar mUploadProgressBar;
    private TextView mUpload_info;

    private Uri mFilePath;
    AndroidFrameConverter mAndroidConverter = new AndroidFrameConverter();

    private double mW_RealRatio = 0;
    private double mH_RealRatio = 0;
    private double mImgViewWidth = 0;
    private double mImgViewHeight = 0;
    private int mVideoWidth = 0;
    private int mVideoHeight = 0;

    private static final MediaType MEDIA_TYPE_MP4 = MediaType.parse("application/octet-stream");
    public static final MediaType MEDIA_TYPE_TXT
            = MediaType.parse("text/x-markdown; charset=utf-8");
    private OkHttpClient mOkHttpClient;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();
        requestStoragePermission();
        initOkHttpClient();

        btnChoose = (Button) findViewById(R.id.btnChoose);
        btnUpload = (Button) findViewById(R.id.btnUpload);

        ipEditText = (EditText) findViewById(R.id.ipEditText);
        userEditText = (EditText) findViewById(R.id.userEditText);

        nvView = (NavigationView) findViewById(R.id.nav_view);
        nvView.setItemIconTintList(null);

        mDrawImg = (ImageView) findViewById(R.id.drawImg);
        mDrawAreaLayout = (RelativeLayout) findViewById(R.id.drawTopView);

        mUploadProgressBar = (ProgressBar) findViewById(R.id.upload_progress);
        mUploadProgressBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.progress_color)));
        mUpload_info = (TextView) findViewById(R.id.upload_info);

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectVideo();
            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVideo();
            }
        });

        //Nav
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Draw
        mDragRectView = (DragRectView) findViewById(R.id.dragRect);

        if (null != mDragRectView) {
            mDragRectView.setOnUpCallback(new DragRectView.OnUpCallback() {
                @Override
                public void onRectFinished(final Rect rect) {
//                    if(!Constants.getBoolSharedPref(Constants.RGB_SET_ALL, mContext)) {
//                        Toast.makeText(mContext, "Rect is (" + rect.left + ", " + rect.top + ", " + rect.right + ", " + rect.bottom + ")",
//                                Toast.LENGTH_SHORT).show();
//                    }
                    if (Constants.getPosition(mContext, "G").left() == -1) {
                        Log.i("Main", "set G");
                        Constants.setPosition(mContext, "G", rect.left, rect.top, rect.right, rect.bottom);
                    } else if (Constants.getPosition(mContext, "R").left() == -1) {
                        Log.i("Main", "set R");
                        Constants.setPosition(mContext, "R", rect.left, rect.top, rect.right, rect.bottom);
                    } else if (Constants.getPosition(mContext, "B").left() == -1) {
                        Log.i("Main", "set B");
                        Constants.setPosition(mContext, "B", rect.left, rect.top, rect.right, rect.bottom);
                        Constants.setBoolSharedPref(Constants.RGB_SET_ALL, true, mContext);
                    }
                }
            });
        }

        // Example of a call to a native method
        // TextView tv = (TextView) findViewById(R.id.sample_text);
        // tv.setText(stringFromJNI());
    }

    private void initOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS);
        mOkHttpClient = builder.build();
    }

    // Nav
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void displaySettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_settings, null);
        builder.setView(view);
        final EditText user_edittext = (EditText) view.findViewById(R.id.userEditText);
        final EditText ip_edittext = (EditText) view.findViewById(R.id.ipEditText);
        user_edittext.setText(Constants.getSharedPref(Constants.INPUT_USER, mContext));
        ip_edittext.setText(Constants.getSharedPref(Constants.INPUT_IP, mContext));
        builder.setTitle(getResources().getString(R.string.input_info));
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                String user_val = user_edittext.getText().toString();
                String ip_val = ip_edittext.getText().toString();
                Constants.setSharedPref(Constants.INPUT_USER, user_val, mContext);
                Constants.setSharedPref(Constants.INPUT_IP, ip_val, mContext);
            }
        })
                .setNegativeButton(R.string.cancel, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            displaySettingsDialog();
        } else if (id == R.id.nav_about) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    // End Nav
    // remove trimStart
    public String trimStart(String oriStr, String removeStr) {
        String result = oriStr;
        if (oriStr.indexOf(removeStr) == 0) {
            result = oriStr.substring(removeStr.length());
        }
        return result;
    }

    private void selectVideo() {
        Intent selectIntent = new Intent();
        selectIntent.setType(Constants.FILE_TYPE);
        selectIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(selectIntent, getResources().getString(R.string.please_select_mp4)), Constants.REQ_CHOOSE_VIDEO);
    }

    public void sendVideo() {
        String name = Constants.getSharedPref(Constants.INPUT_USER, mContext).trim();
        String ip = Constants.getSharedPref(Constants.INPUT_IP, mContext).trim();
        UPLOAD_URL = "http://" + ip + "/dashboard/php/connect.php";
        String info_path = createVideoInfoFile("video_info.txt", getRealPositionRGB());

        if (mFilePath == null) {
            Toast.makeText(this, R.string.video_file_not_found, Toast.LENGTH_LONG).show();
            return;
        }

        String path = trimStart(mFilePath.getPath(), "/file");

        Log.i(TAG ,"name: " + name);
        //getting the actual path of the image
        Log.i(TAG ,"filePath: " + mFilePath);
        Log.i(TAG ,"URI path: " + path);
        Log.i(TAG ,"info_path: " + info_path);

        if ((ip == null) || (ip == "")) {
            Toast.makeText(this, R.string.ip_cannot_be_empty, Toast.LENGTH_LONG).show();
            return;
        }

        if ((name == null) || (name == "")) {
            Toast.makeText(this, R.string.user_cannot_be_empty, Toast.LENGTH_LONG).show();
            return;
        }

        if ((path == null) || (path == "")) {
            Toast.makeText(this, R.string.video_file_not_found, Toast.LENGTH_LONG).show();
            return;
        }

        if ((info_path == null) || (info_path == "")) {
            Toast.makeText(this, R.string.info_file_not_found, Toast.LENGTH_LONG).show();
            return;
        }

        try {
            String uploadId = UUID.randomUUID().toString();
            Request.Builder request = new Request.Builder();
            request.header("Authorization", "Client-ID " + uploadId);
            request.url(UPLOAD_URL);

            MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder();
            requestBodyBuilder.setType(MultipartBody.FORM);
            requestBodyBuilder.addFormDataPart("video", path,
                            RequestBody.create(MEDIA_TYPE_MP4, new File(path)));
            requestBodyBuilder.addFormDataPart("txt", info_path,
                            RequestBody.create(MEDIA_TYPE_TXT, new File(info_path)));
            requestBodyBuilder.addFormDataPart("upload_user_name", name);
            MultipartBody build = requestBodyBuilder.build();

            RequestBody requestBody = ProgressHelper.withProgress(build, new ProgressUIListener() {

                //if you don't need this method, don't override this methd. It isn't an abstract method, just an empty method.
                @Override
                public void onUIProgressStart(long totalBytes) {
                    super.onUIProgressStart(totalBytes);
                    Log.e("TAG", "onUIProgressStart:" + totalBytes);
                    Toast.makeText(getApplicationContext(), "开始上传：" + totalBytes, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onUIProgressChanged(long numBytes, long totalBytes, float percent, float speed) {
                    Log.e("TAG", "============= start ===============");
                    Log.e("TAG", "numBytes: " + numBytes + " bytes");
                    Log.e("TAG", "totalBytes: " + totalBytes + " bytes");
                    Log.e("TAG", "percent: " + percent + " %");
                    Log.e("TAG", "speed: " + speed * 1000 / 1024 / 1024 + "  MB/second");
                    Log.e("TAG", "============= end ===============");
                    mUploadProgressBar.setProgress((int) (100 * percent));
                    mUpload_info.setText(percent * 100 + " %");
                }

                //if you don't need this method, don't override this methd. It isn't an abstract method, just an empty method.
                @Override
                public void onUIProgressFinish() {
                    super.onUIProgressFinish();
                    Log.e("TAG", "onUIProgressFinish:");
                    Toast.makeText(getApplicationContext(), "结束上传", Toast.LENGTH_SHORT).show();
                }
            });
            request.post(requestBody);
            Call call = mOkHttpClient.newCall(request.build());

            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.i(TAG, "sendVideo onFailure: " +e.toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.i(TAG, "sendVideo: " + response.body().string());
                }
            });
        } catch (Exception ex) {
            Toast.makeText(this, ex.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private String createVideoInfoFile(String filename, String content) {
        try {
            File storedDir = new File(Environment.getExternalStorageDirectory(), "BabyHelperTxt");
            if (!storedDir.exists()) {
                storedDir.mkdirs();
            }
            File txtFile = new File(storedDir, filename);
            if (txtFile.exists()) {
                txtFile.delete();
            }
            FileWriter writer = new FileWriter(txtFile);
            writer.append(content);
            writer.flush();
            writer.close();
            Log.d(TAG, "createVideoInfoFile saved.");
            return txtFile.getAbsolutePath();
        } catch (IOException e) {
            Log.d(TAG, "createVideoInfoFile exception: " + e.toString());
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQ_CHOOSE_VIDEO && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mFilePath = data.getData();
            getFrame();
        }
    }

    private int getEdgeInPx(int p1, int p2) {
        return Math.abs(p2 - p1);
    }

    private int toRealWScale(int w_val) {
        Log.d(TAG, "Img Width:" + mImgViewWidth + "w_val: " + w_val + ", mW_RealRatio: " + mW_RealRatio + "\n");
        return (int) (w_val * mW_RealRatio);
    }

    private int toRealHScale(int h_val) {
        Log.d(TAG, "Img height:" + mImgViewHeight + "h_val: " + h_val + ", mH_RealRatio: " + mH_RealRatio + "\n");
        return (int) (h_val * mH_RealRatio);
    }

    private String getColorPositionStr(String color) {
        String result =
            toRealWScale(Constants.getPosition(mContext, color).left()) + ","
            + toRealHScale(Constants.getPosition(mContext, color).top()) + ","
            + toRealWScale(getEdgeInPx(Constants.getPosition(mContext, color).right(), Constants.getPosition(mContext, color).left())) + ","
            + toRealHScale(getEdgeInPx(Constants.getPosition(mContext, color).top(), Constants.getPosition(mContext, color).bottom())) + "\n";
        return result;
    }


    private String getRealPositionRGB() { // Return X, Y, W, H
        // Here left, top, right, bottom already in pixels.
        String result =
                getColorPositionStr("G") + getColorPositionStr("R") + getColorPositionStr("B");
        Log.d(TAG, "getRealPositionRGB: \n" + result);
        return result;
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Log.i(TAG, "User not allow storage permission");
            finish();
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constants.REQ_STORAGE_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constants.REQ_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.chk_storage_failed, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void fetchFrame(String videofile) throws Exception {
        // long startTime = System.currentTimeMillis();
        FFmpegFrameGrabber fGrabber = new FFmpegFrameGrabber(videofile);
        fGrabber.start();
        int length = fGrabber.getLengthInFrames();
        Log.i(TAG, "frame length: " + length);
        int i = 0;
        int remove_frame_count = 30;
        Frame detected_frame = null;
        while (i < length) {
            detected_frame = fGrabber.grabFrame();
            // long currentTime = System.currentTimeMillis();

            if ((i > remove_frame_count) && (detected_frame.image != null)) {
            // Log.i(TAG, "startTime: " + startTime + ", currentTime: " + currentTime);
            // if ((currentTime - startTime) > remove_frame_count) {
                break;
            }

            if (detected_frame.image != null) {
                i++;
                Log.i(TAG, "frame i: " + i);
            }
        }

        // ex. 960*540 video
        Log.i(TAG, "Frame image height: " + detected_frame.imageHeight + ", width: " + detected_frame.imageWidth);
        mVideoWidth = detected_frame.imageWidth;
        mVideoHeight = detected_frame.imageHeight;
        Bitmap originalBitmap = mAndroidConverter.convert(detected_frame);
        originalBitmap.setWidth(mVideoWidth);
        originalBitmap.setHeight(mVideoHeight);
        mDrawImg.setImageDrawable(new BitmapDrawable(getResources(), originalBitmap));

        ViewGroup.LayoutParams drawViewParams=mDrawAreaLayout.getLayoutParams();
        drawViewParams.width = mVideoWidth;
        drawViewParams.height = mVideoHeight;
        mDrawAreaLayout.setLayoutParams(drawViewParams);

        ViewGroup.LayoutParams drawAreaParams=mDragRectView.getLayoutParams();
        drawAreaParams.width = mVideoWidth;
        drawAreaParams.height = mVideoHeight;
        mDragRectView.setLayoutParams(drawAreaParams);
        fGrabber.stop();

        ViewTreeObserver vto = mDrawImg.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                mDrawImg.getViewTreeObserver().removeOnPreDrawListener(this);
                mImgViewWidth = mDrawImg.getMeasuredWidth();
                mImgViewHeight = mDrawImg.getMeasuredHeight();
                Log.i(TAG, "Height: " + mImgViewHeight + " Width: " + mImgViewWidth);
                mW_RealRatio = mVideoWidth / mImgViewWidth;
                mH_RealRatio = mVideoHeight / mImgViewHeight;
                return true;
            }
        });
    }

    private void getFrame() {
        try {
            Constants.resetAllPotition(mContext);
            Constants.setBoolSharedPref(Constants.RGB_SET_ALL, false, mContext);
            String path = trimStart(mFilePath.getPath(), "/file");
            Log.i(TAG ,"filePath: " + mFilePath);
            fetchFrame(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    // public native String stringFromJNI();
}