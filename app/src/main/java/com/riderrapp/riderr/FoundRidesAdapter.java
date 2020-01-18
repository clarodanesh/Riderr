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

class FoundRidesAdapter extends RecyclerView.Adapter<FoundRidesAdapter.MyViewHolder>{

    private String[] captions;
    private Listener listener;

    interface Listener{
        void onClick(int position);
    }

    public void setListener(Listener listener){
        this.listener = listener;
    }

    List<studentData> studentDataList;
    public  FoundRidesAdapter(List<studentData> studentDataList){
        this.studentDataList = studentDataList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.found_rides_card, viewGroup, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder viewHolder, final int i) {
        Button cview = viewHolder.b;
        studentData data=studentDataList.get(i);
        Random rnd = new Random();
        int currentColor = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        viewHolder.name.setText(data.name);
        viewHolder.age.setText(String.valueOf(data.age));
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
        return studentDataList.size();
    }
    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name,age;
        LinearLayout parent;
        CardView cv;
        Button b;
        public MyViewHolder(View itemView) {
            super(itemView);
            b = itemView.findViewById(R.id.registerBtnLogin);
            cv = itemView.findViewById(R.id.card_view);
            parent = itemView.findViewById(R.id.parent);
            name = itemView.findViewById(R.id.info_text);
            age = itemView.findViewById(R.id.info_text2);
        }
    }
}
