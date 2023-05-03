package com.rio.appriosupermarket.Modelo;

public class FormatImpres {
    public static String FormatoImpresion(Habladores hablador){
        String stringHablador="";
        if(hablador.getOferta().equals("1")){//Hay Oferta
            stringHablador = "! 0 200 200 210 1\n" +
                    "JOURNAL\n" +
                    "CENTER\n" +
                    "SETMAG 0 0" + "\n" +
                    "TEXT 7 0 0 0 " + hablador.getDescripcion() + "\n" +
                    "TEXT 4 0 0 15 Oferta" + "\n" +
                    "TEXT 4 1 0 40 " + hablador.getPrecioo() + "\n" +
                    "TEXT 7 0 0 130 Precio: "+ hablador.getPrecio() + "\n" +
                    "TEXT 0 1 0 160 " + hablador.getCodigo_barra() + "\n" +
                    //"TEXT 0 1 0 130 " + hablador.getCodigo_barra() + "\n" +
                    "TEXT 7 0 0 167 RioSupermarket\n" +
                    //"TEXT 0 1 0 165 Act." + hablador.getFecha()+" Imp. "+hablador.getFechaImpresion() + "\n" +
                    "SETMAG 1 2" + "\n" +
                    "TEXT 0 0 0 190 Act." + hablador.getFecha()+" Imp. "+hablador.getFechaImpresion() + "\n" +
                    "FORM\n" +
                    "PRINT";
        }else {

            stringHablador = "! 0 200 200 210 1\n" +
                    "JOURNAL\n" +
                    "CENTER\n" +
                    "SETMAG 0 0" + "\n" +
                    "TEXT 7 0 0 10 " + hablador.getDescripcion() + "\n" +
                    "TEXT 4 1 0 30 " + hablador.getPrecio() + "\n" +
                    "TEXT 0 1 0 130 " + hablador.getCodigo_barra() + "\n" +
                    "TEXT 7 0 0 140 RioSupermarket\n" +
                    //"TEXT 7 0 0 165 " + hablador.getFecha() + "\n" +
                    "SETMAG 1 2" + "\n" +
                    "TEXT 0 0 0 165 Act." + hablador.getFecha()+" Imp. "+hablador.getFechaImpresion() + "\n" +
                    //"TEXT 0 0 0 190 Impr. " + hablador.getFechaImpresion() + "\n" +
                    "FORM\n" +
                    "PRINT";
        }
        return stringHablador;
    }

    /*
            String stringHablador = "! 0 200 200 210 1\n" +
                "JOURNAL\n"+
                "CENTER\n" +
                "TEXT 7 0 0 15 "+hablador.getDescripcion()+"\n" +
                "TEXT 4 1 0 50 "+hablador.getPrecio()+"\n" +
                "TEXT 0 1 0 150 "+hablador.getCodigo_barra()+"\n" +
                "TEXT 7 0 0 165 RioSupermarket\n" +
                "TEXT 7 0 0 190 "+hablador.getFecha()+"\n" +
                "TEXT 7 0 0 195 Fecha Imp.:"+hablador.getFecha()+"\n" +
                "FORM\n" +
                "PRINT";*
    * */
}
