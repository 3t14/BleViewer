package com.dev_training.imos.ble_viewer;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.dev_training.imos.ble_viewer.MyLeDevicesModel.*;
import static com.dev_training.imos.ble_viewer.UUIDs.*;

/**
 * ServiceとCharacteristicの一覧を表示するためのアダプタ
 * Created by 3t14 on 2014/08/19.
 */
public class ServiceExpandableListAdapter extends BaseExpandableListAdapter{

    private static final String TAG = "ServiceExpandableListAdapter" ;
    private Fragment mFragment;
    private Activity mActivity;
    // 呼び出し元へのコールバック
    private ServiceExpandableListAdapterCallback mCallback;

    //デバイス情報
    private BluetoothDevice mLeDevice;
    // デバイス付加情報の格納用
    private HashMap<String, Object> mDeviceInfo;

    private BluetoothGatt mGatt;

    public ServiceExpandableListAdapter(
            Fragment fragment,
            BluetoothDevice leDevice,
            HashMap<String, Object> deviceInfo,
            ServiceExpandableListAdapterCallback callback) {

        this.mCallback = callback;
        this.mFragment = fragment;
        this.mActivity = fragment.getActivity();

        mLeDevice = leDevice;
        mDeviceInfo = deviceInfo;

        mGatt = (BluetoothGatt)mDeviceInfo.get(KEY_GATT);
    }

    @Override
    public int getGroupCount() {
        // Servicesの数
        if (mGatt != null) return mGatt.getServices().size();
        else return 0;
    }

    @Override
    public int getChildrenCount(int i) {
        if (mGatt == null) return 0;
        BluetoothGattService service = mGatt.getServices().get(i);
        return service.getCharacteristics().size();
    }

    @Override
    public Object getGroup(int i) {
        return null;
    }

    @Override
    public Object getChild(int i, int i2) {
        return null;
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i2) {
        return i2;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int serviceIndex, boolean isExpanded, View convertView, ViewGroup parent) {
        if ( convertView == null) {
            LayoutInflater inflater = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listitem_le_service, null);
        }
        BluetoothGattService service = mGatt.getServices().get(serviceIndex);

        TextView serviceTitle = (TextView)convertView.findViewById(R.id.serviceTitleTextView);
        serviceTitle.setText(getUuidTitle(service.getUuid()));
        TextView uuidTextView = (TextView)convertView.findViewById(R.id.uuidTextView);
        uuidTextView.setText("UUID: "+service.getUuid().toString());

        // 背景色の更新
        int backgroundColor = Color.rgb(255, 255, 255);
        LinearLayout layout = (LinearLayout)convertView.findViewById(R.id.linearLayout);
        Date date = (Date)mDeviceInfo.get(service.getUuid().toString() + PROPERTY_KEY_LAST_UPDATED);
        if (date != null){
            long diffTime = System.currentTimeMillis() - date.getTime();
            // 1秒以内の更新の場合緑色に
            if (diffTime < 3000) {
                backgroundColor = Color.rgb(
                        (int) (diffTime * 255 / 3000), 255, (int) (diffTime * 255 / 3000));
            }
        }
        if (layout != null)
            layout.setBackgroundColor(backgroundColor);

        return convertView;
    }

    @Override
    public View getChildView(int serviceIndex, int characteristicIndex, boolean isExpanded, View convertView, ViewGroup viewGroup) {
        if ( convertView == null) {
            LayoutInflater inflater = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listitem_le_characteristic, null);
        }
        BluetoothGattService service = mGatt.getServices().get(serviceIndex);
        BluetoothGattCharacteristic characteristic = service.getCharacteristics().get(characteristicIndex);

        TextView characteristicTitleTextView = (TextView)convertView.findViewById(R.id.characteristicTitleTextView);
        characteristicTitleTextView.setText(getUuidTitle(characteristic.getUuid()));
        TextView uuidTextView = (TextView)convertView.findViewById(R.id.uuidTextView);
        uuidTextView.setText("UUID: "+characteristic.getUuid().toString());

        TextView valueTextView = (TextView)convertView.findViewById(R.id.valueTextView);
        TextView valueHexTextView = (TextView) convertView.findViewById(R.id.valueHexTextView);

        // 値の更新
        byte[] value = (byte[])mDeviceInfo.get(characteristic.getUuid().toString());
        if (value != null) {
            valueTextView.setText(new String(value));
            valueHexTextView.setText("Value: "+HexDump.toHex(value));
            // 背景色の更新
            int backgroundColor = Color.rgb(255, 255, 255);
            LinearLayout layout = (LinearLayout)convertView.findViewById(R.id.linearLayout);
            Date date = (Date)mDeviceInfo.get(characteristic.getUuid().toString() + PROPERTY_KEY_LAST_UPDATED);
            if (date != null){
                long diffTime = System.currentTimeMillis() - date.getTime();
                // 1秒以内の更新の場合緑色に
                if (diffTime < 3000) {
                    backgroundColor = Color.rgb(
                            (int) (diffTime * 255 / 3000), 255, (int) (diffTime * 255 / 3000));
                }
            }

            layout.setBackgroundColor(backgroundColor);
        } else {
            valueTextView.setText("");
            valueHexTextView.setText("No value");
        }

        return convertView;
    }

    /**
     * この値をtrueで返さなければ、onChildClickListenerが無効のままになる
     * @param serviceIndex
     * @param characteristicIndex
     * @return
     */
    @Override
    public boolean isChildSelectable(int serviceIndex, int characteristicIndex) {
        return true;
    }

    /**
     * ビューの更新
     * @param characteristic
     */
    public void updateView(BluetoothGattCharacteristic characteristic, final ExpandableListView expandableListView) {
        Log.v(TAG, "ServiceExpandableListAdapter updateView");
        BluetoothGatt gatt = (BluetoothGatt)mDeviceInfo.get(KEY_GATT);
        BluetoothGattService targetService = characteristic.getService();
        List<BluetoothGattCharacteristic> targetCharacteristics = targetService.getCharacteristics();

        List<BluetoothGattService> services = gatt.getServices();
        int serviceIndex;
        for (serviceIndex = 0; serviceIndex < services.size(); serviceIndex++) {
            if (services.get(serviceIndex).equals(targetService)){
                // hit
                Log.v(TAG, "updateView hit! serviceIndex = "+serviceIndex);
                int characteristicIndex;

                for (characteristicIndex = 0;
                     characteristicIndex < targetCharacteristics.size(); characteristicIndex++){
                    final int sIndex = serviceIndex;
                    final int cIndex = characteristicIndex;

                    Log.v(TAG, "updateView hit! characteristicIndex = "+characteristicIndex);

                    // Viewの更新
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //View view = expandableListView.get(sIndex, cIndex);
                            //getChildView(sIndex, cIndex, true, view, expandableListView);
                            notifyDataSetChanged();
                        }
                    });
                    return;
                }
            }
        }


    }


}

/**
 * 登録元への通知用コールバック
 */
interface ServiceExpandableListAdapterCallback
{

}