package Gobang.Client;

public class Dot {
	private int col;
	private int row;
	private int color;
	public Dot(int row, int col, int color) {
		this.row = row;
		this.col = col;
		this.color = color;
	}
	public int getCol() {
		return col;
	}
	public int getRow() {
		return row;
	}
	public int getColor() {
		return color;
	}
}