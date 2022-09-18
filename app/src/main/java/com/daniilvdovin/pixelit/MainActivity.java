package com.daniilvdovin.pixelit;

import static com.daniilvdovin.pixelit.Data.PixelRate;
import static com.daniilvdovin.pixelit.Data.ScaleSize;
import static com.daniilvdovin.pixelit.Data._isDots;
import static com.daniilvdovin.pixelit.Data._isFilter;
import static com.daniilvdovin.pixelit.Data._isGalleryOpen;
import static com.daniilvdovin.pixelit.Data._isGoogleAds_DebugDevice;
import static com.daniilvdovin.pixelit.Data._isGray;
import static com.daniilvdovin.pixelit.Data._isGrid;
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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
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
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.LongFunction;

public class MainActivity extends AppCompatActivity {
    //TODO: Editor
    //System
    private static final int RESULT_LOAD_IMG = 1;
    private static final int RESULT_CROP_IMG = 2;
    private static final int SCALESIZE = 60;//50
    private static final int PIXEL = 8;
    int ResultSize = 0;

    //UI
    ImageView imageView;
    Button reset,save,imagepicker,share;
    ProgressBar progressBar;
    //UI-Parameters
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch s_grid,s_gray,s_dot,s_filter;
    SeekBar sb_pixelRate;
    //UI-Text
    TextView t_pixelSize,t_imageSize,t_pixelRate;

    //Google ads frame
    View AdFrame;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Init system
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        //Init UI
        imageView = findViewById(R.id.imageView);
        reset = findViewById(R.id.b_reset);
        save = findViewById(R.id.b_save);
        share = findViewById(R.id.b_share);
        imagepicker = findViewById(R.id.b_imagepick);
        s_gray = findViewById(R.id.s_gray);
        s_grid = findViewById(R.id.s_grid);
        s_dot = findViewById(R.id.s_dot);
        s_filter = findViewById(R.id.s_filter);
        sb_pixelRate = findViewById(R.id.sb_pixelRate);
        t_pixelSize = findViewById(R.id.t_pixelSize);
        t_imageSize = findViewById(R.id.t_ImageSize);
        t_pixelRate = findViewById(R.id.t_pixel_rate);
        progressBar = findViewById(R.id.progressBar);
        AdFrame = findViewById(R.id.AdFrame);
        imagepicker.setVisibility(View.VISIBLE);
        //UI PreSetup
        if(image==null) {
            _isGalleryOpen = false;
            imagepicker.setEnabled(false);
            imageView.setEnabled(false);
            progressBar.setVisibility(View.GONE);
        }else{
            imagepicker.setVisibility(View.GONE);
            refreshImage(image);
        }
        AdFrame.setVisibility(View.GONE);
        //Google Ads
        if(_isGoogleAds) {
            MobileAds.initialize(this, view->{
                if(_isDebug)Log.e("ADS","Ad Initialize");
            });
            if(_isGoogleAds_DebugDevice){
                RequestConfiguration configuration = new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("70F94B3F952979B29EA2674BF7D27490")).build();
                MobileAds.setRequestConfiguration(configuration);
            }
            AdManagerAdView adView = findViewById(R.id.adManagerAdView);
            AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();
            adView.loadAd(adRequest);
            if(_isDebug && adRequest.isTestDevice(this))Log.e("ADS","Load On Test Devices");
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    if(_isDebug)Log.e("ADS","Ad Closed");
                    AdFrame.setVisibility(View.GONE);
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    if(_isDebug)Log.e("ADS","Ad Failed To Load");
                    AdFrame.setVisibility(View.GONE);
                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    if(_isDebug)Log.e("ADS","Ad Loaded");
                    AdFrame.setVisibility(View.VISIBLE);
                }
            });
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
            imagepicker.setEnabled(true);
        } else {
            imageView.setEnabled(false);
            imagepicker.setEnabled(false);
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
        imagepicker.setOnClickListener(ip);
        imageView.setOnClickListener(ip);

        //UI-Logic
        //UI-Logic-Button
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
                ScaleSize = SCALESIZE;///(i>4?4:i);
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
    //Off button if don't have loaded image
    void Parameters_ShowHide(boolean _is){
        reset.setEnabled(_is);
        save.setEnabled(_is);
        share.setEnabled(_is);
        s_gray.setEnabled(_is);
        s_grid.setEnabled(_is);
        s_dot.setEnabled(_is);
        s_filter.setEnabled(_is);
        sb_pixelRate.setEnabled(_is);
    }
    //Update image in ImageView
    void refreshImage(Bitmap image){
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
                            Toast.makeText(this, R.string.notfound, Toast.LENGTH_SHORT);
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
    @SuppressLint({"SetTextI18n", "DefaultLocale", "StaticFieldLeak"})
    Bitmap pixelit_b(Bitmap bitmap){
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
                //Transfer image to grayscale
                if(_isGray)
                    bitmap = toGrayscale(bitmap);
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
                image_processed = bitmap;
                progressBar.setVisibility(View.GONE);
                imageView.setImageBitmap(image_processed);
                t_imageSize.setText(getText(R.string.i_s)+"\n"+bitmap.getWidth()+"x"+bitmap.getHeight());
                save.setText(getText(R.string.save)+" ±("+ String.format("%.2f", byteSizeOf(bitmap)) +" kb)");
            }
        };
        //Start thread processing
        processing.execute(bitmap);
        return image_processed;
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
                    imagepicker.setEnabled(true);
                } else {
                    imageView.setEnabled(false);
                    imagepicker.setEnabled(false);
                }
            });
    //Calculate byte size bitmap to kb
    public static float byteSizeOf(Bitmap bitmap) {
        return ((float)bitmap.getAllocationByteCount())/1024/100;
    }
}