package co.xelabs.todo.UI;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import co.xelabs.todo.Callback.ItemTouchHelperAdapter;
import co.xelabs.todo.Content.TodoContentManager;
import co.xelabs.todo.Content.TodoItem;
import co.xelabs.todo.Database.ContentManager;
import co.xelabs.todo.ItemDetailActivity;
import co.xelabs.todo.MainActivity;
import co.xelabs.todo.R;

public class TodoListAdapter extends RecyclerView.Adapter<TodoItemHolder> implements ItemTouchHelperAdapter
{
    private static final String LOG_TAG = TodoListAdapter.class.getSimpleName();

    private Context context;
    private LayoutInflater layoutInflater;

    private int lastIndex;
    private ContentManager contentManager;

    public TodoListAdapter(Context context)
    {
        this.context = context;

        layoutInflater = LayoutInflater.from(context);
        contentManager = TodoContentManager.getInstance();
    }

    @Override
    public TodoItemHolder onCreateViewHolder(ViewGroup parent, int position)
    {
        View view = layoutInflater.inflate(R.layout.todo_card, parent, false);
       return new TodoItemHolder(view);
    }

    @Override
    public void onBindViewHolder(TodoItemHolder holder, final int uiIdx)
    {
        final TodoItem currentItem = contentManager.getTodoItem(uiIdx);
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

        if (uiIdx > lastIndex) {
            AnimationUtil.animate(holder, true);
        } else {
            AnimationUtil.animate(holder, false);
        }
        lastIndex = uiIdx;

        holder.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent detailViewIntent = new Intent(context, ItemDetailActivity.class);

                detailViewIntent.putExtra(context.getString(R.string.intent_idx), currentItem.getUiIdx());
                context.startActivity(detailViewIntent);
                ((MainActivity)context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);;
            }
        });

        holder.checkMark.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                boolean isDone = currentItem.isDone();
                contentManager.setDone(currentItem.getUiIdx(), !isDone);
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

    //For swipe to delete and drag to move
    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if(fromPosition < toPosition){
            for (int i = fromPosition; i < toPosition; i++) {
                contentManager.swapUiIdx(i, i+1);
            }
        }else{
            for (int i = fromPosition; i < toPosition; i++) {
                contentManager.swapUiIdx(i, i-1);
            }
        }

        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        contentManager.removeTodoItem(position);
        notifyItemRemoved(position);
    }
}
