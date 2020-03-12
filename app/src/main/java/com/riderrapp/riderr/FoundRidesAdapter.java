package com.riderrapp.riderr;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Random;

class FoundRidesAdapter extends RecyclerView.Adapter<FoundRidesAdapter.FoundRidesViewHolder>{

    private String[] captions;
    private Listener listener;

    interface Listener{
        void onClick(int position);
    }

    public void setListener(Listener listener){
        this.listener = listener;
    }

    List<rideData> rideDataList;
    public  FoundRidesAdapter(List<rideData> rideDataList){
        this.rideDataList = rideDataList;
    }

    @NonNull
    @Override
    public FoundRidesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.found_rides_card, viewGroup, false);
        return new FoundRidesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(FoundRidesViewHolder viewHolder, final int i) {
        Button cview = viewHolder.b;
        rideData data = rideDataList.get(i);
        Random rnd = new Random();
        int currentColor = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        viewHolder.place.setText("Place: " + data.place);
        viewHolder.date.setText("Date: " + data.date);
        viewHolder.time.setText("Time: " + data.time);
        viewHolder.price.setText("Price: " + data.price);
        cview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(i);
                }
            }
        });
    }
    @Override
    public int getItemCount() {
        return rideDataList.size();
    }
    class FoundRidesViewHolder extends RecyclerView.ViewHolder {
        TextView place, date, time, price;
        LinearLayout parent;
        CardView cv;
        Button b;
        public FoundRidesViewHolder(View itemView) {
            super(itemView);
            b = itemView.findViewById(R.id.registerBtnLogin);
            cv = itemView.findViewById(R.id.card_view);
            parent = itemView.findViewById(R.id.parent);
            place = itemView.findViewById(R.id.info_text);
            date = itemView.findViewById(R.id.info_text2);
            time = itemView.findViewById(R.id.info_text3);
            price = itemView.findViewById(R.id.info_text4);
        }
    }
}
