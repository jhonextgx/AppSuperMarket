package com.rio.appriosupermarket.Vistas;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rio.appriosupermarket.Adapter.AdapterDatos;
import com.rio.appriosupermarket.Modelo.DataServer;
import com.rio.appriosupermarket.Modelo.FormatImpres;
import com.rio.appriosupermarket.Modelo.Habladores;
import com.rio.appriosupermarket.R;
import com.rio.appriosupermarket.Service.DateTime;
import com.rio.appriosupermarket.Service.JsonPlaceHolderApi;
import com.rio.appriosupermarket.Service.MensajeService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.Set;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HabladoresFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HabladoresFragment extends Fragment implements AdapterDatos.OnDatosListener {
    //variables de impresion
    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket bluetoothSocket;
    BluetoothDevice bluetoothDevice;

    OutputStream outputStream;
    InputStream inputStream;
    Thread thread;

    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;

    //variables de datos globales
    private DataServer dataServer;
    private AdapterDatos adapterDatos;

    private RecyclerView recycler;//vista del listado
    private EditText textBuscador;//buscardor del listado

    private String idUser, departamento/*Departamento a buscar*/, DescripBuscar;
    private DateTime horaFecha;
    private ArrayList<Habladores> listDatos;//carga los datos para el recycler
    private List<Habladores> postsList;//items desde api
    private ArrayList<Habladores> filtrarLista;//items filtrados

    public HabladoresFragment(DataServer dataServer, String departamento) {
        this.dataServer = dataServer;
        this.departamento = departamento;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @DataServer dataServer Parameter 1.
     * @return A new instance of fragment HabladoresFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HabladoresFragment newInstance(DataServer dataServer, String departamento) {
        HabladoresFragment fragment = new HabladoresFragment(dataServer, departamento);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Fecha Actual
        horaFecha = new DateTime(dataServer, getContext());
        //Usuario conectado
        SharedPreferences prefs = getContext().getSharedPreferences("shared_login_data",   Context.MODE_PRIVATE);
        idUser = prefs.getString("IdUser","");//usuario en linea
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_habladores, container, false);

        //final Button btnConnect = (Button) root.findViewById(R.id.btnConnect);
        textBuscador = root.findViewById(R.id.etBuscador);
        recycler = (RecyclerView) root.findViewById(R.id.recyclerid);

        recycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,false));
        adapterDatos = new AdapterDatos(this);

        /*Eventos*/
        //click button dab imprimir listado completo
        final FloatingActionButton fab = root.findViewById(R.id.fabHabladores);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialogListHabla("Listado de Habladores", "Esta Segura(o) de imprimir el listado de Habladores?");
            }
        });

        //click button dab imprimir listado completo
        final FloatingActionButton fabBuscar = root.findViewById(R.id.fabBusHabladores);
        fabBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!DescripBuscar.equals(""))
                    buscarPorDescripcion(DescripBuscar);
            }
        });

        //change edittext buscador
        textBuscador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String changetest =  textBuscador.getText().toString();
                if(changetest.equals("")){
                    MensajeService.ShowMensaje(getContext(),"Buscando Habladores....");
                    if(departamento.equals("")){
                        getHabladores();//Cargando lista de habladores
                    }
                    else{
                        getHabladoresDepto(departamento);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                DescripBuscar = s.toString();
                filtrar(s.toString());
            }
        });

        return root;
    }

    //La vista de layout ha sido creada y ya está disponible
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Conectando Impresora
        try {
            MensajeService.ShowMensaje(getContext(),"Buscando Habladores....");
            if(departamento.equals("")){
                getHabladores();//Cargando lista de habladores
            }
            else{
                getHabladoresDepto(departamento);
            }
            FindBluetoothDevice();
            openBluetoothPrinter();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //El Fragment ha sido quitado de su Activity y ya no está disponible
    @Override
    public void onDetach() {
        super.onDetach();
        try {
            disconnectBT();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onHabladorClick(int position) throws IOException {
        try {
            horaFecha.getDateTime(dataServer, getContext());
            MensajeService.ShowMensaje(getContext(),"Imprimiendo Hablador....");
            Thread.sleep(100);
            Habladores hablador = (filtrarLista!=null && !textBuscador.getText().equals(""))?filtrarLista.get(position):postsList.get(position);
            hablador.setFechaImpresion(horaFecha.gethDateTime());
            if(printData(hablador)) {
                setActualizarHablador(hablador.getCodigo());
                MensajeService.ShowMensaje(getContext(),"Hablador: "+hablador.getCodigo()+" Fue Impreso y Actualizado");
            }else{
                MensajeService.ShowMensaje(getContext(),"Hablador No Actualizado");
            }
        }catch (Exception e){
            MensajeService.ShowMensaje(getContext(), e.getMessage());
        }
    }

    /* filtro del listado de habladores */
    public void filtrar(String texto){
        filtrarLista = new ArrayList<>();
        for(Habladores habladores: listDatos){
            if(habladores.getCodigo_barra().toLowerCase().contains(texto.toLowerCase()) || habladores.getDescripcion().toLowerCase().contains(texto.toLowerCase())){
                filtrarLista.add(habladores);
            }
        }
        adapterDatos.filtrar(filtrarLista);
    }

    /*-- Metodos hacia la API --*/
    /*** Busca Habladores por descripcion****/
    private void buscarPorDescripcion(String descripBuscar) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(dataServer.getIpAdress())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JsonPlaceHolderApi jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);

        Call<List<Habladores>> call = jsonPlaceHolderApi.getHabladoresDescripcion(descripBuscar);
        call.enqueue(new Callback<List<Habladores>>() {
            @Override
            public void onResponse(Call<List<Habladores>> call, Response<List<Habladores>> response) {

                if (!response.isSuccessful()) {
                    return;
                }

                postsList = response.body();
                listDatos = new ArrayList<Habladores>();
                for (int i = 0; i < postsList.size(); i++) {//se cargan los datos en el adapter
                    listDatos.add(new Habladores(postsList.get(i).getCodigo(), postsList.get(i).getCodigo_barra(), postsList.get(i).getDescripcion(), postsList.get(i).getPrecio(), postsList.get(i).getDepartamento(), postsList.get(i).getPorImprimir(), postsList.get(i).getFecha(), postsList.get(i).getFechaImpresion(), postsList.get(0).getPrecioo(), postsList.get(0).getOferta()));
                }
                adapterDatos.SetDataList(listDatos);//asigna los datos en el adapter
                recycler.setAdapter(adapterDatos);//se carga los datos en la vista
            }

            @Override
            public void onFailure(Call<List<Habladores>> call, Throwable t) {
                MensajeService.ShowMensaje(getContext(), "Error! " + t.getMessage());
            }
        });
    }

    /***Obtener todos los Habladores Pendientes****/
    private void getHabladores(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(dataServer.getIpAdress())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JsonPlaceHolderApi jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);

        Call<List<Habladores>> call = jsonPlaceHolderApi.getHabladores();
        call.enqueue(new Callback<List<Habladores>>() {
            @Override
            public void onResponse(Call<List<Habladores>> call, Response<List<Habladores>> response) {

                if(!response.isSuccessful()){
                    return;
                }

                postsList = response.body();
                listDatos = new ArrayList<Habladores>();
                for(int i= 0; i< postsList.size();i++){//se cargan los datos en el adapter
                    listDatos.add(new Habladores(postsList.get(i).getCodigo(),postsList.get(i).getCodigo_barra(),postsList.get(i).getDescripcion(),postsList.get(i).getPrecio(), postsList.get(i).getDepartamento(), postsList.get(i).getPorImprimir(), postsList.get(i).getFecha(), postsList.get(i).getFechaImpresion(), postsList.get(0).getPrecioo(), postsList.get(0).getOferta()));
                }
                adapterDatos.SetDataList(listDatos);//asigna los datos en el adapter
                recycler.setAdapter(adapterDatos);//se carga los datos en la vista
            }
            @Override
            public void onFailure(Call<List<Habladores>> call, Throwable t) {
                MensajeService.ShowMensaje(getContext(),"Error! "+t.getMessage());
            }
        });

    }//end getHabladores

    /***Obtener todos los Habladores Por Departamento****/
    private void getHabladoresDepto(String departamento){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(dataServer.getIpAdress())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JsonPlaceHolderApi jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);

        Call<List<Habladores>> call = jsonPlaceHolderApi.getHabladoresDepartamentos(departamento);
        call.enqueue(new Callback<List<Habladores>>() {
            @Override
            public void onResponse(Call<List<Habladores>> call, Response<List<Habladores>> response) {

                if(!response.isSuccessful()){
                    return;
                }

                postsList = response.body();
                listDatos = new ArrayList<Habladores>();
                for(int i= 0; i< postsList.size();i++){//se cargan los datos en el adapter
                    listDatos.add(new Habladores(postsList.get(i).getCodigo(),postsList.get(i).getCodigo_barra(),postsList.get(i).getDescripcion(),postsList.get(i).getPrecio(), postsList.get(i).getDepartamento(), postsList.get(i).getPorImprimir(), postsList.get(i).getFecha(), postsList.get(i).getFechaImpresion(), postsList.get(0).getPrecioo(), postsList.get(0).getOferta()));
                }
                adapterDatos.SetDataList(listDatos);//asigna los datos en el adapter
                recycler.setAdapter(adapterDatos);//se carga los datos en la vista
            }
            @Override
            public void onFailure(Call<List<Habladores>> call, Throwable t) {
                MensajeService.ShowMensaje(getContext(),"Error! "+t.getMessage());
            }
        });
    }//end getHabladores Dpto

    private void setActualizarHablador(String Codigo){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(dataServer.getIpAdress())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JsonPlaceHolderApi jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);

        Call<List<Habladores>> call = jsonPlaceHolderApi.setActualizarHablador(Codigo, idUser, dataServer.getImpresora());
        call.enqueue(new Callback<List<Habladores>>() {
            @Override
            public void onResponse(Call<List<Habladores>> call, Response<List<Habladores>> response) {

                if(!response.isSuccessful()){
                    return;
                }
                postsList = response.body();
                listDatos = new ArrayList<Habladores>();
                filtrarLista =null;

                for(int i= 0; i< postsList.size();i++){
                    listDatos.add(new Habladores(postsList.get(i).getCodigo(),postsList.get(i).getCodigo_barra(),postsList.get(i).getDescripcion(),postsList.get(i).getPrecio(), postsList.get(i).getDepartamento(), postsList.get(i).getPorImprimir(), postsList.get(i).getFecha(), postsList.get(i).getFechaImpresion(), postsList.get(0).getPrecioo(), postsList.get(0).getOferta()));
                }

                adapterDatos.SetDataList(listDatos);
                recycler.setAdapter(adapterDatos);
            }
            @Override
            public void onFailure(Call<List<Habladores>> call, Throwable t) {
                MensajeService.ShowMensaje(getContext(),"Error! "+t.getMessage());
            }
        });
    }//end ActualizaHabladores

    void alertDialogListHabla(String titulo, String mensaje){
        //-------------------------
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle(titulo);
        alert.setMessage(mensaje);
        alert.setIcon(R.drawable.logo_x);
        alert.setPositiveButton("SI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try{
                    MensajeService.ShowMensaje(getContext(),"Buscando Habladores....");
                    if(printDataMasivo()){
                        MensajeService.ShowMensaje(getContext(),"Habladores Fueron Impresos");
                    }else{
                        MensajeService.ShowMensaje(getContext(),"No se Imprimieron Habladores");
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                dialog.dismiss();
            }
        });

        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
        //-------------------------
    }

    /*--------- Metodos para impresion ----------------*/
    // Printing Text to Bluetooth Printer //
    boolean printData(Habladores hablador) throws  IOException{
        try{
            //se llama el formato de impresion
            String msg = FormatImpres.FormatoImpresion(hablador);
            msg+="\n";
            outputStream.write(msg.getBytes());
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }

    // Printing Text to Bluetooth Printer Masivo//
    boolean printDataMasivo() throws  IOException{
        try{
            for (int i = 0; i < postsList.size(); i++) {
                String msg = FormatImpres.FormatoImpresion(postsList.get(i));
                msg+="\n";
                outputStream.write(msg.getBytes());
                setActualizarHablador(postsList.get(i).getCodigo());
                Thread.sleep(100);
            }

            return true;
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }

    void FindBluetoothDevice(){
        try{
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if(bluetoothAdapter==null){
                MensajeService.ShowMensaje(getContext(),"No se encontró ningún adaptador Bluetooth");
            }
            if(bluetoothAdapter.isEnabled()){
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBT,0);
            }

            Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();

            if(pairedDevice.size()>0){
                for(BluetoothDevice pairedDev:pairedDevice){
                    // My Bluetoth printer name is BTP_F09F1A

                    if(pairedDev.getName().equals(dataServer.getImpresora())){
                        bluetoothDevice=pairedDev;
                        //Mensaje("Impresora Bluetooth: "+pairedDev.getName());
                        break;
                    }
                }
            }
            MensajeService.ShowMensaje(getContext(),"Impresora Bluetooth Lista");
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    void openBluetoothPrinter() throws IOException {
        try{
            //Standard uuid from string //
            UUID uuidSting = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            bluetoothSocket=bluetoothDevice.createRfcommSocketToServiceRecord(uuidSting);
            bluetoothSocket.connect();
            outputStream=bluetoothSocket.getOutputStream();
            inputStream=bluetoothSocket.getInputStream();

            beginListenData();

        }catch (Exception ex){

        }
    }

    void beginListenData(){
        try{

            final Handler handler =new Handler();
            final byte delimiter=10;
            stopWorker =false;
            readBufferPosition=0;
            readBuffer = new byte[1024];

            thread=new Thread(new Runnable() {
                @Override
                public void run() {

                    while (!Thread.currentThread().isInterrupted() && !stopWorker){
                        try{
                            int byteAvailable = inputStream.available();
                            if(byteAvailable>0){
                                byte[] packetByte = new byte[byteAvailable];
                                inputStream.read(packetByte);

                                for(int i=0; i<byteAvailable; i++){
                                    byte b = packetByte[i];
                                    if(b==delimiter){
                                        byte[] encodedByte = new byte[readBufferPosition];
                                        System.arraycopy(
                                                readBuffer,0,
                                                encodedByte,0,
                                                encodedByte.length
                                        );
                                        final String data = new String(encodedByte,"US-ASCII");
                                        readBufferPosition=0;
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                //lblPrinterName.setText(data);
                                            }
                                        });
                                    }else{
                                        readBuffer[readBufferPosition++]=b;
                                    }
                                }
                            }
                        }catch(Exception ex){
                            stopWorker=true;
                        }
                    }

                }
            });

            thread.start();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    // Disconnect Printer //
    void disconnectBT() throws IOException{
        try {
            stopWorker=true;
            outputStream.close();
            inputStream.close();
            bluetoothSocket.close();
            MensajeService.ShowMensaje(getContext(),"Printer Disconnected.");
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}