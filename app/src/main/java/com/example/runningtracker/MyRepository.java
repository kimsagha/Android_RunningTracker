package com.example.runningtracker;

import android.app.Application;
import android.database.Cursor;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MyRepository {

    private RunDao runDao;
    private LiveData<List<Run>> allRunsById;
    private LiveData<List<Run>> allRunsBySpeed;

    // constructor for repository, create dao and get all runs from db using dao (access through getters below)
    MyRepository(Application application) {
        MyRoomDatabase db = MyRoomDatabase.getDatabase(application);
        runDao = db.runDao();
        allRunsById = runDao.getRunsById();
        allRunsBySpeed = runDao.getRunsBySpeed();
    }

    LiveData<List<Run>> getAllRunsById() {
        return allRunsById;
    }

    LiveData<List<Run>> getAllRunsBySpeed() {
        return allRunsBySpeed;
    }

    // query db using dao to insert new runs into it
    void insert(Run run) {
        MyRoomDatabase.databaseWriteExecutor.execute(() -> {
            runDao.insert(run);
        });
    }

    // update names of runs using dao
    void updateName(String name, int id) {
        MyRoomDatabase.databaseWriteExecutor.execute(() -> {
            runDao.updateName(name, id);
        });
    }

    // update temperature of runs using dao
    void updateTemperature(int temperature, int id) {
        MyRoomDatabase.databaseWriteExecutor.execute(() -> {
            runDao.updateTemperature(temperature, id);
        });
    }

    // update rating of runs using dao
    void updateRating(int rating, int id) {
        MyRoomDatabase.databaseWriteExecutor.execute(() -> {
            runDao.updateRating(rating, id);
        });
    }

    // delete run based on its id using dao
    void deleteRunById(int id) {
        MyRoomDatabase.databaseWriteExecutor.execute(() -> {
            runDao.deleteRunById(id);
        });
    }

    // create Future task to query db for id of fastest run, get the id after the future has finished
    int getIdByFastest() throws ExecutionException, InterruptedException {
        Future<Integer> future = MyRoomDatabase.databaseWriteExecutor.submit(new Callable<Integer>(){
            public Integer call() throws Exception {
                return runDao.getIdByFastest();
            }
        });
        int runId = future.get();
        return runId;
    }

    // create Future task to query db for id of run based on time, get the id after the future has finished
    int getRunIdByTime(String time) throws ExecutionException, InterruptedException {
        Future<Integer> future = MyRoomDatabase.databaseWriteExecutor.submit(new Callable<Integer>(){
            public Integer call() throws Exception {
                return runDao.getRunIdByTime(time);
            }
        });
        int runId = future.get();
        return runId;
    }

    // create Future task to query db for distance run on a specific date, get cursor and increment values
    double getDistanceByDate(String date) throws ExecutionException, InterruptedException {
        Future<Cursor> future = MyRoomDatabase.databaseWriteExecutor.submit(() -> {
            return runDao.getDistanceByDate(date);
        });
        Cursor c = future.get();
        double distance = 0;
        int i = 0;
        c.moveToFirst();
        while(!c.isAfterLast()) {
            distance += c.getDouble(0);
            i++;
            c.moveToNext();
        }
        c.close();
        return distance;
    }

    Cursor getAllRuns() throws ExecutionException, InterruptedException {
        Future<Cursor> future = MyRoomDatabase.databaseWriteExecutor.submit(() -> {
            return runDao.getAllRuns();
        });
        return future.get();
    }

}
