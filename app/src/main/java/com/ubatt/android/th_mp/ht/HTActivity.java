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

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.ubatt.android.th_mp.ConfigActivity;
import com.ubatt.android.th_mp.R;
import com.ubatt.android.th_mp.ht.settings.SettingsFragment;
import com.ubatt.android.th_mp.profile.BleProfileService;
import com.ubatt.android.th_mp.profile.BleProfileServiceReadyActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * HTSActivity is the main Health Thermometer activity. It implements {@link HTManagerCallbacks}
 * to receive callbacks from {@link HTManager} class. The activity supports portrait and landscape
 * orientations.
 */
public class HTActivity extends BleProfileServiceReadyActivity<HTService.HTSBinder> {
	@SuppressWarnings("unused")
	private final String TAG = "HTSActivity";
	private final String TEMP_ENTRIES = "Temp_Entries";
	private final String HUMI_ENTRIES = "Humi_Entries";

	private TextView tempValueView;
	private TextView humiValueView;
	private TextView tempUnitView;
	private TextView humiUnitView;
	private TextView batteryLevelView;
	private LineChart chart_temp;
	private LineChart chart_humi;

	private TextView tempCountView;
	private TextView humiCountView;
	private TextView tempMinView;
	private TextView tempMaxView;
	private TextView humiMinView;
	private TextView humiMaxView;
	private TextView ACTextViwe;

	private Button menu00Button;
	private Button menu01Button;
	private Button menu02Button;
	private Button menu03Button;
	private Button menu04Button;
	private Button menu05Button;
	private Button menu06Button;
	private Button menu07Button;

	private Button menuButton;
	private Button resetButton;
	private Button connectButton;

	private byte[] modeByte;

	private Integer ACvalue;

	private Dialog menuDialog;

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private float temp_startIndex;
	private float humi_startIndex;
	private float temp_avrg;
	private float temp_minmax = 30;
	private float humi_avrg = 30;
	private float humi_minmax;

	private boolean pause = false;



	private long backKeyPressedTime = 0;

	private Toast toast;

	List<Entry> temp_entries = new ArrayList<Entry>();
	List<Entry> humi_entries = new ArrayList<Entry>();
	List<String> dates = new ArrayList<String>();

	THDBHelper thdbHelper = null;

	public static Context context_main;

	@RequiresApi(api = Build.VERSION_CODES.N)
	@Override
	protected void onCreateView(final Bundle savedInstanceState) {
		setContentView(R.layout.activity_feature_hts);
		setGUI();
		init_tables();
		save_init_value();

		context_main = this;
		/*
		startIndex = 0;
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(HTActivity.this);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(KEY_ENTRIES, null);
		editor.apply(); */
		modeByte = new byte[1];
		temp_startIndex = 0;
		humi_startIndex = 0;

		SQLiteDatabase db = thdbHelper.getWritableDatabase();

//		Cursor cursor = db.rawQuery("SELECT * FROM DEV_DATA WHERE data_type = 4",null);
//		if (cursor.moveToFirst()) {
//			for (int i = 0 ; i < cursor.getCount() ; i++)
//			{
//				Log.v("skinny_OYM", "saved DB data load type 4 : " + cursor.getFloat(7) + " Data count : " + (i+1) + " / " +cursor.getCount());
//				temp_entries.add(new Entry(temp_startIndex, cursor.getFloat(7)));
//				temp_startIndex++;
//				cursor.move(1);
//			}
//			Entry temp_min = temp_entries.stream().min(Comparator.comparing(Entry::getY)).orElseThrow(NoSuchElementException::new);
//			Entry temp_max = temp_entries.stream().max(Comparator.comparing(Entry::getY)).orElseThrow(NoSuchElementException::new);
//			temp_avrg = (temp_max.getY() + temp_min.getY()) / 2;
//			temp_minmax = temp_max.getY() - temp_min.getY();
//		}
//
//		cursor = db.rawQuery("SELECT * FROM DEV_DATA WHERE data_type = 3",null);
//		if (cursor.moveToFirst()) {
//			for (int i = 0 ; i < cursor.getCount() ; i++)
//			{
//				Log.v("skinny_OYM", "saved DB data load type 3 : " + cursor.getFloat(7) + " Data count : " + (i+1) + " / " +cursor.getCount());
//				humi_entries.add(new Entry(humi_startIndex, cursor.getFloat(7)));
//				humi_startIndex++;
//				cursor.move(1);
//			}
//			Entry humi_min = humi_entries.stream().min(Comparator.comparing(Entry::getY)).orElseThrow(NoSuchElementException::new);
//			Entry humi_max = humi_entries.stream().max(Comparator.comparing(Entry::getY)).orElseThrow(NoSuchElementException::new);
//			humi_avrg = (humi_max.getY() + humi_min.getY()) / 2;
//			humi_minmax = humi_max.getY() - humi_min.getY();
//		}
//		cursor.close();

		chart_temp = (LineChart) findViewById(R.id.chart_temp);
		chart_temp.setDescription(null);
		chart_temp.getLegend().setEnabled(false);
		//chart_temp.getAxisLeft().setDrawLabels(false);
		chart_temp.getXAxis().setEnabled(false);
		chart_temp.getAxisLeft().setDrawGridLines(true);
		chart_temp.getXAxis().setDrawGridLines(false);
		//chart_temp.setOnChartValueSelectedListener(this);

		XAxis temp_xAxis;
		{
			temp_xAxis = chart_temp.getXAxis();
		}
		YAxis temp_yAxis;
		{
			temp_yAxis = chart_temp.getAxisLeft();
			chart_temp.getAxisRight().setEnabled(false);

			temp_yAxis.setAxisMaximum(135f);
			temp_yAxis.setAxisMinimum(-35f);
			temp_yAxis.enableGridDashedLine(10f, 10f, 0f);
			temp_yAxis.setZeroLineColor(Color.BLUE);
		}
		if (temp_minmax > 170) {
			temp_minmax = 170;
		} else if (temp_minmax < 70) {
			temp_minmax = 70;
		}

		chart_temp.zoom(1.0f,170f/temp_minmax,0f,0f);
		chart_temp.centerViewToY(temp_avrg, YAxis.AxisDependency.LEFT);


		chart_humi = (LineChart) findViewById(R.id.chart_humi);
		chart_humi.setDescription(null);
		chart_humi.getLegend().setEnabled(false);
		//chart_humi.getAxisLeft().setDrawLabels(false);
		chart_humi.getXAxis().setEnabled(false);
		chart_humi.getAxisLeft().setDrawGridLines(true);
		chart_humi.getXAxis().setDrawGridLines(false);

		XAxis humi_xAxis;
		{
			humi_xAxis = chart_humi.getXAxis();
		}
		YAxis humi_yAxis;
		{
			humi_yAxis = chart_humi.getAxisLeft();
			chart_humi.getAxisRight().setEnabled(false);

			humi_yAxis.setAxisMaximum(100f);
			humi_yAxis.setAxisMinimum(0f);
			humi_yAxis.enableGridDashedLine(10f, 10f, 0f);
			humi_yAxis.setZeroLineColor(Color.BLUE);
		}
		if (humi_minmax > 100) {
			humi_minmax = 100;
		} else if (humi_minmax < 70) {
			humi_minmax = 70;
		}

		chart_humi.zoom(1.0f,100f/humi_minmax,0f,0f);
		chart_humi.centerViewToY(humi_avrg, YAxis.AxisDependency.LEFT);


		LineDataSet temp_dataSet = new LineDataSet(temp_entries, ""); // add entries to dataset
		//dataSet.setFillAlpha(65);
		temp_dataSet.setFillColor(Color.RED);
		temp_dataSet.setColor(Color.RED);
		temp_dataSet.setCircleRadius(2f);
		temp_dataSet.setCircleColor(Color.RED);
		temp_dataSet.setCircleHoleColor(Color.RED);
		temp_dataSet.setLineWidth(1f);
		temp_dataSet.setCubicIntensity(0.2f);
		temp_dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
		temp_dataSet.setDrawValues(false);

		LineData temp_lineData = new LineData(temp_dataSet);
		chart_temp.setData(temp_lineData);
		chart_temp.invalidate();
		chart_temp.clear();

		LineDataSet humi_dataSet = new LineDataSet(humi_entries, ""); // add entries to dataset
		//dataSet.setFillAlpha(65);
		humi_dataSet.setFillColor(Color.BLUE);
		humi_dataSet.setColor(Color.BLUE);
		humi_dataSet.setCircleRadius(2f);
		humi_dataSet.setCircleColor(Color.BLUE);
		humi_dataSet.setCircleHoleColor(Color.BLUE);
		humi_dataSet.setLineWidth(1f);
		humi_dataSet.setCubicIntensity(0.2f);
		humi_dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
		humi_dataSet.setDrawValues(false);

		LineData humi_lineData = new LineData(humi_dataSet);
		chart_humi.setData(humi_lineData);
		chart_humi.invalidate();
		chart_humi.clear();

		resetButton = findViewById(R.id.action_reset);
		resetButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View view) {
				graphReset();
			}
		});

		menuButton = findViewById(R.id.button_menu);
		menuButton.setEnabled(false);
		menuButton.setBackgroundColor(Color.rgb(0xC0, 0xC0, 0xC0));
		menuButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openMenuDialog();
			}
		});

		resetButton.setOnTouchListener(menuTouched);
		menuButton.setOnTouchListener(menuTouched);
		connectButton = findViewById(R.id.action_connect);
		connectButton.setOnTouchListener(menuTouched);
	}

	public void openMenuDialog() {
		menuDialog = new Dialog(HTActivity.this);
		menuDialog.setContentView(R.layout.dialog_menu);
		menuDialog.show();

		menu00Button = menuDialog.findViewById(R.id.button_menu_00);
		menu01Button = menuDialog.findViewById(R.id.button_menu_01);
		menu02Button = menuDialog.findViewById(R.id.button_menu_02);
		menu03Button = menuDialog.findViewById(R.id.button_menu_03);
		menu04Button = menuDialog.findViewById(R.id.button_menu_04);
		menu05Button = menuDialog.findViewById(R.id.button_menu_05);
		menu06Button = menuDialog.findViewById(R.id.button_menu_06);
		menu07Button = menuDialog.findViewById(R.id.button_menu_07);

//		menu04Button.setVisibility(View.GONE);
//		menu05Button.setVisibility(View.GONE);

		menu00Button.setOnClickListener(menuClicked);
		menu01Button.setOnClickListener(menuClicked);
		menu02Button.setOnClickListener(menuClicked);
		menu03Button.setOnClickListener(menuClicked);
		menu04Button.setOnClickListener(menuClicked);
		menu05Button.setOnClickListener(menuClicked);
		menu06Button.setOnClickListener(menuClicked);
		menu07Button.setOnClickListener(menuClicked);

		menu00Button.setOnTouchListener(menuTouched);
		menu01Button.setOnTouchListener(menuTouched);
		menu02Button.setOnTouchListener(menuTouched);
		menu03Button.setOnTouchListener(menuTouched);
		menu04Button.setOnTouchListener(menuTouched);
		menu05Button.setOnTouchListener(menuTouched);
		menu06Button.setOnTouchListener(menuTouched);
		menu07Button.setOnTouchListener(menuTouched);
	}

	View.OnTouchListener menuTouched = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch ( v.getId() ) {
				case R.id.button_menu_00:
					if(event.getAction() == MotionEvent.ACTION_DOWN) {
						menu00Button.setBackgroundColor(Color.rgb(0x80, 0x80, 0x80));
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						menu00Button.setBackgroundColor(Color.rgb(0x00, 0xA9, 0xCE));
					}
					break;

				case R.id.button_menu_01:
					if(event.getAction() == MotionEvent.ACTION_DOWN) {
						menu01Button.setBackgroundColor(Color.rgb(0x80, 0x80, 0x80));
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						menu01Button.setBackgroundColor(Color.rgb(0x00, 0xA9, 0xCE));
					}
					break;

				case R.id.button_menu_02:
					if(event.getAction() == MotionEvent.ACTION_DOWN) {
						menu02Button.setBackgroundColor(Color.rgb(0x80, 0x80, 0x80));
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						menu02Button.setBackgroundColor(Color.rgb(0x00, 0xA9, 0xCE));
					}
					break;

				case R.id.button_menu_03:
					if(event.getAction() == MotionEvent.ACTION_DOWN) {
						menu03Button.setBackgroundColor(Color.rgb(0x80, 0x80, 0x80));
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						menu03Button.setBackgroundColor(Color.rgb(0x00, 0xA9, 0xCE));
					}
					break;

				case R.id.button_menu_04:
					if(event.getAction() == MotionEvent.ACTION_DOWN) {
						menu04Button.setBackgroundColor(Color.rgb(0x80, 0x80, 0x80));
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						menu04Button.setBackgroundColor(Color.rgb(0x00, 0xA9, 0xCE));
					}
					break;
				case R.id.button_menu_05:
					if(event.getAction() == MotionEvent.ACTION_DOWN) {
						menu05Button.setBackgroundColor(Color.rgb(0x80, 0x80, 0x80));
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						menu05Button.setBackgroundColor(Color.rgb(0x00, 0xA9, 0xCE));
					}
					break;
				case R.id.button_menu_06:
					if(event.getAction() == MotionEvent.ACTION_DOWN) {
						menu06Button.setBackgroundColor(Color.rgb(0x80, 0x80, 0x80));
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						menu06Button.setBackgroundColor(Color.rgb(0x00, 0xA9, 0xCE));
					}
					break;
				case R.id.button_menu_07:
					if(event.getAction() == MotionEvent.ACTION_DOWN) {
						menu07Button.setBackgroundColor(Color.rgb(0x80, 0x80, 0x80));
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						menu07Button.setBackgroundColor(Color.rgb(0x00, 0xA9, 0xCE));
					}
					break;
				case R.id.button_menu:
					if(event.getAction() == MotionEvent.ACTION_DOWN) {
						menuButton.setBackgroundColor(Color.rgb(0x80, 0x80, 0x80));
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						menuButton.setBackgroundColor(Color.rgb(0x00, 0x00, 0x00));
					}
					break;
				case R.id.action_reset:
					if(event.getAction() == MotionEvent.ACTION_DOWN) {
						resetButton.setBackgroundColor(Color.rgb(0x80, 0x80, 0x80));
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						resetButton.setBackgroundColor(Color.rgb(0x00, 0x00, 0x00));
					}
					break;
				case R.id.action_connect:
					if(event.getAction() == MotionEvent.ACTION_DOWN) {
						connectButton.setBackgroundColor(Color.rgb(0x80, 0x80, 0x80));
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						connectButton.setBackgroundColor(Color.rgb(0x00, 0x00, 0x00));
					}
					break;
			}
			return false;
		}
	};

	View.OnClickListener menuClicked = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch ( v.getId() ) {

				case R.id.button_menu_00:
					modeByte[0] = 0x00;
					LiveBroadcast(modeByte);
					menuDialog.dismiss();
					break;

				case R.id.button_menu_01:
					openConfigActivity();
					menuDialog.dismiss();
					break;

				case R.id.button_menu_02:
					modeByte[0] = 0x02;
					LiveBroadcast(modeByte);
					menuDialog.dismiss();
					break;

				case R.id.button_menu_03:
					modeByte[0] = 0x03;
					LiveBroadcast(modeByte);
					graphReset();
					menuDialog.dismiss();
					break;

				case R.id.button_menu_04:
					modeByte[0] = 0x04;
					LiveBroadcast(modeByte);
					menuDialog.dismiss();
					break;
				case R.id.button_menu_05:
					modeByte[0] = 0x05;
					LiveBroadcast(modeByte);
					menuDialog.dismiss();
					break;
				case R.id.button_menu_06:
					modeByte[0] = 0x06;
					graphReset();
					connectButton.setEnabled(false);
					connectButton.setBackgroundColor(Color.rgb(0xC0, 0xC0, 0xC0));
					LiveBroadcast(modeByte);
					menuDialog.dismiss();
					break;
				case R.id.button_menu_07:
					modeByte[0] = 0x07;
					graphReset();
					LiveBroadcast(modeByte);
					menuDialog.dismiss();
					break;
			}
		}
	};





	public void standbyBroadcast() {
		final Intent broadcast = new Intent(HTService.BROADCAST_THD_STANDBY_BUTTON);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
		//showToast("Download button clicked");
	}

	public void LiveBroadcast(byte[] modeByte) {
		final Intent broadcast = new Intent(HTService.BROADCAST_THD_LIVE_BUTTON);
		broadcast.putExtra("configByteArray",configByteArray());
		broadcast.putExtra("MODE_SELECT",modeByte);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	private void init_tables() {
		thdbHelper = new THDBHelper(this);
	}

	private void save_init_value() {
		SQLiteDatabase db = thdbHelper.getWritableDatabase();

		Cursor cursor_data_type = db.rawQuery("SELECT * FROM DATA_TYPE",null);
		Cursor cursor_dev_type = db.rawQuery("SELECT * FROM DEV_TYPE",null);
		Cursor cursor_user_info = db.rawQuery("SELECT * FROM USER_INFO",null);

		if (!cursor_data_type.moveToFirst()){
			db.execSQL("INSERT OR REPLACE INTO DATA_TYPE (sensor_type) VALUES (0);");
			db.execSQL("INSERT OR REPLACE INTO DATA_TYPE (sensor_type) VALUES (1);");
			db.execSQL("INSERT OR REPLACE INTO DATA_TYPE (sensor_type) VALUES (2);");
			Log.v("OYM", "check point 1");
		} else {
			for (int i = 0 ; i < cursor_data_type.getCount() ; i++)
			{
				Log.v("OYM", "data type inserted : " + cursor_data_type.getInt(1));
				cursor_data_type.moveToNext();
			}
//			db.execSQL("DELETE FROM DATA_TYPE;");
		}

		if (!cursor_dev_type.moveToFirst()){
			db.execSQL("INSERT OR REPLACE INTO DEV_TYPE (dev_type) VALUES (0);");
			Log.v("OYM", "check point 2");
		} else {
			for (int i = 0 ; i < cursor_dev_type.getCount() ; i++)
			{
				Log.v("OYM", "dev type inserted : " + cursor_dev_type.getInt(1));
				cursor_data_type.moveToNext();
			}
//			db.execSQL("DELETE FROM DATA_TYPE;");
		}

		if (!cursor_user_info.moveToFirst()){
			Log.v("OYM", "user info is null");
		}
		cursor_data_type.close();
		cursor_dev_type.close();
		cursor_user_info.close();

		db.close();

	}

	public byte[] configByteArray() {
		byte[] configByteArray = new byte[34];
		configByteArray[0] = 0x00; configByteArray[1] = 0x00; configByteArray[2] = 0x00; configByteArray[3] = (byte) 0xFE;

		// ***** LTL - Limit Temp Low *****
		configByteArray[4] = 0x00; configByteArray[5] = 0x00; configByteArray[6] = 0x00; configByteArray[7] = (byte) 0xFE;

		// ***** LHH - Limit Humi High *****
		configByteArray[8] = 0x00; configByteArray[9] = 0x00; configByteArray[10] = 0x00; configByteArray[11] = (byte) 0xFE;

		// ***** LHL - Limit Humi Low *****
		configByteArray[12] = 0x00; configByteArray[13] = 0x00; configByteArray[14] = 0x00; configByteArray[15] = (byte) 0xFE;

		// ***** SI - Store Interval *****
		configByteArray[16] = 0x03; configByteArray[17] = 0x00; configByteArray[18] = 0x00; configByteArray[19] = 0x00;

		// ***** DI - Detect Interval *****
		configByteArray[20] = 0x01; configByteArray[21] = 0x00; configByteArray[22] = 0x00; configByteArray[23] = 0x00;

		// ***** ASA - Auto End After *****
		configByteArray[24] =  0x00; configByteArray[25] = 0x00; configByteArray[26] = 0x00; configByteArray[27] = 0x00;

		// ***** AEA - Auto End After *****
		configByteArray[28] =  0x00; configByteArray[29] = 0x00; configByteArray[30] = 0x00; configByteArray[31] = 0x00;

		// ***** ST - Sensor Type *****
		configByteArray[32] = 0x02; configByteArray[33] = 0x00;

		return configByteArray;
	}

	public void upload() {
		AlertDialog.Builder ad = new AlertDialog.Builder(HTActivity.this);

		ad.setTitle("데이터 셋 식별자");       // 제목 설정
		ad.setMessage("");   // 내용 설정

// EditText 삽입하기
		final EditText et = new EditText(HTActivity.this);
		ad.setView(et);

// 확인 버튼 설정
		ad.setPositiveButton("확인", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.v(TAG, "Yes Btn Click");

				// Text 값 받아서 로그 남기기
				String value = et.getText().toString();
				Log.v(TAG, value);

				dialog.dismiss();     //닫기

				pause = true;

				final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(HTActivity.this);
				String json = preferences.getString(TEMP_ENTRIES, null);

				ArrayList<String> t = new ArrayList<String>();
				if (json != null) {
					Log.d("skinny", json);
					new OkHttpHandlerTransfer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"{\"title\" : \"" + value + "\", \"data\" : " + json + "}");
				}
				// Event
			}
		});


// 취소 버튼 설정
		ad.setNegativeButton("취소", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.v(TAG,"No Btn Click");
				dialog.dismiss();     //닫기
				// Event
			}
		});

// 창 띄우기
		ad.show();


		/*pause = true;

		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(HTActivity.this);
		String json = preferences.getString(KEY_ENTRIES, null);

		ArrayList<String> t = new ArrayList<String>();
		if (json != null) {
			Log.d("skinny", json);
			new OkHttpHandlerTransfer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"{\"data\" : " + json + "}");
		}*/
	}

	public class OkHttpHandlerTransfer extends AsyncTask<String, Void, String> {

		OkHttpClient client = new OkHttpClient();
		String url = "https://api.pcohh.com/skinny/upload";

		@Override
		protected String doInBackground(String...params) {
			//Request.Builder builder = new Request.Builder();
			//builder.url(myApp.getMyUrl() + params[0]);

			//if (params[1] != null)
			//   builder.addHeader("Cookie", params[1]);

			//Request request = builder.build();
			Request request = new Request.Builder()
					.url(url)
					.post(RequestBody.create(MediaType.parse("application/json"), params[0]))
					.build();

			try {
				Log.d("skinny",url + "로 HTTP 요청 전송");
				Response response = client.newCall(request).execute();

				return response.body().string();
			}catch (Exception e){
				Log.e("skinny", e.toString() + "****************");
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String s) {
			super.onPostExecute(s);
			//txtString.setText(s);
			Log.d("skinny", "Result:" + s);
			if (s.contains("success")) {
				Log.d("skinny", "Sccess:");
				Toast.makeText(HTActivity.this, "전송 성공...", Toast.LENGTH_LONG).show();
				pause = false;
			}

			/*try {
				JSONObject jsonObject = new JSONObject(s);

				if (jsonObject.has("result_code")) {
					String result_code =  jsonObject.getString("result_code");
					String msg =  jsonObject.getString("msg");
					String shortage_yn = jsonObject.getString("shortage_yn");
					//String gps_yn = j
					postOfflineInvoiceTransfer(result_code, msg, shortage_yn, jsonObject.getJSONArray("invoice_result_list"));

				}
			} catch (Exception e) {
				Log.e(appName, e.toString());
				postOfflineInvoiceTransfer("failure", "Server connection error. Contact support@bwisesoft.com", "", null);
			}*/
		}
	}

	@Override
	protected void onInitialize(final Bundle savedInstanceState) {
		LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, makeIntentFilter());
	}

	@Override
	public void onBackPressed() {
		if (System.currentTimeMillis() > backKeyPressedTime + 2500) {
			backKeyPressedTime = System.currentTimeMillis();
			toast = Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
			toast.show();
			return;
		}
		if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
//			Intent in = new Intent(context_main, HTService.class);
//			context_main.stopService(in);
//			modeByte[0] = (byte) 0xff;
//			final Intent broadcast = new Intent(HTService.BROADCAST_THD_LIVE_BUTTON);
//			broadcast.putExtra("configByteArray",configByteArray());
//			broadcast.putExtra("MODE_SELECT",modeByte);
//			LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
			finish();
			toast.cancel();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d("Skinny_oym", "HTActivity destroy");
		LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
	}

	private void setGUI() {
		tempValueView = findViewById(R.id.text_temp_value);
		humiValueView = findViewById(R.id.text_humi_value);
		tempUnitView = findViewById(R.id.text_temp_unit);
		humiUnitView = findViewById(R.id.text_humi_unit);
		batteryLevelView = findViewById(R.id.battery);
		tempCountView = findViewById(R.id.text_temp_count);
		humiCountView = findViewById(R.id.text_humi_count);
		tempMaxView = findViewById(R.id.text_temp_max);
		tempMinView = findViewById(R.id.text_temp_min);
		humiMaxView = findViewById(R.id.text_humi_max);
		humiMinView = findViewById(R.id.text_humi_min);
		ACTextViwe = findViewById(R.id.AC_count);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setUnits();

		pause = false;



		/*View decorView = getWindow().getDecorView();
		decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);*/

		//tempValueView.setText("test");
	}

	@Override
	protected void setDefaultUI() {
		tempValueView.setText(R.string.not_available_value);
		humiValueView.setText(R.string.not_available_value);
		batteryLevelView.setText(R.string.not_available);
		tempCountView.setText("0");
		humiCountView.setText("0");
		tempMinView.setText("Min : ");
		tempMaxView.setText("Max : ");
		humiMinView.setText("Min : ");
		humiMaxView.setText("Max : ");
		setUnits();
	}

	private void setUnits() {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		final int unit = Integer.parseInt(preferences.getString(SettingsFragment.SETTINGS_UNIT, String.valueOf(SettingsFragment.SETTINGS_UNIT_DEFAULT)));

		switch (unit) {
			case SettingsFragment.SETTINGS_UNIT_C:
				this.tempUnitView.setText(R.string.hts_unit_celsius);
				break;
			case SettingsFragment.SETTINGS_UNIT_F:
				this.tempUnitView.setText(R.string.hts_unit_fahrenheit);
				break;
			case SettingsFragment.SETTINGS_UNIT_K:
				this.tempUnitView.setText(R.string.hts_unit_kelvin);
				break;
		}
	}

	@Override
	protected void onServiceBound(final HTService.HTSBinder binder) {
		onTemperatureMeasurementReceived(binder.getBluetoothDevice().getName(), binder.getTemperature(), binder.getType(), binder.getDatetime());
	}

	@Override
	protected void onServiceUnbound() {
		// not used
	}

	@Override
	protected int getLoggerProfileTitle() {
		return R.string.hts_feature_title;
	}

	@Override
	protected int getAboutTextId() {
		return R.string.hts_about_text;
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		//getMenuInflater().inflate(R.menu.settings_and_about, menu);
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setHomeButtonEnabled(false);      // Disable the button
			actionBar.setDisplayHomeAsUpEnabled(false); // Remove the left caret
			actionBar.setDisplayShowHomeEnabled(false); // Remove the icon
		} else {
			Log.d("skinny", "action bar is null");
		}

		return true;
	}

	@Override
	protected boolean onOptionsItemSelected(final int itemId) {
		/*switch (itemId) {
			case R.id.action_settings:
				final Intent intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				break;
		}*/
		return true;
	}

	@Override
	protected int getDefaultDeviceName() {
		return R.string.hts_default_name;
	}

	@Override
	protected UUID getFilterUUID() {
		return HTManager.HT_SERVICE_UUID;
	}

	@Override
	protected Class<? extends BleProfileService> getServiceClass() {
		return HTService.class;
	}

	@Override
	public void onServicesDiscovered(@NonNull final BluetoothDevice device, boolean optionalServicesFound) {
		// this may notify user or show some views
	}

	@Override
	public void onDeviceConnected(@NonNull final BluetoothDevice device) {
		super.onDeviceConnected(device);
		menuButton.setEnabled(true);
		menuButton.setBackgroundColor(Color.rgb(0x00, 0x00, 0x00));
		//showToast("Bluetooth connected");
	}

	@Override
	public void onDeviceDisconnected(@NonNull final BluetoothDevice device) {
		super.onDeviceDisconnected(device);
		menuButton.setEnabled(false);
		menuButton.setBackgroundColor(Color.rgb(0xC0, 0xC0, 0xC0));
		connectButton.setEnabled(true);
		connectButton.setBackgroundColor(Color.rgb(0x00, 0x00, 0x00));
		batteryLevelView.setText(R.string.not_available);
	}

	public void plot(Float value, int type,String datetime) {
		runOnUiThread(new Runnable() {
			@RequiresApi(api = Build.VERSION_CODES.N)
			@Override
			public void run() {
				//Log.d("skinny", String.valueOf(value));
				SQLiteDatabase db = thdbHelper.getWritableDatabase();

				Log.v("skinny_OYM", "Date time : " + datetime);
				if ( type == 4 ) {

					tempValueView.setText(getString(R.string.hts_value, value));

					db.execSQL("INSERT INTO DEV_DATA (date_time, data_type, sensor_data)" +
									"VALUES (datetime('" +  datetime + "'), " + type + ", " + value + ")");
//					Cursor cursor = db.rawQuery("SELECT * FROM DEV_DATA",null);
//					if (cursor.moveToFirst()){
//						Log.v("OYM", "dev data inserted : " + cursor.getFloat(7));
//						for (int i = 0 ; i < cursor.getCount()-1 ; i++)
//						{
//							cursor.moveToNext();
//							Log.v("OYM", "dev data inserted : " + cursor.getFloat(7));
//						}
//					}
//					cursor.close();

					temp_startIndex++;
					temp_entries.add(new Entry(temp_startIndex, value));
					Entry temp_min = temp_entries.stream().min(Comparator.comparing(Entry::getY)).orElseThrow(NoSuchElementException::new);
					Entry temp_max = temp_entries.stream().max(Comparator.comparing(Entry::getY)).orElseThrow(NoSuchElementException::new);
					tempMinView.setText("Min : " +getString(R.string.hts_value, temp_min.getY()));
					tempMaxView.setText("Max : " +getString(R.string.hts_value, temp_max.getY()));
					dates.add(datetime);
					if (temp_entries.size() > 200000) {
						temp_entries.remove(0);
						dates.remove(0);
					}

					LineDataSet temp_dataSet = new LineDataSet(temp_entries, ""); // add entries to dataset
					//dataSet.setFillAlpha(65);
					temp_dataSet.setFillColor(Color.RED);
					temp_dataSet.setColor(Color.RED);
					temp_dataSet.setCircleRadius(2f);
					temp_dataSet.setCircleColor(Color.RED);
					temp_dataSet.setCircleHoleColor(Color.RED);
					temp_dataSet.setLineWidth(1f);
					temp_dataSet.setCubicIntensity(0.2f);
					temp_dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
					temp_dataSet.setDrawValues(false);

					LineData temp_lineData = new LineData(temp_dataSet);
					if ( temp_startIndex == 1 ) {
						chart_temp.zoom(1.0f,170/70f,0f,0f);
						chart_temp.centerViewToY(30, YAxis.AxisDependency.LEFT);
					}
					chart_temp.setData(temp_lineData);
					chart_temp.invalidate();

					tempCountView.setText(String.valueOf(temp_entries.size()));

				} else if ( type == 3 ) {
					humiValueView.setText(getString(R.string.hts_value, value));

					db.execSQL("INSERT INTO DEV_DATA (date_time, data_type, sensor_data)" +
							"VALUES (datetime('" +  datetime + "'), " + type + ", " + value + ")");
//					Cursor cursor = db.rawQuery("SELECT * FROM DEV_DATA",null);
//				if (cursor.moveToFirst()){
//						Log.v("OYM", "dev data inserted : " + cursor.getFloat(7));
//						for (int i = 0 ; i < cursor.getCount()-1 ; i++)
//						{
//							cursor.moveToNext();
//							Log.v("OYM", "dev data inserted : " + cursor.getFloat(7));
//						}
//					}
//					cursor.close();

					humi_startIndex++;
					humi_entries.add(new Entry(humi_startIndex, value));
					Entry humi_min = humi_entries.stream().min(Comparator.comparing(Entry::getY)).orElseThrow(NoSuchElementException::new);
					Entry humi_max = humi_entries.stream().max(Comparator.comparing(Entry::getY)).orElseThrow(NoSuchElementException::new);
					humiMinView.setText("Min : " +getString(R.string.hts_value, humi_min.getY()));
					humiMaxView.setText("Max : " +getString(R.string.hts_value, humi_max.getY()));
					dates.add(datetime);
					if (humi_entries.size() > 200000) {
						humi_entries.remove(0);
						dates.remove(0);
					}

					LineDataSet humi_dataSet = new LineDataSet(humi_entries, ""); // add entries to dataset
					//dataSet.setFillAlpha(65);
					humi_dataSet.setFillColor(Color.BLUE);
					humi_dataSet.setColor(Color.BLUE);
					humi_dataSet.setCircleRadius(2f);
					humi_dataSet.setCircleColor(Color.BLUE);
					humi_dataSet.setCircleHoleColor(Color.BLUE);
					humi_dataSet.setLineWidth(1f);
					humi_dataSet.setCubicIntensity(0.2f);
					humi_dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
					humi_dataSet.setDrawValues(false);

					LineData humi_lineData = new LineData(humi_dataSet);
					if ( humi_startIndex == 1 ) {
						chart_humi.zoom(1.0f,100f/70f,0f,0f);
						chart_humi.centerViewToY(30, YAxis.AxisDependency.LEFT);
					}
					chart_humi.setData(humi_lineData);
					chart_humi.invalidate();

					humiCountView.setText(String.valueOf(humi_entries.size()));


				} else if (type == 1 ) {
					showToast("Download complete.");
					connectButton.setEnabled(true);
					connectButton.setBackgroundColor(Color.rgb(0x00, 0x00, 0x00));
				}

				db.close();
			}
		});

	}

	private void onTemperatureMeasurementReceived(String deviceName, Float value, int type, String datetime) {
		if (value != null) {
			final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
			//preferences.edit().
			final int unit = Integer.parseInt(preferences.getString(SettingsFragment.SETTINGS_UNIT,
					String.valueOf(SettingsFragment.SETTINGS_UNIT_DEFAULT)));

			switch (unit) {
				case SettingsFragment.SETTINGS_UNIT_F:
					value = value * 1.8f + 32f;
					break;
				case SettingsFragment.SETTINGS_UNIT_K:
					value += 273.15f;
					break;
				case SettingsFragment.SETTINGS_UNIT_C:
					break;
			}
			if (!pause)
				plot(value,type,datetime);
			//tempValueView.setText(getString(R.string.hts_value, value));
			Log.d("skinny", deviceName + "/" + String.valueOf(value));
		} else {
			tempValueView.setText(R.string.not_available_value);
			humiValueView.setText(R.string.not_available_value);
		}
	}

	public void onOTSRecived(int data) {
		Log.d("skinny_OYM", " AC Activity data = " + data );
		ACTextViwe.setText(Integer.toString(data));
	}

	public void onBatteryLevelChanged(final int value) {
		batteryLevelView.setText(getString(R.string.battery, value));
	}

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();

			if (HTService.BROADCAST_HTS_MEASUREMENT.equals(action)) {
				final String mac_address = intent.getStringExtra("mac_address");
				final String datetime = intent.getStringExtra("THD_datetime");
				final float value = intent.getFloatExtra(HTService.EXTRA_TEMPERATURE, 0.0f);
				final String deviceName = intent.getStringExtra(HTService.EXTRA_DEVICE_NAME);
				final int type = intent.getIntExtra("type",0);
				Log.d("skinny_OYM",  "Type # = " + type +" / value = " + value + " / mac address" + mac_address);
				// Update GUI
				onTemperatureMeasurementReceived(deviceName, value, type, datetime);
			} else if (HTService.BROADCAST_BATTERY_LEVEL.equals(action)) {
				final int batteryLevel = intent.getIntExtra(HTService.EXTRA_BATTERY_LEVEL, 0);
				// Update GUI
				onBatteryLevelChanged(batteryLevel);
			} else if (HTService.BROADCAST_THD_DATETIME.equals(action)) {
				String calendar = intent.getStringExtra(HTService.EXTRA_DATETIME);
				Log.d("skinny_OYM", " Current time = " + calendar);
				showToast(calendar);
			} else if (HTService.BROADCAST_THD_OTS_FEATURE.equals(action)) {
				int ACvalue = intent.getIntExtra("OTS_FEATURE",0);
				Log.d("skinny_OYM", " OTS Activity AC data = " + ACvalue);
				onOTSRecived(ACvalue);
			}
		}
	};

	private static IntentFilter makeIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(HTService.BROADCAST_HTS_MEASUREMENT);
		intentFilter.addAction(HTService.BROADCAST_BATTERY_LEVEL);
		intentFilter.addAction(HTService.BROADCAST_THD_DATETIME);
		intentFilter.addAction(HTService.BROADCAST_THD_OTS_FEATURE);
		return intentFilter;
	}

	public void openConfigActivity() {
		Intent intent = new Intent(this, ConfigActivity.class);
		startActivity(intent);
	}

	public void graphReset() {
		SQLiteDatabase db = thdbHelper.getWritableDatabase();
		db.execSQL("DELETE FROM DEV_DATA;");
		db.close();
		setDefaultUI();
		temp_startIndex = 0;
		humi_startIndex = 0;
		temp_entries.clear();
		humi_entries.clear();
		dates.clear();

		chart_temp.zoom(0f,0f,0,0);
		chart_humi.zoom(0f,0f,0,0);
		chart_temp.clear();
		chart_humi.clear();

		}



}
