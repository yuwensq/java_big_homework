package Gobang.Client;

import java.io.IOException;
import java.net.Socket;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class NetAssistant {
	private static NetAssistant instance;
	private static Socket s;
	private static BufferedReader in;
	private static BufferedWriter out;
	private NetAssistant() {
	};
	public static NetAssistant getInstance() {
		if (instance == null)
			instance = new NetAssistant();
		return instance;
	}
	public boolean tryConnectToServer() {
		try {
			s = new Socket("localhost", 1200);
			if (s == null || !s.isConnected())
				return false;
			in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	public void putChess(int row, int col, int color) {
		String output = "XiaQi:" + row + "," + col + "," + color;
		try {
			sendMessage(output);
		} catch (IOException e) {
			System.out.println("与服务器连接异常");
		}
	}
	public void sendMessage (String text) throws IOException {
		out.write(text + "\n");
		out.flush();
	}
	public void startReceiverMessageThread() {
		new Thread() {
			@Override
			public void run() {
				try {
					while (true) {
						String message = in.readLine();
						System.out.println(message);
						if (message.startsWith("XiaQi")) {
							String vars = message.substring(message.indexOf(':') + 1);
							int row = Integer.parseInt(vars.substring(0, vars.indexOf(',')));
							vars = vars.substring(vars.indexOf(',') + 1);
							int col = Integer.parseInt(vars.substring(0, vars.indexOf(',')));
							vars = vars.substring(vars.indexOf(',') + 1);
							int color = Integer.parseInt(vars);
							Controller.getInstance().putChess(row, col, color);
						}else if (message.startsWith("List")) {
							Controller.getInstance().refreshPlayerList(message.substring(message.indexOf(":") + 1));
						}else if (message.startsWith("Message")) {
							String content = "对方：" + message.substring(message.indexOf(':') + 1);
							Controller.getInstance().getMessageFromOpponent(content);
						}else if (message.startsWith("HuiQi")) {
							String op = message.substring(message.indexOf(':') + 1);
							if (op.equals("Want")) {
								int aggree = Controller.getInstance().opponentWantToRegretChess();
								if (aggree == 0) {
									sendMessage("HuiQi:Aggree");
								}else {
									sendMessage("HuiQi:Refuse");
								}
							}else if (op.equals("Refuse")) {
								Controller.getInstance().regretChessWasRefused();
							}else if (op.equals("Aggree")) {
								Controller.getInstance().regretChessWasAggreed();
							}
						}else if (message.equals("TimeOver")) {
							Controller.getInstance().opponentTimeOver();
						}else if (message.equals("NameRepeated")) {
							Controller.getInstance().nameRepeat();
						}else if (message.startsWith("WantToPlayWithYou")) {
							String name = message.substring(message.indexOf(':') + 1);
							int aggree = Controller.getInstance().someOneWantToPlayWithYou(name);
							if (aggree == 0) {
								sendMessage("AggreeToPlayWith:" + name);
							}else if (aggree == 1) {
								sendMessage("DisaggreeToPlayWith:" + name);
							}
						}else if (message.startsWith("AggreeToPlayWithYou")) {
							String name = message.substring(message.indexOf(':') + 1);
							Controller.getInstance().someOneAggreeToPlayWithYou(name);
						}else if (message.startsWith("DisaggreeToPlayWithYou")) {
							String name = message.substring(message.indexOf(':') + 1);
							Controller.getInstance().someOneDisaggreeToPlayWithYou(name);
						}else if (message.equals("CanStartConnect")) {
							Controller.getInstance().successfullyConnect(Model.WHIET);
						}else if (message.equals("ThePeopleInviteYouLost")) {
							Controller.getInstance().thePeopleInviteYouLost();
						}else if (message.equals("TryToPlay")) {
							int aggree = Controller.getInstance().opponentWantToStartPlay();
							if (aggree == 0) {
								sendMessage("AggreeToStartPlay");
							}else {
								sendMessage("DisaggreeToStartPlay");
							}
						}else if (message.equals("AggreeToStartPlay")) {
							Controller.getInstance().opponentAggreeStartPlay();
						}else if (message.equals("DisaggreeToStartPlay")) {
							Controller.getInstance().opponentRefuseStartPlay();
						}else if (message.startsWith("Win")) {
							int color = Integer.parseInt(message.substring(message.indexOf(':') + 1));
							Controller.getInstance().gameOver(color);
						}else if (message.equals("QuitGame")) {
							Controller.getInstance().opponentLost();
						}else if (message.startsWith("World")) {
							String text = message.substring(message.indexOf(':') + 1);
							Controller.getInstance().getMEssageFromWorld(text);
						}else if (message.startsWith("In")) {
							String name = message.substring(message.indexOf(':') + 1);
							Controller.getInstance().someOneEnteredHall(name);
						}else if (message.startsWith("Out")) {
							String name = message.substring(message.indexOf(':') + 1);
							Controller.getInstance().someOneGotOutHall(name);
						}else if (message.startsWith("SetBusy")) {
							String name = message.substring(message.indexOf(':') + 1);
							Controller.getInstance().setPeoPleIsBusy(name);
						}else if (message.startsWith("SetFree")) {
							String name = message.substring(message.indexOf(':') + 1);
							Controller.getInstance().setPeoPleIsFree(name);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
}