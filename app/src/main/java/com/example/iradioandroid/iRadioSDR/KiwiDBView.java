package com.example.iradioandroid.iRadioSDR;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.iradioandroid.R;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class KiwiDBView extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "KiwiDBView";

    Button btn_exit, btn_saveexit, btn_updateDB;

    Spinner spinner_servers;
    ArrayAdapter<String> arrayAdapter = null;

    TextView textView_server, textView_new_server;
    KiwiDB myKiwiDB = new KiwiDB();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kiwidbview);

        // erzwinge Querformat
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        btn_exit = findViewById(R.id.btn_Exit);
        btn_exit.setOnClickListener(this);

        btn_saveexit = findViewById(R.id.btn_SaveExit);
        btn_saveexit.setOnClickListener(this);

        btn_updateDB = findViewById(R.id.btn_UpdateDB);
        btn_updateDB.setOnClickListener(this);

        textView_server = findViewById(R.id.tv_server);
        textView_new_server = findViewById(R.id.tv_new_server);

        if (myKiwiDB.loadDB() == KiwiDB.KiwiDBResult.ERROR) {
            Toast.makeText(this, "Open Database failed. No file or need permission.", Toast.LENGTH_LONG).show();
        }


        spinner_servers = findViewById(R.id.spinner_servers);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, myKiwiDB.getKiwiSDRInfoDB());
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_servers.setAdapter(arrayAdapter);
        spinner_servers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "selected server: " + parent.getSelectedItem());
                textView_new_server.setText(myKiwiDB.getKiwiSDRElementAt(parent.getSelectedItemPosition()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        textView_server.setText(myKiwiDB.loadSelectedKiwiServerURL());

    }

    @Override
    public void onClick(View view) {
        if (btn_exit.getId() == view.getId()) {
            finish();
        }

        if (btn_saveexit.getId() == view.getId()) {
            myKiwiDB.saveSelectedKiwiServerURL(textView_new_server.getText().toString());
            finish();
        }

        if (btn_updateDB.getId() == view.getId()) {
            if (myKiwiDB != null) {
                Toast.makeText(this, "downloading database from web ...", Toast.LENGTH_LONG).show();
                myKiwiDB.updateDBfromWeb();
                if (myKiwiDB.loadDB() == KiwiDB.KiwiDBResult.OK) {
                    Toast.makeText(this, "found " + myKiwiDB.getKiwiSDRInfoDB().size() + " servers", Toast.LENGTH_LONG).show();
                    if (arrayAdapter != null) {
                        arrayAdapter.notifyDataSetChanged();
                    }
                }
            }
        }

    }
}
