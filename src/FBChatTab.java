import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import org.jivesoftware.smack.XMPPException;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;

public class FBChatTab extends JPanel {

	private static final long serialVersionUID = 1L;
	private int lastEnd = 0;
	
	public final ChatWindowClient cwc;
	public final FBChatTab myself = this;
	public boolean logged_in = false;
	
	public JPanel tabPanel;
	public JTextField textChat = new JTextField();
	public JList<Object> onlineList = new JList<Object>(new DefaultListModel<Object>());
	public JList<Object> offlineList = new JList<Object>(new DefaultListModel<Object>());
	public JLabel onlineLabel = new JLabel("\u5728\u7DDA\u5217\u8868");

	public StyleContext sc = new StyleContext();
	Style defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
	final Style mainStyle = sc.addStyle(null, defaultStyle);
	final Style boldStyle = sc.addStyle(null, defaultStyle);
	final Style grayStyle = sc.addStyle(null, defaultStyle);
	final Style boldGrayStyle = sc.addStyle(null, defaultStyle);
	
	public JTextPane textPane = new JTextPane(new DefaultStyledDocument(sc));
	public JScrollPane textScroll = new JScrollPane();
	public JButton btnLeavePage = new JButton("\u96E2\u958B");
	
	public JLabel lblfacebook = new JLabel("\u2190 \u767B\u5165Facebook");
	public JLabel fb_label = new JLabel("");
	public JLabel usernameLabel = new JLabel("");
	public JLabel profilePicLabel = new JLabel("");
	
	public FBChatTab(ChatWindowClient c) {
		this.cwc = c;
		
		tabPanel = new JPanel();		
		tabPanel.setBounds(100, 100, 903, 604);
		tabPanel.setLayout(null);
		
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
		onlineLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				cwc.fbClient.updateUserList();
			}
		});
		onlineLabel.setToolTipText("\u9EDE\u64CA\u5373\u53EF\u5237\u65B0\u5217\u8868");
		
		onlineLabel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		onlineLabel.setBounds(11, 10, 150, 25);
		onlineLabel.setHorizontalAlignment(SwingConstants.CENTER);
		tabPanel.add(onlineLabel);
		
		btnLeavePage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {		
				cwc.webBrowser.navigate("about:blank");
				cwc.fbClient.clear();
				cwc.initFbBrowser();
				cwc.tabbedPane.remove(tabPanel);
				tabPanel.setName(null);
				clearText();
				textChat.setEditable(false);
				lblfacebook.setVisible(true);
				fb_label.setVisible(true);
				profilePicLabel.setVisible(false);
				usernameLabel.setVisible(false);
			}
		});
		btnLeavePage.setBounds(74, 546, 87, 25);
		tabPanel.add(btnLeavePage);
		
		textChat.setBounds(172, 546, 822, 25);				
		textChat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {	
				String message = textChat.getText();
                printText(message, cwc.fbClient.getUserName());		
        		try {
        		    cwc.fbClient.getConversation().sendMessage(message);
        		}
        		catch (XMPPException e) {
        		    System.out.println("Message sending error!");
        		}
				textChat.setText(null);
			}
		});		
		tabPanel.add(textChat);
		textChat.setEditable(false);
		
		textPane.setEditable(false);
	    textScroll.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
	    textScroll.setBounds(172, 10, 822, 526);
	    tabPanel.add(textScroll);
	    textScroll.setViewportView(textPane);	    	
	    
	    fb_label.addMouseListener(new MouseAdapter() {
	    	public void mousePressed(MouseEvent arg0) {
	    		if (logged_in) return;
	    		NativeInterface.open();
	    		cwc.webBrowser.navigate("https://www.facebook.com/dialog/oauth?scope=xmpp_login&redirect_uri=https://www.facebook.com/connect/login_success.html&display=popup&response_type=token&client_id=284623318334487");
	    		new Thread(new FBChatClient(myself)).start();
	    	}
	    });
	    fb_label.setToolTipText("\u767B\u5165Facebook");
	    fb_label.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
	    fb_label.setBounds(11, 544, 50, 50);
		BufferedImage img_scaled = null, img = null;
		try {
			img_scaled = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
			img = ImageIO.read(new URL("http://profile.ak.fbcdn.net/hprofile-ak-snc6/c13.12.160.160/281468_259870444030331_5568518_n.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		img_scaled.createGraphics().drawImage(img, 0, 0, 50, 50, null);
		fb_label.setIcon(new ImageIcon(img_scaled));
	    tabPanel.add(fb_label);
	    
	    lblfacebook.setBounds(64, 579, 150, 15);
	    tabPanel.add(lblfacebook);
	    
	    usernameLabel.setBounds(64, 579, 150, 15);
	    usernameLabel.setVisible(false);
	    tabPanel.add(usernameLabel);
	    
	    profilePicLabel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
	    profilePicLabel.setBounds(11, 544, 50, 50);
	    profilePicLabel.setVisible(false);
	    tabPanel.add(profilePicLabel);
	    
	    JScrollPane onlineListScrollPane = new JScrollPane();
	    onlineListScrollPane.setBounds(11, 45, 150, 220);
	    tabPanel.add(onlineListScrollPane);
	    onlineListScrollPane.setViewportView(onlineList);
	    
	    onlineList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    onlineList.setLayoutOrientation(JList.VERTICAL);
	    onlineList.setFixedCellHeight(25);
	    onlineList.addMouseListener(new MouseAdapter() {
			@SuppressWarnings("unchecked")
	        public void mouseClicked(MouseEvent evt) {
	        	// when double clicked on a friend name
				JList<FBUser> list = (JList<FBUser>)evt.getSource();
	            if (evt.getClickCount() == 2) {
	            	if (list.getSelectedValue() == null) return;
	            	String id = list.getSelectedValue().getId();
	                if (id == null) return;
	                
	                clearText();
	                printSystemMsg("<\u7cfb\u7d71\u8a0a\u606f> \u5df2\u958b\u555f\u8207" + cwc.fbClient.getName(id) + "\u7684\u5c0d\u8a71\u3002\n");
	        	    textChat.setEditable(true);
	            	textChat.requestFocus();
	            	cwc.fbClient.openConversation(id);
	            }
	            offlineList.clearSelection();
	        }
	    });
	    onlineList.setCellRenderer(new myCellRenderer());
	    
	    JScrollPane offlineListScrollPane = new JScrollPane();
	    offlineListScrollPane.setBounds(11, 315, 150, 220);
	    tabPanel.add(offlineListScrollPane);
	    offlineListScrollPane.setViewportView(offlineList);
	    
	    offlineList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    offlineList.setLayoutOrientation(JList.VERTICAL);
	    offlineList.setFixedCellHeight(25);
	    offlineList.addMouseListener(new MouseAdapter() {
			@SuppressWarnings("unchecked")
	        public void mouseClicked(MouseEvent evt) {
	        	// when double clicked on a friend name
				JList<FBUser> list = (JList<FBUser>)evt.getSource();
	            if (evt.getClickCount() == 2) {
	            	if (list.getSelectedValue() == null) return;
	                String id = list.getSelectedValue().getId();
	                if (id == null) return;
	                
	                clearText();
	                printSystemMsg("<\u7cfb\u7d71\u8a0a\u606f> \u5df2\u958b\u555f\u8207" + cwc.fbClient.getName(id) + "\u7684\u5c0d\u8a71\u3002\n");
	        	    textChat.setEditable(true);
	            	textChat.requestFocus();
	            	cwc.fbClient.openConversation(id);
	            }
	            onlineList.clearSelection();
	        }
	    });
	    offlineList.setCellRenderer(new myCellRenderer());
	    
	    JLabel offlineLabel = new JLabel("\u96E2\u7DDA\u5217\u8868");
		offlineLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				cwc.fbClient.updateUserList();
			}
		});
	    offlineLabel.setToolTipText("\u9EDE\u64CA\u5373\u53EF\u5237\u65B0\u5217\u8868");
	    offlineLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    offlineLabel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
	    offlineLabel.setBounds(11, 280, 150, 25);
	    tabPanel.add(offlineLabel);
	}
	
	public void printInputMsg(String name) {
		textPane.setEditable(true);
		if (lastEnd >= 0) {
			textPane.setSelectionStart(lastEnd);
			lastEnd = -1;
		}
		else
			textPane.setSelectionStart(textPane.getText().length());
		lastEnd = textPane.getSelectionStart();
		textPane.setSelectionEnd(textPane.getText().length());
		textPane.replaceSelection("");
		
		textPane.setSelectionStart(textPane.getText().length());
		textPane.setSelectionEnd(textPane.getText().length());
		textPane.setCharacterAttributes(textPane.getStyle("SystemMessage"), true);
		textPane.replaceSelection(name + "\u6b63\u5728\u8f38\u5165\u8a0a\u606f..");
		textPane.setEditable(false);
	}
	
	public void printSystemMsg(String s) {
		textPane.setEditable(true);
		if (lastEnd >= 0) {
			textPane.setSelectionStart(lastEnd);
			lastEnd = -1;
		}
		else
			textPane.setSelectionStart(textPane.getText().length());
		textPane.setSelectionEnd(textPane.getText().length());
		textPane.replaceSelection("");
		
		textPane.setSelectionStart(textPane.getText().length());
		textPane.setSelectionEnd(textPane.getText().length());
		textPane.setCharacterAttributes(textPane.getStyle("SystemMessage"), true);
		textPane.replaceSelection(s);
		textPane.setEditable(false);
	}
	
	public void printText(String s, String username) {
		textPane.setEditable(true);
		if (lastEnd >= 0) {
			textPane.setSelectionStart(lastEnd);
			lastEnd = -1;
		}
		else
			textPane.setSelectionStart(textPane.getText().length());
		textPane.setSelectionEnd(textPane.getText().length());
		textPane.replaceSelection("");
				
		textPane.setSelectionStart(textPane.getText().length());
		textPane.setSelectionEnd(textPane.getText().length());
		if (username.equals(cwc.fbClient.getUserName()))
			textPane.setCharacterAttributes(textPane.getStyle("UserName"), true);
		else
			textPane.setCharacterAttributes(textPane.getStyle("FriendName"), true);
		textPane.replaceSelection(username + ":\n");

		textPane.setSelectionStart(textPane.getText().length());
		textPane.setSelectionEnd(textPane.getText().length());
		textPane.setCharacterAttributes(textPane.getStyle("NormalMessage"), true);
		textPane.replaceSelection(" \u2027 " + s + "\n");
		textPane.setEditable(false);
	}
	
	public void clearText() {
		textPane.setEditable(true);
		textPane.setText("");
		textPane.setEditable(false);
	}
	
	public void setProfilePic(String url) {
		BufferedImage img_scaled = null, img = null;
		try {
			img_scaled = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
			img = ImageIO.read(new URL(url));
		} catch (IOException e) {
			e.printStackTrace();
		}
		img_scaled.createGraphics().drawImage(img, 0, 0, 50, 50, null);
		profilePicLabel.setIcon(new ImageIcon(img_scaled));
	}
	
	public void setUserName(String username) {
		usernameLabel.setText(username);
	}
}

class myCellRenderer extends JLabel implements ListCellRenderer<Object> {

	private static final long serialVersionUID = -7010512396484912136L;
	
	public myCellRenderer () { this.setOpaque(true); }

	@SuppressWarnings("rawtypes")
	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		if (value != null) {
			FBUser user = (FBUser)value;
			setText(user.getName());
		}
        setBackground(isSelected? list.getSelectionBackground(): list.getBackground());
        setForeground(isSelected? list.getSelectionForeground(): list.getForeground());
		return this;
	}
}