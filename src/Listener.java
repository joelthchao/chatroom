import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.plaf.basic.BasicTabbedPaneUI.TabSelectionHandler;

import org.json.simple.*;

class Listener extends Frame implements Runnable
{
	private static final long serialVersionUID = 1L;
	Socket socket;
	DataOutputStream out; // client->server
	DataInputStream in;   // server->client
	ChatWindowClient cwc;
	boolean robotMode = false;	
	VideoChat v;

	public Listener(ChatWindowClient c) {		
		cwc = c; 
		try	{
			socket = new Socket(cwc.server_ip, 2525);
			out = new DataOutputStream(socket.getOutputStream());
			in = new DataInputStream(socket.getInputStream());
			
			if (cwc.username.length() == 0) {
				cwc.username = generateUsername();
				cwc.tabs.get(0).textUsername.setText(cwc.username);
			}
			out.writeUTF(cwc.username);
			setProfilePic(cwc.tabs.get(0).selfProfilePic);
			
			String message = in.readUTF();
			if (message.startsWith("(UserList)"))
				parseUserList(message);
		} catch (ConnectException e) {
			disconnect();
			JOptionPane.showMessageDialog(cwc.frmLabChatroom, "\u4f3a\u670d\u5668\u9023\u7dda\u5931\u6557\uff01", "Error", JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run()
	{
		while(true)
		{				
			String receivedLine = "";
			try {
				if (in == null) return;
				receivedLine = in.readUTF();
				System.out.println("Message received: " + receivedLine);
				if (!isSpecialMsg(receivedLine)) {
					parseAll(receivedLine);
					if (robotMode) {
						int room_id = Integer.parseInt(receivedLine.substring(receivedLine.indexOf('%', receivedLine.indexOf('%') + 1) + 1, receivedLine.indexOf(')')));
						String response = "(text%" + cwc.username + "%" + room_id + ")" + getRobotText(receivedLine.substring(receivedLine.indexOf(')') + 1)); 
						out.writeUTF(response);
						receivedLine = in.readUTF();
						parseAll(receivedLine);
					}
				}
			} catch (SocketException e) {
				return;
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
	}
	
	public void disconnect() {
		try {
			if (socket != null) {
				socket.close();
				socket = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		ChatTabClient ctc = cwc.tabs.get(0);
		ctc.userList.removeAllElements();
		ctc.textUsername.setEditable(true);
		ctc.btnConnect.setEnabled(true);
		ctc.btnDisconnect.setEnabled(false);
		ctc.btnConnect.setVisible(true);
		ctc.btnDisconnect.setVisible(false);
		ctc.textChat.setEditable(false);
		ctc.textPane.setEditable(true);
		ctc.textPane.setText("");
		ctc.textPane.setEditable(false);
		cwc.username = "";
		cwc.tabs.get(0).selfProfilePic.setIcon(null);
	    cwc.removeAllTabs();
	    robotMode = false;
	}
	
	public void printText(int r, String s) {
		printText(r, s, "NormalMessage");
	}
	
	public void printText(int r, String s, String style) {
		if (cwc.tabs.get(r) != null) {
			JTextPane textPane = cwc.tabs.get(r).textPane;
			textPane.setEditable(true);
			textPane.setSelectionStart(textPane.getText().length());
			textPane.setSelectionEnd(textPane.getText().length());
		    textPane.setCharacterAttributes(textPane.getStyle(style), true);
			textPane.replaceSelection(s);
			textPane.setEditable(false);
		}
	}
	
	public void printIcon(int r, String s) {
		JTextPane textPane = cwc.tabs.get(r).textPane;
		textPane.setSelectionStart(textPane.getText().length());
		textPane.setSelectionEnd(textPane.getText().length());		
		textPane.insertIcon(new ImageIcon(s));
	}
	
	private boolean isSpecialMsg(String message) throws IOException {
		String header = message.substring(0, message.indexOf(")") + 1), username, ip;
		int room_id;
		FileDialog fd;
		String transmitterName, transmitterInfo, receiverInfo;
		
		switch (header) {
		case "(UserConnected)":
			room_id = Integer.parseInt(message.substring(message.indexOf(')') + 1, message.indexOf('%')));
			username = message.substring(message.indexOf("%") + 1);
			cwc.tabs.get(room_id).userList.addElement(username);
			printText(room_id, "<\u7cfb\u7d71\u8a0a\u606f> \u4f7f\u7528\u8005 " + username + " \u52a0\u5165\u4e86\u623f\u9593 " + room_id + "\u3002\n", "SystemMessage");
			return true;
		case "(UserDisconnected)":
			room_id = Integer.parseInt(message.substring(message.indexOf(')') + 1, message.indexOf('%')));
			username = message.substring(message.indexOf("%") + 1);
			cwc.tabs.get(room_id).userList.removeElement(username);
			printText(room_id, "<\u7cfb\u7d71\u8a0a\u606f> \u4f7f\u7528\u8005 " + username + " \u96e2\u958b\u4e86\u623f\u9593 " + room_id + "\u3002\n", "SystemMessage");
			return true;
		case "(UserList)":
			parseUserList(message);
			return true;
		case "(IPReply)":
			username = message.substring(message.indexOf(')') + 1, message.indexOf('%'));
			ip = message.substring(message.indexOf('%') + 1);
			fd = new FileDialog(cwc.dialogFrame, "Load file..", FileDialog.LOAD); // use FileDialog to get filename
			fd.setLocationByPlatform(true);
			fd.setVisible(true);			
			if (fd.getFile() == null)
				printText(cwc.getRoomIdOnFocus(), "<\u7cfb\u7d71\u8a0a\u606f> \u53d6\u6d88\u50b3\u6a94\u3002\n", "SystemMessage");
			else {
				out.writeUTF("(FileRequest)" + username); // transmitter->server: (FileRequest)username
				new Thread(new Transmitter(ip, fd, this)).start();
			}
			return true;
		case "(WhisperRequest)":
			username = message.substring(message.indexOf(')') + 1);
			if (JOptionPane.showConfirmDialog(cwc.frmLabChatroom, username + " \u60f3\u8ddf\u4f60\u8b1b\u500b\u6084\u6084\u8a71\u5152", "Whisper", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
				out.writeUTF("(OpenRoomRequest)" + username);
			else
				out.writeUTF("(RejectInvitation)" + username);
			return true;
		case "(FileRequest)":
			username = message.substring(message.indexOf(')') + 1);
			fd = new FileDialog(cwc.dialogFrame, "Save file..", FileDialog.SAVE); // use FileDialog to get filename
			fd.setLocationByPlatform(true);
            new Thread(new Receiver(username, fd, this)).start();
			return true;
		case "(Opened_Room)":
			room_id = Integer.parseInt(message.substring(message.indexOf(")") + 1));
			cwc.createNewRoom(room_id);
			return true;
		case "(Opened_Whisper)":
			room_id = Integer.parseInt(message.substring(message.indexOf(")") + 1, message.indexOf('%')));
			cwc.createNewRoom(room_id);
			parseUserList(message);
			setWhisperRoom(room_id);
			return true;
		case "(UserNameConflict)":
			disconnect();
			JOptionPane.showMessageDialog(cwc.frmLabChatroom, "\u5df2\u6709\u76f8\u540c\u540d\u7a31\u4f7f\u7528\u8005\u767b\u5165\uff01", "Error", JOptionPane.ERROR_MESSAGE);
			return true;
		case "(Invite_Room)":
			room_id = Integer.parseInt(message.substring(message.indexOf(')') + 1, message.indexOf('%')));
			username = message.substring(message.indexOf("%") + 1);
			if (JOptionPane.showConfirmDialog(cwc.frmLabChatroom, username + " \u60f3\u8ddf\u4f60\u958b\u623f\u9593\n\u662f\u5426\u63a5\u53d7\u795d\u798f\uff1f(y/n)", "Invitation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				out.writeUTF("(ReceiveInvitation)" + room_id + "%" + cwc.username);
				cwc.createNewRoom(room_id);
			}
			else
				out.writeUTF("(RejectInvitation)" + username);
			return true;
		case "(RejectInvitation)":
			JOptionPane.showMessageDialog(cwc.frmLabChatroom, "\u88ab\u6253\u69cd\u60f9 \u310f\u310f", "lol", JOptionPane.INFORMATION_MESSAGE);
			return true;
		case "(Close_Room)":
			room_id = Integer.parseInt(message.substring(message.indexOf(")") + 1));
			cwc.removeTab(room_id);
			return true;
		case "(VideoChatRequest)":
			transmitterName = message.substring(message.indexOf(")") + 1);
			if (JOptionPane.showConfirmDialog(cwc.dialogFrame, transmitterName 
					         + "\u9080\u8acb\u4f60\u9032\u884c\u8996\u8a0a\u901a\u8a71\n\u662f\u5426\u63a5\u53d7\u795d\u798f\uff1f(y/n)",
							"\u6709\u4EBA\u9080\u8ACB\u4F60\u804A\u5929", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				out.writeUTF("(ReceiveVideoChat)" + transmitterName + "_"
						+ cwc.username);
				v = new VideoChat(cwc);
			} else
				out.writeUTF("(RejectVideoChat)" + transmitterName);
			return true;
		case "(TransmitterBeginVideoChat)":
			receiverInfo = message.substring(message.indexOf(")") + 1,
					message.indexOf("_"));
			transmitterInfo = message.substring(message.indexOf("_") + 1);
			v = new VideoChat(cwc);
			v.setTitle("Transmitter");
			v.sendLocalVideo(receiverInfo);
			v.receiveRemoteVideo(transmitterInfo);
			return true;
		case "(ReceiverBeginVideoChat)":
			receiverInfo = message.substring(message.indexOf(")") + 1,
					message.indexOf("_"));
			transmitterInfo = message.substring(message.indexOf("_") + 1);
			v.setTitle("Receiver");
			v.sendLocalVideo(transmitterInfo);
			v.receiveRemoteVideo(receiverInfo);
			return true;
		case "(RejectVideoChat)":
			v.close();			
			return true;
		case "(ReceiveVibrate)":
			int r_vibrate = Integer.parseInt(message.substring(message.indexOf(')') + 1));
			if (cwc.tabs.get(r_vibrate) != null)
				cwc.tabs.get(r_vibrate).vibrate();
			return true;
		}
		
		
		return false;
	}
	
	private void parseAll(String s) throws IOException {
		if (!s.startsWith("(text"))
			throw new IOException("Invalid message: " + s);
		
		int offset1 = s.indexOf("%");
		int offset2 = s.indexOf("%", offset1+1);
		int offset3 = s.indexOf(")", offset2+1);
		String name = s.substring(offset1+1, offset2);
		String room_str = s.substring(offset2+1, offset3);
		int r = Integer.parseInt(room_str);
		
		if (cwc.tabs.get(r) != null){
			JTextPane textPane = cwc.tabs.get(r).textPane;
			textPane.setEditable(true);	
			textPane.setSelectionStart(textPane.getText().length());
			textPane.setSelectionEnd(textPane.getText().length());
			if (name.equals(cwc.username))
				textPane.setCharacterAttributes(textPane.getStyle("UserName"), true);
			else
				textPane.setCharacterAttributes(textPane.getStyle("FriendName"), true);
			textPane.replaceSelection(name + ":\n");
			textPane.setEditable(false);
		}
		
		printText(r, " \u2027 ");
		int begin = offset3+1;
		int end = offset3+1;
		String pureText = s.substring(begin);
		if (pureText.indexOf("www.youtube.com") != -1 && pureText.indexOf("?v=") != -1){
			int v = pureText.indexOf("?v=")+3;			
			printText(r, "\n");
			//cwc.tabs.get(r).addYouTube("google.com");
			cwc.tabs.get(r).addYouTube("www.youtube.com/embed/"+pureText.substring(v));
			System.out.println("youtube!");
			printText(r, "\n");	
		}
		else if (pureText.indexOf("趙式隆好帥") != -1){	
			printText(r, "\n");
			//cwc.tabs.get(r).addYouTube("google.com");
			cwc.tabs.get(r).addYouTube("https://www.google.com.tw/#hl=zh-TW&q=趙式隆");
			printText(r, "\n");	
		}
		else{				
			IconInfo getIcon = getIconPos(s, begin);
			while (getIcon != null){					
				end = getIcon.pos;
				String cut = "";
				cut = s.substring(begin, end);
				printText(r, cut);
				printIcon(r, getIcon.name);
				begin = end+7;
				if (begin >= s.length())
					break;
				getIcon = getIconPos(s, begin);
			}
			String last = s.substring(begin)+"\n";
			printText(r, last);
		}
	}
	
	private IconInfo getIconPos(String s, int b) {
		Vector<Integer> find = new Vector<Integer>();
		find.add(s.indexOf("{emo01}", b));
		find.add(s.indexOf("{emo02}", b));
		find.add(s.indexOf("{emo03}", b));
		find.add(s.indexOf("{emo04}", b));
		find.add(s.indexOf("{emo05}", b));
		find.add(s.indexOf("{emo06}", b));
		find.add(s.indexOf("{emo07}", b));
		find.add(s.indexOf("{emo08}", b));
		find.add(s.indexOf("{emo09}", b));
		find.add(s.indexOf("{emo10}", b));
		find.add(s.indexOf("{emo11}", b));
		find.add(s.indexOf("{emo12}", b));
		find.add(s.indexOf("{emo13}", b));
		find.add(s.indexOf("{emo14}", b));
		find.add(s.indexOf("{emo15}", b));
		find.add(s.indexOf("{emo16}", b));		
		int min = 10000;
		int min_index = 0;
		for (int i = 0; i < find.size(); i++){
			if (find.get(i) <= min && find.get(i) != -1){
				min = find.get(i);			
				min_index = i;
			}			
		}
		IconInfo ret = null;
		if (min != 10000){
			if (find.get(min_index) != -1){
				switch (min_index){
					case 0:
						ret = new IconInfo("images/1.png", find.get(min_index));
						break;
					case 1:
						ret = new IconInfo("images/2.png", find.get(min_index));
						break;
					case 2:
						ret = new IconInfo("images/3.png", find.get(min_index));
						break;
					case 3:
						ret = new IconInfo("images/4.png", find.get(min_index));
						break;
					case 4:
						ret = new IconInfo("images/5.png", find.get(min_index));
						break;
					case 5:
						ret = new IconInfo("images/6.png", find.get(min_index));
						break;
					case 6:
						ret = new IconInfo("images/7.png", find.get(min_index));
						break;
					case 7:
						ret = new IconInfo("images/8.png", find.get(min_index));
						break;
					case 8:
						ret = new IconInfo("images/9.png", find.get(min_index));
						break;
					case 9:
						ret = new IconInfo("images/10.png", find.get(min_index));
						break;
					case 10:
						ret = new IconInfo("images/11.png", find.get(min_index));
						break;
					case 11:
						ret = new IconInfo("images/12.png", find.get(min_index));
						break;
					case 12:
						ret = new IconInfo("images/13.png", find.get(min_index));
						break;
					case 13:
						ret = new IconInfo("images/14.png", find.get(min_index));
						break;
					case 14:
						ret = new IconInfo("images/15.png", find.get(min_index));
						break;
					case 15:
						ret = new IconInfo("images/16.png", find.get(min_index));
						break;						
				}
			}
		}
		return ret;
	}
	
	private void parseUserList(String userList) {
		int room_id = Integer.parseInt(userList.substring(userList.indexOf(')') + 1, userList.indexOf('%'))),
			  begin = userList.indexOf('%') + 1,
				end = userList.indexOf('%', begin);
		while (end > 0) {
			cwc.tabs.get(room_id).userList.addElement(userList.substring(begin, end));
			begin = end + 1;
			end = userList.indexOf('%', begin);
		}
	}
	
	public boolean isConnected() { return socket != null; }
	
	public void sendVibration(int room_id){
		try {
			out.writeUTF("(sendVibrate)" + room_id);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendInvitation(int room_id, String username) {
		try {
			out.writeUTF("(AddPeopleRequest)" + room_id + "%" + username);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void setWhisperRoom(int room_id) throws IOException {
		String friendName = null;
		if (cwc.tabs.get(room_id).userList.firstElement().equals(cwc.username))
			friendName = cwc.tabs.get(room_id).userList.lastElement();
		else
			friendName = cwc.tabs.get(room_id).userList.firstElement();
		
		cwc.tabbedPane.setTitleAt(cwc.tabbedPane.getSelectedIndex(), "with " + friendName);
		cwc.tabs.get(room_id).friendNameLabel.setText(friendName);
		setProfilePic(friendName, cwc.tabs.get(room_id).friendProfilePic);
		
		cwc.tabs.get(room_id).btnLeaveRoom.setVisible(false);
		cwc.tabs.get(room_id).btnInvitation.setVisible(false);
		cwc.tabs.get(room_id).btnWhisper.setVisible(false);
		cwc.tabs.get(room_id).btnLeaveWhisper.setVisible(true);
		cwc.tabs.get(room_id).btnRobot.setVisible(true);
		cwc.tabs.get(room_id).scrollPane.setVisible(false);
		cwc.tabs.get(room_id).label.setVisible(false);
		cwc.tabs.get(room_id).selfNameLabel.setText(cwc.username);
		cwc.tabs.get(room_id).selfNameLabel.setVisible(true);
		cwc.tabs.get(room_id).friendNameLabel.setVisible(true);
		cwc.tabs.get(room_id).friendProfilePic.setVisible(true);
	}
	
	private String getRobotText(String message) throws IOException {
		String url = "http://sandbox.api.simsimi.com/request.p?key=4636d53c-f01f-469e-86d3-74d0a19c26d8&lc=zh&ft=1.0&text=" + URLEncoder.encode(message, "UTF-8");
		BufferedReader in = new BufferedReader(new InputStreamReader(new URL(url).openStream(), "UTF-8"));
		String json_result = in.readLine();
		
		JSONObject json_obj = (JSONObject)JSONValue.parse(json_result);
		if ((Long)json_obj.get("result") == 100)
			return (String)json_obj.get("response");
		else {
			// if API daily limit exceeded
			try {
				Thread.sleep((int)(1500 * Math.random() + 1200)); // add random delay
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			Random rand = new Random();
			String[] randomText = {"", "嗯嗯", "呵呵", "哈哈", "喔喔", "?", "恩恩", "", "ㄏㄏ", "蛤"};
			String[] randomFace = {"", "^^", "><", ":目", "?", "~", "XD", "QQ"};
			return randomText[rand.nextInt(randomText.length)] + randomFace[rand.nextInt(randomFace.length)];
		}
	}
	
	private String generateUsername() {
		Random rng = new Random();
		Scanner sc = null;
		try {
			sc = new Scanner(new FileInputStream("username.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String ret = sc.nextLine();
		for (int i = 0, limit = rng.nextInt(20000); i < limit; ++i)
			ret = sc.nextLine();
		sc.close();
		return ret;
	}
	
	private void setProfilePic(JLabel target) throws IOException {
		setProfilePic(cwc.username, target);
	}
	
	private void setProfilePic(String username, JLabel target) throws IOException {
		// image search query via Google Image API
		String url = "http://ajax.googleapis.com/ajax/services/search/images?v=1.0&imgtype=face&rsz=8&q=" + URLEncoder.encode(username, "UTF-8");
		BufferedReader in = new BufferedReader(new InputStreamReader(new URL(url).openStream(), "UTF-8"));
		JSONObject json_obj = (JSONObject)JSONValue.parse(in.readLine());
		in.close();
		
		// parse JSON to get image url
		json_obj = (JSONObject)json_obj.get("responseData");
		JSONArray json_array = null;
		if (json_obj != null)
			json_array = (JSONArray)json_obj.get("results");
		
		// set image from url
		BufferedImage img_scaled = new BufferedImage(150, 150, BufferedImage.TYPE_INT_ARGB), img = null;
		if (json_array != null)
			for (int i = 0; i < json_array.size(); ++i) {
				json_obj = (JSONObject)json_array.get(i);
				if (Integer.parseInt((String)json_obj.get("width")) > 768 || Integer.parseInt((String)json_obj.get("height")) > 768 ||
					Integer.parseInt((String)json_obj.get("width")) < 100 || Integer.parseInt((String)json_obj.get("height")) < 100)
					continue;
				url = (String)json_obj.get("url");
				if (isValidImageExt(url.substring(url.lastIndexOf('.') + 1))) {
					try {
						img = ImageIO.read(new URL(url));
					} catch (IIOException e) {
						img = null;
						continue;
					}			
					break;
				}
			}
		
		if (img == null)
			img = ImageIO.read(new URL("http://image.kmt.org.tw/people/20090606164842.jpg"));
			
		img_scaled.createGraphics().drawImage(img, 0, 0, 150, 150, null);
		ImageIcon icon = new ImageIcon(img_scaled);
		if (username.equals(cwc.username))
			cwc.userIcon = icon;
		target.setIcon(icon);
	}
	
	private boolean isValidImageExt(String ext) {
		String[] validExts = {"bmp", "jpg", "gif", "png", "BMP", "JPG", "GIF", "PNG"};
		return Arrays.asList(validExts).contains(ext);
	}
}

