package Gobang.Client;

public class Model {
	public static final int WIDTH = 15;
	public static final int BLACK = 1;
	public static final int WHIET = -1;
	public static final int SPACE = 0;
	private static Model instance = null;
	private int[][] array = new int[WIDTH][WIDTH];
	private static int lastRow;
	private static int lastCol;
	private Model() {
		for (int i = 0; i < WIDTH; i++) {
			for (int j = 0; j < WIDTH; j++) {
				array[i][j] = SPACE;
			}
		}
	};
	public static Model getInstance() {
		if (instance == null)
			instance = new Model();
		return instance;
	}

	public boolean putChess(int row, int col, int colo) {
		if (row <0 || row >= WIDTH || col < 0 || col >= WIDTH || array[row][col] != SPACE) {
			return false;
		}
		array[row][col] = colo;
		lastRow = row;
		lastCol = col;
		return true;
	}
	public boolean ifWin() {
		int num = 0;
		for (int i = 0; i < 5; i++) {
			num = 0;
			for (int j = lastCol + i; num < 5 && j >= 0 && j < WIDTH; j--) {
				if (array[lastRow][j] == array[lastRow][lastCol])
					num++;
				else
					break;
			}
			if (num == 5)
				return true;
		}
		for (int i = 0; i < 5; i++) {
			num = 0;
			for (int j = lastRow + i; num < 5 && j >= 0 && j < WIDTH; j--) {
				if (array[j][lastCol] == array[lastRow][lastCol])
					num++;
				else 
					break;
			}
			if (num == 5)
				return true;
		}
		for (int i = 0; i < 5; i++) {
			num = 0;
			for (int j = lastRow + i, k = lastCol + i; num < 5 && j >= 0 && j < WIDTH && k >= 0 && k < WIDTH; j--, k--) {
				if (array[j][k] == array[lastRow][lastCol])
					num++;
				else
					break;
			}
			if (num == 5)
				return true;
		}
		for (int i = 0; i < 5; i++) {
			num = 0;
			for (int j = lastRow - i, k = lastCol + i; num < 5 && j >= 0 && j < WIDTH && k >= 0 && k < WIDTH; j++, k--) {
				if (array[j][k] == array[lastRow][lastCol])
					num++;
				else
					break;
			}
			if (num == 5)
				return true;
		}
		return false;
	}
	public int[][] getChessBoard() {
		return array;
	}
	public void clearChessBoard() {
		for (int i = 0; i < array.length; i++)
			for (int j = 0; j < array[i].length; j++) 
				array[i][j] = SPACE;
	}
	public void regretChess(int row, int col) {
		array[row][col] = SPACE;
	}
}