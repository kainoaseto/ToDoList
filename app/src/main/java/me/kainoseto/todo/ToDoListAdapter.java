package me.kainoseto.todo;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Kainoa on 10/14/16.
 */

public class TodoListAdapter extends RecyclerView.Adapter<TodoItemHolder> {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private Context context;
    private ArrayList<TodoItem> todoItems;
    private LayoutInflater layoutInflater;

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
        String fullName = todoItems.get(position).getName();
        if(fullName.length() > 15) {
            fullName = fullName.substring(0, 15);
            fullName += "...";
        }
        holder.name.setText(fullName);
        String fullDesc = todoItems.get(position).getDescription();
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
                detailViewIntent.putExtra("POSITION", currentPosition);

                context.startActivity(detailViewIntent);
            }

            @Override
            public boolean onLongItemClick(View view, int pos) {
                Log.d(LOG_TAG, "OnLongItemClick");
                notifyItemRemoved(pos);
                return true;
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
