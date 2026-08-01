package com.example.smartspendai.data.local.pojo;

public class DayTotal {
    /** 0 = Sunday, 1 = Monday, ... 6 = Saturday (SQLite's strftime('%w') convention). */
    public int dow;
    public double total;
}
