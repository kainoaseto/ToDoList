package me.kainoseto.todo;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Kainoa on 10/14/16.
 */

public class TodoListAdapter extends RecyclerView.Adapter<TodoItemHolder> {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private Context context;
    private ArrayList<TodoItem> todoItems;
    private LayoutInflater layoutInflater;

    private int idx;
    private String fullName;
    private String fullDesc;
    private int lastPosition;

    public TodoListAdapter(Context context, ArrayList<TodoItem> data) {
        this.context = context;
        this.todoItems = data;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public TodoItemHolder onCreateViewHolder(ViewGroup parent, int position) {
        View view = layoutInflater.inflate(R.layout.todo_card, parent, false);
       return new TodoItemHolder(view);
    }

    @Override
    public void onBindViewHolder(TodoItemHolder holder, final int position) {
        fullName = todoItems.get(position).getName();
        if(fullName.length() > 15) {
            fullName = fullName.substring(0, 15);
            fullName += "...";
        }
        holder.name.setText(fullName);
        fullDesc = todoItems.get(position).getDescription();
        for (int i = 0; i < 30; i++) {
            if(fullDesc.charAt(i) == '\n') {
                fullDesc = fullDesc.substring(0, i-1);
                fullDesc += "...";
                break;
            }
        }

        if(fullDesc.length() > 30) {
            fullDesc = fullDesc.substring(0, 30);
            fullDesc += "...";
        }

        holder.description.setText(fullDesc);

        if(todoItems.get(position).isDone()) {
            holder.checkMark.setImageResource(R.drawable.ic_check_circle_white_48dp);
        }

        if (position > lastPosition) {
            AnimationUtil.animate(holder, true);
        } else {
            AnimationUtil.animate(holder, false);
        }

        lastPosition = position;
        final int currentPosition = position;
        final TodoItem currentItem = todoItems.get(currentPosition);

        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(View view, int pos) {
                Intent detailViewIntent = new Intent(context, ItemDetailActivity.class);
                detailViewIntent.putExtra("NAME", currentItem.getName());
                detailViewIntent.putExtra("DESC", currentItem.getDescription());
                detailViewIntent.putExtra("DONE", currentItem.isDone());
                detailViewIntent.putExtra("POSITION", currentItem.getIdx());

                context.startActivity(detailViewIntent);
            }

            // TODO: Get this working
            @Override
            public boolean onLongItemClick(View view, int pos) {
                Log.d(LOG_TAG, "OnLongItemClick");
                notifyItemRemoved(pos);
                return true;
            }
        });

        holder.checkMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isDone = currentItem.isDone();
                MainActivity.todoDbHelper.updateDone(currentItem.getIdx(), !isDone);
                currentItem.setDone(!currentItem.isDone());
                if(isDone) {
                    ((ImageView) v).setImageResource(R.drawable.ic_remove_circle_outline_white_48dp);
                } else {
                    ((ImageView) v).setImageResource(R.drawable.ic_check_circle_white_48dp);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return todoItems.size();
    }

    public void notifyNewItem(int position, TodoItem item) {
        todoItems.add(position, item);
        notifyItemInserted(position);
    }

    public void notifyRemoveItem(int position) {
        todoItems.remove(position);
        notifyItemRemoved(position);
    }
}
