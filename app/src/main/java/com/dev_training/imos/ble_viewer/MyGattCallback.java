package com.dev_training.imos.ble_viewer;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import static com.dev_training.imos.ble_viewer.UUIDs.*;

/**
 *
 * Created by @3t14 on 2014/08/18.
 */
public class MyGattCallback extends BluetoothGattCallback {

    private static final String TAG = "MyGattCallback";
    private Activity mActivity; //
    // BLEでバイス情報を格納・操作するモデルクラス
    private MyLeDevicesModel mMyLeDevicesModel;
    // 結果を通知するコールバッククラス
    private MyGattCallbackCallback mCallbackCallback;


    // readCharacteristic state
    // -1 : 単体の読み込み
    // 0以上： 連続読み込み時の読み込み対象（getServicesメソッドで取得できる要素番号）
    int readServiceState = -1;
    // readCharacteristic state
    // -1 : 単体の読み込み
    // 0以上： 連続読み込み時の読み込み対象（getCharacteristicsメソッドで取得できる要素番号）
    int readCharacteristicState = -1;


    // キューにひとつの要素として格納するために定義
    private class GattAndDescriptor {
        BluetoothGatt gatt;
        BluetoothGattDescriptor descriptor;

        private GattAndDescriptor(BluetoothGatt gatt, BluetoothGattDescriptor descriptor) {
            this.gatt = gatt;
            this.descriptor = descriptor;
        }
    }
    // キューにひとつの要素として格納するために定義
    private class GattAndCharacteristic {
        BluetoothGatt gatt;
        BluetoothGattCharacteristic characteristic;

        private GattAndCharacteristic(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            this.gatt = gatt;
            this.characteristic = characteristic;
        }

    }

    //同時にCharacteristic等の値の読み書きができないため、キューを用いてプログレッシブに一つ筒処理を行う
    // 書き込みを優先
    private Queue<GattAndDescriptor> descriptorWriteQueue = new LinkedList<GattAndDescriptor>() ;
    private Queue<GattAndDescriptor> descriptorReadQueue = new LinkedList<GattAndDescriptor>() ;
    private Queue<GattAndCharacteristic> characteristicWriteQueue = new LinkedList<GattAndCharacteristic>();
    private Queue<GattAndCharacteristic> characteristicReadQueue = new LinkedList<GattAndCharacteristic>();


    public MyGattCallback(Activity activity, MyLeDevicesModel myLeDevicesModel, MyGattCallbackCallback callbackCallback) {
        this.mActivity = activity;
        this.mCallbackCallback = callbackCallback;
        this.mMyLeDevicesModel = myLeDevicesModel;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }


    /**
     * キューを用いたwrite処理
     * @param gatt
     * @param descriptor
     */
    public boolean writeDescriptor(BluetoothGatt gatt, BluetoothGattDescriptor descriptor){
        descriptorWriteQueue.add(new GattAndDescriptor(gatt, descriptor));
        // 前にキューがたまっていないならば、そのまま書き込む
        if (descriptorWriteQueue.size() == 1) {
            if (gatt.writeDescriptor(descriptor)) {
                Log.v(TAG, "Write descriptor was succeeded.");
                return true;
            } else {
                Log.v(TAG, "Write descriptor was not succeeded.");
                return false;
            }
        }
        return true;
    }

    /**
     * キューを用いて指定したcharacteristicを読み込む（非同期）
     * @param gatt
     * @param characteristic
     */
    public boolean writeCharacteristic(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
        characteristicWriteQueue.add(new GattAndCharacteristic(gatt, characteristic));

        Log.v(TAG, "characteristicReadQueue.size() = " + characteristicReadQueue.size());
        if ((characteristicWriteQueue.size() == 1)
                && (descriptorWriteQueue.size() == 0)){
            if (gatt.writeCharacteristic(characteristic)) {
                Log.v(TAG, "Write characteristic was succeeded.");
                return true;
            } else {
                Log.v(TAG, "Write characteristic was not succeeded.");
                return false;
            }
        }
        return true;
    }

    /**
     * キューを用いて指定したcharacteristicを読み込む（非同期）
     * @param gatt
     * @param characteristic
     */
    public boolean readCharacteristic(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
        characteristicReadQueue.add(new GattAndCharacteristic(gatt, characteristic));

        Log.v(TAG, "characteristicReadQueue.size() = " + characteristicReadQueue.size());
        if ((characteristicReadQueue.size() == 1) && (descriptorWriteQueue.size() == 0)){
            if (gatt.readCharacteristic(characteristic)) {
                Log.v(TAG, "Read characteristic was succeeded.");
                return true;
            } else {
                Log.v(TAG, "Read characteristic was not succeeded.");
                return false;
            }
        }
        return true;
    }

    /**
     * BLEの読み書き終了後のキューの処理
     */
    private void leProcessQueue() {
        Log.v(TAG, "leProcessQueue");

        if ( descriptorWriteQueue.size() > 0 ) {
            if (!descriptorWriteQueue.element().gatt
                    .writeDescriptor( descriptorWriteQueue.element().descriptor)){
                descriptorWriteQueue.remove();
                leProcessQueue();
            };
        } else if ( characteristicWriteQueue.size() > 0 ){
            if (!characteristicWriteQueue.element().gatt.writeCharacteristic(
                    characteristicWriteQueue.element().characteristic)) {
                // エラーが発生した場合は次の処理へ
                characteristicWriteQueue.remove();
                leProcessQueue();
            }
        }else if ( characteristicReadQueue.size() > 0 ){
            if (!characteristicReadQueue.element().gatt.readCharacteristic(
                    characteristicReadQueue.element().characteristic)) {
                // エラーが発生した場合は次の処理へ
                characteristicReadQueue.remove();
                leProcessQueue();
            }
        }
    }

    // 接続状態の変更
    @Override
    public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
        Log.d(TAG, "onConnectionStateChange gatt = " + gatt.getDevice().getName()
                + ", status = " + status + ", new state = " + newState);


        switch (newState) {
            case BluetoothProfile.STATE_CONNECTED:
                gatt.discoverServices();
                break;
            case BluetoothProfile.STATE_DISCONNECTED:
                gatt.close();

                break;

        }

         mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //updateView
                    mCallbackCallback.updateView(false);
                    Log.v(TAG, "onConnectionStateChange updateView");
                }
            });

        super.onConnectionStateChange(gatt, status, newState);

    }



    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);

        Log.d(TAG, "onServicesDiscovered..." + gatt.getDevice().getAddress());

        // サービスを発見 それぞれのサービスをログに出力
        List<BluetoothGattService> services = gatt.getServices();
        // 対応すべきサービスなし
        if (services.size() <= 0) return;
        // 最初のサービスを取得する
        BluetoothGattService service = services.get(0);
        Log.d(TAG, "service UUID = " + service.getUuid().toString());
        readServiceState = 0;

        // 最初のCharacteristicを取得
        List<BluetoothGattCharacteristic> characteristics;
        readCharacteristicState = 0;


        //


        // センサーの起動とNotificationの設定
        for (BluetoothGattService gattService: services){
            characteristics = gattService.getCharacteristics();
            Log.d(TAG, "Service UUID = " + gattService.getUuid().toString());

            for (BluetoothGattCharacteristic characteristic : characteristics) {
                // 個々のCharacteristicを出力
                Log.d(TAG, "Characteristic UUID = " + characteristic.getUuid().toString());

                // Config対象
                UUID configTargets[] = { UUID_TI_SENSOR_TAG_TEMPERATURE_CONFIG,
                        UUID_TI_SENSOR_TAG_ACCELEROMETER_CONFIG
                        //UUID_TI_SENSOR_TAG_SIMPLE_KEY
                };
                // 対象でなければスキップ
                if (Arrays.asList(configTargets).contains(characteristic.getUuid())) {
                    // 対処のcharacteristicの通知を有効にする
                    // Configの値は0x01でON
                    characteristic.setValue(new byte[]{0x01});
                    // Configの値は0x00でOFF
                    //characteristic.setValue(new byte[]{0x00});
                    writeCharacteristic(gatt, characteristic);
                    Log.d(TAG, "writeCharacteristic value[0]= " + characteristic.getValue()[0]);
                } else {
                    Log.d(TAG, "writeCharacteristic was not succeeded.");
                }

                // Notify対象のCharacteristicか否かを判別
                // Notify対象
                UUID notifyTargets[] = { //UUID_TI_SENSOR_TAG_TEMPERATURE,
                        UUID_TI_SENSOR_TAG_ACCELEROMETER,
                        UUID_TI_SENSOR_TAG_SIMPLE_KEY};
                // 対象でなければスキップ
                if (Arrays.asList(notifyTargets).contains(characteristic.getUuid())) {
                    // 対処のcharacteristicの通知を有効にする

                    if (gatt.setCharacteristicNotification(characteristic, true)) {
                        Log.v(TAG, "Set notification was succeeded. UUID = " + characteristic.getUuid().toString());

                        // Notificationを実現するにはDescriptorの設定が必要
                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID_CLIENT_CHARACTERISTIC_CONFIGURATION);

                        if (descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            //descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                            writeDescriptor(gatt, descriptor);

                            Log.d(TAG, "ENABLE_NOTIFICATION_VALUE = "
                                    + BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE[0] + "," + BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE[1]);
                        }
                    } else {
                        Log.d(TAG, "Set notification was not succeeded.");
                    }
                }



            }
        }

        for (BluetoothGattService gattService: services){
            characteristics = gattService.getCharacteristics();
            for (BluetoothGattCharacteristic characteristic : characteristics) {
                // 値を出力
                String hex = HexDump.toHex(characteristic.getValue());
                Log.v(TAG, "Characteristic = " + characteristic.getUuid() + ", value = " + hex);
                // キューへの追加
                readCharacteristic(gatt, characteristic);
            }
        }

    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);


        // 値を出力表示する
        String hex = HexDump.toHex(characteristic.getValue());
        Log.v(TAG, "OnRead Characteristic = " + characteristic.getUuid() + ", value = " + hex);
        Log.v(TAG, "OnRead characteristicReadQueue.size() = " + characteristicReadQueue.size());


        // 時刻付きで値を記録
        mMyLeDevicesModel.addValueWithTimestamp(
                gatt.getDevice().getAddress(),
                characteristic);

        // キューの要素を削除
        if (characteristicReadQueue.size() > 0)
            characteristicReadQueue.remove();
        // 後処理（キューに残りがある場合それを実行）
        leProcessQueue();

    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        // 値を出力表示するcharacteristic.getValue();
        String hex = HexDump.toHex(characteristic.getValue());
        Log.d(TAG, "Notify Characteristic = " + characteristic.getUuid().toString() + ", value = " + hex);

        // 時刻付きで値を記録
        mMyLeDevicesModel.addValueWithTimestamp(
                gatt.getDevice().getAddress(),
                characteristic);

        // ビューの更新
        mCallbackCallback.updateView(gatt.getDevice().getAddress(), characteristic);

    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        String hex = HexDump.toHex(characteristic.getValue());
        Log.d(TAG, "Write Characteristic = " + characteristic.getUuid().toString() + ", value = " + hex);
        // キューの要素を削除
        if (characteristicWriteQueue.size() > 0)
            characteristicWriteQueue.remove();
        // キューの処理
        leProcessQueue();
        // ビューの更新
        mCallbackCallback.updateView(gatt.getDevice().getAddress(), characteristic);
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        Log.d(TAG, "onDescriptorWrite status = " + status);
        Log.d(TAG, "descriptor = "+descriptor.getValue()[0]+","+descriptor.getValue()[1]);

        // 一つ分の処理完了
        if (descriptorWriteQueue.size() > 0)
            descriptorWriteQueue.remove();
        leProcessQueue();

    }


    // MyGattCallbackのCallback
    // ビューの更新等を求める場合等
    interface MyGattCallbackCallback {
        // Viewを全て更新
        void updateView(boolean repeatFlag);
        // 特定の項目のみ更新
        void updateView(String address, BluetoothGattCharacteristic characteristic);
        // BLEデバイス情報を格納するListAdapterを取得
        //LeDeviceListAdapter getLeDeviceListAdapter();
    }
}

