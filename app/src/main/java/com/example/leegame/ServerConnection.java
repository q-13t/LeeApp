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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Main class for connection layout.
 * 
 * @author Volodymyr Davybida
 */
public class ServerConnection extends AppCompatActivity {
    private ImageButton back_button;
    private Button connect_button;
    private Button disconnect_button;
    protected EditText ip_field;
    protected EditText port_field;
    public static TextView status_field;

    private BackButtonListener back_button_listener = new BackButtonListener(this);
    private ConnectButtonListener connect_button_listener = new ConnectButtonListener(this);
    private DisconnectButtonListener disconnect_button_listener =  new DisconnectButtonListener(this);


    protected static int PORT = 4000;
    protected static String IP = "";
    protected static ConnectionHandler connection;

    /**
     * Controls {@code server_connection_layout}. Sets buttons and text.
     * 
     * @param savedInstanceState
     */
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

        if (connection != null) {
            status_field.setTextColor(Color.GREEN);
            status_field.setText("Connected!");
        } else {
            status_field.setTextColor(Color.RED);
            status_field.setText("Not Connected!");
        }

        if (PORT != 0) {
            port_field.setText(PORT + "");
        }
        if (IP != "") {
            ip_field.setText(IP);
        }

        back_button.setOnClickListener(back_button_listener);

        connect_button.setOnClickListener(connect_button_listener);

        disconnect_button.setOnClickListener(disconnect_button_listener);
    }
}

/**
 * Handles connection with the server whose {@link #IP} and {@link #PORT} are
 * provided in {@code server_connection_layout} layout.
 */
class ConnectionHandler implements Runnable {
    private int PORT;
    private String IP;
    public static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static volatile String message = "";

    /**
     * @param IP
     * @param PORT
     */
    ConnectionHandler(String IP, int PORT) {
        this.IP = IP;
        this.PORT = PORT;
    }

    /**
     * @return Socket that is connected to the server
     */
     public Socket getSocket() {
     return socket;
     }

    /**
     * Memorizes message and notifies {@code run} to send it to the server
     * 
     * @param msg message to be send to the server
     */
    public synchronized void send(String msg) {
        synchronized (this) {
            message = msg;
            this.notify();
        }
    }

    /**
     * Main body of connection communication with the server. When connection is
     * established expects for {@link String} with available maps. Then proceeds to
     * wait for notification to send a request to the server. If message is
     * {@code DISCONNECT} server will be notified and connection will be terminated.
     *
     * 
     */
    @Override
    public void run() {
        try {
            synchronized (this) {
                if (socket == null) {
                    socket = new Socket(IP, PORT);
                    out = new PrintWriter(socket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    ServerConnection.status_field.setTextColor(Color.GREEN);
                    ServerConnection.status_field.setText("Connected!");
                    String[] read = in.readLine().split("\\|");
                    for (String map : read) {
                        MainActivity.server_maps.add(map);
                    }
                    while (socket.isConnected()) {
                        this.wait();
                        if (message.equals("DISCONNECT")) {
                            out.println(message);
                            out.flush();
                            try {
                                ServerConnection.status_field.setTextColor(Color.RED);
                                ServerConnection.status_field.setText("Not Connected!");
                                break;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {

                            out.println(message);
                            out.flush();
                            String line = in.readLine();
                            MainActivity.map = new ArrayList<>();

                            for (String str_1 : line.split("\\|")) {
                                ArrayList<Character> characterList = (ArrayList<Character>) str_1.chars()
                                        .mapToObj(c -> (char) c).collect(Collectors.toList());
                                MainActivity.map.add(characterList);
                            }
                            synchronized (MainActivity.class) {
                                System.out.println("Notified!");
                                MainActivity.class.notify();
                            }
                            this.wait();
                            out.println(message);
                            out.flush();

                        }
                    }
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                MainActivity.server_maps.clear();
                in.close();
                out.close();
                if (socket != null) {
                    socket.close();
                    socket = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("END!");
        synchronized (MainActivity.class) {
            MainActivity.class.notify();
        }
    }
}

class BackButtonListener implements View.OnClickListener{
    private static ServerConnection server_connection;

    BackButtonListener(ServerConnection connection){
        server_connection = connection;
    }

    @Override
    public void onClick(View view) {
        Intent main_intent = new Intent(server_connection.getApplicationContext(), MainActivity.class);
        server_connection.startActivity(main_intent);
    }
}

class ConnectButtonListener implements View.OnClickListener{
    private static ServerConnection server_connection;

    ConnectButtonListener(ServerConnection connection){
        server_connection = connection;
    }

    @Override
    public void onClick(View view) {
        if (server_connection.port_field.getText().toString().contains("[^\\d]")) {
            Toast.makeText(server_connection, "Incorrect port value!", Toast.LENGTH_SHORT).show();
        } else {
            try {
               server_connection.PORT = Integer.valueOf(server_connection.port_field.getText().toString());
                server_connection.IP = server_connection.ip_field.getText().toString();
                if (server_connection.connection == null || !server_connection.connection.getSocket().isConnected()) {
                    System.out.println("connecting");
                    server_connection.connection = new ConnectionHandler(server_connection.IP, server_connection.PORT);
                    new Thread(server_connection.connection).start();
                    MainActivity.connection_handler = server_connection.connection;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

class DisconnectButtonListener implements View.OnClickListener{
    private static ServerConnection server_connection;

    DisconnectButtonListener(ServerConnection connection){
        server_connection = connection;
    }

    @Override
    public void onClick(View view) {
        if (server_connection.connection != null) {
            if (ConnectionHandler.socket.isConnected()) {
                server_connection.connection.send("DISCONNECT");
            } else {
                server_connection.status_field.setTextColor(Color.RED);
                server_connection.status_field.setText("Not Connected!");
            }
            server_connection.connection = null;
        }
    }
}