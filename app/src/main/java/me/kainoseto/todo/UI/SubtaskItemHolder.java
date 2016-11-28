package me.kainoseto.todo.UI;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import me.kainoseto.todo.R;

/**
 * Created by tylerdomitrovich on 11/25/16.
 */

public class SubtaskItemHolder extends RecyclerView.ViewHolder {

    public CheckBox checkMark;
    public TextView name;

    private View holder;

    public SubtaskItemHolder(View itemView) {
        super(itemView);
        checkMark = (CheckBox) itemView.findViewById(R.id.checkMark_subtask);
        name = (TextView) itemView.findViewById(R.id.name_subtask);

        holder = itemView;
    }
}
