import java.util.*;

public class Solver {

	private static final int[][] sudoku = new int[9][9];

	private static final String info = """
						
						Fill in the initial state of the sudoku puzzle by inputting one of the following:
						- a number from 0 - 9 to go into the blank. 0 is considered a blank square.
						- "back" or "b" to go back a square
						- "next" or "n" or "skip" or "s" to skip and go to the next
						- "info" or "i" to bring up this text again
						You will be able to go back and make changes later:
						""";

	private static final List<int[][]> solutions = new LinkedList<>();

	public static void main(String[] args) {
		generateSudoku();
		solve();
		showSolutions();
	}

	private static void generateSudoku() {
		Scanner scanner = new Scanner(System.in);
		System.out.println(info);

		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				System.out.printf("Line %d: %s", i + 1, getLineString(sudoku, i, j));
				System.out.print("Entry: ");
				int value = 0;
				boolean valueModified = true;

				valueQuery:
				do {
					while (!scanner.hasNextInt()) {
						String input = scanner.next();
						if (input.equals("back") || input.equals("b")) {
							j -= 2;
							if (j == -2) { //Check if it goes out of row bounds
								j = 7;
								i--;
								if (i == -1) { //Check if out of column bounds
									i = 0;
									j = -1;
									System.out.println("Cannot go back any further."); //This should only be triggered if command is typed at the first item of first row
								}
							}
							valueModified = false;
							break valueQuery;
						}
						if (input.equals("next") || input.equals("n") || input.equals("skip") || input.equals("s")) {
							valueModified = false;
							break valueQuery;
						}
						if (input.equals("info") || input.equals("i")) {
							System.out.println(info);
							valueModified = false;
							break valueQuery;
						}
						System.out.print("Not a valid input. Please reenter: ");
					}
					value = scanner.nextInt();
					if (value < 0 || value > 9) {
						System.out.print("Not a valid input. Please enter a number from 1 - 9: ");
					}
				} while (value < 0 || value > 9);

				if (valueModified) {
					sudoku[i][j] = value;
				}

				System.out.print("\n\n");
			}
		}

		boolean correct;
		do {
			printSudoku(sudoku);
			System.out.print("Is this correct? y\\n: ");

			String input = scanner.next();
			correct = input.equals("yes") || input.equals("y");

			if (!correct) {
				System.out.print("Row - ");
				int row = getUserInputRange(scanner, 1, 9) - 1;
				if (row == -1)
					continue;
				System.out.print("Column - ");
				int col = getUserInputRange(scanner, 1, 9) - 1;
				if (col == -1)
					continue;

				System.out.print("Value - ");
				int val = getUserInputRange(scanner, 0, 9);
				if (val == -1)
					continue;

				sudoku[row][col] = val;
			}
		} while (!correct);

		scanner.close();
	}

	private static int getUserInputRange(Scanner scanner, int low, int high) {
		System.out.printf("enter a number from %d to %d. \"cancel\" or \"c\" to cancel: ", low, high);

		while (true) {
			while (!scanner.hasNextInt()) {
				String input = scanner.next();
				if (input.equals("cancel") || input.equals("c")) {
					return low - 1;
				}
				System.out.println("Not a valid input. Please reenter: ");
			}
			int value = scanner.nextInt();
			if (low <= value && value <= high) {
				return value;
			}
			System.out.printf("Not a valid input. Please enter a number from %d - %d: ", low, high);
		}
	}
	
	private static void printSudoku(int[][] sudoku) {
		final String horizontalLine = "-".repeat(25) + "\n";

		StringBuilder output = new StringBuilder(horizontalLine);

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				output.append(getLineString(sudoku, i * 3 + j));
			}
			output.append(horizontalLine);
		}

		System.out.println(output);
	}

	private static String getLineString(int[][] sudoku, int line) {
		return getLineString(sudoku, line, -1);
	}

	private static String getLineString(int[][] sudoku, int line, int selectedIndex) {
		StringBuilder lineString = new StringBuilder();

		for (int i = 0; i < 3; i++) {
			lineString.append("| ");
			for (int j = 0; j < 3; j++) {
				lineString.append(selectedIndex == i * 3 + j ? "_" : sudoku[line][i * 3 + j]).append(" ");
			}
		}

		return lineString.append("|\n").toString();
	}


	private static void solve() {
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				if (sudoku[i][j] == 0) {
					for (int k = 1; k <= 9; k++) {
						if (possible(i, j, k)) {
							sudoku[i][j] = k;
							solve();
							sudoku[i][j] = 0;
						}
					}
					return;
				}
			}
		}

		//Found solution at this point
		solutions.add(Arrays.stream(sudoku).map(int[]::clone).toArray($ -> sudoku.clone()));
	}

	private static boolean possible(int row, int col, int val) {
		//Check row
		if (Arrays
				.stream(sudoku[row])
				.anyMatch(sudokuVal -> sudokuVal == val))
		{
			return false;
		}

		//Check column
		if (Arrays
				.stream(sudoku)
				.map(ints -> ints[col])
				.anyMatch(sudokuVal -> sudokuVal == val))
		{
			return false;
		}

		//Check box
		return Arrays
				.stream(sudoku)
				.skip(row / 3 * 3)
				.limit(3)
				.flatMapToInt(sudokuRow -> Arrays.stream(sudokuRow)
						.skip(col / 3 * 3)
						.limit(3))
				.noneMatch(sudokuVal -> sudokuVal == val);
	}

	private static void showSolutions() {
		System.out.printf("Found %d solutions to this sudoku.\n\n", solutions.size());
		if (solutions.size() == 0) {
			return;
		}

		ListIterator<int[][]> sudokuIterator = solutions.listIterator();
		printSudoku(sudokuIterator.next());
		Scanner scanner = new Scanner(System.in);

		while (sudokuIterator.hasNext()) {
			System.out.println("\nPress enter for next solution");
			scanner.nextLine();
			printSudoku(sudokuIterator.next());
		}
	}
}
