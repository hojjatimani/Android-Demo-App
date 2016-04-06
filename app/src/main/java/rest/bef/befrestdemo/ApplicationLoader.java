/******************************************************************************
 * Copyright 2015-2016 BefrestImpl
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package rest.bef.befrestdemo;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.ArraySet;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rest.bef.BefrestFactory;

import static rest.bef.befrestdemo.PrefrenceManager.PREF_IS_FIRST_RUN_VERSION2;

public class ApplicationLoader extends Application {
    private static final String TAG = "ApplicationLoader";

    public static final String TOPIC = "TOPIC";

    static Set<String> dontShowNotifFor = new HashSet<>();

    @Override
    public void onCreate() {
        super.onCreate();
        if (!SignupHelper.needsSignUp(this))
            BefrestFactory.getInstance(this).start();
    }

    public static void clearOldDataInFirstRunVersion2(Context context){
        SharedPreferences prefs = PrefrenceManager.getPrefs(context);
        if(prefs.getBoolean(PREF_IS_FIRST_RUN_VERSION2 , true)){
            prefs.edit().clear().commit();
            prefs.edit().putBoolean(PREF_IS_FIRST_RUN_VERSION2 , false).commit();
        }
    }

    static void dontShowNotifFor(String who){
        dontShowNotifFor.add(who);
    }

    static void showNotifFor(String who){
        dontShowNotifFor.remove(who);
    }

    static boolean shouldFireNotifFor(String who){
        for (String s : dontShowNotifFor) {
            Log.d(TAG, s);
        }
        return !dontShowNotifFor.contains(who);
    }
}