package com.rio.appriosupermarket.Modelo;

public class Producto {
    private String Codigo;
    private String Codigo_barra;
    private String Descripcion;
    private String Cantidad;
    private String Fecha;

    public Producto(String Codigo, String Codigo_barra, String Descripcion, String Cantidad, String Fecha){
        this.Codigo = Codigo;
        this.Codigo_barra = Codigo_barra;
        this.Descripcion = Descripcion;
        this.Cantidad = Cantidad;
        this.Fecha = Fecha;
    }

    public String getCodigo(){
        return Codigo;
    }
    public String getCodigo_barra(){
        return Codigo_barra;
    }
    public String  getDescripcion(){
        return Descripcion;
    }
    public String  getCantidad(){
        return Cantidad;
    }
    public String  getFecha(){
        return Fecha;
    }
}
