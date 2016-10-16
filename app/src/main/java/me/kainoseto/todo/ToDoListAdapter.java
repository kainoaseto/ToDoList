package me.kainoseto.todo;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Kainoa on 10/14/16.
 */

public class TodoListAdapter extends RecyclerView.Adapter<TodoItemHolder> {
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
        holder.name.setText(todoItems.get(position).getName());
        holder.checkMark.setImageResource(todoItems.get(position).getImageId());

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
                Intent detailViewIntent = new Intent(context, EditItemDetailActivity.class);
                detailViewIntent.putExtra("POSITION", currentPosition);

                context.startActivity(detailViewIntent);
            }
        });

        holder.checkMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
