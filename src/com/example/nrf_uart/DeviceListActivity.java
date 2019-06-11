
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
package com.example.nrf_uart;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;







import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceListActivity extends Activity {
    private BluetoothAdapter mBluetoothAdapter;

   // private BluetoothAdapter mBtAdapter;
    private TextView mEmptyList;
    public static final String TAG = "DeviceListActivity";
    
    List<BluetoothDevice> deviceList;
    private DeviceAdapter deviceAdapter;
    private ServiceConnection onService = null;
    Map<String, Integer> devRssiValues;
    private static final long SCAN_PERIOD = 10000; //scanning for 10 seconds
    private Handler mHandler;
    private boolean mScanning;
    private int count=0;
    private boolean flag=false;
    
   



    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar);
        setContentView(R.layout.device_list);
        android.view.WindowManager.LayoutParams layoutParams = this.getWindow().getAttributes();
        layoutParams.gravity=Gravity.TOP;
        layoutParams.y = 200;
        mHandler = new Handler();
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
      //  populateList();
        
       
        
        mEmptyList = (TextView) findViewById(R.id.empty);
        Button cancelButton = (Button) findViewById(R.id.btn_cancel);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	
            	 startService(new Intent(getApplicationContext(), BluetoothLE.class));
            /*	
            	if (mScanning==false) 
            	{
            		//while(true)
            		scanLeDevice(true);
            		
            		} 
            	else finish();*/
            	
            	
            	Log.e(TAG,"XXX CLICK");
            	
           /* 	while(true){
            	
            		count++;
            		try {
						Thread.sleep(1000);
						Log.e(TAG,"XXX count :"+count);
	            		Log.e(TAG,"XXX WHILE");

	            		if(count>10){
	            			
	            			mBluetoothAdapter.stopLeScan(mLeScanCallback);
	            			Thread.sleep(2000);
	            			count=0;
	            			mBluetoothAdapter.startLeScan(mLeScanCallback);
	            			
	            		}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            		
            	
            		
            		
            		
			      
			       

//			        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
//			        newDevicesListView.setAdapter(deviceAdapter);
            		
            		//mBluetoothAdapter.startLeScan(mLeScanCallback);
            		
            	
            			
            		
            	
            			
            		
            	}*/
            	
            }
        });

    }
    
    
    

    private void populateList() {
        /* Initialize device list container */
    	
    	Log.e(TAG,"XXX populate");
        Log.d(TAG, "populateList");
        deviceList = new ArrayList<BluetoothDevice>();
        deviceAdapter = new DeviceAdapter(this, deviceList);
        devRssiValues = new HashMap<String, Integer>();

        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(deviceAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

       
      

           scanLeDevice(true);

    }
    
    private void scanLeDevice(final boolean enable) {
        final Button cancelButton = (Button) findViewById(R.id.btn_cancel);
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
					mScanning = false;
					
					
					
					Log.e(TAG,"XXX ScanleDevice");
					
					// mBluetoothAdapter.startLeScan(mLeScanCallback);
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                  //  mBluetoothAdapter.disable();
                    Log.e(TAG," xxx Thread A");
                    
                    populateList();
                 // mBluetoothAdapter.startLeScan(mLeScanCallback);
                   Log.e(TAG,"scanning");     
                    cancelButton.setText("Scanning");

                }
            }, SCAN_PERIOD);

            mScanning = true;
            
//            try {
//				
//				//mBluetoothAdapter.enable();
//				Thread.sleep(4000);
//				Log.e(TAG," xxx Thread main");
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
            
            deviceList.clear();
            deviceAdapter.notifyDataSetChanged();
            
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            
            
            cancelButton.setText("Scanning");
        } else {
            mScanning = false;
           
            Log.e(TAG,"mscanning false");
             mBluetoothAdapter.stopLeScan(mLeScanCallback);
            cancelButton.setText(R.string.scan);
        }

    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
            
    	
    	
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                	
                	
                			Log.e(TAG,"yyy Lescan name :"+device.getName()+"   " +device.getAddress());	
                              addDevice(device,rssi);
                              deviceAdapter.addOrUpdateDevice(new ExtendedBluetoothDevice(device, rssi, false));
                }
            });
        }
    };
    
    private void addDevice(BluetoothDevice device, int rssi) {
        boolean deviceFound = false;

        for (BluetoothDevice listDev : deviceList) {
            if (listDev.getAddress().equals(device.getAddress())) {
                deviceFound = true;
                break;
            }
        }
        
        
        devRssiValues.put(device.getAddress(), rssi);
        if (!deviceFound) {
        	deviceList.add(device);
            mEmptyList.setVisibility(View.GONE);
                 	
        	

            
            deviceAdapter.notifyDataSetChanged();
            
            
        }
    }

    @Override
    public void onStart() {
        super.onStart();
       
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
    }

    @Override
    public void onStop() {
        super.onStop();
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
    
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        
    }

    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
    	
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BluetoothDevice device = deviceList.get(position);
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
  
            Bundle b = new Bundle();
            b.putString(BluetoothDevice.EXTRA_DEVICE, deviceList.get(position).getAddress());

            Intent result = new Intent();
            result.putExtras(b);
            setResult(Activity.RESULT_OK, result);
            finish();
        	
        }
    };


    
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
    }
    
    class DeviceAdapter extends BaseAdapter {
        Context context;
        List<BluetoothDevice> devices;
        LayoutInflater inflater;
        
        private final ArrayList<ExtendedBluetoothDevice> mListBondedValues = new ArrayList();
        private final ArrayList<ExtendedBluetoothDevice> mListValues = new ArrayList();

        public DeviceAdapter(Context context, List<BluetoothDevice> devices) {
            this.context = context;
            inflater = LayoutInflater.from(context);
            this.devices = devices;
        }

        
        public void addBondedDevice(ExtendedBluetoothDevice paramExtendedBluetoothDevice)
        {
          this.mListBondedValues.add(paramExtendedBluetoothDevice);
          notifyDataSetChanged();
        }
        
        public void addOrUpdateDevice(ExtendedBluetoothDevice paramExtendedBluetoothDevice)
        {
          if (this.mListBondedValues.contains(paramExtendedBluetoothDevice)) {
            return;
          }
          int i = this.mListValues.indexOf(paramExtendedBluetoothDevice);
          if (i >= 0)
          {
            ((ExtendedBluetoothDevice)this.mListValues.get(i)).rssi = paramExtendedBluetoothDevice.rssi;
            notifyDataSetChanged();
            return;
          }
          this.mListValues.add(paramExtendedBluetoothDevice);
          
          Log.e("Device List","xxx newdevice "+mListValues);
          notifyDataSetChanged();
        }
        
        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewGroup vg;

            if (convertView != null) {
                vg = (ViewGroup) convertView;
            } else {
                vg = (ViewGroup) inflater.inflate(R.layout.device_element, null);
            }

            BluetoothDevice device = devices.get(position);
            final TextView tvadd = ((TextView) vg.findViewById(R.id.address));
            final TextView tvname = ((TextView) vg.findViewById(R.id.name));
            final TextView tvpaired = (TextView) vg.findViewById(R.id.paired);
            final TextView tvrssi = (TextView) vg.findViewById(R.id.rssi);

            tvrssi.setVisibility(View.VISIBLE);
            byte rssival = (byte) devRssiValues.get(device.getAddress()).intValue();
            if (rssival != 0) {
                tvrssi.setText("Rssi = " + String.valueOf(rssival));
            }

            tvname.setText(device.getName());
            tvadd.setText(device.getAddress());
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                Log.i(TAG, "device::"+device.getName());
                tvname.setTextColor(Color.WHITE);
                tvadd.setTextColor(Color.WHITE);
                tvpaired.setTextColor(Color.GRAY);
                tvpaired.setVisibility(View.VISIBLE);
                tvpaired.setText(R.string.paired);
                tvrssi.setVisibility(View.VISIBLE);
                tvrssi.setTextColor(Color.WHITE);
                
            } else {
                tvname.setTextColor(Color.WHITE);
                tvadd.setTextColor(Color.WHITE);
                tvpaired.setVisibility(View.GONE);
                tvrssi.setVisibility(View.VISIBLE);
                tvrssi.setTextColor(Color.WHITE);
            }
            return vg;
        }
    }
    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    
    
    private boolean refreshDeviceCache(BluetoothGatt gatt){
        try {
            BluetoothGatt localBluetoothGatt = gatt;
            Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
            if (localMethod != null) {
               boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
                return bool;
             }
        } 
        catch (Exception localException) {
            Log.e(TAG, "An exception occured while refreshing device");
        }
        return false;
    }

}
