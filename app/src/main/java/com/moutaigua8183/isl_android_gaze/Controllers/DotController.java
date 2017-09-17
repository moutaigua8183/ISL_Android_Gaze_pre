package com.moutaigua8183.isl_android_gaze.Controllers;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.moutaigua8183.isl_android_gaze.R;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Mou on 9/15/2017.
 */

public class DotController {

    public static final int POINT_TYPE_LEFT = 0;
    public static final int POINT_TYPE_RIGHT = 1;
    private final int ROW_COL_POINT_NUM = 4;
    private Context ctxt;
    private int SCREEN_WIDTH;
    private int SCREEN_HEIGHT;
    private ArrayList<Point> pointCandidates;
    private FrameLayout dotHolderLayout;
    private TextView dotTextView;
    private Point   currPoint;
    private int     currPointType;




    public DotController(Context context, int[] screenSize){
        ctxt = context;
        SCREEN_WIDTH = screenSize[0];
        SCREEN_HEIGHT = screenSize[1];
        pointCandidates = new ArrayList<>();
        initPointsCandidates();
        dotHolderLayout = null;
        currPoint = new Point();
        currPoint.set(-1, -1);
        currPointType = -1;
    }

    public void showNext(){
        generateDot();
        showDot();
    }

    public void setDotHolderLayout(FrameLayout frameLayout){
        dotHolderLayout = frameLayout;
    }

    public Point getCurrPoint(){
        return currPoint;
    }

    public int getCurrPointType(){
        return currPointType;
    }



    /**
     * init point candidates
     */
    private void initPointsCandidates(){
        if( null!=pointCandidates ) {
            int width_interval = (SCREEN_WIDTH / 100) * 100 / (ROW_COL_POINT_NUM + 1);
            int height_interval = (SCREEN_HEIGHT / 100) * 100 / (ROW_COL_POINT_NUM + 1);
            for (int i = 1; i <= ROW_COL_POINT_NUM; ++i) {
                for (int j = 1; j <= ROW_COL_POINT_NUM; ++j) {
                    pointCandidates.add(new Point(i * height_interval, j * width_interval));
                }
            }
        }
    }


    /**
     * randomly generate a dot, not the same as the previous one
     */
    private void generateDot(){

        int randIndex;
        do{
            randIndex = new Random().nextInt( pointCandidates.size() );
        } while( pointCandidates.get(randIndex).equals(currPoint.x, currPoint.y) );
        currPoint = pointCandidates.get(randIndex);
        currPointType = new Random().nextInt(2);
    }


    /**
     * show the dot
     */
    private void showDot(){
        dotHolderLayout.removeAllViews();
        dotTextView = new TextView(ctxt);
        dotTextView.setBackgroundResource(R.drawable.dot_r20);
        dotTextView.setText( currPointType==POINT_TYPE_LEFT ? "L":"R" );
        dotTextView.setTextAlignment( TextView.TEXT_ALIGNMENT_CENTER );
//        dotImageView.post(new Runnable() {
//            @Override
//            public void run() {
//                AnimationDrawable animationDrawable = (AnimationDrawable)dotImageView.getBackground();
//                animationDrawable.setOneShot(false);
//                animationDrawable.start();
//            }
//        });
        //setting image position
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = currPoint.x;
        params.topMargin = currPoint.y;
        dotTextView.setLayoutParams(params);
        //adding view to layout
        dotHolderLayout.addView(dotTextView);

    }






}
