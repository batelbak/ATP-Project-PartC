package ViewModel;

import Model.IModel;
import algorithms.search.AState;
import javafx.beans.property.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MyViewModel {
    private final IModel model;

    // Properties for data binding to View
    private final IntegerProperty playerRow = new SimpleIntegerProperty();
    private final IntegerProperty playerCol = new SimpleIntegerProperty();
    private final ObjectProperty<int[][]> maze = new SimpleObjectProperty<>();

    public MyViewModel(IModel model) {
        this.model = model;
    }

    public void generateMaze(int rows, int cols) {
        model.generateMaze(rows, cols);
        maze.set(model.getMaze());
        updateCharacterPosition();
    }

    public void solveMaze() {
        model.solveMaze();
    }

    public void moveCharacter(String direction) {
        model.moveCharacter(direction);
        updateCharacterPosition();
    }

    private void updateCharacterPosition() {
        int[] pos = model.getCharacterPosition();
        playerRow.set(pos[0]);
        playerCol.set(pos[1]);
    }

    public void saveMaze(File file) throws IOException {
        model.saveMaze(file);
    }

    public void loadMaze(File file) throws IOException, ClassNotFoundException {
        model.loadMaze(file);
        maze.set(model.getMaze());
        updateCharacterPosition();
    }

    public List<AState> getSolution() {
        return model.getSolution();
    }

    // Getters for properties
    public IntegerProperty playerRowProperty() {
        return playerRow;
    }

    public IntegerProperty playerColProperty() {
        return playerCol;
    }

    public ObjectProperty<int[][]> mazeProperty() {
        return maze;
    }
    public int getGoalRow() {
        return model.getGoalPosition().getRowIndex();
    }

    public int getGoalCol() {
        return model.getGoalPosition().getColumnIndex();
    }


}