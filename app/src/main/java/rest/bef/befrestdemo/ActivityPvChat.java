package rest.bef.befrestdemo;

import android.database.Cursor;
import android.os.AsyncTask;
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
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import rest.bef.BefrestFactory;
import rest.bef.befrestdemo.database.ChatContentProvider;
import rest.bef.befrestdemo.database.ChatTable;
import rest.bef.befrestdemo.database.MyDatabaseHelper;
import rest.bef.befrestdemo.database.PushMsg;

/**
 * Created by hojjatimani on 4/5/2016 AD.
 */
public class ActivityPvChat extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ActivityPvChat";

    public static final String USER_ID_KEY = "USER_KEY";

    public static final String STATUS_FETCHING = "Fetching...";
    public static final String STATUS_ONLINE = "Online";
    public static final String STATUS_OFFLINE = "Offline";
    public static final String STATUS_UNKNOWN = "UnKnown";
    public static final String STATUS_NO_NETWORK = "no network";

    boolean fetchingStatus = false;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.userId)
    TextView userIdTextView;

    @Bind(R.id.status)
    TextView status;

    @Bind(R.id.refreshLayout)
    SwipeRefreshLayout refreshLayout;

    @Bind(R.id.list)
    RecyclerView list;

    @Bind(R.id.msg)
    EditText msg;

    @Bind(R.id.send)
    ImageButton sendBtn;

    AdapterChat listAdapter;

    String userId;

    int loadBlockSize = 13;
    int numberOfItemsInList = loadBlockSize;

    int prevNumerOfchats = -1;

    int green;
    int gray;
    int red;
    int white;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pv_chat);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        userId = getIntent().getStringExtra(USER_ID_KEY);
        userIdTextView.setText(userId);

        BefrestFactory.getInstance(this).refresh();

        getSupportLoaderManager().initLoader(0, null, this);
        listAdapter = new AdapterChat(this, null, AdapterChat.CHAT_TYPE_CHANNEL);
        list.setAdapter(listAdapter);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        llm.setStackFromEnd(true);
        llm.setReverseLayout(true);
        list.setLayoutManager(llm);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                boolean moreItemsExist = MyDatabaseHelper.getNumberOfChatsWithUser(ActivityPvChat.this, userId) > numberOfItemsInList;
                if (moreItemsExist) {
                    prevNumerOfchats = listAdapter.getItemCount();
                    numberOfItemsInList += loadBlockSize;
                    getSupportLoaderManager().restartLoader(0, null, ActivityPvChat.this);
                } else {
                    refreshLayout.setRefreshing(false);
                    UIHelper.notifyUser(ActivityPvChat.this, "پیام دیگری نیست!");
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

        green = ActivityPvChat.this.getResources().getColor(R.color.green);
        gray = ActivityPvChat.this.getResources().getColor(R.color.gray);
        red = ActivityPvChat.this.getResources().getColor(R.color.befrestRed);
        white = ActivityPvChat.this.getResources().getColor(R.color.white);

        fetchUserStatus();

        ApplicationLoader.dontShowNotifFor(userId);
    }

    private void fetchUserStatus() {
        if (!NetworkHelper.isConnectedToInternet(this)) {
            UIHelper.notifyUser(this, NetworkHelper.NO_NOTWORK_MSG);
            status.setTextColor(gray);
            status.setText(STATUS_NO_NETWORK);
        } else {
            if (!fetchingStatus) new userStatusSetter().execute();
        }
    }

    @Override
    protected void onResume() {
        ApplicationLoader.dontShowNotifFor(userId);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ApplicationLoader.showNotifFor(userId);
    }

    @Override
    protected void onDestroy() {
        ApplicationLoader.showNotifFor(userId);
        super.onDestroy();
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
        switch (vId) {
            case R.id.send:
                sendMessage();
                break;
            case R.id.status:
                fetchUserStatus();
        }
    }

    private void sendMessage() {
        if (!NetworkHelper.isConnectedToInternet(this)) {
            UIHelper.notifyUser(this, NetworkHelper.NO_NOTWORK_MSG);
            return;
        }
        String s = msg.getText().toString();
        PushMsg pushMsg = PushMsg.newMsgChat(this, userId, s);
        pushMsg.save(this);
        pushMsg.send(this);
        msg.setText("");
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader: ");
        String[] projection = ChatTable.ALL_COLUMNS;
        String selection = null;
        if (SignupHelper.getUserId(this).equals(userId)) {
            //chatting with my self!
            selection = ChatTable.COLUMN_TOPIC + " IS NULL AND " + ChatTable.COLUMN_FROM + " = '" + this.userId + "' AND " + ChatTable.COLUMN_TO + " = '" + this.userId + "' ";
        } else {
            selection = ChatTable.COLUMN_TOPIC + " IS NULL AND (" + ChatTable.COLUMN_FROM + " = '" + this.userId + "' OR " + ChatTable.COLUMN_TO + " = '" + this.userId + "' )";
        }
        CursorLoader cursorLoader = new CursorLoader(this,
                ChatContentProvider.CONTENT_URI, projection, selection, null, ChatTable.COLUMN_ID + " DESC limit " + numberOfItemsInList);
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

    class userStatusSetter extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            status.setTextColor(white);
            status.setText(STATUS_FETCHING);
            fetchingStatus = true;
        }

        @Override
        protected String doInBackground(String... params) {
            return BefrestHelper.getUserStatus(ActivityPvChat.this, userId);
        }

        @Override
        protected void onPostExecute(String s) {
            status.setText(s);
            if (STATUS_ONLINE.equals(s)) {
                status.setTextColor(green);
            } else if (STATUS_OFFLINE.equals(s))
                status.setTextColor(gray);
            else
                status.setTextColor(red);
            fetchingStatus = false;
        }
    }
}
