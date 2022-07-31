package com.example.leegame;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    private static final int STORAGE_PERMISSION_CODE = 1000;
    private static final int STORAGE_PERMISSION_CODE_2 = 1001;
    private static final int WIFI_PERMISSION_CODE = 1002;
    private static final int NETWORK_PERMISSION_CODE = 1003;
    private static final int INTERNET_PERMISSION_CODE = 1004;
    private Spinner spinner;
    private CheckBox checkBox;
    private TextView textView;
    private Button run_button;
    private Button delete_button;
    private Button user_map_button;
    private ImageButton connection_button;
    protected static final StringBuilder stringBuilder = new StringBuilder();
    private String map_selected = "";
    private int selection_pos = 0;
    protected static ArrayList<ArrayList<Character>> map = new ArrayList<>();
    private int last_map_numb=0;
    private char[][] path;
    private boolean repeat = false;
    public static ArrayList<String> server_maps = new ArrayList<>();
    public static String map_str = "";


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
        dir_creator();

        connection_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent connection_intent= new Intent(getApplicationContext(),server_connection.class);
                startActivity(connection_intent);
            }
        });

        run_button.setOnClickListener(view -> {
            map_str = "";

            System.out.println(map_selected);

            if(checkBox.isChecked()){
                checkBox.setChecked(false);
                create_map();
            }else {
                stringBuilder.delete(0, stringBuilder.length());
                stringBuilder.append("Input:\n");
                if(map_selected.matches("s map .")){

                    ConnectionHandler.send(map_selected.replaceAll(" ","_")+".txt");
                    stringBuilder.append(map_str);
                    while (map.size()==0){
                        try {
                            Thread.sleep(50);
                        }catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    for (ArrayList<Character> ch_1:map) {
                        for (Character ch_2:ch_1) {
                            stringBuilder.append(ch_2);
//                            System.out.println(ch_2);
                        }
                        stringBuilder.append("\n");
                        System.out.println();
                    }

                    if(!map_selected.contains("done")) {
                        try {
                            calculate_path();
                        } catch (UnableToFindSolutionException e) {
                            stringBuilder.append(e.getMessage());
                        }
                    }else{
                        reader(map_selected);
                    }

                    textView.setText(stringBuilder);
//                text = stringBuilder.toString();
                    stringBuilder.delete(0,stringBuilder.length());

                }else {
                reader(map_selected);
                if(!map_selected.contains("done")) {
                    try {
                        calculate_path();
                    } catch (UnableToFindSolutionException e) {
                        stringBuilder.append(e.getMessage());
                    }
                }
                textView.setText(stringBuilder);
//                text = stringBuilder.toString();
                stringBuilder.delete(0,stringBuilder.length());
            }
            }
        });

        user_map_button.setOnClickListener(view -> {
            String input = textView.getText().toString();
//            System.out.println(input);
            boolean contains_player = true;
            boolean contains_goal = true;
            if(!input.contains("@") ){
                contains_player = false;
                Toast.makeText(MainActivity.this,"Missing @!",Toast.LENGTH_SHORT).show();
            }
            if(!input.contains("$")){
                contains_goal = false;
                Toast.makeText(MainActivity.this,"Missing $!",Toast.LENGTH_SHORT).show();
            }
//            System.out.println(contains_goal&& contains_player);
            if(contains_goal && contains_player){
            String[] lines = input.split( "\n" );
            map = new ArrayList<>();
        int X = 0;
        int Y = 0;

            map_selected = "u_map_"+last_map_numb;

            for(String line: lines){
//                System.out.println(line);
                ArrayList<Character> characterList = (ArrayList<Character>) line.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
                if(Y < characterList.size()){
                    Y= characterList.size();
                }
                map.add(characterList);
            }
            X = map.size();
                char[][] map_chars = new char[X][Y];

            for (int i = 0; i < map.size(); i++) {
                for (int j = 0; j < map.get(i).size(); j++) {
//                    System.out.println(map.get(i).get(j));
                    map_chars[i][j] = map.get(i).get(j);
                }
            }

            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("Input:\n");
            String map_str = "";
            for (int i = 0; i <map.size(); i++) {
                for (int j = 0; j < map.get(i).size(); j++) {
                    if(map.get(i).get(j).equals(' ')){
                        map_str+=" ";
                    }else{
                        map_str+=map.get(i).get(j);
                    }
                }
                map_str+="\n";
            }
            stringBuilder.append(map_str);


            writer(map_chars, map_selected,"maps");
            try {
                calculate_path();
            } catch (UnableToFindSolutionException e) {
                stringBuilder.append(e.getMessage());
            }
            textView.setText(stringBuilder);
//                text = stringBuilder.toString();
            stringBuilder.delete(0,stringBuilder.length());
            }
        });

        delete_button.setOnClickListener(view -> {
            delete_selected_file(map_selected.replaceAll(" ","_")+".txt");
            update_spinner();
        });


    }

    private void delete_selected_file(String map_to_delete) {
        try {
            Files.walk(getFilesDir().toPath()).forEach(x -> {
//                System.out.println(x.getFileName());
                if (x.getFileName().toString().contains(map_to_delete)) {
                    x.toFile().delete();
                    Toast.makeText(MainActivity.this,map_to_delete+" deleted!",Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
        }
    }


    void update_spinner() {
        String[]  files =  list_files();
        int files_size = files.length;
        int server_maps_size = server_maps.size();

        String[] maps_combined = new String[files_size+server_maps_size];

        int i =0;

        if(server_maps_size!=0){
            for (; i < server_maps_size; i++) {
                maps_combined[i] = server_maps.get(i).replaceAll(".txt","").replaceAll("_"," ");
            }
        }

        if(files_size!=0){
            for (int j =0; j < files_size; i++,j++) {
                maps_combined[i] = files[j];
            }
        }

        Arrays.stream(maps_combined).sorted();

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.item_dropdown_layout,maps_combined);
        adapter.setDropDownViewResource(R.layout.item_selected_layout);
        spinner.setAdapter(adapter);
        if(selection_pos>=maps_combined.length){
            spinner.setSelection(0);
        }else{
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

    public void checkPermission(String permission, int requestCode){
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission }, requestCode);
       }       // else {
//            Toast.makeText(MainActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
//        }
    }

    public void dir_creator(){
        try{
            File dir =new File(getFilesDir(),"maps");
            if(!dir.exists()){
                dir.mkdir();
            }
            File dir_end =new File(getFilesDir(),"maps_done");
            if(!dir_end.exists()){
                dir_end.mkdir();
            }
        }catch (Exception e){
            stringBuilder.append(e.toString());
        }
    }

    private String[] list_files(){
        last_map_numb = 0;
        File dir = new File(getFilesDir()+"");
        ArrayList<String> list = new ArrayList<>();
        try {
            Files.walk(dir.toPath()).forEach((x) -> {
                String line = x.toString().replaceAll(".*(?=..map..*.txt)", "");
                if (line.matches("..map..*.txt")) {
                    int numb =Integer.valueOf( line.replaceAll("[^\\d]",""));
                    if(numb >= last_map_numb){
                        last_map_numb=numb+1;
                    }
                    list.add(line.replaceAll(".txt","").replaceAll("_"," "));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                int first_value = Integer.valueOf(o1.replaceAll("[^\\d]", ""));
                int second_value = Integer.valueOf(o2.replaceAll("[^\\d]", ""));
                if (first_value < second_value) {
                    return -1;
                }
                if (first_value > second_value) {
                    return 1;
                }
                if (first_value == second_value) {
                    return 0;
                }
                return o1.compareTo(o2);
            }
        });
        String[] maps = new String[list.size()];
        list.toArray(maps);

//        System.out.print("\n****************\n"+maps.length+"\n****************\n");
        return maps;
    }

    public void reader(String map_name) {
        File dir;
        if(map_name.contains("done")){
//            System.out.println("TRUE");
             dir = new File(getFilesDir()+"/maps_done/"+map_name.replaceAll(" ","_")+".txt");
        }else {
//            System.out.println("FALSE");
             dir = new File(getFilesDir()+"/maps/"+map_name.replaceAll(" ","_")+".txt");
        }
        map = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(dir.toString()));
            String line = "";
            while ((line = br.readLine()) != null) {
                ArrayList<Character> characterList = (ArrayList<Character>) line.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
                map.add(characterList);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i <map.size(); i++) {
            for (int j = 0; j < map.get(i).size(); j++) {
                if(map.get(i).get(j).equals(' ')){
                    map_str+=" ";
                }else{
                    map_str+=map.get(i).get(j);
                }
            }
            map_str+="\n";
        }
        stringBuilder.append(map_str);
    }

    public void create_map() {
        String map_name = "c_map_" +last_map_numb;
        int X = (int) Math.round((Math.random() * 21)+5);
        int Y = (int) Math.round((Math.random() * 21)+5);
        System.out.println("Dimensions: X " + X + " Y " + Y);
        char[][] map;
        boolean contains_player = false;
        boolean contains_goal = false;

        do {
            System.out.println("HERE!");
            contains_goal = false;
            contains_player = false;
            map = new char[X][Y];
            for (int x = 0; x < X; x++) {
                for (int y = 0; y < Y; y++) {
                    int random = (int) Math.round(Math.random() * 100);
                    if (!contains_goal & x > X * 0.33) {
                        if (random >= 99) {
                            map[x][y] = '$';
                            contains_goal = true;
                            continue;
                        }
                    }
                    if (!contains_player & x < X * 0.66) {
                        if (random >= 99) {
                            map[x][y] = '@';
                            contains_player = true;
                            continue;
                        }
                    }
                    if (random < 95) {
                        map[x][y] = '+';
                    } else {
                        map[x][y] = ' ';
                    }
                }
            }
        } while (!check_correctness(map));
        Toast.makeText(MainActivity.this, "Map created: "+map_name, Toast.LENGTH_SHORT).show();
        writer(map, map_name,"maps");
    }

    private boolean check_correctness(char[][] map) {
        boolean contains_player = false;
        boolean contains_goal = false;


        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[x].length; y++) {
                if (map[x][y] == '@') {
                    contains_player = true;
                }
                if (map[x][y] == '$') {
                    contains_goal = true;
                }
            }
        }
        boolean contains_all = contains_goal & contains_player;
        System.out.println(contains_all);
        return contains_all;
    }

    public void writer(char[][] path, String map_name, String destination) {
        String ending = "";
        if (destination.equals("maps")) {
            ending = ".txt";
        }
        if (destination.equals("maps_done")) {
            ending = "_done.txt";
        }
        File dir = new File(getFilesDir()+"/"+destination);
        StringBuilder sBuilder = new StringBuilder();
//        System.out.println(dir+"/"+map_name.replaceAll(" ","_")+ending);
        try (FileWriter fw = new FileWriter(new File(dir+"/", map_name.replaceAll(" ","_") + ending))) {
            for (int i = 0; i < path.length; i++) {
                for (int j = 0; j < path[i].length; j++) {
                    sBuilder.append(path[i][j]);
                }
                sBuilder.append("\n");
            }
            fw.write(sBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        last_map_numb++;
        update_spinner();
    }

    public void calculate_path() throws UnableToFindSolutionException {
        repeat = false;
        path = new char[map.size()][map.get(0).size()];
        int[][] map_o_ints = new int[map.size()][map.get(0).size()];
        int[][] pre = new int[map.size()][map.get(0).size()];

        int iterations = 0;

        do {
            for (int x = 0; x < map_o_ints.length; x++) {
                for (int y = 0; y < map_o_ints[x].length; y++) {
                    pre[x][y] = map_o_ints[x][y];
                }
            }
            // for (int[] ints_1 : pre) {
            // for (int ints_2 : ints_1) {
            // if (ints_2 == Integer.MAX_VALUE) {
            // System.out.print("M\t");
            // } else {
            // System.out.print(ints_2 + "\t");
            // }
            // }
            // System.out.println();
            // }
            // System.out.println("--------------------------------------------");
            for (int x = 0; x < map.size(); x++) {
                // System.out.println("HERE");
                for (int y = 0; y < map.get(x).size(); y++) {

                    if (map.get(x).get(y).equals(' ')) {
                        map_o_ints[x][y] = Integer.MAX_VALUE;
                        continue;
                    }
                    if (map.get(x).get(y).equals('@')) {
                        map_o_ints[x][y] = -1;
                        if (x - 1 >= 0) {
                            map_o_ints[x - 1][y] = 1;
                        }
                        if (x + 1 < map.size()) {
                            map_o_ints[x + 1][y] = 1;
                        }
                        if (y + 1 < map.get(x).size()) {
                            map_o_ints[x][y + 1] = 1;
                        }
                        if (y - 1 >= 0) {
                            map_o_ints[x][y - 1] = 1;
                        }
                    }

                    if (x - 1 >= 0 && map_o_ints[x][y] != 0) {
                        if (map_o_ints[x - 1][y] == 0) {
                            map_o_ints[x - 1][y] = map_o_ints[x][y] + 1;
                        }
                    }
                    if (x + 1 < map.size() && map_o_ints[x][y] != 0) {
                        if (map_o_ints[x + 1][y] == 0) {
                            map_o_ints[x + 1][y] = map_o_ints[x][y] + 1;
                        }
                    }
                    if (y + 1 < map.get(x).size() && map_o_ints[x][y] != 0) {
                        if (map_o_ints[x][y + 1] == 0) {
                            map_o_ints[x][y + 1] = map_o_ints[x][y] + 1;
                        }
                    }
                    if (y - 1 >= 0 && map_o_ints[x][y] != 0) {
                        if (map_o_ints[x][y - 1] == 0) {
                            map_o_ints[x][y - 1] = map_o_ints[x][y] + 1;
                        }
                    }
                }
            }
//             for (int[] ints_1 : map_o_ints) {
//             for (int ints_2 : ints_1) {
//             if (ints_2 == Integer.MAX_VALUE) {
//             System.out.print("M\t");
//             } else {
//             System.out.print(ints_2 + "\t");
//             }
//             }
//             System.out.println();
//             }
//             System.out.println("*****************************************************");
            if (iterations >= 200) {
                throw new UnableToFindSolutionException();
            }
            iterations++;
        } while (!check(map_o_ints, pre));

//        for (int[] ints_1 : map_o_ints) {
//            for (int ints_2 : ints_1) {
//                if (ints_2 == Integer.MAX_VALUE) {
//                    System.out.print("M\t");
//                } else {
//                    System.out.print(ints_2 + "\t");
//                }
//
//            }
//            System.out.println();
//        }

        build_path(map_o_ints);

//        for (char[] chars_1 : path) {
//            for (char chars_2 : chars_1) {
//                System.out.print(chars_2);
//            }
//            System.out.println();
//        }
    }

    public boolean check(int[][] map_new, int[][] map_old) {
        // for (int[] ints_1 : map) {
        // for (int ints_2 : ints_1) {
        // if (ints_2 == 0) {
        // return true;
        // }
        // }
        // }
        // return false;
        boolean get_to_target = false;
        boolean same = true;
        OuterLoop: for (int x = 0; x < map_new.length; x++) {
            for (int y = 0; y < map_new[x].length; y++) {

                if (map.get(x).get(y).equals('$')) {
                    if (x - 1 >= 0) {
                        if (map_new[x - 1][y] != 0 && map_new[x - 1][y] != Integer.MAX_VALUE) {
                            get_to_target = true;
                            break OuterLoop;
                        }
                    }
                    if (x + 1 < map_new.length) {
                        if (map_new[x + 1][y] != 0 && map_new[x + 1][y] != Integer.MAX_VALUE) {
                            get_to_target = true;
                            break OuterLoop;
                        }
                    }
                    if (y + 1 < map_new[x].length) {
                        if (map_new[x][y + 1] != 0 && map_new[x][y + 1] != Integer.MAX_VALUE) {
                            get_to_target = true;
                            break OuterLoop;
                        }
                    }
                    if (y - 1 >= 0) {
                        if (map_new[x][y - 1] != 0 && map_new[x][y - 1] != Integer.MAX_VALUE) {
                            get_to_target = true;
                            break OuterLoop;
                        }
                    }
                }

                if (!map_new.equals(map_old)) {
                    same = false;
                }
            }
        }
        boolean result = !same && get_to_target;
        // if (result) {
        // for (int x = 0; x < this.map_o_ints.length; x++) {
        // for (int y = 0; y < this.map_o_ints[x].length; y++) {
        // if (this.map_o_ints[x][y] == 0) {
        // this.map_o_ints[x][y] = Integer.MAX_VALUE;
        // }
        // }
        // }
        // }
        // System.out.println(result);
        if (!repeat && result) {
            repeat = true;
            return !result;
        }
        return result;
    }

    private void build_path(int[][] map_o_ints) {
    path = new char[map_o_ints.length][map_o_ints[0].length];

        int numb = 0;
        int X = 0;
        int Y = 0;
        for (int i = 0; i < path.length; i++) {
            for (int j = 0; j < path[i].length; j++) {
                if (map.get(i).get(j).equals(' ')) {
                    path[i][j] = ' ';
                    continue;
                } else {
                    path[i][j] = '+';
                }
                if (map.get(i).get(j).equals('$')) {
                    numb = map_o_ints[i][j];
                    path[i][j] = '$';
                    X = i;
                    Y = j;
                }
                if (map.get(i).get(j).equals('@')) {
                    path[i][j] = '@';
                }
            }
        }

        do {
            for (int x = map_o_ints.length - 1; x >= 0; x--) {
                for (int y = 0; y < map_o_ints[x].length; y++) {
                    // ////////////////
                    // if (map_o_ints[x][y] == Integer.MAX_VALUE) {
                    // System.out.print("M\t");
                    // } else {
                    // System.out.print(map_o_ints[x][y] + "\t");
                    // }

                    if (map.get(x).get(y).equals(" ")) {
                        continue;
                    } else {
                        if (numb == map_o_ints[x][y] + 1) {
                            if ((X == x + 1) || (X == x - 1)) {
                                if (Y == y) {
                                    X = x;
                                    Y = y;
                                    numb = map_o_ints[x][y];
                                    path[x][y] = '*';
                                }
                            } else if ((Y == y + 1) || Y == y - 1) {
                                if (X == x) {
                                    X = x;
                                    Y = y;
                                    numb = map_o_ints[x][y];
                                    path[x][y] = '*';
                                }
                            } else if (X == x && Y == y) {
                                X = x;
                                Y = y;
                                numb = map_o_ints[x][y];
                                path[x][y] = '*';
                            }
                        }
                    }
                }

            }
            // System.out.println("\n\n****************************************************************");
        } while (numb!=1);
        writer(path,map_selected,"maps_done");
        stringBuilder.append("\nSolution:\n");
        for (int i = 0; i <path.length; i++) {
            for (int j = 0; j < path[i].length; j++) {
                stringBuilder.append(path[i][j]);
            }
            stringBuilder.append("\n");
        }
    }
}

class UnableToFindSolutionException extends Exception {
    public UnableToFindSolutionException() {
        super("Algorithm could not find solution for this map!");
    }

}