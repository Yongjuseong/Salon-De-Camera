package com.project.backspace;

import retrofit2.Call;
import retrofit2.http.GET;

public interface getXY {
    @GET("/trans")
    Call<String> getXY();
}
