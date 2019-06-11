
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




import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;

public class MainActivity extends Activity implements RadioGroup.OnCheckedChangeListener {
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    public static final String TAG = "nRFUART";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;



    private static final String test_data_byte="TEST_DATA_BYTE";
    private static final String firmware_update_request="FIRMWARE_UPDATE_REQUEST";
    private static final String acknowledged="ACKNOWLEDGED";
    private static final String packet_count_correct="PACKET_COUNT_CORRECT";
    private static final String program="PROGRAM";
    private static final String programmed="PROGRAMMED";
    private static final String all_data_sent="ALL_DATA_SENT";
    private static final String update_complete="UPDATE_COMPLETE";
    private static final String receive_bulk_data="RECEIVE_BULK_DATA";
    private static final String end_of_data_chunk="END_OF_DATA_CHUNK";
    private static final String end_of_bulk_data="END_OF_BULK_DATA";
    private static final String firmware_update_request_command="FIRM_UPDATE_REQ";
    private static final String program_flash_command="PROGRAM_FLASH";
    private static final String firmware_update_success_command="FIRM_UPDATE_SUCCESS";
    private static final String firmware_update_failure_command = "FIRM_UPDATE_FAILURE";
    private static final String jump_to_main_app_command = "JUMP_TO_APP";
    private static final String unknown_command = "UNKNOWN_COMMAND";
    private static final String data_length="DATA_LENGTH=";




   private String temp_storage="";
    private String temp_storage_b="";
   private String wholefile="";
   private String remaining_chunk=null;
    private int chunk_length=2001;
    private int countTransfer=0;



    private boolean isTest_data_byte=false;
    private boolean isAcknowledged=false;
    private boolean isPacketcountCorrect=false;
    private boolean isProgrammed=false;
    private boolean isUpdateComplete=false;
    private boolean issentremainingbytes=false;
    private boolean endflag=false;
    private boolean lengthflag=false;
    private boolean readfileasstring=false;


    private int temp_int=0;
    private String tempresult="";
    private String lastpacketsize="";

    TextView mRemoteRssiVal;
    RadioGroup mRg;
    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private ListView messageListView;
    private ArrayAdapter<String> listAdapter;
    private Button btnConnectDisconnect,btnSend;
    private EditText edtMessage;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        messageListView = (ListView) findViewById(R.id.listMessage);
        listAdapter = new ArrayAdapter<String>(this, R.layout.message_detail);
        messageListView.setAdapter(listAdapter);
        messageListView.setDivider(null);
        btnConnectDisconnect=(Button) findViewById(R.id.btn_select);
        btnSend=(Button) findViewById(R.id.sendButton);
        edtMessage = (EditText) findViewById(R.id.sendText);
        service_init();



     
       
        // Handle Disconnect & Connect button
        btnConnectDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBtAdapter.isEnabled()) {
                    Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                else {
                	if (btnConnectDisconnect.getText().equals("Connect")){
                		
                		//Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices
                		
            			Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
            			startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
        			} else {
        				//Disconnect button pressed
        				if (mDevice!=null)
        				{
        					mService.disconnect();
        					
        				}
        			}
                }
            }
        });








        // Handle Send button
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	EditText editText = (EditText) findViewById(R.id.sendText);
                   boolean sendflag=false;
                 boolean isFlag=true;
                  byte[] value;

                issentremainingbytes=false;
                readfileasString();





                try {

                    value = receive_bulk_data.getBytes();

                    Log.e("Value","Value is "+value.length);
                    Log.e("Value","Value is "+value.toString());





//                    if(isTest_data_byte){
//                        Log.e("PACKET","test_data_byte is true ");
//                        value=firmware_update_request.getBytes();
//                    }
//                    else if(isAcknowledged){
//                        Log.e("PACKET","Acknowledged is true");
//                        value=acknowledged.getBytes();
//                    }
//                    else if(isPacketcountCorrect){
//                        Log.e("PACKET","PacketCount is true");
//                      //  value=program.getBytes();
//                    }







                    ByteArrayInputStream bis= new ByteArrayInputStream(value);
                    int n = 0;
                    int countpacket=0;
                    byte[] buffer = new byte[20];

                       while ((n = bis.read(buffer)) > 0) {
                           String result = "";

                           Log.e("Count","count"+countpacket);
                           for (byte b : buffer) {

                               result += (char) b;
                              // Log.e("PACKET", "Result :" + result);
                               Log.e("PACKET", " Onclick SIZEa " + result.length());
                           }



                           Log.e("PACKET", "Onclick SIZEb  " + result.length());

                         //  Arrays.fill(buffer, (byte) 0);


                           Log.e("PACKET", "Onclick sending packet");
                           senddatabytes(buffer);



                           countpacket++;



                       }













                    Log.e("Packet", "Packet Counts" + countpacket);


				} catch (IOException e) {
					// TODO Auto-generated catch block

                    Log.e("ReadFile","Exception in BTnsend "+e.getMessage());
					e.printStackTrace();
				}
//                catch (InterruptedException e) {
//                    e.printStackTrace();
//                }


            }
        });

        // Set initial UI state
        
    }
    
    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
        		mService = ((UartService.LocalBinder) rawBinder).getService();
        		Log.d(TAG, "onServiceConnected mService= " + mService);
        		if (!mService.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth");
                    finish();
                }

        }

        public void onServiceDisconnected(ComponentName classname) {
       ////     mService.disconnect(mDevice);
        		mService = null;
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        
        //Handler events that received from UART service 
        public void handleMessage(Message msg) {
  
        }
    };

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            final Intent mIntent = intent;
           //*********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
            	 runOnUiThread(new Runnable() {
                     public void run() {
                         	String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                             Log.d(TAG, "UART_CONNECT_MSG");
                             btnConnectDisconnect.setText("Disconnect");
                             edtMessage.setEnabled(true);
                             btnSend.setEnabled(true);
                             ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - ready");
                             listAdapter.add("["+currentDateTimeString+"] Connected to: "+ mDevice.getName());
                        	 	messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                             mState = UART_PROFILE_CONNECTED;
                     }
            	 });
            }
           
          //*********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
            	 runOnUiThread(new Runnable() {
                     public void run() {
                    	 	 String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                             Log.d(TAG, "UART_DISCONNECT_MSG");
                             btnConnectDisconnect.setText("Connect");
                             edtMessage.setEnabled(false);
                             btnSend.setEnabled(false);
                             ((TextView) findViewById(R.id.deviceName)).setText("Not Connected");
                             listAdapter.add("["+currentDateTimeString+"] Disconnected to: "+ mDevice.getName());
                             mState = UART_PROFILE_DISCONNECTED;
                             mService.close();
                            //setUiState();
                         
                     }
                 });
            }
            
          
          //*********************//
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
             	 mService.enableTXNotification();
            }
          //*********************//
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {
              
                 final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                 runOnUiThread(new Runnable() {
                     public void run() {
                         try {
                         	String text = new String(txValue, "UTF-8");
                         	String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        	 	listAdapter.add("["+currentDateTimeString+"] RX: "+text);
                        	 	messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                                checkReceivedPackets(text);

                        	
                         } catch (Exception e) {
                             Log.e(TAG, e.toString());
                         }
                     }
                 });
             }
           //*********************//
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)){
            	showMessage("Device doesn't support UART. Disconnecting");
            	mService.disconnect();
            }
            
            
        }
    };

    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
  
        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
    	 super.onDestroy();
        Log.d(TAG, "onDestroy()");
        
        try {
        	LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        } 
        unbindService(mServiceConnection);
        mService.stopSelf();
        
        
        mService= null;
       
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
 
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

        case REQUEST_SELECT_DEVICE:
        	//When the DeviceListActivity return, with the selected device address
            if (resultCode == Activity.RESULT_OK && data != null) {
                String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
               
                Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - connecting");
                mService.connect(deviceAddress);
                            

            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

            } else {
                // User did not enable Bluetooth or an error occurred
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                finish();
            }
            break;
        default:
            Log.e(TAG, "wrong request code");
            break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
       
    }

    
    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
  
    }

    @Override
    public void onBackPressed() {
    	
    	Log.e(TAG,"XXX Back1");
        if (mState == UART_PROFILE_CONNECTED) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            showMessage("nRFUART's running in background.\n             Disconnect to exit");
        }
        else {
        	Log.e(TAG,"XXX Back2");
            new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.popup_title)
            .setMessage(R.string.popup_message)
            .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    	Log.e(TAG,"XXX Back3");
   	                finish();
                }
            })
            .setNegativeButton(R.string.popup_no, null)
            .show();
        }
    }





    public byte[] readfilebybytes(){


        int count=0;




        AssetManager assetManager = getResources().getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }


        byte[] fileBytes = new byte[0];

        if (files != null) {
            String filename ="bin_program_a.bin";
            InputStream in = null;






            try {

                in = assetManager.open(filename);


                in.skip(temp_int);
                fileBytes = new byte[in.available()];
                in.read(fileBytes);
                in.close();


            } catch (IOException e) {
                e.printStackTrace();
            }

        }



        temp_int=temp_int+2000;

        return fileBytes;
    }


        public void checkReceivedPackets(String s){


            if(s.equalsIgnoreCase(test_data_byte)){
                isTest_data_byte=true;

            }
            else if(s.equalsIgnoreCase(acknowledged)){
                isAcknowledged=true;
                Log.e("ACKNOWLEDGED","acknowledged");






                if(endflag) {
                    Log.e("Flags","flag endflag");
                    mService.writeRXCharacteristic(end_of_bulk_data.getBytes());
                    endflag=false;


                }

                if(lengthflag){

                    String x=data_length+lastpacketsize;
                    Log.e("Flags","flag lengthflag");
                    mService.writeRXCharacteristic(x.getBytes());
                    lengthflag=false;
                    endflag=true;
                }

                if(!issentremainingbytes){
                    Log.e("Flags","flag sendActualfile");
                    sendActualfile();
                }





            }

            else if(s.equalsIgnoreCase(programmed)){
                isProgrammed=true;
            }else if(s.equalsIgnoreCase(update_complete)){
                isUpdateComplete=true;
            }
            else if(s.equalsIgnoreCase(packet_count_correct)){
                isPacketcountCorrect=true;
               // mService.writeRXCharacteristic(program.getBytes());
                Log.e("CHECKRECEIVED","packet count CORRECT ");
                sendActualfile();

            }
            else{


                isPacketcountCorrect=false;

            }



        }

         public void senddatabytes(byte[] s){

           try {
               Log.e("PACKET", "sending test data byte");

               mService.writeRXCharacteristic(s);
              // Thread.sleep();

               String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
               listAdapter.add("[" + currentDateTimeString + "] TX: " + s);
               messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
               edtMessage.setText("");

           } catch (Exception e) {
               e.printStackTrace();
           }
         }

    public void sendActualfile(){



        boolean isFlag=false;
        boolean sendflag=false;
        boolean bufferfull=false;

        temp_storage_b="";
        readfileasstring=false;
        Log.e("Packets","Send Actual file entered");
         byte[] value;
        String bucket="";



        try {



            value=readfilebybytes();

            long x=doHash(value);

            Log.e("Hash","hash value of whole file :"+x);
            Log.e("Packet","Value length is :"+value.length);
            lastpacketsize=String.valueOf(value.length);



            ByteArrayInputStream bis = new ByteArrayInputStream(value);
            int n = 0;
            int index=0;
           int countpacket=1;
             byte[] buffer = new byte[20];
            byte[] bits=new byte[20];
            





            byte[] ackvalue;
            ackvalue=end_of_data_chunk.getBytes("UTF-8");


            
            
            	


            while ((n = bis.read(buffer)) > 0) {
                String result = "";

                int i=0;


                for (byte b : buffer) {

                    result += (char) b;
                    bits[i]=b;
                    Log.e("Bits","bits content"+bits[i]);
                    Log.e("Char b","bits char "+(char)b);
                    i++;
                    
                    
                }
                
               

                Log.e("Bits","bits length"+bits.length);


                    temp_storage_b=temp_storage_b+result;
                    remaining_chunk=temp_storage_b;
                    chunk_length=remaining_chunk.length();




                Log.e("Packet", "packet count W ,SAF "+countpacket);

                Log.e("SendActualFile","TempB length :"+temp_storage_b.length());
               // Log.e("SendActualFile","TempB contents :"+temp_storage_b.toString());



                Log.e("SendActualFile","Result length :"+result.length());
                Log.e("SendActualFile","Result contents :"+result.toString());

               // Log.e("SendActualFile","Buffer length :"+buffer.length());
                Log.e("SendActualFile","Buffer contents :"+buffer.toString());

                   // temp_storage+=result;

                    Arrays.fill(buffer, (byte) 0);











                    if(countpacket==100){


                      isFlag=true;

                        long s=doHash(bits);

                        String hash= String.valueOf(s);
                        Log.e("Hash","hash one packet :"+hash);

                        senddatabytes(bits);

                        mService.writeRXCharacteristic(ackvalue);


                        Log.e("Packet", "100 packets sent");
                        listAdapter.add(" TX: " +end_of_data_chunk);
                      break;


                }else
                  //  isFlag=false;



                	
                	if(value.length<2000){
                    	
                    	int abs=value.length/20;
                    	int bytesize=value.length%20;
                    	Log.e("BYTE","xxx value length :"+value.length);
                    	Log.e("BYTE","xxx :"+bytesize);
                    	Log.e("BYTE","xxx abs :"+abs);
                    	
                    	if(countpacket==abs+1){
                    		Log.e("BYTE","xxx enter");
                    		bits=new byte[bytesize];
                    		for(int j=0;j<bytesize;j++){
                    			bits[j]=buffer[j];
                    		}
                    		bufferfull=true;
                    	}
                    		
                    }


                  //  senddatabytes(result);

                        if(!isFlag){
                        	Log.e("BYTE","xxx bits length :"+bits.length);
                            senddatabytes(bits);
                        }
                        else
                            break;



                countpacket++;
            }




            Log.e("Packet","remaining chunk length "+remaining_chunk.length());

            if(chunk_length < 2000){

                long s=doHash(bits);

                String hash= String.valueOf(s);

                mService.writeRXCharacteristic(ackvalue);


                issentremainingbytes=true;
               // endflag=true;
                lengthflag=true;

                Log.e("Hash","hash of remaining chunk :"+hash);
                Log.e("Packet","end of bulk data sent");
                // Log.e("Packet", "100 packets sent");
                listAdapter.add(" TX: " +end_of_data_chunk);


            }

            Log.e("Packet", "packet count is 100 ,SAF "+countpacket);

        } catch (IOException e) {
            // TODO Auto-generated catch block

            Log.e("ReadFile","Exception in BTnsend "+e.getMessage());
            e.printStackTrace();
        }



    }

/*
    public void sendremainingchunks(String s){

        byte[] value;



        try {



            Log.e("SRC","String length is "+s.length());
            value = s.getBytes();

            Log.e("Send Remaining Chunks","File Length is  :"+value.length);




            ByteArrayInputStream bis= new ByteArrayInputStream(value);
            int n = 0;
            int countpacket=0;

            byte[] buffer=new byte[20];

           


            while ((n = bis.read(buffer)) > 0) {
                String result = "";


                Log.e("Count","count"+countpacket);
                for (byte b : buffer) {

                    result += (char) b;
                    // Log.e("PACKET", "Result :" + result);


                    Log.e("PACKET", "SIZEa " + result.length());
                    Log.e("PACKET", "Result " + result);

                }

                senddatabytes(buffer);


                countpacket++;



            }

            mService.writeRXCharacteristic(end_of_data_chunk.getBytes());
          // Thread.sleep(0);
           // mService.writeRXCharacteristic(end_of_bulk_data.getBytes());


                Log.e("Packet","end of bulk data sent");
                // Log.e("Packet", "100 packets sent");
                listAdapter.add(" TX: " +end_of_data_chunk);
                listAdapter.add(" TX: " +end_of_bulk_data);
          //  Log.e("Packet", "Remaining Packet Counts" + countpacket);

            issentremainingbytes=true;
            endflag=true;

        } catch (Exception e) {
            // TODO Auto-generated catch block

            Log.e("SendRemainingChunks","Exception "+e.getMessage());
            e.printStackTrace();
        }

    }

*/


public void readfileasString(){


    byte[] value;
    temp_storage_b="";
    readfileasstring=true;



    try {


        value = readfilebybytes();
        temp_int=0;
        Log.e("ReadFileAsString","File Length is  :"+value.length);






      ByteArrayInputStream bis= new ByteArrayInputStream(value);
        int n = 0;
        int countpacket=0;
        byte[] buffer=new byte[20];


        while ((n = bis.read(buffer)) > 0) {
            String result = "";

            Log.e("Count","count"+countpacket);
            for (byte b : buffer) {


              //  Log.e("PACKET", "buffer length is  : " + buffer.length);
                result += (char) b;

                // Log.e("PACKET", "Result :" + result);
               // Log.e("PACKET", " SIZEa readfileasString " + result.length());
              //  Log.e("PACKET", "result contentXFAS : " + result);
            }




            Log.e("BYTE","temp byte is "+tempresult);


           // Log.e("PACKET", "result content : " + result);


//              temp_storage_b = temp_storage_b + result;
//            remaining_chunk=temp_storage_b;
//            chunk_length=remaining_chunk.length();

            countpacket++;

           // Log.e("PACKET", "temp storageFAS  : " + temp_storage_b);

        }




        Log.e("Packet", "Original File Length :"+value.length);
    } catch (IOException e) {
        // TODO Auto-generated catch block

        Log.e("ReadFile","Exception in BTnsend "+e.getMessage());
        e.printStackTrace();
    }

}

   public  int compute_hash(byte [] array, int len)
    {
        int hash, i;
        for(hash = i = 0; i < len; ++i)
        {
            hash += array[((int) i)];
            hash += (hash << 10);
            hash ^= (hash >> 6);
        }
        hash += (hash << 3);
        hash ^= (hash >> 11);
        hash += (hash << 15);
        return hash;
    }



    private long doHash(byte[] b) {
        int hash = 0;

        for (int i = 0; i < b.length; i++) {
            hash += (int)((int)b[i]&0xFF);
            hash = hash+(hash << 10);

            hash ^= (hash >> 6);

            //hash = ((((hash << 5))) + (hash)) + b[i];
        }

        hash =hash+ (hash << 3);

        hash ^= (hash >> 11);

        hash = hash+(hash << 15);

        return hash;
    }

}
