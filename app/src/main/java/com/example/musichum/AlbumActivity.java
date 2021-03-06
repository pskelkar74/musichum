package com.example.musichum;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.musichum.adapter.StockRecyclerAdapter;
import com.example.musichum.constants.Constants;
import com.example.musichum.models.InventoryItem;
import com.example.musichum.models.Product;
import com.example.musichum.network.IApiCalls;
import com.example.musichum.networkmanager.RetrofitBuilder;
import com.example.musichum.networkmanager.TempCartRetrofitBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AlbumActivity extends AppCompatActivity implements StockRecyclerAdapter.InventoryItemInterface, Constants {
    String title;
    String artist;
    String album;
    String pid;
    float cost;
    String coverUrl;
    String songUrl;
    String did;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Retrofit retrofit = RetrofitBuilder.getInstance();
        IApiCalls iApiCalls = retrofit.create(IApiCalls.class);
        SharedPreferences sharedPreferences = getSharedPreferences("com.example.musichum", Context.MODE_PRIVATE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        pid = getIntent().getStringExtra(PID);
        title = getIntent().getStringExtra(TITLE);
        coverUrl = getIntent().getStringExtra(COVER_URL);
        cost = getIntent().getFloatExtra(COST, -1);
        artist = getIntent().getStringExtra(ARTIST);
        album = getIntent().getStringExtra(ALBUM_NAME);
        songUrl = getIntent().getStringExtra(SONG_URL);
        did = getIntent().getStringExtra(DIST_ID);


        TextView tvTitle = findViewById(R.id.tv_title);
        TextView tvArtist = findViewById(R.id.tv_artistName);
        TextView tvAlbum = findViewById(R.id.tv_albumName);
        TextView year = findViewById(R.id.tv_year);
        ImageView ivCover = findViewById(R.id.iv_coverArt);
        String songURL = songUrl;

        findViewById(R.id.bt_media).setOnClickListener(view -> {

        });

        Glide.with(this)
                .load(coverUrl)
                .placeholder(R.drawable.close)
                .into((ImageView) findViewById(R.id.iv_coverArt));

        Call<Product> response = iApiCalls.getProduct(pid);
        response.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if(response.body() != null && response.code() == 200){
                    pid = response.body().getPid();
                    title = response.body().getTitle();
                    artist = response.body().getArtist();
                    album = response.body().getAlbum();
                    coverUrl = response.body().getCoverUrl();
                    cost = response.body().getLowestCost();
                    float rating = response.body().getAverageRating();
                    String genre = response.body().getGenre();
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {

            }
        });

        tvTitle.setText(title);
        tvAlbum.setText(album);
        tvArtist.setText(artist);
        Glide.with(this)
                .load(coverUrl)
                .placeholder(R.drawable.close)
                .into(ivCover);

        List<InventoryItem> inventoryItemList = new ArrayList<>();
        generateInventory(inventoryItemList, pid);

    }

    private void generateInventory(List<InventoryItem> inventoryItems, String pid){
        Retrofit retrofit = TempCartRetrofitBuilder.getInstance();
        IApiCalls iApiCalls = retrofit.create(IApiCalls.class);

        Call<List<InventoryItem>> responses = iApiCalls.getInventory(pid, TYPE_SONG);
        responses.enqueue(new Callback<List<InventoryItem>>() {
            @Override
            public void onResponse(Call<List<InventoryItem>> call, Response<List<InventoryItem>> response) {
                if(response.code() == 200 && response.body() != null){
                    for(InventoryItem inventoryItem : response.body()){
                        inventoryItems.add(inventoryItem);
                    }

                    RecyclerView rvDistributors = findViewById(R.id.rv_stock);
                    StockRecyclerAdapter stockRecyclerAdapter = new StockRecyclerAdapter(inventoryItems, AlbumActivity.this);
                    rvDistributors.setLayoutManager(new LinearLayoutManager(AlbumActivity.this));
                    rvDistributors.setAdapter(stockRecyclerAdapter);
                }
                else{
                    Toast.makeText(AlbumActivity.this, "Error " +response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<InventoryItem>> call, Throwable t) {

                InventoryItem inventoryItem = new InventoryItem();
                inventoryItem.setType(TYPE_SONG);
                inventoryItem.setDid(did);
                inventoryItem.setCost(cost);

                inventoryItems.add(inventoryItem);
                RecyclerView rvDistributors = findViewById(R.id.rv_stock);
                StockRecyclerAdapter stockRecyclerAdapter = new StockRecyclerAdapter(inventoryItems, AlbumActivity.this);
                rvDistributors.setLayoutManager(new LinearLayoutManager(AlbumActivity.this));
                rvDistributors.setAdapter(stockRecyclerAdapter);

                Toast.makeText(AlbumActivity .this, "Error fetching distributors.\nPlease try again later.", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onUserClick(InventoryItem inventoryItem) {

    }
}
