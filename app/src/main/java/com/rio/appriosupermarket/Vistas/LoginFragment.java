package com.rio.appriosupermarket.Vistas;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.rio.appriosupermarket.MainActivity;
import com.rio.appriosupermarket.Modelo.DataServer;
import com.rio.appriosupermarket.Modelo.LoginUser;
import com.rio.appriosupermarket.R;
import com.rio.appriosupermarket.Service.ImpresionService;
import com.rio.appriosupermarket.Service.JsonPlaceHolderApi;
import com.rio.appriosupermarket.Service.MensajeService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private EditText Username;
    private EditText Password;
    private LoginUser responseLogin;//respuesta de api-sesion
    private DataServer dataServer;//data del servidor
    private ImpresionService impresionService;

    //Usuarios
    SharedPreferences prefs;

    public LoginFragment(DataServer dataServer) {
        this.dataServer = dataServer;
    }

    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance(DataServer dataServer) {
        return new LoginFragment(dataServer);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_login, container, false);

        //elementos de la vista
        Username = (EditText) view.findViewById(R.id.txtUser);
        Password = (EditText) view.findViewById(R.id.txtPass);
        final Button btnLogin = (Button) view.findViewById(R.id.btnLogin);

        //Evento iniciar Sesion
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Username.getText().toString().equalsIgnoreCase("")  || Password.getText().toString().equalsIgnoreCase("") ){
                    MensajeService.ShowMensaje(getContext(),"Error: Ingrese su usuario y contraseña");
                }else {
                    IniciarSesion(Username.getText().toString(), Password.getText().toString());
                }
            }
        });
        return view;
    }

    private void IniciarSesion(String User, String Pwd){
        try{
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(dataServer.getIpAdress())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                JsonPlaceHolderApi jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);

                Call<LoginUser> call = jsonPlaceHolderApi.Login(User, Pwd, dataServer.getLocalidad());

                call.enqueue(new Callback<LoginUser>() {
                    @Override
                    public void onResponse(Call<LoginUser> call, Response<LoginUser> response) {
                        if (!response.isSuccessful()) {
                            MensajeService.ShowMensaje(getContext(),"Error Verifique sus datos de Acceso: " + response.message());
                            return;
                        }
                        responseLogin = response.body();
                        if (responseLogin.getCodigoUser() != "") {
                            //Username.setText("");
                            Password.setText("");
                            //----------------Se guarda Usuario Conectado
                            prefs = getContext().getSharedPreferences("shared_login_data",   Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("IdUser", responseLogin.getCodigoUser());
                            editor.commit();
                            /* se desbloquea el menu */
                            ((MainActivity) getActivity()).desbloquearMenu();
                            /* Se asigna nombre de usuario */
                            ((MainActivity) getActivity()).changeNavHeaderData(responseLogin.getNombreUser());
                            /* Envio al Home */
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                    new HomeFragment(dataServer)).commit();
                        } else {
                            MensajeService.ShowMensaje(getContext(),"Error Verifique sus datos de Acceso");
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginUser> call, Throwable t) {
                        MensajeService.ShowMensaje(getContext(),"Error! " + t.getMessage());
                    }
                });
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}