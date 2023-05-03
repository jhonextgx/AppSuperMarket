package com.rio.appriosupermarket.Vistas;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rio.appriosupermarket.MiBarraActivity;
import com.rio.appriosupermarket.Modelo.DataServer;
import com.rio.appriosupermarket.Modelo.Habladores;
import com.rio.appriosupermarket.Modelo.Producto;
import com.rio.appriosupermarket.R;
import com.rio.appriosupermarket.Service.JsonPlaceHolderApi;
import com.rio.appriosupermarket.Service.MensajeService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class InventarioFragment extends Fragment {
    private DataServer dataServer;
    //Data Api
    Producto postsList;
    private TextView tvCodigoLeido;
    private TextView tvCantLeido;
    private TextView tvDescripLeido;
    private TextView tvBarraLeido;
    private TextView tvFechaLeido;

    //camara
    private static final int CODIGO_PERMISOS_CAMARA = 1, CODIGO_INTENT = 2;
    private boolean permisoCamaraConcedido = false, permisoSolicitadoDesdeBoton = false;

    public InventarioFragment(DataServer dataServer) {
        this.dataServer = dataServer;
    }


    // TODO: Rename and change types and number of parameters
    public static InventarioFragment newInstance(DataServer dataServer) {
        InventarioFragment fragment = new InventarioFragment(dataServer);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        verificarYPedirPermisosDeCamara();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_inventario, container, false);

        tvCodigoLeido = root.findViewById(R.id.tvCodigoLeidoInv);
        tvFechaLeido = root.findViewById(R.id.idFechaScanInv);
        tvBarraLeido = root.findViewById(R.id.idCodigoBarraScanInv);
        tvCantLeido = root.findViewById(R.id.idCantidadScanInv);
        tvDescripLeido = root.findViewById(R.id.idDescripcionScanInv);
        FloatingActionButton fab = root.findViewById(R.id.btnfabescanInv);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!permisoCamaraConcedido) {
                    MensajeService.ShowMensaje(getContext(), "Por favor permite que la app acceda a la cámara");
                    permisoSolicitadoDesdeBoton = true;
                    verificarYPedirPermisosDeCamara();
                    return;
                }
                escanear();
            }
        });
        return root;
    }

    //---Escaneo
    private void escanear() {
        Intent i = new Intent(getContext() , MiBarraActivity.class);
        startActivityForResult(i, CODIGO_INTENT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODIGO_INTENT) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    String codigo = data.getStringExtra("codigo");
                    tvCodigoLeido.setText("COD. "+codigo);
                    //SE Imprime aqui
                    getExistenciaProducto(codigo);
                }
            }
        }
    }

    /*** Metodos de API ****/
    private void getExistenciaProducto(String codigo){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(dataServer.getIpAdress())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JsonPlaceHolderApi jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);

        Call<Producto> call = jsonPlaceHolderApi.getExistenciaProducto(codigo);
        call.enqueue(new Callback<Producto>() {
            @Override
            public void onResponse(Call<Producto> call, Response<Producto> response) {

                if(!response.isSuccessful()){
                    return;
                }
                postsList = response.body();
                tvCodigoLeido.setText("Buscando...");
                try {

                    tvCodigoLeido.setText("Producto Encontrado!!");
                    tvBarraLeido.setText(postsList.getCodigo_barra());
                    tvDescripLeido.setText(postsList.getDescripcion());
                    tvCantLeido.setText("Cant.: "+postsList.getCantidad());
                    tvFechaLeido.setText(postsList.getFecha());

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            @Override
            public void onFailure(Call<Producto> call, Throwable t) {

                MensajeService.ShowMensaje(getContext(),"Error! "+t.getMessage());

            }
        });

    }//end getExistenciaProducto

    /*-- Metodos de Camara --*/
    private void verificarYPedirPermisosDeCamara() {
        int estadoDePermiso = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);
        if (estadoDePermiso == PackageManager.PERMISSION_GRANTED) {
            // En caso de que haya dado permisos ponemos la bandera en true
            // y llamar al método
            permisoCamaraConcedido = true;
        } else {
            // Si no, pedimos permisos. Ahora mira onRequestPermissionsResult
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    CODIGO_PERMISOS_CAMARA);
        }
    }

    private void permisoDeCamaraDenegado() {
        // Esto se llama cuando el usuario hace click en "Denegar" o
        // cuando lo denegó anteriormente
        MensajeService.ShowMensaje( getContext(),"No puedes escanear si no das permiso");
    }
}