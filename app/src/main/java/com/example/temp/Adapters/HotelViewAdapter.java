package com.example.temp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.temp.Activities.Dishes;
import com.example.temp.Interfaces.EditHotelInterface;
import com.example.temp.Models.HotelDetails;
import com.example.temp.R;
import com.google.android.material.textview.MaterialTextView;
import com.squareup.picasso.Picasso;

import java.util.List;

public class HotelViewAdapter extends RecyclerView.Adapter<HotelViewAdapter.HotelViewHolder> {

    private Context context;
    private List<HotelDetails> list;
    private List<String> hotelKeys;
    private EditHotelInterface editHotelInterface;

    public HotelViewAdapter(Context context, List<HotelDetails> list, List<String> hotelKeys,EditHotelInterface editHotelInterface) {
        this.context = context;
        this.list = list;
        this.hotelKeys = hotelKeys;
        this.editHotelInterface=editHotelInterface;

    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.hotel_view ,
                        parent ,
                        false);
        return new HotelViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {

        Picasso.get()
                .load(list.get(position).getImage())
                .placeholder(R.drawable.ic_baseline_image_24)
                .into(holder.image);

        holder.name.setText(list.get(position).getHotel_name());
        holder.location.setText(list.get(position).getLocation());
        holder.address.setText(list.get(position).getAddress());

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                editHotelInterface.editHotel(list.get(holder.getAdapterPosition()) , hotelKeys.get(holder.getAdapterPosition()));

                return true;
            }
        });


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent openDishesSection = new Intent(context , Dishes.class);
                openDishesSection.putExtra("hotel_key" , hotelKeys.get(holder.getAdapterPosition()));
                openDishesSection.putExtra("hotelDetails" , list.get(holder.getAdapterPosition()));
                openDishesSection.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(openDishesSection);

            }
        });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class HotelViewHolder extends RecyclerView.ViewHolder {

        private MaterialTextView name , address , location;
        private AppCompatImageView image;


        public HotelViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.hotel_name);
            address = itemView.findViewById(R.id.hotel_address);
            location = itemView.findViewById(R.id.hotel_location);
            image = itemView.findViewById(R.id.hotel_image);

        }
    }
}
