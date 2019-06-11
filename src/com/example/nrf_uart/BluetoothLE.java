package com.example.nrf_uart;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class BluetoothLE extends Service {

	
	
	private BluetoothAdapter mBLEAdapter;
	private BluetoothManager manager;
	private Handler scanHandler = new Handler();

	
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.e("Service","Service -->>");
		turnonBLE();
	    discoverBLEDevices();
	}
	
	
	
	@SuppressLint("NewApi")
	private void turnonBLE() {
	    // TODO Auto-generated method stub
	    manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
	    mBLEAdapter = manager.getAdapter();
	    mBLEAdapter.enable();
	    Toast.makeText(getApplicationContext(), "BTLE ON Service",
	            Toast.LENGTH_LONG).show();
	    Log.e("BLE_Scanner", "TurnOnBLE");}

	@SuppressLint("NewApi")
	private void discoverBLEDevices() {
	    // TODO Auto-generated method stub
	    startScan.run();
	    Log.e("BLE_Scanner", "DiscoverBLE");
	}


	private Runnable startScan = new Runnable() {
	    @Override
	    public void run() {
	        scanHandler.postDelayed(stopScan, 500);
	        mBLEAdapter.startLeScan(mLeScanCallback);
	    }
	};


	// Device scan callback.
	@SuppressLint("NewApi")
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

	        @SuppressLint("NewApi")
	        public void onLeScan(final BluetoothDevice device, int rssi,
	                byte[] scanRecord) {

	            String Address = device.getAddress();
	            String Name = device.getName();
	            Log.e("mLeScanCallback","Address :"+Address +" Name: "+Name);
	            }

			
	    };


	private Runnable stopScan = new Runnable() {
	    @Override
	    public void run() {
	        mBLEAdapter.stopLeScan(mLeScanCallback);
	        scanHandler.postDelayed(startScan, 10);
	    }
	};
	
}
