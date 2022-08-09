package com.example.leegame;

import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

public class FileOperator {
    private static MainActivity main_activity ;

        FileOperator(MainActivity activity){
            main_activity = activity;
        }

    /**
     * Writes {@linkplain MainActivity#map} to the desired location.
     * </p>
     * If {@code destination} is "maps_done" {@code map_name} will be suited with
     * "_done".
     *
     * @param map
     * @param map_name
     * @param destination
     */
    public void writer(char[][] map, String map_name, String destination) {
        if(map_name.equals("Clear Field")){
            return;
        }
        String ending = "";
        if (destination.equals("maps")) {
            ending = ".txt";
        }
        if (destination.equals("maps_done")) {
            ending = "_done.txt";
        }
        File dir = new File(main_activity.getFilesDir() + "/" + destination);
        StringBuilder sBuilder = new StringBuilder();

        try (FileWriter fw = new FileWriter(new File(dir + "/", map_name.replaceAll(" ", "_") + ending))) {
            for (int i = 0; i < map.length; i++) {
                for (int j = 0; j < map[i].length; j++) {
                    sBuilder.append(map[i][j]);
                }
                sBuilder.append("\n");
            }
            fw.write(sBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        main_activity.last_map_numb++;
       main_activity.update_spinner();
    }

    /**
     * Reads requested file into {@link MainActivity#map} variable and adds it to the
     * {@code stringBuilder} to be displayed.
     *
     * @param map_name
     */
    public void reader(String map_name) {
        File dir;
        if (map_name.contains("done")) {

            dir = new File(main_activity.getFilesDir() + "/maps_done/" + map_name.replaceAll(" ", "_") + ".txt");
        } else {

            dir = new File(main_activity.getFilesDir() + "/maps/" + map_name.replaceAll(" ", "_") + ".txt");
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(dir.toString()));
            String line = "";
            while ((line = br.readLine()) != null) {
                ArrayList<Character> characterList = (ArrayList<Character>) line.chars().mapToObj(c -> (char) c)
                        .collect(Collectors.toList());
                main_activity.map.add(characterList);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < main_activity.map.size(); i++) {
            for (int j = 0; j <  main_activity.map.get(i).size(); j++) {
                if (main_activity.map.get(i).get(j).equals(' ')) {
                    main_activity.map_str += " ";
                } else {
                    main_activity.map_str += main_activity.map.get(i).get(j);
                }
            }
            main_activity.map_str += "\n";
        }
        main_activity.stringBuilder.append(main_activity.map_str);
    }

    /**
     * Walks directory {@code files} searching for the files with maps.
     *
     * @return String[] with available maps.
     */
    public String[] list_files() {
      main_activity.last_map_numb = 0;
        File dir = new File(main_activity.getFilesDir() + "");
        ArrayList<String> list = new ArrayList<>();
        try {
            Files.walk(dir.toPath()).forEach((x) -> {
                String line = x.toString().replaceAll(".*(?=..map..*.txt)", "");
                if (line.matches(".*.map..*.txt")) {
                    if(!line.replaceAll("[^\\d]", "").equals("")){
                        int numb = Integer.valueOf(line.replaceAll("[^\\d]", ""));
                        if (numb >= main_activity.last_map_numb) {
                            main_activity.last_map_numb = numb + 1;
                        }
                    }

                    list.add(line.replaceAll(".txt", "").replaceAll("_", " "));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                try {
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
                }catch (Exception e){
                    e.printStackTrace();
                }
                return o1.compareTo(o2);
            }
        });
        String[] maps = new String[list.size()];
        list.toArray(maps);
        return maps;
    }

    /**
     * Deletes file containing map that is provided.
     *
     * @param map_to_delete
     */
    protected void delete_selected_file(String map_to_delete) {
        try {
            Files.walk(main_activity.getFilesDir().toPath()).forEach(x -> {

                if (x.getFileName().toString().contains(map_to_delete)) {
                    x.toFile().delete();
                    Toast.makeText(main_activity, map_to_delete + " deleted!", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
        }
    }
    /**
     * Creates directories {@code maps} and {@code maps_done} for the application to
     * save the files with maps to.
     *
     *
     */
    public static void dir_creator(StringBuilder  stringBuilder) {
        try {
            File dir = new File(main_activity.getFilesDir(), "maps");
            if (!dir.exists()) {
                dir.mkdir();
            }
            File dir_end = new File(main_activity.getFilesDir(), "maps_done");
            if (!dir_end.exists()) {
                dir_end.mkdir();
            }
        } catch (Exception e) {
            stringBuilder.append(e.toString());
        }
    }
}
