import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

import org.json.simple.*;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.*;

public class FBChatClient implements Runnable {

	private final static String apiKey = "284623318334487";
	private final FBChatClient myself = this;
	private String username = "";
	private String accessToken = "";
	private XMPPConnection connection; 
	private ChatManager chatmanager;
	private Chat conversation;
	final FBChatTab tab;
	Hashtable<String, FBUser> onlineList = new Hashtable<String, FBUser>();
	Hashtable<String, FBUser> offlineList = new Hashtable<String, FBUser>();
	Hashtable<String, MyMessageListener> listenerTable = new Hashtable<String, MyMessageListener>();
	Roster roster;
	RosterListener rosterListener = new RosterListener() {
	    public void entriesAdded(Collection<String> addresses) {}
	    public void entriesDeleted(Collection<String> addresses) {}
	    public void entriesUpdated(Collection<String> addresses) {}
	    public void presenceChanged(Presence presence) {
	    	if (offlineList.containsKey(presence.getFrom())) {
	    		onlineList.put(presence.getFrom(), offlineList.get(presence.getFrom()));
	    		offlineList.remove(presence.getFrom());
		    	updateUserList();
	    	}
	    	else if (onlineList.containsKey(presence.getFrom())){
	    		offlineList.put(presence.getFrom(), onlineList.get(presence.getFrom()));
	    		onlineList.remove(presence.getFrom());
		    	updateUserList();
	    	}
	    }
	};
	
	public FBChatClient(FBChatTab t) {
		tab = t;
	}
	
	public void run() {
		tab.cwc.browserFrame.setVisible(true); // invoke FB login dialog to get the OAuth token
	}
	
	public void clear() {
		if (connection != null)
			connection.disconnect();
		roster.removeRosterListener(rosterListener);
		onlineList.clear();
		offlineList.clear();
		accessToken = "";
		updateUserList();
		tab.logged_in = false;
	}
	
	public void setAccessToken(String t) { accessToken = t; }
	public String getAccessToken() { return accessToken; }
	
	public void setUserName() {
		String url = "https://graph.facebook.com/me?fields=picture,name&access_token=" + accessToken;
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new URL(url).openStream(), "UTF-8"));
			JSONObject data = (JSONObject)JSONValue.parse(in.readLine());
			in.close();
			this.username = (String)data.get("name");
			tab.setUserName(username);
			
			data = (JSONObject)data.get("picture");
			data = (JSONObject)data.get("data");
			url = (String)data.get("url");
			tab.setProfilePic(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public String getUserName() { return username; }
	
	public void connect() throws XMPPException {
	    ConnectionConfiguration config = new ConnectionConfiguration("chat.facebook.com", 5222);
	    config.setSASLAuthenticationEnabled(true);
	    
	    connection = new XMPPConnection(config);
	    SASLAuthentication.registerSASLMechanism("X-FACEBOOK-PLATFORM", SASLXFacebookPlatformMechanism.class);
	    SASLAuthentication.supportSASLMechanism("X-FACEBOOK-PLATFORM", 0);
	    
	    connection.connect();
	    connection.login(apiKey, accessToken);
	    
		chatmanager = connection.getChatManager();
		chatmanager.addChatListener(
				new ChatManagerListener() {
					@Override
					public void chatCreated(Chat chat, boolean createdLocally) {
						String jid = chat.getParticipant();
						
						if (!createdLocally) {
							if (listenerTable.containsKey(jid))
								chat.addMessageListener(listenerTable.get(jid));
							else {
								MyMessageListener listener = new MyMessageListener(myself);
								chat.addMessageListener(listener);
								listenerTable.put(jid, listener);
							}
						}
						
						conversation = chat;
						String name = getName(jid);
						tab.clearText();
						tab.printSystemMsg("<\u7cfb\u7d71\u8a0a\u606f> \u5df2\u958b\u555f\u8207" + name + "\u7684\u5c0d\u8a71\u3002\n");
		        	    tab.textChat.setEditable(true);
		            	tab.textChat.requestFocus();
					}
				});
		
		roster = connection.getRoster();
		Collection<RosterEntry> entries = roster.getEntries();
		for (RosterEntry entry : entries) {
			if (roster.getPresence(entry.getUser()).isAvailable())
				onlineList.put(entry.getUser(), new FBUser(entry));
			else
				offlineList.put(entry.getUser(), new FBUser(entry));
		}
		updateUserList();
        
		roster.addRosterListener(rosterListener);
		
		tab.logged_in = true;
	}
	
	public void updateUserList() {
		ArrayList<FBUser> arr = new ArrayList<FBUser>(onlineList.values());
        Collections.sort(arr);
        tab.onlineList.setListData(arr.toArray());
        arr = new ArrayList<FBUser>(offlineList.values());
        Collections.sort(arr);
        tab.offlineList.setListData(arr.toArray());
	}
	
	public void openConversation(String jid) {
		if (listenerTable.containsKey(jid))
			conversation = chatmanager.createChat(jid, listenerTable.get(jid));
		else {
			MyMessageListener listener = new MyMessageListener(myself);
			conversation = chatmanager.createChat(jid, listener);
			listenerTable.put(jid, listener);
		}
	}
	public Chat getConversation() { return conversation; }
	
	public String getName(String id) {
		return onlineList.containsKey(id)? onlineList.get(id).getName(): offlineList.get(id).getName();
	}
}

class FBUser implements Comparable<Object> {
	private String id;
	private String name;
	
	@Override
	public String toString() {
		return "FBUser [id=" + id + ", name=" + name + "]";
	}

	public FBUser(RosterEntry entry) {
		id = entry.getUser();
		name = entry.getName();
	}
	
	public String getId() { return id; }
	public String getName() { return name; }

	@Override
	public int compareTo(Object other) {
		return name.compareTo(((FBUser)other).getName());
	}
}

class MyMessageListener implements MessageListener {
	private FBChatClient master;
	
	public MyMessageListener(FBChatClient m) {
		this.master = m;
	}
	
	public void processMessage(Chat chat, Message message) {
		System.out.println("Get FBmsg: " + message.getBody());
		String id = chat.getParticipant(), name = master.getName(id);
		if (message.getBody() != null)
			master.tab.printText(message.getBody(), name);
		else
			master.tab.printInputMsg(name);
    }
}
