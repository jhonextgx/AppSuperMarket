package com.rio.appriosupermarket.Service;

import android.content.Context;

import com.rio.appriosupermarket.Modelo.DataServer;
import com.rio.appriosupermarket.Modelo.Producto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DateTime {
    private String hDateTime;
    /*** Metodos de API ****/
    public DateTime(DataServer dataServer, final Context context){
        this.getDateTime(dataServer, context);
    }//end getExistenciaProducto

    public void getDateTime(DataServer dataServer, final Context context){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(dataServer.getIpAdress())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JsonPlaceHolderApi jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);

        Call<String> call = jsonPlaceHolderApi.getHabladoresDatetime();
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                if(!response.isSuccessful()){
                    return;
                }
                hDateTime = response.body();
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {

                MensajeService.ShowMensaje(context,"Error! "+t.getMessage());

            }
        });
    }

    public String gethDateTime() {
        return hDateTime;
    }
}
