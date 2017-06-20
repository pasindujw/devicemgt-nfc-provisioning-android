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
import android.preference.ListPreference;
import android.util.AttributeSet;

import org.wso2.iot.nfcprovisioning.utils.Constants;

import java.util.TimeZone;

/**
 * This class is extended to get the summary easily
 */
public class TimeZoneListPreference extends ListPreference {
    public TimeZoneListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TimeZoneListPreference(Context context) {
        super(context);
    }

    @Override
    public CharSequence getSummary() {
        String tzId = getValue();
        TimeZone zone = TimeZone.getTimeZone(tzId);
        int offset = zone.getRawOffset() / 1000;
        int hour = offset / 3600;
        int minutes = (offset % 3600) / 60;
        String val = String.format(Constants.UI.TIME_FORMAT, hour, minutes, TimeZone.getTimeZone(tzId).getDisplayName());
        return val.replaceAll( ":-", ":");
    }
}