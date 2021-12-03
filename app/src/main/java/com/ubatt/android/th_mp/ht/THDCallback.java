package com.ubatt.android.th_mp.ht;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import java.util.Calendar;

public interface THDCallback {
    void onTHDDateTimeReceived (@NonNull final BluetoothDevice device, Calendar calendar);
}
