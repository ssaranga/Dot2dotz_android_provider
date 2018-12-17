package com.dot2dotz.provider.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dot2dotz.provider.Bean.Flows;
import com.dot2dotz.provider.R;
import com.dot2dotz.provider.Utilities.MyBoldTextView;
import com.dot2dotz.provider.Utilities.MyTextView;

import java.util.ArrayList;


public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {

    private static final String TAG = "StatusAdapter";
    boolean[] selectedService;
    private ArrayList<Flows> listModels;
    private Context context;

    public TripAdapter(ArrayList<Flows> listModel, Context context) {
        this.listModels = listModel;
        this.context = context;
    }

    public void setListModels(ArrayList<Flows> listModels) {
        this.listModels = listModels;
    }

    @Override
    public TripAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.trip_item, parent, false);
        return new TripAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Flows status = listModels.get(position);
        if (status.getdeliveryAddress() != null) {
            holder.destination_address.setText(status.getdeliveryAddress());
        } else {
            holder.destination_address.setText("");
        }
        if (status.getcomments() != null) {
            holder.comments.setText(status.getcomments());
        } else {
            holder.comments.setText("");
        }
        holder.destination_address.setTag(status);
        holder.trip.setText(context.getResources().getString(R.string.trip)+String.valueOf(position+1));

    }

    @Override
    public int getItemCount() {
        return listModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        MyBoldTextView trip;
        MyTextView destination_address, comments, status;
        private ArrayList<Flows> listModels;
        public ViewHolder(View itemView) {
            super(itemView);
            destination_address = itemView.findViewById(R.id.destination_address);
            comments = itemView.findViewById(R.id.comments);
            trip = itemView.findViewById(R.id.trip);
        }

    }

}


