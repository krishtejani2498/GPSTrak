// UserLocation.java
package com.example.fftrak;

import android.os.Parcel;
import android.os.Parcelable;

public class UserLocation implements Parcelable {
    private String id;
    private double latitude;
    private double longitude;

    public UserLocation() {
        // Default constructor required for calls to DataSnapshot.getValue(UserLocation.class)
    }

    public UserLocation(String id, double latitude, double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    protected UserLocation(Parcel in) {
        id = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public static final Creator<UserLocation> CREATOR = new Creator<UserLocation>() {
        @Override
        public UserLocation createFromParcel(Parcel in) {
            return new UserLocation(in);
        }

        @Override
        public UserLocation[] newArray(int size) {
            return new UserLocation[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }
}
