package Model;

import java.io.File;
import java.io.IOException;
import algorithms.mazeGenerators.Position;

public interface IModel {
    void generateMaze(int rows, int cols);
    void solveMaze();
    void saveMaze(File file) throws IOException;
    void loadMaze(File file) throws IOException, ClassNotFoundException;

    int[][] getMaze(); // 2D maze data for drawing
    int[] getCharacterPosition(); // current [row, col] of player
    void moveCharacter(String direction); // "UP", "DOWN", "LEFT", "RIGHT"
    java.util.List<algorithms.search.AState> getSolution(); // for drawing path
    Position getGoalPosition();
}