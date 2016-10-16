package me.kainoseto.todo;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ToDoItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView name;
    public ImageView checkMark;
    private ItemClickListener itemClickListener;

    public ToDoItemHolder(View itemView) {
        super(itemView);
        checkMark = (ImageView) itemView.findViewById(R.id.checkMark);
        name = (TextView) itemView.findViewById(R.id.name);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        this.itemClickListener.onItemClick(v, getLayoutPosition());
    }

    public void setItemClickListener(ItemClickListener listener) {
        this.itemClickListener = listener;
    }


}
