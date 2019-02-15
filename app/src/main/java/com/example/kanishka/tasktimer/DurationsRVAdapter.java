package com.example.kanishka.tasktimer;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.text.format.DateFormat;


import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.Locale;


public class DurationsRVAdapter extends RecyclerView.Adapter<DurationsRVAdapter.ViewHolder> {

  private static Cursor mCursor;

  private static final String TAG = "DurationsRVAdapter";
  public static ArrayList<PieEntry> yValues = new ArrayList<>();

  private final java.text.DateFormat mDateFormat;
  public static String name;
  public static long totalDuration;


  public DurationsRVAdapter(Context context, Cursor cursor) {
    this.mCursor = cursor;
    mDateFormat = DateFormat.getDateFormat(context);
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_durations_items, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    if ((mCursor != null) && (mCursor.getCount() != 0)) {
      if(!mCursor.moveToPosition(position)) {
        throw new IllegalStateException("Could not move cursor to position" + position);
      }
      //yValues.clear();
      name = mCursor.getString(mCursor.getColumnIndex(DurationsContract.Columns.DURATIONS_NAME));
      String description = mCursor.getString(mCursor.getColumnIndex(DurationsContract.Columns.DURATIONS_DESCRIPTION));
      Long startTime = mCursor.getLong(mCursor.getColumnIndex(DurationsContract.Columns.DURATIONS_START_TIME));
      totalDuration = mCursor.getLong(mCursor.getColumnIndex(DurationsContract.Columns.DURATIONS_DURATION));

      holder.name.setText(name);
      if (holder.description != null) {
        holder.description.setText(description);
      }

      String userDate = mDateFormat.format(startTime * 1000);
      String totalTime = formatDuration(totalDuration);

      holder.startDate.setText(userDate);
      holder.duration.setText(totalTime);

      yValues.add(new PieEntry(totalDuration, name));
    }
  }


  @Override
  public int getItemCount() {
    Log.d(TAG, "getItemCount: starts");
    return mCursor != null ? mCursor.getCount() : 0;
  }

  public String formatDuration(long duration) {
    //convert seconds to hh:mm:ss
    long hours = duration / 3600;
    long reminder = duration - (hours * 3600);
    long minutes = reminder / 60;
    long seconds = reminder - (minutes * 60);

    return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
  }

//  public static void graphData(){
//    yValues.add(new PieEntry(totalDuration, name));
//  }

  Cursor swapCursor(Cursor newCursor) {
    yValues.clear();
    if (newCursor == mCursor) {
      return null;
    }

    int numItems = getItemCount();

    final Cursor oldCursor = mCursor;
    mCursor = newCursor;
    if (newCursor != null) {
      notifyDataSetChanged();
    } else {
      notifyItemRangeRemoved(0, numItems);
    }
    return oldCursor;
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    TextView name;
    TextView description;
    TextView startDate;
    TextView duration;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      this.name = itemView.findViewById(R.id.td_name);
      this.description = itemView.findViewById(R.id.td_description);
      this.startDate = itemView.findViewById(R.id.td_start);
      this.duration = itemView.findViewById(R.id.td_duration);
    }

  }


}
