import java.awt.FileDialog;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JOptionPane;

public class Transmitter implements Runnable {
	private Socket socket;
	private FileDialog fileDialog;
	private Listener listener;
	
	public Transmitter(String ip, FileDialog fd, Listener l) throws IOException {
		/* [File transfer protocol]
		 * 1. transmitter->server: (IPRequest)username
		 * 2. server->transmitter: (IPReply)IpOfReceiver
		 * 3. transmitter->server: (FileRequest)username
		 * 4. server->receiver: (FileRequest)
		 * 5. transmitter->receiver: (FileInfo)filename%fileSize
		 * 6. transmitter->receiver: file content
		 */
		socket = new Socket(ip, 25535);
		fileDialog = fd;
		listener = l;
	}
	
	public void run() {
		String filePath = "", fileName = "";
		
		try {
			DataInputStream inStream = new DataInputStream(socket.getInputStream());
			DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
			outStream.flush();

			// transmitter->receiver: (FileInfo)filename%fileSize
			filePath = fileDialog.getDirectory();
			fileName = fileDialog.getFile();
			
			if (fileName.length() == 0) {
				listener.printText(listener.cwc.getRoomIdOnFocus(), "<\u7cfb\u7d71\u8a0a\u606f> \u53d6\u6d88\u50b3\u6a94\u3002\n", "SystemMessage");
			}
			else {
				long fileSize = new File(filePath + fileName).length();
				String fileInfo = "(FileInfo)" + fileName + "%" + fileSize;
				outStream.writeUTF(fileInfo);
				outStream.flush();
				
				// wait for ACK
				inStream.readUTF();

				// transmitter->receiver: file content
				transmitFile(filePath + fileName, fileSize, outStream);
				listener.printText(listener.cwc.getRoomIdOnFocus(), "<\u7cfb\u7d71\u8a0a\u606f> \u6a94\u6848 [" + filePath + fileName + "] \u50b3\u8f38\u6210\u529f\n", "SystemMessage");
			}
			
			socket.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(listener.cwc.frmLabChatroom, "\u88ab\u6253\u69cd\u60f9 \u310f\u310f", "lol", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	private void transmitFile(String fileName, long fileSize, DataOutputStream outStream) throws IOException {
	    File inFile = new File(fileName);
	    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inFile));
	    
		byte[] fileContent = new byte[150000];
		int bytesRead = bis.read(fileContent, 0, fileContent.length);
		
		while (bytesRead >= 0) {
			outStream.write(fileContent, 0, bytesRead);
			bytesRead = bis.read(fileContent, 0, fileContent.length);
		}
		
		bis.close();
	}
}
