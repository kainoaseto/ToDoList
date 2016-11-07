package me.kainoseto.todo;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

public class AnimationUtil {

    public static void checkAnimate(ImageView image, boolean fadeIn) {
        if(fadeIn) {
            Animation fadeInAnim = new AlphaAnimation(0, 1);
            fadeInAnim.setInterpolator(new DecelerateInterpolator());
            fadeInAnim.setDuration(1000);
            image.startAnimation(fadeInAnim);

        } else {
            Animation fadeOutAnim = new AlphaAnimation(1, 0);
            fadeOutAnim.setInterpolator(new AccelerateInterpolator());
            fadeOutAnim.setDuration(1000);
            image.startAnimation(fadeOutAnim);
        }

    }

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
