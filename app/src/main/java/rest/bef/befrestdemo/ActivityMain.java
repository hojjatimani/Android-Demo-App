package rest.bef.befrestdemo;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import rest.bef.Befrest;
import rest.bef.BefrestFactory;
import rest.bef.BefrestMessage;
import rest.bef.BefrestPushReceiver;
import rest.bef.befrestdemo.database.ChatContentProvider;
import rest.bef.befrestdemo.database.ChatTable;
import rest.bef.befrestdemo.database.MyDatabaseHelper;
import rest.bef.befrestdemo.database.PushMsg;

/**
 * Created by hojjatimani on 3/2/2016 AD.
 */
public class ActivityMain extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ActivityMain";
    @Bind(R.id.toolbar)
    Toolbar toolbar;


    @Bind(R.id.myId)
    TextView myId;

    @Bind(R.id.refresh)
    TextView refresh;
    @Bind(R.id.contacts)
    TextView contacts;

    @Bind(R.id.refreshLayout)
    SwipeRefreshLayout refreshLayout;

    @Bind(R.id.list)
    RecyclerView list;

    @Bind(R.id.topic_chooser)
    RadioGroup topicChooser;

    @Bind(R.id.other_topic_name)
    EditText otherTopicName;

    @Bind(R.id.msg)
    EditText msg;

    @Bind(R.id.send)
    ImageButton sendBtn;

    AdapterChat listAdapter;


    Befrest befrest;
    DynamicPushReceiver pushReceiver = new DynamicPushReceiver();


    int loadBlockSize = 13;
    int numberOfItemsInList = loadBlockSize;

    int prevNumerOfchats = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApplicationLoader.clearOldDataInFirstRunVersion2(this);
        goToSignUpIfNeeded();
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        myId.setText(SignupHelper.getUserId(this));

        befrest = BefrestFactory.getInstance(this);
        befrest.registerPushReceiver(pushReceiver);
        befrest.refresh();


        getSupportLoaderManager().initLoader(0, null, this);
        listAdapter = new AdapterChat(this, null, AdapterChat.CHAT_TYPE_TOPIC);
        list.setAdapter(listAdapter);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        llm.setStackFromEnd(true);
        llm.setReverseLayout(true);
        list.setLayoutManager(llm);


        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                boolean moreItemsExist = MyDatabaseHelper.getNumberOfAllTopicMessages(ActivityMain.this) > numberOfItemsInList;
                if (moreItemsExist) {
                    prevNumerOfchats = listAdapter.getItemCount();
                    numberOfItemsInList += loadBlockSize;
                    getSupportLoaderManager().restartLoader(0, null, ActivityMain.this);
                } else {
                    refreshLayout.setRefreshing(false);
                    UIHelper.notifyUser(ActivityMain.this, "پیام دیگری نیست!");
                }
            }
        });

        msg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (msg.getText().toString().trim().length() > 0)
                    sendBtn.setEnabled(true);
                else sendBtn.setEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        TextHelper.setFont(this, TextHelper.FontFamily.Default, TextHelper.FontWeight.Regular, msg);
        msg.setText("");

        ApplicationLoader.dontShowNotifFor(ApplicationLoader.TOPIC);
    }

    private void goToSignUpIfNeeded() {
        if (SignupHelper.needsSignUp(this)) {
            startActivity(new Intent(this, SignupActivity.class));
            finish();
        }
    }

    @Override

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_settings) {
            startActivity(new Intent(this, ActivitySettings.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        ApplicationLoader.dontShowNotifFor(ApplicationLoader.TOPIC);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ApplicationLoader.showNotifFor(ApplicationLoader.TOPIC);
    }

    @Override
    protected void onDestroy() {
        ApplicationLoader.showNotifFor(ApplicationLoader.TOPIC);
        super.onDestroy();
        if (pushReceiver != null) befrest.unregisterPushReceiver(pushReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        ApplicationLoader.showNotifFor(ApplicationLoader.TOPIC);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        ApplicationLoader.dontShowNotifFor(ApplicationLoader.TOPIC);
    }

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        ApplicationLoader.dontShowNotifFor(ApplicationLoader.TOPIC);
    }

    public void onViewClicked(View v) {
        int vId = v.getId();
        if (vId == refresh.getId())
            refresh();
        else if (vId == sendBtn.getId())
            sendMessage();
        else if (vId == R.id.contacts)
            showContactsList();
        else if (vId == R.id.myId)
            try {
                Log.d(TAG, "removing topic");
                befrest.removeTopic("hojjat");
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    private void sendMessage() {
        String topic = getSelectedTopic();
        if (topic == null) {
            UIHelper.notifyUser(this, "تاپیک را انتخاب کنید!");
            return;
        }
        if (!topic.matches("[A-Za-z0-9]+")) {
            UIHelper.notifyUser(this, "نام تاپیک نامعتبر است!");
            return;
        }
        if (!NetworkHelper.isConnectedToInternet(this)) {
            UIHelper.notifyUser(this, NetworkHelper.NO_NOTWORK_MSG);
            return;
        }
        String s = msg.getText().toString();
        PushMsg pushMsg = PushMsg.newTopicChat(this, topic, s);
        pushMsg.save(this);
        pushMsg.send(this);
        msg.setText("");
    }

    private String getSelectedTopic() {
        int checkedRadio = topicChooser.getCheckedRadioButtonId();
        switch (checkedRadio) {
            case R.id.topic_befrest:
                return "befrest";
            case R.id.topic_nazdika:
                return "nazdika";
            case R.id.topic_other:
                return otherTopicName.getText().toString();
        }
        return null;
    }

//    private void startPrivateChat() {
//        String userId = pvChannel.getText().toString();
//        if (userId == null || userId.length() < 1) {
//            UIHelper.notifyUser(this, "invalid user id!");
//            return;
//        }
//        Intent intent = new Intent(this, ActivityPvChat.class);
//        intent.putExtra(ActivityPvChat.USER_ID_KEY, userId);
//        startActivity(intent);
//    }

    private void startPrivateChat(String userId) {
        Intent intent = new Intent(this, ActivityPvChat.class);
        intent.putExtra(ActivityPvChat.USER_ID_KEY, userId);
        startActivity(intent);
    }

    private void refresh() {
        if (NetworkHelper.checkNetworkAvailabilityAndNotifyUserIfNotAvailable(this)) {
            refresh.setText("Refreshing...");
            befrest.refresh();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader: ");
        String[] projection = ChatTable.ALL_COLUMNS;
        CursorLoader cursorLoader = new CursorLoader(this,
                ChatContentProvider.CONTENT_URI, projection, ChatTable.COLUMN_TOPIC + " IS NOT NULL ", null, ChatTable.COLUMN_ID + " DESC limit " + numberOfItemsInList);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        listAdapter.changeCursor(data);
        int itemCount = listAdapter.getItemCount();
        if (prevNumerOfchats > -1) {
            list.scrollToPosition(prevNumerOfchats);
            prevNumerOfchats = -1;
        } else list.scrollToPosition(0);
        Log.d(TAG, "onLoadFinished: itemCount=" + itemCount);
        refreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void showContactsList() {
        final Dialog dialog = new Dialog(this);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setTitle("Start Chat With ...");
        dialog.setContentView(R.layout.dialog_contacts);
        RecyclerView list = (RecyclerView) dialog.findViewById(R.id.list);
        dialog.setCancelable(true);
        dialog.getWindow().getAttributes().width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.show();
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        list.setLayoutManager(llm);
        final AdapterContactsList adapter = new AdapterContactsList(this);
        list.setAdapter(adapter);

        list.addOnItemTouchListener(new RecyclerItemClickListener(ActivityMain.this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                dialog.dismiss();
                startPrivateChat(adapter.getItem(position));
            }
        }));
    }

    class DynamicPushReceiver extends BefrestPushReceiver {
        @Override
        public void onPushReceived(Context context, BefrestMessage[] messages) {
        }

        @Override
        public void onConnectionRefreshed(Context context) {
            refresh.setText("Refresh");
        }
    }
}