package com.example.smartspendai.data.model;

/** Matches the backend's `ExpenseCreate` schema exactly (field names + types). */
public class ExpenseRequest {
    public String title;
    public double amount;
    public String category;
    public String payment_method;
    public String note;
    public long date_millis;

    public ExpenseRequest(String title, double amount, String category,
                           String paymentMethod, String note, long dateMillis) {
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.payment_method = paymentMethod;
        this.note = note;
        this.date_millis = dateMillis;
    }
}
