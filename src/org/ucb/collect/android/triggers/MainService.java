package org.ucb.collect.android.triggers;

import java.util.Calendar;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MainService extends Service {
	private static final String TAG = "TriggerMainService";
	
	private final int randomHour = (int) (Math.random()*3 + 3);
	private final int randomMin = (int) (Math.random()*60);
	private final int randomSecond = (int) (Math.random()*60);
	
	private AlarmManager cron;
	private PendingIntent pTimeTrigger;
	private PendingIntent pDownloadRequest;
	
	@Override
	public void onCreate(){
		super.onCreate();
		Log.d(TAG, "MainService is created");
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, randomHour);
		calendar.set(Calendar.MINUTE, randomMin);
		calendar.set(Calendar.SECOND, randomSecond);
		
		cron = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        
		//Schedule settimetrigger task
		Intent timeTrigger = new Intent(this, SetTimeTrigger.class);
		pTimeTrigger = PendingIntent.getBroadcast(this, 0, timeTrigger, PendingIntent.FLAG_UPDATE_CURRENT);
		cron.setRepeating(AlarmManager.RTC_WAKEUP, 
				calendar.getTimeInMillis(),
				AlarmManager.INTERVAL_DAY, 
				pTimeTrigger);
		
		//Schedule download task
		Intent downloadRequest = new Intent(this, DownloadRequest.class);
		pDownloadRequest = PendingIntent.getBroadcast(this, 0, downloadRequest, PendingIntent.FLAG_UPDATE_CURRENT);
		cron.setRepeating(AlarmManager.RTC_WAKEUP,
				calendar.getTimeInMillis(),
				AlarmManager.INTERVAL_DAY,
				pDownloadRequest);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    // We don't want this service to continue running if it is explicitly
	    // stopped, so return not sticky.
		Log.d(TAG, "MainService starts");
	    return START_NOT_STICKY;
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "MainService is destroyed");
		super.onDestroy();
		cron.cancel(pDownloadRequest);
		pDownloadRequest.cancel();
		cron.cancel(pTimeTrigger);
		pTimeTrigger.cancel();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
