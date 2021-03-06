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

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ubatt.android.th_mp.FeaturesActivity;
import com.ubatt.android.th_mp.R;
import com.ubatt.android.th_mp.ToolboxApplication;
import com.ubatt.android.th_mp.profile.BleProfileService;
import com.ubatt.android.th_mp.profile.LoggableBleManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import no.nordicsemi.android.ble.common.profile.ht.TemperatureMeasurementCallback;
import no.nordicsemi.android.ble.common.profile.ht.TemperatureType;
import no.nordicsemi.android.ble.common.profile.ht.TemperatureUnit;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.log.Logger;

@SuppressWarnings("FieldCanBeLocal")
public class HTService extends BleProfileService implements HTManagerCallbacks {
	public static final String BROADCAST_HTS_MEASUREMENT = "com.ubatt.android.th_mp.hts.BROADCAST_HTS_MEASUREMENT";
	public static final String EXTRA_TEMPERATURE = "com.ubatt.android.th_mp.hts.EXTRA_TEMPERATURE";
	public static final String EXTRA_DEVICE_NAME = "com.ubatt.android.th_mp.EXTRA_DEVICE_NAME";

	public static final String BROADCAST_THD_STANDBY_BUTTON = "com.ubatt.android.THD.STANDBY_BUTTON";
	public static final String BROADCAST_THD_LIVE_BUTTON = "com.ubatt.android.THD.LIVE_BUTTON";

	public static final String BROADCAST_THD_DATETIME = "com.ubatt.android.TH.DATETIME";
	public static final String EXTRA_DATETIME = "com.ubatt.android.TH.EXTRA_DATETIME";

	public static final String BROADCAST_THD_OTS_FEATURE = "com.ubatt.android.TH.OTS_FEATURE";

	public static final String BROADCAST_BATTERY_LEVEL = "com.ubatt.android.th_mp.BROADCAST_BATTERY_LEVEL";
	public static final String EXTRA_BATTERY_LEVEL = "com.ubatt.android.th_mp.EXTRA_BATTERY_LEVEL";
	public static final String EXTRA_CURRENT_TIME = "com.ubatt.android.th_mp.EXTRA_CURRENT_TIME";

	private final static String ACTION_DISCONNECT = "com.ubatt.android.th_mp.hts.ACTION_DISCONNECT";

	private final static int NOTIFICATION_ID = 267;
	private final static int OPEN_ACTIVITY_REQ = 0;
	private final static int DISCONNECT_REQ = 1;
	/** The last received temperature value in Celsius degrees. */
	private Float temp;
	private int type;
	private String datetime;

	private byte[] configByteArray = new byte[34];
	private byte[] modeByte = new byte[1];

	private boolean receiveFlag = false;
	private boolean safeFlag = false;
	private boolean standbyFlag = false;

	@SuppressWarnings("unused")
	private HTManager manager;

	private final LocalBinder minder = new HTSBinder();

	public HTService() {
	}


	/**
	 * This local binder is an interface for the bonded activity to operate with the HTS sensor
	 */
	class HTSBinder extends LocalBinder {
		/**
		 * Returns the last received temperature value.
		 *
		 * @return Temperature value in Celsius.
		 */
		Float getTemperature() {
			return temp;
		}
		int getType() {
			return type;
		}
		String getDatetime() {
			return datetime;
		}
	}

	@Override
	protected LocalBinder getBinder() {
		return minder;
	}

	@Override
	protected LoggableBleManager<HTManagerCallbacks> initializeManager() {
		return manager = new HTManager(this);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		final IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_DISCONNECT);
		registerReceiver(disconnectActionBroadcastReceiver, filter);
		LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, THDDateTimeIntentFilter());
		configByteArray[16] = 0x03;
		configByteArray[17] = 0x00;
	}

	@Override
	public void onDestroy() {
		// when user has disconnected from the sensor, we have to cancel the notification that we've created some milliseconds before using unbindService
		Log.d("Skinny_oym", "Service destroy");
		safeFlag = true;
		cancelNotification();
		unregisterReceiver(disconnectActionBroadcastReceiver);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
		super.onDestroy();
	}

	@Override
	protected void onRebind() {
		stopForegroundService();
	}

	@Override
	protected void onUnbind() {
		startForegroundService();
	}

	@Override
	public void onDeviceDisconnected(@NonNull final BluetoothDevice device) {
		super.onDeviceDisconnected(device);
		safeFlag = true;
		temp = null;
	}

	@Override
	public void onTemperatureMeasurementReceived(@NonNull final BluetoothDevice device,
												 final float temperature, @TemperatureUnit final int unit,
												 @Nullable final Calendar calendar,
												 @Nullable @TemperatureType final Integer type) {
		temp = TemperatureMeasurementCallback.toCelsius(temperature, unit);

		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		final String calenderString = String.format(Locale.US, "%1$te %1$tb %1$tY, %1$tH:%1$tM:%1$tS", calendar);
		//Log.d("Skinny", "Date Time received test 1: " + calenderString );
		final String datetime = dateFormat.format(calendar.getTime());

		 if (type == 3 && modeByte[0] !=0x06) {
			int safeTimeMilliSec = 500;
			int safeTime = (int) configByteArray[16] | ((int) configByteArray[17]<<8) ;
			Log.d("Skinny_oym", "Safe time : " + safeTime );
			if (safeTime != 1) {
				safeTimeMilliSec = (safeTime-1) * 1000;
			}

			safeFlag = true;
			Log.d("Skinny_oym", "Safe Flag Changed : " + safeFlag + " safe time milliSec : " + safeTimeMilliSec);
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					safeFlag = false;
					Log.d("Skinny_oym", "Safe Flag Changed : " + safeFlag );
				}
			}, safeTimeMilliSec);
		}

		final Intent broadcast = new Intent(BROADCAST_HTS_MEASUREMENT);
		broadcast.putExtra(EXTRA_DEVICE, getBluetoothDevice());
		broadcast.putExtra("mac_address", getBluetoothDevice().getAddress());
		broadcast.putExtra("THD_datetime",datetime);
		broadcast.putExtra(EXTRA_DEVICE_NAME, getBluetoothDevice().getName());
		broadcast.putExtra(EXTRA_TEMPERATURE, temp);
		broadcast.putExtra("type", type);
		// ignore the rest
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);

		if (!bound) {
			// Here we may update the notification to display the current temperature.
			// TODO modify the notification here
		}
	}

	@Override
	public void onBatteryLevelChanged(@NonNull final BluetoothDevice device, final int batteryLevel) {
		final Intent broadcast = new Intent(BROADCAST_BATTERY_LEVEL);
		broadcast.putExtra(EXTRA_DEVICE, getBluetoothDevice());
		broadcast.putExtra(EXTRA_BATTERY_LEVEL, batteryLevel);
		broadcast.putExtra(EXTRA_CURRENT_TIME, 999);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onTHDDateTimeReceived(@NonNull BluetoothDevice device, @NonNull Calendar calendar) {
		final Intent broadcast = new Intent(BROADCAST_THD_DATETIME);
		broadcast.putExtra(EXTRA_DEVICE, getBluetoothDevice());
		final String calenderString = String.format(Locale.US, "%1$te %1$tb %1$tY, %1$tH:%1$tM:%1$tS", calendar);
		broadcast.putExtra(EXTRA_DATETIME, calenderString);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
		Log.d("Skinny_oym", "Date Time received: " + calenderString + "2nd");
	}

	@Override
	public void onOTSReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
		int ACvalue = 0;
		ACvalue |= (data.getByte(29) & (int)0xFF)<<24;
		ACvalue |= (data.getByte(28) & (int)0xFF)<<16;
		ACvalue |= (data.getByte(27) & (int)0xFF)<<8;
		ACvalue |= (data.getByte(26) & (int)0xFF);
		final Intent broadcast = new Intent(BROADCAST_THD_OTS_FEATURE);
		broadcast.putExtra(EXTRA_DEVICE, getBluetoothDevice());
		broadcast.putExtra("OTS_FEATURE",ACvalue);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
		Log.d("Skinny_oym", "OTS service data call back : " + data.toString());
		Log.d("Skinny_oym", "OTS AC value : " + ACvalue);
	}

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_THD_STANDBY_BUTTON.equals(action)) {
				manager.readDateTime();
			} else if (BROADCAST_THD_LIVE_BUTTON.equals(action)) {
				modeByte = intent.getByteArrayExtra("MODE_SELECT");
					if ( modeByte[0] == 0x00) {
						final Handler mHandler = new Handler();
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								if (safeFlag) {
									manager.configUpload(configByteArray, modeByte);
								} else {
									mHandler.postDelayed(this, 100);
									Log.d("Skinny_oym", "Not safe Mode Select Delayed");
								}
							}
						});
					} else {
						configByteArray = intent.getByteArrayExtra("configByteArray");
						manager.configUpload(configByteArray, modeByte);
						int safeTimeMilliSec = 500;
						int safeTime = (int) configByteArray[16] | ((int) configByteArray[17]<<8) ;
						Log.d("Skinny_oym", "Safe time : " + safeTime );
						if (safeTime != 1) {
							safeTimeMilliSec = (safeTime-1) * 1000;
						}

						safeFlag = true;
						Log.d("Skinny_oym", "Safe Flag Changed : " + safeFlag + " safe time milliSec : " + safeTimeMilliSec);
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								safeFlag = false;
								Log.d("Skinny_oym", "Safe Flag Changed : " + safeFlag );
							}
						}, safeTimeMilliSec);
					}


//				Message msg1 = configHandler.obtainMessage();
//				Bundle b1 = new Bundle();
//				b1.putByteArray("configByteArray",configByteArray);
//				b1.putByteArray("MODE_SELECT",modeByte);
//				msg1.setData(b1);
//				configHandler.sendMessage(msg1);

;
			}
		}
	};


	private static IntentFilter THDDateTimeIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BROADCAST_THD_STANDBY_BUTTON);
		intentFilter.addAction(BROADCAST_THD_LIVE_BUTTON);
		return intentFilter;
	}


	/**
	 * Sets the service as a foreground service
	 */
	private void startForegroundService(){
		// when the activity closes we need to show the notification that user is connected to the peripheral sensor
		// We start the service as a foreground service as Android 8.0 (Oreo) onwards kills any running background services
		final Notification notification = createNotification(R.string.hts_notification_connected_message, 0);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			startForeground(NOTIFICATION_ID, notification);
		} else {
			final NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			nm.notify(NOTIFICATION_ID, notification);
		}
	}

	/**
	 * Stops the service as a foreground service
	 */
	private void stopForegroundService(){
		// when the activity rebinds to the service, remove the notification and stop the foreground service
		// on devices running Android 8.0 (Oreo) or above
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			stopForeground(true);
		} else {
			cancelNotification();
		}
	}

	/**
	 * Creates the notification
	 *  @param messageResId
	 *            message resource id. The message must have one String parameter,<br />
	 *            f.e. <code>&lt;string name="name"&gt;%s is connected&lt;/string&gt;</code>
	 * @param defaults
	 */
	@SuppressWarnings("SameParameterValue")
	private Notification createNotification(final int messageResId, final int defaults) {
		final Intent parentIntent = new Intent(this, FeaturesActivity.class);
		parentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		final Intent targetIntent = new Intent(this, HTActivity.class);

		final Intent disconnect = new Intent(ACTION_DISCONNECT);
		final PendingIntent disconnectAction = PendingIntent.getBroadcast(this, DISCONNECT_REQ, disconnect, PendingIntent.FLAG_UPDATE_CURRENT);

		// both activities above have launchMode="singleTask" in the AndroidManifest.xml file, so if the task is already running, it will be resumed
		final PendingIntent pendingIntent = PendingIntent.getActivities(this, OPEN_ACTIVITY_REQ, new Intent[] { parentIntent, targetIntent }, PendingIntent.FLAG_UPDATE_CURRENT);
		final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ToolboxApplication.CONNECTED_DEVICE_CHANNEL);
		builder.setContentIntent(pendingIntent);
		builder.setContentTitle(getString(R.string.app_name)).setContentText(getString(messageResId, getDeviceName()));
		builder.setSmallIcon(R.drawable.ic_stat_notify_hts);
		builder.setShowWhen(defaults != 0).setDefaults(defaults).setAutoCancel(true).setOngoing(true);
		builder.addAction(new NotificationCompat.Action(R.drawable.ic_action_bluetooth, getString(R.string.hts_notification_action_disconnect), disconnectAction));

		return builder.build();
	}

	/**
	 * Cancels the existing notification. If there is no active notification this method does nothing
	 */
	private void cancelNotification() {
		final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nm.cancel(NOTIFICATION_ID);
	}

	/**
	 * This broadcast receiver listens for {@link #ACTION_DISCONNECT} that may be fired by pressing Disconnect action button on the notification.
	 */
	private final BroadcastReceiver disconnectActionBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			Logger.i(getLogSession(), "[Notification] Disconnect action pressed");
			if (isConnected())
				getBinder().disconnect();
			else
				stopSelf();
		}
	};

	@SuppressLint("HandlerLeak")
	final android.os.Handler configHandler = new android.os.Handler() {
		@Override
		public void handleMessage(Message msg) {
			Bundle bd = msg.getData();
			byte[] configByteArray = bd.getByteArray("configByteArray");
			byte[] modebyte = bd.getByteArray("MODE_SELECT");
			switch (modebyte[0]) {
				case 0x00 :
					manager.configUpload(configByteArray,modebyte);
					break;
				case 0x01 :
					manager.configUpload(configByteArray,modebyte);
					break;
				case 0x02 :
					manager.configUpload(configByteArray,modebyte);
					break;
				case 0x03 :
					manager.configUpload(configByteArray,modebyte);
					break;
				case 0x04 :
					manager.configUpload(configByteArray,modebyte);
					break;
				case 0x05 :
					manager.configUpload(configByteArray,modebyte);
					break;
				case 0x06 :
					manager.configUpload(configByteArray,modebyte);
					break;
				case 0x07 :
					manager.configUpload(configByteArray,modebyte);
					break;
				case (byte) 0x99 :
					manager.configUpload(configByteArray,modebyte);
					break;

			}
			Log.d("Skinny_oym", "OYM : handle message : " + modebyte[0]);
		}
	};




}
