package com.jm.service.RemoteMouseService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RemoteMouseBroadcastReceiver extends BroadcastReceiver {

	static private final String TAG = "RemoteMouseBroadcastReceiver";
    @Override 
    public void onReceive(Context context, Intent intent) { 
		Log.d(TAG, "boot completed");
	    
		Intent myintent = new Intent("actions.intent.RemoteMouseService");
		context.startService(myintent);
    } 
}

