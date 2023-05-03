package com.rio.appriosupermarket.Service;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

  public class MensajeService {
    public static void ShowMensaje(Context context, String Msj){
        CharSequence text = Msj;
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, text, duration);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, -150);
        toast.show();
    }
}
