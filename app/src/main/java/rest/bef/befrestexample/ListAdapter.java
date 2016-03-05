package rest.bef.befrestexample;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by hojjatimani on 3/2/2016 AD.
 */
public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    ArrayList<Message> data;
    boolean showTime;
    ActivityMain activityMain;

    public ListAdapter(ArrayList<Message> data, boolean showTime, ActivityMain activityMain) {
        this.data = data;
        this.showTime = showTime;
        this.activityMain = activityMain;
    }

    public void addMessage(Message msg){
        data.add(0, msg);
        notifyItemInserted(0);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Message message = data.get(position);
        holder.msg.setText(message.msg);
        holder.from.setText("from: " + message.from);
        if (showTime) {
            holder.timeReceivd.setText("" + message.timeReceived);
            holder.timeSent.setText(message.timeSent);
            holder.timeReceivd.setVisibility(View.VISIBLE);
            holder.timeSent.setVisibility(View.VISIBLE);
        } else {
            holder.timeReceivd.setVisibility(View.GONE);
            holder.timeSent.setVisibility(View.GONE);
        }
        holder.reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activityMain.setToChannel(message.from);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView from;
        TextView reply;
        TextView timeSent;
        TextView timeReceivd;
        TextView msg;


        public ViewHolder(View v) {
            super(v);
            from = (TextView) v.findViewById(R.id.from);
            reply = (TextView) v.findViewById(R.id.reply);
            timeSent = (TextView) v.findViewById(R.id.time_sent);
            timeReceivd = (TextView) v.findViewById(R.id.time_received);
            msg = (TextView) v.findViewById(R.id.msg);
        }
    }
}
