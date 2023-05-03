package com.rio.appriosupermarket.Vistas;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.rio.appriosupermarket.MiBarraActivity;
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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EscanFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EscanFragment extends Fragment {
    //Bluethoot
    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket bluetoothSocket;
    BluetoothDevice bluetoothDevice;
    //impresora
    OutputStream outputStream;
    InputStream inputStream;
    Thread thread;

    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;

    //camara
    private static final int CODIGO_PERMISOS_CAMARA = 1, CODIGO_INTENT = 2;
    private boolean permisoCamaraConcedido = false, permisoSolicitadoDesdeBoton = false;
    private TextView tvCodigoLeido;
    private TextView tvPrecioLeido;
    private TextView tvDescripLeido;
    private TextView tvBarraLeido;
    private TextView tvFechaLeido;

    //Data Api
    List<Habladores> postsList;
    DataServer dataServer;
    String CodigoAct/*codigo leido por camara*/, IdUser/*Usuario Activo*/;


    public EscanFragment(DataServer dataServer) {
        this.dataServer = dataServer;
    }


    // TODO: Rename and change types and number of parameters
    public static EscanFragment newInstance(DataServer dataServer) {
        EscanFragment fragment = new EscanFragment(dataServer);
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
        View root =  inflater.inflate(R.layout.fragment_escan, container, false);

        verificarYPedirPermisosDeCamara();
        //Usuario conectado
        SharedPreferences prefs = getContext().getSharedPreferences("shared_login_data",   Context.MODE_PRIVATE);
        IdUser = prefs.getString("IdUser","");// prefs.getString("nombre del campo" , "valor por defecto")

       // Button btnEscanear = root.findViewById(R.id.btnEscanear);
        tvCodigoLeido = root.findViewById(R.id.tvCodigoLeido);
        tvFechaLeido = root.findViewById(R.id.idFechaScan);
        tvBarraLeido = root.findViewById(R.id.idCodigoBarraScan);
        tvPrecioLeido = root.findViewById(R.id.idPrecioScan);
        tvDescripLeido = root.findViewById(R.id.idDescripcionScan);
        FloatingActionButton fab = root.findViewById(R.id.btnfabescan);
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
        /*btnEscanear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!permisoCamaraConcedido) {
                    MensajeService.ShowMensaje(getContext(), "Por favor permite que la app acceda a la cámara");
                    permisoSolicitadoDesdeBoton = true;
                    verificarYPedirPermisosDeCamara();
                    return;
                }
                escanear();
            }
        });*/

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
                    tvCodigoLeido.setText(codigo);
                    //SE Imprime aqui
                    getHablador(codigo);
                }
            }
        }
    }

    //El Activity que contiene el Fragment ha terminado su creación
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //conecto Impresora
        try {
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

    /*** Metodos de API ****/
    private void getHablador(String codigo){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(dataServer.getIpAdress())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JsonPlaceHolderApi jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);

        Call<List<Habladores>> call = jsonPlaceHolderApi.getHablador(codigo);
        call.enqueue(new Callback<List<Habladores>>() {
            @Override
            public void onResponse(Call<List<Habladores>> call, Response<List<Habladores>> response) {

                if(!response.isSuccessful()){
                    return;
                }
                postsList = response.body();
                tvCodigoLeido.setText("Imprimiendo...");
                try {

                    printData(postsList.get(0));
                    Thread.sleep(500);
                    if(postsList.get(0).getPorImprimir()==0){
                        MensajeService.ShowMensaje(getContext(),"Este Producto tenía Actualización pendiente");
                        //actualiza hablador y paso código interno
                        setActualizarHablador(postsList.get(0).getCodigo(), IdUser);
                    }
                    tvCodigoLeido.setText("Hablador impreso!!");
                    tvBarraLeido.setText(postsList.get(0).getCodigo_barra());
                    tvDescripLeido.setText(postsList.get(0).getDescripcion());
                    tvPrecioLeido.setText(postsList.get(0).getPrecio());
                    tvFechaLeido.setText(postsList.get(0).getFecha());

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            @Override
            public void onFailure(Call<List<Habladores>> call, Throwable t) {

               MensajeService.ShowMensaje(getContext(),"Error! "+t.getMessage());

            }
        });

    }//end getHablador

    //Actualiza Hablador Escaneado
    private void setActualizarHablador(String Codigo, String IdUsuario){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(dataServer.getIpAdress())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JsonPlaceHolderApi jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);

        Call<List<Habladores>> call = jsonPlaceHolderApi.setActualizarHablador(Codigo, IdUsuario, dataServer.getImpresora());//getPost();
        call.enqueue(new Callback<List<Habladores>>() {
            @Override
            public void onResponse(Call<List<Habladores>> call, Response<List<Habladores>> response) {

                if(!response.isSuccessful()){
                    return;
                }
                postsList = response.body();
            }
            @Override
            public void onFailure(Call<List<Habladores>> call, Throwable t) {
                MensajeService.ShowMensaje(getContext(),"Error! "+t.getMessage());
            }
        });

    }//end ActualizaHablador

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

    //Impresion Bluethoot
    void FindBluetoothDevice(){
        try{
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if(bluetoothAdapter==null){
                tvCodigoLeido.setText("No se encontró ningún adaptador Bluetooth");
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
                        tvCodigoLeido.setText("Impresora Bluetooth: "+pairedDev.getName());
                        break;
                    }
                }
            }
            tvCodigoLeido.setText("Impresora Bluetooth adjunta");
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
                                                tvCodigoLeido.setText(data);
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

    // Printing Text to Bluetooth Printer //
    void printData(Habladores hablador) throws  IOException{
        try{
            String msg = FormatImpres.FormatoImpresion(hablador);
            msg+="\n";
            outputStream.write(msg.getBytes());
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
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}