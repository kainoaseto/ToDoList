package co.xelabs.todo.UI;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import co.xelabs.todo.R;

public class TodoItemHolder extends RecyclerView.ViewHolder
{
    public TextView name;
    public ImageView checkMark;
    public TextView description;

    private View holder;

    public TodoItemHolder(View itemView)
    {
        super(itemView);
        checkMark       = (ImageView) itemView.findViewById(R.id.checkMark_card);
        name            = (TextView) itemView.findViewById(R.id.name_card);
        description     = (TextView) itemView.findViewById(R.id.description_card);
        holder          = itemView;
    }

    public void setOnClickListener(View.OnClickListener listener)
    {
        holder.setOnClickListener(listener);
    }

}
