 package com.example.temp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;

import java.util.List;

public class DishesAdapter extends RecyclerView.Adapter<DishesAdapter.dishesHolder> {

    private Context context;
    private List<DishDetails> list;
    private totalCounterValueInterface totalCounterValueInterface;
    private int TOTAL_COUNT = 0;
    private int[] priceArray;


    public DishesAdapter(Context context, List<DishDetails> list , totalCounterValueInterface totalCounterValueInterface) {
        this.context = context;
        this.list = list;
        this.priceArray = new int[list.size()];
        this.totalCounterValueInterface = totalCounterValueInterface;
    }


    @NonNull
    @Override
    public DishesAdapter.dishesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.dish_view , parent , false);
        dishesHolder ds = new dishesHolder(v);

        return ds;
    }

    @Override
    public void onBindViewHolder(@NonNull DishesAdapter.dishesHolder holder, int position) {

        Glide.with(context).load(list.get(position).getPic()).into(holder.dishImage);
        holder.dishName.setText(list.get(position).getName());
        holder.price.setText(Integer.toString(list.get(position).getPrice())+" \u20B9");


        holder.dec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = Integer.parseInt(holder.counter.getText().toString());
                if (count != 0)
                    count--;

                holder.counter.setText(Integer.toString(count));

                TOTAL_COUNT = count*list.get(holder.getAdapterPosition()).getPrice();
                priceArray[holder.getAdapterPosition()] = TOTAL_COUNT;

                Log.d("Total count", Integer.toString(TOTAL_COUNT));
                Log.d("price array" , Integer.toString(priceArray[holder.getAdapterPosition()]));

                totalCounterValueInterface.getCounterValue(priceArray);

            }
        });


        holder.inc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int count = Integer.parseInt(holder.counter.getText().toString());
                count++;
                holder.counter.setText(Integer.toString(count));

                TOTAL_COUNT = count*list.get(holder.getAdapterPosition()).getPrice();
                priceArray[holder.getAdapterPosition()] = TOTAL_COUNT;

                Log.d("Total count", Integer.toString(TOTAL_COUNT));
                Log.d("price array" , Integer.toString(priceArray[holder.getAdapterPosition()]));

                totalCounterValueInterface.getCounterValue(priceArray);

            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class dishesHolder extends RecyclerView.ViewHolder {

        ImageView dishImage;
        TextView dishName , counter ,  price;
        Button inc , dec;


        public dishesHolder(@NonNull View itemView) {
            super(itemView);

            dishName = itemView.findViewById(R.id.dish_name);
            dishImage = itemView.findViewById(R.id.dish_image);
            counter = itemView.findViewById(R.id.num);
            inc = itemView.findViewById(R.id.inc);
            dec = itemView.findViewById(R.id.dec);
            price = itemView.findViewById(R.id.price);

        }
    }

}
