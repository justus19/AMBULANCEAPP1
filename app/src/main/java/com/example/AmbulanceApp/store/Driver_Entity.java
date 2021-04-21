package com.example.AmbulanceApp.store;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "budget")

public class Driver_DAO {
        @PrimaryKey()
        private int id;
        @ColumnInfo(name = "name")
        private String name;
        @ColumnInfo(name = "phone")
        private String phone;
        @ColumnInfo(name = "car")
        private String car;

    public Driver_DAO(String name, String phone, String car) {
        this.name = name;
        this.phone = phone;
        this.car = car;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCar() {
        return car;
    }

    public void setCar(String car) {
        this.car = car;
    }
}
