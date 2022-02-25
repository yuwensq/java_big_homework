package Gobang.Client;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.LinkedList;

public class View2 extends JFrame{

	private static final long serialVersionUID = 1L;
	private static ChessBoard chessBoard;
	private static JPanel functionalArea;
	private static JButton music;	//控制音乐播放
	private static JSlider volumnControll;
	private static boolean playingMusic = true;
	private static boolean overPlaying = false;
	private static JTextArea messageArea; //显示聊天记录
	private static JTextField message; //输入文本消息
	private static JButton sendMessage; //发送信息
	private static JLabel remainSeconds; //记录还剩多少秒
	private static JButton startGame; //开局
	private static JButton regretChess; //悔棋
	private static JButton replay;	//复盘
	private static View2 instance = null;
	private static Font allFont = new Font("仿宋", Font.PLAIN, 18);
	public static View2 getInstance() {
		if (instance == null)
			instance = new View2();
		return instance;
	}
	private View2() {
		inintGraph();
		inintMusic();
		bindListener();		
	}
	private void inintMusic() {
		new Thread() {
			@Override
			public void run() {
				while (!overPlaying) {
					playMusic("music/backGroundMusic.wav", 0);					
				}
			}
		}.start();
	}
	public void playMusic(String path, int op) {
		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(new File(path));
			AudioFormat aif = ais.getFormat();
			final SourceDataLine sdl;
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, aif);
			sdl = (SourceDataLine) AudioSystem.getLine(info);
			sdl.open(aif);
			sdl.start();
			FloatControl fc = (FloatControl) sdl.getControl(FloatControl.Type.MASTER_GAIN);
			int baseValue = 1;
			if (op == 1)
				baseValue = 2;
			float volumnValue = 1;
			float dB = (float) (Math.log(volumnValue == 0.0 ? 0.0001 : volumnValue) / Math.log(10.0) * 20.0);
			fc.setValue(dB);
			int nByte = 1;
			final int SIZE = 1024 * 64;
			byte[] buffer = new byte[SIZE];
			while (nByte != -1) {
				int nowVolumnValue = volumnControll.getValue();
				volumnValue = baseValue * (nowVolumnValue / 50.0f);
				dB = (float) (Math.log(volumnValue == 0.0 ? 0.0001 : volumnValue) / Math.log(10.0) * 20.0);
				fc.setValue(dB);
				if (op == 0)
				{
					if (playingMusic) {
						nByte = ais.read(buffer, 0, SIZE);
						sdl.write(buffer, 0, nByte);
					}else
						nByte = ais.read(buffer, 0, 0);
				}else {
					nByte = ais.read(buffer, 0, SIZE);
					sdl.write(buffer, 0, nByte);
				}
			}
			sdl.stop();
		} catch (Exception e) {

		}
	}
	private void bindListener() {
		addWindowListener(new WindowAdapter() {
			@Override
            public void windowClosing(WindowEvent e) {
				playingMusic = false;
				exit();
				Controller.getInstance().quitGame();
			}
            @Override
            public void windowClosed(WindowEvent e) {
				
            }
		});
		chessBoard.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				int col = chessBoard.getCol(x, y);
				int row = chessBoard.getRow(x, y);
				if (col == -1 || row == -1)
					return;					
				Controller.getInstance().putChess(row, col);
			}
		});
		startGame.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (!startGame.isEnabled())
					return;
				Controller.getInstance().tryToStartPlay();
			}
		});
		sendMessage.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				String text = message.getText();
				boolean success = Controller.getInstance().sendMessageToOpponent(text);
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
				boolean success = Controller.getInstance().sendMessageToOpponent(text);
				if (success)
					message.setText("");
			}
		});
		regretChess.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				Controller.getInstance().wantToRegretChess();
			}
		});
		music.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				playingMusic = !playingMusic;
			}
		});
		replay.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				LinkedList<Dot> rec = Controller.getInstance().getChessRecord();
				if (rec == null || rec.isEmpty()) {
					JOptionPane.showMessageDialog(null, "没有啥记录，先下棋吧", "Tip", JOptionPane.DEFAULT_OPTION);
					return;
				}
				JTextArea textArea = new JTextArea();
				textArea.setFont(allFont);
				TitledBorder titleTextArea = new TitledBorder(new EtchedBorder(), "复盘");
				titleTextArea.setTitleFont(allFont);
				textArea.setBorder(titleTextArea);
				for (int i = 0; i < rec.size(); i++) {
					Dot nowDot = rec.get(i);
					String color = "白";
					if (nowDot.getColor() == Model.BLACK)
						color = "黑";
					textArea.append("第" + (i + 1) + "步：" + color + " " + (nowDot.getRow() + 1) + "行, " + (nowDot.getCol() + 1) + "列" + "\n");
				}
				textArea.setLineWrap(true);
				textArea.setWrapStyleWord(true);
				JScrollPane scrollPanel = new JScrollPane(
						textArea,
		                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
		                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
		        );
				scrollPanel.setPreferredSize(new Dimension(500, 400)); 
				JOptionPane.showMessageDialog(null, scrollPanel, new String("Record"), JOptionPane.DEFAULT_OPTION);
			}
		});
	}
	private void inintGraph() {
		this.setTitle("五子棋");
		
		this.setSize(936, 664);
		this.setResizable(false);
		this.setLayout(new BorderLayout());
		
		chessBoard = new ChessBoard(); 
		this.getContentPane().add(chessBoard, BorderLayout.CENTER);

		functionalArea = new JPanel();
		functionalArea.setLayout(new BorderLayout());
		JPanel musicPanel = new JPanel();
		volumnControll = new JSlider();
		volumnControll.setPaintTicks(true);
		volumnControll.setPaintLabels(true);
		volumnControll.setMajorTickSpacing(20);
		volumnControll.setMinorTickSpacing(5);
		musicPanel.add(volumnControll);
		music = new JButton("🔊");
		musicPanel.add(music);
		functionalArea.add(musicPanel, BorderLayout.NORTH);

		JPanel messagePanel = new JPanel();
		messagePanel.setLayout(new BorderLayout());
		messageArea = new JTextArea("");
		messageArea.setEditable(false);
		TitledBorder titleMessageArea = new TitledBorder(new EtchedBorder(), "消息");
		titleMessageArea.setTitleFont(allFont);
		messageArea.setBorder(titleMessageArea);
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
		messageEditPanel.setLayout(new FlowLayout());
		message = new JTextField();
		message.setPreferredSize(new Dimension(200, 20));
		message.setFont(allFont);
		sendMessage = new JButton("⬅");
		messageEditPanel.add(message);
		messageEditPanel.add(sendMessage);
		messagePanel.add(messageEditPanel, BorderLayout.SOUTH);
		
		functionalArea.add(messagePanel, BorderLayout.CENTER);
		
		JPanel controllArea = new JPanel();
		controllArea.setLayout(new GridLayout(2, 1));
		remainSeconds = new JLabel("Your turn:∞s");
		remainSeconds.setHorizontalAlignment(SwingConstants.CENTER);
		Font f = new Font("仿宋", Font.PLAIN, 30);
		remainSeconds.setFont(f);
		remainSeconds.setForeground(new Color(205, 92, 92));
		controllArea.add(remainSeconds);
		
		JPanel keyArea = new JPanel();
		keyArea.setLayout(new FlowLayout());
		startGame = new JButton("开局");
		startGame.setFont(allFont);
		regretChess = new JButton("悔棋");
		regretChess.setFont(allFont);
		replay = new JButton("复盘");
		replay.setFont(allFont);
		keyArea.add(startGame);
		keyArea.add(regretChess);
		keyArea.add(replay);
		controllArea.add(keyArea);
		
		functionalArea.add(controllArea, BorderLayout.SOUTH);
		
		this.getContentPane().add(functionalArea, BorderLayout.EAST);
		this.setVisible(true);
	}
	public void exec() {
		overPlaying = false;
		playingMusic = true;
		messageArea.setText("");
		if (instance == null)
			instance = getInstance();
		instance.setVisible(true);
		System.out.println("View2执行一次");
	}
	public void exit() {
		if (instance != null)
			instance.dispose();
		playingMusic = false;
		overPlaying = true;
	}
	public void upDate() {
		chessBoard.upDate();		
	}
	public void showMessage(String text) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				messageArea.append(text + "\n");
				messageArea.setCaretPosition(messageArea.getText().length());
			}
		});
	}
	public void successfullyConnectInit() {	
		remainSeconds.setText("Your turn:∞S");
		volumnControll.setValue(50);
		startGame.setEnabled(true);
		upDate();
		showMessage("成功连接！");
	}
	public void startPlayInit() {
		startGame.setEnabled(false);
		showMessage("开始游戏！");
		upDate();
	}
	public void gameOverInit(boolean youWin) {
		startGame.setEnabled(true);
		if (youWin)
			showMessage("你赢了");
		else
			showMessage("你输了");
	}
	public void opponentLostInit() {
		startGame.setEnabled(false);
		upDate();
	}
	public void setRemainTime(int remainTime, boolean yourTurn) {
		if (yourTurn == true)
			remainSeconds.setText("Your turn:" + remainTime + "S");
		else 
			remainSeconds.setText("His turn:" + remainTime + "S");
	}
	public void playPutChessMusic(String path) {
		new Thread() {
			@Override
			public void run() {
				playMusic(path, 1);
			}
		}.start();
	}
}