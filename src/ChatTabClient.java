import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.ImageIcon;

public class ChatTabClient extends JPanel {

	private static final long serialVersionUID = 1L;
	public ChatWindowClient cwc; 
	public int room_id;
	
	public JPanel tabPanel;
	public JTextField textUsername = new JTextField();
	public JTextField textChat = new JTextField();
	public DefaultListModel<String> userList = new DefaultListModel<String>();
	public JList<String> userListUI = new JList<String>(userList);
    public JScrollPane scrollPane = new JScrollPane();
	
	public JLabel label = new JLabel("\u4F7F\u7528\u8005\u5217\u8868");
    public JLabel selfNameLabel = new JLabel("");
	public JLabel selfProfilePic = new JLabel("");
	public JLabel friendNameLabel = new JLabel("");
    public JLabel friendProfilePic = new JLabel("");

	public ChatTabClient myself;
	public StyleContext sc = new StyleContext();
	public JTextPane textPane = new JTextPane(new DefaultStyledDocument(sc));
	public JScrollPane textScroll = new JScrollPane();
	
	public JButton btnConnect = new JButton("\u9023\u7DDA");
	public JButton btnDisconnect = new JButton("\u96E2\u7DDA");
	public JButton btnWhisper = new JButton("\u6084\u6084\u8A71");
	public JButton btnChatroom = new JButton("\u6703\u5BA2\u5BA4");
	public JButton btnEmoticon = new JButton("\u8868\u60C5\u7B26\u865F");
	public JButton btnRobot = new JButton("\u6A5F\u5668\u4EBA");
	public JButton btnTransfer = new JButton("\u50B3\u9001\u6A94\u6848");
	public JButton btnVoice = new JButton("\u8996\u8A0A\u901A\u8A71");
	public JButton btnLeaveRoom = new JButton("\u96E2\u958B\u623F\u9593");
	public JButton btnLeaveWhisper = new JButton("\u96E2\u958B\u5bc6\u8ac7");
	public JButton btnInvitation = new JButton("\u9080\u8ACB\u4F7F\u7528\u8005");
	public JButton btnFB = new JButton("Facebook");
	public JButton btnBonus =  new JButton("HTML"); 
	private final JPanel emoticonPane = new JPanel();
	private final JPanel emoticonTable = new JPanel();
	private final JButton emo1 = new JButton("");
	private final JButton emo2 = new JButton("");
	private final JButton emo3 = new JButton("");
	private final JButton emo4 = new JButton("");
	private final JButton emo5 = new JButton("");
	private final JButton emo6 = new JButton("");
	private final JButton emo7 = new JButton("");
	private final JButton emo8 = new JButton("");
	private final JButton emo9 = new JButton("");
	private final JButton emo10 = new JButton("");
	private final JButton emo11 = new JButton("");
	private final JButton emo12 = new JButton("");
	private final JButton emo13 = new JButton("");
	private final JButton emo14 = new JButton("");
	private final JButton emo15 = new JButton("");
	private final JButton emo16 = new JButton("");
	
	Style defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
	final Style mainStyle = sc.addStyle(null, defaultStyle);
	final Style boldStyle = sc.addStyle(null, defaultStyle);
	final Style grayStyle = sc.addStyle(null, defaultStyle);
	final Style boldGrayStyle = sc.addStyle(null, defaultStyle);
	
	private final JScrollPane emoticonScroll = new JScrollPane();

	/**
	 * Create the application.
	 */	
	public ChatTabClient(ChatWindowClient _cwc, int rmid) {
		cwc = _cwc;
		room_id = rmid;
		myself = this;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		tabPanel = new JPanel();		
		tabPanel.setBounds(100, 100, 1004, 606);
		tabPanel.setLayout(null);
		emoticonPane.setBorder(new LineBorder(new Color(0, 0, 0)));
		emoticonPane.setBackground(Color.WHITE);
		
		emoticonPane.setBounds(449, 160, 360, 360);
		emoticonTable.setBackground(Color.WHITE);
		emoticonTable.setBounds(0, 0, 450, 420);
		tabPanel.add(emoticonPane);
		emoticonPane.setLayout(null);
		emoticonScroll.setBounds(0, 0, 360, 360);
		emoticonPane.add(emoticonScroll);
		emoticonScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		emoticonScroll.setViewportView(emoticonTable);
		emoticonTable.setLayout(null);
		emoticonPane.setVisible(false);
		emo1.setBackground(Color.WHITE);
		emo1.setForeground(Color.WHITE);
		emo1.setIcon(new ImageIcon("images/1.png"));
		emo2.setForeground(Color.WHITE);
		emo2.setBackground(Color.WHITE);
		emo2.setIcon(new ImageIcon("images/2.png"));
		emo3.setForeground(Color.WHITE);
		emo3.setBackground(Color.WHITE);
		emo3.setIcon(new ImageIcon("images/3.png"));
		emo4.setForeground(Color.WHITE);
		emo4.setBackground(Color.WHITE);
		emo4.setIcon(new ImageIcon("images/4.png"));
		emo5.setForeground(Color.WHITE);
		emo5.setBackground(Color.WHITE);
		emo5.setIcon(new ImageIcon("images/5.png"));
		emo6.setForeground(Color.WHITE);
		emo6.setBackground(Color.WHITE);
		emo6.setIcon(new ImageIcon("images/6.png"));
		emo7.setForeground(Color.WHITE);
		emo7.setBackground(Color.WHITE);
		emo7.setIcon(new ImageIcon("images/7.png"));
		emo8.setForeground(Color.WHITE);
		emo8.setBackground(Color.WHITE);
		emo8.setIcon(new ImageIcon("images/8.png"));
		emo9.setForeground(Color.WHITE);
		emo9.setBackground(Color.WHITE);
		emo9.setIcon(new ImageIcon("images/9.png"));
		emo10.setForeground(Color.WHITE);
		emo10.setBackground(Color.WHITE);
		emo10.setIcon(new ImageIcon("images/10.png"));
		emo11.setForeground(Color.WHITE);
		emo11.setBackground(Color.WHITE);
		emo11.setIcon(new ImageIcon("images/11.png"));
		emo12.setForeground(Color.WHITE);
		emo12.setBackground(Color.WHITE);
		emo12.setIcon(new ImageIcon("images/12.png"));
		emo13.setForeground(Color.WHITE);
		emo13.setBackground(Color.WHITE);
		emo13.setIcon(new ImageIcon("images/13.png"));
		emo14.setForeground(Color.WHITE);
		emo14.setBackground(Color.WHITE);
		emo14.setIcon(new ImageIcon("images/14.png"));
		emo15.setForeground(Color.WHITE);
		emo15.setBackground(Color.WHITE);
		emo15.setIcon(new ImageIcon("images/15.png"));
		emo16.setForeground(Color.WHITE);
		emo16.setBackground(Color.WHITE);
		emo16.setIcon(new ImageIcon("images/16.png"));
		emo1.setBorder(null);
		emo2.setBorder(null);
		emo3.setBorder(null);
		emo4.setBorder(null);
		emo5.setBorder(null);
		emo6.setBorder(null);
		emo7.setBorder(null);
		emo8.setBorder(null);
		emo9.setBorder(null);
		emo10.setBorder(null);
		emo11.setBorder(null);
		emo12.setBorder(null);
		emo13.setBorder(null);
		emo14.setBorder(null);
		emo15.setBorder(null);
		emo16.setBorder(null);
		
		emo1.addMouseListener(emoMouseListener("{emo01}"));
		emo2.addMouseListener(emoMouseListener("{emo02}"));
		emo3.addMouseListener(emoMouseListener("{emo03}"));
		emo4.addMouseListener(emoMouseListener("{emo04}"));
		emo5.addMouseListener(emoMouseListener("{emo05}"));
		emo6.addMouseListener(emoMouseListener("{emo06}"));
		emo7.addMouseListener(emoMouseListener("{emo07}"));
		emo8.addMouseListener(emoMouseListener("{emo08}"));
		emo9.addMouseListener(emoMouseListener("{emo09}"));
		emo10.addMouseListener(emoMouseListener("{emo10}"));
		emo11.addMouseListener(emoMouseListener("{emo11}"));
		emo12.addMouseListener(emoMouseListener("{emo12}"));
		emo13.addMouseListener(emoMouseListener("{emo13}"));
		emo14.addMouseListener(emoMouseListener("{emo14}"));
		emo15.addMouseListener(emoMouseListener("{emo15}"));
		emo16.addMouseListener(emoMouseListener("{emo16}"));
				
		emo1.setBounds(0, 0, 90, 90);
		emo2.setBounds(90, 0, 90, 90);
		emo3.setBounds(180, 0, 90, 90);
		emo4.setBounds(270, 0, 90, 90);
		emo5.setBounds(0, 90, 90, 90);
		emo6.setBounds(90, 90, 90, 90);
		emo7.setBounds(180, 90, 90, 90);
		emo8.setBounds(270, 90, 90, 90);
		emo9.setBounds(0, 180, 90, 90);		
		emo10.setBounds(90, 180, 90, 90);		
		emo11.setBounds(180, 180, 90, 90);		
		emo12.setBounds(270, 180, 90, 90);		
		emo13.setBounds(0, 270, 90, 90);	
		emo14.setBounds(90, 270, 90, 90);		
		emo15.setBounds(180, 270, 90, 90);		
		emo16.setBounds(270, 270, 90, 90);
		
		emoticonTable.add(emo1);
		emoticonTable.add(emo2);				
		emoticonTable.add(emo3);				
		emoticonTable.add(emo4);						
		emoticonTable.add(emo5);					
		emoticonTable.add(emo6);
		emoticonTable.add(emo7);						
		emoticonTable.add(emo8);		
		emoticonTable.add(emo9);	
		emoticonTable.add(emo10);		
		emoticonTable.add(emo11);		
		emoticonTable.add(emo12);		
		emoticonTable.add(emo13);		
		emoticonTable.add(emo14);		
		emoticonTable.add(emo15);		
		emoticonTable.add(emo16);
		
		StyleConstants.setBold(boldStyle, true);
		StyleConstants.setBold(boldGrayStyle, true);
		StyleConstants.setForeground(mainStyle, Color.BLACK);
		StyleConstants.setForeground(boldStyle, Color.BLACK);
		StyleConstants.setForeground(grayStyle, Color.GRAY);
		StyleConstants.setForeground(boldGrayStyle, Color.DARK_GRAY);
		textPane.addStyle("NormalMessage", mainStyle);
		textPane.addStyle("UserName", boldStyle);
		textPane.addStyle("SystemMessage", grayStyle);
		textPane.addStyle("FriendName", boldGrayStyle);
		textPane.setBorder(null);
		
		label.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		label.setBounds(11, 10, 150, 25);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		tabPanel.add(label);
		
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cwc.username = textUsername.getText();
				textUsername.setEditable(false);
			    textChat.requestFocus();
			    btnConnect.setEnabled(false);
			    btnDisconnect.setEnabled(true);
			    btnConnect.setVisible(false);
			    btnDisconnect.setVisible(true);
			    textChat.setEditable(true);
				new Thread(cwc.listener = new Listener(cwc)).start();
			}
		});
		btnConnect.setBounds(10, 525, 87, 23);
		tabPanel.add(btnConnect);
		
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cwc.listener.disconnect();
				cwc.listener = null;
			}
		});
		btnDisconnect.setBounds(10, 525, 87, 23);
		tabPanel.add(btnDisconnect);
		
		btnWhisper.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (cwc.listener == null || !cwc.listener.isConnected() || userListUI.isSelectionEmpty()) return;
				String receiver = userListUI.getSelectedValue();
				if (cwc.username.equals(receiver)) return;
				
				try {
					cwc.listener.out.writeUTF("(WhisperRequest)" + receiver);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}			
		});
		btnWhisper.setBounds(221, 525, 104, 23);
		tabPanel.add(btnWhisper);
		
		btnChatroom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (cwc.listener == null || !cwc.listener.isConnected()) return;
				cwc.sentNewRoomReq();				
			}
		});
		btnChatroom.setBounds(335, 525, 104, 23);
		tabPanel.add(btnChatroom);
		
		btnEmoticon.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (cwc.listener == null || !cwc.listener.isConnected()) return;
				emoticonPane.setVisible(!emoticonPane.isVisible());
			}
		});
		btnEmoticon.setBounds(449, 525, 104, 23);
		tabPanel.add(btnEmoticon);						
		
		btnRobot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (cwc.listener == null || !cwc.listener.isConnected()) return;
				textChat.setEditable(cwc.listener.robotMode);
				cwc.listener.printText(room_id, "<\u7cfb\u7d71\u8a0a\u606f> \u81ea\u52d5\u56de\u8a71\u6a21\u5f0f" + (cwc.listener.robotMode? "\u95dc\u9589": "\u958b\u555f") + "\u3002\n", "SystemMessage");
				cwc.listener.robotMode = !cwc.listener.robotMode;
			}
		});
		btnRobot.setBounds(563, 525, 104, 23);
		btnRobot.setVisible(false);
		tabPanel.add(btnRobot);			
		
		btnFB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cwc.createFBChat();
			}
		});
		btnFB.setBounds(563, 525, 104, 23);
		tabPanel.add(btnFB);
		
		btnTransfer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String receiver = null;
				if (btnLeaveWhisper.isVisible()) {
					receiver = cwc.tabbedPane.getTitleAt(cwc.tabbedPane.getSelectedIndex()).substring(5);
				}
				else {
					if (cwc.listener == null || !cwc.listener.isConnected() || userListUI.isSelectionEmpty()) return;
					receiver = userListUI.getSelectedValue();
					if (cwc.username.equals(receiver)) return;
				}
				
				try {
					cwc.listener.out.writeUTF("(IPRequest)" + receiver);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}			
		});
		btnTransfer.setBounds(677, 525, 104, 23);
		tabPanel.add(btnTransfer);
		
		btnVoice.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (cwc.listener == null || !cwc.listener.isConnected() || userListUI.isSelectionEmpty()) return;
				String receiver = userListUI.getSelectedValue();
				if (cwc.username.equals(receiver)) return;
				askForVideoChat(receiver, cwc.username);
			}			
		});
		btnVoice.setBounds(791, 525, 104, 23);
		tabPanel.add(btnVoice);		
		
		btnBonus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new EditHTML(); 
			}			
		});
		btnBonus.setBounds(905, 525, 90, 23);
		tabPanel.add(btnBonus);		
		
		btnLeaveRoom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {		
				cwc.removeTab(room_id);
				try	{
					cwc.listener.out.writeUTF("(LeaveRoomRequest)" + room_id);
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		btnLeaveRoom.setBounds(10, 525, 87, 23);
		btnLeaveRoom.setVisible(false);
		tabPanel.add(btnLeaveRoom);
		
		btnLeaveWhisper.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {		
				try	{
					cwc.listener.out.writeUTF("(LeaveWhisperRequest)" + room_id);
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		btnLeaveWhisper.setBounds(10, 525, 87, 23);
		btnLeaveWhisper.setVisible(false);
		tabPanel.add(btnLeaveWhisper);
		
		btnInvitation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (cwc.listener == null || !cwc.listener.isConnected()) return;
				ChatTabClient lobby = cwc.tabs.get(0);
				String receiver = (String)JOptionPane.showInputDialog(cwc.frmLabChatroom,
						                                              "\u8acb\u9078\u64c7\u9080\u8acb\u5c0d\u8c61\uff1a",
						                                              "Invitation",
						                                              JOptionPane.QUESTION_MESSAGE,
						                                              null,
						                                              lobby.userList.toArray(),
						                                              lobby.userList.firstElement());
				if (receiver == null) return;
				if (cwc.username.equals(receiver) || userList.contains(receiver)) {
					JOptionPane.showMessageDialog(cwc.frmLabChatroom, "\u8acb\u9078\u64c7\u5176\u4ed6\u4f7f\u7528\u8005\uff01", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				cwc.listener.sendInvitation(room_id, receiver);
			}			
		});
		btnInvitation.setBounds(107, 525, 104, 23);
		btnInvitation.setVisible(false);
		tabPanel.add(btnInvitation);
		
		JButton btnVibrate = new JButton("");
		btnVibrate.setToolTipText("Vibrate!");
		btnVibrate.setBackground(Color.WHITE);
		btnVibrate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cwc.listener.sendVibration(room_id);
			}
		});
		btnVibrate.setBounds(969, 563, 23, 22);
		btnVibrate.setIcon(new ImageIcon("images/vibration.png"));
		tabPanel.add(btnVibrate);
		
		textUsername = new JTextField();
		textUsername.setToolTipText("Please enter username");
		textUsername.setBounds(10, 562, 150, 25);
		textUsername.setColumns(10);
		tabPanel.add(textUsername);
		
		textChat.setBounds(172, 562, 794, 25);				
		textChat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {			
				try	{
					cwc.listener.out.writeUTF("(text%"+cwc.username+"%"+room_id+")" + textChat.getText());
				} catch (IOException ex) {
					cwc.listener.disconnect();
					JOptionPane.showMessageDialog(cwc.frmLabChatroom, "\u5931\u53bb\u8207\u4f3a\u670d\u5668\u7684\u9023\u7dda\u3002", "Error", JOptionPane.ERROR_MESSAGE);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				textChat.setText(null);				
			}
		});		
		tabPanel.add(textChat);
		
		if (room_id == 0) {
			textChat.setEditable(false);
			textUsername.requestFocus();
		}

		textPane.setEditable(false);
	    textScroll.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
	    textScroll.setBounds(172, 10, 822, 501);
	    tabPanel.add(textScroll);
	    textScroll.setViewportView(textPane);	    	
	    
	    selfProfilePic.setBackground(Color.WHITE);
	    selfProfilePic.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
	    selfProfilePic.setBounds(11, 361, 150, 150);
	    tabPanel.add(selfProfilePic);

	    friendProfilePic.setVisible(false);
	    friendProfilePic.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
	    friendProfilePic.setBackground(Color.WHITE);
	    friendProfilePic.setBounds(11, 10, 150, 150);
	    tabPanel.add(friendProfilePic);
	    
	    selfNameLabel.setVisible(false);
	    selfNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    selfNameLabel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
	    selfNameLabel.setBounds(11, 326, 150, 25);
	    tabPanel.add(selfNameLabel);
	    friendNameLabel.setVisible(false);
	    friendNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    friendNameLabel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
	    friendNameLabel.setBounds(11, 170, 150, 25);
	    
	    tabPanel.add(friendNameLabel);
	    
	    scrollPane.setBounds(11, 44, 150, 300);
	    tabPanel.add(scrollPane);
	    scrollPane.setViewportView(userListUI);
	}
	
	public void addYouTube(final String s){		
		JScrollPane scrollPane = new JScrollPane();
        JPanel p = new JPanel();
        scrollPane.setViewportView(p);
        textPane.setSelectionStart(textPane.getText().length());
        textPane.setSelectionEnd(textPane.getText().length());
        textPane.replaceSelection("\n");
        textPane.insertComponent(scrollPane);
        textPane.setSelectionStart(textPane.getText().length());
        textPane.setSelectionEnd(textPane.getText().length());
        textPane.replaceSelection("\n");	            
        final JFXPanel fxPanel = new JFXPanel();
        p.add(fxPanel);
        Platform.setImplicitExit(false);
		Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	initFX(fxPanel, s);
            }
        });     
		scrollPane.setSize(200, 200);
	}
	
	public void vibrate() {
		try {
			final JFrame frame = cwc.frmLabChatroom;
			final int originalX = frame.getLocationOnScreen().x;
			final int originalY = frame.getLocationOnScreen().y;
			for (int i = 0; i < 10; i++) {
				Thread.sleep(10);
				frame.setLocation(originalX, originalY + 10);
				Thread.sleep(10);
				frame.setLocation(originalX, originalY - 10);
				Thread.sleep(10);
				frame.setLocation(originalX + 10, originalY);
				Thread.sleep(10);
				frame.setLocation(originalX, originalY);
			}
		} catch (Exception err) {
			err.printStackTrace();
		}
	}
	
	public MouseAdapter emoMouseListener(final String s) {
		MouseAdapter ma = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				textChat.setText(textChat.getText()+s);
				emoticonPane.setVisible(false);
				textChat.requestFocus();
			};
		}; 
		return ma;
	}
	
	public void autoConnect(int r_id) {
		room_id = r_id;
		selfProfilePic.setIcon(cwc.userIcon);
		textUsername.setText(cwc.username);
		textUsername.setEditable(false);		
	    textChat.requestFocus();
	    btnConnect.setEnabled(false);
	    btnDisconnect.setEnabled(false);
	    btnConnect.setVisible(false);
	    btnDisconnect.setVisible(false);
	    btnRobot.setVisible(false);
		btnInvitation.setVisible(true);
		btnLeaveRoom.setVisible(true);
	    textChat.setEnabled(true);		
	}
    private static void initFX(JFXPanel fxPanel, String s) {
        // This method is invoked on the JavaFX thread
        Scene scene = createScene(s);
        fxPanel.setScene(scene);
    }

    private static Scene createScene(String s) {
    	String DEFAULT_URL = s;
    	Group root = new Group();
		WebView webView = new WebView();
		final WebEngine webEngine = webView.getEngine();
		webEngine.load(DEFAULT_URL);
		final TextField locationField = new TextField(DEFAULT_URL);
		webEngine.locationProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				locationField.setText(newValue);
			}
		});

		EventHandler<javafx.event.ActionEvent> goAction = new EventHandler<javafx.event.ActionEvent>() {
			@Override
			public void handle(javafx.event.ActionEvent event) {
				webEngine.load(locationField.getText().startsWith("http://")
						? locationField.getText()
						: "http://" + locationField.getText());
				
			}
		};

		locationField.setOnAction(goAction);
		Button goButton = new Button("Go");
		goButton.setDefaultButton(true);
		goButton.setOnAction(goAction);

		// Layout logic

		HBox hBox = new HBox(5);
		hBox.getChildren().setAll(locationField, goButton);
		HBox.setHgrow(locationField, Priority.ALWAYS);
		VBox vBox = new VBox(5);
		vBox.getChildren().setAll(hBox, webView);
		VBox.setVgrow(webView, Priority.ALWAYS);
		root.getChildren().add(vBox);		
        return new Scene(root);
    }
    
    public void askForVideoChat(String receiverName, String transmitterName)
	{
		try {		
			cwc.listener.out.writeUTF("(VideoChatRequest)" + receiverName + "_" + transmitterName);
			JOptionPane.showMessageDialog(cwc.dialogFrame, "等待 "+receiverName+" 接受邀約...", "SkypeLog",JOptionPane.PLAIN_MESSAGE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
