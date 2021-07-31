package com.example.runningtracker;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.os.Bundle;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.util.ArrayList;

// statistics activity to view run statistics, i.e., distance per run, starting at first run
public class StatisticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        // get linechart
        LineChart lineChart = findViewById(R.id.lineChart);

        // get list of entries from bundle sent to start this activity
        Bundle bundle = getIntent().getExtras();
        ArrayList<Entry> lineEntries = (ArrayList<Entry>) bundle.getSerializable("lineEntries");

        // create data using list of entries and insert into line chart
        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Distance (km)/run (from first to last)");
        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);

        // design chart
        lineDataSet.setValueTextColor(Color.BLACK);
        lineDataSet.setValueTextSize(18f);

        Description desc = new Description();
        desc.setText("Running Statistics");
        desc.setTextSize(28);
        lineChart.setDescription(desc);
    }
}