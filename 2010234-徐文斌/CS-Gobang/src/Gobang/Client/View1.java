package Gobang.Client;

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.border.*;

public class View1 extends JFrame {

	private static final long serialVersionUID = 1L;
	private static View1 instance;
	private static JButton inviteToPlay;
	private static JTextField message;
	private static JButton sendMessage;
	private static JTextArea messageArea;
	private static DefaultListModel<String> players;
	private static JList<String> allPlayers;
	private static HashMap<String, String> playersWheBusy;
	private static Font allFont = new Font("仿宋", Font.PLAIN, 18);

	public static View1 getInstance() {
		if (instance == null)
			instance = new View1();
		return instance;
	}
	private View1() {
		initGraph();
		bindListener();
	}
	private void initGraph() {
		this.setTitle("大厅");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(800, 450);
		this.setLayout(new BorderLayout());
		JPanel gamerPanel = new JPanel();
		gamerPanel.setLayout(new BorderLayout());
		players = new DefaultListModel<>();
		playersWheBusy = new HashMap<String, String>();
		allPlayers = new JList<String>(players);
		allPlayers.setCellRenderer(new NameListRender());
		gamerPanel.add(allPlayers, BorderLayout.CENTER);
		inviteToPlay = new JButton("邀请游戏");
		inviteToPlay.setFont(allFont);
		gamerPanel.add(inviteToPlay, BorderLayout.SOUTH);
		
		JPanel messagePanel = new JPanel();
		messagePanel.setLayout(new BorderLayout());
		messageArea = new JTextArea();
		TitledBorder titleMessageArea = new TitledBorder(new EtchedBorder(), "世界消息");
		titleMessageArea.setTitleFont(allFont);
		messageArea.setBorder(titleMessageArea);
		messageArea.setEditable(false);
		messageArea.setLineWrap(true);
		messageArea.setWrapStyleWord(true);
		messageArea.setFont(allFont);
		JScrollPane scrollPane = new JScrollPane(
                messageArea,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
		messagePanel.add(scrollPane, BorderLayout.CENTER);
		JPanel messageEditPanel = new JPanel();
		FlowLayout f = new FlowLayout();
		f.setHgap(0);
		messageEditPanel.setLayout(f);
		message = new JTextField();
		message.setPreferredSize(new Dimension(300, 30));
		message.setFont(allFont);
		sendMessage = new JButton("发送");
		sendMessage.setFont(allFont);
		messageEditPanel.add(message);
		messageEditPanel.add(sendMessage);
		messagePanel.add(messageEditPanel, BorderLayout.SOUTH);
		messagePanel.setPreferredSize(new Dimension(400, 500));
		
		this.add(gamerPanel, BorderLayout.CENTER);
		this.add(messagePanel, BorderLayout.EAST);
		this.setVisible(true);
	}
	private void bindListener() {
		inviteToPlay.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				String opponentName = allPlayers.getSelectedValue();
				if (opponentName == null || opponentName.equals(Controller.getInstance().getYourName()))
					return;
				Controller.getInstance().tryToPlayWithOthers(opponentName);
			}
		});
		sendMessage.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				String text = message.getText();
				boolean success = Controller.getInstance().sendMessageToWorld(text);
				if (success)
					message.setText("");
			}
		});
		message.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();
				if (c != KeyEvent.VK_ENTER)
					return;
				String text = message.getText();
				boolean success = Controller.getInstance().sendMessageToWorld(text);
				if (success)
					message.setText("");
			}
		});
	}
	public void exec() {
		if (instance == null)
			instance = new View1();
		instance.setVisible(true);
	}
	public void exit() {
		if (instance != null)
			instance.dispose();
	}
	public void refreshPlayerList(String names) {
		players.clear();
		playersWheBusy.clear();
		String nowName = "";
		for (int i = 0; i < names.length(); i++) {
			if (names.charAt(i) == ',') {
				players.addElement(nowName.substring(0, nowName.indexOf('.')));
				playersWheBusy.put(nowName.substring(0, nowName.indexOf('.')), nowName.substring(nowName.indexOf('.') + 1));
				nowName = "";
			}else {
				nowName += names.charAt(i);
			}
		}
		players.addElement(nowName.substring(0, nowName.indexOf('.')));
		playersWheBusy.put(nowName.substring(0, nowName.indexOf('.')), nowName.substring(nowName.indexOf('.') + 1));
	}
	public boolean isWork() {
		if (instance == null)
			instance = new View1();
		return instance.isVisible();
	}
	public void showWorldMessage(String text) {
		messageArea.append(text + "\n");
		messageArea.setCaretPosition(messageArea.getText().length());
	}
	public void peopleComeIn(String name) {
		players.addElement(name);
		playersWheBusy.put(name, "free");
	}
	public void peopleComeOut(String name) {
		players.removeElement(name);
		playersWheBusy.remove(name);
	}
	public HashMap<String, String> getPlayerStatue(){
		return playersWheBusy;
	}
	public void setPeopleBusy(String name) {
		playersWheBusy.replace(name, "busy");
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					allPlayers.updateUI();
				}
			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public void setPeopleFree(String name) {
		playersWheBusy.replace(name, "free");
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					allPlayers.updateUI();
				}
			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
