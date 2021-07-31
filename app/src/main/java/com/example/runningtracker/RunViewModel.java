package com.example.runningtracker;

import android.app.Application;
import java.util.List;
import java.util.concurrent.ExecutionException;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class RunViewModel extends AndroidViewModel {

    private MyRepository repository;
    private final LiveData<List<Run>> allRunsById;
    private final LiveData<List<Run>> allRunsBySpeed;

    // constructor for viewmodel class that uses repository to query db
    // initialise lists of all runs according to id and speed
    public RunViewModel(Application application) {
        super(application);
        repository = new MyRepository(application);
        allRunsById = repository.getAllRunsById();
        allRunsBySpeed = repository.getAllRunsBySpeed();
    }

    // getters for lists of runs by id and speed
    LiveData<List<Run>> getAllRunsById() {
        return allRunsById;
    }

    LiveData<List<Run>> getAllRunsBySpeed() {
        return allRunsBySpeed;
    }

    // insert run into db
    public void insert(Run run) {
        repository.insert(run);
    }

    // update name of run
    public void updateName(String name, int id) {
        repository.updateName(name, id);
    }

    // update temperature of run
    public void updateTemperature(int temperature, int id) {
        repository.updateTemperature(temperature, id);
    }

    // update rating of run
    public void updateRating(int rating, int id) {
        repository.updateRating(rating, id);
    }

    // delete specific run by id
    public void deleteRunById(int id) {
        repository.deleteRunById(id);
    }

    // get id of fastest run
    public int getIdByFastest() throws ExecutionException, InterruptedException {
        return repository.getIdByFastest();
    }

    // get run by time
    public int getRunIdByTime(String time) throws ExecutionException, InterruptedException {
        return repository.getRunIdByTime(time);
    }

    // get distance run at specific date
    public double getDistanceByDate(String date) throws ExecutionException, InterruptedException {
        return repository.getDistanceByDate(date);
    }

}
