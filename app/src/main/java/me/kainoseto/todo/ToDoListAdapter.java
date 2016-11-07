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

public class TodoListAdapter extends RecyclerView.Adapter<TodoItemHolder> {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private Context context;
    private ArrayList<TodoItem> todoItems;
    private LayoutInflater layoutInflater;

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

        /*
         If the name is longer than 15 chars then add '...' to the end so it doesn't mess up
         the views and go off the screen
          */
        if(fullName.length() > 15) {
            fullName = fullName.substring(0, 15);
            fullName += "...";
        }
        holder.name.setText(fullName);

        /*
         If the decsiption is too long or is a block description add '...' to the end so it
         doesn't mess up the title or any other views
         */
        fullDesc = todoItems.get(position).getDescription();
        int len = 30;
        if(fullDesc.length() < 30) {
            len = fullDesc.length();
        }
        for (int i = 0; i < len; i++) {
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
                Log.d(LOG_TAG, "Pos: " + pos + " position: " + position + " currentPosition: " + currentPosition);
                Intent detailViewIntent = new Intent(context, ItemDetailActivity.class);
                detailViewIntent.putExtra("NAME", currentItem.getName());
                detailViewIntent.putExtra("DESC", currentItem.getDescription());
                detailViewIntent.putExtra("DONE", currentItem.isDone());
                detailViewIntent.putExtra("POSITION", currentItem.getIdx());

                context.startActivity(detailViewIntent);
            }
        });

        holder.checkMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isDone = currentItem.isDone();
                MainActivity.todoDbHelper.updateDone(currentItem.getIdx(), !isDone);
                currentItem.setDone(!currentItem.isDone());
                if(isDone) {
                    AnimationUtil.checkAnimate((ImageView) v, true);
                    ((ImageView) v).setImageResource(R.drawable.ic_remove_circle_outline_white_48dp);


                } else {
                    AnimationUtil.checkAnimate((ImageView) v, true);
                    ((ImageView) v).setImageResource(R.drawable.ic_check_circle_white_48dp);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return todoItems.size();
    }
}
