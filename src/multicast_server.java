import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class multicast_server
{
	private static ServerSocket serverSocket;
	public Hashtable<Integer, Hashtable<String, DataOutputStream>> ht_rooms = new Hashtable<Integer, Hashtable<String, DataOutputStream>>();
	public Hashtable<String, UserData> ht_user = new Hashtable<String, UserData>();
	public int roomIndex = 0;
	
	public multicast_server() throws IOException
	{
		serverSocket = new ServerSocket(2525);
		ht_rooms.put(0, new Hashtable<String, DataOutputStream>());
		System.out.println("Waiting for client to connect...");
	}
	
	public void run() throws IOException
	{
		while (true)
		{
			Socket socket = serverSocket.accept();
			String ip = socket.getInetAddress().getHostAddress();
			System.out.println("Connected from client " + ip);
			
			DataInputStream in = new DataInputStream(socket.getInputStream());
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			
			String username = in.readUTF();
			synchronized (ht_user) {
				// send userList to the new user
				String userStr = "(UserList)" + 0 + "%";
				for (Enumeration<String> e = ht_user.keys(); e.hasMoreElements();)
					userStr += e.nextElement() + "%";
				out.writeUTF(userStr);
			
				// update user database
				if (!ht_user.containsKey(username))
					ht_user.put(username, new UserData(username, ip, socket));
				else {
					out.writeUTF("(UserNameConflict)");
					continue;
				}
				
				// start listening
				ht_user.get(username).enterRoom(0);
				new Thread(new ServerThread(this, ht_user.get(username))).start();
			}

			Hashtable<String, DataOutputStream> lobby;
			synchronized (ht_rooms) {
				lobby = ht_rooms.get(0);
			}
			
			// broadcast user connect message
			synchronized (lobby) {
				lobby.put(username, out);
				for (Enumeration<DataOutputStream> e = lobby.elements(); e.hasMoreElements();)
					e.nextElement().writeUTF("(UserConnected)" + 0 + "%" + username);
			}
		}
	}

	public static void main(String[] args)
	{
		try {
			multicast_server s = new multicast_server();
			s.run();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class ServerThread implements Runnable
{
	private multicast_server master;
	private UserData userdata;

	public ServerThread(multicast_server ss, UserData u)
	{
		this.master = ss;
		this.userdata = u;
	}

	public void run()
	{
		try {
			listen();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void listen() throws IOException
	{
		try {
			DataInputStream in = new DataInputStream(userdata.getSocket().getInputStream());
			
			while (true) {
				String message = in.readUTF();
				System.out.println("Message received: " + message);

				// for normal Text, broadcast it to everyone in this room
				if (!isSpecialMsg(message)) {
					Hashtable<String, DataOutputStream> ht_room; 
					int room_id = Integer.parseInt(message.substring(message.indexOf('%', message.indexOf('%') + 1) + 1, message.indexOf(')')));
					
					synchronized (master.ht_rooms) {
						ht_room = master.ht_rooms.get(room_id);
					}

					synchronized (ht_room) {
						for(Enumeration<DataOutputStream> e = ht_room.elements(); e.hasMoreElements();)
							e.nextElement().writeUTF(message);
					}
				}
			}
		}
		catch (EOFException e) {}
		catch (SocketException e) {}
		catch (IOException e) { e.printStackTrace(); }
		finally {
			// broadcast user disconnect message
			synchronized (master.ht_rooms) {
				for (Enumeration<Integer> r = userdata.rooms(); r.hasMoreElements();) {
					int room_id = r.nextElement();
					Hashtable<String, DataOutputStream> ht_room = master.ht_rooms.get(room_id);
					synchronized (ht_room) {
						userdata.leaveRoom(room_id);
						ht_room.remove(userdata.getName());
						for (Enumeration<DataOutputStream> e = ht_room.elements(); e.hasMoreElements();) {
							e.nextElement().writeUTF("(UserDisconnected)" + room_id + "%" + userdata.getName());
						}
						if (ht_room.isEmpty() && room_id != 0)
							master.ht_rooms.remove(room_id);
					}
				}
			}
			
			System.out.println("Remove connection " + userdata.getSocket());
			userdata.getSocket().close(); // close connection
			synchronized (master.ht_user) {
				master.ht_user.remove(userdata.getName()); // remove user
			}
		}
	}
	
	private boolean isSpecialMsg(String msg) throws IOException {
		String header = msg.substring(0, msg.indexOf(")") + 1), username, ip;
		DataOutputStream out;
		Hashtable<String, DataOutputStream> ht_room;
		int room_id;
		String receiverName, transmitterName, receiverIP, transmitterIP;
		
		switch (header) {
		case "(IPRequest)":
			username = msg.substring(msg.indexOf(")") + 1);
			synchronized (master.ht_user) {
				ip = master.ht_user.get(username).getIp();
			}
			out = new DataOutputStream(userdata.getSocket().getOutputStream());
			out.writeUTF("(IPReply)" + username + "%" + ip);
			return true;
		case "(WhisperRequest)":
		case "(FileRequest)":
			username = msg.substring(msg.indexOf(")") + 1);
			synchronized (master.ht_user) {
				out = new DataOutputStream(master.ht_user.get(username).getSocket().getOutputStream());
			}
			out.writeUTF(header + userdata.getName());
			return true;
		case "(OpenRoomRequest)":
			room_id = ++master.roomIndex;
			out = new DataOutputStream(userdata.getSocket().getOutputStream());
			System.out.println("(Opened_Room)" + Integer.toString(room_id));

			ht_room = new Hashtable<String, DataOutputStream>(); // create a new hashtable for new room, and insert it into ht_rooms
			synchronized (master.ht_rooms) {
				master.ht_rooms.put(room_id, ht_room);
			}
			synchronized (ht_room) {
				ht_room.put(userdata.getName(), out);
				userdata.enterRoom(room_id);
				
				username = msg.substring(msg.indexOf(")") + 1);
				String replyMsg = "(Opened_Room)" + Integer.toString(room_id);
				if (!username.isEmpty()) {
					synchronized (master.ht_user) {
						ht_room.put(username, new DataOutputStream(master.ht_user.get(username).getSocket().getOutputStream()));
						master.ht_user.get(username).enterRoom(room_id);
					}
					replyMsg = "(Opened_Whisper)" + room_id + "%";
					for (Enumeration<String> e = ht_room.keys(); e.hasMoreElements();)
						replyMsg += e.nextElement() + "%";
				}
				for (Enumeration<DataOutputStream> e = ht_room.elements(); e.hasMoreElements();)
					e.nextElement().writeUTF(replyMsg);
				if (username.isEmpty())
					out.writeUTF("(UserConnected)" + room_id + "%" + userdata.getName());
			}
			return true;
		case "(LeaveRoomRequest)":
			room_id = Integer.parseInt(msg.substring(header.indexOf(')') + 1));
			userdata.leaveRoom(room_id);
			
			synchronized (master.ht_rooms) {
				ht_room = master.ht_rooms.get(room_id);
			}
			synchronized (ht_room) {
				ht_room.remove(userdata.getName());			
				// broadcast user disconnect message
				for (Enumeration<DataOutputStream> e = ht_room.elements(); e.hasMoreElements();)
					e.nextElement().writeUTF("(UserDisconnected)" + room_id + "%" + userdata.getName());
				if (ht_room.isEmpty() && room_id != 0)
					master.ht_rooms.remove(room_id);
			}
			return true;
		case "(LeaveWhisperRequest)":
			room_id = Integer.parseInt(msg.substring(header.indexOf(')') + 1));
			synchronized (master.ht_rooms) {
				ht_room = master.ht_rooms.get(room_id);
			}
			synchronized (ht_room) {
				for (Enumeration<DataOutputStream> e = ht_room.elements(); e.hasMoreElements();)
					e.nextElement().writeUTF("(Close_Room)" + room_id);
				synchronized (master.ht_user) {
					for (Enumeration<String> e = ht_room.keys(); e.hasMoreElements();)
						master.ht_user.get(e.nextElement()).leaveRoom(room_id);
				}
				master.ht_rooms.remove(room_id);
			}
			return true;
		case "(AddPeopleRequest)":
			room_id = Integer.parseInt(msg.substring(msg.indexOf(')') + 1, msg.indexOf('%')));
			username = msg.substring(msg.indexOf("%") + 1);
			synchronized (master.ht_user) {
				out = new DataOutputStream(master.ht_user.get(username).getSocket().getOutputStream());
			}
			out.writeUTF("(Invite_Room)" + room_id + "%" + userdata.getName());
			return true;
		case "(ReceiveInvitation)":
			room_id = Integer.parseInt(msg.substring(msg.indexOf(')') + 1, msg.indexOf('%')));
			synchronized (master.ht_rooms) {
				ht_room = master.ht_rooms.get(room_id);
			}
			
			username = msg.substring(msg.indexOf("%") + 1);
			synchronized (master.ht_user) {
				out = new DataOutputStream(master.ht_user.get(username).getSocket().getOutputStream());
				master.ht_user.get(username).enterRoom(room_id);
			}
			
			synchronized (ht_room) {
				// send userList to the new user
				String userStr = "(UserList)" + room_id + "%";
				for (Enumeration<String> e = ht_room.keys(); e.hasMoreElements();)
					userStr += e.nextElement() + "%";
				out.writeUTF(userStr);
			
				// broadcast user connect message
				ht_room.put(username, out);
				for (Enumeration<DataOutputStream> e = ht_room.elements(); e.hasMoreElements();)
					e.nextElement().writeUTF("(UserConnected)" + room_id + "%" + username);
			}
			return true;
		case "(RejectInvitation)":
			username = msg.substring(msg.indexOf(")") + 1);
			synchronized (master.ht_user) {
				out = new DataOutputStream(master.ht_user.get(username).getSocket().getOutputStream());
			}
			out.writeUTF("(RejectInvitation)");
			return true;
		case "(VideoChatRequest)":
			receiverName = msg.substring(msg.indexOf(")") + 1, msg.indexOf("_"));
			transmitterName = msg.substring(msg.indexOf("_") + 1);
			synchronized (master.ht_user) {
				ip = master.ht_user.get(transmitterName).getIp();
			}
			synchronized (master.ht_user) {
				out = new DataOutputStream(master.ht_user.get(receiverName).getSocket().getOutputStream());
			}
			out.writeUTF("(VideoChatRequest)" + transmitterName);
			return true;
		case "(ReceiveVideoChat)":
			transmitterName = msg.substring(msg.indexOf(")") + 1, msg.indexOf("_"));
			receiverName = msg.substring(msg.indexOf("_") + 1);
			synchronized (master.ht_user) {
				out = new DataOutputStream(master.ht_user.get(transmitterName).getSocket().getOutputStream());
			}
			synchronized (master.ht_user) {
				receiverIP = master.ht_user.get(receiverName).getIp();
				transmitterIP = master.ht_user.get(transmitterName).getIp();
			}
			out.writeUTF("(TransmitterBeginVideoChat)"+receiverIP+":"+"5555_" + transmitterIP+":"+"5555");
			
			synchronized (master.ht_user) {
				out = new DataOutputStream(master.ht_user.get(receiverName).getSocket().getOutputStream());
			}
			out.writeUTF("(ReceiverBeginVideoChat)"+receiverIP+":"+"5555_" + transmitterIP+":"+"5555");
			return true;
		case "(RejectVideoChat)":
			transmitterName = msg.substring(msg.indexOf(")") + 1);
			synchronized (master.ht_user) {
				out = new DataOutputStream(master.ht_user.get(transmitterName).getSocket().getOutputStream());
			}
			out.writeUTF("(RejectVideoChat)");
			return true;		
		case "(sendVibrate)":
			int r_vibrate = Integer.parseInt(msg.substring(msg.indexOf(")") + 1));
			out = new DataOutputStream(userdata.getSocket().getOutputStream());
			out.writeUTF("(ReceiveVibrate)"+r_vibrate);
			synchronized (master.ht_rooms) {
				ht_room = master.ht_rooms.get(r_vibrate);
			}
			synchronized (ht_room) {		
				for (Enumeration<DataOutputStream> e = ht_room.elements(); e.hasMoreElements();)
					e.nextElement().writeUTF("(ReceiveVibrate)" + r_vibrate);
			}
			return true;
		}
		return false;
	}
	
}

class UserData {
	private String name;
	private String ip;
	private Socket socket;
	private Vector<Integer> roomList;
	
	public UserData(String n, String i, Socket s)
	{
		name = n;
		ip = i;
		socket = s;
		roomList = new Vector<Integer>();
	}
	
	public String getName()	{ return name; }
	public String getIp() {	return ip; }
	public Socket getSocket() {	return socket; }
	public void enterRoom(int room_id) { roomList.add(room_id); }
	public void leaveRoom(int room_id) { roomList.removeElement(room_id); }
	public Enumeration<Integer> rooms() { return roomList.elements(); }
}
