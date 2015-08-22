/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.root.ailight;


import android.content.Context;
import android.text.StaticLayout;

import org.json.JSONException;
import org.json.JSONObject;

/**
 */
public class Constants {

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    public static final String PREFER_NAME ="smartLight";
    public static final String PREFER_INFO_KEY ="connectInfo";

    public static JSONObject getSavedInfo(Context cnt){
        String info =cnt.getSharedPreferences(PREFER_NAME,cnt.MODE_PRIVATE).getString(PREFER_INFO_KEY,"{}");
        try {
            return new JSONObject(info);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  null;
    }

    public static void saveInfo(JSONObject info,Context cnt){
        cnt.getSharedPreferences(PREFER_NAME,cnt.MODE_PRIVATE).edit().putString(PREFER_INFO_KEY,info.toString()).commit();
    }

}
