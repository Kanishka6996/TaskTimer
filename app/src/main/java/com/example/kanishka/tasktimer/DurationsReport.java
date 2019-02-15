package com.example.kanishka.tasktimer;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.support.v4.app.LoaderManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.TextView;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.security.InvalidParameterException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import static com.example.kanishka.tasktimer.DurationsRVAdapter.yValues;


public class DurationsReport extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
              DatePickerDialog.OnDateSetListener,
              AppDialog.DialogEvents,
              View.OnClickListener{

  private static final String TAG = "DurationsReport";

  private static final int LOADER_ID = 1;

  public static final int DIALOG_FILTER = 1;
  public static final int DIALOG_DELETE = 2;

  private static final String SELECTION_PARAM = "SELECTION";
  private static final String SELECTION_ARGS_PARAM = "SELECTION_ARGS";
  private static final String SORT_ORDER_PARAM = "SORT_ORDER";

  public static final String DELETION_DATE = "DELETION_DATE";

  public static final String CURRENT_DATE = "CURRENT_DATE";
  public static final String DISPLAY_WEEK = "DISPLAY_WEEK";

  private Bundle mArgs = new Bundle();
  private boolean mDisplayWeek = true;

  private DurationsRVAdapter mAdapter;
  private AlertDialog mDialog = null;

  private final GregorianCalendar mCalendar = new GregorianCalendar();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_durations_report);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    if (savedInstanceState != null) {
      long timeInMillis = savedInstanceState.getLong(CURRENT_DATE, 0);
      // it'll be zero when the Activity's first created, so don't set the value
      if (timeInMillis != 0) {
        mCalendar.setTimeInMillis(timeInMillis);
        // Make sure the time part is cleared, because we filter the database by seconds
        mCalendar.clear(GregorianCalendar.HOUR_OF_DAY);
        mCalendar.clear(GregorianCalendar.MINUTE);
        mCalendar.clear(GregorianCalendar.SECOND);
      }
      mDisplayWeek = savedInstanceState.getBoolean(DISPLAY_WEEK, true);
    }

    applyFilter();

    TextView taskName = findViewById(R.id.td_name_heading);
    taskName.setOnClickListener(this);

    TextView taskDescription = findViewById(R.id.td_description_heading);
    if (taskDescription != null) {
      taskDescription.setOnClickListener(this);
    }

    TextView taskDate = findViewById(R.id.td_start_heading);
    taskDate.setOnClickListener(this);

    TextView taskDuration = findViewById(R.id.td_duration_heading);
    taskDuration.setOnClickListener(this);

    RecyclerView recyclerView = findViewById(R.id.td_list);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    if (mAdapter == null) {
      mAdapter = new DurationsRVAdapter(this, null);
    }
    recyclerView.setAdapter(mAdapter);

    getSupportLoaderManager().initLoader(LOADER_ID, mArgs, this);

  }

  @Override
  public void onClick(View v) {
    Log.d(TAG, "onClick: called");

    switch (v.getId()) {
      case R.id.td_name_heading :
        mArgs.putString(SORT_ORDER_PARAM, DurationsContract.Columns.DURATIONS_NAME);
        break;
      case R.id.td_description_heading :
        mArgs.putString(SORT_ORDER_PARAM, DurationsContract.Columns.DURATIONS_DESCRIPTION);
        break;
      case R.id.td_start_heading :
        mArgs.putString(SORT_ORDER_PARAM, DurationsContract.Columns.DURATIONS_START_DATE);
        break;
      case R.id.td_duration_heading :
        mArgs.putString(SORT_ORDER_PARAM, DurationsContract.Columns.DURATIONS_DURATION);
        break;
    }

    getSupportLoaderManager().restartLoader(LOADER_ID, mArgs, this);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putLong(CURRENT_DATE, mCalendar.getTimeInMillis());
    outState.putBoolean(DISPLAY_WEEK, mDisplayWeek);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_report, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    int id = item.getItemId();

    switch (id) {
      case R.id.rm_filter_period:
        mDisplayWeek = !mDisplayWeek;
        applyFilter();
        invalidateOptionsMenu();
        getSupportLoaderManager().restartLoader(LOADER_ID, mArgs, this);
        return true;

      case R.id.rm_filter_date:
        showDatePickerDialog("Select Date Report", DIALOG_FILTER);     //actual delete done in onDateSet()
        return true;

      case R.id.rm_delete:
        showDatePickerDialog("Select Date to delete data before it.", DIALOG_DELETE);
        return true;

      case R.id.rm_graph :
        showGraphDialog();
        return true;
    }

    return super.onOptionsItemSelected(item);
  }
  private PieChart pieChart;

  public void showGraphDialog() {
    Log.d(TAG, "showGraphDialog: called");

    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
      WindowManager.LayoutParams.FLAG_FULLSCREEN);
    setContentView(R.layout.pie_chart);

    pieChart = findViewById(R.id.pieChart);
    pieChart.setUsePercentValues(true);
    pieChart.getDescription().setEnabled(true);
    pieChart.setExtraOffsets(5,10,5,5);
    pieChart.setDragDecelerationFrictionCoef(0.9f);
    pieChart.setTransparentCircleRadius(61f);
    pieChart.setHoleColor(Color.WHITE);
    pieChart.setCenterText("TASK DURATIONS");
    pieChart.animateY(1000, Easing.EasingOption.EaseInOutCubic);


    PieDataSet dataSet = new PieDataSet(yValues, "Task Durations");
    dataSet.setSliceSpace(30f);
    dataSet.setSelectionShift(5f);
    dataSet.setColors(ColorTemplate.COLORFUL_COLORS);

    PieData pieData = new PieData((dataSet));
    pieData.setValueTextSize(10f);
    pieData.setValueTextColor(Color.BLACK);
    pieChart.setData(pieData);

  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    MenuItem item = menu.findItem(R.id.rm_filter_period);

    if (item != null) {
      if (mDisplayWeek) {
        item.setIcon(R.drawable.ic_filter_1_black_24dp);
        item.setTitle(R.string.title_filter_day);
      } else {
        item.setIcon(R.drawable.ic_filter_7_black_24dp);
        item.setTitle(R.string.title_filter_week);
      }
    }

    return super.onPrepareOptionsMenu(menu);
  }

  private void showDatePickerDialog(String title, int dialogId) {
    Log.d(TAG, "showDatePickerDialog: enterting");
    DialogFragment dialogFragment = new DatePickerFragment();

    Bundle arguments = new Bundle();
    arguments.putInt(DatePickerFragment.DATE_PICKER_ID, dialogId);
    arguments.putString(DatePickerFragment.DATE_PICKER_TITLE, title);
    arguments.putSerializable(DatePickerFragment.DATE_PICKER_DATE, mCalendar.getTime());

    dialogFragment.setArguments(arguments);
    dialogFragment.show(getSupportFragmentManager(), "datePicker");
    Log.d(TAG, "showDatePickerDialog: exiting");
  }

  @Override
  public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
    Log.d(TAG, "onDateSet: called");

    int dialogId = (int) view.getTag();
    mCalendar.set(year, month, dayOfMonth, 0, 0, 0);

    switch (dialogId) {
      case DIALOG_FILTER:
        applyFilter();
        getSupportLoaderManager().restartLoader(LOADER_ID, mArgs, this);
        break;

      case DIALOG_DELETE:
        String fromDate = DateFormat.getDateFormat(this)
          .format(mCalendar.getTimeInMillis());
        AppDialog dialog = new AppDialog();
        Bundle args = new Bundle();
        args.putInt(AppDialog.DIALOG_ID, 1);
        args.putString(AppDialog.DIALOG_MESSAGE, "Are you sure you want to delete all timings before " + fromDate + "?");
        args.putLong(DELETION_DATE, mCalendar.getTimeInMillis());
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), null);
        break;

      default:
        throw new IllegalArgumentException("Invalif mode when receiving DatePickerDialog result");
    }
  }

  private void deleteRecords(long timeInMillis) {
    Log.d(TAG, "deleteRecords: entering");

    long longDate = timeInMillis / 1000;
    String[] selectionArgs = new String[]{Long.toString(longDate)};
    String selection = TimingsContract.Columns.TIMINGS_START_TIME + " < ?";

    Log.d(TAG, "deleteRecords: dates prior to " + longDate);

    ContentResolver contentResolver = getContentResolver();
    contentResolver.delete(TimingsContract.CONTENT_URI, selection, selectionArgs);
    applyFilter();
    getSupportLoaderManager().restartLoader(LOADER_ID, mArgs, this);
    Log.d(TAG, "deleteRecords: exiting");
  }

  @Override
  public void onPositiveDialogResult(int dialogId, Bundle args) {
    Log.d(TAG, "onPositiveDialogResult: called");
    long deleteDate = args.getLong(DELETION_DATE);
    deleteRecords(deleteDate);
    //reset the cursor
    getSupportLoaderManager().restartLoader(LOADER_ID, mArgs, this);
  }

  @Override
  public void onNegativeDialogResult(int dialogId, Bundle args) {

  }

  @Override
  public void onDialogCancelled(int dialogId) {

  }

  private void applyFilter() {
    Log.d(TAG, "applyFilter: Entering");

    if (mDisplayWeek) {
      Date currentCalendarDate = mCalendar.getTime();

      int dayOfWeek = mCalendar.get(GregorianCalendar.DAY_OF_WEEK);
      int weekStart = mCalendar.getFirstDayOfWeek();
      Log.d(TAG, "applyFilter: first day of week" + weekStart);
      Log.d(TAG, "applyFilter: day of week is" + dayOfWeek);
      Log.d(TAG, "applyFilter: date is" + mCalendar.getTime());

      mCalendar.set(GregorianCalendar.DAY_OF_WEEK, weekStart);
      String startDate = String.format(Locale.US, "%04d-%02d-%02d",
        mCalendar.get(GregorianCalendar.YEAR),
        mCalendar.get(GregorianCalendar.MONTH) + 1,
        mCalendar.get(GregorianCalendar.DAY_OF_MONTH));

      mCalendar.add(GregorianCalendar.DAY_OF_WEEK, 6);
      String endDate = String.format(Locale.US, "%04d-%02d-%02d",
        mCalendar.get(GregorianCalendar.YEAR),
        mCalendar.get(GregorianCalendar.MONTH) + 1,
        mCalendar.get(GregorianCalendar.DAY_OF_MONTH));

      String[] selectionArgs = new String[]{startDate, endDate};
      mCalendar.setTime(currentCalendarDate);

      Log.d(TAG, "applyFilter(7): start date is " + startDate + "end date is" + endDate);
      mArgs.putString(SELECTION_PARAM, "StartDate Between ? AND ?");
      mArgs.putStringArray(SELECTION_ARGS_PARAM, selectionArgs);

    } else {
      String startDate = String.format(Locale.US, "%04d-%02d-%02d",
        mCalendar.get(GregorianCalendar.YEAR),
        mCalendar.get(GregorianCalendar.MONTH) + 1,
        mCalendar.get(GregorianCalendar.DAY_OF_MONTH));
      String[] selectionArgs = new String[]{startDate};
      Log.d(TAG, "applyFilter: start date is" + startDate);
      mArgs.putString(SELECTION_PARAM, "StartDate = ?");
      mArgs.putStringArray(SELECTION_ARGS_PARAM, selectionArgs);

    }
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    switch (id) {
      case LOADER_ID:
        String[] projection = {BaseColumns._ID,
          DurationsContract.Columns.DURATIONS_NAME,
          DurationsContract.Columns.DURATIONS_DESCRIPTION,
          DurationsContract.Columns.DURATIONS_START_TIME,
          DurationsContract.Columns.DURATIONS_START_DATE,
          DurationsContract.Columns.DURATIONS_DURATION,
          };
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;

        if (args != null) {
          selection = args.getString(SELECTION_PARAM);
          selectionArgs = args.getStringArray(SELECTION_ARGS_PARAM);
          sortOrder = args.getString(SORT_ORDER_PARAM);
        }

        return new CursorLoader(this, DurationsContract.CONTENT_URI,
          projection, selection, selectionArgs, sortOrder);

      default:
        throw new InvalidParameterException(TAG + "loader called with invalid loader id" + id);
    }
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    Log.d(TAG, "onLoadFinished: Entering onLoadFinished");
    mAdapter.swapCursor(data);
    int count = mAdapter.getItemCount();

    Log.d(TAG, "onLoadFinished: count is" + count);

  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    Log.d(TAG, "onLoaderReset: starts");
    mAdapter.swapCursor(null);

  }


}
