/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.ubatt.android.th_mp.ht;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ubatt.android.th_mp.battery.BatteryManager;
import com.ubatt.android.th_mp.parser.TemperatureMeasurementParser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.common.callback.ht.TemperatureMeasurementDataCallback;
import no.nordicsemi.android.ble.common.profile.ht.TemperatureType;
import no.nordicsemi.android.ble.common.profile.ht.TemperatureUnit;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.log.LogContract;

/**
 * {@link HTManager} class performs {@link BluetoothGatt} operations for connection, service discovery,
 * enabling indication and reading characteristics. All operations required to connect to device
 * with BLE HT Service and reading health thermometer values are performed here.
 * {@link HTActivity} implements {@link HTManagerCallbacks} in order to receive callbacks of
 * {@link BluetoothGatt} operations.
 */
public class HTManager extends BatteryManager<HTManagerCallbacks> {


	/** Health Thermometer service UUID */
	final static UUID HT_SERVICE_UUID = UUID.fromString("00001809-0000-1000-8000-00805f9b34fb");
	final static UUID CURRENT_TIME_SERVICE_UUID = UUID.fromString("00001805-0000-1000-8000-00805f9b34fb");
	final static UUID DEVICE_INFORMATION_SERVICE_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
	private final static UUID BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");

	/** Health Thermometer Measurement characteristic UUID */
	private static final UUID HT_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A1C-0000-1000-8000-00805f9b34fb");
	private static final UUID TEMPERATURE_TYPE_CHARACTERISTIC_UUID = UUID.fromString("00002A1D-0000-1000-8000-00805f9b34fb");
	private static final UUID CURRENT_TIME_CHARACTERISTIC_UUID = UUID.fromString("00002a08-0000-1000-8000-00805f9b34fb");
	private static final UUID DIS_OTS_CHARACTERISTIC_UUID = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb");
	private final static UUID BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");

	private BluetoothGattCharacteristic htCharacteristic;
	private BluetoothGattCharacteristic temperatureTypeCharacteristic;
	private BluetoothGattCharacteristic currentTimeCharacteristic;
	private BluetoothGattCharacteristic OTSCharacteristic;

	byte[] stop = {0,};


		HTManager(final Context context) {
		super(context);
	}

	@NonNull
	@Override
	protected BatteryManagerGattCallback getGattCallback() {
		return new HTManagerGattCallback();
	}



	public void readDateTime() {
		readCharacteristic(currentTimeCharacteristic)
				.with(THDDateTimeCallback)
				.fail((device, status) -> log(Log.WARN,"OYM : THD Current Time characteristic not found"))
				.enqueue();
		//int cts_clicked = currentTimeCharacteristic.getProperties();
		//Log.d("Skinny", "cts_clicked : " + cts_clicked);

		readCharacteristic(OTSCharacteristic)
				.with(OTSCallback)
				.fail((device, status) -> log(Log.WARN,"OYM :  Device Information OTS characteristic not found / status = " + status))
				.enqueue();


		writeCharacteristic(temperatureTypeCharacteristic, stop )
				.with((device, data) -> log(LogContract.Log.Level.APPLICATION,
						"\"" + data.toString() + "\" OYM : Measure Stop"))
				.enqueue();
	}




	public void configUpload(byte[] configByteArray, byte[] modeByte) {

		switch (modeByte[0]) {
			case 0x00:
			case 0x02:
			case 0x03:
			case 0x04:
			case 0x05:
			case 0x06:
			case 0x07:
				writeCharacteristic(temperatureTypeCharacteristic, modeByte)
						.with((device, data) -> log(LogContract.Log.Level.APPLICATION,
								"\"" + data.toString() + "\" skinny_OYM - Mode selected : " + data ))
						.enqueue();
				break;
			case 0x01:
				StringBuilder sb = new StringBuilder();
				for(final byte b: configByteArray)
					sb.append(String.format("%02x ", b&0xff));
				Log.d("Skinny_oym", "OYM : config upload data = " + sb.toString());

				writeCharacteristic(OTSCharacteristic, configByteArray)
						.with((device, data) -> log(LogContract.Log.Level.APPLICATION,
								"\"" + data.toString() + "\" OTS sent OYM"))
						.enqueue();

				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						writeCharacteristic(temperatureTypeCharacteristic, modeByte)
								.with((device, data) -> log(LogContract.Log.Level.APPLICATION,
										"\"" + data.toString() + "\" skinny_OYM - Mode selected : " + data ))
								.enqueue();
					}
				},500);
				break;
			case (byte) 0x99:

				break;
			default:
				break;
		}

	}



	private DataReceivedCallback THDDateTimeCallback = new THDDateTimeDataCallback() {
		@Override
		public void onTHDDateTimeReceived(@NonNull BluetoothDevice device, Calendar calendar) {
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			String datetime = dateFormat.format(calendar.getInstance().getTime());
//			Log.v("OYM", "THD Date Time received: " + datetime);
			mCallbacks.onTHDDateTimeReceived(device,calendar);
		}
	};

	private DataReceivedCallback OTSCallback = new OTSDataCallback() {
		@Override
		public void onOTSReceived(@NonNull BluetoothDevice device, Data data) {
			Log.d("skinny_OYM" , "OTS manager read data : " + String.valueOf(data));
			mCallbacks.onOTSReceived(device,data);
		}
	};



	/**
	 * BluetoothGatt callbacks for connection/disconnection, service discovery,
	 * receiving indication, etc..
	 */
	private class HTManagerGattCallback extends BatteryManagerGattCallback {
		@Override
		protected void initialize() {
			super.initialize();



			setIndicationCallback(htCharacteristic)
					.with(new TemperatureMeasurementDataCallback() {
						@Override
						public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
							log(LogContract.Log.Level.APPLICATION, "\"" + TemperatureMeasurementParser.parse(data) + "\" received");
							super.onDataReceived(device, data);
							Log.d("skinny_oym", "receive data : "+ data.toString() );
						}

						@Override
						public void onTemperatureMeasurementReceived(@NonNull final BluetoothDevice device,
																	 final float temperature,
																	 @TemperatureUnit final int unit,
																	 @Nullable final Calendar calendar,
																	 @Nullable @TemperatureType final Integer type) {
							mCallbacks.onTemperatureMeasurementReceived(device, temperature, unit, calendar, type);
						}
					});
			enableIndications(htCharacteristic).enqueue();

			readCharacteristic(OTSCharacteristic)
					.with(OTSCallback)
					.done(device -> log(Log.INFO, "skinny_oym : OTS Read"))
					.fail((device, status) -> log(Log.WARN,"OYM :  Device Information OTS characteristic not found / status = " + status))
					.enqueue();


			Calendar cal = Calendar.getInstance();
			byte[] textByteArray = new byte[7];
			textByteArray[0] = (byte) (cal.get(Calendar.YEAR) & 0xff);
			textByteArray[1] = (byte) ((cal.get(Calendar.YEAR)>>8) & 0xff);
			textByteArray[2] = (byte) (cal.get(Calendar.MONTH)+1);
			textByteArray[3] = (byte) cal.get(Calendar.DATE);
			textByteArray[4] = (byte) cal.get(Calendar.HOUR_OF_DAY);
			textByteArray[5] = (byte) cal.get(Calendar.MINUTE);
			textByteArray[6] = (byte) cal.get(Calendar.SECOND);

			writeCharacteristic(currentTimeCharacteristic, textByteArray)
					.with((device, data) -> log(LogContract.Log.Level.APPLICATION,
							"\"" + data.toString() + "\" DTS sent OYM"))
			.enqueue();
			readBatteryLevelCharacteristic();
			enableBatteryLevelCharacteristicNotifications();


		}

		@Override
		protected boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
			final BluetoothGattService service = gatt.getService(HT_SERVICE_UUID);
			List<BluetoothGattService> service_list = gatt.getServices();
			final BluetoothGattService DT_service = service_list.get(6);
			List<BluetoothGattCharacteristic> DT_characteristic = DT_service.getCharacteristics();

			final BluetoothGattService DI_service = service_list.get(5);
			List<BluetoothGattCharacteristic> DI_characteristic = DI_service.getCharacteristics();

			int permissions = DT_characteristic.get(0).getPermissions();
			int properties = DT_characteristic.get(0).getProperties();

			Log.d("skinny_OYM"," CTS DT permission = " + permissions + " / properties = " +properties);

			if (service != null) {
				htCharacteristic = service.getCharacteristic(HT_MEASUREMENT_CHARACTERISTIC_UUID);
				temperatureTypeCharacteristic = service.getCharacteristic(TEMPERATURE_TYPE_CHARACTERISTIC_UUID);
				DT_characteristic.get(0).setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
				currentTimeCharacteristic = DT_characteristic.get(0);
				OTSCharacteristic = DI_characteristic.get(0);
				Log.d("skinny_OYM"," Characteristic list = " + DI_characteristic.get(0).getUuid().toString() );
				Log.d("skinny_OYM"," OTS setting");
				}
			return htCharacteristic != null;
		}


		@Override
		protected void onDeviceDisconnected() {
			super.onDeviceDisconnected();
			htCharacteristic = null;
			currentTimeCharacteristic = null;
			OTSCharacteristic = null;
		}
	}
	}
