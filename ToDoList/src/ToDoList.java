/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author karl
 */

import javafx.application.Application;
import javafx.stage.Stage;

//import listBuilder.ListBuilder;
import listBuilder.ListDisplay;

public class ToDoList extends Application {

    @Override
    public void start(Stage stage) {
        ListDisplay.show(stage);
    }

    public static void main(String[] args) {
        launch();
    }
}

