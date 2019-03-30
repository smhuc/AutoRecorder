package com.jeejio.autorecorder.utils;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public class AnimalUtils {
    private static AnimalUtils animalUtils;
    private Context mContext;
    private ImageView imageView;
    private AnimationDrawable anim;

    public static AnimalUtils init(Context context, ImageView imageView) {
        if (animalUtils == null) animalUtils = new AnimalUtils(context, imageView);
        return animalUtils;
    }

    public AnimalUtils(Context context, ImageView imageView) {
        this.mContext = context;
        this.imageView = imageView;
        anim = new AnimationDrawable();
    }

    public void startAnimals() {
        for (int i = 0; i < 4; i++) {
            int id = mContext.getResources().getIdentifier("play" + i, "mipmap", mContext.getPackageName());
            Drawable drawable = mContext.getResources().getDrawable(id);
            anim.addFrame(drawable, 200);
        }
        anim.setOneShot(false);
        imageView.setImageDrawable(anim);
        anim.start();
    }

    public void stopAnimals() {
        if (anim != null && anim.isRunning()) {
            anim.stop();
        }
    }
}
