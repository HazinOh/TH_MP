package com.ubatt.android.th_mp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ubatt.android.th_mp.ht.HTActivity;
import com.ubatt.android.th_mp.ht.HTService;

import java.util.Objects;

public class ConfigActivity extends AppCompatActivity {

    private TextView sensorTypeText;
    private TextView startMeasureText;
    private TextView keepMeasureText;
    private TextView savePeriodText;
    private TextView limitText;

    private Button sensorTypeButton;
    private Button startMeasureButton;
    private Button keepMeasureButton;
    private Button savePeriodButton;
    private Button limitButton;
    private Button setStartButton;

    private Button startImmedi;
    private Button keepMeasureFull;

    private Button dialogOk;
    private Button dialogCancel;

    private RadioGroup sensorType;
    private RadioButton radio_sensor_TH;
    private RadioButton radio_sensor_H;
    private RadioButton radio_sensor_T;

    private EditText startMeasureTime;
    private EditText keepMeasureTime;
    private EditText savePeriodTime;
    private EditText detectPeriodTime;
    private EditText limitTempMin;
    private EditText limitTempMax;
    private EditText limitHumiMin;
    private EditText limitHumiMax;

    private Spinner timeUnitSprinner;
    private Spinner savePeriod;
    private Spinner detectPeriod;
    private Spinner tempUnitSprinner;

    private Dialog sensorDialog;
    private Dialog startMeasureDialog;
    private Dialog keepMeasureDialog;
    private Dialog savePeriodDialog;
    private Dialog limitDialog;

    private Toast toast;

    byte[] configByteArray = new byte[34];

    int LTH_value = 30 ;
    int LTL_value = 0 ;
    int LHH_value = 60 ;
    int LHL_value = 20 ;
    int SI_value = 3 ;
    int DI_value = 1 ;
    int ASA_value = 0 ;
    int AEA_value = 0;
    int ST = 2;

    int SI_unit = 0 ;
    int DI_unit = 0 ;
    int ASA_unit = 0 ;
    int AEA_unit = 0 ;
    int LT_unit = 0 ;

    int current_time_unit = 0;
    int current_time_unit_1 = 0;
    int current_temp_unit = 0;
    int[] time_unit_value = { 1, 60, 3600 };

    final String[] time_unit_text = { "Second", "Minute" , "Hour" };
    final String[] temp_unit_text = { "℃", "℉" };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        final Toolbar toolbar = findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        sensorTypeText = findViewById(R.id.text_sensorType);
        startMeasureText = findViewById(R.id.text_stratMeasure);
        keepMeasureText = findViewById(R.id.text_keepMeasure);
        savePeriodText = findViewById(R.id.text_savePeriod);
        limitText = findViewById(R.id.text_limit);

        sensorTypeButton = findViewById(R.id.sensorType_button);
        startMeasureButton = findViewById(R.id.startMeasure_button);
        keepMeasureButton = findViewById(R.id.keepMeasure_button);
        savePeriodButton = findViewById(R.id.savePeriod_button);
        limitButton = findViewById(R.id.limit_button);
        setStartButton = findViewById(R.id.setStart_button);

        sensorTypeButton.setBackgroundColor(Color.rgb(0xC0,0xC0,0xC0));
        startMeasureButton.setBackgroundColor(Color.rgb(0xC0,0xC0,0xC0));
        keepMeasureButton.setBackgroundColor(Color.rgb(0xC0,0xC0,0xC0));
        limitButton.setBackgroundColor(Color.rgb(0xC0,0xC0,0xC0));


        sensorTypeButton.setOnClickListener(ConfigClicked);
        startMeasureButton.setOnClickListener(ConfigClicked);
        keepMeasureButton.setOnClickListener(ConfigClicked);
        savePeriodButton.setOnClickListener(ConfigClicked);
        limitButton.setOnClickListener(ConfigClicked);
        setStartButton.setOnClickListener(ConfigClicked);

        sensorTypeButton.setOnTouchListener(ConfigTouched);
        startMeasureButton.setOnTouchListener(ConfigTouched);
        keepMeasureButton.setOnTouchListener(ConfigTouched);
        savePeriodButton.setOnTouchListener(ConfigTouched);
        limitButton.setOnTouchListener(ConfigTouched);
        setStartButton.setOnTouchListener(ConfigTouched);

         defaultConfigByteArray();
    }

    private void defaultConfigByteArray() {
//        // ***** LTH - Limit Temp High *****
//        configByteArray[0] = 0x1E; configByteArray[1] = 0x00; configByteArray[2] = 0x00; configByteArray[3] = (byte) 0xFE;
//
//        // ***** LTL - Limit Temp Low *****
//        configByteArray[4] = 0x00; configByteArray[5] = 0x00; configByteArray[6] = 0x00; configByteArray[7] = (byte) 0xFE;
//
//        // ***** LHH - Limit Humi High *****
//        configByteArray[8] = 0x3C; configByteArray[9] = 0x00; configByteArray[10] = 0x00; configByteArray[11] = (byte) 0xFE;
//
//        // ***** LHL - Limit Humi Low *****
//        configByteArray[12] = 0x14; configByteArray[13] = 0x00; configByteArray[14] = 0x00; configByteArray[15] = (byte) 0xFE;
//
//        // ***** SI - Store Interval *****
//        configByteArray[16] = 0x00; configByteArray[17] = 0x00; configByteArray[18] = 0x00; configByteArray[19] = 0x3c;
//
//        // ***** DI - Detect Interval *****
//        configByteArray[20] = 0x00; configByteArray[21] = 0x00; configByteArray[22] = 0x00; configByteArray[23] = 0x05;
//
//        // ***** ASA - Auto Start After *****
//        configByteArray[24] = 0x00; configByteArray[25] = 0x00; configByteArray[26] = 0x00; configByteArray[27] = 0x00;
//
//        // ***** AEA - Auto End After *****
//        configByteArray[28] =  0x00; configByteArray[29] = 0x00; configByteArray[30] = 0x00; configByteArray[31] = 0x00;
//
//        // ***** ST - Sensor Type *****
//        configByteArray[32] = 0x00; configByteArray[33] = 0x02;
        // ***** LTH - Limit Temp High *****
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


    }

    public  boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    View.OnTouchListener ConfigTouched =new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch ( v.getId() ){

                case R.id.sensorType_button:
//                    if(event.getAction() == MotionEvent.ACTION_DOWN) {
//                        sensorTypeButton.setBackgroundColor(Color.rgb(0x80, 0x80, 0x80));
//                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
//                        sensorTypeButton.setBackgroundColor(Color.rgb(0x00, 0xA9, 0xCE));
//                    }
                    break;

                case R.id.startMeasure_button:
//                    if(event.getAction() == MotionEvent.ACTION_DOWN) {
//                        startMeasureButton.setBackgroundColor(Color.rgb(0x80, 0x80, 0x80));
//                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
//                        startMeasureButton.setBackgroundColor(Color.rgb(0x00, 0xA9, 0xCE));
//                    }
                    break;

                case R.id.keepMeasure_button:
//                    if(event.getAction() == MotionEvent.ACTION_DOWN) {
//                        keepMeasureButton.setBackgroundColor(Color.rgb(0x80, 0x80, 0x80));
//                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
//                        keepMeasureButton.setBackgroundColor(Color.rgb(0x00, 0xA9, 0xCE));
//                    }
                    break;

                case R.id.savePeriod_button:
                    if(event.getAction() == MotionEvent.ACTION_DOWN) {
                        savePeriodButton.setBackgroundColor(Color.rgb(0x80, 0x80, 0x80));
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        savePeriodButton.setBackgroundColor(Color.rgb(0x00, 0xA9, 0xCE));
                    }
                    break;

                case R.id.limit_button:
//                    if(event.getAction() == MotionEvent.ACTION_DOWN) {
//                        limitButton.setBackgroundColor(Color.rgb(0x80, 0x80, 0x80));
//                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
//                        limitButton.setBackgroundColor(Color.rgb(0x00, 0xA9, 0xCE));
//                    }
                    break;

                case R.id.setStart_button:
                    if(event.getAction() == MotionEvent.ACTION_DOWN) {
                        setStartButton.setBackgroundColor(Color.rgb(0x80, 0x80, 0x80));
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        setStartButton.setBackgroundColor(Color.rgb(0x00, 0xA9, 0xCE));
                    }
                    break;
                case R.id.button_ok:
                    if(event.getAction() == MotionEvent.ACTION_DOWN) {
                        dialogOk.setBackgroundColor(Color.rgb(0x80, 0x80, 0x80));
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        dialogOk.setBackgroundColor(Color.rgb(0x00, 0xA9, 0xCE));
                    }
                    break;
                case R.id.button_cancel:
                    if(event.getAction() == MotionEvent.ACTION_DOWN) {
                        dialogCancel.setBackgroundColor(Color.rgb(0x80, 0x80, 0x80));
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        dialogCancel.setBackgroundColor(Color.rgb(0x00, 0xA9, 0xCE));
                    }
                    break;

            }
            return false;
        }
    };

    View.OnClickListener ConfigClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch ( v.getId() ){

                case R.id.sensorType_button:
                    //sensorTypeConfig();
                    showToast("기능 준비중 입니다.");
                    break;

                case R.id.startMeasure_button:
                    //startMeasureConfig();
                    showToast("기능 준비중 입니다.");
                    break;

                case R.id.keepMeasure_button:
                    //keepMeasureConfig();
                    showToast("기능 준비중 입니다.");
                    break;

                case R.id.savePeriod_button:
                    savePeriodConfig();
                    break;

                case R.id.limit_button:
                    //limitConfig();
                    showToast("기능 준비중 입니다.");
                    break;

                case R.id.setStart_button:
                    setStart();
                    finish();
                    break;
            }
        }
    };

    public void setStart() {
        configByteArray[16] = (byte) (SI_value*time_unit_value[SI_unit]);
        configByteArray[17] = (byte) ((SI_value*time_unit_value[SI_unit])>>8);

        configByteArray[20] = (byte) (DI_value*time_unit_value[SI_unit]);
        configByteArray[21] = (byte) ((DI_value*time_unit_value[SI_unit])>>8);

        configByteArray[24] = (byte) (ASA_value*time_unit_value[ASA_unit]);
        configByteArray[25] = (byte) (ASA_value*time_unit_value[ASA_unit]>>8);

        configByteArray[28] = (byte) (AEA_value*time_unit_value[AEA_unit]);
        configByteArray[29] = (byte) (AEA_value*time_unit_value[AEA_unit]>>8);

        ((HTActivity)HTActivity.context_main).graphReset();

        byte[] modeByte ={0x01};
        final Intent broadcast = new Intent(HTService.BROADCAST_THD_LIVE_BUTTON);
        broadcast.putExtra("configByteArray",configByteArray);
        broadcast.putExtra("MODE_SELECT",modeByte);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);

    }


    public void sensorTypeConfig() {
        sensorDialog = new Dialog(ConfigActivity.this);
        sensorDialog.setContentView(R.layout.dialog_sensortype);
        sensorDialog.show();

        sensorType = sensorDialog.findViewById(R.id.radiogroup_sensorType);
        radio_sensor_TH = sensorDialog.findViewById(R.id.radio_sensor_TH);
        radio_sensor_T = sensorDialog.findViewById(R.id.radio_sensor_T);
        radio_sensor_H = sensorDialog.findViewById(R.id.radio_sensor_H);

        dialogOk = sensorDialog.findViewById(R.id.button_ok);
        dialogCancel = sensorDialog.findViewById(R.id.button_cancel);

        dialogOk.setOnTouchListener(ConfigTouched);
        dialogCancel.setOnTouchListener(ConfigTouched);

        if ( ST == 0x02 ) {
            sensorTypeText.setText("Sensor type : temperature and humidity");
            radio_sensor_TH.setChecked(true);
        } else if ( ST == 0x01 ) {
            sensorTypeText.setText("Sensor type : humidity");
            radio_sensor_H.setChecked(true);
        } else if ( ST == 0x00 ) {
            sensorTypeText.setText("Sensor type : temperature");
            radio_sensor_T.setChecked(true);
        }

        dialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (sensorType.getCheckedRadioButtonId()){
                    case R.id.radio_sensor_TH:
                        sensorTypeText.setText("Sensor type : temperature and humidity");
                        ST = 2;
                        break;
                    case R.id.radio_sensor_T:
                        sensorTypeText.setText("Sensor type : temperature");
                        ST = 0;
                        break;
                    case R.id.radio_sensor_H:
                        sensorTypeText.setText("Sensor type : humidity");
                        ST = 1;
                        break;
                    default:
                        break;
                }
                sensorDialog.dismiss();
            }
        });

        dialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorDialog.dismiss();
            }
        });
    }


    public void startMeasureConfig() {
        startMeasureDialog = new Dialog(ConfigActivity.this);
        startMeasureDialog.setContentView(R.layout.dialog_startmeasure);
        startMeasureDialog.show();

        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.timeUnit, R.layout.spinner_custom);
        adapter.setDropDownViewResource(R.layout.spinner_custom);

        startImmedi = startMeasureDialog.findViewById(R.id.button_startImmedi);
        startImmedi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMeasureText.setText("Start measuring immediately");
                startMeasureDialog.dismiss();
            }
        });

        startMeasureTime = startMeasureDialog.findViewById(R.id.editText_startMeasure);
        startMeasureTime.setSelection(startMeasureTime.length());
        timeUnitSprinner = startMeasureDialog.findViewById(R.id.spinner_startMeasure);
        timeUnitSprinner.setAdapter(adapter);
        timeUnitSprinner.setSelection(ASA_unit);
        startMeasureTime.setText(String.valueOf(ASA_value));

        timeUnitSprinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("skinny_OYM",  "Start Measure Selected ID = " +  timeUnitSprinner.getSelectedItemId() +"/"+ position +"/"+ id );
                current_time_unit = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        dialogOk = startMeasureDialog.findViewById(R.id.button_ok);
        dialogCancel = startMeasureDialog.findViewById(R.id.button_cancel);

        dialogOk.setOnTouchListener(ConfigTouched);
        dialogCancel.setOnTouchListener(ConfigTouched);

        dialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ASA_unit = current_temp_unit;
                if ( startMeasureText.getText().toString().replace(" ","").equals("") ) startMeasureText.setText("0");

                    ASA_value = Integer.parseInt(startMeasureTime.getText().toString());
                    if (ASA_value == 0) {
                        startMeasureText.setText("Start measuring immediately");
                    } else {
                        startMeasureText.setText("Start measuring after time " + startMeasureTime.getText() + " " + time_unit_text[ASA_unit]);
                    }

                    Log.d("skinny_OYM", "ASA_value : " + ASA_value + " / ASA_unit : " + time_unit_value[ASA_unit] + " / * = " + (ASA_value * time_unit_value[ASA_unit]));
                    startMeasureDialog.dismiss();

            }
        });

        dialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMeasureDialog.dismiss();
            }
        });
    }

    public void keepMeasureConfig() {
        keepMeasureDialog = new Dialog(ConfigActivity.this);
        keepMeasureDialog.setContentView(R.layout.dialog_keepmeasure);
        keepMeasureDialog.show();

        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.timeUnit, R.layout.spinner_custom);
        adapter.setDropDownViewResource(R.layout.spinner_custom);

        keepMeasureFull = keepMeasureDialog.findViewById(R.id.button_keepmeasurefull);
        keepMeasureFull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keepMeasureText.setText("Keep measuring until storage is full");
                keepMeasureDialog.dismiss();
            }
        });

        keepMeasureTime = keepMeasureDialog.findViewById(R.id.editText_keepMeasure);
        timeUnitSprinner = keepMeasureDialog.findViewById(R.id.spinner_keepMeasure);
        timeUnitSprinner.setAdapter(adapter);
        timeUnitSprinner.setSelection(AEA_unit);
        keepMeasureTime.setText(String.valueOf(AEA_value));

        timeUnitSprinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Log.d("skinny_OYM",  "Start Measure Selected ID = " +  timeUnitSprinner.getSelectedItemId() +"/"+ position +"/"+ id );
                current_time_unit = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        dialogOk = keepMeasureDialog.findViewById(R.id.button_ok);
        dialogCancel = keepMeasureDialog.findViewById(R.id.button_cancel);

        dialogOk.setOnTouchListener(ConfigTouched);
        dialogCancel.setOnTouchListener(ConfigTouched);

        dialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AEA_unit = current_time_unit;
                if ( keepMeasureText.getText().toString().replace(" ","").equals("") ) keepMeasureText.setText("0");

                AEA_value = Integer.parseInt(keepMeasureTime.getText().toString());
                if (AEA_value == 0) {
                    keepMeasureText.setText("Keep measuring until storage is full");
                } else {
                    keepMeasureText.setText("Keep measuring " + keepMeasureTime.getText() + " " + time_unit_text[AEA_unit] + " after power-off");
                }

                Log.d("skinny_OYM", "AEA_value : " + AEA_value + " / ASA_unit : " + time_unit_value[AEA_unit] + " / * = " + (AEA_value * time_unit_value[AEA_unit]));
                keepMeasureDialog.dismiss();

            }
        });

        dialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keepMeasureDialog.dismiss();
            }
        });
    }

    public void savePeriodConfig() {
        savePeriodDialog = new Dialog(ConfigActivity.this);
        savePeriodDialog.setContentView(R.layout.dialog_saveperiod);
        savePeriodDialog.show();

        savePeriodTime = savePeriodDialog.findViewById(R.id.editText_savePeriod);
        detectPeriodTime = savePeriodDialog.findViewById(R.id.editText_detectPeriod);

        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.timeUnit, R.layout.spinner_custom);
        adapter.setDropDownViewResource(R.layout.spinner_custom);

        savePeriod = savePeriodDialog.findViewById(R.id.spinner_savePeriod);
        savePeriod.setAdapter(adapter);
        savePeriod.setSelection(SI_unit);
        savePeriodTime.setText(String.valueOf(SI_value));

        savePeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Log.d("skinny_OYM",  "Save Period Selected ID = " +  savePeriod.getSelectedItemId() +"/"+ position +"/"+ id );
                current_time_unit = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        detectPeriod = savePeriodDialog.findViewById(R.id.spinner_detectPeriod);
        detectPeriod.setAdapter(adapter);
        detectPeriod.setSelection(DI_unit);
        detectPeriodTime.setText(String.valueOf(DI_value));
        detectPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Log.d("skinny_OYM",  "Detect Period Selected ID = " +  detectPeriod.getSelectedItemId() +"/"+ position +"/"+ id );
                current_time_unit_1 = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        dialogOk = savePeriodDialog.findViewById(R.id.button_ok);
        dialogCancel = savePeriodDialog.findViewById(R.id.button_cancel);

        dialogOk.setOnTouchListener(ConfigTouched);
        dialogCancel.setOnTouchListener(ConfigTouched);

        dialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SI_unit = current_time_unit;
                DI_unit = current_time_unit_1;
                if ( savePeriodTime.getText().toString().replace(" ","").equals("") ) savePeriodTime.setText("0");
                if ( detectPeriodTime.getText().toString().replace(" ","").equals("") ) detectPeriodTime.setText("0");

                SI_value = Integer.parseInt(savePeriodTime.getText().toString());
                if (SI_value == 0) {
                    SI_value = 3;
                    SI_unit = 0;
                }
                DI_value = Integer.parseInt(detectPeriodTime.getText().toString());
                if (DI_value == 0) {
                    DI_value = 1;
                    DI_unit = 0;
                }


                savePeriodText.setText("Save period is " + String.valueOf(SI_value) +" "+  time_unit_text[SI_unit] + "\n" +
                            "Detection period is " + String.valueOf(DI_value) + " " +  time_unit_text[DI_unit] );

                Log.d("skinny_OYM",  "SI_value : " + SI_value + " / SI_unit : " +  time_unit_value[SI_unit] + " / * = " + (SI_value* time_unit_value[SI_unit]));
                Log.d("skinny_OYM",  "DI_value : " + DI_value + " / DI_unit : " +  time_unit_value[DI_unit] + " / * = " + (DI_value* time_unit_value[DI_unit]));

                savePeriodDialog.dismiss();
            }
        });

        dialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePeriodDialog.dismiss();
            }
        });
    }

    public void limitConfig() {
        limitDialog = new Dialog(ConfigActivity.this);
        limitDialog.setContentView(R.layout.dialog_limit);
        limitDialog.show();

        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.tempUnit, R.layout.spinner_custom);
        adapter.setDropDownViewResource(R.layout.spinner_custom);

        limitTempMin = limitDialog.findViewById(R.id.editText_limittemp_min);
        limitTempMax = limitDialog.findViewById(R.id.editText_limittemp_max);
        limitHumiMin = limitDialog.findViewById(R.id.editText_limithumi_min);
        limitHumiMax = limitDialog.findViewById(R.id.editText_limithumi_max);

        limitTempMin.setText(String.valueOf(LTL_value));
        limitTempMax.setText(String.valueOf(LTH_value));
        limitHumiMin.setText(String.valueOf(LHL_value));
        limitHumiMax.setText(String.valueOf(LHH_value));

        tempUnitSprinner = limitDialog.findViewById(R.id.spinner_tempunit);
        tempUnitSprinner.setAdapter(adapter);
        tempUnitSprinner.setSelection(LT_unit);
        tempUnitSprinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("skinny_OYM",  "Temp Unit Selected ID = " +  tempUnitSprinner.getSelectedItemId() +"/"+ position +"/"+ id );

                current_temp_unit = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        dialogOk = limitDialog.findViewById(R.id.button_ok);
        dialogCancel = limitDialog.findViewById(R.id.button_cancel);

        dialogOk.setOnTouchListener(ConfigTouched);
        dialogCancel.setOnTouchListener(ConfigTouched);

        dialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LT_unit = current_temp_unit;
                if ( limitTempMin.getText().toString().replace(" ","").equals("") ) limitTempMin.setText("0");
                if ( limitTempMax.getText().toString().replace(" ","").equals("") ) limitTempMax.setText("30");
                if ( limitHumiMin.getText().toString().replace(" ","").equals("") ) limitHumiMin.setText("20");
                if ( limitHumiMax.getText().toString().replace(" ","").equals("") ) limitHumiMax.setText("60");

                LTL_value = Integer.parseInt(limitTempMin.getText().toString());
                LTH_value = Integer.parseInt(limitTempMax.getText().toString());
                LHL_value = Integer.parseInt(limitHumiMin.getText().toString());
                LHH_value = Integer.parseInt(limitHumiMax.getText().toString());

                limitText.setText("Desired temperature is above "+ limitTempMin.getText() + temp_unit_text[current_temp_unit] +" and below " +
                                    limitTempMax.getText() + temp_unit_text[current_temp_unit] +"\n" +
                                     "Desired humidity is above " + limitHumiMin.getText() + "%" + " and below " +
                                        limitHumiMax.getText() + "%" );

                limitDialog.dismiss();
            }
        });

        dialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                limitDialog.dismiss();
            }
        });

    }

    protected void showToast(final String message) {
        toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();

    }
}



