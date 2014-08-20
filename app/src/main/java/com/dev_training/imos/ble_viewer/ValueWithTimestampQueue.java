package com.dev_training.imos.ble_viewer;

import android.renderscript.Sampler;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by @3t14 on 2014/08/20.
 */
public class ValueWithTimestampQueue extends LinkedList < ValueWithTimestamp > {
    private int valueType;

    public ValueWithTimestampQueue(int valueType) {
        this.valueType = valueType;
    }

    public ValueWithTimestampQueue(Collection<? extends ValueWithTimestamp> collection, int valueType) {
        super(collection);
        this.valueType = valueType;
    }

    @Override
    synchronized public boolean offer(ValueWithTimestamp o) {
        return super.offer(o);
    }

    @Override
    synchronized public Object clone() {
        return super.clone();
    }

    /**
     * 値の種別を取得
     * @return
     */
    public int getValueType() {
        return valueType;
    }

    /**
     * 最初のタイムスタンプを取得する
     * @return
     */
    public long getBeginTimestamp(){
        ValueWithTimestamp valueWithTimestamp = this.getFirst();
        if (valueWithTimestamp == null) return 0;
        return valueWithTimestamp.timestamp;
    }

    /**
     * 最後のタイムスタンプを取得する
     * @return
     */
    public long getEndTimestamp(){
        ValueWithTimestamp valueWithTimestamp = this.getLast();
        if (valueWithTimestamp == null) return 0;
        return valueWithTimestamp.timestamp;
    }


    /**
     * バイト配列データからvalueTypeに応じてint型を取得
     * @param data
     * @return
     */
    public int[] getIntValues(byte[] data) {
        if ( data == null ) return null;

        int intValues[];
        switch (valueType){
            case UUIDs.VALUE_TYPE_1_BYTE_1_SAMPLE:
                intValues = new int[1];
                intValues[0] = data[0];
                break;
            case UUIDs.VALUE_TYPE_1_BYTE_3_SAMPLE:
                intValues = new int[3];
                intValues[0] = data[0];
                intValues[1] = data[1];
                intValues[2] = data[2];
                break;
            case UUIDs.VALUE_TYPE_UNKNOWN:
            default:
                // 1サンプルとして返却
                intValues = new int[1];
                for (int i = 0; i < data.length; i++){
                    intValues[0] += data[i] << i*8;
                }
        }
        return intValues;
    }
    /**
     * 最大値を取得
     * @return
     */
    synchronized public int getMaxValue(){
        int maxValue = Integer.MIN_VALUE;
        for (ValueWithTimestamp valueWithTimestamp: this){
            int[] intValues = getIntValues(valueWithTimestamp.value);
            if (intValues == null) continue;
            // それぞれの要素に対して比較
            for (int i=0; i < intValues.length; i++){
                if (maxValue < intValues[i]) maxValue = intValues[i];
            }
        }
        return maxValue;
    }

    /**
     * 最小値を取得
     * @return
     */
    synchronized public int getMinValue(){
        int minValue = Integer.MAX_VALUE;
        for (ValueWithTimestamp valueWithTimestamp: this){
            int[] intValues = getIntValues(valueWithTimestamp.value);
            if (intValues == null) continue;
            // それぞれの要素に対して比較
            for (int i=0; i < intValues.length; i++){
                if (minValue > intValues[i]) minValue = intValues[i];
            }
        }
        return minValue;
    }
}
