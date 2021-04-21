 package com.example.temp;

import android.content.Context;
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

    public DishesAdapter(Context context, List<DishDetails> list) {
        this.context = context;
        this.list = list;
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


        holder.dec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = Integer.parseInt(holder.counter.getText().toString());
                if (count != 0)
                    count--;

                holder.counter.setText(Integer.toString(count));

                Toast.makeText(context , list.get(holder.getAdapterPosition()).getName()+" Decremented" , Toast.LENGTH_SHORT).show();

            }
        });


        holder.inc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int count = Integer.parseInt(holder.counter.getText().toString());
                count++;
                holder.counter.setText(Integer.toString(count));

               Toast.makeText(context , list.get(holder.getAdapterPosition()).getName()+" Incremented" , Toast.LENGTH_SHORT).show();


            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class dishesHolder extends RecyclerView.ViewHolder {

        ImageView dishImage;
        TextView dishName , counter;
        Button inc , dec;


        public dishesHolder(@NonNull View itemView) {
            super(itemView);

            dishName = itemView.findViewById(R.id.dish_name);
            dishImage = itemView.findViewById(R.id.dish_image);
            counter = itemView.findViewById(R.id.num);
            inc = itemView.findViewById(R.id.inc);
            dec = itemView.findViewById(R.id.dec);

        }
    }

}
