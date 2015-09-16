package com.SecUpwN.AIMSICD.widget;

import android.view.View;

import com.kaichunlin.transition.TransitionHandler;
import com.kaichunlin.transition.internal.TransitionController;

/**
 * The progress valued passed to @link {@link #onUpdateScaledProgress(TransitionController, View, float)} has taken the start and
 * end range into consideration. The method will only be called when the progress is within range, and the value passed has been
 * scaled to always be [0..1].
 * <p>
 * Created by Kai-Chun Lin on 2015/9/16.
 */
public abstract class ScaledTransitionHandler implements TransitionHandler {
    private final boolean updateOnceOutsideRange;
    private boolean updateMinProgress = true;
    private boolean updateMaxProgress = true;

    public ScaledTransitionHandler() {
        this(false);
    }

    public ScaledTransitionHandler(boolean updateOnceOutsideRange) {
        this.updateOnceOutsideRange = updateOnceOutsideRange;
    }

    @Override
    public final void onUpdateProgress(TransitionController controller, View target, float progress) {
        final float start = controller.getStart();
        final float end = controller.getEnd();
        if (progress < start) {
            if (updateOnceOutsideRange && updateMinProgress) {
                onUpdateScaledProgress(controller, target, 0);
            }
            updateMinProgress = false;
            return;
        }
        updateMinProgress = true;
        if (progress > end) {
            if (updateOnceOutsideRange && updateMaxProgress) {
                onUpdateScaledProgress(controller, target, 1);
            }
            updateMaxProgress = false;
            return;
        }
        updateMaxProgress = true;
        onUpdateScaledProgress(controller, target, (progress - start) / (end - start));
    }

    protected abstract void onUpdateScaledProgress(TransitionController controller, View target, float modifiedProgress);
}
