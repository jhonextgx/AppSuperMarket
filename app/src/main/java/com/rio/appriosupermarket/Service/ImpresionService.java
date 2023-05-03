package com.rio.appriosupermarket.Service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.rio.appriosupermarket.Modelo.FormatImpres;
import com.rio.appriosupermarket.Modelo.Habladores;
import com.rio.appriosupermarket.Modelo.Producto;

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

public class ImpresionService extends AppCompatActivity {
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

    private Context context;

    public ImpresionService(Context context){
        this.context = context;
    }

    public void ActivarImpresora(String impresora){
        try {
            FindBluetoothDevice(impresora);
            openBluetoothPrinter();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void DesconectarImpresora() throws IOException {
        disconnectBT();
    }

    /*-- Metodos de impresion --*/
    // Printing Text to Bluetooth Printer //
    public void printData(Habladores hablador) throws IOException {
        try{
            //se llama el formato de impresion
            String msg = FormatImpres.FormatoImpresion(hablador);
            msg+="\n";
            outputStream.write(msg.getBytes());
            MensajeService.ShowMensaje(context,"Hablador Fue Impreso");
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    // Printing Text to Bluetooth Printer Masivo//
    public void printDataMasivo(List<Habladores> postsList) throws  IOException{
        try{
            for (int i = 0; i < postsList.size(); i++) {
                String msg = FormatImpres.FormatoImpresion(postsList.get(i));
                msg+="\n";
                outputStream.write(msg.getBytes());
                //setActualizarHablador(postsList.get(i).getCodigo());
                Thread.sleep(100);
            }

            MensajeService.ShowMensaje(context,"Habladores  Impresos");
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    void FindBluetoothDevice(String Impresora){
        try{
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if(bluetoothAdapter==null){
                MensajeService.ShowMensaje(context,"No se encontró ningún adaptador Bluetooth");
            }
            if(bluetoothAdapter.isEnabled()){
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBT,0);
            }

            Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();

            if(pairedDevice.size()>0){
                for(BluetoothDevice pairedDev:pairedDevice){
                    // My Bluetoth printer name is BTP_F09F1A

                    if(pairedDev.getName().equals(Impresora)){
                        bluetoothDevice=pairedDev;
                        //Mensaje("Impresora Bluetooth: "+pairedDev.getName());
                        break;
                    }
                }
            }
            MensajeService.ShowMensaje(context,"Impresora Bluetooth Lista");
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
            MensajeService.ShowMensaje(context,"Printer Disconnected.");
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

}
