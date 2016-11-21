package me.kainoseto.todo.UI;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

import me.kainoseto.todo.AnimationUtil;
import me.kainoseto.todo.Database.TodoDatabaseHandler;
import me.kainoseto.todo.ItemClickListener;
import me.kainoseto.todo.ItemDetailActivity;
import me.kainoseto.todo.MainActivity;
import me.kainoseto.todo.R;
import me.kainoseto.todo.TodoContentManager;

public class TodoListAdapter extends RecyclerView.Adapter<TodoItemHolder>
{
    private static final String LOG_TAG = TodoListAdapter.class.getSimpleName();

    private Context context;
    private LayoutInflater layoutInflater;

    private int lastPosition;
    private TodoContentManager contentManager;
    private TodoDatabaseHandler databaseHandler;

    public TodoListAdapter(Context context)
    {
        this.context = context;

        layoutInflater = LayoutInflater.from(context);
        contentManager = TodoContentManager.getInstance();
        databaseHandler = contentManager.getDatabaseHandler();
    }

    @Override
    public TodoItemHolder onCreateViewHolder(ViewGroup parent, int position)
    {
        View view = layoutInflater.inflate(R.layout.todo_card, parent, false);
       return new TodoItemHolder(view);
    }

    @Override
    public void onBindViewHolder(TodoItemHolder holder, final int position)
    {
        final TodoItem currentItem = contentManager.getTodoItem(position);
        String fullName = currentItem.getName();
        String fullDesc = currentItem.getDescription();

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
         If the description is too long or is a block description add '...' to the end so it
         doesn't mess up the title or any other views
         */
        int len = 30;
        if(fullDesc.length() < 30) {
            len = fullDesc.length();
        }
        for (int i = 0; i < len; i++)
        {
            if(fullDesc.charAt(i) == '\n')
            {
                fullDesc = fullDesc.substring(0, i-1);
                fullDesc += "...";
                break;
            }
        }

        if(fullDesc.length() > 30)
        {
            fullDesc = fullDesc.substring(0, 30);
            fullDesc += "...";
        }

        holder.description.setText(fullDesc);

        if(currentItem.isDone())
            holder.checkMark.setImageResource(R.drawable.ic_check_circle_white_48dp);

        if (position > lastPosition) {
            AnimationUtil.animate(holder, true);
        } else {
            AnimationUtil.animate(holder, false);
        }
        lastPosition = position;

        holder.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent detailViewIntent = new Intent(context, ItemDetailActivity.class);
                detailViewIntent.putExtra(context.getString(R.string.intent_idx), currentItem.getIdx());
                context.startActivity(detailViewIntent);
            }
        });

        holder.checkMark.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                boolean isDone = currentItem.isDone();
                databaseHandler.updateDone(currentItem.getIdx(), !isDone);
                currentItem.setDone(!isDone);

                AnimationUtil.checkAnimate((ImageView) v, true);
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
        return contentManager.getSize();
    }
}
