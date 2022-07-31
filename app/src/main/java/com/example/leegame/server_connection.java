package com.example.leegame;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class server_connection extends AppCompatActivity {
    private ImageButton back_button;
    private Button connect_button;
    private Button disconnect_button;
    private EditText ip_field;
    private EditText port_field;
    public static TextView status_field;
    private static int PORT =4000;
    private static String IP = "192.168.1.157";





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_connection_layout);

        back_button = findViewById(R.id.return_button);
        connect_button = findViewById(R.id.connect_button);
        disconnect_button = findViewById(R.id.disconnect_button);
        ip_field = findViewById(R.id.ip_field);
        port_field = findViewById(R.id.port_field);
        status_field = findViewById(R.id.connection_status);


        if(ConnectionHandler.socket != null){
            status_field.setTextColor(Color.GREEN);
            status_field.setText("Connected!");
        }else{
            status_field.setTextColor(Color.RED);
            status_field.setText("Not Connected!");
        }

        if(PORT!=0){
            port_field.setText(PORT+"");
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
                if(port_field.getText().toString().contains("[^\\d]")){
                    Toast.makeText(server_connection.this,"Incorrect port value!",Toast.LENGTH_SHORT).show();
                }else{
                    try {
                        PORT  = Integer.valueOf(port_field.getText().toString());
                        IP = ip_field.getText().toString();
                        if(ConnectionHandler.socket==null){
                             new Thread(new ConnectionHandler(IP,PORT)).start();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });

        disconnect_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ConnectionHandler.socket!=null){
                 ConnectionHandler.send("DISCONNECT");
                }
            }
        });
    }
}


class ConnectionHandler implements Runnable {
    private int PORT ;
    private String IP ;
    public static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static String message;

    ConnectionHandler(String IP,int PORT){
        this.IP=IP;
        this.PORT=PORT;
    }

    public static void send(String msg){
//        System.out.println(msg);
        message = msg;
    }

    @Override
    public void run() {
        try {
            if(socket ==null){
                socket = new Socket(IP,PORT);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                server_connection.status_field.setTextColor(Color.GREEN);
                server_connection.status_field.setText("Connected!");
                String[] read = in.readLine().split("\\|");
//                for (String string : read) {
//                    System.out.println(string);
//                }
                for (String map : read) {
                    MainActivity.server_maps.add(map);
                }
                while (socket.isConnected()){

//                   System.out.println("ALIVE!");

                   if(message!=null){
                    if(message.equals("DISCONNECT")) {
                        out.println(message);
                        out.flush();
                        try {
                            message=null;
                            server_connection.status_field.setTextColor(Color.RED);
                            server_connection.status_field.setText("Not Connected!");
                            break;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else{
//                        System.out.println(message);
                        out.println(message);
                        out.flush();

                        message=null;

                        String line = in.readLine();


//                        String input_line = line.replaceAll("\\|","\n");

//                        System.out.println(input_line);

//                        MainActivity.map_str = input_line;

//                        MainActivity.stringBuilder.append(input_line);

                       MainActivity.map = new ArrayList<>();

                        for (String str_1: line.split("\\|")) {
//                           System.out.println(str_1);
                            ArrayList<Character> characterList = (ArrayList<Character>) str_1.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
                            MainActivity.map.add(characterList);
                        }

                    }
                }
                }
            }
        }catch (Exception e ){
            e.printStackTrace();
        }finally{
            try{
                MainActivity.server_maps.clear();
                in.close();
                out.close();
                if(socket!=null){
                    socket.close();
                    socket = null;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        System.out.println("END!");
    }
}