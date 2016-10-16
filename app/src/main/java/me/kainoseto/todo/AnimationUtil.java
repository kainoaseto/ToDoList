package me.kainoseto.todo;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.v7.widget.RecyclerView;

/**
 * Created by Kainoa on 10/15/2016.
 */

public class AnimationUtil {
    public static void animate(RecyclerView.ViewHolder holder, boolean down) {
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator animatorTranslateY = ObjectAnimator.ofFloat(holder.itemView, "translationY", down ? 200 : -200, 0);
        animatorTranslateY.setDuration(1000);

        ObjectAnimator animatorTranslateX = ObjectAnimator.ofFloat(holder.itemView, "translationX", -50, 50, -30, 30, -20, 20, -5, 5, 0);
        animatorTranslateX.setDuration(1000);

        animatorSet.playTogether(animatorTranslateX, animatorTranslateY);

        animatorSet.start();
    }
}
