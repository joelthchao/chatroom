import java.awt.FileDialog;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JOptionPane;

public class Receiver implements Runnable {
	private ServerSocket servsock;
	private FileDialog fileDialog;
	private Listener listener;
	private String transmitter;
	
	public Receiver(String n, FileDialog fd, Listener l) throws IOException {
		/* [File transfer protocol]
		 * 1. transmitter->server: (IPRequest)username
		 * 2. server->transmitter: (IPReply)IpOfReceiver
		 * 3. transmitter->server: (FileRequest)username
		 * 4. server->receiver: (FileRequest)
		 * 5. transmitter->receiver: (FileInfo)filename%fileSize
		 * 6. transmitter->receiver: file content
		 */
		servsock = new ServerSocket(25535);
		fileDialog = fd;
		listener = l;
		transmitter = n;
	}
	
	public void run() {
		String filePath = "", fileName = "";
		
		try {
			// after server->receiver: (FileRequest)
			Socket socket = servsock.accept();
			DataInputStream inStream = new DataInputStream(socket.getInputStream());
			DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
			outStream.flush();
			
			// after connection is opened, transmitter should then send "(FileInfo)filename%fileSize" to receiver
			String fileInfo = inStream.readUTF();
			int fileSize;
			if (fileInfo.startsWith("(FileInfo)")) {
				fileName = fileInfo.substring(fileInfo.indexOf(')') + 1, fileInfo.indexOf('%'));
				fileSize = Integer.parseInt(fileInfo.substring(fileInfo.indexOf('%') + 1));
			}
			else {
				servsock.close();
				throw new IOException("FileInfo error!");
			}
			
			int decision = JOptionPane.showConfirmDialog(listener.cwc.frmLabChatroom,
					                                     transmitter + " \u50b3\u9001\u6a94\u6848 [" + fileName + "] \u7d66\u4f60\n\u662f\u5426\u63a5\u53d7\u795d\u798f\uff1f(y/n)",
					                                     "Invitation", JOptionPane.YES_NO_OPTION);
			if (decision == JOptionPane.YES_OPTION) {
				fileDialog.setVisible(true); // choose file location via fileDialog
				filePath = fileDialog.getDirectory();
				fileName = fileDialog.getFile();

				if (fileName == null) {
					listener.printText(listener.cwc.getRoomIdOnFocus(), "<\u7cfb\u7d71\u8a0a\u606f> \u5df2\u62d2\u7d55\u6a94\u6848\u3002\n", "SystemMessage");
				}
				else {
					outStream.writeUTF("(FileACK)");
					outStream.flush();
					receiveFile(filePath + fileName, fileSize, inStream); // start listening for file content
					listener.printText(listener.cwc.getRoomIdOnFocus(), "<\u7cfb\u7d71\u8a0a\u606f> \u5df2\u6536\u5230\u6a94\u6848 [" + filePath + fileName + "]\n", "SystemMessage");
				}
			}
			
			socket.close();
			servsock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void receiveFile(String fileName, int fileSize, DataInputStream inputStream) throws IOException {
		byte[] fileContent = new byte[200000];
	    FileOutputStream fos = new FileOutputStream(fileName);
	    BufferedOutputStream bos = new BufferedOutputStream(fos);
		int bytesRead = inputStream.read(fileContent);
		
		while (bytesRead >= 0) {
			bos.write(fileContent, 0, bytesRead);
			bytesRead = inputStream.read(fileContent);
		}
	    
	    bos.close();
	}
}
