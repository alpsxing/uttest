package com.ut50.bluetoothcontrol;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends Activity {

    // Debugging
    private static final String TAG = "MainActivity";
    private static final boolean D = true;

    // Message types sent from the BluetoothService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_CONN_FAIL = 5;
    public static final int MESSAGE_CONN_LOST = 6;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_CHOOSE_DEVICE = 2;
    private static final int REQUEST_SCAN_DEVICE = 3;

    private static final int STAGE_BLUETOOTH_DISABLED = 1;
    private static final int STAGE_BLUETOOTH_NO_DEVICE = 2;
    private static final int STAGE_BLUETOOTH_IDLE = 3;
    private static final int STAGE_BLUETOOTH_CONNECTING = 4;
    private static final int STAGE_BLUETOOTH_CONNECTED = 5;
    private static final int STAGE_BLUETOOTH_MONITORING = 6;
    private static final int STAGE_BLUETOOTH_CALIBRATING = 7;
    private static final int STAGE_BLUETOOTH_CALIBRATE_START = 8;
    private static final int STAGE_BLUETOOTH_CALIBRATE_STOP = 9;
    private static final int STAGE_BLUETOOTH_PENDING = 255;

    private static final int COUNT_DOWN_TIME = 5000;
    private static final int COUNT_DOWN_STEP = 5000;

    private static final byte[] MONITOR_REQUEST = {0x31};
    private static final byte[] RESULT_LOW_BOUND = {0x0D, 0x0A, 0x30, 0x30, 0x30, 0x2E, 0x30, 0x00};
    private static final byte[] RESULT_HIGH_BOUND = {0x0D, 0x0A, 0x33, 0x39, 0x39, 0x2E, 0x39, (byte)0xFF};
    private static final byte[] START_CALIBRATION_REQUEST = {(byte)0xC0};
    private static final byte[] STOP_CALIBRATION_REQUEST = {(byte)0xC1};

    private static final int RESULT_LENGTH = 8;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice m_device = null;
    private BluetoothService mBluetoothService;

    private Button mButtonEnableBluetooth;
    private Button mButtonScanBluetooth;
    private Button mButtonConnectBluetooth;
    private Button mButtonCalibrate;
    private Button mButtonMonitor;
    private TextView mTextResult;

    private int mStage;
    private byte[] mResult = new byte[RESULT_LENGTH];
    private int mResultLen = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);

        mButtonEnableBluetooth = (Button)findViewById(R.id.button_enable_bluetooth);
        mButtonEnableBluetooth.setOnClickListener(clickListener);
        mButtonScanBluetooth = (Button)findViewById(R.id.button_scan_devices);
        mButtonScanBluetooth.setOnClickListener(clickListener);
        mButtonConnectBluetooth = (Button)findViewById(R.id.button_connect);
        mButtonConnectBluetooth.setOnClickListener(clickListener);
        mButtonCalibrate = (Button)findViewById(R.id.button_calibrate);
        mButtonCalibrate.setOnClickListener(clickListener);
        mButtonMonitor = (Button)findViewById(R.id.button_monitor);
        mButtonMonitor.setOnClickListener(clickListener);

        mTextResult = (TextView)findViewById(R.id.textView_result);
        mTextResult.setVisibility(View.INVISIBLE);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(this, R.string.toast_bluetooth_nav, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mBluetoothService = new BluetoothService(this, mHandler);
        setButtonState(STAGE_BLUETOOTH_PENDING);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!mBluetoothAdapter.isEnabled()) {
            setButtonState(STAGE_BLUETOOTH_DISABLED);
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }else {
            if(isDeviceAvailable()) {
                setButtonState(STAGE_BLUETOOTH_IDLE);
            }
            else {
                setButtonState(STAGE_BLUETOOTH_NO_DEVICE);
                Toast.makeText(this, R.string.toast_bluetooth_nd, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothService.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mBluetoothAdapter.isEnabled()) {
            setButtonState(STAGE_BLUETOOTH_DISABLED);
        }
        else {
            if(isDeviceAvailable()) {
                setButtonState(STAGE_BLUETOOTH_IDLE);
            }
            else {
                setButtonState(STAGE_BLUETOOTH_NO_DEVICE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    if(isDeviceAvailable()) {
                        setButtonState(STAGE_BLUETOOTH_IDLE);
                    }
                    else {
                        setButtonState(STAGE_BLUETOOTH_NO_DEVICE);
                    }
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.toast_bluetooth_ne, Toast.LENGTH_LONG).show();
                    setButtonState(STAGE_BLUETOOTH_DISABLED);
                }
                break;
            case REQUEST_SCAN_DEVICE:
                if(isDeviceAvailable()) {
                    setButtonState(STAGE_BLUETOOTH_IDLE);
                }
                else {
                    Toast.makeText(this, R.string.toast_bluetooth_nd, Toast.LENGTH_LONG).show();
                    setButtonState(STAGE_BLUETOOTH_NO_DEVICE);
                }
                break;
            case REQUEST_CHOOSE_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data);
                    mButtonConnectBluetooth.setText(R.string.button_disconnect_bluetooth);
                }
                break;
        }
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v == mButtonEnableBluetooth) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                setButtonState(STAGE_BLUETOOTH_PENDING);
            }
            else if(v == mButtonScanBluetooth) {
                final Intent intent = new Intent(Intent.ACTION_MAIN, null);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                final ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.bluetooth.BluetoothSettings");
                intent.setComponent(cn);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, REQUEST_SCAN_DEVICE);
                setButtonState(STAGE_BLUETOOTH_PENDING);
            }
            else if(v == mButtonConnectBluetooth) {
                setButtonState(STAGE_BLUETOOTH_PENDING);
                if(mBluetoothService.getState() == BluetoothService.STATE_NONE) {
                    final Intent intent = new Intent(v.getContext(), DeviceListActivity.class);
                    startActivityForResult(intent, REQUEST_CHOOSE_DEVICE);
                }
                else {
                    mBluetoothService.stop();
                    mButtonConnectBluetooth.setText(R.string.button_connect_bluetooth);
                    setProgressBarIndeterminateVisibility(false);
                    mTimer.cancel();
                }
            }
            else if(v == mButtonMonitor) {
                if(mStage == STAGE_BLUETOOTH_MONITORING) {
                    setButtonState(STAGE_BLUETOOTH_CONNECTED);
                    setProgressBarIndeterminateVisibility(false);
                    mTextResult.setVisibility(View.INVISIBLE);
                    mButtonMonitor.setText(R.string.button_monitor);
                    mTimer.cancel();
                }
                else {
                    setButtonState(STAGE_BLUETOOTH_MONITORING);
                    setProgressBarIndeterminateVisibility(true);
                    mTextResult.setVisibility(View.VISIBLE);
                    mResultLen = 0;
                    mButtonMonitor.setText(R.string.button_stop_monitor);
                    monitor();
                }
            }
            else if(v == mButtonCalibrate) {
                if(mStage == STAGE_BLUETOOTH_CALIBRATING) {
                    setButtonState(STAGE_BLUETOOTH_CALIBRATE_STOP);
                    stopCalibration();
                }
                else {
                    setButtonState(STAGE_BLUETOOTH_CALIBRATE_START);
                    setProgressBarIndeterminateVisibility(true);
                    mButtonMonitor.setText(R.string.button_stop_calibrate);
                    startCalibration();
                }
            }
        }
    };

    private boolean isDeviceAvailable() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            return true;
        }
        else {
            return false;
        }
    }

    private void connectDevice(Intent data) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        m_device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mBluetoothService.connect(m_device);
        setProgressBarIndeterminateVisibility(true);
    }

    private void monitor() {
        mBluetoothService.write(MONITOR_REQUEST);
        mTimer.start();
    }

    private void analyzeMonitorResult() {
        float degree = 0.0f;
        degree  = mResult[2] * 100 + mResult[3] * 10 + mResult[4] + mResult[6] / 10.0f;
        mTextResult.setText(Float.toString(degree));
    }

    private void startCalibration() {
        mBluetoothService.write(START_CALIBRATION_REQUEST);
        mTimer.start();
    }

    private void analyzeCalibrateStartResult() {
        if((mResult[2] != 0) || (mResult[3] != 0) || (mResult[4] != 0) || (mResult[6] != 0)) {
            setProgressBarIndeterminateVisibility(false);
            Toast.makeText(this, R.string.toast_bluetooth_cal_start_fail, Toast.LENGTH_LONG).show();
            setButtonState(STAGE_BLUETOOTH_CONNECTED);
        }
        else {
            Toast.makeText(this, R.string.toast_bluetooth_cal_start, Toast.LENGTH_LONG).show();
            setButtonState(STAGE_BLUETOOTH_CALIBRATING);
            mButtonMonitor.setText(R.string.button_stop_calibrate);
        }
    }

    private void stopCalibration() {
        mBluetoothService.write(STOP_CALIBRATION_REQUEST);
        mTimer.start();
    }

    private void analyzeCalibrateStopResult() {
        int level = mResult[6];
        if((mResult[2] != 0) || (mResult[3] != 0) || (mResult[4] != 0)) {
            setProgressBarIndeterminateVisibility(false);
            Toast.makeText(this, R.string.toast_bluetooth_cal_stop_fail, Toast.LENGTH_LONG).show();
            setButtonState(STAGE_BLUETOOTH_CONNECTED);
        }
        else {
            setProgressBarIndeterminateVisibility(false);
            Toast.makeText(this, R.string.toast_bluetooth_cal_stop, Toast.LENGTH_LONG).show();
            setButtonState(STAGE_BLUETOOTH_CONNECTED);
            mButtonMonitor.setText(R.string.button_calibrate);
            mTextResult.setText("CAL: " + Integer.toString(level));
        }
    }

    private boolean isResultValid() {
        byte check_sum = 0;
        int i;
        for(i = 0; i < mResultLen - 1; i ++) {
            if((mResult[i] < RESULT_LOW_BOUND[i]) || (mResult[i] > RESULT_HIGH_BOUND[i])) {
                return false;
            }
            check_sum += mResult[i];
        }
        if(check_sum == mResult[i]) {
            return true;
        }
        else {
            return false;
        }
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            Toast.makeText(getApplicationContext(), R.string.toast_bluetooth_connected, Toast.LENGTH_LONG).show();
                            setButtonState(STAGE_BLUETOOTH_CONNECTED);
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            Toast.makeText(getApplicationContext(), R.string.toast_bluetooth_connecting, Toast.LENGTH_LONG).show();
                            setButtonState(STAGE_BLUETOOTH_CONNECTING);
                            break;
                        case BluetoothService.STATE_NONE:
                            Toast.makeText(getApplicationContext(), R.string.toast_bluetooth_disconnect, Toast.LENGTH_LONG).show();
                            setButtonState(STAGE_BLUETOOTH_IDLE);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    int length = msg.arg1;
                    for(int index = 0; index < length; index ++) {
                        if(mResultLen == 0) {
                            if(readBuf[index] != 0x0d) {
                                continue;
                            }
                        }
                        else if(mResultLen == 1) {
                            if(readBuf[index] != 0x0a) {
                                continue;
                            }
                        }
                        mResult[mResultLen] = readBuf[index];
                        mResultLen ++;
                        if(mResultLen == RESULT_LENGTH) {
                            mTimer.cancel();
                            if(!isResultValid()) {
                                Toast.makeText(getApplicationContext(), R.string.toast_bluetooth_data_invalid, Toast.LENGTH_LONG).show();
                            }
                            else {
                                if(mStage == STAGE_BLUETOOTH_MONITORING) {
                                    analyzeMonitorResult();
                                }
                                else if(mStage == STAGE_BLUETOOTH_CALIBRATE_START) {
                                    analyzeCalibrateStartResult();
                                }
                                else if(mStage == STAGE_BLUETOOTH_CALIBRATE_STOP) {
                                    analyzeCalibrateStopResult();
                                }
                            }
                            mResultLen = 0;
                            if(mStage == STAGE_BLUETOOTH_MONITORING) {
                                monitor();
                            }
                        }
                    }
                    break;
                case MESSAGE_DEVICE_NAME:
                    break;
                case MESSAGE_CONN_FAIL:
                    mBluetoothService.stop();
                    mButtonConnectBluetooth.setText(R.string.button_connect_bluetooth);
                    Toast.makeText(getApplicationContext(), R.string.toast_bluetooth_connect_fail, Toast.LENGTH_LONG).show();
                    setProgressBarIndeterminateVisibility(false);
                    break;
                case MESSAGE_CONN_LOST:
                    mBluetoothService.stop();
                    mButtonConnectBluetooth.setText(R.string.button_connect_bluetooth);
                    Toast.makeText(getApplicationContext(), R.string.toast_bluetooth_connect_lost, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    private final CountDownTimer mTimer = new CountDownTimer(COUNT_DOWN_TIME, COUNT_DOWN_STEP) {
        @Override
        public void onTick(long l) {
            ;
        }

        @Override
        public void onFinish() {
            Toast.makeText(getApplicationContext(), R.string.toast_bluetooth_timeout, Toast.LENGTH_LONG).show();
            if(mStage == STAGE_BLUETOOTH_MONITORING) {
                monitor();
            }
            else if(mStage == STAGE_BLUETOOTH_CALIBRATE_START) {
                setButtonState(STAGE_BLUETOOTH_CONNECTED);
                setProgressBarIndeterminateVisibility(false);
                mButtonMonitor.setText(R.string.button_calibrate);
            }
            else if(mStage == STAGE_BLUETOOTH_CALIBRATE_STOP) {
                setButtonState(STAGE_BLUETOOTH_CONNECTED);
                setProgressBarIndeterminateVisibility(false);
                mButtonMonitor.setText(R.string.button_calibrate);
            }
        }
    };

    private void setButtonState(int stage) {
        mStage = stage;
        switch (stage) {
            case STAGE_BLUETOOTH_DISABLED:
                mButtonEnableBluetooth.setEnabled(true);
                mButtonScanBluetooth.setEnabled(false);
                mButtonConnectBluetooth.setEnabled(false);
                mButtonCalibrate.setEnabled(false);
                mButtonMonitor.setEnabled(false);
                break;
            case STAGE_BLUETOOTH_NO_DEVICE:
                mButtonEnableBluetooth.setEnabled(false);
                mButtonScanBluetooth.setEnabled(true);
                mButtonConnectBluetooth.setEnabled(false);
                mButtonCalibrate.setEnabled(false);
                mButtonMonitor.setEnabled(false);
                break;
            case STAGE_BLUETOOTH_IDLE:
                mButtonEnableBluetooth.setEnabled(false);
                mButtonScanBluetooth.setEnabled(true);
                mButtonConnectBluetooth.setEnabled(true);
                mButtonCalibrate.setEnabled(false);
                mButtonMonitor.setEnabled(false);
                break;
            case STAGE_BLUETOOTH_CONNECTING:
                mButtonEnableBluetooth.setEnabled(false);
                mButtonScanBluetooth.setEnabled(false);
                mButtonConnectBluetooth.setEnabled(true);
                mButtonCalibrate.setEnabled(false);
                mButtonMonitor.setEnabled(false);
                break;
            case STAGE_BLUETOOTH_CONNECTED:
                mButtonEnableBluetooth.setEnabled(false);
                mButtonScanBluetooth.setEnabled(false);
                mButtonConnectBluetooth.setEnabled(true);
                mButtonCalibrate.setEnabled(true);
                mButtonMonitor.setEnabled(true);
                break;
            case STAGE_BLUETOOTH_MONITORING:
                mButtonEnableBluetooth.setEnabled(false);
                mButtonScanBluetooth.setEnabled(false);
                mButtonConnectBluetooth.setEnabled(true);
                mButtonCalibrate.setEnabled(false);
                mButtonMonitor.setEnabled(true);
                break;
            case STAGE_BLUETOOTH_CALIBRATING:
                mButtonEnableBluetooth.setEnabled(false);
                mButtonScanBluetooth.setEnabled(false);
                mButtonConnectBluetooth.setEnabled(true);
                mButtonCalibrate.setEnabled(true);
                mButtonMonitor.setEnabled(false);
                break;
            case STAGE_BLUETOOTH_CALIBRATE_START:
            case STAGE_BLUETOOTH_CALIBRATE_STOP:
            case STAGE_BLUETOOTH_PENDING:
                mButtonEnableBluetooth.setEnabled(false);
                mButtonScanBluetooth.setEnabled(false);
                mButtonConnectBluetooth.setEnabled(false);
                mButtonCalibrate.setEnabled(false);
                mButtonMonitor.setEnabled(false);
                break;
        }
    }
}
