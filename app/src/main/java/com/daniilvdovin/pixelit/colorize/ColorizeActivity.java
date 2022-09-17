package com.daniilvdovin.pixelit.colorize;

import static com.daniilvdovin.pixelit.Data.PixelRate;
import static com.daniilvdovin.pixelit.Data.ScaleSize;
import static com.daniilvdovin.pixelit.Data.colors;
import static com.daniilvdovin.pixelit.Data.image_processed;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daniilvdovin.pixelit.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

class mPixel extends View{
    int width;
    int height;
    int color;
    String text;
    boolean coloring=false;
    public mPixel(Context context, int color,int size) {
        super(context);
        this.color = color;
        this.width = size;
        this.height = size;
    }
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Paint r = new Paint();
        r.setStyle(Paint.Style.STROKE);
        r.setColor(Color.GRAY);

        Paint f = new Paint();
        f.setStyle(Paint.Style.FILL);
        f.setColor(color);

        if(coloring)canvas.drawRect(0,0,width,height,f);
        canvas.drawRect(0,0,width,height,r);

        Paint tx = new Paint();
        tx.setStyle(Paint.Style.FILL);
        tx.setTextSize(20);
        tx.setColor(Color.BLACK);
        if(!coloring)canvas.drawText(text,(int)(width-(width/2)),(int)(height-(height/2f)),tx);
    }
}
public class ColorizeActivity extends AppCompatActivity {

    GridLayout layout;
    LinearLayout paliter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colorize);

        layout = findViewById(R.id.grid);
        paliter = findViewById(R.id.palitre);

        layout.setColumnCount(10);
        layout.removeAllViews();

        Log.e("itemFF",image_processed.getWidth()+"x"+image_processed.getHeight());

        List<mPixel> pixelList = new ArrayList<>();

        for (int i =0;i<colors.size();i++) {
            PixelData p = colors.get(i);
            Log.e("item",p.x+"x"+p.y);
            int size = 140;
            mPixel t = new mPixel(this,p.color,size);
            GridLayout.LayoutParams param =new GridLayout.LayoutParams();
            param.height =param.width = size;
            t.text = p.descript;
            t.setLayoutParams(param);
            t.setOnClickListener(view -> {
                t.coloring = !t.coloring;
                Toast.makeText(this,p.descript,Toast.LENGTH_SHORT).show();
                t.invalidate();
            });
            pixelList.add(t);
            layout.addView(t);
        }
        List<Integer> colors_palitre = new ArrayList<>();
        for (int i =0;i<colors.size();i++) {
            boolean result = true;
            for (int p:colors_palitre) {
                if(String.valueOf(colors.get(i).color).substring(1, 2).equals(String.valueOf(p).substring(1, 2))) {
                    result = false;
                    break;
                }
            }
            if(result)
                colors_palitre.add(colors.get(i).color);
        }
        for (int i = 0; i < colors_palitre.size(); i++) {
            TextView t = new TextView(this);
            t.setText(""+colors_palitre.get(i));
            t.setBackgroundColor(colors_palitre.get(i));
            t.setWidth(140);
            t.setHeight(140);

            t.setPadding(5,0,5,0);
            paliter.addView(t);
        }
    }

}