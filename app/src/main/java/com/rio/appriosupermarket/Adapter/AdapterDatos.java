package com.rio.appriosupermarket.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rio.appriosupermarket.Modelo.Habladores;
import com.rio.appriosupermarket.R;

import java.io.IOException;
import java.util.ArrayList;

public class AdapterDatos extends RecyclerView.Adapter<AdapterDatos.ViewHolderDatos> {
    ArrayList<Habladores> listdatos;
    private OnDatosListener mOnDatosListener;

    public AdapterDatos(ArrayList<Habladores> listdatos, OnDatosListener onDatosListener){
        this.listdatos = listdatos;
        this.mOnDatosListener = onDatosListener;
    }

    public AdapterDatos(OnDatosListener onDatosListener){
        this.mOnDatosListener = onDatosListener;
    }

    public void SetDataList(ArrayList<Habladores> listdatos){
        this.listdatos = listdatos;
    }

    @NonNull
    @Override
    public ViewHolderDatos onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.itemlist,null, false);
        return new ViewHolderDatos(view, mOnDatosListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderDatos holder, int position) {
        holder.etiNombre.setText(listdatos.get(position).getDescripcion());
        holder.etiInformacion.setText(listdatos.get(position).getPrecio());

        holder.etiCodigoBarra.setText("COD: "+listdatos.get(position).getCodigo_barra());
        holder.etiFecha.setText(listdatos.get(position).getFecha());
    }

    @Override
    public int getItemCount() {
        return listdatos.size();
    }

    public class ViewHolderDatos extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView etiNombre, etiInformacion, etiCodigoBarra, etiFecha;
        ImageView foto;
        OnDatosListener onDatosListener; //contenedor con el evento listener del adapter

        public ViewHolderDatos(@NonNull View itemView, OnDatosListener mOnDatosListener) {
            super(itemView);
            etiNombre = (TextView) itemView.findViewById(R.id.idDescripcion);
            etiInformacion = (TextView) itemView.findViewById(R.id.idPrecio);
            etiCodigoBarra = (TextView) itemView.findViewById(R.id.idCodigoBarra);
            etiFecha = (TextView) itemView.findViewById(R.id.idFecha);
            foto = (ImageView) itemView.findViewById(R.id.idImagen);

            this.onDatosListener = mOnDatosListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            try {
                onDatosListener.onHabladorClick(getAdapterPosition());//recibe la posicion del view en interface
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public interface OnDatosListener{
        void onHabladorClick(int position) throws IOException;
    }

    //filtro de la lista
    public void filtrar(ArrayList<Habladores> filtroHabladores){
        this.listdatos = filtroHabladores;
        notifyDataSetChanged();
    }
}
