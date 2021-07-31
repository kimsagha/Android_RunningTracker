package com.example.runningtracker;

import android.database.Cursor;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

// dao with db queries
@Dao
public interface RunDao {

    // insert new run into db
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Run run);

    // delete all runs when creating new db
    @Query("DELETE FROM runs")
    void deleteAll();

    // delete specific run based on its id
    @Query("DELETE FROM runs WHERE id = :id")
    void deleteRunById(int id);

    // get all runs ordered by id in descending order, i.e., latest run first
    @Query("SELECT * FROM runs ORDER BY id DESC")
    LiveData<List<Run>> getRunsById();

    // get all runs ordered by speed in descending order, i.e., fastest run first
    @Query("SELECT * FROM runs ORDER BY speed DESC")
    LiveData<List<Run>> getRunsBySpeed();

    // update name of run
    @Query("UPDATE Runs SET name = :name WHERE id = :id")
    void updateName(String name, int id);

    // update temperature of run
    @Query("UPDATE Runs SET temperature = :temperature WHERE id = :id")
    void updateTemperature(int temperature, int id);

    // update rating of run
    @Query("UPDATE Runs SET rating = :rating WHERE id = :id")
    void updateRating(int rating, int id);

    // get id of fastest run
    @Query("SELECT id FROM runs WHERE speed=(SELECT MAX(speed) FROM runs)")
    int getIdByFastest();

    // get run based on time
    @Query("SELECT id from runs WHERE time = :time")
    int getRunIdByTime(String time);

    // get distance run on specific date
    @Query("SELECT distance from runs WHERE date = :date")
    Cursor getDistanceByDate(String date);

    // get cursor pointing at all runs ordered by id in descending order
    @Query("SELECT * FROM runs ORDER BY id DESC")
    Cursor getAllRuns();

}
