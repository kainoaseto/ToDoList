package me.kainoseto.todo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import static android.R.attr.data;

/**
 * Created by Kainoa on 10/14/16.
 */

public class ToDoListAdapter extends RecyclerView.Adapter<ToDoItemHolder> {
    private Context context;
    private ArrayList<ToDoItem> toDoItems;
    private LayoutInflater layoutInflater;

    private int lastPosition;

    public ToDoListAdapter(Context context, ArrayList<ToDoItem> data) {
        this.context = context;
        this.toDoItems = data;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public ToDoItemHolder onCreateViewolder(ViewGroup parent, int position) {
        View view = layoutInflater.inflate(R.layout.todo_card, parent, false);
        ToDoItemHolder holder = new ToDoItemHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(ToDoItemHolder holder, final int position) {
        holder.checkMark.setImageResource();
        holder.name.setText(data.get(position).title);
    }
}
