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
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private float temp_startIndex;
	private float humi_startIndex;
	private float temp_avrg;
	private float temp_minmax;
	private float humi_avrg;
	private float humi_minmax;

	private boolean pause = false;

	List<Entry> temp_entries = new ArrayList<Entry>();
	List<Entry> humi_entries = new ArrayList<Entry>();
	List<String> dates = new ArrayList<String>();

	@RequiresApi(api = Build.VERSION_CODES.N)
	@Override
	protected void onCreateView(final Bundle savedInstanceState) {
		setContentView(R.layout.activity_feature_hts);
		setGUI();

		/*
		startIndex = 0;
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(HTActivity.this);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(KEY_ENTRIES, null);
		editor.apply(); */

		temp_startIndex = 0;
		humi_startIndex = 0;

		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		String json = preferences.getString(TEMP_ENTRIES, null);
		String json_humi = preferences.getString(HUMI_ENTRIES, null);

		ArrayList<String> t = new ArrayList<String>();
		ArrayList<String> h = new ArrayList<String>();
		if (json != null) {
			try {
				JSONArray a = new JSONArray(json);
				Log.d("skinny", "json array size:" + a.length());
				for (int i = 0; i < a.length(); i++) {
					//String en = a.optString(i);
					JSONObject obj = a.getJSONObject(i);
					String en = obj.getString("val");
					temp_startIndex++;
					temp_entries.add(new Entry(temp_startIndex, Float.parseFloat(en)));
					dates.add(obj.getString("dt"));
					//Log.d("skinny", String.valueOf(i) + "/" + en);
				}
				Entry temp_min = temp_entries.stream().min(Comparator.comparing(Entry::getY)).orElseThrow(NoSuchElementException::new);
				Entry temp_max = temp_entries.stream().max(Comparator.comparing(Entry::getY)).orElseThrow(NoSuchElementException::new);
				temp_avrg = (temp_max.getY() + temp_min.getY()) / 2;
				temp_minmax = temp_max.getY() - temp_min.getY();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		if (json_humi != null) {
			try {
				JSONArray a_humi = new JSONArray(json_humi);
				Log.d("skinny", "json array size:" + a_humi.length());
				for (int i = 0; i < a_humi.length(); i++) {
					//String en = a.optString(i);
					JSONObject obj = a_humi.getJSONObject(i);
					String en = obj.getString("val");
					humi_startIndex++;
					humi_entries.add(new Entry(humi_startIndex, Float.parseFloat(en)));
					dates.add(obj.getString("dt"));
					//Log.d("skinny", String.valueOf(i) + "/" + en);
				}
				Entry humi_min = humi_entries.stream().min(Comparator.comparing(Entry::getY)).orElseThrow(NoSuchElementException::new);
				Entry humi_max = humi_entries.stream().max(Comparator.comparing(Entry::getY)).orElseThrow(NoSuchElementException::new);
				humi_avrg = (humi_max.getY() + humi_min.getY()) / 2;
				humi_minmax = humi_max.getY() - humi_min.getY();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		/*TimerTask tt = new TimerTask() {
			@Override
			public void run() {
				Random random = new Random();
				//int randomNumber = random.nextInt(80 – 65) + 65;
				int randomNumber = random.nextInt(3900 - 3500) + 3500;
				//int randomNumber = random.nextInt(3900 - 3500) - 5000;
				float rN = (float)randomNumber / 100;
				//Log.d("skinny", "Testing..." + getString(R.string.hts_value, rN));
				if (!pause)
					plot(rN);
			}
		};

		Timer timer = new Timer();
		timer.schedule(tt, 0 * 60 * 1000, 2 * 1000);*/

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
		} else if (temp_minmax < 50) {
			temp_minmax = 50;
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
		} else if (humi_minmax < 50) {
			humi_minmax = 50;
		}

		chart_humi.zoom(1.0f,100f/humi_minmax,0f,0f);
		chart_humi.centerViewToY(humi_avrg, YAxis.AxisDependency.LEFT);


		Button resetButton = (Button) findViewById(R.id.action_reset);
		resetButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View view) {
				//Log.d("skinny", "test");
				temp_startIndex = 0;
				humi_startIndex = 0;
				temp_entries.clear();
				humi_entries.clear();
				dates.clear();
			}
		});

		Button uploadButton = (Button) findViewById(R.id.upload_data);
		uploadButton.setVisibility(View.GONE);
		uploadButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View view) {
				//Log.d("skinny", "upload click");
				// 서버로 전송시작
				upload();
				/*pause = true;

				final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(HTActivity.this);
				String json = preferences.getString(KEY_ENTRIES, null);

				ArrayList<String> t = new ArrayList<String>();
				if (json != null) {
					Log.d("skinny", json);
					new OkHttpHandlerTransfer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"{\"data\" : " + json + "}");
				}*/
			}

		});

		Button configButton = (Button) findViewById(R.id.config_button);
		configButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) { openConfigActivity(); }
		});

		Button downloadButton = (Button) findViewById(R.id.download_button);
		downloadButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				downLoadBroadcast();
			}
		});

		Button configUploadButton = (Button) findViewById(R.id.upload_button);
		configUploadButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				configUploadBroadcast();
			}
		});



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

	}

	public void downLoadBroadcast() {
		final Intent broadcast = new Intent(HTService.BROADCAST_TEST_DOWNLOAD_BUTTON);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
		//showToast("Download button clicked");
	}

	public void configUploadBroadcast() {
		final Intent broadcast = new Intent(HTService.BROADCAST_TEST_UPLOAD_BUTTON);
		broadcast.putExtra("configByteArray",configByteArray());
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	public byte[] configByteArray() {
		byte[] textByteArray = new byte[28];
		textByteArray[0] = (byte) 0x3C; textByteArray[1] = (byte) 0xf2; textByteArray[2] = (byte) 0xff; textByteArray[3] = (byte) 0xfe;
		textByteArray[4] = (byte) 0x4a; textByteArray[5] = (byte) 0x08; textByteArray[6] = (byte) 0x00; textByteArray[7] = (byte) 0xfe;
		textByteArray[8] = (byte) 0x32; textByteArray[9] = (byte) 0x0e; textByteArray[10] = (byte) 0x00; textByteArray[11] = (byte) 0xfe;
		textByteArray[12] = (byte) 0xaf; textByteArray[13] = (byte) 0x08; textByteArray[14] = (byte) 0x00; textByteArray[15] = (byte) 0xfe;
		textByteArray[16] = (byte) 0x05; textByteArray[17] = (byte) 0x00; textByteArray[18] = (byte) 0x00; textByteArray[19] = (byte) 0x00;
		textByteArray[20] = (byte) 0x02; textByteArray[21] = (byte) 0x00; textByteArray[22] = (byte) 0x00; textByteArray[23] = (byte) 0x00;
		textByteArray[24] = (byte) 0x07; textByteArray[25] = (byte) 0x08; textByteArray[26] = (byte) 0x09; textByteArray[27] = (byte) 0x11;
		return textByteArray;
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
	protected void onDestroy() {
		super.onDestroy();
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
		onTemperatureMeasurementReceived(binder.getBluetoothDevice().getName(), binder.getTemperature(), binder.getType());
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
	public void onDeviceDisconnected(@NonNull final BluetoothDevice device) {
		super.onDeviceDisconnected(device);
		batteryLevelView.setText(R.string.not_available);
	}

	public void plot(Float value, int type) {
		runOnUiThread(new Runnable() {
			@RequiresApi(api = Build.VERSION_CODES.N)
			@Override
			public void run() {
				//Log.d("skinny", String.valueOf(value));


				Calendar cal = Calendar.getInstance();

				if ( type == 4 ) {
					tempValueView.setText(getString(R.string.hts_value, value));

					temp_startIndex++;
					temp_entries.add(new Entry(temp_startIndex, value));
					Entry temp_min = temp_entries.stream().min(Comparator.comparing(Entry::getY)).orElseThrow(NoSuchElementException::new);
					Entry temp_max = temp_entries.stream().max(Comparator.comparing(Entry::getY)).orElseThrow(NoSuchElementException::new);
					tempMinView.setText("Min : " +getString(R.string.hts_value, temp_min.getY()));
					tempMaxView.setText("Max : " +getString(R.string.hts_value, temp_max.getY()));
					dates.add(sdf.format(cal.getTime()));
					if (temp_entries.size() > 200000) {
						temp_entries.remove(0);
						dates.remove(0);
					}
					final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(HTActivity.this);
					SharedPreferences.Editor editor = preferences.edit();
					JSONArray a = new JSONArray();
					try {

						//Log.d("skinny", sdf.format(cal.getTime()) + " " + String.valueOf(value));

						for (int i = 0; i < temp_entries.size(); i++) {
							JSONObject obj = new JSONObject();
							obj.put("dt", dates.get(i));
							obj.put("val", String.valueOf(temp_entries.get(i).getY()));
							//obj.put("val", dates.get(i));
							a.put(obj);

							//a.put(String.valueOf(entries.get(i).getY()));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

					if (!temp_entries.isEmpty()) {
						editor.putString(TEMP_ENTRIES, a.toString());
					} else {
						editor.putString(TEMP_ENTRIES, null);
					}
					editor.apply();
				} else if ( type == 3 ) {
					humiValueView.setText(getString(R.string.hts_value, value));

					humi_startIndex++;
					humi_entries.add(new Entry(humi_startIndex, value));
					Entry humi_min = humi_entries.stream().min(Comparator.comparing(Entry::getY)).orElseThrow(NoSuchElementException::new);
					Entry humi_max = humi_entries.stream().max(Comparator.comparing(Entry::getY)).orElseThrow(NoSuchElementException::new);
					humiMinView.setText("Min : " +getString(R.string.hts_value, humi_min.getY()));
					humiMaxView.setText("Max : " +getString(R.string.hts_value, humi_max.getY()));
					dates.add(sdf.format(cal.getTime()));
					if (humi_entries.size() > 200000) {
						humi_entries.remove(0);
						dates.remove(0);
					}
					final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(HTActivity.this);
					SharedPreferences.Editor editor = preferences.edit();
					JSONArray a = new JSONArray();
					try {

						//Log.d("skinny", sdf.format(cal.getTime()) + " " + String.valueOf(value));

						for (int i = 0; i < humi_entries.size(); i++) {
							JSONObject obj = new JSONObject();
							obj.put("dt", dates.get(i));
							obj.put("val", String.valueOf(humi_entries.get(i).getY()));
							//obj.put("val", dates.get(i));
							a.put(obj);

							//a.put(String.valueOf(entries.get(i).getY()));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

					if (!humi_entries.isEmpty()) {
						editor.putString(HUMI_ENTRIES, a.toString());
					} else {
						editor.putString(HUMI_ENTRIES, null);
					}
					editor.apply();
				}



				if ( type == 4 ) {
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

					tempCountView.setText(String.valueOf(temp_entries.size()));
				} else if ( type == 3) {
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

					humiCountView.setText(String.valueOf(humi_entries.size()));
				}

				/*entries.add(new Entry(1, 2));
				entries.add(new Entry(2, 3));
				entries.add(new Entry(3, 8));

				LineDataSet dataSet = new LineDataSet(entries, "Label"); // add entries to dataset
				LineData lineData = new LineData(dataSet);
				chart.setData(lineData);
				chart.invalidate(); // refresh*/
			}
		});

	}

	private void onTemperatureMeasurementReceived(String deviceName, Float value, int type) {
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
				plot(value,type);
			//tempValueView.setText(getString(R.string.hts_value, value));
			Log.d("skinny", deviceName + "/" + String.valueOf(value));
		} else {
			tempValueView.setText(R.string.not_available_value);
			humiValueView.setText(R.string.not_available_value);
		}
	}

	public void onBatteryLevelChanged(final int value) {
		batteryLevelView.setText(getString(R.string.battery, value));
	}

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();

			if (HTService.BROADCAST_HTS_MEASUREMENT.equals(action)) {
				final float value = intent.getFloatExtra(HTService.EXTRA_TEMPERATURE, 0.0f);
				final String deviceName = intent.getStringExtra(HTService.EXTRA_DEVICE_NAME);
				final int type = intent.getIntExtra("type",0);
				Log.d("skinny_OYM",  "Type # = " + type +" / value = " + value);
				// Update GUI
				onTemperatureMeasurementReceived(deviceName, value, type);
			} else if (HTService.BROADCAST_BATTERY_LEVEL.equals(action)) {
				final int batteryLevel = intent.getIntExtra(HTService.EXTRA_BATTERY_LEVEL, 0);
				// Update GUI
				onBatteryLevelChanged(batteryLevel);
			} else if (HTService.BROADCAST_THD_DATETIME.equals(action)) {
				String calendar = intent.getStringExtra(HTService.EXTRA_DATETIME);
				Log.d("skinny", " Current time = " + calendar);
				showToast(calendar);
			}
		}
	};

	private static IntentFilter makeIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(HTService.BROADCAST_HTS_MEASUREMENT);
		intentFilter.addAction(HTService.BROADCAST_BATTERY_LEVEL);
		intentFilter.addAction(HTService.BROADCAST_THD_DATETIME);
		return intentFilter;
	}

	public void openConfigActivity() {
		Intent intent = new Intent(this, ConfigActivity.class);
		startActivity(intent);
	}
}
