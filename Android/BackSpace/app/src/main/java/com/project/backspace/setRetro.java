package com.project.backspace;

import retrofit2.Call;
import retrofit2.http.GET;


public interface setRetro{
    @GET("/test")
    Call<String> getImg();
}
