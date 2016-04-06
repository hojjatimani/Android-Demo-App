package rest.bef.befrestdemo;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import rest.bef.befrestdemo.database.PushMsg;

/**
 * Created by hojjatimani on 1/24/2016 AD.
 */
public class AdapterChat extends AdapterCursorRecyclerView<AdapterChat.ViewHolder> {
    private static final String TAG = "AdapterChat";

    int blue;
    int red;

    public static final int CHAT_TYPE_TOPIC = 0;
    public static final int CHAT_TYPE_CHANNEL = 1;
    int chatType;

    public AdapterChat(Context context, Cursor cursor, int chatType) {
        super(context, cursor);
        blue = context.getResources().getColor(R.color.lightBlue);
        red = context.getResources().getColor(R.color.befrestRed);
        this.chatType = chatType;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        final PushMsg item = PushMsg.fromCursor(cursor);
        if (chatType == CHAT_TYPE_TOPIC) {
            if (item.isIncoming) {
                viewHolder.topic.setGravity(Gravity.LEFT);
                viewHolder.topic.setText(item.from + "^" + item.topic);
            } else {
                viewHolder.topic.setText("^" + item.topic);
                viewHolder.topic.setGravity(Gravity.RIGHT);
            }
        }
        if (item.isIncoming) {
            viewHolder.outMsg.setVisibility(View.GONE);
            viewHolder.inMsg.setVisibility(View.VISIBLE);
            viewHolder.inText.setText(item.msg);
            viewHolder.inTime.setText(TimeHelper.shortTimeStamp(item.time));
        } else {
            viewHolder.inMsg.setVisibility(View.GONE);
            viewHolder.outMsg.setVisibility(View.VISIBLE);
            viewHolder.outText.setText(item.msg);
            viewHolder.outTime.setText(TimeHelper.shortTimeStamp(item.time));

            Log.d(TAG, "status : " + item.status);
            if (item.status == PushMsg.SENDING) {
                viewHolder.status.setTextColor(blue);
                viewHolder.status.setText("Sending...");
                viewHolder.status.setVisibility(View.VISIBLE);
            } else if (item.status == PushMsg.FAILED) {
                viewHolder.status.setTextColor(red);
                viewHolder.status.setText("Resend");
                viewHolder.status.setVisibility(View.VISIBLE);
            } else {
                viewHolder.status.setVisibility(View.GONE);
            }
            viewHolder.outMsg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (item.status == PushMsg.FAILED)
                        item.resend(mContext);
                }
            });
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (chatType == CHAT_TYPE_TOPIC)
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_topic_chat_item, parent, false);
        else
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_topic_chat_item, parent, false);

        ViewHolder vh = new ViewHolder(itemView);
        TextHelper.setFont(parent.getContext(), TextHelper.FontFamily.Default, TextHelper.FontWeight.Regular,
                vh.topic, vh.inText, vh.outText, vh.inTime, vh.outTime);
        return vh;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.topic)
        TextView topic;
        @Bind(R.id.in_msg)
        View inMsg;
        @Bind(R.id.out_msg)
        View outMsg;
        @Bind(R.id.in_text)
        TextView inText;
        @Bind(R.id.out_text)
        TextView outText;
        @Bind(R.id.in_time)
        TextView inTime;
        @Bind(R.id.out_time)
        TextView outTime;
        @Bind(R.id.status)
        TextView status;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            if (chatType == CHAT_TYPE_TOPIC)
                topic.setVisibility(View.VISIBLE);
            else topic.setVisibility(View.GONE);
        }
    }
}
