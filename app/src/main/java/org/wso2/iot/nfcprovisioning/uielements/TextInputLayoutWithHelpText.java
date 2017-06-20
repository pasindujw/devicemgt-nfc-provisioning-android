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
import org.wso2.iot.nfcprovisioning.utils.Constants;

/**
 * This class is extended to display help text as stated in material design
 * guide lines.
 */
public class TextInputLayoutWithHelpText extends TextInputLayout {

    static final Interpolator FAST_OUT_SLOW_IN_INTERPOLATOR = new FastOutSlowInInterpolator();

    private CharSequence helperText;
    private ColorStateList helperTextColor;
    private boolean helperTextEnabled = false;
    private boolean errorEnabled = false;
    private TextView helperView;

    public TextInputLayoutWithHelpText(Context context) {
        super(context);
    }

    public TextInputLayoutWithHelpText(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs,
                R.styleable.TextInputLayoutWithHelpText,0,0);
        try {
            helperTextColor = a.getColorStateList(R.styleable.TextInputLayoutWithHelpText_helperTextColor);
            helperText = a.getText(R.styleable.TextInputLayoutWithHelpText_helperText);
        } finally {
            a.recycle();
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        if (child instanceof EditText) {
            if (!TextUtils.isEmpty(helperText)) {
                setHelperText(helperText);
            }
        }
    }

    public void setHelperTextColor(ColorStateList helperTextColor) {
        this.helperTextColor = helperTextColor;
    }

    public void setHelperTextEnabled(boolean enabled) {
        if (helperTextEnabled == enabled) return;
        if (enabled && errorEnabled) {
            setErrorEnabled(false);
        }
        if (this.helperTextEnabled != enabled) {
            if (enabled) {
                this.helperView = new TextView(this.getContext());
                if (helperTextColor != null){
                    this.helperView.setTextColor(helperTextColor);
                }
                this.helperView.setText(helperText);
                this.helperView.setVisibility(VISIBLE);
                this.addView(this.helperView);
                if (this.helperView != null) {
                    ViewCompat.setPaddingRelative(
                            this.helperView,
                            ViewCompat.getPaddingStart(getEditText()),
                            0, ViewCompat.getPaddingEnd(getEditText()),
                            getEditText().getPaddingBottom());
                }
            } else {
                this.removeView(this.helperView);
                this.helperView = null;
            }
            this.helperTextEnabled = enabled;
        }
    }

    public void setHelperText(CharSequence helperText) {
        this.helperText = helperText;
        if (!this.helperTextEnabled) {
            if (TextUtils.isEmpty(this.helperText)) {
                return;
            }
            this.setHelperTextEnabled(true);
        }
        if (!TextUtils.isEmpty(this.helperText)) {
            this.helperView.setText(this.helperText);
            this.helperView.setVisibility(VISIBLE);
            ViewCompat.setAlpha(this.helperView, Constants.UI.ALPHA_ZERO);
            ViewCompat.animate(this.helperView)
                    .alpha(Constants.UI.ANIMATE_ALPHA).setDuration(Constants.UI.ANIMATE_DURATION)
                    .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
                    .setListener(null).start();
        } else if (this.helperView.getVisibility() == VISIBLE) {
            ViewCompat.animate(this.helperView)
                    .alpha(Constants.UI.ALPHA_ZERO).setDuration(Constants.UI.ANIMATE_DURATION)
                    .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
                    .setListener(new ViewPropertyAnimatorListenerAdapter() {
                        public void onAnimationEnd(View view) {
                            helperView.setText(null);
                            helperView.setVisibility(INVISIBLE);
                        }
                    }).start();
        }
        this.sendAccessibilityEvent(Constants.UI.ACCESSIBILITY_EVENT_TYPE);
    }

    @Override
    public void setErrorEnabled(boolean enabled) {
        if (errorEnabled == enabled) return;
        errorEnabled = enabled;
        if (enabled && helperTextEnabled) {
            setHelperTextEnabled(false);
        }
        super.setErrorEnabled(enabled);
        if (!(enabled || TextUtils.isEmpty(helperText))) {
            setHelperText(helperText);
        }
    }
}
