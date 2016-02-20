package com.example.aleksa.androgen;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class PlantSelectionActivity extends AppCompatActivity {

    //TODO temporary for testing purposes
    private static final String[] PLANTS = new String[]{
            "Neka biljcica 1",
            "Neka biljcica 2",
            "Neka biljcica 3",
            "Neka biljcica 4",
            "Neka biljcica 5",
            "Neka biljcica 6",};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plants_selection);

        //TODO use loader here instead, once the DB and content providers are in
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.plants_selection_list_item,
                PLANTS);

        ((ListView)findViewById(R.id.list_plants_selection)).setAdapter(adapter);
    }
}
