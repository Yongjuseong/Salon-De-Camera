package com.project.backspace;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.gson.Gson;
import com.project.backspace.ui.main.MainPager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static android.content.Intent.FLAG_ACTIVITY_NO_HISTORY;

public class MainActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback{

    private ViewPager viewPager ;
    private MainPager pagerAdapter ;

    private final int GET_GALLERY_IMAGE = 200;
    private final int GET_CAMERA_IMAGE = 201;
    private File tmpUri;
    private File file;
    private Uri cameraUri;

    private View mLayout;
    private String uri;

    private String baseURL = "http://18.189.192.9:51234";
    private Retrofit mRetrofit,mRetrofit2;
    private Gson mGson;
    private setRetro mSetRetro;
    private transImage mTransImage;
    private getXY mgetXY;
    private Call<String> mtest;
    private Call<String> mCallgetXY;
    private Call<ResponseBody> mCallImg;

    private String TAG = "VideoActivity";
    private VideoView mVideoview;

    private void setRetrofitInit() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(120, TimeUnit.SECONDS)
                .build();
        mRetrofit = new Retrofit.Builder()
                .baseUrl(baseURL)
                .client(okHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        mRetrofit2 = new Retrofit.Builder()
                .baseUrl(baseURL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mSetRetro = mRetrofit.create(setRetro.class);
        mTransImage = mRetrofit2.create(transImage.class);
        mgetXY = mRetrofit.create(getXY.class);
    }

    private void test() {
        mtest = mSetRetro.getImg();
        mtest.enqueue(mRetrofitCallback);
    }

    private void callImg(int code) {
        if(code==1) {
            String a[];
            a=cameraUri.toString().split("/");
            file = new File("/storage/emulated/0/SalonDeCamera/"+a[5]);
            Log.d("fuck", "/storage/emulated/0/SalonDeCamera/"+a[5]);
        }
        if(code==2){
            file = new File(uri);
        Log.d("fuck",uri);}
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("img", file.getName(), requestFile);
        mCallImg = mTransImage.uploadImage(body);
        mCallImg.enqueue(mTransImageCallback);
    }

    private void callGetXY(){
        mCallgetXY = mgetXY.getXY();
        mCallgetXY.enqueue(mgetXYCallback);
    }

    private Callback<String> mgetXYCallback = new Callback<String>() {
        @Override
        public void onResponse(Call<String> call, Response<String> response) {
            String tmp[]=response.body().split("\n");
            String xyStr[]=tmp[0].split(",");
            String size[]=tmp[1].split(",");
            int xy[] = new int[3];
            for(int i=0; i<3; i++)
                xy[i]=Integer.parseInt(xyStr[i]);
            setInt(getApplicationContext(),"x", xy[0]);
            setInt(getApplicationContext(),"y", xy[1]);
            setInt(getApplicationContext(),"w", xy[2]);
            setInt(getApplicationContext(),"width", Integer.parseInt(size[0]));
            setInt(getApplicationContext(),"height", Integer.parseInt(size[1]));
            Intent intent = new Intent(MainActivity.this, ARActivity.class);
            startActivity(intent);
        }
        @Override
        public void onFailure(Call<String> call, Throwable t) {
            Log.d("fuck","error : "+t);
        }
    };

    private Callback<String> mRetrofitCallback = new Callback<String>() {
        @Override
        public void onResponse(Call<String> call, Response<String> response) {
        }
        @Override
        public void onFailure(Call<String> call, Throwable t) {
            Log.d("fuck","error : "+t);
        }
    };

    private Callback<ResponseBody> mTransImageCallback = new Callback<ResponseBody>(){
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            Log.d("fuck","Download Start");
            Log.d("fuck",response.body().toString());
            try {
                File storageDir = new File(Environment.getExternalStorageDirectory() + "/SalonDeCamera/GET/");
                if (!storageDir.exists()) storageDir.mkdirs();
                File futureStudioIconFile = new File("/storage/emulated/0/SalonDeCamera/GET/getImage.png");
                InputStream inputStream = null;
                OutputStream outputStream = null;
                try {
                    byte[] fileReader = new byte[4096];
                    long fileSize = response.body().contentLength();
                    long fileSizeDownloaded = 0;
                    inputStream = response.body().byteStream();
                    outputStream = new FileOutputStream(futureStudioIconFile);
                    while (true) {
                        int read = inputStream.read(fileReader);
                        if (read == -1) {
                            break;
                        }
                        outputStream.write(fileReader, 0, read);
                        fileSizeDownloaded += read;
                    }
                    outputStream.flush();
                } catch (IOException e) {
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                }
            } catch (IOException e) {
            }
            callGetXY();
        }
        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            Log.d("fuck","error : "+t);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager = (ViewPager)findViewById(R.id.view_pager);
        pagerAdapter = new MainPager(this);
        viewPager.setAdapter(pagerAdapter);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                if (i == 0) {
                    mVideoview = (VideoView) viewPager.findViewById(R.id.videoView);
                    Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.loop);
                    mVideoview.setVideoURI(uri);
                    mVideoview.start();
                    mVideoview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mp.setLooping(true);
                        }
                    });
                } else if (i == 1) {
                    ImageView cameraBtn = (ImageView) viewPager.findViewById(R.id.cameraBtn);
                    cameraBtn.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            try{
                                tmpUri = createImageFile();
                            }catch(IOException e){
                                Toast.makeText(MainActivity.this,"다시 시도해주세요!",Toast.LENGTH_LONG).show();
                                finish();
                            }
                            if(tmpUri!=null){
                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                cameraUri = FileProvider.getUriForFile(MainActivity.this,"com.project.backspace.fileprovider",tmpUri);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
                                startActivityForResult(intent, GET_CAMERA_IMAGE);
                            }
                        }
                    });

                    ImageView galleryBtn = (ImageView) viewPager.findViewById(R.id.galleryBtn);
                    galleryBtn.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            intent.setType("image/*");
                            startActivityForResult(intent, GET_GALLERY_IMAGE);
                        }
                    });
                }
            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        setRetrofitInit();
        test();
        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if ( writeExternalStoragePermission != PackageManager.PERMISSION_GRANTED){

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {


                Snackbar.make(mLayout, "이 앱을 실행하려면 카메라와 외부 저장소 접근 권한이 필요합니다.",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        ActivityCompat.requestPermissions( MainActivity.this, REQUIRED_PERMISSIONS,
                                PERMISSIONS_REQUEST_CODE);
                    }
                }).show();


            } else {

                ActivityCompat.requestPermissions( this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }
    }

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {
        if ( requestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            boolean check_result = true;
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }
            if ( check_result ) {
                ;
            }
            else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();
                }else {
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode==GET_CAMERA_IMAGE) {
            callImg(1);
        }
        if(requestCode==GET_GALLERY_IMAGE) {
            uri = getRealPathFromURI(data.getData());
            ThreadA threadA = new ThreadA();
            threadA.start();
            callImg(2);
        }
    }

    private String getRealPathFromURI(Uri contentUri) {

        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        cursor.moveToFirst();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        return cursor.getString(column_index);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        String imageFileName = "SalonDeCamera_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/SalonDeCamera/");
        if (!storageDir.exists()) storageDir.mkdirs();
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(image));
        getApplicationContext().sendBroadcast(mediaScanIntent);
        return image;
    }
    private class ThreadA extends Thread {
        public ThreadA() {

        }
        public void run() {
            Intent intent = new Intent(MainActivity.this, Loading.class);
            intent.addFlags(FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
        }
    }
    public void setInt(Context context, String key, int value){
        SharedPreferences pref=context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(key,value);
        editor.commit();
    }

}



