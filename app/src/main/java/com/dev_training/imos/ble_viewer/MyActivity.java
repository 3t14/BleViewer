package com.dev_training.imos.ble_viewer;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ListView;

import java.util.HashMap;

import static com.dev_training.imos.ble_viewer.MyLeDevicesModel.*;
// ダイレクトに定数名で参照可能にする

/**
 * @author @3t14
 */
public class MyActivity extends Activity {



    private static DeviceListFragment deviceListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        if (savedInstanceState == null) {
            deviceListFragment = new DeviceListFragment();

            getFragmentManager().beginTransaction()
                    .add(R.id.container, deviceListFragment)
                    .commit();
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent mActivity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case R.id.action_settings:
                return true;

            case R.id.action_scan_ble:
                if (deviceListFragment != null) {
                    deviceListFragment.scanLeDevice(item);
                }
                return true;
            case R.id.action_clear_ble:
                if (deviceListFragment != null) {
                    deviceListFragment.resetBle();
                }
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A device list fragment containing a simple view.
     */
    public static class DeviceListFragment extends Fragment
            implements AdapterView.OnItemClickListener,
                MyGattCallback.MyGattCallbackCallback,
                LeDeviceListAdapterCallback,
                ServiceExpandableListAdapterCallback, ExpandableListView.OnChildClickListener {

        private static final long SCAN_PERIOD = 20000 ;
        private static final String TAG = "DeviceListFragment" ;

        private static final int REQUEST_ENABLE_BT = 0x1000;
        private static BluetoothAdapter mBluetoothAdapter;

        private View mRootView;
        private ListView mListView;
        private TimeSeriesGraphView mGraphView;

        // Bleデバイス情報を格納するためのクラス
        private MyLeDevicesModel mMyLeDevicesModel;

        private Handler mHandler = new Handler();
        private MenuItem mScanMenuItem;
        private boolean mScanning = false;


        // ListViewをカスタマイズ表示するためのアダプタ
        private LeDeviceListAdapter mLeDeviceListAdapter;
        //
        private ServiceExpandableListAdapter mServiceExpandableListAdapter;
        // BluetoothGattのコールバック
        private MyGattCallback mMyGattCallback;
        private int currentDeviceIndex;

        // ExpandableListViewに表示されているデバイス
        private BluetoothDevice mActiveDevice;

        // BLEデバイスのスキャンコールバック
        private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice bluetoothDevice, final int i, final byte[] bytes) {
                //
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // デバイス情報の追加
                        mLeDeviceListAdapter.addDevice(bluetoothDevice, i, bytes);
                        // 指定したビューのみを更新
                        mLeDeviceListAdapter.updateView(bluetoothDevice.getAddress(), mListView);

                    }
                });
            }
        };


        public DeviceListFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            mRootView = inflater.inflate(R.layout.fragment_my, container, false);

            // デバイスリストを表示するためのListViewの取得とアダプタの割当
            mListView = (ListView)mRootView.findViewById(R.id.listView);

            mMyLeDevicesModel = new MyLeDevicesModel();

            mLeDeviceListAdapter = new LeDeviceListAdapter(this, mMyLeDevicesModel, this);
            mListView.setAdapter(mLeDeviceListAdapter);

            // デバイス一覧の項目をクリックした時の処理
            mListView.setOnItemClickListener(this);

            // コールバックの生成とモデルの引き渡し（参照）
            mMyGattCallback = new MyGattCallback(getActivity(), mMyLeDevicesModel, this);

            // Viewを定期的に更新する
            updateView(true);

            return mRootView;
        }


        protected void scanLeDevice(final MenuItem menuItem) {
            this.mScanMenuItem = menuItem;

            Log.d(TAG, "scanLeDevice");
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                return;
            }

            if (!mScanning) {
                LeDeviceListAdapter adapter = (LeDeviceListAdapter)mListView.getAdapter();



                if (mBluetoothAdapter.startLeScan(mLeScanCallback)){
                    //
                    Log.d(TAG, "Scan started");
                    mScanning = true;
                    if (menuItem != null) menuItem.setTitle(R.string.action_stop_scan);
                } else {
                    Log.e(TAG, "Could not startLeScan...");
                };
                // 定義された時間だけスキャンを行い、
                // 経過後、ストップする
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        mScanning = false;
                        if (menuItem != null) menuItem.setTitle(R.string.action_scan_ble);
                    }
                }, SCAN_PERIOD);


            } else {

                mScanning = false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                if (menuItem != null) menuItem.setTitle(R.string.action_scan_ble);

            }

        }

        /**
         * Bleの接続をリセットする
         */
        public void resetBle() {
            if (mBluetoothAdapter == null) return;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            if (mScanMenuItem != null) mScanMenuItem.setTitle(R.string.action_scan_ble);
            mMyLeDevicesModel.removeAllDeviceInfo();

        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, final View view, int i, long l) {
            Log.d(TAG, "onItemClick deviceIndex = "+i);
            // ExpandableListViewを利用して、中身を表示

            // 選択されたデバイスインデックスを更新
            currentDeviceIndex = i;

            mActiveDevice = mMyLeDevicesModel.getLeDevices().get(i);


            // デバイス情報がないためクリア
            if (mActiveDevice == null) return;
            HashMap<String, Object> deviceInfo = mMyLeDevicesModel.getDeviceInfos().get(mActiveDevice.getAddress());
            if (deviceInfo == null) {
                deviceInfo = new HashMap<String, Object>();
                mMyLeDevicesModel.getDeviceInfos().put(mActiveDevice.getAddress(), deviceInfo);
            }
            // ビューの取得
            ExpandableListView expandableListView = (ExpandableListView)mRootView.findViewById(R.id.expandableListView);
            mServiceExpandableListAdapter =
                    new ServiceExpandableListAdapter(this, mActiveDevice, deviceInfo, this);
            // アダプタの割当て
            expandableListView.setAdapter(mServiceExpandableListAdapter);
            expandableListView.setOnChildClickListener(this);

        }

        @Override
        public void updateView(boolean repeatUpdateFlag) {

            final boolean flag = repeatUpdateFlag;

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mLeDeviceListAdapter != null)
                        mLeDeviceListAdapter.notifyDataSetChanged();
                    if (mServiceExpandableListAdapter != null)
                        mServiceExpandableListAdapter.notifyDataSetChanged();
                    if (mGraphView != null)
                        mGraphView.invalidate(); //グラフの更新
                    if (flag) updateView(true);
                }
            }, 300);

        }

        // MyGattCallbackから呼び出されるビューの更新
        @Override
        public void updateView(String address, BluetoothGattCharacteristic characteristic) {
            mLeDeviceListAdapter.updateView(address, mListView);

            // ExpandableListViewが対象デバイスの場合
            // Viewを更新
            if (address.equals(mActiveDevice.getAddress())){
                ExpandableListView expandableListView = (ExpandableListView)mRootView.findViewById(R.id.expandableListView);
                mServiceExpandableListAdapter.updateView(characteristic, expandableListView);
            }
        }

        @Override
        public void connectToDevice(int deviceIndex) {
            // アダプタの取得
            final LeDeviceListAdapter adapter = mLeDeviceListAdapter;
            final BluetoothDevice device = adapter.getItem(deviceIndex);
            final DeviceListFragment fragment = this;


            // Fail to register callback対策としてUIスレッドで実行
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    BluetoothManager bluetoothManager =
                            (BluetoothManager)getActivity().getSystemService(Context.BLUETOOTH_SERVICE);

                    BluetoothGatt gatt = (BluetoothGatt)mMyLeDevicesModel.getDeviceInfo(device.getAddress(), KEY_GATT);
                    if (gatt != null){
                        // 接続から切断へ
                        switch (bluetoothManager.getConnectionState(device, BluetoothProfile.GATT)){
                            case BluetoothGatt.STATE_CONNECTED:
                                gatt.disconnect();
                            case BluetoothGatt.STATE_CONNECTING: // Do nothing
                                return;
                        }
                    }

                    // 接続処理
                    // 接続後にGATT情報を格納
                    mMyLeDevicesModel.putDeviceInfo(device.getAddress(), KEY_GATT,
                            device.connectGatt(getActivity(), false, mMyGattCallback));

                    mActiveDevice = device;

                    HashMap<String, Object> deviceInfo = mMyLeDevicesModel.getDeviceInfos().get(mActiveDevice.getAddress());
                    if (deviceInfo == null) {
                        deviceInfo = new HashMap<String, Object>();
                        mMyLeDevicesModel.getDeviceInfos().put(mActiveDevice.getAddress(), deviceInfo);
                    }
                    // ビューの取得
                    ExpandableListView expandableListView = (ExpandableListView)mRootView.findViewById(R.id.expandableListView);
                    mServiceExpandableListAdapter =
                            new ServiceExpandableListAdapter(fragment, mActiveDevice, deviceInfo, fragment);
                    // アダプタの割当て
                    expandableListView.setAdapter(mServiceExpandableListAdapter);
                    expandableListView.setOnChildClickListener(fragment);
                }
            });

        }

        /**
         * expandableListView（ServiceExpandableListAdapter）の子項目をタップした際の処理
         *
         * @param expandableListView
         * @param view
         * @param serviceIndex
         * @param characteristicIndex
         * @param viewId
         * @return
         */
        @Override
        public boolean onChildClick(
                ExpandableListView expandableListView, View view, int serviceIndex, int characteristicIndex, long viewId) {
            //
            BluetoothGatt gatt = (BluetoothGatt) mMyLeDevicesModel.getDeviceInfo(mActiveDevice.getAddress(), KEY_GATT);
            BluetoothGattService service  = gatt.getServices().get(serviceIndex);
            BluetoothGattCharacteristic characteristic = service.getCharacteristics().get(characteristicIndex);

            ExpandableListView serviceExpandableListView = (ExpandableListView) mRootView.findViewById(R.id.expandableListView);
            if (expandableListView == serviceExpandableListView){
                //Toast.makeText(getActivity(), "onCharacteristicClick", Toast.LENGTH_SHORT).show();
                // 項目タップ＝TimeSeriesGraphViewに結果表示
                mGraphView = new TimeSeriesGraphView(getActivity(), mMyLeDevicesModel);

                FrameLayout frameLayout = (FrameLayout) mRootView.findViewById(R.id.frameLayout);
                frameLayout.addView(mGraphView);
                //描画対象を選択
                mGraphView.setCharacteristic(mActiveDevice.getAddress(), characteristic);


                // 値の読み取り
                mMyGattCallback.readCharacteristic(gatt, characteristic);

                return true;
            }

            return false;
        }


    }
}
