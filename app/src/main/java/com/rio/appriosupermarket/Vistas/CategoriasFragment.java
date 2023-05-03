package com.rio.appriosupermarket.Vistas;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.rio.appriosupermarket.Modelo.DataServer;
import com.rio.appriosupermarket.Modelo.Departamento;
import com.rio.appriosupermarket.R;
import com.rio.appriosupermarket.Service.JsonPlaceHolderApi;
import com.rio.appriosupermarket.Service.MensajeService;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CategoriasFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CategoriasFragment extends Fragment {

    ListView listView;
    List<Departamento> departamentosList;
    DataServer dataServer;

    public CategoriasFragment(DataServer dataServer) {
        // Required empty public constructor
        this.dataServer = dataServer;
    }

    public static CategoriasFragment newInstance(DataServer dataServer) {
        CategoriasFragment fragment = new CategoriasFragment(dataServer);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_categorias, container, false);

        listView = (ListView) root.findViewById(R.id.listView);
        /*Eventos ListView */
        listView.setClickable(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                // Realiza lo que deseas, al recibir clic en el elemento de tu listView determinado por su posicion.
                try {
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new HabladoresFragment(dataServer, departamentosList.get(position).getDescripcion())).commit();
                }catch (Exception e){

                }
            }
        });


        return root;
    }

    //La vista de layout ha sido creada y ya está disponible
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
            getDepartamentos();
    }

    private void getDepartamentos(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(dataServer.getIpAdress())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JsonPlaceHolderApi jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);

        Call<List<Departamento>> call = jsonPlaceHolderApi.getDepartamentos();
        call.enqueue(new Callback<List<Departamento>>() {
            @Override
            public void onResponse(Call<List<Departamento>> call, Response<List<Departamento>> response) {

                if(!response.isSuccessful()){
                    return;
                }
                departamentosList = response.body();

                //Creating an String array for the ListView
                String[] heroes = new String[departamentosList.size()];
                //looping through all the heroes and inserting the names inside the string array
                for (int i = 0; i < departamentosList.size(); i++) {
                    heroes[i] = departamentosList.get(i).getDescripcion();
                }
                //displaying the string array into listview
                listView.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, heroes));
            }
            @Override
            public void onFailure(Call<List<Departamento>> call, Throwable t) {
                MensajeService.ShowMensaje(getContext(),"Error! "+t.getMessage());
            }
        });

    }//end getDepartamentos
}