package com.dev_training.imos.ble_viewer;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import static com.dev_training.imos.ble_viewer.MyLeDevicesModel.*;

/**
 * BLEデバイス一覧を作成するためのListAdapter
 * Created by @3t14 on 2014/08/12.
 */
public class LeDeviceListAdapter extends BaseAdapter{

    private static final String TAG = "LeDeviceListAdapter";

    private Fragment mFragment;
    private Activity mActivity;
    // 呼び出し元へのコールバック
    private LeDeviceListAdapterCallback mCallback;

    //デバイス情報
    private ArrayList<BluetoothDevice> mLeDevices;
    // デバイス付加情報の格納用
    private HashMap<String, HashMap<String, Object>> mDeviceInfos;
    // レイアウト展開用インスタンス
    private final LayoutInflater mInflator;


    public LeDeviceListAdapter(Fragment fragment,
                               MyLeDevicesModel myLeDevicesModel,
                               LeDeviceListAdapterCallback callback) {
        super();

        this.mFragment = fragment;
        this.mActivity = fragment.getActivity();
        this.mCallback = callback;
        // デバイス情報を格納
        mLeDevices = myLeDevicesModel.getLeDevices();
        mDeviceInfos = myLeDevicesModel.getDeviceInfos();

        mInflator = mActivity.getLayoutInflater();
    }

    /**
     * 指定したアドレスから位置を取得
     * @param address
     * @return
     */
    public int getPosition(String address){
        for (int i = 0; i < mLeDevices.size(); i++){
            if (address.equals(mLeDevices.get(i).getAddress())){
                return i;
            }
        }
        return -1; // 存在しない場合
    }

    long lastUpdated = 0;

    // 指定したアドレスを持つデバイスの項目を更新する
    public void updateView(final String address, final ListView listView){
        // 指定ミリ秒を経過ごとにしか更新できない
        if (System.currentTimeMillis()  - lastUpdated > 0) {

            // Viewの更新
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int position = getPosition(address);
                    View itemView =
                            listView.getChildAt(position);
                    getView(position, itemView, listView);

                    lastUpdated = System.currentTimeMillis();

                    //notifyDataSetChanged();
                }
            });
        }
    }

    public void addDevice(BluetoothDevice bluetoothDevice, int rssi, byte[] scanData){
        HashMap<String, Object> deviceInfo = null;
        Log.d(TAG, "addDevice "+bluetoothDevice.getAddress()+", "+mLeDevices.size());
        // 同一のデバイスの有無を探索、
        for (BluetoothDevice device: mLeDevices){
            if (bluetoothDevice.getAddress().equals(device.getAddress())) {
                Log.d(TAG, "Device " + bluetoothDevice.getAddress() + " has been already added.");
                deviceInfo = mDeviceInfos.get(bluetoothDevice.getAddress());
                if (deviceInfo != null) {
                    deviceInfo.put(KEY_RSSI, Integer.valueOf(rssi));
                    deviceInfo.put(KEY_SCAN_DATA, scanData);
                }
                return; //既に登録済
            }
        }
        // 存在しなければ追加
        mLeDevices.add(bluetoothDevice);

        // Rssiの情報等を格納する
        deviceInfo = new HashMap<String, Object>();
        deviceInfo.put(KEY_RSSI, Integer.valueOf(rssi));
        deviceInfo.put(KEY_SCAN_DATA, scanData);
        if (mDeviceInfos == null)
            mDeviceInfos = new HashMap<String, HashMap<String, Object>>();
        mDeviceInfos.put(bluetoothDevice.getAddress(), deviceInfo);
        // Viewの更新
        notifyDataSetChanged();

        Log.d(TAG, "Device added " + bluetoothDevice.getName());
    }

    @Override
    public int getCount() {
        return mLeDevices.size();
    }

    @Override
    public BluetoothDevice getItem(int i) {
        return mLeDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        if (mLeDevices.get(i)==null) return view;

        Log.d(TAG, "getView i = " + i +" name = "+ mLeDevices.get(i).getName());
        if (view == null) {

            view = mInflator.inflate(R.layout.listitem_le_device, null);


        }

        BluetoothDevice device = mLeDevices.get(i);
        TextView nameTextView = (TextView) view.findViewById(R.id.nameTextView);
        nameTextView.setText(mLeDevices.get(i).getName());

        if (mLeDevices != null){
            HashMap <String, Object> deviceInfo = mDeviceInfos.get(device.getAddress());
            TextView rssiTextView = (TextView) view.findViewById(R.id.rssiTextView);
            rssiTextView.setText(deviceInfo.get(KEY_RSSI).toString()+" dBm");

            TextView addressTextView = (TextView)view.findViewById(R.id.addressTextView);
            addressTextView.setText("Address:\n" + device.getAddress());

            TextView scanDataTextView = (TextView) view.findViewById(R.id.scanDataTextView);
            scanDataTextView.setText("Scan Data:\n"+HexDump.toHex((byte[])deviceInfo.get(KEY_SCAN_DATA)));

            // 状態によって背景色を変更
            BluetoothManager bluetoothManager =
                    (BluetoothManager) mActivity.getSystemService(Context.BLUETOOTH_SERVICE);
            TextView statusTextView =  (TextView) view.findViewById(R.id.statusTextView);
            Button connectButton = (Button)view.findViewById(R.id.connectButton);

            int backgroundColor = Color.rgb(255, 255, 255);
            switch (bluetoothManager.getConnectionState(device, BluetoothProfile.GATT)){
                case BluetoothGatt.STATE_CONNECTED:
                    backgroundColor = Color.rgb(224, 224, 255);
                    statusTextView.setText("Connected");
                    connectButton.setText("Disconnect");
                    break;
                case BluetoothGatt.STATE_CONNECTING:
                    backgroundColor = Color.rgb(224, 224, 255);
                    statusTextView.setText("Connecting");
                    break;

                case BluetoothGatt.STATE_DISCONNECTED:
                default:
                    backgroundColor = Color.rgb(244, 244, 244);
                    statusTextView.setText("Disconnected");
                    connectButton.setText("Connect");
                    break;

            }

            byte buttonData[] = (byte[]) deviceInfo.get(UUIDs.TI_SENSOR_TAG_SIMPLE_KEY);

            if (buttonData != null){
                switch (buttonData[0]){
                    case 1:
                        backgroundColor = Color.rgb(128, 255, 255);
                        break;
                    case 2:
                        backgroundColor = Color.rgb(128, 255, 128);
                        break;
                    case 3:
                        backgroundColor = Color.rgb(255, 128, 128);
                        break;
                }
            }

            LinearLayout layout = (LinearLayout)view.findViewById(R.id.linearLayout);
            layout.setBackgroundColor(backgroundColor);

            connectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 接続処理
                    mCallback.connectToDevice(i);
                }
            });
        }


        return view;
    }



}

/**
 * 利用元へのイベント通知
 */
interface LeDeviceListAdapterCallback {
    // 指定したデバイスに接続する
    void connectToDevice(int deviceIndex);
}