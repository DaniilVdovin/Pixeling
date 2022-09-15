package com.daniilvdovin.pixelit;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    //TODO: Editor
    //System
    private static final int RESULT_LOAD_IMG = 1;
    private static final int SCALESIZE = 40;
    private static final int PIXEL = 8;
    Bitmap image;
    int ResultSize = 0;

    //UI
    ImageView imageView;
    Button reset,save;
    //UI-Parameters
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch s_grid,s_gray,s_dot,s_filter;
    SeekBar sb_pixelRate;
    //UI-Text
    TextView t_pixelSize,t_imageSize,t_pixelRate;

    //Parameters
    boolean _isDots = false;
    boolean _isGrid = false;
    boolean _isGray = false;
    boolean _isFilter = false;
    boolean _isScanColor = false;
    int ScaleSize = 30; //30
    int PixelRate = 48; //48

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
        s_gray = findViewById(R.id.s_gray);
        s_grid = findViewById(R.id.s_grid);
        s_dot = findViewById(R.id.s_dot);
        s_filter = findViewById(R.id.s_filter);
        sb_pixelRate = findViewById(R.id.sb_pixelRate);
        t_pixelSize = findViewById(R.id.t_pixelSize);
        t_imageSize = findViewById(R.id.t_ImageSize);
        t_pixelRate = findViewById(R.id.t_pixel_rate);


        //Image Load
        imageView.setOnClickListener(view->{
            startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
        });
        //Pixelating
        reset.setOnClickListener(view -> {
        });
        //UI-Logic
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
        sb_pixelRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(i==0)i=1;
                ScaleSize = SCALESIZE/(i>4?4:i);
                //Log.e("ScaleSaze","i:"+i+" ScaleSize:"+ScaleSize+" f:"+(i>2?i/2:i));
                PixelRate = PIXEL*i;
                t_pixelRate.setText(getText(R.string.p_r)+": "+i);
                t_pixelSize.setText(getText(R.string.p_s)+": "+PixelRate+"x"+PixelRate);
                t_imageSize.setText(getText(R.string.i_s)+": "+(PixelRate*ScaleSize)+1+"x"+(PixelRate*ScaleSize)+1);
                refreshImage(image);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        Parameters_ShowHide(image != null);
    }
    void Parameters_ShowHide(boolean _is){
        reset.setEnabled(_is);
        save.setEnabled(_is);
        s_gray.setEnabled(_is);
        s_grid.setEnabled(_is);
        s_dot.setEnabled(_is);
        s_filter.setEnabled(_is);
        sb_pixelRate.setEnabled(_is);
    }
    void refreshImage(Bitmap image){
        imageView.setImageBitmap(pixelit_b(image));
    }
    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                image = BitmapFactory.decodeStream(imageStream);
                imageView.setImageBitmap(image);
                Parameters_ShowHide(image != null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        }else {
            Toast.makeText(this, "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }
    Bitmap pixelit_b(Bitmap bitmap){
        //x30
        bitmap = Bitmap.createScaledBitmap(bitmap,PixelRate,PixelRate,false);
        bitmap = Bitmap.createScaledBitmap(bitmap,(PixelRate*ScaleSize)+1,(PixelRate*ScaleSize)+1,_isFilter);
        if(_isGrid){
            for (int i = 0; i < bitmap.getWidth(); i++) {
                if(i%ScaleSize==0)
                for (int j = 0; j < bitmap.getHeight(); j++) {
                    bitmap.setPixel(i, j, Color.GRAY);
                    bitmap.setPixel(j, i, Color.GRAY);
                }
            }
        }
        if(_isDots) {
            for (int i = (ScaleSize / 2); i < bitmap.getWidth(); i += ScaleSize) {
                for (int j = (ScaleSize / 2); j < bitmap.getHeight(); j += ScaleSize) {
                    int c = Color.BLACK;
                    if(_isScanColor) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            c = bitmap.getPixel(i, j);
                            //Log.e("color", "[" + i + "," + j + "]:" + Color.valueOf(c).toString());
                        }
                        c = Color.BLACK;
                    }
                    if(ScaleSize<20) {
                        bitmap.setPixel(i - 1, j - 1, c);
                        bitmap.setPixel(i + 1, j - 1, c);
                        bitmap.setPixel(i - 1, j, c);
                        bitmap.setPixel(i, j - 1, c);
                        bitmap.setPixel(i, j, c);
                        bitmap.setPixel(i + 1, j, c);
                        bitmap.setPixel(i, j + 1, c);
                        bitmap.setPixel(i + 1, j + 1, c);
                        bitmap.setPixel(i - 1, j + 1, c);
                        bitmap.setPixel(i + 1, j - 1, c);
                    }else{
                        bitmap.setPixel(i, j, c);
                    }
                }
            }
        }
        if(_isGray)
            bitmap = toGrayscale(bitmap);
        return bitmap;
    }
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

}