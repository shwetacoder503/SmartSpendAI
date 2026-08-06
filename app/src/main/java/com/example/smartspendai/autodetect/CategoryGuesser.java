package com.example.smartspendai.autodetect;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple, explainable keyword matching — deliberately NOT a trained model.
 * With only a merchant name (a few words) and ~9 possible categories, a
 * keyword lookup is exactly as accurate as a model would be here, without
 * needing any training data or introducing a black-box "why did it pick
 * this category" problem. Falls back to "Others" when nothing matches,
 * and the category is always editable by the user afterward anyway.
 */
public class CategoryGuesser {

    private static final Map<String, String> KEYWORD_TO_CATEGORY = new LinkedHashMap<>();
    static {
        KEYWORD_TO_CATEGORY.put("swiggy", "Food");
        KEYWORD_TO_CATEGORY.put("zomato", "Food");
        KEYWORD_TO_CATEGORY.put("dominos", "Food");
        KEYWORD_TO_CATEGORY.put("pizza", "Food");
        KEYWORD_TO_CATEGORY.put("restaurant", "Food");

        KEYWORD_TO_CATEGORY.put("uber", "Travel");
        KEYWORD_TO_CATEGORY.put("ola", "Travel");
        KEYWORD_TO_CATEGORY.put("rapido", "Travel");
        KEYWORD_TO_CATEGORY.put("irctc", "Travel");
        KEYWORD_TO_CATEGORY.put("indigo", "Travel");

        KEYWORD_TO_CATEGORY.put("amazon", "Shopping");
        KEYWORD_TO_CATEGORY.put("flipkart", "Shopping");
        KEYWORD_TO_CATEGORY.put("myntra", "Shopping");
        KEYWORD_TO_CATEGORY.put("ajio", "Shopping");

        KEYWORD_TO_CATEGORY.put("netflix", "Entertainment");
        KEYWORD_TO_CATEGORY.put("spotify", "Entertainment");
        KEYWORD_TO_CATEGORY.put("hotstar", "Entertainment");
        KEYWORD_TO_CATEGORY.put("bookmyshow", "Entertainment");
        KEYWORD_TO_CATEGORY.put("prime", "Entertainment");

        KEYWORD_TO_CATEGORY.put("electricity", "Bills");
        KEYWORD_TO_CATEGORY.put("airtel", "Bills");
        KEYWORD_TO_CATEGORY.put("jio", "Bills");
        KEYWORD_TO_CATEGORY.put("vodafone", "Bills");
        KEYWORD_TO_CATEGORY.put("recharge", "Bills");
        KEYWORD_TO_CATEGORY.put("broadband", "Bills");

        KEYWORD_TO_CATEGORY.put("pharmacy", "Medical");
        KEYWORD_TO_CATEGORY.put("apollo", "Medical");
        KEYWORD_TO_CATEGORY.put("hospital", "Medical");
        KEYWORD_TO_CATEGORY.put("medplus", "Medical");
    }

    public static String guess(String merchant) {
        if (merchant == null) return "Others";
        String lower = merchant.toLowerCase();
        for (Map.Entry<String, String> entry : KEYWORD_TO_CATEGORY.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "Others";
    }
}
