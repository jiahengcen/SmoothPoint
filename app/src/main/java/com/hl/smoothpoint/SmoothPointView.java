package com.hl.smoothpoint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 黄龙 on 2017/5/3.
 */

public class SmoothPointView extends View {
    private Paint mPointPaint;
    private Paint mLinePaint;
    private Path mPath;
    private List<Point> mPointList;

    public SmoothPointView(Context context) {
        super(context);
    }

    public SmoothPointView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initSmoothPointView();
        test();
    }

    private void test() {
        mPointList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Point point = new Point();
            point.x = i * 90+30;
            point.y = (int) (100+Math.random() * 500);
            mPointList.add(point);
        }

    }

    private void initSmoothPointView() {
        mPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPointPaint.setColor(Color.RED);
        mPointPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPointPaint.setStrokeWidth(5);
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(Color.BLUE);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(5);
        mPath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawPoint(canvas);
        drawLine(canvas);
    }

    /**
     * 第一控制点到当前点距离的比例
     * 取值范围0-1 且小于等于 secondControlPointDistance
     */
    private static final float firstControlPointDistance = 0.3f;
    /**
     * 第二控制点到当前点距离的比例
     * 取值范围0-1
     */
    private static final float secondControlPointDistance = 0.6f;
    /**
     *
     */
    private static final float KRate = 0.5f;

    private void drawLine(Canvas canvas) {
        //
        float K = 0f;
        float currentPointX;
        float currentPointY;
        float nextPointX = Float.NaN;
        float nextPointY = Float.NaN;

        float nextNextPointY = Float.NaN;

        boolean hasNextPoint = false;
        boolean hasNextNextPoint = false;
        if(firstControlPointDistance>secondControlPointDistance){
           throw new RuntimeException("firstControlPointDistance must small then secondControlPointDistance");
        }
        mPath.moveTo(mPointList.get(0).x, mPointList.get(0).y);

        for (int i = 0; i < mPointList.size(); i++) {

            currentPointX = mPointList.get(i).x;
            currentPointY = mPointList.get(i).y;
            if (i < mPointList.size() - 1) {
                nextPointX = mPointList.get(i + 1).x;
                nextPointY = mPointList.get(i + 1).y;
                hasNextPoint = true;
            } else {
                hasNextPoint = false;
            }
            if (i < mPointList.size() - 2) {
                nextNextPointY = mPointList.get(i + 2).y;
                hasNextNextPoint = true;
            } else {
                hasNextNextPoint = false;
            }
            float firstControlPointX, firstControlPointY, secondControlPointX, secondControlPointY;

            if (hasNextPoint) {
                if (K == 0f) {
                    firstControlPointX = currentPointX + (nextPointX - currentPointX) * firstControlPointDistance;
                    firstControlPointY = currentPointY;
                } else {
                    float tempX = getKLineX(currentPointX, currentPointY, KRate, nextPointY);
                    float tempY = getKLineY(currentPointX, currentPointY, KRate, nextPointX);
                    if (Math.abs(tempX - currentPointX) - Math.abs(nextPointX - currentPointX) > 0) {
                        tempX = nextPointX;
                    }
                    if (Math.abs(tempY - currentPointY) - Math.abs(nextPointY - currentPointY) > 0) {
                        tempY = nextPointY;
                    }
                    firstControlPointX = currentPointX + (tempX - currentPointX) * firstControlPointDistance;
                    firstControlPointY = currentPointY + (tempY - currentPointY) * firstControlPointDistance;

                }
                if (hasNextNextPoint) {
                    if (nextPointY - currentPointY > 0 && nextNextPointY - nextPointY > 0) {
                        K = KRate;
                    } else if (nextPointY - currentPointY < 0 && nextNextPointY - nextPointY < 0) {
                        K = -1 * KRate;
                    } else {
                        K = 0f;
                    }

                    if (K == 0f) {
                        secondControlPointX = currentPointX + (nextPointX - currentPointX) * secondControlPointDistance;
                        secondControlPointY = nextPointY;
                    } else {
                        float tempX = getKLineX(nextPointX, nextPointY, KRate, currentPointY);
                        float tempY = getKLineY(nextPointX, nextPointY, KRate, currentPointX);
                        if (Math.abs(tempX - nextPointX) - Math.abs(nextPointX - currentPointX) > 0) {
                            tempX = currentPointX;
                        }
                        if (Math.abs(tempY - nextPointY) - Math.abs(nextPointY - currentPointY) > 0) {
                            tempY = currentPointY;
                        }
                        secondControlPointX = tempX + (nextPointX - tempX) * secondControlPointDistance;
                        secondControlPointY = tempY + (nextPointY - tempY) * secondControlPointDistance;
                    }
                } else {
                    secondControlPointX = currentPointX + (nextPointX - currentPointX) * secondControlPointDistance;
                    secondControlPointY = nextPointY;
                }

                mPath.cubicTo(firstControlPointX, firstControlPointY, secondControlPointX, secondControlPointY, nextPointX, nextPointY);
            }

            canvas.drawPath(mPath,mLinePaint);
        }

    }

    private void drawControlPoint(Canvas canvas, float PointX, float PointY) {
        canvas.drawCircle(PointX, PointY, 10, mLinePaint);
    }

    /**
     * @param currentX 当前X
     * @param currentY 当前Y
     * @param k        斜率
     * @param pointX   x值
     * @return 获取经过 currentX currentY 斜率为 K 对应 pointY 的值
     */
    private float getKLineY(float currentX, float currentY, float k, float pointX) {
        return k * pointX - k * currentX + currentY;
    }

    /**
     * @param currentX 当前X
     * @param currentY 当前Y
     * @param k        斜率(斜率不应该为0）
     * @param pointY   y值
     * @return 获取经过 currentX currentY 斜率为 K 对应 pointx 的值
     */
    private float getKLineX(float currentX, float currentY, float k, float pointY) {
        if (k == 0f) {
            return currentX;
        }
        return (pointY - currentY) / k + currentX;
    }

    private void drawPoint(Canvas canvas) {
        for (Point point : mPointList) {
            canvas.drawCircle(point.x, point.y, 10, mPointPaint);
        }
    }

}
