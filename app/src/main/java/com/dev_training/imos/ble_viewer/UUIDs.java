package com.dev_training.imos.ble_viewer;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by @3t14 on 2014/08/15.
 */
public class UUIDs {
    // Standard UUIDs
    static final String BLUETOOTH_BASE_UUID = "-0000-1000-8000-00805f9b34fb";

    // Generic Access Service
    static final String GENERIC_ACCESS_TITLE = "Generic Access Service";
    static final String GENERIC_ACCESS = "00001800" + BLUETOOTH_BASE_UUID;
    static final UUID UUID_GENERIC_ACCESS = UUID.fromString(GENERIC_ACCESS);
    //
    static final String DEVICE_NAME_TITLE = "Device Name";
    static final String DEVICE_NAME = "00002a00" + BLUETOOTH_BASE_UUID;
    static final UUID UUID_DEVICE_NAME = UUID.fromString(DEVICE_NAME);
    //
    static final String APPEARANCE_TITLE = "Appearance";
    static final String APPEARANCE = "00002a00" + BLUETOOTH_BASE_UUID;
    static final UUID UUID_APPEARANCE = UUID.fromString(APPEARANCE);
    /*
    //
    static final String _TITLE = "";
    static final String  = "00002a00" + BLUETOOTH_BASE_UUID;
    static final UUID UUID_ = UUID.fromString();
    */
    //

    // Descriptors
    //
    static final String CLIENT_CHARACTERISTIC_CONFIGURATION_TITLE = "Client Characteristic Configuration";
    static final String CLIENT_CHARACTERISTIC_CONFIGURATION = "00002902" + BLUETOOTH_BASE_UUID;
    static final UUID UUID_CLIENT_CHARACTERISTIC_CONFIGURATION = UUID.fromString(CLIENT_CHARACTERISTIC_CONFIGURATION);

    // Texas Instruments SensorTag
    static final String TI_BASE_UUID = "-0451-4000-b000-000000000000";

    // Temperature
    // service
    static final String TI_SENSOR_TAG_TEMPERATURE_SERVICE_TITLE = "TI SensorTag Temperature Service";
    static final String TI_SENSOR_TAG_TEMPERATURE_SERVICE = "f000aa00" + TI_BASE_UUID;
    static final UUID UUID_TI_SENSOR_TAG_TEMPERATURE_SERVICE = UUID.fromString(TI_SENSOR_TAG_TEMPERATURE_SERVICE);
    // value
    static final String TI_SENSOR_TAG_TEMPERATURE_TITLE = "TI SensorTag Temperature";
    static final String TI_SENSOR_TAG_TEMPERATURE = "f000aa01" + TI_BASE_UUID;
    static final UUID UUID_TI_SENSOR_TAG_TEMPERATURE = UUID.fromString(TI_SENSOR_TAG_TEMPERATURE);
    // config
    static final String TI_SENSOR_TAG_TEMPERATURE_CONFIG_TITLE = "TI SensorTag Temperature Config";
    static final String TI_SENSOR_TAG_TEMPERATURE_CONFIG = "f000aa02" + TI_BASE_UUID;
    static final UUID UUID_TI_SENSOR_TAG_TEMPERATURE_CONFIG = UUID.fromString(TI_SENSOR_TAG_TEMPERATURE_CONFIG);
    // period
    static final String TI_SENSOR_TAG_TEMPERATURE_PERIOD_TITLE = "TI SensorTag Temperature Period";
    static final String TI_SENSOR_TAG_TEMPERATURE_PERIOD = "f000aa03" + TI_BASE_UUID;
    static final UUID UUID_TI_SENSOR_TAG_TEMPERATURE_PERIOD = UUID.fromString(TI_SENSOR_TAG_TEMPERATURE_PERIOD);

    // Accelerometer
    // service
    static final String TI_SENSOR_TAG_ACCELEROMETER_SERVICE_TITLE = "TI SensorTag Accelerometer Service";
    static final String TI_SENSOR_TAG_ACCELEROMETER_SERVICE = "f000aa10" + TI_BASE_UUID;
    static final UUID UUID_TI_SENSOR_TAG_ACCELEROMETER_SERVICE = UUID.fromString(TI_SENSOR_TAG_ACCELEROMETER_SERVICE);
    // value
    static final String TI_SENSOR_TAG_ACCELEROMETER_TITLE = "TI SensorTag Accelerometer";
    static final String TI_SENSOR_TAG_ACCELEROMETER = "f000aa11" + TI_BASE_UUID;
    static final UUID UUID_TI_SENSOR_TAG_ACCELEROMETER = UUID.fromString(TI_SENSOR_TAG_ACCELEROMETER);
    // config
    static final String TI_SENSOR_TAG_ACCELEROMETER_CONFIG_TITLE = "TI SensorTag Accelerometer Config";
    static final String TI_SENSOR_TAG_ACCELEROMETER_CONFIG = "f000aa12" + TI_BASE_UUID;
    static final UUID UUID_TI_SENSOR_TAG_ACCELEROMETER_CONFIG = UUID.fromString(TI_SENSOR_TAG_ACCELEROMETER_CONFIG);
    // period
    static final String TI_SENSOR_TAG_ACCELEROMETER_PERIOD_TITLE = "TI SensorTag Accelerometer Period";
    static final String TI_SENSOR_TAG_ACCELEROMETER_PERIOD = "f000aa13" + TI_BASE_UUID;
    static final UUID UUID_TI_SENSOR_TAG_ACCELEROMETER_PERIOD = UUID.fromString(TI_SENSOR_TAG_ACCELEROMETER_PERIOD);


    //
    // Simple Key Service
    static final String TI_SENSOR_TAG_SIMPLE_KEY_SERVICE_TITLE = "TI SensorTag Simple Key Service";
    static final String TI_SENSOR_TAG_SIMPLE_KEY_SERVICE = "0000ffe0" + BLUETOOTH_BASE_UUID;
    static final UUID   UUID_TI_SENSOR_TAG_SIMPLE_KEY_SERVICE = UUID.fromString(TI_SENSOR_TAG_SIMPLE_KEY_SERVICE);

    static final String TI_SENSOR_TAG_SIMPLE_KEY_TITLE = "TI SensorTag Simple Key";
    static final String TI_SENSOR_TAG_SIMPLE_KEY = "0000ffe1" + BLUETOOTH_BASE_UUID;
    static final UUID   UUID_TI_SENSOR_TAG_SIMPLE_KEY = UUID.fromString(TI_SENSOR_TAG_SIMPLE_KEY);

    private static final HashMap<String, String> uuidTitleMap = new HashMap<String, String>(){{

        put(GENERIC_ACCESS, GENERIC_ACCESS_TITLE);
        put(DEVICE_NAME, DEVICE_NAME_TITLE);
        put(APPEARANCE, APPEARANCE_TITLE);

        put(CLIENT_CHARACTERISTIC_CONFIGURATION, CLIENT_CHARACTERISTIC_CONFIGURATION_TITLE);

        put(TI_SENSOR_TAG_TEMPERATURE_SERVICE, TI_SENSOR_TAG_TEMPERATURE_SERVICE_TITLE);
        put(TI_SENSOR_TAG_TEMPERATURE, TI_SENSOR_TAG_TEMPERATURE_TITLE);
        put(TI_SENSOR_TAG_TEMPERATURE_CONFIG, TI_SENSOR_TAG_TEMPERATURE_CONFIG_TITLE);
        put(TI_SENSOR_TAG_TEMPERATURE_PERIOD, TI_SENSOR_TAG_TEMPERATURE_PERIOD_TITLE);

        put(TI_SENSOR_TAG_ACCELEROMETER_SERVICE, TI_SENSOR_TAG_ACCELEROMETER_SERVICE_TITLE);
        put(TI_SENSOR_TAG_ACCELEROMETER, TI_SENSOR_TAG_ACCELEROMETER_TITLE);
        put(TI_SENSOR_TAG_ACCELEROMETER_CONFIG, TI_SENSOR_TAG_ACCELEROMETER_CONFIG_TITLE);
        put(TI_SENSOR_TAG_ACCELEROMETER_PERIOD, TI_SENSOR_TAG_ACCELEROMETER_PERIOD_TITLE);

        put(TI_SENSOR_TAG_SIMPLE_KEY_SERVICE,   TI_SENSOR_TAG_SIMPLE_KEY_SERVICE_TITLE);
        put(TI_SENSOR_TAG_SIMPLE_KEY,           TI_SENSOR_TAG_SIMPLE_KEY_TITLE);


    }};

    // valueのデータ種別

    // 不明
    public static final int VALUE_TYPE_UNKNOWN = 0;
    // 1バイト1サンプル
    public static final int VALUE_TYPE_1_BYTE_1_SAMPLE = 1;
    // 1バイト2サンプル
    public static final int VALUE_TYPE_1_BYTE_2_SAMPLE = 2;
    // 1バイト3サンプル
    public static final int VALUE_TYPE_1_BYTE_3_SAMPLE = 3;


    private static final HashMap<String, Integer> valueTypeMap = new HashMap<String, Integer>(){{
        put(TI_SENSOR_TAG_SIMPLE_KEY, VALUE_TYPE_1_BYTE_1_SAMPLE);
        put(TI_SENSOR_TAG_ACCELEROMETER, VALUE_TYPE_1_BYTE_3_SAMPLE);
    }};

    // UUIDの名称の取得
    static String getUuidTitle(UUID uuid){
        String uuidStr = uuid.toString();
        String title = uuidTitleMap.get(uuidStr);
        if (title == null) title = "Unknown";
        return title;
    };

    /**
     * CharacteristicからValueの種別を返す
     * @param characteristic
     * @return
     */
    public static Integer getValueType(BluetoothGattCharacteristic characteristic) {
        Integer valueType = valueTypeMap.get(characteristic.getUuid().toString());
        if (valueType == null) valueType = VALUE_TYPE_UNKNOWN;
        return valueType;
    }


}
