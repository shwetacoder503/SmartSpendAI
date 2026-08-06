package com.example.smartspendai.autodetect;

public class ParsedTransaction {
    public double amount;
    public String merchant;
    public boolean isDebit; // true = money went OUT (expense) — we only auto-add these, never credits/refunds

    public ParsedTransaction(double amount, String merchant, boolean isDebit) {
        this.amount = amount;
        this.merchant = merchant;
        this.isDebit = isDebit;
    }
}
