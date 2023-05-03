package com.rio.appriosupermarket;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.rio.appriosupermarket.Modelo.DataServer;
import com.rio.appriosupermarket.Service.ImpresionService;
import com.rio.appriosupermarket.Service.MensajeService;
import com.rio.appriosupermarket.Vistas.CategoriasFragment;
import com.rio.appriosupermarket.Vistas.EscanFragment;
import com.rio.appriosupermarket.Vistas.HabladoresFragment;
import com.rio.appriosupermarket.Vistas.HomeFragment;
import com.rio.appriosupermarket.Vistas.InventarioFragment;
import com.rio.appriosupermarket.Vistas.LoginFragment;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawer; //control del menu
    NavigationView navigationView;//menu header
    //data del Servers
    DataServer dataServer;
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //localidad del server [in: 0 py: 1 Jb:2 Trk:3 CD: 4 ]
        dataServer= new DataServer(1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);

        toggle.syncState();

        if (savedInstanceState == null) {
            bloquearMenu();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new LoginFragment(dataServer)).commit();
            navigationView.setCheckedItem(R.id.nav_menu_home);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_menu_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new HomeFragment(dataServer)).commit();
                break;

            case R.id.nav_menu_habladores:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                       new HabladoresFragment(dataServer,"")).commit();
                break;

            case R.id.nav_menu_escanear:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new EscanFragment(dataServer)).commit();
                break;

            case R.id.nav_menu_buscar:
                 getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                       new CategoriasFragment(dataServer)).commit();
                break;

            case R.id.nav_menu_inv:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                       new InventarioFragment(dataServer)).commit();
                break;

            case R.id.nav_menu_salir:
                bloquearMenu();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new LoginFragment(dataServer)).commit();
                break;
        }


        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void changeNavHeaderData(String NombreUsuario){
        View header = navigationView.getHeaderView(0);
        TextView txtUser = (TextView) header.findViewById(R.id.username_subtitulo);
        txtUser.setText("Hola, "+ NombreUsuario);
    }

    public void bloquearMenu(){
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    public void desbloquearMenu(){
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }
}