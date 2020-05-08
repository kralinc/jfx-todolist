/*
 *Author: Liam Andrade
 *This: ListDisplay.java
 *Purpose: Handle the display of the to-do list on the screen.
*/
package listBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *
 * @author karl
 */
public class ListDisplay {
    
    public static final String RESOURCES_LOCATION = new File(ListBuilder.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile().getAbsolutePath() + "/resources/";
    
    /*
        Display the to-do window
    */
    public static void show(Stage stage) {
        stage.setTitle("To-Do List");
        
        
        Date todaysDate = new Date();
        String format = String.format("%1$tA, %1$te %1$tB %1$tY", todaysDate);
        File styleSheet = new File(RESOURCES_LOCATION + "appStyle.css");
        
        //The next 5 lines set the welcome label. I really wish it could be less than 5 lines.
        Text welcomeLabel = new Text();
        welcomeLabel.setText("Welcome! Today is " + format);
        welcomeLabel.setX(25);
        welcomeLabel.setY(25);
        welcomeLabel.setFont(Font.font("impact", FontWeight.BOLD, FontPosture.REGULAR, 25));
        
        //This button launches the list builder when clicked
        Button launchBuilder = new Button("Edit");
        launchBuilder.setLayoutX(690);
        launchBuilder.setLayoutY(740);
        launchBuilder.setOnAction(ListDisplay.eh);
        
        //root is the group under which all other elements are stored
        Group root = new Group(welcomeLabel, launchBuilder);
        
        //This is the link to all other non-event functions in this file. Follow this to find the rest of the functionality
        Group toDoList = ListDisplay.LoadText();
        
        //Layout and style loading.
        root.getChildren().add(toDoList);
        Scene scene = new Scene(root, 750, 780);
        scene.getStylesheets().add("File://" + styleSheet.getAbsolutePath());
        stage.setScene(scene);
        stage.show();
    }
    
    
    //todo have the tasks reload when saved
    public static void reload() {
        
    }
    
    /*
        This is the "master function" that assembles the tasks into a single file, reads the file, and interprets the text to create tasks
    */
    public static Group LoadText(){
        Group g = new Group();
        
        VBox taskBox = new VBox();
        taskBox.setLayoutX(30);
        taskBox.setLayoutY(45);
        taskBox.setSpacing(15);
        
        Date date = new Date();
        String today = String.format("%1$tY-%1$tm-%1$td", date);
        
        File list = new File(RESOURCES_LOCATION + "lists/" + today + ".txt"); 
        addRegularTasks(today);
        
        if (!list.exists()){
            Text noneFound = new Text ("No tasks found for " + today);
            noneFound.setLayoutX(25);
            noneFound.setLayoutY(75);
            noneFound.getStyleClass().add("checkBox");
            g.getChildren().add(noneFound);
            return g;
        }
        
        try (BufferedReader bReader = new BufferedReader(new FileReader(list))){
            
            String line;
            
            /*
                This while loop reads the file and creates a new item in the list for each line.
                If the task has already been completed, the checkbox will already be checked.
            
            */
            
            while ((line = bReader.readLine()) != null) {
                boolean completed = line.endsWith("~\n");
                line = line.replace("~", "\u0000");


                CheckBox newItem = new CheckBox(line);
                newItem.getStyleClass().add("checkBox");
                newItem.setSelected(completed);

                newItem.setOnAction(eh);

                taskBox.getChildren().add(newItem);
            }
            
        } catch (IOException e) {
        }
        
        g.getChildren().add(taskBox);
        
        return g;
    }
    
    /*
        This adds events that occur on a regular basis.
    */
    static String regularEvents(){
        
        Date d = new Date();
        String eventDate;
        try (BufferedReader beter = new BufferedReader(new FileReader(RESOURCES_LOCATION + "REGULAR_TASKS_LIST.txt"))){
            String line;
            while ((line = beter.readLine()) != null){
                String[] splitLine = line.split("#");
                eventDate = String.format(splitLine[0], d);
                if (splitLine[1].equals(eventDate)) {
                    return splitLine[2];
                }
            }
        }catch (IOException e){
        }
        return "";
    }
    
    /*
        Add regularly scheduled events to the end of the file, if they have not already been.
    */
    public static void addRegularTasks(String today) {
        String tasksToWrite;
        tasksToWrite = regularEvents();
        //tasksToWrite = dailyEvents(today);
        //Check if the last line equals 
        if (!tasksToWrite.equals("")) {
            try (BufferedReader beter = new BufferedReader(new FileReader(RESOURCES_LOCATION + "lists/" + today + ".txt"))){
                String line;
                while ((line = beter.readLine()) != null){
                    //Cancel the write if the task already exists in the file.
                    if (line.trim().replace("~", "").equals(tasksToWrite.trim())) {
                        return;
                    }
                }
            }catch (IOException e){
            }

            try(Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(RESOURCES_LOCATION + "lists/" + today + ".txt", true), "UTF-8"))) {
                writer.write("\n" + (tasksToWrite.trim()));
            } catch (IOException e) {
            }
        }
        
    }
    
    /*
        When a checkbox is clicked, it will save its state as checked or unchecked in the text file
    */
    static void saveCheckBoxes(CheckBox task) {
        
        Date date = new Date();
        String today = String.format("%1$tY-%1$tm-%1$td", date);
        File list = new File(RESOURCES_LOCATION + "lists/" + today + ".txt");
        
        try (BufferedReader beter = new BufferedReader(new FileReader(list))){
                    
            String line;
            String outputStream = "";

            /*
                Reads through the list of tasks, and performs a simple operation on them.
                If it is selected, but shouldn't be, deselect it. If it isn't selected, but should be, select it.
                At the end, add the line to the output stream to be written to the file.
            */
            while ((line = beter.readLine()) != null) {
                if (line.replace("~", "").equals(task.getText())) {
                    line = line.replace("~", "");
                    if (task.isSelected()) {
                        line += "~";
                    }
                }
                outputStream += line + "\n";
            }

           try(Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(list), "UTF-8"))) {
               writer.write(outputStream.trim());
            }catch (IOException e) {
            
            }

        }catch(IOException e){
        }
    }
    
    public static EventHandler eh = (EventHandler<ActionEvent>) new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            
            if (event.getSource() instanceof CheckBox) {
                CheckBox task = (CheckBox) event.getSource();
                saveCheckBoxes(task);
            }else if (event.getSource() instanceof Button) {
                ListBuilder.open();
            }
            
        }
    };
}
