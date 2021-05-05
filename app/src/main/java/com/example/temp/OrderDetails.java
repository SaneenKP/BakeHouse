package com.example.temp;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

@SuppressWarnings("serial")
public class OrderDetails implements Parcelable {

    private String total;
    private String date;
    private String time;
    private String latitude;
    private String longitude;
    private String name;
    private String houseNo;
    private String houseName;
    private String landmark;
    private String street;
    private String placedIndex;
    private String confirmedIndex;
    private String pickupIndex;
    private String deliveryIndex;
    private String userId;
    private String COD;
    private String POD;
    private String transactionId;
    private String hotelId;

    public String getHotelId() {
        return hotelId;
    }

    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }

    public OrderDetails() {

    }


    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<OrderDetails> CREATOR = new Creator<OrderDetails>() {
        @Override
        public OrderDetails createFromParcel(Parcel in) {
            return new OrderDetails(in);
        }

        @Override
        public OrderDetails[] newArray(int size) {
            return new OrderDetails[size];
        }
    };

    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHouseNo() {
        return houseNo;
    }

    public void setHouseNo(String houseNo) {
        this.houseNo = houseNo;
    }

    public String getHouseName() {
        return houseName;
    }

    public void setHouseName(String houseName) {
        this.houseName = houseName;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getPlacedIndex() {
        return placedIndex;
    }

    public void setPlacedIndex(String placedIndex) {
        this.placedIndex = placedIndex;
    }

    public String getConfirmedIndex() {
        return confirmedIndex;
    }

    public void setConfirmedIndex(String confirmedIndex) {
        this.confirmedIndex = confirmedIndex;
    }

    public String getPickupIndex() {
        return pickupIndex;
    }

    public void setPickupIndex(String pickupIndex) {
        this.pickupIndex = pickupIndex;
    }

    public String getDeliveryIndex() {
        return deliveryIndex;
    }

    public void setDeliveryIndex(String deliveryIndex) {
        this.deliveryIndex = deliveryIndex;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCOD() {
        return COD;
    }

    public void setCOD(String COD) {
        this.COD = COD;
    }

    public String getPOD() {
        return POD;
    }

    public void setPOD(String POD) {
        this.POD = POD;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }


    public void writeToParcel(Parcel dest, int flags){
        //write all properties to the parcle
        dest.writeString(total);
        dest.writeString(date);
        dest.writeString(time);
        dest.writeString(latitude);
        dest.writeString(longitude);
        dest.writeString(name);
        dest.writeString(houseNo);
        dest.writeString(houseName);
        dest.writeString(landmark);
        dest.writeString(street);
        dest.writeString(placedIndex);
        dest.writeString(confirmedIndex);
        dest.writeString(pickupIndex);
        dest.writeString(deliveryIndex);
        dest.writeString(userId);
        dest.writeString(COD);
        dest.writeString(POD);
        dest.writeString(transactionId);
        dest.writeString(hotelId);
    }

    //constructor used for parcel
    public OrderDetails(Parcel parcel){
        //read and set saved values from parcel
        total = parcel.readString();
        date = parcel.readString();
        time = parcel.readString();
        latitude = parcel.readString();
        longitude = parcel.readString();
        name = parcel.readString();
        houseNo = parcel.readString();
        houseName = parcel.readString();
        landmark = parcel.readString();
        street = parcel.readString();
        placedIndex = parcel.readString();
        confirmedIndex = parcel.readString();
        pickupIndex = parcel.readString();
        deliveryIndex = parcel.readString();
        userId = parcel.readString();
        COD = parcel.readString();
        POD = parcel.readString();
        transactionId = parcel.readString();
        hotelId = parcel.readString();
    }



}
