package com.example.temp.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class HotelDetails implements Parcelable {

    private String hotel_name;
    private String address;
    private String location;
    private String image;

    public HotelDetails() {
    }

    protected HotelDetails(Parcel in) {
        hotel_name = in.readString();
        address = in.readString();
        location = in.readString();
        image = in.readString();
    }

    public static final Creator<HotelDetails> CREATOR = new Creator<HotelDetails>() {
        @Override
        public HotelDetails createFromParcel(Parcel in) {
            return new HotelDetails(in);
        }

        @Override
        public HotelDetails[] newArray(int size) {
            return new HotelDetails[size];
        }
    };

    public String getHotel_name() {
        return hotel_name;
    }

    public void setHotel_name(String hotel_name) {
        this.hotel_name = hotel_name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(hotel_name);
        dest.writeString(address);
        dest.writeString(location);
        dest.writeString(image);
    }
}
