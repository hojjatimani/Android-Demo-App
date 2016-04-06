package rest.bef.befrestdemo;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;

import static rest.bef.befrestdemo.ActivityPvChat.*;

/**
 * Created by hojjatimani on 4/6/2016 AD.
 */
public class AdapterContactsList extends RecyclerView.Adapter<AdapterContactsList.ViewHolder> {

    String[] contacts;
    String[] statuses;
    Context context;

    int green;
    int gray;
    int red;
    int white;

    public AdapterContactsList(Context context) {
        this.context = context;
        contacts = ContactsHelper.getContactsList(context);
        statuses = new String[contacts.length];
        green = context.getResources().getColor(R.color.green);
        gray = context.getResources().getColor(R.color.gray);
        red = context.getResources().getColor(R.color.befrestRed);
        white = context.getResources().getColor(R.color.white);
        fillStatuses();
    }

    private void fillStatuses() {
        if (NetworkHelper.isConnectedToInternet(context))
            for (int i = 0; i < statuses.length; i++)
                statuses[i] = STATUS_FETCHING;
        else
            for (int i = 0; i < statuses.length; i++)
                statuses[i] = STATUS_NO_NETWORK;
        for (int i = 0; i < statuses.length; i++) {
            new StatusFetcher(i).execute();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_contact_list, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.userId.setText(contacts[position]);
        String stat = statuses[position];
        holder.status.setText(stat);
        if (stat.equals(STATUS_ONLINE))
            holder.status.setTextColor(green);
        else if (stat.equals(STATUS_OFFLINE))
            holder.status.setTextColor(red);
        else
            holder.status.setTextColor(gray);
    }

    @Override
    public int getItemCount() {
        return contacts.length;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.userId)
        TextView userId;

        @Bind(R.id.status)
        TextView status;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public String getItem(int position){
        return contacts[position];
    }

    class StatusFetcher extends AsyncTask<String, String, String> {

        int position;

        public StatusFetcher(int position) {
            this.position = position;
        }

        @Override
        protected String doInBackground(String... params) {
            statuses[position] = BefrestHelper.getUserStatus(context, contacts[position]);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            notifyItemChanged(position);
        }
    }
}
