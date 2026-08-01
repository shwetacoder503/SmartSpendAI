package com.example.smartspendai.data.model;

/** Matches the backend's `ExpenseOut` schema exactly (field names + types). */
public class ExpenseResponse {
    public long id;
    public String title;
    public double amount;
    public String category;
    public String payment_method;
    public String note;
    public long date_millis;
}
