package com.example.leegame;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Main class to handle {@code activity_main} layout
 * 
 * @author Volodymyr Davybida
 */
public class MainActivity extends AppCompatActivity {
    private static final int STORAGE_PERMISSION_CODE = 1000;
    private static final int STORAGE_PERMISSION_CODE_2 = 1001;
    private static final int WIFI_PERMISSION_CODE = 1002;
    private static final int NETWORK_PERMISSION_CODE = 1003;
    private static final int INTERNET_PERMISSION_CODE = 1004;

    public static ConnectionHandler connection_handler;
    public FileOperator file_operator = new FileOperator(this);
    public LeeAlgorithm lee_algorithm = new LeeAlgorithm(this,file_operator);

    public Spinner spinner;
    public CheckBox checkBox;
    public TextView textView;
    private Button run_button;
    private Button delete_button;
    private Button user_map_button;
    private ImageButton connection_button;

    private RunButtonListener run_button_listener = new RunButtonListener(this,file_operator,lee_algorithm,connection_handler);
    private ConnectionButtonListener connection_button_listener = new ConnectionButtonListener(this,connection_handler);
    private UserMapButtonListener User_map_button_listener = new UserMapButtonListener(this,  file_operator,lee_algorithm);
    private DeleteButtonListener delete_button_listener = new DeleteButtonListener(this,file_operator);

    protected static final StringBuilder stringBuilder = new StringBuilder();
    protected String map_selected = "";
    private int selection_pos = 0;
    protected static volatile ArrayList<ArrayList<Character>> map = new ArrayList<>();
    public int last_map_numb = 0;
    public static ArrayList<String> server_maps = new ArrayList<>();
    public static String map_str = "";


    /**
     * Controls {@code activity_main}. Sets buttons and text.
     * 
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
            checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE_2);
            checkPermission(Manifest.permission.ACCESS_WIFI_STATE, WIFI_PERMISSION_CODE);
            checkPermission(Manifest.permission.ACCESS_NETWORK_STATE, NETWORK_PERMISSION_CODE);
            checkPermission(Manifest.permission.INTERNET, INTERNET_PERMISSION_CODE);

            spinner = findViewById(R.id.spinner);
            run_button = findViewById(R.id.button);
            checkBox = findViewById(R.id.checkBox);
            textView = findViewById(R.id.text_field);
            user_map_button = findViewById(R.id.user_map_button);
            delete_button = findViewById(R.id.delete_button);
            connection_button = findViewById(R.id.connection_button);

            update_spinner();
            FileOperator.dir_creator(stringBuilder);

            connection_button.setOnClickListener(connection_button_listener);

            run_button.setOnClickListener(run_button_listener);

            user_map_button.setOnClickListener(User_map_button_listener);

            delete_button.setOnClickListener(delete_button_listener);


    }

    /**
     * Updates spinner for the layout. If it is available adds server maps to the
     * pool.
     */
    void update_spinner() {
        String[] files = file_operator.list_files();
        int files_size = files.length;
        int server_maps_size = server_maps.size();

        String[] maps_combined = new String[files_size + server_maps_size + 1];

        int i = 0;
        maps_combined[i] = "Clear Field";
        i++;
        if (server_maps_size != 0) {
            for (int j = 0; j < server_maps.size(); i++, j++) {
                maps_combined[i] = server_maps.get(j).replaceAll(".txt", "").replaceAll("_", " ");
            }
        }

        if (files_size != 0) {
            for (int j = 0; j < files_size; i++, j++) {
                maps_combined[i] = files[j];
            }
        }

        Arrays.stream(maps_combined).sorted();

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.item_dropdown_layout,
                maps_combined);
        adapter.setDropDownViewResource(R.layout.item_selected_layout);
        spinner.setAdapter(adapter);
        if (selection_pos >= maps_combined.length) {
            spinner.setSelection(0);
        } else {
            spinner.setSelection(selection_pos);
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                map_selected = adapterView.getItemAtPosition(position).toString();
                selection_pos = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    /**
     * Checks permissions for the application
     * 
     * @param permission  name
     * @param requestCode random integer to be related to the permission
     */
    public void checkPermission(String permission, int requestCode) {

        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission }, requestCode);
        } // else {
          // Toast.makeText(MainActivity.this, "Permission already granted",
          // Toast.LENGTH_SHORT).show();
          // }
    }
}

class RunButtonListener implements View.OnClickListener {
    private static MainActivity main_activity ;
    private static FileOperator file_operator;
    private static LeeAlgorithm lee_algorithm;
    private static ConnectionHandler connection_handler;

    RunButtonListener(MainActivity activity,FileOperator operator,LeeAlgorithm algorithm,ConnectionHandler handler){
        main_activity = activity;
        file_operator = operator;
        lee_algorithm = algorithm;
        connection_handler = handler;
    }

    @Override
    public void onClick(View view) {
        main_activity.map_str = "";

        System.out.println(main_activity.map_selected);
        main_activity.map = new ArrayList<>();

        if (main_activity.checkBox.isChecked()) {
            main_activity.checkBox.setChecked(false);
            main_activity.lee_algorithm.create_map();
            main_activity.spinner.setSelection(main_activity.spinner.getCount() - 1);
        } else {
            main_activity.stringBuilder.delete(0, main_activity.stringBuilder.length());
            main_activity.stringBuilder.append("Input:\n");
            if (main_activity.map_selected.equals("Clear Field")) {
                main_activity.stringBuilder.replace(0, main_activity.stringBuilder.length(), "");
                main_activity.textView.setText(main_activity.stringBuilder);
            } else if (main_activity.map_selected.matches("s map .")) {
                synchronized (MainActivity.class) {
                    if (!main_activity.map_selected.contains("done")) {
                        try {
                            connection_handler.send(main_activity.map_selected.replaceAll(" ", "_") + ".txt");
                            main_activity.stringBuilder.append(main_activity.map_str);
                            try {
                                System.out.println("Waiting!");
                                MainActivity.class.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (connection_handler.getSocket() != null) {
                                if (connection_handler.getSocket().isConnected()) {
                                    for (ArrayList<Character> ch_1 : main_activity.map) {
                                        for (Character ch_2 : ch_1) {
                                            main_activity.stringBuilder.append(ch_2);
                                        }
                                        main_activity.stringBuilder.append("\n");
                                        System.out.println();
                                    }
                                    main_activity.lee_algorithm.calculate_path();
                                    connection_handler.send("Solved!");
                                }
                            } else {
                                Toast.makeText(main_activity, "Server is disconnected!", Toast.LENGTH_SHORT)
                                        .show();
                                main_activity.server_maps.clear();
                                main_activity.update_spinner();
                            }
                        } catch (UnableToFindSolutionException e) {
                            main_activity.stringBuilder.append(e.getMessage());
                            connection_handler.send(e.getMessage().toString());
                        }
                    } else {
                        main_activity.file_operator.reader(main_activity.map_selected);
                    }

                    main_activity.textView.setText(main_activity.stringBuilder);
                    main_activity.stringBuilder.delete(0, main_activity.stringBuilder.length());

                }

            } else {
                file_operator.reader(main_activity.map_selected);
                if (!main_activity.map_selected.contains("done")) {
                    try {
                        lee_algorithm.calculate_path();
                    } catch (UnableToFindSolutionException e) {
                        main_activity.stringBuilder.append(e.getMessage());
                    }
                }
                main_activity.textView.setText(main_activity.stringBuilder);
                main_activity.stringBuilder.delete(0, main_activity.stringBuilder.length());
            }
        }
    }
}

class UserMapButtonListener implements  View.OnClickListener {
    private static MainActivity main_activity;
    private static LeeAlgorithm lee_algorithm;
    private static FileOperator file_operator;

    UserMapButtonListener(MainActivity activity,FileOperator operator,LeeAlgorithm algorithm){
        main_activity = activity;
        file_operator = operator;
        lee_algorithm = algorithm;
    }

    @Override
    public void onClick(View view) {
        String input = main_activity.textView.getText().toString();
        boolean contains_player = true;
        boolean contains_goal = true;
        if (!input.contains("@")) {
            contains_player = false;
            Toast.makeText(main_activity, "Missing @!", Toast.LENGTH_SHORT).show();
        }
        if (!input.contains("$")) {
            contains_goal = false;
            Toast.makeText(main_activity, "Missing $!", Toast.LENGTH_SHORT).show();
        }
        if (contains_goal && contains_player) {

            input = input.replaceAll("[^@+ $\\n]", " ");
            String[] lines = input.split("\n");

            main_activity.map = new ArrayList<>();

            int X = 0;
            int Y = 0;

//            main_activity.map_selected = ;

            for (String line : lines) {

                ArrayList<Character> characterList = (ArrayList<Character>) line.chars().mapToObj(c -> (char) c)
                        .collect(Collectors.toList());
                if (Y < characterList.size()) {
                    Y = characterList.size();
                }
                main_activity.map.add(characterList);
            }
            X = main_activity.map.size();
            char[][] map_chars = new char[X][Y];

            boolean pass = true;

            for (int i = 0; i < main_activity.map.size(); i++) {
                if (Y != main_activity.map.get(i).size()) {
                    pass = false;
                }
            }
            if (pass) {
                for (int i = 0; i < main_activity.map.size(); i++) {
                    for (int j = 0; j < main_activity.map.get(i).size(); j++) {

                        map_chars[i][j] = main_activity.map.get(i).get(j);
                    }
                }

                main_activity.stringBuilder.delete(0, main_activity.stringBuilder.length());
                main_activity.stringBuilder.append("Input:\n");
                String map_str = "";
                for (int i = 0; i < main_activity.map.size(); i++) {
                    for (int j = 0; j < main_activity.map.get(i).size(); j++) {
                        if (main_activity.map.get(i).get(j).equals(' ')) {
                            map_str += " ";
                        } else {
                            map_str += main_activity.map.get(i).get(j);
                        }
                    }
                    map_str += "\n";
                }
                main_activity.stringBuilder.append(map_str);

//                file_operator.writer(map_chars, main_activity.map_selected, "maps");
                file_operator.writer(map_chars,  "u_map_" + main_activity.last_map_numb, "maps");

                try {
                    lee_algorithm.calculate_path();
                } catch (UnableToFindSolutionException e) {
                    main_activity.stringBuilder.append(e.getMessage());
                }
                main_activity.textView.setText(main_activity.stringBuilder);

                main_activity.stringBuilder.delete(0, main_activity.stringBuilder.length());
            } else {
                main_activity. map = new ArrayList<>();
                Toast.makeText(main_activity, "Incorrect dimensions!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

class DeleteButtonListener implements View.OnClickListener{
    private static MainActivity main_activity;
    private static FileOperator file_operator;

    DeleteButtonListener(MainActivity activity, FileOperator operator){
        main_activity= activity;
        file_operator = operator;
    }
    @Override
    public void onClick(View view) {
        file_operator.delete_selected_file(main_activity.map_selected.replaceAll(" ", "_") + ".txt");
        main_activity.update_spinner();
    }
}

class ConnectionButtonListener implements View.OnClickListener{
    private static MainActivity main_activity;
    private static ConnectionHandler connection_handler;
    public static Intent connection_intent;

    ConnectionButtonListener(MainActivity activity,ConnectionHandler handler){
        main_activity = activity;
                connection_handler = handler;
    }

    @Override
    public void onClick(View view) {
        if(connection_intent == null){
            connection_intent = new Intent(main_activity.getApplicationContext(), ServerConnection.class);
        }
        main_activity.startActivity(connection_intent);
    }
}