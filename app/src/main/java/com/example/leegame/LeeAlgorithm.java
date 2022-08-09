package com.example.leegame;

import android.widget.Toast;

public class LeeAlgorithm {
    private static MainActivity main_activity;
    private static FileOperator file_operator;
    private char[][] path;
    private boolean repeat = false;

    LeeAlgorithm(MainActivity activity,FileOperator operator){
        main_activity = activity;
        file_operator = operator;
    }

    /**
     * Crates map that contains player (@) and goal ($). And calls {@link FileOperator#writer(char[][], String, String)}
     * to write newly created map to {@code files/maps} directory.
     * </p>
     * Dimensions of the map are from 5x5 up to 26x26
     *
     * @return name of the created map
     */
    public String create_map() {
        String map_name = "c_map_" + main_activity.last_map_numb;
        int X = (int) Math.round((Math.random() * 21) + 5);
        int Y = (int) Math.round((Math.random() * 21) + 5);
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
        Toast.makeText(main_activity, "Map created: " + map_name, Toast.LENGTH_SHORT).show();
       file_operator.writer(map, map_name, "maps");
        return map_name;
    }

    /**
     * Helper function for {@link #create_map()} function to check wether generated
     * map fulfills requirements (contains "@" and "$")
     *
     * @param map to be checked
     */
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

    /**
     * Calculates {@link MainActivity#map} using Lee algorithm. Firstly fills {@code map_o_ints}
     * with integers then calls {@link #build_path(int[][])}.
     *
     * @see {@link #check(int[][], int[][])}
     *
     * @throws UnableToFindSolutionException
     */
    public void calculate_path() throws UnableToFindSolutionException {
        repeat = false;
        path = new char[main_activity.map.size()][main_activity.map.get(0).size()];
        int[][] map_o_ints = new int[main_activity.map.size()][main_activity.map.get(0).size()];
        int[][] pre = new int[main_activity.map.size()][main_activity.map.get(0).size()];

        int iterations = 0;

        do {
            for (int x = 0; x < map_o_ints.length; x++) {
                for (int y = 0; y < map_o_ints[x].length; y++) {
                    pre[x][y] = map_o_ints[x][y];
                }
            }
            for (int x = 0; x < main_activity.map.size(); x++) {

                for (int y = 0; y < main_activity.map.get(x).size(); y++) {

                    if (main_activity.map.get(x).get(y).equals(' ')) {
                        map_o_ints[x][y] = Integer.MAX_VALUE;
                        continue;
                    }
                    if (main_activity.map.get(x).get(y).equals('@')) {
                        map_o_ints[x][y] = -1;
                        if (x - 1 >= 0) {
                            map_o_ints[x - 1][y] = 1;
                        }
                        if (x + 1 < main_activity.map.size()) {
                            map_o_ints[x + 1][y] = 1;
                        }
                        if (y + 1 < main_activity.map.get(x).size()) {
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
                    if (x + 1 < main_activity.map.size() && map_o_ints[x][y] != 0) {
                        if (map_o_ints[x + 1][y] == 0) {
                            map_o_ints[x + 1][y] = map_o_ints[x][y] + 1;
                        }
                    }
                    if (y + 1 < main_activity.map.get(x).size() && map_o_ints[x][y] != 0) {
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

            if (iterations >= 200) {
                throw new UnableToFindSolutionException();
            }
            iterations++;
        } while (!check(map_o_ints, pre));

        build_path(map_o_ints);

    }


    /**
     * Checker function for {@link #calculate_path()} function.
     *
     * @param map_new
     * @param map_old
     * @return wether map has been solved
     */
    public boolean check(int[][] map_new, int[][] map_old) {

        boolean get_to_target = false;
        boolean same = true;
        OuterLoop: for (int x = 0; x < map_new.length; x++) {
            for (int y = 0; y < map_new[x].length; y++) {

                if (main_activity.map.get(x).get(y).equals('$')) {
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
            }
        }

        if (!map_new.equals(map_old)) {
            same = false;
        }

        boolean result = !same && get_to_target;

        if (!repeat && result) {
            repeat = true;
            return !result;
        }
        return result;
    }


    /**
     * Fills {@link #path} with "*" representing shortest distance from "@" to "$"
     * using {@code map_o_ints} provided by {@link #calculate_path()} function.
     * After calling {@link FileOperator#writer(char[][], String, String)} to write solved map.
     *
     * @param map_o_ints
     */
    private void build_path(int[][] map_o_ints) {
        path = new char[map_o_ints.length][map_o_ints[0].length];

        int numb = 0;
        int X = 0;
        int Y = 0;
        for (int i = 0; i < path.length; i++) {
            for (int j = 0; j < path[i].length; j++) {
                if (main_activity.map.get(i).get(j).equals(' ')) {
                    path[i][j] = ' ';
                    continue;
                } else {
                    path[i][j] = '+';
                }
                if (main_activity.map.get(i).get(j).equals('$')) {
                    numb = map_o_ints[i][j];
                    path[i][j] = '$';
                    X = i;
                    Y = j;
                }
                if (main_activity.map.get(i).get(j).equals('@')) {
                    path[i][j] = '@';
                }
            }
        }

        do {
            for (int x = map_o_ints.length - 1; x >= 0; x--) {
                for (int y = 0; y < map_o_ints[x].length; y++) {

                    if (main_activity.map.get(x).get(y).equals(" ")) {
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

        } while (numb != 1);
        file_operator.writer(path, main_activity.map_selected, "maps_done");
        main_activity.stringBuilder.append("\nSolution:\n");
        for (int i = 0; i < path.length; i++) {
            for (int j = 0; j < path[i].length; j++) {
                main_activity.stringBuilder.append(path[i][j]);
            }
            main_activity.stringBuilder.append("\n");
        }
    }

}
