package rest.bef.befrestdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import rest.bef.Befrest;
import rest.bef.BefrestFactory;

/**
 * Created by hojjatimani on 4/5/2016 AD.
 */
public class ActivitySettings extends AppCompatActivity {
    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.regTTL)
    EditText regTTL;

    @Bind(R.id.topicTTL)
    EditText topicTTL;

    @Bind(R.id.topics)
    TextView topics;

    @Bind(R.id.topic_name)
    EditText topicName;

    @Bind(R.id.add_topic)
    Button addTopic;

    @Bind(R.id.remove_topic)
    Button removeTopic;

    Befrest befrest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        befrest = BefrestFactory.getInstance(this);

        regTTL.setText("" + UserPrefrences.getMessageTTL(this));
        topicTTL.setText("" + UserPrefrences.getTopicMsgTTL(this));
    }

    public void onViewClicked(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.done:
                if (save())
                    finish();
                break;
            case R.id.add_topic:
                addTopic();
                break;
            case R.id.remove_topic:
                removeTopic();
                break;
        }
    }

    private void removeTopic() {
        String topic = topicName.getText().toString();
        try {
            befrest.removeTopic(topic);
        }catch (IllegalArgumentException e){
            UIHelper.notifyUser(this, "no such topic");
        }
    }

    private void addTopic() {
        String topic = topicName.getText().toString();
        try {
            befrest.addTopic(topic);
            topicName.setText("");
            updateTopicsList();
        } catch (IllegalArgumentException e) {
            UIHelper.notifyUser(this, "invalid topic name!");
        }
    }

    private void updateTopicsList() {

    }

    private boolean save() {
        int regTTLValue = -1;
        int topicTTLValue = -1;
        try {
            regTTLValue = Integer.valueOf(regTTL.getText().toString());
            topicTTLValue = Integer.valueOf(topicTTL.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (regTTLValue < 0 || topicTTLValue < 0) {
            UIHelper.notifyUser(this, "invalid values!");
            return false;
        }
        UserPrefrences.setRegularTTL(this, regTTLValue);
        UserPrefrences.setTopicTTL(this, topicTTLValue);
        return true;
    }
}
