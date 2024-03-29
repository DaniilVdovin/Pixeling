package com.daniilvdovin.pixelit;

import static com.daniilvdovin.pixelit.Data.PixelRate;
import static com.daniilvdovin.pixelit.Data.ScaleSize;
import static com.daniilvdovin.pixelit.Data._MyTargetAds;
import static com.daniilvdovin.pixelit.Data._isAppGalleryAds;
import static com.daniilvdovin.pixelit.Data._isDots;
import static com.daniilvdovin.pixelit.Data._isFilter;
import static com.daniilvdovin.pixelit.Data._isGalleryOpen;
import static com.daniilvdovin.pixelit.Data._isGoogleAds_DebugDevice;
import static com.daniilvdovin.pixelit.Data._isGooglePlayServices;
import static com.daniilvdovin.pixelit.Data._isGray;
import static com.daniilvdovin.pixelit.Data._isGrid;
import static com.daniilvdovin.pixelit.Data._isML_FaceDetected;
import static com.daniilvdovin.pixelit.Data._isML_SegmentDetected;
import static com.daniilvdovin.pixelit.Data._isScanColor;
import static com.daniilvdovin.pixelit.Data._isGoogleAds;
import static com.daniilvdovin.pixelit.Data._isDebug;
import static com.daniilvdovin.pixelit.Data._isCanCrop;
import static com.daniilvdovin.pixelit.Data.colors;
import static com.daniilvdovin.pixelit.Data.image;
import static com.daniilvdovin.pixelit.Data.imageAfterSave;
import static com.daniilvdovin.pixelit.Data.imageUri;
import static com.daniilvdovin.pixelit.Data.image_processed;
import static com.daniilvdovin.pixelit.Data.image_name;
import static com.daniilvdovin.pixelit.Data.processing;
import static com.daniilvdovin.pixelit.ml.FaceDetect.resultBitmap;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.daniilvdovin.pixelit.colorize.ColorizeActivity;
import com.daniilvdovin.pixelit.colorize.PixelData;
import com.daniilvdovin.pixelit.ml.FaceDetect;
import com.daniilvdovin.pixelit.ml.SegmentMl;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.my.target.ads.MyTargetView;
import com.my.target.common.MyTargetConfig;
import com.my.target.common.MyTargetManager;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    //System
    private static final int RESULT_LOAD_IMG = 1;
    private static final int RESULT_CROP_IMG = 2;
    private static final int SCALE_SIZE = 60;//50
    private static final int PIXEL = 8;
    private static final String SHARED_PREFERENCES_NAME = "Setup";
    private static final String FIRST_START = "First_Start";
    int ResultSize = 0;

    //UI
    ImageView imageView;
    Button reset,save, image_picker,share,inst_confirm;
    ProgressBar progressBar;
    //UI-Parameters
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch s_grid,s_gray,s_dot,s_filter,
            s_ml_face,s_ml_face_invert;
    SeekBar sb_pixelRate;
    //UI-Text
    TextView t_pixelSize,t_imageSize,t_pixelRate;

    //Google ads frame
    View GoogleAdFrame
            ,MainFrame,ToolFrame,InstructionCard;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Init system
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        _isGooglePlayServices = isGooglePlayServicesAvailable(MainActivity.this);
        if(_isDebug) Log.e("GPS","GPS:"+_isGooglePlayServices);
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME,MODE_PRIVATE);
        boolean _isFirstStart = sharedPreferences.getBoolean(FIRST_START,true);

        //Init UI
        //Image View Processing
        imageView = findViewById(R.id.imageView);
        //Buttons Tool frame
        reset = findViewById(R.id.b_reset);
        save = findViewById(R.id.b_save);
        share = findViewById(R.id.b_share);
        //Buttons Instruction Frame
        inst_confirm = findViewById(R.id.Instruction_complite);
        //Image Picker Button
        image_picker = findViewById(R.id.b_imagepick);
        //Controllers
        s_gray = findViewById(R.id.s_gray);
        s_grid = findViewById(R.id.s_grid);
        s_dot = findViewById(R.id.s_dot);
        s_filter = findViewById(R.id.s_filter);
        s_ml_face = findViewById(R.id.s_ml_face_detector);
        s_ml_face_invert = findViewById(R.id.s_ml_face_detector_invert);
        sb_pixelRate = findViewById(R.id.sb_pixelRate);
        //Text
        t_pixelSize = findViewById(R.id.t_pixelSize);
        t_imageSize = findViewById(R.id.t_ImageSize);
        t_pixelRate = findViewById(R.id.t_pixel_rate);
        //Progress Bar
        progressBar = findViewById(R.id.progressBar);
        //ADS
        GoogleAdFrame = findViewById(R.id.AdFrame);
        //Frames
        MainFrame = findViewById(R.id.MainFrame);
        ToolFrame = findViewById(R.id.ToolFrame);
        InstructionCard = findViewById(R.id.Instruction_card);

        //UI PreSetup
        image_picker.setVisibility(View.VISIBLE);
        if(_isFirstStart){
            MainFrame.setVisibility(View.GONE);
            ToolFrame.setVisibility(View.GONE);
        }else{
            InstructionCard.setVisibility(View.GONE);
        }
        if(image==null) {
            _isGalleryOpen = false;
            image_picker.setEnabled(false);
            imageView.setEnabled(false);
            progressBar.setVisibility(View.GONE);
        }else{
            image_picker.setVisibility(View.GONE);
            LoadProcessedImage();
        }
        GoogleAdFrame.setVisibility(View.GONE);
        if(_MyTargetAds){
            MyTargetView adView = findViewById(R.id.adManagerAdView);
            adView.setSlotId(1189445);
            adView.setRefreshAd(true);
            adView.setListener(new MyTargetView.MyTargetViewListener() {
                @Override
                public void onLoad(@NonNull MyTargetView myTargetView) {
                    if (_isDebug) Log.e("ADS", "Ad Loaded");
                    GoogleAdFrame.setVisibility(View.VISIBLE);
                }

                @Override
                public void onNoAd(@NonNull String s, @NonNull MyTargetView myTargetView) {
                    if (_isDebug) Log.e("ADS", "Ad Closed");
                    GoogleAdFrame.setVisibility(View.GONE);
                }

                @Override
                public void onShow(@NonNull MyTargetView myTargetView) {

                }

                @Override
                public void onClick(@NonNull MyTargetView myTargetView) {

                }
            });
            adView.load();
        }
        //Google Ads
        if (_isGooglePlayServices && _isGoogleAds) {
            if (_isDebug) Log.e("ADS","Ads Pre Load");

        }
        //Editor
        reset.setVisibility(_isScanColor?View.VISIBLE:View.GONE);
        //share.setVisibility(View.GONE);
        Parameters_ShowHide(image != null);
        //Permissions
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            imageView.setEnabled(true);
            image_picker.setEnabled(true);
        } else {
            imageView.setEnabled(false);
            image_picker.setEnabled(false);
            requestPermissionLauncher.launch(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        //Image Load
        View.OnClickListener ip = (v) -> {
            if(!_isGalleryOpen) {
                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
                _isGalleryOpen = true;
            }
        };
        image_picker.setOnClickListener(ip);
        imageView.setOnClickListener(ip);

        //UI-Logic
        //UI-Logic-Button
        inst_confirm.setOnClickListener(view -> {
            @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(FIRST_START,false);
            editor.apply();
            MainFrame.setVisibility(View.VISIBLE);
            ToolFrame.setVisibility(View.VISIBLE);
            InstructionCard.setVisibility(View.GONE);
        });
        reset.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, ColorizeActivity.class));
        });
        save.setOnClickListener(view -> {
            imageAfterSave = saveImage();
        });
        share.setOnClickListener(view -> {
            Share();
        });
        //UI-Logic-Switch
        s_gray.setOnClickListener(view -> {
            _isGray = s_gray.isChecked();
            refreshImage(image);
        });
        s_grid.setOnClickListener(view -> {
            _isGrid = s_grid.isChecked();
            refreshImage(image);
        });
        s_dot.setOnClickListener(view -> {
            _isDots = s_dot.isChecked();
            refreshImage(image);
        });
        s_filter.setOnClickListener(view -> {
            _isFilter = s_filter.isChecked();
            refreshImage(image);
        });
        s_ml_face.setOnClickListener(view -> {
            _isML_FaceDetected = s_ml_face.isChecked();
            if(FaceDetect.resultBitmap==null) {
                Toast.makeText(MainActivity.this, "Face not detected", Toast.LENGTH_SHORT).show();
                _isML_FaceDetected = false;
                s_ml_face.setChecked(false);
            }else {
                Toast.makeText(MainActivity.this, "Face detected", Toast.LENGTH_SHORT).show();
                Parameters_ShowHide(true);
                if(_isML_FaceDetected){
                    _isDots = false;
                    _isGrid = false;
                }else{
                    _isDots = s_dot.isChecked();
                    _isGrid = s_grid.isChecked();
                }
                refreshImage(image);
            }
        });
        s_ml_face_invert.setOnClickListener(view -> {
            _isML_SegmentDetected = s_ml_face_invert.isChecked();
            if(SegmentMl.resultBitmap==null) {
                Toast.makeText(MainActivity.this, "Segment not detected", Toast.LENGTH_SHORT).show();
                _isML_SegmentDetected = false;
                s_ml_face_invert.setChecked(false);
            }else {
                Toast.makeText(MainActivity.this, "Segment detected", Toast.LENGTH_SHORT).show();
                Parameters_ShowHide(true);
                if(_isML_SegmentDetected){
                    _isML_FaceDetected = false;
                }else{
                    _isML_FaceDetected = s_ml_face.isChecked();
                }
                refreshImage(image);
            }
        });
        //UI-Logic-SeekBar
        sb_pixelRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(i==0){
                    progressBar.setVisibility(View.GONE);
                    if(processing != null) processing.cancel(true);
                    imageView.setImageBitmap(image);
                    Parameters_ShowHide(false);
                    sb_pixelRate.setEnabled(true);
                    t_pixelRate.setText(getText(R.string.p_r)+": "+getString(R.string.original));
                    t_pixelSize.setText(getText(R.string.p_s)+"\n"+ 1+"x"+1);
                    return;
                }else{
                    Parameters_ShowHide(true);
                }
                ScaleSize = SCALE_SIZE;///(i>4?4:i);
                if(_isDebug)Log.e("ScaleSaze","i:"+i+" ScaleSize:"+ScaleSize+" f:"+(i>2?i/2:i));
                PixelRate = PIXEL*i;
                t_pixelRate.setText(getText(R.string.p_r)+": "+i);
                t_pixelSize.setText(getText(R.string.p_s)+"\n"+ PixelRate+"x"+ PixelRate);
                refreshImage(image);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
    //Google Play Services
    public boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if(status != ConnectionResult.SUCCESS && status != ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
            if(_isDebug) {
                Log.e("GMS", "status:" + status);
                Toast.makeText(this, "Status:" + status, Toast.LENGTH_LONG).show();
            }
            if(status == ConnectionResult.SERVICE_INVALID){
                Toast.makeText(activity, "MicroG ^_^", Toast.LENGTH_SHORT).show();
                return true;
            }
            if(googleApiAvailability.isUserResolvableError(status)) {
                Objects.requireNonNull(googleApiAvailability.getErrorDialog(activity, status, 2404)).show();
            }
            return false;
        }
        return true;
    }
    //Off button if don't have loaded image
    void Parameters_ShowHide(boolean _is){
        reset.setEnabled(_is);
        save.setEnabled(_is);
        share.setEnabled(_is);
        s_gray.setEnabled(_is);
        if(_isML_FaceDetected)
        {
            s_grid.setEnabled(false);
            s_dot.setEnabled(false);
            s_ml_face_invert.setEnabled(false);
        }else {
            s_grid.setEnabled(_is);
            s_dot.setEnabled(_is);
            s_ml_face_invert.setEnabled(_is);
        }
        if(_isML_SegmentDetected){
            s_ml_face.setEnabled(false);
        }else{
            s_ml_face.setEnabled(_is);
        }
        if(!_isGooglePlayServices){
            s_ml_face.setEnabled(false);
            s_ml_face_invert.setEnabled(false);
        }
        s_filter.setEnabled(_is);
        sb_pixelRate.setEnabled(_is);

    }
    public void LoadProcessedImage(){
        imageView.setImageBitmap(image_processed);
    }
    //Update image in ImageView
    public void refreshImage(Bitmap image){
        imageView.setImageBitmap(pixelit_b(image));
    }
    //Get Image from Gallery
    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if(_isDebug)Log.e("IMAGE","resultCode: "+resultCode);
        if (resultCode == RESULT_OK) {
            //if need crop set _isCanCrop = true
            if(_isCanCrop) {
                switch (reqCode) {
                    case RESULT_LOAD_IMG://First pick image and send to crop
                        try {
                            Intent photoCropIntent = new Intent("com.android.camera.action.CROP");
                            photoCropIntent.putExtra("crop", true);
                            photoCropIntent.putExtra("aspectX", 1);
                            photoCropIntent.putExtra("aspectY", 1);
                            photoCropIntent.putExtra("return-data",true);
                            photoCropIntent.setData(data.getData());
                            loadBitmapFromIntent(data);
                            startActivityForResult(photoCropIntent, RESULT_CROP_IMG);
                        }catch (ActivityNotFoundException e){
                            Toast.makeText(this, R.string.notfound, Toast.LENGTH_SHORT).show();
                            loadBitmapFromIntent(data);
                        }
                        break;
                    case RESULT_CROP_IMG://Load image after crop
                        loadBitmapFromIntent(data);
                        break;
                }
            }else{
                loadBitmapFromIntent(data);
            }
            if(_isGooglePlayServices) {
                FaceDetect.resultBitmap = null;
                FaceDetect.getResult(this, image);
                SegmentMl.resultBitmap = null;
                SegmentMl.getResult(MainActivity.this, image);
            }
        }else {
            Toast.makeText(this,
                    R.string.dont_select_image,
                    Toast.LENGTH_LONG).show();
            _isGalleryOpen = false;
        }
    }
    //Load image from intent data
    public void loadBitmapFromIntent(Intent data){
        try {
            imageUri = data.getData();
            final String path = getPathFromURI(imageUri);
            final InputStream imageStream = getContentResolver().openInputStream(imageUri);
            image = BitmapFactory.decodeStream(imageStream);
            int dif = Math.max((image.getHeight()-image.getWidth()),(image.getWidth()-image.getHeight()));
            if(image.getHeight()<image.getWidth())image = cropBitmap(image,dif/2,image.getWidth()-dif/2,0,image.getHeight());
            if(image.getHeight()>image.getWidth())image = cropBitmap(image,0,image.getWidth(), dif/2, image.getHeight()-dif/2);
            refreshImage(image);
            //Auto set PixelRate
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                sb_pixelRate.setProgress(1,true);
            else
                sb_pixelRate.setProgress(1);
            imageAfterSave = null;
            Parameters_ShowHide(image != null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this,
                    R.string.sww,
                    Toast.LENGTH_LONG).show();
        }
        _isGalleryOpen = false;
    }
    public static Bitmap cropBitmap(Bitmap bitmap, int startX, int endX, int startY, int endY) {
        if(_isDebug) Log.e("CROP",(endX - startX)+"x"+(endY - startY));
        Bitmap output = Bitmap.createBitmap(endX - startX, endY - startY, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setColor(0xffffffff);

        Rect srcRect = new Rect(startX, startY, endX, endY);
        Rect desRect = new Rect(0, 0, endX - startX, endY - startY);

        canvas.drawBitmap(bitmap, srcRect, desRect, paint);

        return output;
    }
    //Get path from loaded uri
    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }
    //Processing image with parameters
    @SuppressLint("StaticFieldLeak")
    public Bitmap pixelit_b(Bitmap bitmap){
        if(bitmap==null)return null;
        if(_isScanColor) {
            //Scan color on original image after pixelate
            @SuppressLint("StaticFieldLeak")
            AsyncTask<Bitmap, PixelData, List<PixelData>> scancolor = new AsyncTask<Bitmap, PixelData, List<PixelData>>() {
                @Override
                protected List<PixelData> doInBackground(Bitmap... bitmaps) {
                    List<PixelData> temp = new ArrayList<>();
                    for (int i = (ScaleSize / 2); i < bitmap.getWidth(); i += ScaleSize) {
                        for (int j = (ScaleSize / 2); j < bitmap.getHeight(); j += ScaleSize) {
                            int c = Color.BLACK;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    c = bitmap.getPixel(i, j);
                                    temp.add(new PixelData(i/ScaleSize, j/ScaleSize, c, 0,i/ScaleSize+"."+j/ScaleSize));
                                    if (_isDebug)
                                        Log.e("color", "[" + i/ScaleSize + "," + j/ScaleSize + "]:" + Color.valueOf(c).toString());
                            }
                        }
                    }
                    return temp;
                }
                @Override
                protected void onPostExecute(List<PixelData> pixelData) {
                    super.onPostExecute(pixelData);
                    colors = pixelData;
                }
            };
            scancolor.execute(bitmap);
        }
        if(processing != null) processing.cancel(true);
        processing = new AsyncTask<Bitmap, Integer, Bitmap>(){
            Bitmap bitmap;
            @Override
            protected Bitmap doInBackground(Bitmap... bitmaps) {
                bitmap = bitmaps[0];
                bitmap = Bitmap.createScaledBitmap(bitmap, PixelRate,PixelRate,false);
                bitmap = Bitmap.createScaledBitmap(bitmap,(PixelRate*ScaleSize)+1,(PixelRate*ScaleSize)+1,_isFilter);

                //Display Grid on image
                if(_isGrid){
                    for (int i = 0; i < bitmap.getWidth(); i++) {
                        if(i%ScaleSize==0)
                            for (int j = 0; j < bitmap.getHeight(); j++) {
                                bitmap.setPixel(i, j, Color.GRAY);
                                bitmap.setPixel(j, i, Color.GRAY);
                            }
                    }
                }
                //Display Dots on image (filed circle)
                if(_isDots) {
                    for (int i = (ScaleSize / 2); i < bitmap.getWidth(); i += ScaleSize) {
                        for (int j = (ScaleSize / 2); j < bitmap.getHeight(); j += ScaleSize) {
                            int c = Color.BLACK;
                            int x = i;
                            int y = j;
                            int radius = 3;
                            for (y = -radius; y <= radius; y++)
                                for (x = -radius; x <= radius; x++)
                                    if ((x * x) + (y * y) <= (radius * radius))
                                        bitmap.setPixel(i+x, j+y, c);
                        }
                    }
                }
                //ML Face target
                if(_isML_FaceDetected){
                    bitmap = Masked(bitmap);
                }
                //Transfer image to grayscale
                if(_isGray)
                    bitmap = toGrayscale(bitmap);
                if(_isML_SegmentDetected) {
                    Bitmap mask = SegmentMl.getResult(MainActivity.this, image);
                    bitmap = MaskedSegment(bitmap, mask);
                }
                return bitmap;
            }
            //Show progress bar when processing image
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressBar.setVisibility(View.VISIBLE);
            }
            //Hide progress bar when processing complete and set data to UI
            @Override
            protected void onPostExecute(Bitmap bitmap) {
                postImageFromThread(bitmap);
            }
        };
        //Start thread processing
        processing.execute(bitmap);
        return image_processed;
    }
    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    void postImageFromThread(Bitmap bitmap){
        image_processed = bitmap;
        progressBar.setVisibility(View.GONE);
        LoadProcessedImage();
        t_imageSize.setText(getText(R.string.i_s)+"\n"+bitmap.getWidth()+"x"+bitmap.getHeight());
        save.setText(getText(R.string.save)+" ±("+ String.format("%.2f", byteSizeOf(bitmap)) +" kb)");
    }
    public Bitmap MaskedSegment(Bitmap bitmap,Bitmap mask) {
        Bitmap result = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas tempCanvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        tempCanvas.drawBitmap(Bitmap.createScaledBitmap(bitmap, mask.getWidth(), mask.getHeight(),false),0,0,new Paint());
        tempCanvas.drawBitmap(mask, 0, 0, paint);
        paint.setXfermode(null);
        //Draw result after performing masking
        Bitmap temp = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(temp);
        canvas.drawBitmap(image, 0,0,new Paint());
        canvas.drawBitmap(result, 0, 0, new Paint());
        return temp;
    }
    public Bitmap Masked(Bitmap bitmap) {
        if(resultBitmap==null)return bitmap;
        Bitmap mask = FaceDetect.resultBitmap;
        //You can change original image here and draw anything you want to be masked on it.
        Bitmap result = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas tempCanvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        tempCanvas.drawBitmap(Bitmap.createScaledBitmap(bitmap, mask.getWidth(), mask.getHeight(),false),0,0,new Paint());
        tempCanvas.drawBitmap(mask, 0, 0, paint);
        paint.setXfermode(null);

        result = Bitmap.createScaledBitmap(result, PixelRate,PixelRate,false);
        result = Bitmap.createScaledBitmap(result,mask.getWidth(), mask.getHeight(),_isFilter);
        //Draw result after performing masking
        Bitmap temp = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(temp);
        canvas.drawBitmap(image, 0,0,new Paint());
        canvas.drawBitmap(result, 0, 0, new Paint());
        return temp;
    }
    //Image to grayscale
    public static Bitmap toGrayscale(Bitmap bmpOriginal) {
        final int height = bmpOriginal.getHeight();
        final int width = bmpOriginal.getWidth();

        final Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        final Canvas c = new Canvas(bmpGrayscale);
        final Paint paint = new Paint();
        final ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        final ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }
    //Save image to gallery
    private Uri saveImage() {
        if(_isDebug)Log.e("SAVE","Pixel_"+image_name+".jpg");
        Uri temp = Uri.parse(MediaStore.Images.Media.insertImage(this.getContentResolver(), image_processed ,"Pixel_"+image_name+".jpg", null));
        Toast.makeText(this, R.string.toast_save,Toast.LENGTH_LONG).show();
        return temp;
    }
    //Share and save image to another app
    public void Share(){
        if(imageAfterSave==null) {
            Toast.makeText(this, R.string.message_savefirst,Toast.LENGTH_LONG).show();
        }else {
            Uri photoFile = imageAfterSave;
            final Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/jpg");
            shareIntent.putExtra(Intent.EXTRA_STREAM, photoFile);
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.message));
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_title)));
        }
    }
    //Request permission
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    imageView.setEnabled(true);
                    image_picker.setEnabled(true);
                } else {
                    imageView.setEnabled(false);
                    image_picker.setEnabled(false);
                }
            });
    //Calculate byte size bitmap to kb
    public static float byteSizeOf(Bitmap bitmap) {
        return ((float)bitmap.getAllocationByteCount())/1024/100;
    }
}
