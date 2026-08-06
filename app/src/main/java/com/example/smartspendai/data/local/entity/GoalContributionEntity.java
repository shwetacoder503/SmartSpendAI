package com.example.smartspendai.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Progress on a goal is NOT stored as a single "current amount" field on
 * GoalEntity — instead it's the SUM of all contributions logged here.
 * This is the same pattern as bank ledgers: you never store "balance"
 * directly, you compute it from transactions, so it can never silently
 * drift out of sync with reality.
 */
@Entity(
        tableName = "goal_contributions",
        foreignKeys = @ForeignKey(
                entity = GoalEntity.class,
                parentColumns = "id",
                childColumns = "goal_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("goal_id")}
)
public class GoalContributionEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "goal_id")
    public long goalId;

    @ColumnInfo(name = "amount")
    public double amount;

    @ColumnInfo(name = "date_millis")
    public long dateMillis;

    @ColumnInfo(name = "note")
    public String note;
}
