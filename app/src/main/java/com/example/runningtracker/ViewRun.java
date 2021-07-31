package com.example.runningtracker;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.DecimalFormat;

// activity to view and annotate run details
public class ViewRun extends AppCompatActivity {

    private TextView runName, runRating, runTemperature;
    private Run run;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_run);

        // get run from intent used to start activity
        run = (Run) getIntent().getSerializableExtra("selectedRun");

        // get speed, distance, time and date values from run and display to user (non null)
        DecimalFormat df = new DecimalFormat("#.00");
        double speed = run.getSpeed();
        TextView runSpeed = findViewById(R.id.viewSpeed);
        runSpeed.setText(df.format(speed));

        double distance = run.getDistance();
        TextView runDistance = findViewById(R.id.viewDistance);
        runDistance.setText(df.format(distance));

        String time = run.getTime();
        TextView runTime = findViewById(R.id.viewTime);
        runTime.setText(time);

        String date = run.getDate();
        TextView runDate = findViewById(R.id.viewDate);
        runDate.setText(date);

        // display type of exercise to user, walking, jogging or running, based on speed
        TextView runExercise = findViewById(R.id.viewExercise);
        String exercise = "";
        if(speed <= 6) {
            exercise = "Walking";
        } else if((speed > 6) && (speed < 10)) {
            exercise = "Jogging";
        } else {
            exercise = "Running";
        }
        runExercise.setText(exercise);

        // display name to user if not null, if null, display empty string
        runName = findViewById(R.id.viewName);
        if(run.getName() == null) {
            runName.setText("");
            runName.setHint("Name");
        } else {
            runName.setText(run.getName());
        }

        // display rating to user if not null, if null, display hint to enter rating between 1 and 5
        runRating = findViewById(R.id.viewRating);
        if(run.getRating() == null) {
            runRating.setText("");
            runRating.setHint("Rating 1-5");
        } else {
            runRating.setText(String.valueOf(run.getRating()));
        }

        // display temperature to user if not null, if null, display hint to enter temperature
        runTemperature = findViewById(R.id.viewTemperature);
        if(run.getTemperature() == null) {
            runTemperature.setText("");
            runTemperature.setHint("Temperature");
        } else {
            runTemperature.setText(String.valueOf(run.getTemperature()));
        }
    }

    // click to save annotations (if any) and return to main activity
    public void onClickSave(View v) {
        String name = String.valueOf(runName.getText());
        int rating;
        try {
            rating = Integer.parseInt(String.valueOf(runRating.getText()));
        }catch (NumberFormatException e){
            rating = 1;
        }
        int temperature;
        try {
            temperature = Integer.parseInt(String.valueOf(runTemperature.getText()));
        }catch (NumberFormatException e){
            temperature = 0;
        }

        // if rating is not between 1 and 5, alert the user and clear the input so they can input new data
        if(rating < 1 || rating > 5) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("Rating must be a number between 1 and 5.");
            alertDialogBuilder.setTitle("Enter new rating");
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            runRating.setText("");
        }
        // else, finish activity and send back result to main
        else {
            Intent intent = new Intent(ViewRun.this, MainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("name", name);
            bundle.putInt("rating", rating);
            bundle.putInt("temperature", temperature);
            intent.putExtras(bundle);
            setResult(1, intent);
            finish();
        }
    }

    // ask user if they are sure they want to delete run
    // if yes, send id of run to be deleted with result to main
    // if no (cancel), nothing happens
    public void onClickDelete(View v) {
        int id = run.getId();

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Are you sure you wish to delete this run?");
        alert.setMessage("Action cannot be reversed.");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        alert.setView(layout);
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int yes) {
                Intent intent = new Intent(ViewRun.this, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("id", id);
                intent.putExtras(bundle);
                setResult(2, intent);
                finish();
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int yes) {
            }
        });
        alert.show();
    }

}