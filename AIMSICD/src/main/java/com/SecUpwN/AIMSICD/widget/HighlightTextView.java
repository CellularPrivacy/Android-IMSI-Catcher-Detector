package com.SecUpwN.AIMSICD.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.SecUpwN.AIMSICD.R;
import com.kaichunlin.transition.ViewTransitionBuilder;
import com.kaichunlin.transition.animation.AnimationManager;
import com.kaichunlin.transition.internal.TransitionController;
import com.nineoldandroids.animation.ArgbEvaluator;

/**
 * Highlight the updated text
 */
public class HighlightTextView extends TextView {
    private static final int DURATION = 3000;

    public HighlightTextView(Context context) {
        super(context);
    }

    public HighlightTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HighlightTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void updateText(CharSequence text) {
        AnimationManager animationManager=new AnimationManager();
        updateText(text, animationManager);
        animationManager.startAnimation(DURATION);
    }

    public void updateText(CharSequence text, AnimationManager animationManager) {
        String orgText = getText().toString();
        if (orgText.equals(text)) {
            setBackgroundColor(0x00000000);
        } else {
            final int currentColor = getCurrentTextColor();
            ViewTransitionBuilder.transit(this).addTransitionHandler(new ScaledTransitionHandler() {
                ArgbEvaluator argbEvaluator=new ArgbEvaluator();

                @Override
                protected void onUpdateScaledProgress(TransitionController transitionController, View view, float modifiedProgress) {
                    setTextColor((Integer) argbEvaluator.evaluate(modifiedProgress, currentColor, getResources().getColor(R.color.red_text)));
                }
            }).range(0f, 0.2f).buildAnimationFor(animationManager);
            ViewTransitionBuilder.transit(this).addTransitionHandler(new ScaledTransitionHandler() {
                ArgbEvaluator argbEvaluator=new ArgbEvaluator();

                @Override
                protected void onUpdateScaledProgress(TransitionController transitionController, View view, float modifiedProgress) {
                    setTextColor((Integer) argbEvaluator.evaluate(modifiedProgress, getResources().getColor(R.color.red_text), getResources().getColor(R.color.medium_blue)));
                }
            }).range(0.2f, 1f).buildAnimationFor(animationManager);
        }
        setText(text);
    }
}