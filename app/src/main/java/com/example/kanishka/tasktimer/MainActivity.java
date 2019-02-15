package com.example.kanishka.tasktimer;

import android.annotation.SuppressLint;

import android.content.Intent;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.example.kanishka.tasktimer.TestData;


public class MainActivity extends AppCompatActivity implements CursorRecyclerViewAdapter.OnTaskClickListner,
  AddEditActivityFragment.OnSaveClicked,
  AppDialog.DialogEvents {
  private static final String TAG = "MainActivity";

  private boolean mTwoPane = false;
  public static final int DIALOG_ID_DELETE = 1;
  public static final int DIALOG_ID_CANCEL_EDIT = 2;
  public static final int DIALOG_ID_CANCEL_EDIT_UP = 3;
  public static final String CHANNEL_ID = "Notification";

  private AlertDialog mDialog = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

//    if((findViewById(R.id.task_details_container)) != null ) {
//      mTwoPane = true;
//    }
    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
      mTwoPane = true;
    }
    Log.d(TAG, "onCreate: two pane is :" + mTwoPane);

    FragmentManager fragmentManager = getSupportFragmentManager();
    Boolean editing = fragmentManager.findFragmentById(R.id.task_details_container) != null;
    Log.d(TAG, "onCreate: editing is :" + editing);

    //we need references to the containers,so we can hide/show them as per necessary, we will create views here
    View addEditLayout = findViewById(R.id.task_details_container);
    View mainFragment = findViewById(R.id.fragment);

    if (mTwoPane) {
      Log.d(TAG, "onCreate: twoPane mode");
      mainFragment.setVisibility(View.VISIBLE);
      addEditLayout.setVisibility(View.VISIBLE);
    } else if (editing) {
      Log.d(TAG, "onCreate: single pane, editing");
      mainFragment.setVisibility(View.GONE);
    } else {
      Log.d(TAG, "onCreate: single pane, not editing");
      mainFragment.setVisibility(View.VISIBLE);
      addEditLayout.setVisibility(View.GONE);
    }

  }

  @Override
  public void onSaveClicked() {
    Log.d(TAG, "onSaveClicked: starts");

    FragmentManager fragmentManager = getSupportFragmentManager();
    Fragment fragment = fragmentManager.findFragmentById(R.id.task_details_container);
    if (fragment != null) {
      getSupportFragmentManager().beginTransaction().remove(fragment).commit();
//      FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//      fragmentTransaction.remove(fragment);
//      fragmentTransaction.commit();
    }

    View addEditLayout = findViewById(R.id.task_details_container);
    View mainFragment = findViewById(R.id.fragment);

    if (!mTwoPane) {
      addEditLayout.setVisibility(View.GONE);
      mainFragment.setVisibility(View.VISIBLE);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);

    if (BuildConfig.DEBUG) {
      MenuItem generate = menu.findItem(R.id.menumain_generate);
      generate.setVisible(true);
    }
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    switch (id) {
      case R.id.menumain_addTask:
        taskEditRequest(null);
        break;

      case R.id.menumain_generate:
        TestData.generateTestData(getContentResolver());
        break;

      case R.id.menumain_settings:
        break;

      case R.id.menumain_showAbout:
        showAboutDialog();
        break;

      case R.id.menumain_showDurations:
        startActivity(new Intent(this, DurationsReport.class));
        break;

      case android.R.id.home:
        Log.d(TAG, "onOptionsItemSelected: home button pressed");
        AddEditActivityFragment fragment = (AddEditActivityFragment)
          getSupportFragmentManager().findFragmentById(R.id.task_details_container);
        if (fragment.canClose()) {
          return super.onOptionsItemSelected(item);
        } else {
          showConfirmationDialog(DIALOG_ID_CANCEL_EDIT_UP);
          return true;
        }
    }

    return super.onOptionsItemSelected(item);
  }

  public void showAboutDialog() {
    @SuppressLint("InflateParams") View messageView = getLayoutInflater().inflate(R.layout.about, null, false);
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.app_name);
    builder.setIcon(R.mipmap.ic_launcher);
    builder.setView(messageView);

    mDialog = builder.create();
    mDialog.setCanceledOnTouchOutside(true);


    TextView tv = (TextView) messageView.findViewById(R.id.about_version);
    tv.setText("v" + BuildConfig.VERSION_NAME);

    mDialog.show();
  }


  @Override
  public void onEditClick(@NonNull Task task) {
    taskEditRequest(task);
  }

  @Override
  public void onDeleteClick(@NonNull Task task) {
    Log.d(TAG, "onDeleteClick: starts");

    AppDialog dialog = new AppDialog();
    Bundle args = new Bundle();
    args.putInt(AppDialog.DIALOG_ID, DIALOG_ID_DELETE);
    args.putString(AppDialog.DIALOG_MESSAGE, getString(R.string.deldiag_message, task.getId(), task.getName()));
    args.putInt(AppDialog.DIALOG_POSITIVE_RID, R.string.deldiag_positive_caption);
    args.putLong("TaskId", task.getId());
    dialog.setArguments(args);
    dialog.show(getSupportFragmentManager(), null);

  }

  private void taskEditRequest(Task task) {
    Log.d(TAG, "taskEditRequest: starts");

    Log.d(TAG, "taskEditRequest: in two-pane mode (tablet)");
    AddEditActivityFragment fragment = new AddEditActivityFragment();

    Bundle arguments = new Bundle();
    arguments.putSerializable(Task.class.getSimpleName(), task);
    fragment.setArguments(arguments);

    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    fragmentTransaction.replace(R.id.task_details_container, fragment);
    fragmentTransaction.commit();

    if (!mTwoPane) {
      Log.d(TAG, "taskEditRequest: in single-pane mode");
      View addEditLayout = findViewById(R.id.task_details_container);
      View mainFragment = findViewById(R.id.fragment);

      mainFragment.setVisibility(View.GONE);
      addEditLayout.setVisibility(View.VISIBLE);
    }
  }

  @Override
  public void onPositiveDialogResult(int dialogId, Bundle args) {
    Log.d(TAG, "onPositiveDialogResult: starts");
    switch (dialogId) {
      case DIALOG_ID_DELETE:
        long taskId = args.getLong("TaskId");
        if (BuildConfig.DEBUG && taskId == 0) throw new AssertionError("Task id is Zero");
        getContentResolver().delete(TasksContract.buildTaskUri(taskId), null, null);
        break;
      case DIALOG_ID_CANCEL_EDIT:
      case DIALOG_ID_CANCEL_EDIT_UP:
        //nothing needed
        break;
    }

  }

  @Override
  public void onNegativeDialogResult(int dialogId, Bundle args) {
    Log.d(TAG, "onNegativeDialogResult: starts");
    switch (dialogId) {
      case DIALOG_ID_DELETE:
        break;
      case DIALOG_ID_CANCEL_EDIT:
      case DIALOG_ID_CANCEL_EDIT_UP:
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.task_details_container);
        if (fragment != null) {
          getSupportFragmentManager().beginTransaction()
            .remove(fragment)
            .commit();
          if (mTwoPane) {
            if (dialogId == DIALOG_ID_CANCEL_EDIT) {
              finish();
            }
          } else {
            View addEditLayout = findViewById(R.id.task_details_container);
            View mainFragment = findViewById(R.id.fragment);

            //remove addedit and show main fragment only
            addEditLayout.setVisibility(View.GONE);
            mainFragment.setVisibility(View.VISIBLE);
          }
        } else {
          finish();
        }
        break;
    }
  }

  @Override
  public void onDialogCancelled(int dialogId) {
    Log.d(TAG, "onDialogCancelled: starts");
  }

  @Override
  public void onBackPressed() {
    Log.d(TAG, "onBackPressed: starts");

    FragmentManager fragmentManager = getSupportFragmentManager();
    AddEditActivityFragment fragment = (AddEditActivityFragment) fragmentManager.findFragmentById(R.id.task_details_container);
    if ((fragment == null) || fragment.canClose()) {
      super.onBackPressed();
    } else {

      showConfirmationDialog(DIALOG_ID_CANCEL_EDIT);
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (mDialog != null && mDialog.isShowing()) {
      mDialog.dismiss();
    }
  }

  private void showConfirmationDialog(int dialogId) {
    AppDialog dialog = new AppDialog();
    Bundle args = new Bundle();
    args.putInt(AppDialog.DIALOG_ID, dialogId);
    args.putString(AppDialog.DIALOG_MESSAGE, getString(R.string.cancelEditDiag_message));
    args.putInt(AppDialog.DIALOG_POSITIVE_RID, R.string.cancleEditDiag_positive_caption);
    args.putInt(AppDialog.DIALOG_NEGATIVE_RID, R.string.cancleEditDiag_negetive_caption);

    dialog.setArguments(args);
    dialog.show(getSupportFragmentManager(), null);
  }

  @Override
  public void onTaskLongClick(@NonNull Task task) {
    //Required to satisfy the interface
  }

}
