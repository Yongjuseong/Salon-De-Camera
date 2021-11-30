package com.project.backspace;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class ARActivity extends AppCompatActivity implements View.OnTouchListener {
    float[] value = new float[9];
    float[] savedValue = new float[9];

    int width,height;
    Uri uri = Uri.parse("/storage/emulated/0/SalonDeCamera/GET/getImage.png");

    int editMode = 0;
    private static final String TAG = "Touch";
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();
    private Matrix savedMatrix2 = new Matrix();

    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;
    private double mCurrAngle = 0;
    private double mPrevAngle = 0;
    private double mAddAngle = 0;

    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;

    int hair_select=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);

        ImageView rotationBtn = (ImageView) findViewById(R.id.rotationBtn);
        rotationBtn.setVisibility(View.GONE);
        ImageView checkBtn = (ImageView) findViewById(R.id.checkBtn);
        checkBtn.setVisibility(View.GONE);

        ImageView view = (ImageView) findViewById(R.id.hair_ar);
        view.setImageResource(R.drawable.hair1);
        matrix.getValues(value);

        int x = getInt(getApplicationContext(),"x");
        int y = getInt(getApplicationContext(),"y");
        int w = getInt(getApplicationContext(),"w");
        int sizeW = getInt(getApplicationContext(),"width");
        int sizeH = getInt(getApplicationContext(),"height");

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ARActivity.this.getApplicationContext());
        sp.edit().remove("x").commit();
        sp.edit().remove("y").commit();
        sp.edit().remove("w").commit();
        sp.edit().remove("width").commit();
        sp.edit().remove("height").commit();

        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        int garo = dm.widthPixels;
        int sero = dm.heightPixels;

        int ratio = ((double)sizeW/garo > (double)sizeH/sero)?1:2;

        if(ratio == 1){
            double tmp = (double)garo/sizeW;
            x *= tmp;
            y *= tmp;
            w *= tmp;
        }else if(ratio == 2){
            double tmp = (double)sero/sizeW;
            x *= tmp;
            y *= tmp;
            w *= tmp;
        }

        value[0] = (float) (w * 0.37) / 911;
        value[2] = (float) x ;
        value[4] = (float) (w * 0.37) / 911;
        value[5] = (float) y-(float)(w * 0.34);
        savedMatrix2.getValues(savedValue);
        savedValue[0] = (float) (w * 0.37) / 911;
        savedValue[4] = (float) (w * 0.37) / 911;
        matrix.setValues(value);
        savedMatrix2.set(matrix);
        view.setImageMatrix(matrix);
        view.bringToFront();
        view.setOnTouchListener(this);

        ImageView view2 = (ImageView) findViewById(R.id.hair_sample);
        view2.setImageResource(R.drawable.hairsample1);

        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        ImageView ar_main = (ImageView)findViewById(R.id.ar_main);
        RelativeLayout tv = (RelativeLayout)findViewById(R.id.tv);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tv.getLayoutParams();
        RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) ar_main.getLayoutParams();
        params2.height = params.height = (int)(height * 0.7);
        tv.setLayoutParams(params);
        ar_main.setImageURI(uri);
        ar_main.setLayoutParams(params2);

        ImageView editBtn = (ImageView) findViewById(R.id.editBtn);
        editBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LinearLayout arLayout = (LinearLayout) findViewById(R.id.arLayout);
                LinearLayout colorLayout = (LinearLayout) findViewById(R.id.colorLayout);
                arLayout.setVisibility(View.VISIBLE);
                colorLayout.setVisibility(View.GONE);

                Toast.makeText(ARActivity.this,"터치를 이용해 머리를 수정할 수 있습니다!", Toast.LENGTH_LONG).show();
                editMode = 1;
                ImageView rotationBtn = (ImageView) findViewById(R.id.rotationBtn);
                ImageView checkBtn = (ImageView) findViewById(R.id.checkBtn);
                ImageView leftBtn = (ImageView) findViewById(R.id.leftBtn);
                ImageView rightBtn = (ImageView) findViewById(R.id.rightBtn);
                ImageView hairsample = (ImageView) findViewById(R.id.hair_sample);

                rotationBtn.setVisibility(View.VISIBLE);
                checkBtn.setVisibility(View.VISIBLE);
                leftBtn.setVisibility(View.GONE);
                rightBtn.setVisibility(View.GONE);
                hairsample.setVisibility(View.GONE);
            }
        });
        checkBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(editMode==1) {
                    editMode = 0;
                    ImageView rotationBtn = (ImageView) findViewById(R.id.rotationBtn);
                    ImageView checkBtn = (ImageView) findViewById(R.id.checkBtn);
                    ImageView leftBtn = (ImageView) findViewById(R.id.leftBtn);
                    ImageView rightBtn = (ImageView) findViewById(R.id.rightBtn);
                    ImageView hairsample = (ImageView) findViewById(R.id.hair_sample);

                    rotationBtn.setVisibility(View.GONE);
                    checkBtn.setVisibility(View.GONE);
                    leftBtn.setVisibility(View.VISIBLE);
                    rightBtn.setVisibility(View.VISIBLE);
                    hairsample.setVisibility(View.VISIBLE);
                }else if(editMode==2){
                    editMode = 1;
                    ImageView rotationBtn = (ImageView) findViewById(R.id.rotationBtn);
                    ImageView checkBtn = (ImageView) findViewById(R.id.checkBtn);
                    ImageView leftBtn = (ImageView) findViewById(R.id.leftBtn);
                    ImageView rightBtn = (ImageView) findViewById(R.id.rightBtn);
                    ImageView hairsample = (ImageView) findViewById(R.id.hair_sample);
/*                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) checkBtn.getLayoutParams();
                    params.rightMargin = 50;
                    checkBtn.setLayoutParams(params);*/

                    rotationBtn.setVisibility(View.VISIBLE);
                    checkBtn.setVisibility(View.VISIBLE);
                    leftBtn.setVisibility(View.GONE);
                    rightBtn.setVisibility(View.GONE);
                    hairsample.setVisibility(View.GONE);
                }
            }
        });
        rotationBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(ARActivity.this,"터치를 이용해 머리를 회전할 수 있습니다!", Toast.LENGTH_LONG).show();
                editMode = 2;
                ImageView checkBtn = (ImageView) findViewById(R.id.checkBtn);
                ImageView rotationBtn = (ImageView) findViewById(R.id.rotationBtn);
                /*LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) checkBtn.getLayoutParams();
                params.rightMargin = 0;
                checkBtn.setLayoutParams(params);*/
                rotationBtn.setVisibility(View.GONE);
            }
        });

        ImageView colorBtn = (ImageView) findViewById(R.id.color);
        colorBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LinearLayout arLayout = (LinearLayout) findViewById(R.id.arLayout);
                LinearLayout colorLayout = (LinearLayout) findViewById(R.id.colorLayout);
                arLayout.setVisibility(View.GONE);
                colorLayout.setVisibility(View.VISIBLE);
            }
        });
        ImageView colorCheckBtn = (ImageView) findViewById(R.id.colorCheckBtn);
        colorCheckBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LinearLayout arLayout = (LinearLayout) findViewById(R.id.arLayout);
                LinearLayout colorLayout = (LinearLayout) findViewById(R.id.colorLayout);
                arLayout.setVisibility(View.VISIBLE);
                colorLayout.setVisibility(View.GONE);
            }
        });

        ImageView red = (ImageView) findViewById(R.id.red);
        ImageView pink = (ImageView) findViewById(R.id.pink);
        ImageView orange = (ImageView) findViewById(R.id.orange);
        ImageView yellow = (ImageView) findViewById(R.id.yellow);
        ImageView brown = (ImageView) findViewById(R.id.brown);
        ImageView bam = (ImageView) findViewById(R.id.bam);
        ImageView gray = (ImageView) findViewById(R.id.gray);
        ImageView black = (ImageView) findViewById(R.id.black);

        red.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImageView view = (ImageView) findViewById(R.id.hair_ar);
                int tmp[] = setColor(1);
                view.setColorFilter(Color.argb(100,tmp[0],tmp[1],tmp[2]));
            }
        });
        pink.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImageView view = (ImageView) findViewById(R.id.hair_ar);
                int tmp[] = setColor(2);
                view.setColorFilter(Color.argb(100,tmp[0],tmp[1],tmp[2]));
            }
        });
        orange.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImageView view = (ImageView) findViewById(R.id.hair_ar);
                int tmp[] = setColor(3);
                view.setColorFilter(Color.argb(100,tmp[0],tmp[1],tmp[2]));
            }
        });
        yellow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImageView view = (ImageView) findViewById(R.id.hair_ar);
                int tmp[] = setColor(4);
                view.setColorFilter(Color.argb(100,tmp[0],tmp[1],tmp[2]));
            }
        });
        brown.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImageView view = (ImageView) findViewById(R.id.hair_ar);
                int tmp[] = setColor(5);
                view.setColorFilter(Color.argb(100,tmp[0],tmp[1],tmp[2]));
            }
        });
        bam.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImageView view = (ImageView) findViewById(R.id.hair_ar);
                int tmp[] = setColor(6);
                view.setColorFilter(Color.argb(100,tmp[0],tmp[1],tmp[2]));
            }
        });
        gray.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImageView view = (ImageView) findViewById(R.id.hair_ar);
                int tmp[] = setColor(7);
                view.setColorFilter(Color.argb(100,tmp[0],tmp[1],tmp[2]));
            }
        });
        black.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImageView view = (ImageView) findViewById(R.id.hair_ar);
                int tmp[] = setColor(8);
                view.setColorFilter(Color.argb(100,tmp[0],tmp[1],tmp[2]));
            }
        });


        ImageView leftBtn = (ImageView) findViewById(R.id.leftBtn);
        leftBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImageView view = (ImageView) findViewById(R.id.hair_sample);
                ImageView view2 = (ImageView) findViewById(R.id.hair_ar);
                TextView view3 = (TextView) findViewById(R.id.hairName);
                switch(hair_select){
                    case 1: hair_select = 3;
                        view.setImageResource(R.drawable.hairsample3);
                        view2.setImageResource(R.drawable.hair3);
                        view3.setText("어쉬매트릭컷");
                        break;
                    case 2: hair_select = 1;
                        view.setImageResource(R.drawable.hairsample1);
                        view2.setImageResource(R.drawable.hair1);
                        view3.setText("볼륨펌");
                        break;
                    case 3: hair_select = 2;
                        view.setImageResource(R.drawable.hairsample2);
                        view2.setImageResource(R.drawable.hair2);
                        view3.setText("애즈펌");
                        break;
                }
            }
        });

        ImageView rightBtn = (ImageView) findViewById(R.id.rightBtn);
        rightBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImageView view = (ImageView) findViewById(R.id.hair_sample);
                ImageView view2 = (ImageView) findViewById(R.id.hair_ar);
                TextView view3 = (TextView) findViewById(R.id.hairName);
                switch(hair_select){
                    case 1: hair_select = 2;
                        view.setImageResource(R.drawable.hairsample2);
                        view2.setImageResource(R.drawable.hair2);
                        view3.setText("애즈펌");
                        break;
                    case 2: hair_select = 3;
                        view.setImageResource(R.drawable.hairsample3);
                        view2.setImageResource(R.drawable.hair3);
                        view3.setText("어쉬매트릭컷");
                        break;
                    case 3: hair_select = 1;
                        view.setImageResource(R.drawable.hairsample1);
                        view2.setImageResource(R.drawable.hair1);
                        view3.setText("볼륨펌");
                        break;
                }
            }
        });

        ImageView saveBtn = (ImageView) findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImageView a = (ImageView) findViewById(R.id.ar_main);
                ImageView b = (ImageView) findViewById(R.id.hair_ar);
                Bitmap bitmap = Bitmap.createBitmap(a.getWidth(), a.getHeight(), Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(bitmap);
                a.draw(canvas);
                b.draw(canvas);
                storeImage(bitmap,"test.jpg",ARActivity.this);
                MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "test", "Backspace");
                Toast.makeText(ARActivity.this,"저장되었습니다!",Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
            if(editMode==1) {
                ImageView view = (ImageView) v;

                // Dump touch event to log
                dumpEvent(event);

                // Handle touch events here...
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d("fuck", "work");
                        savedMatrix.set(matrix);
                        start.set(event.getX(), event.getY());
                        Log.d(TAG, "mode=DRAG");
                        mode = DRAG;
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        Log.d("fuck", "work");
                        oldDist = spacing(event);
                        Log.d(TAG, "oldDist=" + oldDist);
                        if (oldDist > 10f) {
                            savedMatrix.set(matrix);
                            midPoint(mid, event);
                            mode = ZOOM;
                            Log.d(TAG, "mode=ZOOM");
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        Log.d("fuck", "work");
                        mode = NONE;
                        Log.d(TAG, "mode=NONE");
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.d("fuck", "work");
                        if (mode == DRAG) {
                            // ...
                            matrix.set(savedMatrix);
                            matrix.postTranslate(event.getX() - start.x,
                                    event.getY() - start.y);
                        } else if (mode == ZOOM) {
                            Log.d("fuck", "work");
                            float newDist = spacing(event);
                            Log.d(TAG, "newDist=" + newDist);
                            if (newDist > 10f) {
                                matrix.set(savedMatrix);
                                float scale = newDist / oldDist;
                                matrix.postScale(scale, scale, mid.x, mid.y);
                            }
                        }
                        break;
                }

                matrixTurning(matrix, view);
                view.setImageMatrix(matrix);
                return true; // indicate event was handled
            }else if(editMode == 2 ){
                ImageView view = (ImageView) v;
                final float centerOfWidth = v.getWidth() / 2;
                final float centerOfHeight = v.getHeight() / 2;
                final float x = event.getX();
                final float y = event.getY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mCurrAngle = Math.toDegrees(Math.atan2(x - centerOfWidth, centerOfHeight - y));
                        break;

                    case MotionEvent.ACTION_MOVE:
                        mPrevAngle = mCurrAngle;
                        mCurrAngle = Math.toDegrees(Math.atan2(x - centerOfWidth, centerOfHeight - y));
                        animate(v, mAddAngle, mAddAngle + mCurrAngle - mPrevAngle);
                        mAddAngle += mCurrAngle - mPrevAngle;
                        break;

                    case MotionEvent.ACTION_UP:
                        break;

                }
                return true;
            }
        return true;
    }

    /** Show an event in the LogCat view, for debugging */
    private void dumpEvent(MotionEvent event) {
        String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
                "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_").append(names[actionCode]);
        if (actionCode == MotionEvent.ACTION_POINTER_DOWN
                || actionCode == MotionEvent.ACTION_POINTER_UP) {
            sb.append("(pid ").append(
                    action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            sb.append(")");
        }
        sb.append("[");
        for (int i = 0; i < event.getPointerCount(); i++) {
            sb.append("#").append(i);
            sb.append("(pid ").append(event.getPointerId(i));
            sb.append(")=").append((int) event.getX(i));
            sb.append(",").append((int) event.getY(i));
            if (i + 1 < event.getPointerCount())
                sb.append(";");
        }
        sb.append("]");
        Log.d(TAG, sb.toString());
    }

    /** Determine the space between the first two fingers */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }

    /** Calculate the mid point of the first two fingers */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }


    private void matrixTurning(Matrix matrix, ImageView view){
        // 매트릭스 값
        matrix.getValues(value);
        savedMatrix2.getValues(savedValue);

        Log.d("fuck",matrix.toString());
        Log.d("fuck",savedMatrix2.toString());

        // 뷰 크기
        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        int width=dm.widthPixels;
        int height=dm.heightPixels;

        // 이미지 크기
        Drawable d = view.getDrawable();
        if (d == null)  return;
        int imageWidth = d.getIntrinsicWidth();
        int imageHeight = d.getIntrinsicHeight();
        int scaleWidth = (int) (imageWidth * value[0]);
        int scaleHeight = (int) (imageHeight * value[4]);

        // 이미지가 바깥으로 나가지 않도록.
/*        if (value[2] < width - scaleWidth)   value[2] = width - scaleWidth;
        if (value[5] < height - scaleHeight)   value[5] = height - scaleHeight;
        if (value[2] > 0)   value[2] = 0;
        if (value[5] > 0)   value[5] = 0;*/

        // 10배 이상 확대 하지 않도록
        if (value[0] > 10 || value[4] > 10){
            value[0] = savedValue[0];
            value[4] = savedValue[4];
            value[2] = savedValue[2];
            value[5] = savedValue[5];
        }

        // 화면보다 작게 축소 하지 않도록
  /*      if (imageWidth > width || imageHeight > height){
            if (scaleWidth < width && scaleHeight < height){
                int target = WIDTH;
                if (imageWidth < imageHeight) target = HEIGHT;

                if (target == WIDTH) value[0] = value[4] = (float)width / imageWidth;
                if (target == HEIGHT) value[0] = value[4] = (float)height / imageHeight;

                scaleWidth = (int) (imageWidth * value[0]);
                scaleHeight = (int) (imageHeight * value[4]);

                if (scaleWidth > width) value[0] = value[4] = (float)width / imageWidth;
                if (scaleHeight > height) value[0] = value[4] = (float)height / imageHeight;
            }
        }*/

        // 원래부터 작은 얘들은 본래 크기보다 작게 하지 않도록
/*        else{
            if (value[0] < 0.5)   value[0] = (float)0.5;
            if (value[4] < 0.5)   value[4] = (float)0.5;
        }*/

        // 그리고 가운데 위치하도록 한다.
/*        scaleWidth = (int) (imageWidth * value[0]);
        scaleHeight = (int) (imageHeight * value[4]);
        if (scaleWidth < width){
            value[2] = (float) width / 2 - (float)scaleWidth / 2;
        }
        if (scaleHeight < height){
            value[5] = (float) height / 2 - (float)scaleHeight / 2;
        }*/

        matrix.setValues(value);
        savedMatrix2.set(matrix);
    }

    private void rotate(View view, double fromDegrees, double toDegrees) {
        final RotateAnimation rotate = new RotateAnimation((float) fromDegrees, (float) toDegrees,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);

        rotate.setDuration(0);
        rotate.setFillAfter(true);
        view.startAnimation(rotate);
    }

    private void animate(View view, double fromDegrees, double toDegrees) {
        final RotateAnimation rotate = new RotateAnimation((float) fromDegrees, (float) toDegrees,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(0);
        rotate.setFillAfter(true);
        view.startAnimation(rotate);
    }

    public int getInt(Context context, String key){
        SharedPreferences pref=context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        return pref.getInt(key,1);
    }

    private int[] setColor(int hairColor){
        int[] tmp = new int[4];
        switch(hairColor){
            case 1: tmp[0] = 255; tmp[1] = 36; tmp[2] = 61; break;
            case 2: tmp[0] = 224; tmp[1] = 97; tmp[2] = 118; break;
            case 3: tmp[0] = 205; tmp[1] = 84; tmp[2] = 31; break;
            case 4: tmp[0] = 255; tmp[1] = 255; tmp[2] = 200; break;
            case 5: tmp[0] = 137; tmp[1] = 74; tmp[2] = 2; break;
            case 6: tmp[0] = 62; tmp[1] = 19; tmp[2] = 0; break;
            case 7: tmp[0] = 162; tmp[1] = 162; tmp[2] = 162; break;
            case 8: tmp[0] = 0; tmp[1] = 0; tmp[2] = 0; break;
            default:break;
        }
        return tmp;
    }
    static public void storeImage(Bitmap v,String filename,Context context){

        String StoragePath =
                Environment.getExternalStorageDirectory().getAbsolutePath();
        String savePath = StoragePath + "/SalonDeCamera";
        File f = new File(savePath);
        if (!f.isDirectory())f.mkdirs();

        FileOutputStream fos;
        try{
            fos = new FileOutputStream(savePath+"/"+filename);
            v.compress(Bitmap.CompressFormat.JPEG,100,fos);

        }catch (Exception e){
            e.printStackTrace();
        }
        context.sendBroadcast(new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(f)));
    }
}

