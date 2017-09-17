package com.moutaigua8183.isl_android_gaze.Controllers;

import android.content.Context;
import android.graphics.Point;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.moutaigua8183.isl_android_gaze.R;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Mou on 9/15/2017.
 */

public class DotController {


    private final int ROW_COL_POINT_NUM = 4;
    private ArrayList<Point> pointCandidates;
    private Context ctxt;
    private FrameLayout dotHolderLayout;
    private Point currPoint;
    private int SCREEN_WIDTH;
    private int SCREEN_HEIGHT;



    public DotController(Context context, int[] screenSize){
        ctxt = context;
        SCREEN_WIDTH = screenSize[0];
        SCREEN_HEIGHT = screenSize[1];
        pointCandidates = new ArrayList<>();
        initPointsCandidates();
        dotHolderLayout = null;
        currPoint = null;
        SCREEN_WIDTH = -1;
        SCREEN_HEIGHT = -1;
    }

    public void showNext(){
        int numOfCandidates = pointCandidates.size();
        int randIndex = new Random().nextInt(numOfCandidates);
        currPoint = pointCandidates.get(randIndex);
        showDot();
    }

    public void setDotHolderLayout(FrameLayout frameLayout){
        dotHolderLayout = frameLayout;
    }

    public Point getCurrPoint(){
        return currPoint;
    }


    // init point candidates
    private void initPointsCandidates(){
        if( null!=pointCandidates ) {
            int width_interval = (SCREEN_WIDTH % 100) * 100 / (ROW_COL_POINT_NUM + 1);
            int height_interval = (SCREEN_HEIGHT % 100) * 100 / (ROW_COL_POINT_NUM + 1);
            for (int i = 1; i <= ROW_COL_POINT_NUM; ++i) {
                for (int j = 1; j <= ROW_COL_POINT_NUM; ++j) {
                    pointCandidates.add(new Point(i * height_interval, j * width_interval));
                }
            }
        }
    }


    // show a dot
    private void showDot(){
        //LinearLayOut Setup
        dotHolderLayout.removeAllViews();
        //ImageView Setup
        ImageView imageView = new ImageView(ctxt);
        //setting image resource
        imageView.setImageResource(R.drawable.dot);
        //setting image position
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = currPoint.x;
        params.topMargin = currPoint.y;
        imageView.setLayoutParams(params);
        //adding view to layout
        dotHolderLayout.addView(imageView);
    }






}
