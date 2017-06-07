/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.iot.nfcprovisioning.uielements;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.TextView;

import org.wso2.iot.nfcprovisioning.R;

public class TextInputLayoutWithHelpText extends TextInputLayout {

    static final Interpolator FAST_OUT_SLOW_IN_INTERPOLATOR = new FastOutSlowInInterpolator();

    private CharSequence mHelperText;
    private ColorStateList mHelperTextColor;
    private boolean mHelperTextEnabled = false;
    private boolean mErrorEnabled = false;
    private TextView mHelperView;

    public TextInputLayoutWithHelpText(Context context) {
        super(context);
    }

    public TextInputLayoutWithHelpText(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TypedArray a = getContext().obtainStyledAttributes(
                attrs,
                R.styleable.TextInputLayoutWithHelpText,0,0);
        try {
            mHelperTextColor = a.getColorStateList(R.styleable.TextInputLayoutWithHelpText_helperTextColor);
            mHelperText = a.getText(R.styleable.TextInputLayoutWithHelpText_helperText);
        } finally {
            a.recycle();
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        if (child instanceof EditText) {
            if (!TextUtils.isEmpty(mHelperText)) {
                setHelperText(mHelperText);
            }
        }
    }

    public void setHelperTextColor(ColorStateList _helperTextColor) {
        mHelperTextColor = _helperTextColor;
    }

    public void setHelperTextEnabled(boolean _enabled) {
        if (mHelperTextEnabled == _enabled) return;
        if (_enabled && mErrorEnabled) {
            setErrorEnabled(false);
        }
        if (this.mHelperTextEnabled != _enabled) {
            if (_enabled) {
                this.mHelperView = new TextView(this.getContext());
                if (mHelperTextColor != null){
                    this.mHelperView.setTextColor(mHelperTextColor);
                }
                this.mHelperView.setText(mHelperText);
                this.mHelperView.setVisibility(VISIBLE);
                this.addView(this.mHelperView);
                if (this.mHelperView != null) {
                    ViewCompat.setPaddingRelative(
                            this.mHelperView,
                            ViewCompat.getPaddingStart(getEditText()),
                            0, ViewCompat.getPaddingEnd(getEditText()),
                            getEditText().getPaddingBottom());
                }
            } else {
                this.removeView(this.mHelperView);
                this.mHelperView = null;
            }

            this.mHelperTextEnabled = _enabled;
        }
    }

    public void setHelperText(CharSequence _helperText) {
        mHelperText = _helperText;
        if (!this.mHelperTextEnabled) {
            if (TextUtils.isEmpty(mHelperText)) {
                return;
            }
            this.setHelperTextEnabled(true);
        }

        if (!TextUtils.isEmpty(mHelperText)) {
            this.mHelperView.setText(mHelperText);
            this.mHelperView.setVisibility(VISIBLE);
            ViewCompat.setAlpha(this.mHelperView, 0.0F);
            ViewCompat.animate(this.mHelperView)
                    .alpha(1.0F).setDuration(200L)
                    .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
                    .setListener(null).start();
        } else if (this.mHelperView.getVisibility() == VISIBLE) {
            ViewCompat.animate(this.mHelperView)
                    .alpha(0.0F).setDuration(200L)
                    .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
                    .setListener(new ViewPropertyAnimatorListenerAdapter() {
                        public void onAnimationEnd(View view) {
                            mHelperView.setText(null);
                            mHelperView.setVisibility(INVISIBLE);
                        }
                    }).start();
        }
        this.sendAccessibilityEvent(2048);
    }

    @Override
    public void setErrorEnabled(boolean _enabled) {
        if (mErrorEnabled == _enabled) return;
        mErrorEnabled = _enabled;
        if (_enabled && mHelperTextEnabled) {
            setHelperTextEnabled(false);
        }

        super.setErrorEnabled(_enabled);

        if (!(_enabled || TextUtils.isEmpty(mHelperText))) {
            setHelperText(mHelperText);
        }
    }
}
