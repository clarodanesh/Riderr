package com.riderrapp.riderr;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

class FoundRidesAdapter extends RecyclerView.Adapter<FoundRidesAdapter.FoundRidesViewHolder>{

    private Listener foundRidesListener;
    List<rideData> rideDataList;

    interface Listener{
        void onClick(int idx);
    }

    public void setListener(Listener foundRidesListener){
        this.foundRidesListener = foundRidesListener;
    }


    public  FoundRidesAdapter(List<rideData> rideDataList){
        this.rideDataList = rideDataList;
    }

    @NonNull
    @Override
    public FoundRidesViewHolder onCreateViewHolder(@NonNull ViewGroup vg, int i) {
        View aView = LayoutInflater.from(vg.getContext()).inflate(R.layout.found_rides_card, vg, false);
        return new FoundRidesViewHolder(aView);
    }

    @Override
    public void onBindViewHolder(FoundRidesViewHolder vh, final int i) {
        Button cviewButton = vh.b;
        rideData data = rideDataList.get(i);

        vh.place.setText("Place: " + data.place);
        vh.date.setText("Date: " + data.date);
        vh.time.setText("Time: " + data.time);
        vh.price.setText("Price: " + data.price);

        cviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clickView) {
                if (foundRidesListener != null) {
                    foundRidesListener.onClick(i);
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
        CardView cv;
        Button b;
        LinearLayout parent;

        public FoundRidesViewHolder(View passedView) {
            super(passedView);
            b = passedView.findViewById(R.id.joinRideBtn);
            cv = passedView.findViewById(R.id.rideCard);
            parent = passedView.findViewById(R.id.parent);
            place = passedView.findViewById(R.id.place);
            date = passedView.findViewById(R.id.date);
            time = passedView.findViewById(R.id.time);
            price = passedView.findViewById(R.id.price);
        }
    }
}
