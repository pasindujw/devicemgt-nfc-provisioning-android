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
package org.wso2.iot.nfcprovisioning.api;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

/**
 * This class represents the device id information related API.
 */
@SuppressLint("HardwareIds")
public class DeviceInfo {

	private Context context;
	private TelephonyManager telephonyManager;

	public DeviceInfo(Context context) {
		this.context = context;
		this.telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	}

	/**
	 * Returns the IMEI Number.
	 * @return - Device IMEI number.
	 */
	public String getDeviceId() {

		String deviceId = null;

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			deviceId = telephonyManager.getDeviceId();
		}

		if (deviceId == null || deviceId.isEmpty()) {
			deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
		}
		
		return deviceId;
	}
}