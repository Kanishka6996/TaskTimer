package com.example.kanishka.tasktimer;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import java.security.InvalidParameterException;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
  CursorRecyclerViewAdapter.OnTaskClickListner {
  private static final String TAG = "MainActivityFragment";

  public static final int LOADER_ID = 0;
  public static final String SAVED_TIMING = "TIMING_DATA";
  public static final String CHANNEL_ID = "Personal Notification";
  private CursorRecyclerViewAdapter mAdapter;

  private Timing mCurrentTiming;

  private long duration;

  public MainActivityFragment() {
    Log.d(TAG, "MainActivityFragment: starts");
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    Log.d(TAG, "onActivityCreated: starts");
    super.onActivityCreated(savedInstanceState);

    Activity activity = getActivity();
    if (!(activity instanceof CursorRecyclerViewAdapter.OnTaskClickListner)) {
      throw new ClassCastException(activity.getClass().getSimpleName() +
        "must implement CursorRecyclerViewAdapter.OnClickSaved interface");
    }

    LoaderManager.getInstance(this).initLoader(LOADER_ID, null, this);

    // Restore any saved timing
    SharedPreferences savedTiming = getActivity().getPreferences(Context.MODE_PRIVATE);

    boolean timingPresent = savedTiming.contains(SAVED_TIMING);
    if (timingPresent) {
      String serializedTiming = savedTiming.getString(SAVED_TIMING, null);
      Gson gson = new Gson();
      mCurrentTiming = gson.fromJson(serializedTiming, Timing.class);


      // Now we've read it, get rid of it
      SharedPreferences.Editor editor = savedTiming.edit();
      editor.remove(SAVED_TIMING).apply();

      Log.d(TAG, "restored saved timing " + mCurrentTiming.toString());
    }

    setTimingText(mCurrentTiming, this.getView());
  }

  @Override
  public void onStop() {
    super.onStop();

    Gson gson = new Gson();
    String serializedTiming = gson.toJson(mCurrentTiming);

    SharedPreferences savedTiming = getActivity().getPreferences(Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = savedTiming.edit();
    if (mCurrentTiming != null) {
      editor.putString(SAVED_TIMING, serializedTiming);
    } else {
      // Make sure we don't have a timing saved.
      editor.remove(SAVED_TIMING);
    }
    editor.apply();
  }

  @Override
  public void onEditClick(@NonNull Task task) {
    Log.d(TAG, "onEditClick: called");
    CursorRecyclerViewAdapter.OnTaskClickListner listner = (CursorRecyclerViewAdapter.OnTaskClickListner) getActivity();
    if (listner != null) {
      listner.onEditClick(task);
    }
  }

  @Override
  public void onDeleteClick(@NonNull Task task) {
    Log.d(TAG, "onDeleteClick: called");
    CursorRecyclerViewAdapter.OnTaskClickListner listner = (CursorRecyclerViewAdapter.OnTaskClickListner) getActivity();
    if (listner != null) {
      listner.onDeleteClick(task);
    }
  }

  @Override
  public void onTaskLongClick(@NonNull Task task) {
    Log.d(TAG, "onTaskLongClick: called");
    if (mCurrentTiming != null) {
      if (task.getId() == mCurrentTiming.getTask().getId()) {
        Log.d(TAG, "onTaskLongClick: stop taskkk");
        // the current task was tapped a second time, so stop timing
        saveTiming(mCurrentTiming);
        mCurrentTiming = null;
        setTimingText(null, this.getView());
        cancleNotification();
      } else {
        // a new task is being timed, so stop the old one first
        Log.d(TAG, "onTaskLongClick: new task timing");
        mCurrentTiming = new Timing(task);
        saveTiming(mCurrentTiming);
        setTimingText(mCurrentTiming, this.getView());
        setNotification(mCurrentTiming);
      }
    } else {
      // no task being timed, so start timing the new task
      Log.d(TAG, "onTaskLongClick: new taskkk");
      mCurrentTiming = new Timing(task);
      setTimingText(mCurrentTiming, this.getView());
      setNotification(mCurrentTiming);
    }
  }

  @SuppressWarnings("ConstantConditions")
  private void saveTiming(@NonNull Timing currentTiming) {
    Log.d(TAG, "Entering saveTiming");

    // If we have an open timing, set the duration and save
    currentTiming.setDuration();

    ContentResolver contentResolver = getActivity().getContentResolver();
    ContentValues values = new ContentValues();
    values.put(TimingsContract.Columns.TIMINGS_TASK_ID, currentTiming.getTask().getId());
    values.put(TimingsContract.Columns.TIMINGS_START_TIME, currentTiming.getStartTime());
    values.put(TimingsContract.Columns.TIMINGS_DURATION, currentTiming.getDuration());

    // update table in database
    contentResolver.insert(TimingsContract.CONTENT_URI, values);

    Log.d(TAG, "Exiting saveTiming");
  }

  private void setTimingText(Timing timing, View view) {
    if (timing != null) {
      Snackbar.make(view, getString(R.string.current_timing_text, timing.getTask().getName()), Snackbar.LENGTH_INDEFINITE).show();
    } else {
      Snackbar.make(view, R.string.no_task_message, Snackbar.LENGTH_LONG).show();
    }
  }

  public void setNotification(Timing timing) {
    Log.d(TAG, "setNotification: Generated");

    Intent intent = new Intent(this.getContext(), MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    PendingIntent pendingIntent = PendingIntent.getActivity(this.getContext(), 0, intent, 0);

    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this.getContext(), CHANNEL_ID);

    mBuilder.setSmallIcon(R.drawable.ic_task_timer);
    mBuilder.setContentTitle("Timing " + timing.getTask().getName() + ". \n");
    mBuilder.setContentText("To stop timing open the app and long press on the task.");
    mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
    mBuilder.setContentIntent(pendingIntent);
    mBuilder.setOngoing(true);



    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      CharSequence name = getString(R.string.channel_name);
      String description = getString(R.string.channel_description);
      int importance = NotificationManager.IMPORTANCE_DEFAULT;
      NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
      channel.setDescription(description);

      NotificationManager notificationManager = getContext().getSystemService(NotificationManager.class);
      notificationManager.createNotificationChannel(channel);
    }
    NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(this.getContext());
    mNotificationManager.notify(0, mBuilder.build());
  }

  public void cancleNotification() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      CharSequence name = getString(R.string.channel_name);
      String description = getString(R.string.channel_description);
      int importance = NotificationManager.IMPORTANCE_DEFAULT;
      NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
      channel.setDescription(description);

      NotificationManager notificationManager = getContext().getSystemService(NotificationManager.class);
      notificationManager.cancel(0);
    }
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    Log.d(TAG, "onCreateView: starts");

    View view = inflater.inflate(R.layout.fragment_main, container, false);
    RecyclerView recyclerView = view.findViewById(R.id.task_list);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

    if (mAdapter == null) {
      mAdapter = new CursorRecyclerViewAdapter(null, this);
//    } else {
//      mAdapter.setListner((CursorRecyclerViewAdapter.OnTaskClickListner) getActivity());
    }
    recyclerView.setAdapter(mAdapter);
    return view;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    Log.d(TAG, "onCreate: called");
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
  }

  @NonNull
  @Override
  public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle bundle) {
    Log.d(TAG, "onCreateLoader: starts");

    String[] projection = {TasksContract.Columns._ID, TasksContract.Columns.TASKS_NAME,
      TasksContract.Columns.TASKS_DESCRIPTION, TasksContract.Columns.TASKS_SORTORDER};
    String sortOrder = TasksContract.Columns.TASKS_SORTORDER + "," + TasksContract.Columns.TASKS_NAME + " COLLATE NOCASE";

    switch (id) {
      case LOADER_ID:
        return new CursorLoader(getActivity(),
          TasksContract.CONTENT_URI,
          projection,
          null,
          null,
          sortOrder);

      default:
        throw new InvalidParameterException(TAG + ".onCreateLoader called with invalid loader id" + id);
    }
  }

  @Override
  public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
    Log.d(TAG, "onLoadFinished: Entering onLoadFinished");
    mAdapter.swapCursor(data);
    int count = mAdapter.getItemCount();

    Log.d(TAG, "onLoadFinished: count is" + count);

  }

  @Override
  public void onLoaderReset(@NonNull Loader<Cursor> loader) {
    Log.d(TAG, "onLoaderReset: starts");
    mAdapter.swapCursor(null);

  }
}
