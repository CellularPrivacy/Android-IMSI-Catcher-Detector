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
* limitations under the License.*/

/* Edited by Paul Kinsella <paulkinsella29@yahoo.ie> */

package com.SecUpwN.AIMSICD.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import com.SecUpwN.AIMSICD.R;

/**
 * Implements a Star Wars-like credits roll.
 */
public class CreditsRollView extends TextView {

    /** Epsilon used to determine if two float values are equal */
    private static final float FLOAT_EPSILON = 0.001f;

    private int mPaddingLeft;
    private int mPaddingTop;
    private int mPaddingRight;
    private int mPaddingBottom;

    private int mTextColor = 0xffffc92a;   // Default: yellow!

    private float mAngle = 60f;
    private float mScrollPosition = 0f;
    private float mEndScrollMult = 2f;
    private float mDistanceFromText = 0f;

    private final Camera mCamera = new Camera();
    private Matrix mMatrix = new Matrix();
    private TextPaint mTextPaint;
    private StaticLayout mTextLayout;

    public CreditsRollView(Context context) {
        super(context);
        init(null, 0, context);
    }

    public CreditsRollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CreditsRollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle, context);
    }

    private void init(AttributeSet attrs, int defStyle, Context context) {
        // Load attributes
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.CreditsRollView, defStyle, 0);

        if (a != null) {
            final int N = a.getIndexCount();

            for (int i = 0; i < N; ++i) {
                int attr = a.getIndex(i);

                if (attr == R.styleable.CreditsRollView_angle) {
                    float angle = a.getFloat(attr, mAngle);
                    setAngle(angle);
                }
                else if (attr == R.styleable.CreditsRollView_scrollPosition) {
                    float scrollPercent = a.getFloat(attr, 0f);
                    setScrollPosition(scrollPercent);
                }
                else if (attr == R.styleable.CreditsRollView_endScrollMultiplier) {
                    float scrollMult = a.getFloat(attr, 0f);
                    setEndScrollMult(scrollMult);
                }
                else if (attr == R.styleable.CreditsRollView_distanceFromText) {
                    float distance = a.getFloat(attr, 0f);
                    setDistanceFromText(distance);
                }
            }

            a.recycle();
        }

        initTextPaint();

    }

    private void initTextPaint() {
        mTextPaint = new TextPaint();

        // Set up a default TextPaint object
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG
         );

        // Update TextPaint and text measurements
        mTextPaint.setTextSize(getTextSize());
        mTextPaint.setColor(mTextColor);
    }

    /**
     * Gets the current scrolling percentage.
     *
     * @return Returns the current scrolling position, in the
     * [0, 1] range.
     */
    public float getScrollPosition() {
        return mScrollPosition;
    }

    /**
     * Sets the scroll position for this view.
     *
     * @param scrollPosition The new scrolling position, in the
     *                       [0, 1] range.
     */
    public void setScrollPosition(float scrollPosition) {
        if (scrollPosition < 0f) {
            scrollPosition = 0f;
        }
        else if (scrollPosition > 1f) {
            scrollPosition = 1f;
        }

        // Only invalidate if really necessary
        if (Math.abs(mScrollPosition - scrollPosition) > FLOAT_EPSILON) {
            mScrollPosition = scrollPosition;
            invalidate();
        }
    }

    /**
     * Gets the credits roll text tilting angle, on the
     * X axis.
     *
     * @return The X-axis tilting angle, in degrees.
     */
    public float getAngle() {
        return mAngle;
    }

    /**
     * Sets the credits roll text tilting angle, on the
     * X axis.
     *
     * @param angle The X-axis tilting angle, in degrees.
     */
    public void setAngle(float angle) {
        if (Math.abs(mAngle - angle) > FLOAT_EPSILON) {
            mAngle = angle;
            invalidate();
        }
    }

    /**
     * Gets the end scrolling multiplier value.
     *
     * @return Returns the end scrolling multiplier.
     */
    public float getEndScrollMult() {
        return mEndScrollMult;
    }

    /**
     * Sets the end scrolling multiplier value.
     * This value is multiplied by the available height of the View
     * (that is, {@link #getHeight()} less the top/bottom paddings)
     * and used to offset the end scrolling point. Adjust this to
     * have the text scrolling end in the desired position.
     * Depending on the angle value, you may need to set this to 2,
     * 3 or more to have the text get scrolled completely out of
     * view.
     *
     * @param endScrollMult The end scrolling multiplier.
     *
     * @see #setAngle(float)
     */
    public void setEndScrollMult(float endScrollMult) {
        if (Math.abs(mEndScrollMult - endScrollMult) > FLOAT_EPSILON) {
            mEndScrollMult = endScrollMult;
            invalidate();
        }
    }

    /**
     * Gets the virtual camera's distance from the text, on
     * the Z axis (depth).
     *
     * @return Returns the camera distance from the text.
     */
    public float getDistanceFromText() {
        return mDistanceFromText;
    }

    /**
     * Sets the virtual camera's distance from the text, on
     * the Z axis (depth).
     * <p/>
     * The default distance is <code>0</code>. You can increase
     * it to "zoom out" from the text, or decrease to "zoom in" to
     * the text.
     *
     * @param distanceFromText The camera distance from the text.
     */
    public void setDistanceFromText(float distanceFromText) {
        mDistanceFromText = distanceFromText;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaddingLeft = getPaddingLeft();
        mPaddingTop = getPaddingTop();
        mPaddingRight = getPaddingRight();
        mPaddingBottom = getPaddingBottom();

        final CharSequence text = getText();
        if (TextUtils.isEmpty(text)) {
            return;
        }

        int contentWidth = getWidth() - mPaddingLeft - mPaddingRight;
        int contentHeight = getHeight() - mPaddingTop - mPaddingBottom;

        final int saveCnt = canvas.save();

        // Rotate/translate the camera
        canvas.getMatrix(mMatrix);
        mCamera.save();

        int cX = contentWidth / 2 + mPaddingLeft;
        int cY = contentHeight / 2 + mPaddingTop;
        mCamera.rotateX(mAngle);
        mCamera.translate(0, 0, mDistanceFromText);
        mCamera.getMatrix(mMatrix);
        mMatrix.preTranslate(-cX, -cY);
        mMatrix.postTranslate(cX, cY);
        mCamera.restore();

        canvas.concat(mMatrix);

        // The end scroll multiplier ensures that the text scrolls completely out of view
        canvas.translate(9f, contentHeight - mScrollPosition *
                (mTextLayout.getHeight() + mEndScrollMult * contentHeight));

        // Draw the text
        mTextLayout.draw(canvas);

        canvas.restoreToCount(saveCnt);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);

        if (mTextPaint == null) {
            initTextPaint();
        }

        measureAndLayoutText(text);
    }

    @Override
    public void setTextColor(int color) {
        super.setTextColor(color);
        mTextColor = color;
        initTextPaint();
    }

    @Override
    public void setTextColor(ColorStateList colors) {
        super.setTextColor(colors);

        if (!colors.isStateful()) {
            mTextColor = colors.getDefaultColor();
        }
        else {
            mTextColor = colors.getColorForState(getDrawableState(), colors.getDefaultColor());
        }

        initTextPaint();
        invalidate();
    }

    @Override
    public void setTextSize(float size) {
        super.setTextSize(size);

        initTextPaint();
        invalidate();
    }

    @Override
    public void setTextSize(int unit, float size) {
        super.setTextSize(unit, size);

        initTextPaint();
        invalidate();
    }

    @Override
    public void setTextAppearance(Context context, int resid) {
        super.setTextAppearance(context, resid);

        initTextPaint();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Measure and layout the text
        final CharSequence text = getText();
        measureAndLayoutText(text);
    }

    private void measureAndLayoutText(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            mTextLayout = null;
            return;
        }

        int availableWidth = getWidth();// - mPaddingLeft - mPaddingRight;
        mTextLayout = new StaticLayout(text, mTextPaint, availableWidth, Layout.Alignment.ALIGN_CENTER,
                1.1f, 0f, true);
    }
}