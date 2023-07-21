public class App {
    public static void main(String[] args) throws Exception {
        start("D:\\sudoku\\sudoku-csp\\Sudoku1.txt");
        start("D:\\sudoku\\sudoku-csp\\Sudoku2.txt");
        start("D:\\sudoku\\sudoku-csp\\Sudoku3.txt");
        start("D:\\sudoku\\sudoku-csp\\Sudoku4.txt");
        start("D:\\sudoku\\sudoku-csp\\Sudoku5.txt");


    }

    /**
     * Start AC-3 using the sudoku from the given filepath, and reports whether the sudoku could be solved or not, and how many steps the algorithm performed
     *
     * @param filePath
     */
    public static void start(String filePath) {
        Game game1 = new Game(new Sudoku(filePath));
        game1.showSudoku();
        game1.resetEvaluations();

        String[] heuristics = {"MRV","Degree","LCV","None"};
        for (String heuristic : heuristics) {
            game1.solve(heuristic);
            if (game1.validSolution()) {
                System.out.println("Solved using " + heuristic + " heuristic! Evaluations: " + game1.getEvaluations());
            } else {
                System.out.println("Could not solve this sudoku with " + heuristic + " heuristic :(");
            }
            game1.showSudoku();
            //game1.resetEvaluations();
        }
    }
}
