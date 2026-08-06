package com.example.smartspendai.ui.goal;

import java.util.Calendar;

/**
 * WHY this is a plain calculation, not ML: estimating "when will I reach
 * this goal" from "how much have I saved so far, over how long" is just
 * arithmetic (rate = amount / time, then remaining / rate) — there's no
 * pattern here complex enough to need a trained model, and a plain formula
 * is instantly explainable to the user ("based on your average pace so far").
 */
public class GoalEstimator {

    /** @return estimated completion date in millis, or null if we don't have enough data to estimate yet. */
    public static Long estimateCompletionMillis(double targetAmount, double totalContributed, long createdDateMillis) {
        if (totalContributed >= targetAmount) {
            return System.currentTimeMillis(); // already reached
        }
        if (totalContributed <= 0) {
            return null; // no pace to estimate from yet
        }

        long monthsElapsed = Math.max(1, monthsBetween(createdDateMillis, System.currentTimeMillis()));
        double avgMonthlyContribution = totalContributed / monthsElapsed;
        if (avgMonthlyContribution <= 0) {
            return null;
        }

        double remaining = targetAmount - totalContributed;
        double monthsNeeded = remaining / avgMonthlyContribution;

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, (int) Math.ceil(monthsNeeded));
        return cal.getTimeInMillis();
    }

    private static long monthsBetween(long startMillis, long endMillis) {
        long diffDays = (endMillis - startMillis) / (24L * 60 * 60 * 1000);
        return Math.max(1, diffDays / 30);
    }
}
