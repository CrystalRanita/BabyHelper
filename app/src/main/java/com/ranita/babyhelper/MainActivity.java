package com.ranita.babyhelper;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = MainActivity.class.getName();
    public static String UPLOAD_URL;

    private Button btnChoose;
    private Button btnUpload;
    private EditText ipEditText;
    private EditText userEditText;
    private NavigationView nvView;

    private Uri filePath;
    private String infoFilePath;
    private UploadFileReceiver mUploadReceiver = null;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUploadReceiver = new UploadFileReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.UPLOAD_FILE_RECEIVER_NAME);
        this.registerReceiver(mUploadReceiver, filter);

        requestStoragePermission();
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID; // must set namespace before using UploadService

        btnChoose = (Button) findViewById(R.id.btnChoose);
        btnUpload = (Button) findViewById(R.id.btnUpload);

        ipEditText = (EditText) findViewById(R.id.ipEditText);
        userEditText = (EditText) findViewById(R.id.userEditText);

        nvView = (NavigationView) findViewById(R.id.nav_view);
        nvView.setItemIconTintList(null);
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Example of a call to a native method
        // TextView tv = (TextView) findViewById(R.id.sample_text);
        // tv.setText(stringFromJNI());
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
        user_edittext.setText(Constants.getSharedPref(Constants.INPUT_USER, getApplicationContext()));
        ip_edittext.setText(Constants.getSharedPref(Constants.INPUT_IP, getApplicationContext()));
        builder.setTitle(getResources().getString(R.string.input_info));
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                String user_val = user_edittext.getText().toString();
                String ip_val = ip_edittext.getText().toString();
                Constants.setSharedPref(Constants.INPUT_USER, user_val, getApplicationContext());
                Constants.setSharedPref(Constants.INPUT_IP, ip_val, getApplicationContext());
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
        String name = Constants.getSharedPref(Constants.INPUT_USER, getApplicationContext()).trim();
        String ip = Constants.getSharedPref(Constants.INPUT_IP, getApplicationContext()).trim();
        UPLOAD_URL = "http://" + ip + "/dashboard/php/connect.php";
        String info_path = infoFilePath;

        if (filePath == null) {
            Toast.makeText(this, R.string.video_file_not_found, Toast.LENGTH_LONG).show();
            return;
        }

        String path = trimStart(filePath.getPath(), "/file");

        Log.i(TAG ,"name: " + name);
        //getting the actual path of the image
        Log.i(TAG ,"filePath: " + filePath);
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
            Log.i(TAG ,uploadId + uploadId);
            //Creating a multi part request
            new MultipartUploadRequest(this, uploadId, UPLOAD_URL)
                    .addFileToUpload(path, "video") //Adding video file
                    .addFileToUpload(info_path, "txt") //Adding txt file
                    .addParameter("upload_user_name", name) //Adding text
                    .setNotificationConfig(new UploadNotificationConfig())
                    .setMaxRetries(2)
                    .startUpload();
        } catch (Exception ex) {
            Toast.makeText(this, ex.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public String createVideoInfoFile(String filename, String content) {
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
            filePath = data.getData();
            infoFilePath = createVideoInfoFile("video_info.txt", "test");
        }
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

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    // public native String stringFromJNI();
}
