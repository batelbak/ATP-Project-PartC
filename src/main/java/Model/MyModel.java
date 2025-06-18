package Model;

import algorithms.mazeGenerators.Maze;
import algorithms.search.*;
import algorithms.mazeGenerators.Position;

import java.io.*;
import java.util.List;

/**
 * Implementation of the Model layer (in MVVM) for handling maze logic.
 * Manages the maze generation, solving, and character movement.
 */
public class MyModel implements IModel {

    private Maze maze;
    private MazeState characterPosition;
    private Solution solution;

    /**
     * Generates a new maze with the given dimensions using a generator.
     * Initializes the character's position to the start of the maze.
     *
     * @param rows number of maze rows
     * @param cols number of maze columns
     */
    @Override
    public void generateMaze(int rows, int cols) {
        // You can replace MyMazeGenerator with a different generator from your JAR if needed
        algorithms.mazeGenerators.MyMazeGenerator generator = new algorithms.mazeGenerators.MyMazeGenerator();
        maze = generator.generate(rows, cols);
        characterPosition = new MazeState(maze.getStartPosition());
        solution = null; // reset previous solution
    }

    /**
     * Solves the current maze using a search algorithm (BestFirstSearch by default).
     * Stores the resulting solution path.
     */
    @Override
    public void solveMaze() {
        if (maze == null) return;
        ISearchable searchableMaze = new SearchableMaze(maze);
        ISearchingAlgorithm algorithm = new BestFirstSearch(); // or use BFS / DFS
        solution = algorithm.solve(searchableMaze);
    }

    /**
     * Attempts to move the character one step in the given direction.
     * Updates the character's position if the move is valid (within maze bounds and not a wall).
     *
     * @param direction "UP", "DOWN", "LEFT", or "RIGHT"
     */
    @Override
    public void moveCharacter(String direction) {
        if (maze == null || characterPosition == null) return;

        int row = characterPosition.getPosition().getRowIndex();
        int col = characterPosition.getPosition().getColumnIndex();

        int newRow = row;
        int newCol = col;

        // Update position based on direction
        switch (direction.toUpperCase()) {
            case "UP": newRow--; break;
            case "DOWN": newRow++; break;
            case "LEFT": newCol--; break;
            case "RIGHT": newCol++; break;
        }

        // Move if the target cell is valid
        if (isValidMove(newRow, newCol)) {
            characterPosition = new MazeState(new Position(newRow, newCol));
        }
    }

    /**
     * Checks if a given row and column represent a valid move (in bounds and not a wall).
     */
    private boolean isValidMove(int row, int col) {
        return row >= 0 && row < maze.getMaze().length &&
                col >= 0 && col < maze.getMaze()[0].length &&
                maze.getMaze()[row][col] == 0;
    }

    /**
     * Saves the current maze to a file using Java object serialization.
     *
     * @param file target file to save to
     * @throws IOException if saving fails
     */
    @Override
    public void saveMaze(File file) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(maze);
        }
    }

    /**
     * Loads a maze from a file and resets the character's position and solution.
     *
     * @param file file to load maze from
     * @throws IOException if loading fails
     * @throws ClassNotFoundException if the class in the file cannot be deserialized
     */
    @Override
    public void loadMaze(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            maze = (Maze) in.readObject();
            characterPosition = new MazeState(maze.getStartPosition());
            solution = null;
        }
    }

    /**
     * Returns the maze as a 2D array of integers (0 = free, 1 = wall).
     */
    @Override
    public int[][] getMaze() {
        return maze != null ? maze.getMaze() : null;
    }

    /**
     * Returns the character's current row and column in the maze.
     */
    @Override
    public int[] getCharacterPosition() {
        if (characterPosition == null) return new int[]{-1, -1};
        return new int[]{
                characterPosition.getPosition().getRowIndex(),
                characterPosition.getPosition().getColumnIndex()
        };
    }

    /**
     * Returns the solution path as a list of AStates, or null if not solved.
     */
    @Override
    public List<AState> getSolution() {
        return solution != null ? solution.getSolutionPath() : null;
    }
}
