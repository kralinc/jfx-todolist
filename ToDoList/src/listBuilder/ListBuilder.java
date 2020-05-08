/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package listBuilder;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;

import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.text.Text;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.util.StringConverter;

/**
 *
 * @author karl
 */

public class ListBuilder {
    
    public static Scene scene;
    public static boolean editingRegularEvents;
    public static final String RESOURCES_LOCATION = new File(ListBuilder.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile().getAbsolutePath() + "/resources/";
    
    public static void open() {
        Stage listBuilderWindow = new Stage();
        
        Group gui = new Group();
        gui = buildGUI(gui);
        Group root = new Group();
        root.getChildren().add(gui);
        
        scene = new Scene(root, 800, 700);
        
        DatePicker today = (DatePicker) scene.lookup("#date");
        printTasks(today.getConverter().toString(today.getValue()));
        editingRegularEvents = false;
        File styleSheet = new File(RESOURCES_LOCATION + "appStyle.css");
        scene.getStylesheets().add("File://" + styleSheet.getAbsolutePath());
        
        listBuilderWindow.setTitle("List Builder");
        listBuilderWindow.setScene(scene);
        listBuilderWindow.show();
    }
    
    /*
        Lays out the GUI elements on the screen.
    */
    static Group buildGUI(Group g) {
        
        VBox uiContainer = new VBox();
        uiContainer.setSpacing(10);
        uiContainer.setLayoutX(10);
        uiContainer.setLayoutY(10);
        
        DatePicker day = new DatePicker();
        day.setId("date");
        //This converts the date format to yyyy-mm-dd
        day.setConverter(new StringConverter<LocalDate>() {
            private final DateTimeFormatter dateTimeFormatter;
            {
                this.dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            }

            @Override
            public String toString(LocalDate localDate)
            {
                if(localDate==null)
                    return "";
                return dateTimeFormatter.format(localDate);
            }

            @Override
            public LocalDate fromString(String dateString)
            {
                if(dateString==null || dateString.trim().isEmpty())
                {
                    return null;
                }
                return LocalDate.parse(dateString,dateTimeFormatter);
            }
        });
        
        day.setOnAction(eh);
        day.setValue(LocalDate.now());
        
        
        Button openRegularEvents = new Button("Open Regular Events");
        openRegularEvents.setId("regular");
        openRegularEvents.setOnAction(eh);
        
        TextArea listMaker = new TextArea();
        listMaker.setId("textArea");
        listMaker.setLayoutX(400);
        listMaker.setLayoutY(15);
        listMaker.setPrefColumnCount(25);
        listMaker.setPrefRowCount(40);
        
        Button saveChanges = new Button("Save");
        saveChanges.setId("save");
        saveChanges.setLayoutX(740);
        saveChanges.setLayoutY(650);
        saveChanges.setOnAction(eh);
        
        Button clean = new Button ("Clear Old Lists");
        clean.setId("clean");
        clean.setOnAction(eh);
        
        Text notifications = new Text();
        notifications.setId("notifications");
        
        uiContainer.getChildren().addAll(day, openRegularEvents, clean, notifications);
        
        g.getChildren().addAll(listMaker, saveChanges, uiContainer);
        return g;
    }
    

    static void printTasks(String day) {
        String tasks = "";
        Text t = (Text) scene.lookup("#notifications");
        TextArea ta = (TextArea) scene.lookup("#textArea");
        String line;
        //It's called beter because it's fun, shorter than "bufferedReader", and has a way more appropriate pronounciation than "breader"
        try(BufferedReader beter = new BufferedReader(new FileReader(RESOURCES_LOCATION + "lists/" + day + ".txt"))) {
            while ((line = beter.readLine()) != null) {
                tasks += line + "\n";
            }
            ta.setText(tasks);
            t.setText("");
        }catch (IOException e) {
            t.setText("No tasks found for " + day);
            ta.setText("");
        }
        
    }
    
    /*
        Writes the contents of the text area to the selected day's file
    */
    static void saveTasks (String day) {
        TextArea ta = (TextArea) scene.lookup("#textArea");
        String tasksToWrite = ta.getText();
        File outputFile = new File(RESOURCES_LOCATION + "lists/" + day + ".txt");
        
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"))){
            writer.write(tasksToWrite.trim());
        } catch (IOException e) {
        }
        Text t = (Text) scene.lookup("#notifications");
        t.setText("List saved for " + day);
        ListDisplay.LoadText();
    }
    
    static void loadRegularTasks () {
        String regularTasks = "";
        String line;
        
        try (BufferedReader beter = new BufferedReader(new FileReader(RESOURCES_LOCATION + "REGULAR_TASKS_LIST.txt"))) {
            while ((line = beter.readLine()) != null) {
                String[] lineParts = line.split("#");
                regularTasks += lineParts[0].replace("%1$t", "");
                regularTasks += " " + lineParts[1] + " " + lineParts[2] + "\n";
            }
        }catch (IOException e) {
            
        }
        
        TextArea ta = (TextArea) scene.lookup("#textArea");
        ta.setText(regularTasks);
    }
    
    static void saveRegularTasks () {
        TextArea ta = (TextArea) scene.lookup("#textArea");
        String tasksToWrite = "";
        String tasksFromTextField = ta.getText();
        
        /*
            This adjusts the formatting of the file input to fit the input requirements later.
            As an example, this would turn "d-m 01-01 Celebrate New Years'" into "%1$td-%1$tm#01-01#Celebrate New Years'"
        */
        for (String line : tasksFromTextField.split("\n")) {
            String[] lineParts = line.split(" ");
            for (char c : lineParts[0].toCharArray()) {
                if (Character.isAlphabetic(c)) {
                    tasksToWrite += "%1$t" + c;
                }else {
                    tasksToWrite += c;
                }
            }
            //Spacers and the target value of the schedule
            tasksToWrite += "#" + lineParts[1] + "#";
            
            //The text of the task
            for (int i = 2; i < lineParts.length; ++i) {
                tasksToWrite += lineParts[i] + " ";
            }
            tasksToWrite += "\n";
            
        }
        File outputFile = new File(RESOURCES_LOCATION + "REGULAR_TASKS_LIST.txt");
        
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"))){
            writer.write(tasksToWrite.trim());
        } catch (IOException e) {
        }
        Text t = (Text) scene.lookup("#notifications");
        t.setText("Regularly scheduled tasks saved");
    }
    
    /*
        This checks the list folder and deletes all lists that were for days that have already happened
    */
    static void clearOldLists() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();
        File listFolder = new File(RESOURCES_LOCATION + "lists/");
        File[] lists = listFolder.listFiles();
        Arrays.sort(lists);
        Text t = (Text) scene.lookup("#notifications");
        String notification = "";
        
        for (File list : lists) {
            if (today.isAfter(LocalDate.parse(list.getName().replace(".txt", ""), format))) {
                notification += "Deleted " + list.getName() + "\n";
                list.delete();
            }else {
                t.setText(notification);
                return;
            }
        }
        t.setText(notification);
    }
    
    
    static EventHandler eh = (EventHandler<ActionEvent>) new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            
            if (event.getSource() instanceof Button) {
                
                Button clicked = (Button) event.getSource();
                if (clicked.getId().equals("regular")) {
                    System.out.println("open regular events file");
                    editingRegularEvents = true;
                    loadRegularTasks();
                } else if (clicked.getId().equals("save")){
                    if (editingRegularEvents) {
                        System.out.println("save regs");
                        saveRegularTasks();
                    }else {
                        DatePicker dateSelector = (DatePicker) scene.lookup("#date");
                        String date = dateSelector.getConverter().toString(dateSelector.getValue());
                        saveTasks(date);
                    }
                    
                } else {
                    clearOldLists();
                }
                
            }else if (event.getSource() instanceof DatePicker) {
                
                DatePicker dateSelector = (DatePicker) event.getSource();
                String date = dateSelector.getConverter().toString(dateSelector.getValue());
                printTasks(date);
                editingRegularEvents = false;
            }
        }

    };
    
}
