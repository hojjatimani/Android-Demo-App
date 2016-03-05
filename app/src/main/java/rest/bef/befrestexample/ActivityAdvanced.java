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
package rest.bef.befrestexample;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import rest.bef.Befrest;
import rest.bef.BefrestFactory;
import rest.bef.BefrestMessage;
import rest.bef.BefrestPushReceiver;

public class ActivityAdvanced extends AppCompatActivity {
    private static final String TAG = "ActivityAdvanced";

    Button clear;
    TextView received;
    TextView count;
    ScrollView scrollView;
    Button send;
    EditText editText;
    Button refresh;
    Button sendOnTopic;

    Button start;
    Button stop;
    Button addTopic;
    Button removeTopic;
    Button listTopic;

    Button enableCheckInSleep;
    Button disableCheckInSleep;


    Button reportClose;

    BefrestPushReceiver receiver = new Receiver();
    Befrest befrest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        received = (TextView) findViewById(R.id.received);
        count = (TextView) findViewById(R.id.count);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        send = (Button) findViewById(R.id.send);
        editText = (EditText) findViewById(R.id.editText);
        refresh = (Button) findViewById(R.id.refresh);
        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);
        addTopic = (Button) findViewById(R.id.addTopic);
        removeTopic = (Button) findViewById(R.id.removeTopic);
        clear = (Button) findViewById(R.id.clear);
        listTopic = (Button) findViewById(R.id.listTopics);
        sendOnTopic = (Button) findViewById(R.id.sendOnTopic);
        enableCheckInSleep = (Button) findViewById(R.id.enableCheckSleep);
        disableCheckInSleep = (Button) findViewById(R.id.disableCheckSleep);
        reportClose = (Button) findViewById(R.id.reportClose);

        befrest = BefrestFactory.getInstance(this);
        befrest.registerPushReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        befrest.unregisterPushReceiver(receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onViewCliced(View v) {
        int vId = v.getId();
        if (vId == clear.getId())
            editText.setText("");
        else if (vId == send.getId())
//            ApplicationLoader.sendMessage(ActivityAdvanced.this, editText.getText().toString(), null);
//        else if (vId == sendOnTopic.getId())
//            ApplicationLoader.sendMessage(ActivityAdvanced.this, editText.getText().toString(), editText.getText().toString().split(" ")[0]);
//        else
        if (vId == start.getId())
            befrest.start();
        else if (vId == stop.getId())
            befrest.stop();
        else if (vId == addTopic.getId())
            befrest.addTopic(editText.getText().toString());
        else if (vId == removeTopic.getId())
            befrest.removeTopic(editText.getText().toString());
        else if (vId == listTopic.getId())
            for (String s : befrest.getCurrentTopics())
                Log.d(TAG, "current Topics : " + s);
        else if (vId == refresh.getId()) {
            if (befrest.refresh())
                refresh.setText("Refreshing ...");
        } else if (vId == disableCheckInSleep.getId())
            befrest.disableCheckInSleep();
        else if (vId == enableCheckInSleep.getId())
            befrest.enableCheckInSleep();
//        else if(vId == reportClose.getId())
//            befrest.reportOnClose(this, (int)(Math.random() * 10));
    }

    class Receiver extends BefrestPushReceiver {

        @Override
        public void onPushReceived(Context context, BefrestMessage[] messages) {
            Log.d(TAG, "#messages in push : " + messages.length);
            int length = 0;
            for (BefrestMessage msg : messages) {
                received.setText(received.getText() + "\n" + msg.getTimeStamp() + " " + msg.getData());
                length += msg.getData().length();
            }
            Toast.makeText(ActivityAdvanced.this, "total length : " + length, Toast.LENGTH_SHORT).show();
            count.setText(Integer.parseInt(count.getText().toString()) + messages.length + "");
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.smoothScrollTo(0, scrollView.getBottom());
                }
            });
        }

        @Override
        public void onConnectionRefreshed(Context context) {
            refresh.setText("REFRESH");
        }


    }
}
