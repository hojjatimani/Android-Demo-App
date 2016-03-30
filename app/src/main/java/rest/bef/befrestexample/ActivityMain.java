package rest.bef.befrestexample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import rest.bef.Befrest;
import rest.bef.BefrestFactory;
import rest.bef.BefrestMessage;
import rest.bef.BefrestPushReceiver;

/**
 * Created by hojjatimani on 3/2/2016 AD.
 */
public class ActivityMain extends Activity {
    private static final String TAG = "ActivityMain";

    AutoCompleteTextView toChannel;
    EditText msgChannel;
    Button sendToChannel;
    String[] topics = {"nazdika", "oddrun", "befrest"};
    String[] contacts;

    AutoCompleteTextView toTopic;
    EditText msgTopic;
    Button sendToTopic;
    Button refresh;

    TextView count;

    int allMsgsCount;

    DynamicPushReceiver receiver = new DynamicPushReceiver();

    boolean showTime;

    RecyclerView list;
    ListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ApplicationLoader.needsSignUp(this)) {
            startActivity(new Intent(this, SignupActivity.class));
            finish();
        }
        setContentView(R.layout.activity_main);

        BefrestFactory.getInstance(this).registerPushReceiver(receiver);

        ((TextView) findViewById(R.id.myId)).setText("@" + ApplicationLoader.getUserId(this));

        count = (TextView) findViewById(R.id.count);

        contacts = ApplicationLoader.getContacts(this);
        toChannel = (AutoCompleteTextView) findViewById(R.id.to_channel);
        toChannel.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contacts));
        toChannel.setThreshold(0);

        msgChannel = (EditText) findViewById(R.id.msg_channel);
        sendToChannel = (Button) findViewById(R.id.send_channel);

        toTopic = (AutoCompleteTextView) findViewById(R.id.to_topic);
        toTopic.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, topics));
        toTopic.setThreshold(0);

        msgTopic = (EditText) findViewById(R.id.msg_topic);
        sendToTopic = (Button) findViewById(R.id.send_topic);

        list = (RecyclerView) findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(this));

        refresh = (Button) findViewById(R.id.refresh);

        new InitList().execute(null, null);


        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ApplicationLoader.isConnectedToInternet(ActivityMain.this)) {
                    refresh.setText("Refreshing...");
                    BefrestFactory.getInstance(ActivityMain.this).refresh();
                } else ApplicationLoader.showToast(ActivityMain.this, "No Network!");
            }
        });

        sendToChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "send clicked");
                if (!("" + toChannel.getText().toString()).matches("[a-z_]+")) {
                    //TODO
                    ApplicationLoader.showToast(ActivityMain.this, "invalid id");
                } else {
                    ApplicationLoader.sendMessage(ActivityMain.this, "" + msgChannel.getText().toString(), toChannel.getText().toString(), null);
                }
            }
        });

        sendToTopic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!("" + toTopic.getText().toString()).matches("[a-z_]+")) {
                    //TODO
                    ApplicationLoader.showToast(ActivityMain.this, "invalid topicId");
                } else {
                    ApplicationLoader.sendMessage(ActivityMain.this, "" + msgTopic.getText().toString(), null, toTopic.getText().toString());
                }
            }
        });
        ApplicationLoader.dontShowNotif = true;
    }

    @Override
    protected void onResume() {
        ApplicationLoader.dontShowNotif = true;
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ApplicationLoader.dontShowNotif = false;
    }

    @Override
    protected void onDestroy() {
        ApplicationLoader.dontShowNotif = false;
        super.onDestroy();
        if (receiver != null) BefrestFactory.getInstance(this).unregisterPushReceiver(receiver);
    }

    void setToChannel(String to) {
        toChannel.setText(to);
        msgChannel.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    class DynamicPushReceiver extends BefrestPushReceiver {

        @Override
        public void onPushReceived(Context context, BefrestMessage[] messages) {
            for (BefrestMessage message : messages) {
                Message msg = new Message(message);
                if (msg.type == Message.MSG) {
                    if (listAdapter != null && !ApplicationLoader.isJunkMessage(msg.msg)) {
                        listAdapter.addMessage(msg);
                        list.smoothScrollToPosition(0);
                        allMsgsCount++;
                        updateCountLabel();
                    }
                } else {
                    toChannel.setAdapter(new ArrayAdapter<String>(ActivityMain.this, android.R.layout.simple_list_item_1, ApplicationLoader.getContacts(ActivityMain.this)));
                }
            }
        }

        @Override
        public void onConnectionRefreshed(Context context) {
            refresh.setText("Refresh");
        }
    }

    class InitList extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            findViewById(R.id.loading).setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            MyDatabaseHelper db = new MyDatabaseHelper(ActivityMain.this);
            ArrayList<Message> messages = db.getMessagesReverse(System.currentTimeMillis() - (24 * 60 * 60 * 1000), System.currentTimeMillis());
            allMsgsCount = db.getNumberOfAllMessages();
            db.close();
            listAdapter = new ListAdapter(messages, showTime, ActivityMain.this);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            list.setAdapter(listAdapter);
            findViewById(R.id.loading).setVisibility(View.GONE);
            updateCountLabel();
        }
    }

    void updateCountLabel() {
        count.setText("" + allMsgsCount);
    }
}