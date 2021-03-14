package com.example.temp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FoodsViewAdapter extends BaseAdapter {

    private android.content.Context context;
    private LayoutInflater inflater;
    private String[] foods;
    private int[] foodImages;

    public FoodsViewAdapter(Context context, String[] foods, int[] foodImages) {

        inflater = LayoutInflater.from(context);
        this.context = context;
        this.foods = foods;
        this.foodImages = foodImages;

    }

    @Override
    public int getCount() {
        return foods.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = inflater.inflate(R.layout.food_view , null);
        TextView foodName = convertView.findViewById(R.id.food_name);
        ImageView foodImage = convertView.findViewById(R.id.food_image);

        foodName.setText(foods[position]);
        foodImage.setImageResource(foodImages[position]);
        return convertView;
    }
}
