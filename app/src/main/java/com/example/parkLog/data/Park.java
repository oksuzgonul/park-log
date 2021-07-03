package com.example.parkLog.data;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "park")
public class Park implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "park_location_name")
    private String parkLocationName;
    @ColumnInfo(name = "park_latitude")
    private Double parkLatitude;
    @ColumnInfo(name = "park_longitude")
    private Double parkLongitude;
    @ColumnInfo(name = "park_snippet")
    private String parkSnippet;
    private String floor;
    private String lot;
    @ColumnInfo(name = "valid_time")
    private Date validTime;
    @ColumnInfo(name = "park_time")
    private Date parkTime;
    @ColumnInfo(name = "picture_location")
    private String pictureLocation;
    @ColumnInfo(name = "parked_car_id")
    private int parkedCarId;
    @ColumnInfo(name = "park_place_id")
    private String parkPlaceId;
    @ColumnInfo(name = "park_end_time")
    private Date parkEndTime;

    @Ignore
    public Park(String parkLocationName, @Nullable String floor, @Nullable String lot,
                Date validTime, Date parkTime, @Nullable String pictureLocation,
                int parkedCarId, Double parkLatitude, Double parkLongitude,
                String parkSnippet, String parkPlaceId) {
        this.parkLocationName = parkLocationName;
        this.floor = floor;
        this.lot = lot;
        this.validTime = validTime;
        this.parkTime = parkTime;
        this.pictureLocation = pictureLocation;
        this.parkedCarId = parkedCarId;
        this.parkLatitude = parkLatitude;
        this.parkLongitude = parkLongitude;
        this.parkSnippet = parkSnippet;
        this.parkPlaceId = parkPlaceId;
    }

    public Park(int id, String parkLocationName, @Nullable String floor, @Nullable String lot,
                Date validTime, Date parkTime, @Nullable String pictureLocation,
                int parkedCarId, Double parkLatitude,  Double parkLongitude,
                String parkSnippet, String parkPlaceId) {
        this.id = id;
        this.parkLocationName = parkLocationName;
        this.floor = floor;
        this.lot = lot;
        this.validTime = validTime;
        this.parkTime = parkTime;
        this.pictureLocation = pictureLocation;
        this.parkedCarId = parkedCarId;
        this.parkLatitude = parkLatitude;
        this.parkLongitude = parkLongitude;
        this.parkSnippet = parkSnippet;
        this.parkPlaceId = parkPlaceId;
    }

    public int getId() { return id; }
    public String getParkLocationName() { return parkLocationName; }
    public String getParkSnippet() { return parkSnippet; }
    public String getFloor() { return floor; }
    public String getLot() { return lot; }
    public Date getValidTime() { return validTime; }
    public Date getParkTime() { return parkTime; }
    public String getPictureLocation() { return pictureLocation; }
    public Double getParkLatitude() { return parkLatitude; }
    public Double getParkLongitude() { return parkLongitude; }
    public int getParkedCarId() { return parkedCarId; }
    public String getParkPlaceId() { return parkPlaceId;}
    public Date getParkEndTime() { return parkEndTime; }
    public void setId(int id) { this.id = id; }
    public void setParkEndTime(Date parkEndTime) { this.parkEndTime = parkEndTime; }
}
