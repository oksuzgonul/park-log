package com.example.parkLog.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "car")
public class Car implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "car_name")
    private String carName;
    @ColumnInfo(name = "license_plate")
    private String licensePlate;
    @ColumnInfo(name = "car_owner")
    private String carOwner;
    @ColumnInfo(name = "parked_state")
    private boolean parkedState;
    @ColumnInfo(name = "parked_time")
    private Date parkedTime;

    @Ignore
    public Car(String carName, @Nullable String licensePlate,
               @Nullable String carOwner, boolean parkedState) {
        this.carName = carName;
        this.licensePlate = licensePlate;
        this.carOwner = carOwner;
        this.parkedState = parkedState;
    }

    public Car(int id, String carName, @Nullable String licensePlate,
               @Nullable String carOwner, boolean parkedState) {
        this.id = id;
        this.carName = carName;
        this.licensePlate = licensePlate;
        this.carOwner = carOwner;
        this.parkedState = parkedState;
    }

    public int getId() { return id; }
    public String getCarName() { return carName; }
    public String getLicensePlate() { return licensePlate; }
    public String getCarOwner() { return carOwner; }
    public boolean getParkedState() { return parkedState; }
    public Date getParkedTime() { return parkedTime; }
    public void setParkedState(boolean parkedState) { this.parkedState = parkedState; }
    public void setId(int id) { this.id = id; }
    public void setParkedTime(Date parkedTime) { this.parkedTime = parkedTime; }

    @NonNull
    @Override
    public String toString() {
        return carName;
    }
}
