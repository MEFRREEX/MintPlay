package com.bestaford.mintplay.util;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerData {

    private final boolean registered;
    private String password;
    private String address;
    private String uuid;
    private double x;
    private double y;
    private double z;

    public PlayerData(ResultSet resultSet) throws SQLException {
        this.registered = true;
        this.password = resultSet.getString("password");
        this.address = resultSet.getString("address");
        this.uuid = resultSet.getString("uuid");
        this.x = resultSet.getDouble("x");
        this.y = resultSet.getDouble("y");
        this.z = resultSet.getDouble("z");
    }

    public PlayerData() {
        this.registered = false;
    }

    public boolean isRegistered() {
        return registered;
    }

    public String getPassword() {
        return password;
    }

    public String getAddress() {
        return address;
    }

    public String getUniqueId() {
        return uuid;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return Math.ceil(y);
    }

    public double getZ() {
        return z;
    }
}