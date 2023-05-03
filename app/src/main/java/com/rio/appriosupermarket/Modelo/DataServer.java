package com.rio.appriosupermarket.Modelo;

public class DataServer {
    String ipAdress;
    String Localidad;
    String Impresora;
    public DataServer(int localidad){
        String[] loca = {/*Inside*/"0001",
                /*Playa el angel*/"0104",
                /*Juan Bautista*/ "0101",
                /*Traki*/ "0102",
                /*Centro Distribucion*/ "0103"};
        String[] Impre = {
                "hablametest","Playa El Angel","Juan Bautista","Traki","CD"
        };

        String[] ip = {/* 0 Inside"http://100.100.2.167:8080/api/",*/"http://100.100.2.131:8300/api/",
                /* 1 Playa el angel*/"http://10.10.21.2:8080/api/",
                /* 2 Juan Bautista*/ "http://10.10.0.77:8080/api/",
                /* 3 Traki*/ "http://10.10.10.4:8080/api/",
                /* 4 Centro Distribucion*/ "http://100.100.4.132:8080/api/"};
        String[] picture = {
                "logo_fondo.jpg", "logo_ply.jpg" , "logo_jb.jpg", "logo_tk.jpg", "logo_fondo.jpg"
        };
        this.ipAdress = ip[localidad];
        this.Localidad = loca[localidad];
        this.Impresora = Impre[localidad];
    }

    public String getSede(){
        return Impresora;
    }

    public String getIpAdress(){
        return ipAdress;
    }

    public String getLocalidad(){
        return  Localidad;
    }

    public void setIpAdress(String ip){
        ipAdress = ip;
    }

    public String getImpresora() {
        return "hablametest";
    }

    public String getImpresoraJB1() {
        return "JB001";
    }
    public String getImpresoraTraki1() {
        return "Traki001";
    }
    public String getImpresoraPlaya1() {
        return "Playa001";
    }

    public String getImpresoraPlaya2() {
        return "Playa002";
    }
    public String getImpresoraTest() {
        return "hablametest";
    }
}
