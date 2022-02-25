package Gobang.Server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import javax.swing.JFrame;

public class Server {
	private ServerSocket serverSocket;
	private int serverPort;
	private HashMap<String, Socket> name2Socket;
	private HashMap<String, String> playerMatch;
	private LinkedList<String> playerList;
	private HashMap<String, String> playerState;
	public Server() {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
		playerState = new HashMap<String, String>();
		playerList = new LinkedList<String>();
		playerMatch = new HashMap<String, String>();
		name2Socket = new HashMap<String, Socket>();
		serverPort = 1200;
		try {
			serverSocket = new ServerSocket(serverPort);
		} catch (IOException e) {
			System.out.println("服务器创建失败！");
			System.exit(0);
		}
		startListenPlayersJoin();
	}
	private void startListenPlayersJoin() {
		//开始监听玩家的加入
		new Thread() {
			@Override
			public void run() {
				while (true) {
					try {
						Socket newSocket = serverSocket.accept();
						BufferedReader in = new BufferedReader(new InputStreamReader(newSocket.getInputStream()));
						BufferedWriter out = new BufferedWriter(new OutputStreamWriter(newSocket.getOutputStream()));
						String playerName = in.readLine();
						while (playerList.indexOf(playerName) != -1) {
							out.write("NameRepeated\n");
							out.flush();
							playerName = in.readLine();
						}
						name2Socket.put(playerName, newSocket); //将新名字加入map中
						playerList.add(playerName);
						playerState.put(playerName, "free");
						someOneComeInToAll(playerName);
						startListenThePlayer(playerName);
					} catch (IOException e) {
						System.out.println("ServerAccept异常。");
					}
				}
			}
		}.start();
	}
	private void someOneComeInToAll(String playerName) {
		sendToPeopleList(playerName);
		sendMessageToAll("In:" + playerName, playerName);
	}
	private void sendToPeopleList(String playerName) {
		String msg = "List:";
		for (int i = 0; i < playerList.size() - 1; i++) {
			msg = msg + playerList.get(i) + "." + playerState.get(playerList.get(i)) + ",";
		}
		if (playerList.size() >= 1) {
			msg = msg + playerList.get(playerList.size() - 1) + "." + playerState.get(playerList.get(playerList.size() - 1));
		}
		try {
			sendMessage(playerName, msg);
		} catch (IOException e) {

		}
	}
	private void someOneComeOutToAll(String playerName) {
		sendMessageToAll("Out:" + playerName, null);
	}
	private void startListenThePlayer(String playerName) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(name2Socket.get(playerName).getInputStream()));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(name2Socket.get(playerName).getOutputStream()));
			new Thread() {
				@Override
				public void run() {
					while (true) {
						try {
							String apply = in.readLine();
							if (apply.startsWith("WantToPlayWith")) {
								String name = apply.substring(apply.indexOf(':') + 1);
								sendMessage(name, "WantToPlayWithYou:" + playerName);
							}else if (apply.startsWith("AggreeToPlayWith")) {
								String name = apply.substring(apply.indexOf(':') + 1);
								if (playerList.indexOf(name) != -1 && playerState.get(name) != "busy") {
									setBusy(name);
									setBusy(playerName);
									sendMessage(playerName, "CanStartConnect");
									sendMessage(name, "AggreeToPlayWithYou:" + playerName);
									playerMatch.put(name, playerName);
									playerMatch.put(playerName, name);
								}else {
									sendMessage(playerName, "ThePeopleInviteYouLost");
								}
							}else if (apply.startsWith("DisaggreeToPlayWith")) {
								String name = apply.substring(apply.indexOf(':') + 1);
								if (playerList.indexOf(name) != -1 && playerState.get(name) != "busy")
									sendMessage(name, "DisaggreeToPlayWithYou:" + playerName);
							}else if (apply.equals("TryToPlay")) {
								sendMessage(playerMatch.get(playerName), "TryToPlay");
							}else if (apply.equals("AggreeToStartPlay")) {
								sendMessage(playerMatch.get(playerName), "AggreeToStartPlay");
							}else if (apply.equals("DisaggreeToStartPlay")) {
								sendMessage(playerMatch.get(playerName), "DisaggreeToStartPlay");
							}else if (apply.startsWith("ToWorld")) {
								String text = apply.substring(apply.indexOf(':') + 1);
								sendMessageToAll("World:" + text, null);
							}else {
								sendMessage(playerMatch.get(playerName), apply);
								if (apply.equals("QuitGame")) {
									setFree(playerMatch.get(playerName));
									setFree(playerName);
									playerMatch.remove(playerMatch.get(playerName));
									playerMatch.remove(playerName);
								}
							}
						} catch (IOException | NullPointerException e) {
							playerState.remove(playerName);
							name2Socket.remove(playerName);
							playerList.remove(playerList.indexOf(playerName));
							someOneComeOutToAll(playerName);
							return;
						}
					}
				}

			}.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	protected void setFree(String name) {
		playerState.replace(name, "free");
		String msg = "SetFree:" + name;
		sendMessageToAll(msg, null);
	}
	private void setBusy(String name) {
		playerState.replace(name, "busy");
		String msg = "SetBusy:" + name;
		sendMessageToAll(msg, null);
	}
	private void sendMessageToAll(String text, String exceptPeople) {
		for (int i = 0; i < playerList.size(); i++) {
			if (playerList.get(i).equals(exceptPeople))
				continue;
			try {
				sendMessage(playerList.get(i), text);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private void sendMessage(String name, String msg) throws IOException {
		Socket s = name2Socket.get(name);
		BufferedWriter tempOut = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
		tempOut.write(msg + "\n");
		tempOut.flush();
	}
	public static void main(String[] args) {
		Server server = new Server();
	}
}
