# Lee App

This is the  main part of mobile application Lee

android: 10

## Main Activity

### onCreate

This is the main function for the application. Firstly on launch it checks wether WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE, ACCESS_WIFI_STATE, ACCESS_NETWORK_STATE and INTERNET permissions are granted to the application.

Then application [updates spinner](#update-spinner) and [creates directories](#dir-creator) (if not created).

#### Connection button

Will switch view to server-connection_layout for further server [connection](#server-connection).

#### run button

Will firstly check if check box is checked. If so application will call [create map](#create-map) function and set selected map to newly created. If false program will check "Clear Field" is selected in dropdown menu. If so it'll simply clear the workspace. If not it'll check if selected map matches regex "s map ." meaning "server map". If so program will use [send](#send) function to send requested map to the server and wait for response with further solving (or not) the selected map and replying to the server. If selected map does not match regex it is expected to be one of generated or solved maps. In this case [reader](#reader) is called to read the map. Then in case map does not contains "done" in its name [calculate path](#calculate-path) function will be called. After all that the result is displayed in [textView](#textview)

#### checkBox

Is used to determine wether [run button](#run-button) will handle selected map or prosed to generating new map.

#### user_map_button

Pressing this button will take text from [textView](#textview) and try to solve the map provided by user.

It'll check if user has provided player "@" and goal "$". If not map will not be solved and user will be notified by Toast.

Then application will remove all unnecessary characters with " " (spaces) and check if maps dimensions are X by Y. Meaning no line should be shorter or longer.

If all above is fulfilled program will [calculate path](#calculate-path) and display the result in [textView](#textview).

#### delete_button

Will call [delete selected file](#delete-selected-file) and [update spinner](#update-spinner).

#### textView

Is the main field for displaying maps and/or user input.

Displays as follows:

INPUT:
Selected map

OUTPUT:
Selected map solved or [UnableToFindSolutionException](#unabletofindsolutionexception)

#### spinner

Is the dropdown list with available maps and "Clear Field" as an option (see [run button](#run-button)).

### delete selected file

Will walk files directory searching for requested map to be deleted.

### update spinner

Updates the list of available maps for [spinner](#spinner). Will always contain "Clear Field" (see [run button](#run-button)). Will contain all the maps found by [list files](#list-files) and, if available, [server](#server-connection) provided maps.

### checkPermission

Will check and if not granted request to grant permissions to run app correctly.

### dir creator

Will create "maps" and "maps_done" directories under "files" directory to store unsolved("maps") and solved("maps_done") maps.

### list files

Will walk directory "files" searching for files matching regex that represent maps. And add them to array of maps (see [update spinner](#update-spinner)).

### reader

Will read file whose name is provided and add contains to the map variable.

### create map

Will create map that contains both player("@") and goal("$") as well as fields("+") and barriers(" ").

Dimensions are from 5x5 up to 26x26.

Correctness of maps are checked by [check correctness](#check-correctness) function.

After map is created it is [written](#writer) under "c_map_(number)" in maps directory.

### check correctness

Is a helper function for [create map](#create-map) that ensures that both player("@") and goal("$") are present in generated map.

### writer

Will write provided map to the destination("maps" or "maps_done") directories. If destination is "maps" simple ".txt" will be added to the files mane, if destination is "maps_done" - "_done.txt" will be added to the files name. After function has successfully written map [update spinner](#update-spinner) will be called.

### calculate path

This is the heart of Lee algorithm. This function will fill map_o_ints map with integers representing steps required to get from "@" to "$". If it is path("+") than it will be field with distance from "@", if it is barrier(" ") INTEGER.MAX_VALUE is placed for further successful calculation. This function uses [check](#check) function to check wether solution has deerhound. If there are already 200 iterations [UnableToFindSolutionException](#unabletofindsolutionexception) will be thrown. If solution has been successfully found [build path](#build-path) is called with filed map_o_ints (representing solution).

### check

Helper function for [calculate path](#calculate-path).

It will iterate over provided map with integer to determine wether previous and current maps are not equal and if goal("$") has been reached. If so function will return false first time for [calculate path](#calculate-path) to iterate one more time and true second time to finish calculation.

### build path

Will fill path map with "*" in place of "+" representing the shortest path from player("@") to goal("$"). It will iterate from number under which goal is located until number equals 1 representing that we are near player.

After algorithm is done [written](#writer) is called to write solved map to "maps_done" folder and stringBuilder will receive solved map.

## UnableToFindSolutionException

This exception is thrown when algorithm can't find solution for map.

See  [calculate path](#calculate-path)

## Server Connection

Is the class for server_connection_layout layout. It is responsible for communication between Lee_Server and this application.

### onCreate

Is responsible for view or server_connection_layout. Handles multiple buttons and textViews.

Will check if connection is established. If so [status field](#status-field) will contain phrase "Connected!" in green color, if not phrase "Not Connected!" in red color. Also it will fill [ip field](#ip-field) with saved IP and [port field](#port-field) with provided port (default 4000).

#### back button

Will return to activity_main layout.

See [Main Activity](#main-activity)

#### connect button

Will check if provided values in [ip field](#ip-field) and [port field](#port-field) are correct and then (if not have been done) proceed to create new [ConnectionHandler](#connectionhandler) variable with IP and PORT. Then start newly created Thread and pass connection variable to [Main Activity](#main-activity).

#### disconnect button

If connection is established will use [send](#send) function to send "DISCONNECT" message to the server to close the connection.

#### ip field

Field for user to write IP provided by the server to connect.

#### port field

Field for user to write PORT provided by the server to connect.

#### status field

Displays the connection status with the server.

Green "Connected!" or red "Not connected!"

## ConnectionHandler

This class is used to establish and keep the connection with the server side. Uses Thread and Socket to operate correctly.

See [Server Connection](#server-connection)


### send

Updates message variable with newly provided and notified [run](#run).

### run

This is where the magic is done.

Firstly function will all the necessary variables to communicate with the server. Then when connection is established client (this function) expects to receive available maps from server. Then it will execute requests as long as connection is established.

If message is "DISCONNECT" function will send the message to the server and change [status field](#status-field) to red "Not Connected!"

If it is map that requested function will send request to the server then await for server to send map itself. Then MainActivity will be notified to continue its work to solve the map, wait for invoke, and write response to te server.

In finally closure there is clearing server_maps variable in [Main Activity](#main-activity), closing necessary communications and connection itself.

## Summary

All in all this is not the best attempt in creation of such application. If any stranger stumbles upon some bugs please advise me and don't fix them by yourself (I want to develope my skills). But still I am more than satisfied with the result
