import java.util.ArrayDeque;
import java.util.Queue;
import java.util.HashSet;
import java.util.Set;
import java.util.*;
public class Game {
  private Sudoku sudoku;
  private int evaluations;

  Game(Sudoku sudoku) {
    this.sudoku = sudoku;
    evaluations = 0;
  }

  public void showSudoku() {
    System.out.println(sudoku);

  }

  /**
   * Implementation of the AC-3 algorithm
   * 
   * @return true if the constraints can be satisfied, else false
   */
  public boolean solve(String heuristic) {
    // Reset evaluations count for each solve call
    //evaluations = 0;
    Queue<Field> queue = new ArrayDeque<>();
    for (Field[] row : sudoku.getBoard()) {
      for (Field field : row) {
        if (field.getValue() != 0) {
          queue.add(field);
        }
      }
    }

    while (!queue.isEmpty()) {
      Field current = queue.poll();
      for (Field neighbor : current.getNeighbours()) {
        if (arcReduce(neighbor, current)) {
          if (neighbor.getDomainSize() == 0) {
            return false; // Invalid sudoku state, domain becomes empty
          }
          queue.add(neighbor);
        }
      }
    }

    return backtrackSolve(heuristic);
  }

  /**
   * Use backtracking to complete the sudoku after applying AC-3 algorithm
   *
   * @return true if the sudoku is successfully completed, else false
   */
  private boolean backtrackSolve(String heuristic) {
    return backtrack(heuristic);
  }

  private boolean backtrack(String heuristic) {
    // Find the next empty field
    int[] nextEmpty = findNextEmpty();
    int row = nextEmpty[0];
    int col = nextEmpty[1];

    // If there are no empty fields, the sudoku is solved
    if (row == -1) {
      return true;
    }


    List<Integer> orderedValues;
    if (heuristic.equals("MRV")) {
      orderedValues = getValuesOrderedByMRV(sudoku.getBoard()[row][col]);
    } else if (heuristic.equals("Degree")) {
      orderedValues = getValuesOrderedByDegree(sudoku.getBoard()[row][col]);
    } else if (heuristic.equals("LCV")) {
      orderedValues = getValuesOrderedByLCV(sudoku.getBoard()[row][col]);
    } else {
      orderedValues = new ArrayList<>();
      for (int i = 1; i <= 9; i++) {
        orderedValues.add(i);
      }
    }

    for (int value : orderedValues) {
      evaluations++;
      if (isValidPlacement(row, col, value)) {
        sudoku.getBoard()[row][col].setValue(value);
        if (backtrack(heuristic)) {
          return true;
        }
        sudoku.getBoard()[row][col].setValue(0);
      }
    }
    // No value can be assigned to the current empty field, backtrack
    return false;
  }

  private int[] findNextEmpty() {
    for (int i = 0; i < 9; i++) {
      for (int j = 0; j < 9; j++) {
        if (sudoku.getBoard()[i][j].getValue() == 0) {
          return new int[]{i, j};
        }
      }
    }
    return new int[]{-1, -1}; // Return -1 if no empty field is found
  }

  private boolean isValidPlacement(int row, int col, int value) {
    // Check row
    for (int j = 0; j < 9; j++) {
      if (sudoku.getBoard()[row][j].getValue() == value) {
        return false;
      }
    }

    // Check column
    for (int i = 0; i < 9; i++) {
      if (sudoku.getBoard()[i][col].getValue() == value) {
        return false;
      }
    }

    // Check 3x3 box
    int boxStartRow = 3 * (row / 3);
    int boxStartCol = 3 * (col / 3);
    for (int i = boxStartRow; i < boxStartRow + 3; i++) {
      for (int j = boxStartCol; j < boxStartCol + 3; j++) {
        if (sudoku.getBoard()[i][j].getValue() == value) {
          return false;
        }
      }
    }

    return true;
  }

  private boolean arcReduce(Field neighbor, Field current) {
    evaluations++;
    if (neighbor.removeFromDomain(current.getValue())) {
      if (neighbor.getDomainSize() == 0) {
        return true; // Domain reduced to zero, invalid state
      }
      if (neighbor.getDomainSize() == 1 && neighbor.getValue() == 0) {
        neighbor.setValue(neighbor.getDomain().get(0)); // Only one value left in domain, assign it
      }
    }
    return false;
  }

  public int getEvaluations() {
    return evaluations;
  }

  /**
   * Checks the validity of a sudoku solution
   * 
   * @return true if the sudoku solution is correct
   */

  public boolean validSolution() {

    // Check each row
    for (int i = 0; i < 9; i++) {
      if (!isUnique(sudoku.getBoard()[i])) {
        return false;
      }
    }

    // Check each column
    for (int j = 0; j < 9; j++) {
      Field[] column = new Field[9];
      for (int i = 0; i < 9; i++) {
        column[i] = sudoku.getBoard()[i][j];
      }
      if (!isUnique(column)) {
        return false;
      }
    }

    // Check each 3x3 box
    for (int boxRow = 0; boxRow < 9; boxRow += 3) {
      for (int boxCol = 0; boxCol < 9; boxCol += 3) {
        Field[] box = new Field[9];
        int k = 0;
        for (int i = boxRow; i < boxRow + 3; i++) {
          for (int j = boxCol; j < boxCol + 3; j++) {
            box[k++] = sudoku.getBoard()[i][j];
          }
        }
        if (!isUnique(box)) {
          return false;
        }
      }
    }
    return true;
  }
  /**
   * Checks if the given array of fields contains unique values (1-9)
   *
   * @param fields The array of fields to check
   * @return true if the values are unique, else false
   */
  private boolean isUnique(Field[] fields) {
    Set<Integer> values = new HashSet<>();
    for (Field field : fields) {
      int value = field.getValue();
      if (value != 0) {
        if (values.contains(value)) {
          return false;
        }
        values.add(value);
      }
    }
    return true;
  }

  private List<Integer> getValuesOrderedByLCV(Field variable) {
    List<Integer> orderedValues = new ArrayList<>();
    if (variable.getDomainSize() == 0) {
      return orderedValues;
    }

    for (int value : variable.getDomain()) {
      int count = 0;
      for (Field neighbor : variable.getNeighbours()) {
        if (neighbor.getValue() == 0 && neighbor.getDomain().contains(value)) {
          count++;
        }
      }
      int index = 0;
      while (index < orderedValues.size() && count > getLCVCount(variable, orderedValues.get(index))) {
        index++;
      }
      orderedValues.add(index, value);
    }
    return orderedValues;
  }

  private int getLCVCount(Field variable, int value) {
    int count = 0;
    for (Field neighbor : variable.getNeighbours()) {
      if (neighbor.getValue() == 0 && neighbor.getDomain().contains(value)) {
        count++;
      }
    }
    return count;
  }
  private List<Integer> getValuesOrderedByMRV(Field variable) {
    List<Integer> orderedValues = new ArrayList<>();
    if (variable.getDomainSize() == 0) {
      return orderedValues;
    }

    Map<Integer, Integer> valueConstraintCount = new HashMap<>();
    for (int value : variable.getDomain()) {
      int count = 0;
      for (Field neighbor : variable.getNeighbours()) {
        if (neighbor.getValue() == 0 && neighbor.getDomain().contains(value)) {
          count++;
        }
      }
      valueConstraintCount.put(value, count);
    }

    // Sort values based on the constraint count in ascending order
    orderedValues.addAll(variable.getDomain());
    orderedValues.sort(Comparator.comparingInt(valueConstraintCount::get));

    return orderedValues;
  }

  private List<Integer> getValuesOrderedByDegree(Field variable) {
    List<Integer> orderedValues = new ArrayList<>();
    if (variable.getDomainSize() == 0) {
      return orderedValues;
    }

    Map<Integer, Integer> valueDegreeCount = new HashMap<>();
    for (int value : variable.getDomain()) {
      int degree = 0;
      for (Field neighbor : variable.getNeighbours()) {
        if (neighbor.getValue() == 0) {
          degree++;
        }
      }
      valueDegreeCount.put(value, degree);
    }

    // Sort values based on the degree count in descending order
    orderedValues.addAll(variable.getDomain());
    orderedValues.sort((a, b) -> valueDegreeCount.get(b) - valueDegreeCount.get(a));

    return orderedValues;
  }
  public void resetEvaluations() {
    evaluations = 0;
  }
}
