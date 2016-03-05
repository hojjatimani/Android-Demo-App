/******************************************************************************
 * Copyright 2015-2016 BefrestImpl
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package rest.bef.befrestexample;

import android.content.Context;
import android.util.Log;

import rest.bef.BefrestFactory;
import rest.bef.BefrestMessage;
import rest.bef.BefrestPushReceiver;


/**
 * Created by ehsan on 11/11/2015.
 */
public class MStaticPushReceiver extends BefrestPushReceiver {
    private static final String TAG = "MStaticPushReceiver";
    String auth = "JYGawV5h1rzC-I_fVMzR4Q";

    @Override
    public void onPushReceived(Context context, BefrestMessage[] messages) {
        Log.d(TAG, "onPUshReceive");
    }

    @Override
    public void onConnectionRefreshed(Context context) {
        Log.d(TAG, "onConnectionRefreshed");
    }

    @Override
    public void onAuthorizeProblem(Context context) {
        Log.d("MReceiver", "onAuthorizeProblem");
        BefrestFactory.getInstance(context)
                .setAuth(auth)
                .start();
    }

    @Override
    public void onBefrestConnected(Context context) {
        Log.d(TAG, "onBefrestConnected");
    }

    @Override
    public void onAnomaly(Context context, String data) {
        Log.d(TAG, "onAnomaly :: " + data );
    }
}