package com.example.runningtracker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteCallbackList;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

// service class
public class RunningService extends Service {

    RemoteCallbackList<MyBinder> remoteCallbackList = new RemoteCallbackList<MyBinder>();
    protected Tracker tracker = new Tracker();
    LocationManager locationManager;
    MyLocationListener locationListener;
    Location location;
    private final String CHANNEL_ID = "100";
    int NOTIFICATION_ID = 001;

    public RunningService(){
        super();
    }

    // nested class that "runs" and sends callbacks
    protected class Tracker extends Thread implements Runnable {
        public boolean running = true;
        public FutureTask<Boolean> future;
        ExecutorService executor;

        public Tracker() {
            this.start();
        }

        public void run() {
            while (this.running) {

                // sleep
                try {Thread.sleep(2000);} catch(Exception e) {return;}
                // get new location from location listener
                location = locationListener.getLocation();
                // create executor that runs on new thread
                executor = Executors.newFixedThreadPool(1);
                // create future task executed by executor to call callbacks
                future = new FutureTask<>(new Callable<Boolean>() {
                    public Boolean call() {
                        doCallbacks(location);
                        return true;
                    }
                });
                executor.execute(future);
                // sleep until callback, i.e., task, is finished
                while(!future.isDone()) {
                    try {Thread.sleep(1000);} catch(Exception e) {return;}
                }
                // shut down executer when done with it
                executor.shutdown();

            }
        }
    }

    // broadcast location to activity as service is running
    public void doCallbacks(Location location) {
        final int n = remoteCallbackList.beginBroadcast();
        for (int i=0; i<n; i++) {
            remoteCallbackList.getBroadcastItem(i).callback.runningTrackerEvent(location);
        }
        remoteCallbackList.finishBroadcast();
    }

    // implement a portal to wrap up communication with the activity
    public class MyBinder extends Binder implements IInterface
    {
        @Override
        public IBinder asBinder() {
            return this;
        }

        public void registerCallback(ICallback callback) {
            this.callback = callback;
            remoteCallbackList.register(MyBinder.this);
        }

        public void unregisterCallback(ICallback callback) {
            remoteCallbackList.unregister(MyBinder.this);
        }

        ICallback callback;

    }

    // create new tracker to start running service
    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        Log.d("g53mdp", "service onCreate");
        super.onCreate();
        tracker = new Tracker();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        Log.d("g53mdp", "service onBind");
        return new MyBinder();
    }

    // start the service
    // create location manager using location listener to request location updates every 5 seconds with minimum 5 m change
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        Log.d("g53mdp", "service onStartCommand");
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 5, locationListener);
        } catch(SecurityException e) {
            Log.d("g53mdp", e.toString()); }

        // display notification to user as service is running
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "channel name";
            String description = "channel description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel);
        }
        // intent to start the MainActivity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Intent notificationIntent = new Intent(RunningService.this, MainActivity.class);
        // pending intent to be able to return to the app
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Running Tracker")
                .setContentText("Currently tracking your location")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());

        return Service.START_STICKY;
    }

    // stop service, stop getting location updates, stop displaying notification
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        Log.d("g53mdp", "service onDestroy");
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
        locationManager = null;
        tracker.running = false;
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    @Override
    public void onRebind(Intent intent) {
        // TODO Auto-generated method stub
        Log.d("g53mdp", "service onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // TODO Auto-generated method stub
        Log.d("g53mdp", "service onUnbind");
        return super.onUnbind(intent);
    }

}