package com.ubatt.android.th_mp.ht;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.data.Data;

public interface OTSCallback {
    void onOTSReceived (@NonNull final BluetoothDevice device, Data data);
}