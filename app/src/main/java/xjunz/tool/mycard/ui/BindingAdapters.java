/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.text.InputFilter;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.databinding.InverseBindingListener;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class BindingAdapters {
    @BindingAdapter("android:maxTextCount")
    public static void setMaxTextCount(@NonNull EditText et, int maxCount) {
        et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxCount)});
    }

    @BindingAdapter("android:tooltip")
    public static void setTiptop(View view, CharSequence tooltip) {
        ViewCompat.setTooltipText(view, tooltip);
        view.setContentDescription(tooltip);
    }

    @BindingAdapter("android:pop")
    public static void setPop(@NonNull View view, Boolean oldValue, Boolean visible) {
        if (oldValue == null) {
            return;
        }
        if (oldValue == visible) {
            return;
        }
        if (visible) {
            view.setScaleY(0);
            view.setScaleX(0);
            view.setVisibility(View.VISIBLE);
            view.animate().scaleX(1).scaleY(1).setListener(null).setInterpolator(new OvershootInterpolator()).start();
        } else {
            view.animate().scaleX(0).scaleY(0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.INVISIBLE);
                }
            }).setInterpolator(new AnticipateInterpolator()).start();
        }
    }

    @BindingAdapter("android:semiFade")
    public static void setSemiFade(@NonNull View view, Boolean oldValue, Boolean visible) {
        if (oldValue == null) {
            return;
        }
        if (oldValue == visible) {
            return;
        }
        if (visible) {
            view.setAlpha(0);
            view.setVisibility(View.VISIBLE);
            view.animate().alpha(0.8f).setListener(null).start();
        } else {
            view.animate().alpha(0f).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.INVISIBLE);
                }
            }).start();
        }
    }

    @BindingAdapter("android:fade")
    public static void setFade(@NonNull View view, Boolean oldValue, Boolean visible) {
        if (oldValue == null) {
            return;
        }
        if (oldValue == visible) {
            return;
        }
        if (visible) {
            view.setAlpha(0);
            view.setVisibility(View.VISIBLE);
            view.animate().alpha(1f).setListener(null).start();
        } else {
            view.animate().alpha(0f).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.INVISIBLE);
                }
            }).start();
        }
    }

    @BindingAdapter("android:gone")
    public static void setGone(@NonNull View view, boolean gone) {
        view.setVisibility(gone ? View.GONE : View.VISIBLE);
    }

    @BindingAdapter("android:visible")
    public static void setVisible(@NonNull View view, boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @BindingAdapter("android:invisible")
    public static void setInvisible(@NonNull View view, boolean invisible) {
        view.setVisibility(invisible ? View.INVISIBLE : View.VISIBLE);
    }

    @BindingAdapter("android:widthWrap")
    public static void setWrapWidth(@NonNull View view, Boolean oldValue, Boolean wrap) {
        if (oldValue != null) {
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            if (wrap) {
                lp.width = WRAP_CONTENT;
            } else {
                lp.width = MATCH_PARENT;
            }
            view.setLayoutParams(lp);
        }
    }

    @InverseBindingAdapter(attribute = "android:height")
    public static int getHeight(@NonNull View view) {
        return view.getHeight();
    }

    @BindingAdapter(value = {"android:heightAttrChanged"})
    public static void setHeightWatcher(@NonNull View view, final InverseBindingListener listener) {
        View.OnLayoutChangeListener newValue = new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (top != oldTop || bottom != oldBottom) {
                    listener.onChange();
                    view.removeOnLayoutChangeListener(this);
                }
            }
        };
        view.addOnLayoutChangeListener(newValue);
    }
}
