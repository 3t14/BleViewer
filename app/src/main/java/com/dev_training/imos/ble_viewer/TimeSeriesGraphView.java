package com.dev_training.imos.ble_viewer;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import static com.dev_training.imos.ble_viewer.MyLeDevicesModel.*;

/**
 * Characteristicの時系列データのグラフを描画
 */
public class TimeSeriesGraphView extends View {

    private static final String TAG = "TimeSeriesGraphView";
    Context mContext;
    MyLeDevicesModel mMyLeDevicesModel;
    int mWidth, mHeight;
    int padding = 10; // 四方の内部余白
    int contentPadding = 48;// グラフ描画部四方の余白

    String deviceAddress;
    BluetoothGattCharacteristic mCurrentCharacteristic;
    ValueWithTimestampQueue mValueTimeSeries; // 描画対象の時系列データ

    public TimeSeriesGraphView(Context context){
        super(context);
    }
    public TimeSeriesGraphView(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public TimeSeriesGraphView(Context context, MyLeDevicesModel myLeDevicesModel) {
        super(context);
        this.mContext = context;
        this.mMyLeDevicesModel = myLeDevicesModel;
        setPadding(padding, padding, padding, padding);
    }

    /**
     * 指定したデバイスの指定したCharacteristicのグラフ描画設定
     * @param address
     * @param characteristic
     */
    public void setCharacteristic(String address, BluetoothGattCharacteristic characteristic){
        deviceAddress = address;
        mValueTimeSeries = (ValueWithTimestampQueue) mMyLeDevicesModel.getDeviceInfo(
                address, characteristic.getUuid().toString() + PROPERTY_KEY_TIME_SERIES);
        mCurrentCharacteristic = characteristic;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(Color.WHITE);
        canvas.drawRect(0,0, mWidth, mHeight, fillPaint);

        String title = "Data is not selected...";

        // 表示名
        if (mCurrentCharacteristic != null) {
            title = UUIDs.getUuidTitle(mCurrentCharacteristic.getUuid());
            mValueTimeSeries = (ValueWithTimestampQueue) mMyLeDevicesModel.getDeviceInfo(
                    deviceAddress, mCurrentCharacteristic.getUuid().toString() + PROPERTY_KEY_TIME_SERIES);
        }

        //Log.v(TAG, "onDraw size = " + mWidth + ", " + mHeight);
        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLUE);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(16.0f);


        // 中央位置に描画するため文字サイズを取得
        float textWidth = textPaint.measureText(title);
        float textHeight = textPaint.getTextSize();
        // Draw the text.
        canvas.drawText(title,
                (mWidth) / 2 - textWidth / 2,
                textHeight,
                textPaint);

        drawAxis(canvas);
        drawData(canvas);

    }

    /**
     * X軸、Y軸を描画
     * @param canvas
     */
    private void drawAxis(Canvas canvas) {


        // 軸の線の設定
        Paint linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(1.0f);
        linePaint.setAntiAlias(true);


        // 下側にX軸(t)を描画
        // (contentPadding, mHeight - contentPadding, mWidth - contentPadding, contentPadding)
        canvas.drawLine(contentPadding, mHeight - contentPadding, mWidth - contentPadding, mHeight - contentPadding, linePaint);

        // 軸の数値用Paint
        Paint textPaint = new Paint();
        textPaint.setTextSize(10.0f);
        // 細軸用Paint
        Paint thinPaint = new Paint();
        thinPaint.setStrokeWidth(1.0f);
        thinPaint.setColor(Color.GRAY);
        thinPaint.setAntiAlias(true);
        // 描画データを取得
        // 存在しない場合は戻る
        if (mValueTimeSeries == null || mValueTimeSeries.size() == 0) return;

        String timestamp = "";
        int numOfXAxisDivision = 5;
        int numOfYAxisDivision = 5;


        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        long beginTime = mValueTimeSeries.getBeginTimestamp();
        long endTime = mValueTimeSeries.getEndTimestamp();

        int timeLength = (int)(endTime - beginTime);

        // 開始時刻から終了時刻までを5当分して時刻を表示
        for (int i = 0; i < numOfXAxisDivision; i++) {

            float positionRatio = (float)i / (numOfXAxisDivision - 1.0f);

            long  currentTime = beginTime + (long) (timeLength * positionRatio);
            float positionX = contentPadding
                    + positionRatio * (mWidth - 2*contentPadding); // グラフコンテンツの幅

            // 終了時刻を描画
            timestamp = sdf.format( new Date(currentTime));

            // 描画位置
            float textWidth = textPaint.measureText(timestamp);
            float textHeight = textPaint.getTextSize();

            canvas.drawText(timestamp,
                    positionX - textWidth/2,
                    mHeight - contentPadding + textHeight*2, textPaint);

            if (i>0)
                canvas.drawLine(positionX, contentPadding,
                        positionX, mHeight-contentPadding, thinPaint);
        }


        // 左側にY軸を描画
        // (contentPadding, mHeight - contentPadding, contentPadding, contentPadding)
        canvas.drawLine(contentPadding, mHeight - contentPadding, contentPadding, contentPadding, linePaint);

        // 最小値、最大値を取得
        int maxValue = mValueTimeSeries.getMaxValue();
        int minValue = mValueTimeSeries.getMinValue();

        //
        int diffValue = (int)(maxValue - minValue);

        for (int i = 0; i < numOfYAxisDivision; i++) {
            float positionRatio = (float)i / (numOfYAxisDivision - 1.0f);

            float currentValue = minValue + (float)diffValue * positionRatio;
            float positionY = mHeight - contentPadding // 開始位置のY座標
                    - positionRatio
                    * (mHeight - 2*contentPadding); // グラフコンテンツの高さ

            String currentValueString = String.format("%.2f  ", currentValue);

            float textWidth = textPaint.measureText(currentValueString);
            float textHeight = textPaint.getTextSize();
            canvas.drawText(currentValueString,
                    contentPadding - textWidth,
                    positionY, textPaint);
         }
    }

    /**
     * グラフデータを描画
     * @param canvas
     */
    private void drawData(Canvas canvas) {

        // データが存在しない場合は戻る
        if (mValueTimeSeries == null || mValueTimeSeries.size() == 0) return;

        //  java.util.ConcurrentModificationException対策でコピー

        ValueWithTimestampQueue currentValueTimeSeries;
        synchronized (mValueTimeSeries){
            currentValueTimeSeries = (ValueWithTimestampQueue) mValueTimeSeries.clone();
        }



        // 開始終了時刻
        long beginTime = currentValueTimeSeries.getBeginTimestamp();
        long endTime = currentValueTimeSeries.getEndTimestamp();

        int timeLength = (int)(endTime - beginTime);

        // 最小値、最大値を取得
        int maxValue = currentValueTimeSeries.getMaxValue();
        int minValue = currentValueTimeSeries.getMinValue();


        // 種別から、サンプル数を算出
        int numOfData = 1;
        switch(currentValueTimeSeries.getValueType()){
            case UUIDs.VALUE_TYPE_1_BYTE_3_SAMPLE:
                numOfData = 3;
                break;
            case UUIDs.VALUE_TYPE_1_BYTE_2_SAMPLE:
                numOfData = 2;
                break;
        }

        int[] colors = new int[]{Color.BLUE, Color.RED, Color.GREEN};
        // グラフ用ペイント
        Paint linePaint = new Paint();
        linePaint.setStrokeWidth(1.0f);
        linePaint.setAntiAlias(true);
        for (int i=0; i<numOfData; i++){
            linePaint.setColor(colors[i]);

            float prevPositionX = contentPadding;
            float prevPositionY = mHeight - contentPadding;

             for (ValueWithTimestamp valueWithTimestamp: currentValueTimeSeries){
                // X座標の算出
                float positionXRatio = (float)(valueWithTimestamp.timestamp-beginTime) / (float)(endTime - beginTime);
                float positionX = contentPadding
                        + positionXRatio * (mWidth - 2*contentPadding); // グラフコンテンツの幅
                //Log.v(TAG, "endTime = "+(endTime- beginTime) +" "+valueWithTimestamp.timestamp + " PositionX = "+positionX);

                // Y座標の算出
                int values[] = currentValueTimeSeries.getIntValues(valueWithTimestamp.value);
                int currentValue = values[i];

                float positionYRatio = (float)(currentValue - minValue)/ (float)(maxValue - minValue);

                float positionY = mHeight - contentPadding -  (float)(mHeight - 2*contentPadding) * positionYRatio;
                //Log.v(TAG, "currentValue = "+currentValue+", "+minValue+", "+maxValue+", "+positionYRatio+","+positionY);


                canvas.drawLine(prevPositionX, prevPositionY, positionX, positionY, linePaint);
                prevPositionX = positionX;
                prevPositionY = positionY;
            }

        }

    }



}
