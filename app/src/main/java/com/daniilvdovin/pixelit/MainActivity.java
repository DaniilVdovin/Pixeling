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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    //System
    private static final int RESULT_LOAD_IMG = 1;
    Bitmap image;

    //UI
    ImageView imageView;
    Button reset,save;
    //UI-Parameters
    Switch s_grid,s_gray,s_dot;
    //UI-Text
    TextView t_pixelSize,t_imageSize,t_pixelRate;

    //Parameters
    boolean _isDots = false;
    boolean _isGrid = false;
    boolean _isGray = false;
    int ScaleSize = 50; //30
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

        //Image Load
        imageView.setOnClickListener(view->{
            startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
        });

        //Pixelating
        reset.setOnClickListener(view -> {
            imageView.setImageBitmap(pixelit_b(image));
        });


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
        bitmap = Bitmap.createScaledBitmap(bitmap,(PixelRate*ScaleSize)+1,(PixelRate*ScaleSize)+1,false);
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
            for (int i = (ScaleSize / 2); i < bitmap.getWidth() - ScaleSize; i += ScaleSize) {
                for (int j = (ScaleSize / 2); j < bitmap.getHeight() - ScaleSize; j += ScaleSize) {
                    int c = Color.BLACK;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        c = bitmap.getPixel(i, j);
                        Log.e("color", "[" + i + "," + j + "]:" + Color.valueOf(c).toString());
                    }
                    c = Color.BLACK;
                    bitmap.setPixel(i - 1, j - 1, c);
                    bitmap.setPixel(i - 1, j, c);
                    bitmap.setPixel(i, j - 1, c);
                    bitmap.setPixel(i, j, c);
                    bitmap.setPixel(i + 1, j, c);
                    bitmap.setPixel(i, j + 1, c);
                    bitmap.setPixel(i + 1, j + 1, c);
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