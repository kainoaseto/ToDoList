package me.kainoseto.todo;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class TodoItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
    public TextView name;
    public ImageView checkMark;
    public TextView description;
    private ItemClickListener itemClickListener;

    public TodoItemHolder(View itemView) {
        super(itemView);
        checkMark = (ImageView) itemView.findViewById(R.id.checkMark_card);
        name = (TextView) itemView.findViewById(R.id.name_card);
        description = (TextView) itemView.findViewById(R.id.description_card);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        this.itemClickListener.onItemClick(v, getLayoutPosition());
    }

    // TODO: Get this working
    @Override
    public boolean onLongClick(View v) {
        this.itemClickListener.onLongItemClick(v, getLayoutPosition());
        return true;
    }

    public void setItemClickListener(ItemClickListener listener) {
        this.itemClickListener = listener;
    }


}
