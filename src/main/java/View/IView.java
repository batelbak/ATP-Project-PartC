package View;

/**
 * Interface for View layer in MVVM architecture
 */
public interface IView {

    /**
     * Display maze on the screen
     * @param maze the maze data to display
     */
    void displayMaze(int[][] maze);

    /**
     * Update character position in the maze
     * @param row new row position
     * @param col new column position
     */
    void updateCharacterPosition(int row, int col);
    /**
     * Show alert/error message to user
     * @param message the message to display
     */
    void showAlert(String message);
    /**
     * Show success message when maze is solved
     */
    void showMazeSolved();
    /**
     * Get maze dimensions from user input
     * @return array with [rows, columns] or null if cancelled
     */
    int[] getMazeDimensions();

    /**
     * Show file chooser for saving maze
     * @return selected file path or null if cancelled
     */
    String showSaveFileDialog();

    /**
     * Show file chooser for loading maze
     * @return selected file path or null if cancelled
     */
    String showLoadFileDialog();
}