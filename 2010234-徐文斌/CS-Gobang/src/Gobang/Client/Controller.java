package Gobang.Client;

import java.io.IOException;
import java.util.LinkedList;
import javax.swing.JOptionPane;

public class Controller {
	private static Controller instance = null;
	private static int color;
	private static boolean isJudgingWhePlay = false;
	private static boolean yourTurn = false;
	private static boolean gameBegin = false;
	private static boolean connecting = false;
	private static int perTime = 30;
	private static int remainTime = 0;
	private static LinkedList<Dot> chessRecord;
	private static String yourName;
	private Controller() {
		chessRecord = new LinkedList<>();
	};
	public static Controller getInstance() {
		if (instance == null)
			instance = new Controller();
		return instance;
	}
	public String getYourName() {
		return yourName;
	}
	public void setColor(int color) {
		Controller.color = color;
	}
	public void putChess(int row, int col) {
		if (yourTurn == false || gameBegin == false)
			return;
		boolean success = Model.getInstance().putChess(row, col, color);
		if (success) {
			chessRecord.add(new Dot(row, col, color));
			View2.getInstance().playPutChessMusic("music/putChessMusic.wav");
			NetAssistant.getInstance().putChess(row, col, color);
			View2.getInstance().upDate();
			boolean win = Model.getInstance().ifWin();
			if (win == true){
				try {
					NetAssistant.getInstance().sendMessage("Win:" + color);
				} catch (IOException e) {
					System.out.println("与服务器连接异常");
				}
				gameOver(color);
			}
			yourTurn = false;
		}
	}
	public void start() {
		View1.getInstance().exec();
		boolean successConnectToServer = NetAssistant.getInstance().tryConnectToServer();
		if (successConnectToServer) {
			NetAssistant.getInstance().startReceiverMessageThread();
			yourName = JOptionPane.showInputDialog(null, "请输入你的大名", "连接成功", JOptionPane.PLAIN_MESSAGE);
			while (yourName == null || yourName.equals("")) {
				yourName = JOptionPane.showInputDialog(null, "请再想想你的大名", "名字重复或不合法", JOptionPane.PLAIN_MESSAGE);
			}
			try {
				NetAssistant.getInstance().sendMessage(yourName);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else {
			JOptionPane.showMessageDialog(null, "连接服务器失败，请重启", "Over", JOptionPane.DEFAULT_OPTION);
			return;
		}
	}
	public void nameRepeat() {
		yourName = JOptionPane.showInputDialog(null, "请再想想你的大名", "名字重复", JOptionPane.PLAIN_MESSAGE);
		try {
			NetAssistant.getInstance().sendMessage(yourName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void refreshPlayerList(String names) {
		View1.getInstance().refreshPlayerList(names);
	}
	public void tryToPlayWithOthers(String opponentName) {
		try {
			NetAssistant.getInstance().sendMessage("WantToPlayWith:" + opponentName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public int someOneWantToPlayWithYou(String name) {
		if (isJudgingWhePlay == false)
			isJudgingWhePlay = true;
		else {
			while (isJudgingWhePlay) {
				if (connecting == true)
					return 1;
			}
		}
		if (connecting == true) {
			return 1;
		}
		int aggree = JOptionPane.showConfirmDialog(null, name + "想和你玩游戏", "开吗？", JOptionPane.YES_NO_OPTION);
		isJudgingWhePlay = false;
		if (aggree == 0)
			connecting = true;
		return aggree;
	}
	public void someOneAggreeToPlayWithYou(String name) {
		successfullyConnect(Model.BLACK);
	}
	public void successfullyConnect(int color) {
		chessRecord.clear();
		connecting = true;
		Controller.color = color;
		if (color == Model.BLACK)
			yourTurn = true;
		View1.getInstance().exit();
		View2.getInstance().exec();
		Model.getInstance().clearChessBoard();
		View2.getInstance().successfullyConnectInit();
	}
	public void someOneDisaggreeToPlayWithYou(String name) {
		JOptionPane.showMessageDialog(null, name + "不想和你玩", "Over", JOptionPane.DEFAULT_OPTION);
	}
	private void startPlay() {
		gameBegin = true;
		chessRecord.clear();
		createTimingThread();
		Model.getInstance().clearChessBoard();
		View2.getInstance().startPlayInit();
	}
	private void createTimingThread() {
		new Thread() {
			@Override
			public void run() {
				remainTime = perTime;
				boolean lastTurn = false;
				long baseTime = System.currentTimeMillis();
				while (true) {
					if (gameBegin == false)
						return;
					if (yourTurn != lastTurn) {
						lastTurn = yourTurn;
						remainTime = perTime;
						baseTime = System.currentTimeMillis();
					}else {
						long nowTime = System.currentTimeMillis();
						long passTime = (nowTime - baseTime) / 1000;
						if (passTime <= perTime)
							remainTime = (int) (perTime - passTime);
						else {
							timeOver();
						}
						View2.getInstance().setRemainTime(remainTime, yourTurn);
					}
				}
			}
		}.start();
	}
	private void timeOver() {
		if (yourTurn == true) {
			yourTurn = false;
			try {
				NetAssistant.getInstance().sendMessage("TimeOver");
			} catch (IOException e) {
				System.out.println("与服务器连接异常");
			}			
		}
	}
	public void tryToStartPlay() {
		if (connecting == false) {
			View2.getInstance().showMessage("请先邀请玩家");
			return;
		}
		try {
			NetAssistant.getInstance().sendMessage("TryToPlay");
		} catch (IOException e) {
			System.out.println("与服务器连接异常");
		}
	}
	public int opponentWantToStartPlay() {
		int aggree = JOptionPane.showConfirmDialog(null, "对方希望开始游戏","开吗？",JOptionPane.YES_NO_OPTION);
		if (aggree == 0)
			startPlay();
		return aggree;
	}
	public void opponentAggreeStartPlay() {
		startPlay();
	}
	public void opponentRefuseStartPlay() {
		View2.getInstance().showMessage("对方拒绝开局");
	}
	public void putChess(int row, int col, int color) {
		yourTurn = true;
		chessRecord.add(new Dot(row, col, color));
		Model.getInstance().putChess(row, col, color);
		View2.getInstance().playPutChessMusic("music/putChessMusic.wav");
		View2.getInstance().upDate();
	}
	public void getMessageFromOpponent(String text) {
		View2.getInstance().showMessage(text);
	}
	public int opponentWantToRegretChess() {
		int aggree = JOptionPane.showConfirmDialog(null, "对方希望悔棋","允许吗？",JOptionPane.YES_NO_OPTION);
		if (aggree == 0) {
			aggreeOpponentToRegretChess();
		}else {
			refuseOpponentToRegretChess();
		}
		return aggree;
	}
	private void refuseOpponentToRegretChess() {
		
	}
	private void aggreeOpponentToRegretChess() {
		yourTurn = false;
		Model.getInstance().regretChess(chessRecord.getLast().getRow(), chessRecord.getLast().getCol());
		chessRecord.removeLast();
		View2.getInstance().upDate();
	}
	public void regretChessWasRefused() {
		View2.getInstance().showMessage("对方拒绝悔棋");
	}
	public void regretChessWasAggreed() {
		yourTurn = true;
		Model.getInstance().regretChess(chessRecord.getLast().getRow(), chessRecord.getLast().getCol());
		chessRecord.removeLast();
		View2.getInstance().upDate();
		View2.getInstance().showMessage("对方同意悔棋");
	}
	public void opponentTimeOver() {
		yourTurn = true;
	}
	public void gameOver(int color) {
		gameBegin = false;
		boolean youWin = (color == Controller.color) ? true : false;
		View2.getInstance().gameOverInit(youWin);
	}
	public boolean sendMessageToOpponent(String text) {
		if (connecting == false)
			return false;
		View2.getInstance().showMessage("你：" + text);
		try {
			NetAssistant.getInstance().sendMessage("Message:" + text);
		} catch (IOException e) {
			System.out.println("与服务器连接异常");
		}
		return true;
	}
	public void wantToRegretChess() {
		if (yourTurn || !gameBegin || !connecting)
			return;
		try {
			NetAssistant.getInstance().sendMessage("HuiQi:Want");
		} catch (IOException e) {
			System.out.println("与服务器连接异常");
		}
	}
	public int getLastRow() {
		if (chessRecord == null || chessRecord.isEmpty())
			return -1;
		Dot lastDot = chessRecord.getLast();
		return lastDot.getRow();
	}
	public int getLastCol() {
		if (chessRecord == null || chessRecord.isEmpty())
			return -1;
		Dot lastDot = chessRecord.getLast();
		return lastDot.getCol();
	}
	public LinkedList<Dot> getChessRecord() {
		return chessRecord;
	}
	public void opponentLost() {
		if (connecting == false)
			return;
		connecting = false;
		gameBegin = false;
		JOptionPane.showMessageDialog(null, "对面大抵是掉线了/(ㄒoㄒ)/~~", "Over", JOptionPane.DEFAULT_OPTION);
		View2.getInstance().opponentLostInit();
	}
	public void quitGame() {
		try {
			if (connecting == true)
				NetAssistant.getInstance().sendMessage("QuitGame");
			gameBegin = false;
			connecting = false;
			View1.getInstance().exec();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public boolean sendMessageToWorld(String text) {
		try {
			NetAssistant.getInstance().sendMessage("ToWorld:" + yourName + ":" + text);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	public void getMEssageFromWorld(String text) {
		if (View1.getInstance().isWork()) {
			View1.getInstance().showWorldMessage(text);
		}
	}
	public void someOneEnteredHall(String name) {
		View1.getInstance().peopleComeIn(name);
	}
	public void someOneGotOutHall(String name) {
		View1.getInstance().peopleComeOut(name);
	}
	public void setPeoPleIsBusy(String name) {
		View1.getInstance().setPeopleBusy(name);
	}
	public void setPeoPleIsFree(String name) {
		View1.getInstance().setPeopleFree(name);
	}
	public void thePeopleInviteYouLost() {
		connecting = false;
		JOptionPane.showMessageDialog(null, "邀请你的人和你玩不了了", "Over", JOptionPane.DEFAULT_OPTION);
	}
}
