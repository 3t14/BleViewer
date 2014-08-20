package com.dev_training.imos.ble_viewer;

/**
 * タイムスタンプ付きCharacteristic.valueを格納するためのクラス
 */
public class ValueWithTimestamp {
    byte[] value;
    long timestamp;

    public ValueWithTimestamp(byte[] value, long timestamp) {
        this.value = value;
        this.timestamp = timestamp;
    }
}
