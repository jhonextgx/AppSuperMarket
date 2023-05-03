package com.rio.appriosupermarket.Modelo;

public class Habladores {
    private String Codigo;
    private String Codigo_barra;
    private String Descripcion;
    private String Precio;
    private String Departamento;
    private Integer PorImprimir;
    private String Fecha;
    private String Oferta;
    private String Precioo;
    private String FechHorImpresion;

    public Habladores(String Codigo, String Codigo_barra, String Descripcion, String Precio, String Departamento, Integer PorImprimir, String Fecha, String fechHorImpresion, String Precioo, String Oferta){
        this.Codigo = Codigo;
        this.Codigo_barra = Codigo_barra;
        this.Descripcion = Descripcion;
        this.Precio = Precio;
        this.Departamento = Departamento;
        this.PorImprimir = PorImprimir;
        this.Fecha = Fecha;
        this.Oferta = Oferta;
        this.Precioo = Precioo;
        this.FechHorImpresion = fechHorImpresion;
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

    public String  getDepartamento(){
        return Departamento;
    }

    public Integer  getPorImprimir(){
        return PorImprimir;
    }

    public String  getFecha(){
        return Fecha;
    }

    public String getFechaImpresion() {
        return FechHorImpresion;
    }

    public String  getPrecio(){
        return Precio;
    }

    public String getOferta() {
        return Oferta;
    }

    public String getPrecioo() {
        return Precioo;
    }

    public void setFechaImpresion(String fechaImpresion) {
        FechHorImpresion = fechaImpresion;
    }
}

