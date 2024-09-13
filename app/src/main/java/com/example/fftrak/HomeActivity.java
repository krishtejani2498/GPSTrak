package com.example.fftrak;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.example.fftrak.databinding.ActivityHomeBinding;
import com.iammert.library.readablebottombar.ReadableBottomBar;

public class HomeActivity extends AppCompatActivity {

    ActivityHomeBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container,new Fragment1());
        transaction.commit();

        binding.readableBottomBar.setOnItemSelectListener(new ReadableBottomBar.ItemSelectListener() {
            @Override
            public void onItemSelected(int i) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                switch (i){

                    case 0:
                        transaction.replace(R.id.fragment_container,new Fragment1());
                        break;

                    case 1:
                        transaction.replace(R.id.fragment_container,new Fragment2());
                        break;

                    case 2:
                        transaction.replace(R.id.fragment_container,new Fragment3());
                        break;
                }
                transaction.commit();
            }
        });
    }
}