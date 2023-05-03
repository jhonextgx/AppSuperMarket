package com.rio.appriosupermarket.Service;

import com.rio.appriosupermarket.Modelo.Departamento;
import com.rio.appriosupermarket.Modelo.Habladores;
import com.rio.appriosupermarket.Modelo.LoginUser;
import com.rio.appriosupermarket.Modelo.Producto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface JsonPlaceHolderApi {
    @GET("habladores")
    Call<List<Habladores>> getHabladores();

    @GET("hablador/{codigo}")
    Call<List<Habladores>> getHablador(@Path("codigo") String codigo);

    @GET("hablador/producto/{Descripcion}")
    Call<List<Habladores>> getHabladoresDescripcion(@Path("Descripcion") String Descripcion);

    @GET("habladores/pendientes")
    Call<String> getHayHabladores();

    @GET("habladores/actualizar/producto/{Codigo}/user/{IdUser}/device/{IdDevice}")
    Call<List<Habladores>> setActualizarHablador(@Path("Codigo") String Codigo, @Path("IdUser") String IdUser, @Path("IdDevice") String IdDevice);

    @GET("habladores/departamento/{Departamento}")
    Call<List<Habladores>> getHabladoresDepartamentos(@Path("Departamento") String Departamento);

    @GET("habladores/impresion/datetime")
    Call<String> getHabladoresDatetime();

    @GET("existencia/producto/{Codigo}")
    Call<Producto> getExistenciaProducto(@Path("Codigo") String Codigo);

    @GET("departamento")
    Call<List<Departamento>> getDepartamentos();

    @FormUrlEncoded
    @POST("Login")
    Call<LoginUser> Login(@Field("Username") String Username,
                          @Field("Password") String Password,
                          @Field("Localidad") String Localidad);
}
