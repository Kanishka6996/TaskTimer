package com.example.kanishka.tasktimer;

import android.util.Log;

import java.io.Serializable;
import java.util.Date;

class Timing implements Serializable {

  public static final long serialVersionUID = 20190129L;
  private static final String TAG = Timing.class.getSimpleName();

  private long m_Id;
  private Task mTask;
  private long mStartTime;
  private long mDuration;

  public Timing(Task task) {
    mTask = task;
    //initialize start time to zero for new object
    Date currentTime = new Date();
    mStartTime = currentTime.getTime()/1000; //providing second
    mDuration = 0;

  }

  long getId() {
    return m_Id;
  }

  void setId(long id) {
    m_Id = id;
  }

  Task getTask() {
    return mTask;
  }

  void setTask(Task task) {
    mTask = task;
  }

  long getStartTime() {
    return mStartTime;
  }

  void setStartTime(long startTime) {
    mStartTime = startTime;
  }

  long getDuration() {
    return mDuration;
  }

  void setDuration() {
    Date currentTime = new Date();
    mDuration = (currentTime.getTime() / 1000) - mStartTime;
    Log.d(TAG, mTask.getId() + "Start Time: " + mStartTime + " | Duration: " + mDuration);
  }
}
