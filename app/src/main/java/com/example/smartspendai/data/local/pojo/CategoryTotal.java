package com.example.smartspendai.data.local.pojo;

/**
 * NOT a @Entity — this is just a plain result shape for a GROUP BY query.
 * Room maps query columns to fields by NAME, so these field names must
 * exactly match the column aliases used in the @Query string.
 */
public class CategoryTotal {
    public String category;
    public double total;
}
