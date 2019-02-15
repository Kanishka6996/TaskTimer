package com.example.kanishka.tasktimer;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

class CursorRecyclerViewAdapter extends RecyclerView.Adapter<CursorRecyclerViewAdapter.TaskViewHolder> {
  private static final String TAG = "CursorRecyclerViewAdapt";
  private Cursor mCursor;
  private OnTaskClickListner mListner;

  interface OnTaskClickListner {
    void onEditClick(@NonNull Task task);
    void onDeleteClick(@NonNull Task task);
    void onTaskLongClick(@NonNull Task task);
  }

  public CursorRecyclerViewAdapter(Cursor cursor, OnTaskClickListner listner) {
    Log.d(TAG, "CursorRecyclerViewAdapter: Constructor called");
    mCursor = cursor;
    mListner = listner;
  }


  @NonNull
  @Override
  public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    Log.d(TAG, "onCreateViewHolder: new view requested");
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tasks_list_items, parent, false);
    return new TaskViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
    Log.d(TAG, "onBindViewHolder: starts");

    if ((mCursor == null) || (mCursor.getCount() == 0)) {
      Log.d(TAG, "onBindViewHolder: providing instruction");
      holder.name.setText((R.string.instructions_heading) );
      holder.description.setText((R.string.instructions));
      holder.editButton.setVisibility(View.GONE);
      holder.deleteButton.setVisibility(View.GONE);
    } else {
      if (!mCursor.moveToPosition(position)) {
        throw new IllegalStateException("Couldn't move cursor to the position " + position);
      }

      final Task task = new Task(mCursor.getLong(mCursor.getColumnIndex(TasksContract.Columns._ID)),
                  mCursor.getString(mCursor.getColumnIndex(TasksContract.Columns.TASKS_NAME)),
                  mCursor.getString(mCursor.getColumnIndex(TasksContract.Columns.TASKS_DESCRIPTION)),
                  mCursor.getInt(mCursor.getColumnIndex(TasksContract.Columns.TASKS_SORTORDER)));

      holder.name.setText(task.getName());
      holder.description.setText(task.getDescription());
      holder.editButton.setVisibility(View.VISIBLE);
      holder.deleteButton.setVisibility(View.VISIBLE);

      View.OnClickListener buttonListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Log.d(TAG, "onClick: starts");
          switch (v.getId()) {
            case R.id.tli_edit :
              if (mListner != null) {
                mListner.onEditClick(task);
              }
              break;
            case R.id.tli_delete :
              if (mListner != null) {
                mListner.onDeleteClick(task);
              }
              break;
            default:
              Log.d(TAG, "onClick: Invalid button id");
          }
        }
      };

      View.OnLongClickListener buttonLongListner = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
          Log.d(TAG, "onLongClick: starts");
          if (mListner != null) {
            mListner.onTaskLongClick(task);
            return true;
          }
          return false;
        }
      };

      holder.editButton.setOnClickListener(buttonListner);
      holder.deleteButton.setOnClickListener(buttonListner);
      holder.itemView.setOnLongClickListener(buttonLongListner);
    }

  }

  @Override
  public int getItemCount() {
    Log.d(TAG, "getItemCount: starts");
    if ((mCursor == null) || (mCursor.getCount() == 0)) {
      return 1;
    } else {
      return mCursor.getCount();
    }
  }

  /**
   * Swap in the new cursor, returned the old cursor
   *
   * @param newCursor the new cursor to be used
   * @return returns the previously set cursor,
   * if cursor is same returns null
   */
  Cursor swapCursor(Cursor newCursor) {
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

  static class TaskViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "TaskViewHolder";

    TextView name;
    TextView description;
    ImageButton editButton;
    ImageButton deleteButton;
    View itemView;

    public TaskViewHolder(@NonNull View itemView) {
      super(itemView);
      Log.d(TAG, "TaskViewHolder: starts");

      this.name = itemView.findViewById(R.id.tli_name);
      this.description = itemView.findViewById(R.id.tli_description);
      this.editButton = itemView.findViewById(R.id.tli_edit);
      this.deleteButton = itemView.findViewById(R.id.tli_delete);
      this.itemView = itemView;
    }
  }
}
