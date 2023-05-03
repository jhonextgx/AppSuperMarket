package com.rio.appriosupermarket.Vistas;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.rio.appriosupermarket.Modelo.DataServer;
import com.rio.appriosupermarket.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename and change types of parameters
    private DataServer dataServer;

    public HomeFragment(DataServer dataServer) {
        this.dataServer = dataServer;
    }

    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(DataServer dataServer) {
        HomeFragment fragment = new HomeFragment(dataServer);
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
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        //tu imageview
        ImageView imageView = (ImageView) root.findViewById(R.id.imageView);

        switch(dataServer.getLocalidad()){
            case "0001": imageView.setImageResource(R.drawable.logo_fondo);break;
            case "0104": imageView.setImageResource(R.drawable.logo_ply);break;
            case "0101": imageView.setImageResource(R.drawable.logo_jb);break;
            case "0102": imageView.setImageResource(R.drawable.logo_tk);break;
            case "0103": imageView.setImageResource(R.drawable.logo_fondo);break;
        }

        return  root;
    }
}