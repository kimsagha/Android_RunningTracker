package com.example.runningtracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.github.mikephil.charting.data.Entry;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements RunAdapter.ItemClickListener {

    private RunningService.MyBinder myService = null;
    RunViewModel runViewModel;
    private RunAdapter runAdapter;
    private Button startButton, finishButton, buttonSort;
    private Intent serviceIntent;
    private boolean firstLocation = true, sortId = true, running = false;
    private double distance, speed, startTime, endTime;
    private TextView timeValue, distanceValue, speedValue, state, distanceToday;
    private String time = "", dateToday = "";
    private Date date;
    private DateFormat dateFormat;
    private Run selectedRun;
    private Location prevLocation, nextLocation;
    private DecimalFormat df;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // request permission from the user to track their location
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 8);

        // create the intent to start the service
        serviceIntent = new Intent(MainActivity.this, RunningService.class);

        startButton = findViewById(R.id.startButton);
        finishButton = findViewById(R.id.finishButton);
        timeValue = findViewById(R.id.timeValue);
        distanceValue = findViewById(R.id.distanceValue);
        speedValue = findViewById(R.id.speedValue);
        state = findViewById(R.id.state);
        distanceToday = findViewById(R.id.distanceTodayValue);

        // format doubles for speed and distance to have 2 decimals
        df = new DecimalFormat("#.00");

        // fill recycler view with data from db using viewmodel and adapter
        runAdapter = new RunAdapter(this);
        RecyclerView recyclerView = findViewById(R.id.view);
        runAdapter.setClickListener(this);
        recyclerView.setAdapter(runAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        runViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(RunViewModel.class);
        runViewModel.getAllRunsById().observe(this, runs -> { runAdapter.setData(runs); });

        // restore activity according to the saved instance if it was interrupted
        if (savedInstanceState != null) {
            // restore the way the recycler view was sorted
            sortId = savedInstanceState.getBoolean("sortId");
            if (sortId == true) {
                sortId = false;
                buttonSort.setText("SORT BY DATE");
                runViewModel.getAllRunsBySpeed().observe(this, runs -> { runAdapter.setData(runs); });
            } else {
                sortId = true;
                buttonSort.setText("SORT BY SPEED");
                runViewModel.getAllRunsById().observe(this, runs -> { runAdapter.setData(runs); });
            }

            // restore if it was running
            running = savedInstanceState.getBoolean("running");
            // restore the start time to calculate end time
            startTime = savedInstanceState.getDouble("startTime");
            // restore distance to increment if still running
            distance = savedInstanceState.getDouble("distance");
            // restore locations to update where the user is for next calculation of distance
            prevLocation = savedInstanceState.getParcelable("prevLocation");
            nextLocation = savedInstanceState.getParcelable("nextLocation");
            if (running) {
                startButton.setVisibility(View.INVISIBLE);
                finishButton.setVisibility(View.VISIBLE);
                state.setVisibility(View.VISIBLE);
            } else {
                // if not running, display values of previous run
                updateScreenValues();
            }
            try {
                // query db for total distance run today by user
                distanceToday.setText((int) runViewModel.getDistanceByDate(dateToday));
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // invoked when the activity is temporarily destroyed
    // save important values needed to restore the activity
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("sortId", sortId);
        outState.putBoolean("running", running);
        outState.putDouble("startTime", startTime);
        outState.putDouble("distance", distance);
        outState.putParcelable("prevLocation", prevLocation);
        outState.putParcelable("nextLocation", nextLocation);
        if(!running) {
            outState.putString("time", time);
            outState.putDouble("distance", distance);
            outState.putDouble("speed", speed);
        }
    }

    // button pressed to start a run
    public void onClickStartRunning(View v) {
        // bind to the service to communicate with it in the background
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        // start the service using the intent created in onCreate()
        startService(serviceIntent);

        // change visibility of buttons and texts to show user that a new run has started
        startButton.setVisibility(View.INVISIBLE);
        finishButton.setVisibility(View.VISIBLE);
        state.setVisibility(View.VISIBLE);
        running = true;

        // get the time in milliseconds when the user started running
        startTime = System.currentTimeMillis();
        // reset screen values, i.e., remove values displayed from previous run
        resetScreenValues();
        dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        date = new Date();
    }

    // button pressed to stop and finish a run
    public void onClickFinishRun(View v) throws ExecutionException, InterruptedException {
        // stop the service
        stopService(serviceIntent);
        // unbind to close connection
        unbindService(serviceConnection);
        // unregister callbacks to stop communication with service
        myService.unregisterCallback(callback);
        myService = null;

        // change visibility of buttons and texts to show user that the run has been stopped
        startButton.setVisibility(View.VISIBLE);
        finishButton.setVisibility(View.INVISIBLE);
        state.setVisibility(View.INVISIBLE);
        running = false;

        // update the screen to display the values of the finished run
        updateScreenValues();

        // create a new Run object with the values of the finished run
        Run newRun = new Run(0, speed, distance, time, dateToday, null, null, null);
        // insert run into db using viewmodel
        runViewModel.insert(newRun);

        // query db for the updated distance run today and display it to the user
        try {
            Thread.sleep(500);
            distanceToday.setText(df.format(runViewModel.getDistanceByDate(dateToday)));
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int id = 0; // initialisation
        // query db for automatically generated id of run
        try {
            Thread.sleep(500);
            id = runViewModel.getRunIdByTime(newRun.getTime());
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // query db for id of fastest run
        int idFastest = runViewModel.getIdByFastest();
        // if run is the fastest (and not the first run because it is obviously the fastest), alert the user
        if((id == idFastest) && (id != 1)) {
            alertFastestRun();
        }
    }

    // create and show the user an alert to tell them they just ran the fastest they ever have
    public void alertFastestRun() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Congratulations!");
        alertDialogBuilder.setMessage("You just beat your fastest run!");
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    // display updated values of latest run on screen for user
    public void updateScreenValues() {
        endTime = System.currentTimeMillis();
        double millis = endTime - startTime;
        long iMillis = (long) millis;
        time = String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(iMillis), TimeUnit.MILLISECONDS.toMinutes(iMillis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(iMillis)),
                TimeUnit.MILLISECONDS.toSeconds(iMillis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(iMillis)));
        millis = millis / (1000 * 60 * 60); // milliseconds to hours
        distance = distance / 1000; // meters to kilometers
        speed = distance / millis;
        timeValue.setText(time);
        distanceValue.setText(df.format(distance));
        speedValue.setText(df.format(speed));
        dateToday = dateFormat.format(date);
    }

    // reset values on screen to empty when a new run has been started
    public void resetScreenValues() {
        timeValue.setText("");
        distanceValue.setText("");
        speedValue.setText("");
    }

    // go to activity to view run details
    @Override
    public void onItemClick(View view, int position) {
        // get the run object from the adapter when user clicks on it on the recycler view
        selectedRun = runAdapter.getItem(position);
        // create intent to go to activity to view run details and annotate run
        Intent intent = new Intent(MainActivity.this, ViewRun.class);
        // put run in intent
        intent.putExtra("selectedRun", selectedRun);
        // start activity with intent and request code 1 (used for annotations)
        startActivityForResult(intent, 1);
    }

    // process results from other activities started by main activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // result code 1 is result from activity where the user might've annotated an existing run
        if(resultCode == 1) {
            String newName; // initialise name
            if (data.getStringExtra("name") == null) {
                // if user did not enter a name string or delete it, set it to be an empty string
                newName = "";
            } else {
                // else, set the name string to be the one sent back with the intent (the result of starting the activity)
                newName = data.getStringExtra("name");
            }
            // update db with new name using viewmodel
            runViewModel.updateName(newName, selectedRun.getId());

            // get new rating from intent, or use default one if null
            int newRating = data.getIntExtra("rating", 1);
            // update db with new rating using viewmodel
            runViewModel.updateRating(newRating, selectedRun.getId());
            // get new temperature from intent, or use default one if null
            int newTemperature = data.getIntExtra("temperature", 0);
            // update db with new temperature using viewmodel
            runViewModel.updateTemperature(newTemperature, selectedRun.getId());

            // query db for distance run today and update value on screen to display it to the user
            try {
                distanceToday.setText(df.format(runViewModel.getDistanceByDate(dateToday)));
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // result code 2 is result from activity where the user deleted a run
        if(resultCode == 2) {
            // get the id of the run to be deleted from the result intent
            int id = data.getIntExtra("id", 1);
            // delete the run from the db using the viewmodel
            runViewModel.deleteRunById(id);
        }
    }

    // communicate with the service through callbacks
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // when run has started, connect to the service and start registering callbacks
            // TODO Auto-generated method stub
            Log.d("g53mdp", "MainActivity onServiceConnected");
            myService = (RunningService.MyBinder) service;
            myService.registerCallback(callback);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            // when run has ended, stop callbacks and set service to null
            // TODO Auto-generated method stub
            Log.d("g53mdp", "MainActivity onServiceDisconnected");
            myService.unregisterCallback(callback);
            myService = null;
        }
    };

    // callbacks when service is running
    ICallback callback = new ICallback() {
        @Override
        // callback event that gets location from service and updates location values to calculate and increment distance
        public void runningTrackerEvent(Location location) {
            runOnUiThread(new Runnable() {
                // updates as the service is running to show the user the progress
                @Override
                public void run() {
                    if (firstLocation == true) {
                        prevLocation = location;
                        firstLocation = false;
                    } else {
                        prevLocation = nextLocation;
                    }
                    nextLocation = location;
                    distance += nextLocation.distanceTo(prevLocation);
                }
            });
        }
    };

    // sort runs in recycler view according to latest run first (sort by id, desc order) or speed (fastest first)
    public void onClickSort(View v) {
        // sort recipes of main activity by title or rating
        buttonSort = findViewById(R.id.buttonSort);
        if (sortId == true) {
            sortId = false;
            buttonSort.setText("SORT BY DATE");
            runViewModel.getAllRunsBySpeed().observe(this, runs -> { runAdapter.setData(runs); });
        } else {
            sortId = true;
            buttonSort.setText("SORT BY SPEED");
            runViewModel.getAllRunsById().observe(this, runs -> { runAdapter.setData(runs); });
        }
    }

    // get statistics from db and start activity to view those statistics
    public void onClickViewStatistics(View v) {
        // if db contains runs, send them to activity
        if(runAdapter.getItemCount() != 0) {
            // create list of runs from querying db using viewmodel
            List<Run> runs = runViewModel.getAllRunsById().getValue();
            // reverse list to start with the latest runs
            Collections.reverse(runs);
            // initialise list of entries for the chart in the statistics activity
            ArrayList<Entry> lineEntries = new ArrayList<>();
            // create entries using the id and distance of speeds, add to list of entries
            for(int i = 0; i < runs.size(); i++) {
                Entry entry = new Entry((float) i, (float) runs.get(i).getDistance());
                lineEntries.add(entry);
            }

            // create bundle to put list of entries in
            Bundle bundle = new Bundle();
            bundle.putSerializable("lineEntries", lineEntries);
            // create intent to start statistics activity, add bundle to it and start the activity
            Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }
        // else, alert the user that there are no runs to see the statistics of and don't start the activity
        else {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("No Current Statistics to View");
            alertDialogBuilder.setMessage("Statistics empty. Add new run to view statistics.");
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    // lifecycle calls of app
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Log.d("g53mdp", "MainActivity onDestroy");
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        Log.d("g53mdp", "MainActivity onPause");
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.d("g53mdp", "MainActivity onResume");
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        Log.d("g53mdp", "MainActivity onStart");
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        Log.d("g53mdp", "MainActivity onStop");
    }

}