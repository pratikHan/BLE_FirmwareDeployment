package com.example.nrf_uart;

import android.bluetooth.BluetoothDevice;

public class ExtendedBluetoothDevice {
	  public BluetoothDevice device;
	  public boolean isBonded;
	  public int rssi;
	  
	  public ExtendedBluetoothDevice(BluetoothDevice paramBluetoothDevice, int paramInt, boolean paramBoolean)
	  {
	    this.device = paramBluetoothDevice;
	    this.rssi = paramInt;
	    this.isBonded = paramBoolean;
	  }
	  
	  public boolean equals(Object paramObject)
	  {
	    if ((paramObject instanceof ExtendedBluetoothDevice))
	    {
	      paramObject = (ExtendedBluetoothDevice)paramObject;
	      return this.device.getAddress().equals(((ExtendedBluetoothDevice)paramObject).device.getAddress());
	    }
	    return super.equals(paramObject);
	  }
	  
	  public static class AddressComparator
	  {
	    public String address;
	    
	    public boolean equals(Object paramObject)
	    {
	      if ((paramObject instanceof ExtendedBluetoothDevice))
	      {
	        paramObject = (ExtendedBluetoothDevice)paramObject;
	        return this.address.equals(((ExtendedBluetoothDevice)paramObject).device.getAddress());
	      }
	      return super.equals(paramObject);
	    }
	  }
}
