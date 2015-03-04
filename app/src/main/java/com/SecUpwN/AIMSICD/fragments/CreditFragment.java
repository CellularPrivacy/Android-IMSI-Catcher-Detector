package com.SecUpwN.AIMSICD.fragments;

/**
 * Created by kai on 2015-03-03
 */
/*
* Copyright 2014 Frakbot (Sebastiano Poggi and Francesco Pontillo)
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

/*
* Copyright 2014 Frakbot (Sebastiano Poggi and Francesco Pontillo)
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import com.SecUpwN.AIMSICD.R;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import android.widget.SeekBar;

import net.frakbot.creditsroll.CreditsRollView;

import static com.SecUpwN.AIMSICD.R.layout;

public class CreditFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {
    private static final float SCROLL_ANIM_DURATION = 30000; // [ms] = 30 s
    private CreditsRollView mCreditsRollView;
    private Context mContext;
    private boolean mScrolling;
    private SeekBar mSeekBar;
    private ValueAnimator mScrollAnimator;
    private Activity mActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(layout.credits_fragment, container, false);

        if (v != null) {
            //mSeekBar = (SeekBar) mActivity.findViewById(R.id.seekbar);
            //mSeekBar.setOnSeekBarChangeListener(this);
            mCreditsRollView = (CreditsRollView) mActivity.findViewById(R.id.creditsroll);
            if (mCreditsRollView != null) {
                String text = loadAssetTextAsString(mContext,"CREDITS");
               mCreditsRollView.setText(text);
               animateScroll();
            }
        }
        return v;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mCreditsRollView.setScrollPosition(progress / 100000f); // We have increments of 1/100000 %
    }
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (mScrolling) {
            stopScrollAnimation();
        }
    }
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
// Don't care
    }

    private void animateScroll() {
        mScrolling = true;
        mScrollAnimator = ObjectAnimator.ofInt(mSeekBar, "progress", mSeekBar.getProgress(), mSeekBar.getMax());
        mScrollAnimator.setDuration(
                (long) (SCROLL_ANIM_DURATION * (1 - (float) mSeekBar.getProgress() / mSeekBar.getMax())));
        mScrollAnimator.setInterpolator(new LinearInterpolator());
        mScrollAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
// Don't care
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                mScrolling = false;
            }
            @Override
            public void onAnimationCancel(Animator animation) {
// Don't care
            }
            @Override
            public void onAnimationRepeat(Animator animation) {
// Don't care
            }
        });
        mScrollAnimator.start();
    }
    private void stopScrollAnimation() {
        if (mScrollAnimator != null) {
            mScrollAnimator.cancel();
            mScrollAnimator = null;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        mContext = activity.getBaseContext();
    }

    public CreditFragment() {
    }

    private String loadAssetTextAsString(Context context, String name) {
        java.io.BufferedReader in = null;
        try {
            StringBuilder buf = new StringBuilder();
            java.io.InputStream is = context.getAssets().open(name);
            in = new java.io.BufferedReader(new java.io.InputStreamReader(is));

            String str;
            boolean isFirst = true;
            while ( (str = in.readLine()) != null ) {
                if (isFirst)
                    isFirst = false;
                else
                    buf.append('\n');
                buf.append(str);
            }
            return buf.toString();
        } catch (java.io.IOException e) {
            // TODO Log Log.e(TAG, "Error opening asset " + name);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (java.io.IOException e) {
                    // TODO LOOG Log.e(TAG, "Error closing asset " + name);
                }
            }
        }

        return null;
    }
}