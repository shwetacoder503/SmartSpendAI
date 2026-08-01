package com.example.smartspendai.data.remote;

import com.example.smartspendai.data.model.ExpenseRequest;
import com.example.smartspendai.data.model.ExpenseResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * No @Header("Authorization", ...) needed on any of these — RetrofitClient's
 * authInterceptor already attaches the saved JWT to every request.
 */
public interface ExpenseApiService {

    @POST("expenses")
    Call<ExpenseResponse> createExpense(@Body ExpenseRequest request);

    @GET("expenses")
    Call<List<ExpenseResponse>> listExpenses();

    @PUT("expenses/{id}")
    Call<ExpenseResponse> updateExpense(@Path("id") long remoteId, @Body ExpenseRequest request);

    @DELETE("expenses/{id}")
    Call<Void> deleteExpense(@Path("id") long remoteId);
}
