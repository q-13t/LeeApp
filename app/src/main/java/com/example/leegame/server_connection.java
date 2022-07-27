package com.example.leegame;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class server_connection extends AppCompatActivity {
    private ImageButton back_button;
    private Button connect_button;
    private Button disconnect_button;
    private EditText ip_field;
    private EditText port_field;
    private TextView status_fied;
    private static String PORT ="";
    private static String IP = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_connection_layout);

        back_button = findViewById(R.id.return_button);
        connect_button = findViewById(R.id.connect_button);
        disconnect_button = findViewById(R.id.disconnect_button);
        ip_field = findViewById(R.id.ip_field);
        port_field = findViewById(R.id.port_field);
        status_fied = findViewById(R.id.connection_status);

        if(PORT!=""){
            port_field.setText(PORT);
        }
        if(IP!=""){
            ip_field.setText(IP);
        }


        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent main_intent= new Intent(getApplicationContext(), MainActivity.class);
                startActivity(main_intent);
            }
        });

        connect_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PORT  = port_field.getText().toString();
                IP = ip_field.getText().toString();
                status_fied.setText(IP+" "+ PORT);

            }
        });

        disconnect_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                status_fied.setText(IP+" "+ PORT);
            }
        });

    }
}