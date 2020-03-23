package com.riderrapp.riderr;


/*

REFERENCE: FoundRidesAdapter created using a book I purchased called "Head First Android Development" By Dawn Griffiths & David Griffiths

Griffiths, D., & Griffiths, D. (2017). Head first android development A brain-friendly guide (2nd ed.) O'Reilly Media.

The book details using an adapter with fragments but I had to adapt this as Iam using activities and not fragments.

Pages 537-577 detail the use of an adapter with a recycler view and fragments in the context of a pizza app.

*/



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

    //the adapter will need a listener so that I can set an on click listener for the join ride button
    private Listener foundRidesListener;

    //the ride data list will include rideData objects
    List<rideData> rideDataList;

    //need to create an interface for the listener so i can use the onclick listener
    interface Listener{
        void onClick(int idx);
    }

    //set the listener for the adapter
    public void setListener(Listener foundRidesListener){
        this.foundRidesListener = foundRidesListener;
    }

    //assign the list that is passed to the member list
    public  FoundRidesAdapter(List<rideData> rideDataList){
        this.rideDataList = rideDataList;
    }

    //can override the oncreateviewholder method as I am extending the recycler view adapter
    @NonNull
    @Override
    public FoundRidesViewHolder onCreateViewHolder(@NonNull ViewGroup vg, int i) {
        View aView = LayoutInflater.from(vg.getContext()).inflate(R.layout.found_rides_card, vg, false);
        return new FoundRidesViewHolder(aView);
    }

    //this is where I will set the on click listener for the join ride btn and set data into the views
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

    //need to override the getItemCount method although I never use it
    @Override
    public int getItemCount() {
        return rideDataList.size();
    }

    //I need to assign the variables to the corresponding views made in xml
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
