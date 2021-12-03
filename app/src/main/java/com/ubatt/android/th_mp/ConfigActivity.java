package com.ubatt.android.th_mp;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

    final String[] timeUnit = new String[2];



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

        sensorTypeButton.setOnClickListener(ConfigClicked);
        startMeasureButton.setOnClickListener(ConfigClicked);
        keepMeasureButton.setOnClickListener(ConfigClicked);
        savePeriodButton.setOnClickListener(ConfigClicked);
        limitButton.setOnClickListener(ConfigClicked);
        setStartButton.setOnClickListener(ConfigClicked);

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

    View.OnClickListener ConfigClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch ( v.getId() ){

                case R.id.sensorType_button:
                    sensorTypeConfig();
                    break;

                case R.id.startMeasure_button:
                    startMeasureConfig();
                    break;

                case R.id.keepMeasure_button:
                    keepMeasureConfig();
                    break;

                case R.id.savePeriod_button:
                    savePeriodConfig();
                    break;

                case R.id.limit_button:
                    limitConfig();
                    break;
                case R.id.setStart_button:
                    finish();
                    break;
            }
        }
    };


    public void sensorTypeConfig() {
        sensorDialog = new Dialog(ConfigActivity.this);
        sensorDialog.setContentView(R.layout.dialog_sensortype);
        sensorDialog.show();

        sensorType = sensorDialog.findViewById(R.id.radiogroup_sensorType);

        dialogOk = sensorDialog.findViewById(R.id.button_startmeasure_ok);
        dialogCancel = sensorDialog.findViewById(R.id.button_startmeasure_cancel);

        dialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (sensorType.getCheckedRadioButtonId()){
                    case R.id.radio_sensor_TH:
                        sensorTypeText.setText("Sensor type : temperature and humidity");
                        break;
                    case R.id.radio_sensor_T:
                        sensorTypeText.setText("Sensor type : temperature");
                        break;
                    case R.id.radio_sensor_H:
                        sensorTypeText.setText("Sensor type : humidity");
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
        timeUnitSprinner = startMeasureDialog.findViewById(R.id.spinner_startMeasure);
        timeUnitSprinner.setAdapter(adapter);
        timeUnitSprinner.setSelection(1);

        timeUnitSprinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Log.d("skinny_OYM",  "Start Measure Selected ID = " +  timeUnitSprinner.getSelectedItemId() +"/"+ position +"/"+ id );
                switch (position) {
                    case 0:
                        timeUnit[0] = "Second";
                        break;
                    case 1:
                        timeUnit[0] = "Minute";
                        break;
                    case 2:
                        timeUnit[0] = "Hour";
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        dialogOk = startMeasureDialog.findViewById(R.id.button_startmeasure_ok);
        dialogCancel = startMeasureDialog.findViewById(R.id.button_startmeasure_cancel);

        dialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMeasureText.setText("Start measuring after time " + startMeasureTime.getText() +" "+ timeUnit[0].toString() );
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
        timeUnitSprinner.setSelection(2);

        timeUnitSprinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Log.d("skinny_OYM",  "Start Measure Selected ID = " +  timeUnitSprinner.getSelectedItemId() +"/"+ position +"/"+ id );
                switch (position) {
                    case 0:
                        timeUnit[0] = "Second";
                        break;
                    case 1:
                        timeUnit[0] = "Minute";
                        break;
                    case 2:
                        timeUnit[0] = "Hour";
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        dialogOk = keepMeasureDialog.findViewById(R.id.button_keepmeasure_ok);
        dialogCancel = keepMeasureDialog.findViewById(R.id.button_keepmeasure_cancel);

        dialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keepMeasureText.setText("Keep measuring " + keepMeasureTime.getText() +" "+ timeUnit[0].toString() + " after power-off" );
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
        savePeriod.setSelection(1);
        savePeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Log.d("skinny_OYM",  "Save Period Selected ID = " +  savePeriod.getSelectedItemId() +"/"+ position +"/"+ id );
                switch (position) {
                    case 0:
                        timeUnit[0] = "Second";
                        break;
                    case 1:
                        timeUnit[0] = "Minute";
                        break;
                    case 2:
                        timeUnit[0] = "Hour";
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        detectPeriod = savePeriodDialog.findViewById(R.id.spinner_detectPeriod);
        detectPeriod.setAdapter(adapter);
        detectPeriod.setSelection(0);
        detectPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Log.d("skinny_OYM",  "Detect Period Selected ID = " +  detectPeriod.getSelectedItemId() +"/"+ position +"/"+ id );
                switch (position) {
                    case 0:
                        timeUnit[1] = "Second";
                        break;
                    case 1:
                        timeUnit[1] = "Minute";
                        break;
                    case 2:
                        timeUnit[1] = "Hour";
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        dialogOk = savePeriodDialog.findViewById(R.id.button_saveperiod_ok);
        dialogCancel = savePeriodDialog.findViewById(R.id.button_saveperiod_cancel);

        dialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePeriodText.setText("Save period is " + savePeriodTime.getText() +" "+ timeUnit[0].toString() + "\n" +
                                            "Detection period is " + detectPeriodTime.getText() + " " + timeUnit[1].toString() );
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

        tempUnitSprinner = limitDialog.findViewById(R.id.spinner_tempunit);
        tempUnitSprinner.setAdapter(adapter);
        tempUnitSprinner.setSelection(0);
        tempUnitSprinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("skinny_OYM",  "Temp Unit Selected ID = " +  tempUnitSprinner.getSelectedItemId() +"/"+ position +"/"+ id );
                switch (position) {
                    case 0:
                        timeUnit[0] = "℃";
                        break;
                    case 1:
                        timeUnit[0] = "℉";
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        dialogOk = limitDialog.findViewById(R.id.button_limit_ok);
        dialogCancel = limitDialog.findViewById(R.id.button_limit_cancel);

        dialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                limitText.setText("Desired temperature is above "+ limitTempMin.getText() + timeUnit[0].toString() +" and below " +
                                    limitTempMax.getText() + timeUnit[0].toString()  +"\n" +
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
}



