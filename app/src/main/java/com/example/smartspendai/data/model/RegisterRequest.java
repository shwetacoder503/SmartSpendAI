package com.example.smartspendai.data.model;

/**
 * Field names here MUST exactly match the FastAPI Pydantic schema
 * (schemas.UserRegister) since Gson serializes by field name.
 */
public class RegisterRequest {
    private final String name;
    private final String email;
    private final String password;
    private final Integer monthly_income;

    public RegisterRequest(String name, String email, String password, Integer monthlyIncome) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.monthly_income = monthlyIncome;
    }
}
