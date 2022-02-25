package Gobang.Client;

import java.awt.*;
import javax.swing.*;

public class ChessBoard extends JPanel{
	
	private static final long serialVersionUID = 1L;
	private int offset;
	private int width;
	private int height;
	private int sideLength;
	private int interval;
	private Image img;
	private Image screenShot;
	private Graphics gbuffer;
	public ChessBoard() {
		img = new ImageIcon("img/chessBackGround.jpg").getImage();
	}
	public void upDate() {
		if (screenShot == null) {
			screenShot = this.createImage(this.getWidth(), this.getHeight());
			gbuffer = screenShot.getGraphics();
			painting(gbuffer);
			paint(getGraphics());
		}
		screenShot = null;
	}
	private void painting(Graphics g) {
		g.setColor(Color.white);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		offset = 0;
		width = this.getWidth();
		height = this.getHeight();
		sideLength = Math.min(width, height);
		offset = sideLength / 20;
		interval = sideLength / Model.WIDTH;
		sideLength = interval * (Model.WIDTH - 1);
		g.drawImage(img, offset / 2, offset / 2, offset + sideLength, offset + sideLength, this);
		g.setColor(Color.black);
		for (int i = 0; i < Model.WIDTH; i++) {
			g.drawLine(offset, offset + i * interval, offset + sideLength, offset + i * interval);
			g.drawLine(offset + i * interval, offset, offset + i * interval, offset + sideLength);
		}
		int[][] array = Model.getInstance().getChessBoard();
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[i].length; j++) {
				int nowX = offset + j * interval;
				int nowY = offset + i * interval;
				if (array[i][j] == Model.BLACK) {
					g.setColor(Color.black);
				}else if (array[i][j] == Model.WHIET) {
					g.setColor(Color.white);					
				}else
					continue;
				g.fillOval(nowX - interval / 2, nowY - interval / 2, interval / 1, interval / 1);
				if (i == Controller.getInstance().getLastRow() && j == Controller.getInstance().getLastCol()) {
					g.setColor(Color.red);
					g.drawOval(nowX - interval / 2 - 1, nowY - interval / 2 - 1, interval / 1 + 1, interval / 1 + 1);
				}
			}
		}
		
	}
	@Override
	public void paint(Graphics g) {
		if (screenShot == null) {
			painting(g);
			return;
		}
		g.drawImage(screenShot, 0, 0, null);
	}
	public int getRow(int x, int y) {
		for (int i = 0; i < Model.WIDTH; i++) {
			for (int j = 0; j < Model.WIDTH; j++) {
				int nowX = offset + j * interval;
				int nowY = offset + i * interval;
				if (Math.abs(x - nowX) <= interval / 2 && Math.abs(y - nowY) <= interval / 2)
					return i;
			}
		}
		return -1;
	}
	public int getCol(int x, int y) {
		for (int i = 0; i < Model.WIDTH; i++) {
			for (int j = 0; j < Model.WIDTH; j++) {
				int nowX = offset + j * interval;
				int nowY = offset + i * interval;
				if (Math.abs(x - nowX) <= interval / 2 && Math.abs(y - nowY) <= interval / 2)
					return j;
			}
		}
		return -1;
	}
}
