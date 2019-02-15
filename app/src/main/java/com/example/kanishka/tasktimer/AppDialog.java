package com.example.kanishka.tasktimer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AppDialog extends DialogFragment {
  private static final String TAG = "AppDialog";

  public static final String DIALOG_ID = "id";
  public static final String DIALOG_MESSAGE = "message";
  public static final String DIALOG_POSITIVE_RID = "positive_rid";
  public static final String DIALOG_NEGATIVE_RID = "negative_rid";

  interface DialogEvents {
    void onPositiveDialogResult(int dialogId, Bundle args);
    void onNegativeDialogResult(int dialogId, Bundle args);
    void onDialogCancelled(int dialogId);
  }
  
  private DialogEvents mDialogEvents;

  @Override
  public void onAttach(Context context) {
    Log.d(TAG, "onAttach: Entering onAttach, activity is: " + context.toString());
    super.onAttach(context);
    //implements callback
    if (!(context instanceof DialogEvents)) {
      throw new ClassCastException(context.toString() + "must inplement AppDialog.DialogEvents interface");
    }

    mDialogEvents = (DialogEvents) context;
  }

  @Override
  public void onDetach() {
    Log.d(TAG, "onDetach: starts");
    super.onDetach();

    //resets the callback
    mDialogEvents = null;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    Log.d(TAG, "onCreateDialog: starts");

    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

    final Bundle arguments = getArguments();
    final int dialogId;
    String messageString;
    int positiveStringId;
    int negativeStringId;

    if (arguments != null) {
      dialogId = arguments.getInt(DIALOG_ID);
      messageString = arguments.getString(DIALOG_MESSAGE);

      if (dialogId == 0 || messageString == null) {
        throw new IllegalArgumentException("Dialog id and/or Dialog message is empty");
      }
      positiveStringId = arguments.getInt(DIALOG_POSITIVE_RID);
      if (positiveStringId == 0) {
        positiveStringId = R.string.ok;
      }
      negativeStringId = arguments.getInt(DIALOG_NEGATIVE_RID);
      if (negativeStringId == 0) {
        negativeStringId = R.string.cancel;
      }
    } else {
      throw new IllegalArgumentException("Must pass DIAlOG_ID and DIALOG_MESSAGE in the bundle");
    }

    builder.setMessage(messageString)
        .setPositiveButton(positiveStringId, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            //callback positive result
            if (mDialogEvents != null) {
              mDialogEvents.onPositiveDialogResult(dialogId, arguments);
            }
          }
        })
        .setNegativeButton(negativeStringId, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            //callback negetive result
            if (mDialogEvents != null) {
              mDialogEvents.onNegativeDialogResult(dialogId, arguments);
            }
          }
        });

    return builder.create();
  }

  @Override
  public void onCancel(DialogInterface dialog) {
    Log.d(TAG, "onCancel: called");
    if (mDialogEvents != null) {
      int dialogId = getArguments().getInt(DIALOG_ID);
      mDialogEvents.onDialogCancelled(dialogId);
    }
  }

  //for logging only

  @Override
  public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
    Log.d(TAG, "onInflate: called");
    super.onInflate(context, attrs, savedInstanceState);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    Log.d(TAG, "onCreate: called");
    super.onCreate(savedInstanceState);
  }

  @Override
  public void onHiddenChanged(boolean hidden) {
    Log.d(TAG, "onHiddenChanged: called");
    super.onHiddenChanged(hidden);
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
    Log.d(TAG, "onCreateView: called");
    return super.onCreateView(inflater, container, savedInstanceState);
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    Log.d(TAG, "onViewCreated: called");
    super.onViewCreated(view, savedInstanceState);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    Log.d(TAG, "onActivityCreated: called");
    super.onActivityCreated(savedInstanceState);
  }

  @Override
  public void onViewStateRestored(Bundle savedInstanceState) {
    Log.d(TAG, "onViewStateRestored: called");
    super.onViewStateRestored(savedInstanceState);
  }

  @Override
  public void onStart() {
    Log.d(TAG, "onStart: called");
    super.onStart();
  }

  @Override
  public void onResume() {
    Log.d(TAG, "onResume: called");
    super.onResume();
  }

  @Override
  public void onPause() {
    Log.d(TAG, "onPause: called");
    super.onPause();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    Log.d(TAG, "onSaveInstanceState: called");
    super.onSaveInstanceState(outState);
  }

  @Override
  public void onStop() {
    Log.d(TAG, "onStop: called");
    super.onStop();
  }

  @Override
  public void onDestroyView() {
    Log.d(TAG, "onDestroyView: called");
    super.onDestroyView();
  }

  @Override
  public void onDestroy() {
    Log.d(TAG, "onDestroy: called");
    super.onDestroy();
  }

}
