package com.example.smartspendai.autodetect;

import androidx.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * WHY this exists: every UPI/bank app phrases its "payment successful"
 * notification slightly differently. This class tries app-specific regex
 * first (more reliable when it matches), then falls back to a generic
 * pattern that works across most bank/UPI notifications.
 *
 * IMPORTANT LIMITATION (be upfront about this in interviews): notification
 * wording changes whenever these apps update, so these patterns need
 * occasional maintenance. There's no officially documented format — this
 * is reverse-engineered from real-world notification text, same as every
 * other expense-tracker app that does this (Walnut, Fold, etc.).
 */
public class UpiNotificationParser {

    // Matches "₹500", "₹500.50", "Rs.500", "Rs 500", "INR 500"
    private static final Pattern AMOUNT_PATTERN =
            Pattern.compile("(?:₹|Rs\\.?|INR)\\s?([\\d,]+(?:\\.\\d{1,2})?)", Pattern.CASE_INSENSITIVE);

    // Words that indicate money LEAVING the account (what we care about for expenses).
    private static final Pattern DEBIT_KEYWORDS =
            Pattern.compile("\\b(paid|debited|sent|payment of)\\b", Pattern.CASE_INSENSITIVE);

    // Words that indicate money ARRIVING (refund/received) — we deliberately ignore these.
    private static final Pattern CREDIT_KEYWORDS =
            Pattern.compile("\\b(received|credited|refund(?:ed)?|added to)\\b", Pattern.CASE_INSENSITIVE);

    // Captures the merchant name after "to X", "at X", or "to VPA X" style phrasing.
    private static final Pattern MERCHANT_PATTERN =
            Pattern.compile("(?:to|at)\\s+([A-Za-z0-9@.\\s]{2,40}?)(?:\\s+(?:was|is|on|successful|from|using)\\b|[.,]|$)",
                    Pattern.CASE_INSENSITIVE);

    @Nullable
    public static ParsedTransaction parse(String packageName, String title, String text) {
        if (text == null) return null;
        String combined = (title != null ? title + " " : "") + text;

        if (!isFromSupportedApp(packageName)) return null;

        boolean isDebit = DEBIT_KEYWORDS.matcher(combined).find();
        boolean isCredit = CREDIT_KEYWORDS.matcher(combined).find();

        // If neither keyword is found, or if it looks like BOTH (ambiguous), skip it —
        // better to miss a transaction than silently mis-categorize a refund as a spend.
        if (!isDebit || isCredit) return null;

        Matcher amountMatcher = AMOUNT_PATTERN.matcher(combined);
        if (!amountMatcher.find()) return null;

        double amount;
        try {
            amount = Double.parseDouble(amountMatcher.group(1).replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
        if (amount <= 0) return null;

        String merchant = "Unknown";
        Matcher merchantMatcher = MERCHANT_PATTERN.matcher(combined);
        if (merchantMatcher.find()) {
            merchant = merchantMatcher.group(1).trim();
            // Bank notifications often say "to VPN swiggy@ybl" — "VPN" here means
            // "Virtual Payment Address", not a useful part of the merchant name.
            if (merchant.regionMatches(true, 0, "VPN ", 0, 4)) {
                merchant = merchant.substring(4).trim();
            }
            // UPI IDs often look like "swiggy@ybl" — keep just the readable part before '@'.
            if (merchant.contains("@")) {
                merchant = merchant.substring(0, merchant.indexOf('@'));
            }
        }

        return new ParsedTransaction(amount, merchant, true);
    }

    private static boolean isFromSupportedApp(String packageName) {
        if (packageName == null) return false;
        // GPay, PhonePe, Paytm — the 3 dominant UPI apps in India.
        // Bank apps vary too much by bank to whitelist individually here,
        // but this generic parser will still work for many of them if you
        // add their package names to this list later.
        return packageName.equals("com.google.android.apps.nbu.paisa.user")  // Google Pay
                || packageName.equals("com.phonepe.app")                     // PhonePe
                || packageName.equals("net.one97.paytm");                    // Paytm
    }

    /** Public wrapper used only for debug logging in the service — lets us tell
     * "wrong app" apart from "right app but regex didn't match" while troubleshooting. */
    public static boolean isFromSupportedAppPublic(String packageName) {
        return isFromSupportedApp(packageName);
    }
}
