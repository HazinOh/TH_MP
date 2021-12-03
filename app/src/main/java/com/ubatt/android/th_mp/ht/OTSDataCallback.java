package com.ubatt.android.th_mp.ht;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;

import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse;
import no.nordicsemi.android.ble.data.Data;

@SuppressWarnings("WeakerAccess")
public abstract class OTSDataCallback extends ProfileReadResponse implements OTSCallback{

    public  OTSDataCallback() {}

    protected OTSDataCallback(final Parcel in) {
        super(in);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
        super.onDataReceived(device, data);
        onOTSReceived(device,data);
    }
}
