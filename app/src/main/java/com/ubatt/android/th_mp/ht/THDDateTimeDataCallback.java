package com.ubatt.android.th_mp.ht;

import static no.nordicsemi.android.ble.common.callback.DateTimeDataCallback.readDateTime;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;

import androidx.annotation.NonNull;

import java.util.Calendar;

import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse;
import no.nordicsemi.android.ble.data.Data;

@SuppressWarnings("WeakerAccess")
public abstract class THDDateTimeDataCallback extends ProfileReadResponse implements THDCallback {
    public  THDDateTimeDataCallback() {

    }

    protected THDDateTimeDataCallback(final Parcel in) {
        super(in);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
        super.onDataReceived(device, data);
        final Calendar calendar = readDateTime(data, 0);
        if (calendar == null) {
            onInvalidDataReceived(device, data);
            return;
        }
        onTHDDateTimeReceived(device, calendar);
    }
}
