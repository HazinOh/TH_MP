package com.ubatt.android.th_mp.battery;

import android.bluetooth.BluetoothDevice;

import java.util.Calendar;

import no.nordicsemi.android.ble.BleManagerCallbacks;
import no.nordicsemi.android.ble.common.profile.battery.BatteryLevelCallback;

public interface BatteryManagerCallbacks extends BleManagerCallbacks, BatteryLevelCallback {
    void onDateTimeReceived(BluetoothDevice device, Calendar calendar);
}
