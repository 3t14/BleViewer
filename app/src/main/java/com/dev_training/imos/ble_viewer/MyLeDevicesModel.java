package com.dev_training.imos.ble_viewer;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;



/**
 * BleDeviceとその周辺の情報を格納するためのクラス
 * Created by @3t14 on 2014/08/19.
 */
public class MyLeDevicesModel {
    public static final String KEY_RSSI = "rssi";
    public static final String KEY_SCAN_DATA = "scan_data";
    public static final String KEY_GATT = "gatt";
    // PROPERTY_KEY = 特定のUUID（String）に付与するキーとして定義
    //                特定のUUIDのServiceもしくはCharacteristicsに関するデータとして扱う
    public static final String PROPERTY_KEY_LAST_UPDATED = ".last_updated";
    public static final String PROPERTY_KEY_TIME_SERIES = ".time_series";
    public static final String PROPERTY_KEY_VALUE_TYPE = ".value_type";


    //デバイス情報
    private ArrayList<BluetoothDevice> leDevices;
    // デバイス付加情報の格納用
    private HashMap<String, HashMap<String, Object>> deviceInfos;

    //
    private int queueSize = 120;

    public MyLeDevicesModel() {
        leDevices = new ArrayList<BluetoothDevice>();
        deviceInfos = new HashMap<String, HashMap<String, Object>>();
    }

    public ArrayList<BluetoothDevice> getLeDevices() {
        return leDevices;
    }

    public void setLeDevices(ArrayList<BluetoothDevice> leDevices) {
        this.leDevices = leDevices;
    }

    public HashMap<String, HashMap<String, Object>> getDeviceInfos() {
        return deviceInfos;
    }

    /**
     * 値を代入
     * @param address   対象のデバイスアドレス
     * @param key   代入先のキー
     * @param value 代入する値
         keyとvalueの関係
            case  "gatt"：
              値＝BluetoothGattインスタンス
            case CharacteristicのUUID（文字列）：
              値＝そのCharacteristicのUUIDのvalue（byte[]）
            case ServiceのUUID（文字列）+".last_updated"：
              値＝下位のCharacteristicが更新されたタイムスタンプ（Date)
            case CharacteristicのUUID（文字列）+".last_updated"：
              値＝そのvalueが更新されたタイムスタンプ（Date)
            case CharacteristicのUUID（文字列）+".time_series"：
              値＝時系列のデータ（TimeSeriesData）
     */
    public void putDeviceInfo(String address, String key, Object value){
        HashMap<String, Object> deviceInfo = deviceInfos.get(address);
        deviceInfo.put(key, value);
    }

    /**
     * 指定したアドレスを持つデバイスのkeyの値を取得する
     * @param address
     * @param key
     * @return
     */
    public Object getDeviceInfo(String address, String key){
        HashMap<String, Object>  deviceInfo = deviceInfos.get(address);
        return deviceInfo.get(key);
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    /**
     *
     * @param address
     * @param characteristic
     */
    public void addValueWithTimestamp(String address, BluetoothGattCharacteristic characteristic){
        if (deviceInfos == null) return;
        // 対象のService, Characteristicのタイムスタンプの登録
        // デバイス情報として保存、状態を他で利用

        byte[] value = characteristic.getValue();

        // 値の登録
        deviceInfos.get(address)
                .put(characteristic.getUuid().toString(), value);

        // 値の種別の登録（本来は最初の１回でOK）
        deviceInfos.get(address)
                .put(characteristic.getUuid().toString() + PROPERTY_KEY_VALUE_TYPE,
                        UUIDs.getValueType(characteristic));
        // タイムスタンプ情報を保存
        // Serviceの更新日時の代入
        deviceInfos.get(address)
                .put(characteristic.getService().getUuid().toString() + PROPERTY_KEY_LAST_UPDATED,
                        new Date());
        // Characteristicの更新日時の代入
        deviceInfos.get(address)
                .put(characteristic.getUuid().toString() + PROPERTY_KEY_LAST_UPDATED,
                        new Date());

        // 時系列データとして保存
        ValueWithTimestamp valueWithTimestamp
                = new ValueWithTimestamp(characteristic.getValue(), System.currentTimeMillis());
        ValueWithTimestampQueue queue
                = (ValueWithTimestampQueue) deviceInfos.get(address)
                    .get(characteristic.getUuid().toString() + PROPERTY_KEY_TIME_SERIES);
        // キューへの登録
        if (queue == null) {
            queue = new ValueWithTimestampQueue(UUIDs.getValueType(characteristic));
            putDeviceInfo(address,
                    characteristic.getUuid().toString() + PROPERTY_KEY_TIME_SERIES,
                    queue);
            // 外れ値の可能性があるため
            // 1回目は登録しない
            return;
        }

        synchronized (queue) {

            queue.offer(valueWithTimestamp);
            // キューサイズが大きいときは先頭を除去
            if (queue.size() > queueSize){
                queue.remove();
            }
        }

    }

    /**
     * デバイス情報の削除
     * @param address
     * @param key
     * @return
     */
    public boolean removeDeviceInfo(String address, String key){
        HashMap<String, Object>  deviceInfo = deviceInfos.get(address);
        deviceInfo.remove(key);
        return true;
    }

    /**
     * デバイス情報の削除
     */
    synchronized public void removeAllDeviceInfo(){
        // Gattを全て閉じる必要あり
        Iterator<HashMap<String, Object>> iterator = deviceInfos.values().iterator();
        while ( iterator.hasNext() ){
            HashMap<String, Object> deviceInfo = iterator.next();
            BluetoothGatt gatt = (BluetoothGatt) deviceInfo.get(KEY_GATT);
            if (gatt != null){
                gatt.close();
                gatt = null;
            }
        }

        if (deviceInfos != null)
            deviceInfos.clear();
        if (leDevices != null)
            leDevices.clear();
    }




}
