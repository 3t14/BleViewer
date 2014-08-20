package com.dev_training.imos.ble_viewer;

/**
 *
 * バイト配列をHEX値の文字列に変換するためのクラス
 * Created by @3t14 on 2014/08/14.
 */
public class HexDump {

    public static String toHex(byte[] data){
        if (data == null) return "";
        StringBuffer buffer = new StringBuffer();

        for (int i=0; i<data.length; i++){
            buffer.append(String.format("%02X ", data[i]));
        }
        return buffer.toString();
    }


}
