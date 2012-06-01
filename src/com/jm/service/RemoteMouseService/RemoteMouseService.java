package com.jm.service.RemoteMouseService;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.ServiceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.IWindowManager;
import android.view.WindowManager;

import android.content.ContentResolver;
import android.provider.Settings;

public class RemoteMouseService extends Service{
	private final static String TAG = "RemoteMouseService";
	private final boolean DEBUG = true;
	private static final int PORT = 8889;
	private IWindowManager mWindowManager;
	DisplayMetrics mDisplayMetrics;
	
	private static final int MSG_START = 0;
	private static final int MSG_STOP = 1;
	private static final int MSG_DATA = 2;
	private static final int MSG_HOME = 3;
	private static final int MSG_MENU = 4;
	private static final int MSG_BACK = 5;
	
	private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
	            case MSG_START:{
	            	if(DEBUG)
	            		Log.d(TAG, "MSG_START");
	            	final ContentResolver cr = getContentResolver();
	            	Settings.System.putInt(cr,
	                        Settings.System.POINTER_LOCATION, 1);
	            }break;
	            case MSG_STOP:{
	            	if(DEBUG)
	            		Log.d(TAG, "MSG_STOP");
	            	final ContentResolver cr = getContentResolver();
	            	Settings.System.putInt(cr,
	                        Settings.System.POINTER_LOCATION, 0);
	            }break;
	            case MSG_HOME:{	
	            	if(DEBUG)
	            		Log.d(TAG, "MSG_HOME");
	            	KeyEvent down = new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HOME, 0);
	            	sendKeySync(down);
	            	KeyEvent up = new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HOME, 0);
	            	sendKeySync(up);
	            }break;
	            case MSG_MENU:{
	            	if(DEBUG)
	            		Log.d(TAG, "MSG_MENU");
	            	KeyEvent down = new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU, 0);
	            	sendKeySync(down);
	            	KeyEvent up = new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MENU, 0);
	            	sendKeySync(up);
	            }break;
	            case MSG_BACK:{
	            	if(DEBUG)
	            		Log.d(TAG, "MSG_BACK");
	            	KeyEvent down = new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK, 0);
	            	sendKeySync(down);
	            	KeyEvent up = new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK, 0);
	            	sendKeySync(up);
	            }break;
	            case MSG_DATA:{          	
	            	String[] l = ((String)(msg.obj)).split(",");
	                float x = 0, y = 0;
	                int act = 0;
	                for(int i = 0; i < l.length; i++){
	             	   if(i == 0){
	             		   
	             	   }else if(i == 1){
	             		   int trans_act = Integer.parseInt(l[i]);
	             		   if(trans_act == 0)
	             			   act = MotionEvent.ACTION_DOWN;
	             		   else if(trans_act == 1)
	            			   act = MotionEvent.ACTION_UP;
	             		   else if(trans_act == 2)
	             			   act = MotionEvent.ACTION_MOVE;
	             	   }else if(i == 2){
	             		   float scale = 0;
	             		   try { 
	             			   scale = Float.parseFloat(l[i]); 
	             		   }catch(NumberFormatException e) { 
	             			   e.printStackTrace();
	             		   } 
	             		   
	             		   x = scale * mDisplayMetrics.widthPixels;                  		   
	             	   }else if(i == 3){
	             		   float scale = 0;
	             		   try { 
	             			   scale = Float.parseFloat(l[i]); 
	             		   }catch(NumberFormatException e) { 
	             			   e.printStackTrace();
	             		   } 
	             		   
	             		   y = scale * mDisplayMetrics.heightPixels;      
	             	   }                   		                    		
	                }
	                
	            	MotionEvent e = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), act, x, y, 0);
	            	if(DEBUG)
	            		Log.d(TAG, "sendEvent:" + "act=" + act + ",x=" + x + "," + "y=" + y + ",mDisplayMetrics.widthPixels=" + mDisplayMetrics.widthPixels + ",mDisplayMetrics.heightPixels=" + mDisplayMetrics.heightPixels);
	                sendPointerSync(e);
	            }break;           
            }
            
            super.handleMessage(msg);
        }             
    };

	private void receiveMouseData() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
               DatagramSocket dSocket = null;
               DatagramPacket dPacket = null;
               
        	   try {
        		   dSocket = new DatagramSocket(PORT);  		      		   
        	   } catch (SocketException e) {
                   e.printStackTrace();
               }
        	 
               while(true){            	              	                    
                   try {
                	   if(DEBUG)
                		   Log.d(TAG, "receiving");
                	   byte[] msg = new byte[1024];
            		   dPacket = new DatagramPacket(msg, msg.length);  
                       dSocket.receive(dPacket); 
                       
                       String str = new String(dPacket.getData(), dPacket.getOffset(), dPacket.getLength());                     
                       
                       int msg_id = MSG_DATA;
                       if(str.equals("start"))
                    	   msg_id = MSG_START;
                       else if(str.equals("stop"))
                    	   msg_id = MSG_STOP;
                       else if(str.equals("home"))
                    	   msg_id = MSG_HOME;
                       else if(str.equals("menu"))
                    	   msg_id = MSG_MENU;
                       else if(str.equals("back"))
                    	   msg_id = MSG_BACK;
                       else
                    	   msg_id = MSG_DATA;
                    	                 	   
                       Message.obtain(mHandler, msg_id, str).sendToTarget();
                   } catch (IOException e) {
                       e.printStackTrace();
                   }                   
                   
            	  
               }
            }
        }, "reveive mouse data");
        
        thread.start();
    }
	
    /** Called when the activity is first created. */
	@Override
	public void onCreate()
	{
		if(DEBUG)
			Log.d(TAG, "onCreate");
		super.onCreate();	
		
		mWindowManager = IWindowManager.Stub.asInterface(  
			           ServiceManager.getService(Context.WINDOW_SERVICE)); 
 
		mDisplayMetrics = new DisplayMetrics();  
		((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(mDisplayMetrics);  

		receiveMouseData();
	}
	
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}
	
	private void sendPointerSync(MotionEvent event) 
	{
		try {
			mWindowManager.injectPointerEvent(event, true);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void sendKeySync(KeyEvent event) {
		try {		
			mWindowManager.injectKeyEvent(event, true);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}	
}

	