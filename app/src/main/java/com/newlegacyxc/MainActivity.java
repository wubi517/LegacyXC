package com.newlegacyxc;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.gson.Gson;
import com.newlegacyxc.activity.LoginActivity;
import com.newlegacyxc.activity.SplashActivity;
import com.newlegacyxc.apps.Constants;
import com.newlegacyxc.apps.FirstServer;
import com.newlegacyxc.apps.MyApp;
import com.newlegacyxc.dialog.UpdateDlg;
import com.newlegacyxc.models.AppInfoModel;
import com.newlegacyxc.utils.Utils;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.newlegacyxc.apps.MyApp.num_server;

public class MainActivity extends Activity implements View.OnClickListener{
    SharedPreferences serveripdetails;
    String version,app_Url;
    VideoView videoView;
    LinearLayout main_lay;
    ImageButton icon1,icon2,icon3;//
    static {
        System.loadLibrary("notifications");
    }
    public native String getTrial();
    public native String getZero();
    public native String getOne();
    public native String getTwo();
    public native String getFive();
    public native String getSix();
    public native String getSeven();

    public void FullScreencall() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //for new api versions.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FullScreencall();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        if(MyApp.instance.getPreference().get(Constants.getPIN_CODE())==null){
            MyApp.instance.getPreference().put(Constants.getPIN_CODE(),"0000");
        }
        if(MyApp.instance.getPreference().get(Constants.OSD_TIME)==null){
            MyApp.instance.getPreference().put(Constants.OSD_TIME,10);
        }
        serveripdetails = this.getSharedPreferences("serveripdetails", Context.MODE_PRIVATE);

        MyApp.mac_address = Utils.getPhoneMac(this);
//        if (BuildConfig.DEBUG) MyApp.mac_address = "5c:f8:a1:b4:8e:12";
        MyApp.instance.getPreference().put(Constants.MAC_ADDRESS, MyApp.mac_address.toUpperCase());
//        MyApp.mac_address = (String) MyApp.instance.getPreference().get(Constants.MAC_ADDRESS);
        Log.e("mac_address",MyApp.mac_address);

        main_lay = findViewById(R.id.main_lay);
        main_lay.setVisibility(View.GONE);
        icon1 = findViewById(R.id.icon1);
        icon2 = findViewById(R.id.icon2);
        icon3 = findViewById(R.id.icon3);
        if (num_server!=1){
            icon1.setOnClickListener(this);
            icon2.setOnClickListener(this);
            icon3.setOnClickListener(this);

            Picasso.with(this).load(getFive())
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .error(R.drawable.icon)
                    .into(icon1);

            Picasso.with(this).load(getSix())
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .error(R.drawable.ad1)
                    .into(icon2);

            Picasso.with(this).load(getSeven())
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .error(R.drawable.ad2)
                    .into(icon3);
        }else {
            icon1.setVisibility(View.GONE);
            icon2.setVisibility(View.GONE);
            icon3.setVisibility(View.GONE);
        }


        videoView = findViewById(R.id.video_view);
//        videoView.setVisibility(View.GONE);
        main_lay.setVisibility(View.VISIBLE);

        if (MyApp.is_video_played) {
            videoView.setVisibility(View.GONE);
            if (num_server==1){
                main_lay.setVisibility(View.GONE);
                findViewById(R.id.image).setVisibility(View.VISIBLE);
            }else {
                main_lay.setVisibility(View.VISIBLE);
                findViewById(R.id.image).setVisibility(View.GONE);
            }
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                CheckSDK23Permission();
            }else if (num_server==1){
                new Thread(this::getRespond).start();
            }
        }else {
            String path = "android.resource://" + getPackageName() + "/" + R.raw.intro;
            videoView.setVideoURI(Uri.parse(path));
            videoView.start();
            videoView.setOnCompletionListener(mp -> {
                MyApp.is_video_played=true;
                videoView.setVisibility(View.GONE);
                main_lay.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                    CheckSDK23Permission();
                }else if (num_server==1){
                    new Thread(this::getRespond).start();
                }
            });

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) videoView.getLayoutParams();
            params.width =  metrics.widthPixels;
            params.height = metrics.heightPixels;
            params.leftMargin = 0;
            videoView.setLayoutParams(params);
        }
        try {
            SharedPreferences.Editor server_editor = serveripdetails.edit();
            byte[] decodeValue20 = Base64.decode(getZero(),Base64.DEFAULT);
            server_editor.putString("url1",new String (decodeValue20));
            Log.e("url1",new String (decodeValue20));
            byte[] decodeValue30 = Base64.decode(getOne(),Base64.DEFAULT);
            server_editor.putString("url2",new String (decodeValue30));
            Log.e("url2",new String (decodeValue30));
            byte[] decodeValue40 = Base64.decode(getTwo(),Base64.DEFAULT);
            server_editor.putString("url3",new String (decodeValue40));
            Log.e("url3",new String (decodeValue40));
            server_editor.apply();
        }catch (Exception e){
            Toast.makeText(MainActivity.this, "Server Error!", Toast.LENGTH_SHORT).show();
        }
    }

    private void getRespond() {
        try {
            long startTime = System.nanoTime();
            String responseBody = null;
            responseBody = MyApp.instance.getIptvclient().login(Constants.GetUrl1(this));

            long endTime = System.nanoTime();

            long MethodeDuration = (endTime - startTime);
            Log.e(getClass().getSimpleName(),responseBody);
            Log.e("BugCheck","authenticate success "+MethodeDuration);
            try{
                JSONObject jsonObject = new JSONObject(responseBody);
                AppInfoModel appInfoModel = new AppInfoModel();
                if(jsonObject.getString("status").toString().equalsIgnoreCase("success")){
                    appInfoModel.setSuccess(true);
                }else {
                    appInfoModel.setSuccess(false);
                }
                AppInfoModel.AppInfoEntity appInfo = new AppInfoModel.AppInfoEntity();
                appInfo.setPin2(jsonObject.getString("dual_screen"));
                appInfo.setPin3(jsonObject.getString("tri_screen"));
                appInfo.setPin4(jsonObject.getString("four_way_screen"));
                String slider_time = jsonObject.getString("slider_time");
                if(!slider_time.isEmpty()){
                    appInfo.setSliderTime(Integer.parseInt(slider_time));
                }else {
                    appInfo.setSliderTime(10);
                }

                JSONArray image_array = jsonObject.getJSONArray("images");
                if(image_array.length() > 1){
                    List<AppInfoModel.SliderEntity> sliderEntities = new ArrayList<>();
                    for(int i = 1;i<image_array.length();i++){
                        AppInfoModel.SliderEntity sliderEntity = new AppInfoModel.SliderEntity();
                        sliderEntity.setImageurl(image_array.get(i).toString());
                        sliderEntity.setBody("Welcome "+ getResources().getString(R.string.app_name));
                        sliderEntity.setHeader("Welcome "+ getResources().getString(R.string.app_name));
                        sliderEntities.add(sliderEntity);
                    }
                    appInfo.setSlider(sliderEntities);
                }
                appInfoModel.setAppInfo(appInfo);

                MyApp.instance.getPreference().put(Constants.getAppInfoModel(),appInfoModel);
                SharedPreferences.Editor server_editor = serveripdetails.edit();
                String dual_screen=appInfoModel.getAppInfo().getPin2();
                String tri_screen=appInfoModel.getAppInfo().getPin3();
                String four_way_screen=appInfoModel.getAppInfo().getPin4();
                server_editor.putString("dual_screen",dual_screen);
                server_editor.putString("tri_screen",tri_screen);
                server_editor.putString("four_way_screen",four_way_screen);
                server_editor.putString("i", appInfoModel.getAppInfo().getLogo());
                server_editor.putString("m", appInfoModel.getAppInfo().getImg_url());
                server_editor.putString("l", appInfoModel.getAppInfo().getImg_url());
                server_editor.putString("d1", appInfoModel.getAppInfo().getImg_url());
                server_editor.putString("d2", appInfoModel.getAppInfo().getImg_url());
                server_editor.putInt("slider_time",Integer.valueOf(appInfoModel.getAppInfo().getSliderTime()));
                server_editor.apply();
                version = appInfoModel.getAppInfo().getVersion();
                app_Url = appInfoModel.getAppInfo().getAppUrl();
                if(MyApp.instance.getPreference().get(Constants.getPIN_CODE())==null){
                    MyApp.instance.getPreference().put(Constants.getPIN_CODE(),"0000");
                }
                if(MyApp.instance.getPreference().get(Constants.getSORT())==null){
                    MyApp.instance.getPreference().put(Constants.getSORT(),0);
                }
                if(MyApp.instance.getPreference().get(Constants.getCurrentPlayer())==null){
                    MyApp.instance.getPreference().put(Constants.getCurrentPlayer(),0);
                }

                if (MyApp.instance.getPreference().get(Constants.getDIRECT_VPN_PIN_CODE())==null){
                    MyApp.instance.getPreference().put(Constants.getDIRECT_VPN_PIN_CODE(),"8888");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            getUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(()->Toast.makeText(MainActivity.this, "Network Error!", Toast.LENGTH_SHORT).show());
            finish();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void CheckSDK23Permission() {
        List<String> permissionsNeeded = new ArrayList<String>();
        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            permissionsNeeded.add("READ / WRITE SD CARD");
        if (!addPermission(permissionsList, Manifest.permission.READ_PHONE_STATE))
            permissionsNeeded.add("READPHONE");
        if (permissionsList.size() > 0) {
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    124);
        }else if (num_server==1){
            new Thread(this::getRespond).start();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean addPermission(List<String> permissionsList, String permission) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.e("PermissionsResult", "onRequestPermissionsResult");
        if (num_server==1){
            new Thread(this::getRespond).start();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void getUpdate(){
        MyApp.instance.versionCheck();
        double code = 0.0;
        try {
            code = Double.parseDouble(version);
        }catch (Exception e){
            code = 0.0;
        }
        MyApp.instance.loadVersion();
        double app_vs = Double.parseDouble(MyApp.version_name);
        if (code > app_vs && !BuildConfig.DEBUG) {//
            runOnUiThread(()->{
                UpdateDlg updateDlg = new UpdateDlg(this, new UpdateDlg.DialogUpdateListener() {
                    @Override
                    public void OnUpdateNowClick(Dialog dialog) {
                        dialog.dismiss();
//                        app_Url = "https://www.supaupload.com/Legacy.apk";
                        new versionUpdate().execute(app_Url);
                    }
                    @Override
                    public void OnUpdateSkipClick(Dialog dialog) {
                        dialog.dismiss();
                        startNextActivity();
                    }
                });
                updateDlg.show();
            });
        }else {
            startNextActivity();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.icon1:
                MyApp.firstServer = FirstServer.first;
                break;
            case R.id.icon2:
                MyApp.firstServer = FirstServer.second;
                break;
            case R.id.icon3:
                MyApp.firstServer = FirstServer.third;
                break;
        }
        new Thread(this::getRespond).start();
    }

    class versionUpdate extends AsyncTask<String, Integer, String> {
        ProgressDialog mProgressDialog;
        File file;

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setMessage(getResources().getString(R.string.request_download));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(values[0]);
        }

        @Override
        protected String doInBackground(String... params) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(params[0]);
                Log.e("url",params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(60000);
                connection.connect();
                Log.e("connect","success");
                int fileLength = connection.getContentLength();
                input = connection.getInputStream();
                String destination = Environment.getExternalStorageDirectory() + "/";
                String fileName = BuildConfig.APPLICATION_ID+".apk";
                destination += fileName;
                final Uri uri = Uri.parse("file://" + destination);
                file = new File(destination);
                if(file.exists()){
                    file.delete();
                }
                output = new FileOutputStream(file, false);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    if (fileLength > 0)
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }
                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            mProgressDialog.dismiss();
            if (result != null) {
                Toast.makeText(getApplicationContext(),"Update Failed",Toast.LENGTH_LONG).show();
                startNextActivity();
            } else
                startInstall(file);
            Log.e("result", result);
        }
    }

    private void startNextActivity() {
        runOnUiThread(()->{
            switch (MyApp.firstServer){
                case first:
                case second:
                case third:
                if (MyApp.instance.getPreference().get(Constants.getLoginInfo())!=null)
                    startActivity(new Intent(MainActivity.this, SplashActivity.class));
                else
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    if(num_server==1) finish();
                    break;
            }
        });
    }

    private void startInstall(File fileName) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(FileProvider.getUriForFile(this,BuildConfig.APPLICATION_ID + ".provider",fileName), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        }else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(fileName), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        View view = getCurrentFocus();
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:
                    finish();
                    System.exit(0);
                    android.os.Process.killProcess(android.os.Process.myPid());
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }
}
