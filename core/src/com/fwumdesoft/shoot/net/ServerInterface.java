package com.fwumdesoft.shoot.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.badlogic.gdx.Gdx;
import com.fwumdesoft.shoot.server.Server;

/**
 * Allows clients to send/receive messages to the server.
 * <li>The first byte in each packet data array identifies the message.
 * <li>The second byte is the netID of the sender.
 * <li>The remainder of the packet data array is the data.
 */
public class ServerInterface {
	private static final String HOST = "45.33.68.145";
	
	private static byte netID = -1;
	private static boolean connected;
	private static DatagramSocket socket;
	private static DatagramPacket packet;
	
	/**
	 * Connects to the server. If the client is already connected, then the client will<br>
	 * disconnect and then attempt to reconnect.
	 * <p><b>Postcondition:</b> <tt>connected</tt> will be set to an appropriate value.
	 * @return <tt>true</tt> if the client is connected with the server, otherwise <tt>false</tt>.
	 */
	public static void connect() {
		if(connected) throw new IllegalStateException("Client is already connected to the server");
		
		try {
			socket = new DatagramSocket();
			
			//set socket options
			socket.setReceiveBufferSize(256);
			socket.setSendBufferSize(256);
			socket.setSoTimeout(2000);
			
			InetAddress remoteHost = InetAddress.getByName(HOST);
			socket.connect(remoteHost, Server.PORT);
			connected = true;
			
			packet = new DatagramPacket(new byte[256], 256, remoteHost, Server.PORT);
			boolean success = send(NetMessage.CONNECT, new byte[] {});
			if(!success) {
				disconnect();
			}
			
			byte[] data = receive();
			if(data == null) {
				disconnect();
			}
			
			if(data[0] == NetMessage.ID_REPLY)
				netID = data[2];
			else {
				Gdx.app.error("ServerInterface.connect()", "Never received a netID");
				disconnect();
			}
		} catch(SocketException e) {
			Gdx.app.error("ServerInterface.connect()", "Failed to connect to instantiate DatagramSocket", e);
			disconnect();
		} catch(UnknownHostException e) {
			Gdx.app.error("ServerInterface.connect()", "Unknown host address", e);
			disconnect();
		}
	}
	
	/**
	 * Sends data to the server.
	 * <p><b>Postcondition:</b> The packet sent must conform to the specifications described 
	 * in the ServerInterface javadoc.
	 * @param buf data buffer
	 * @param netmsg type of message
	 * @return <tt>true</tt> if the packet sent successfully, otherwise <tt>false</tt>.
	 */
	public static boolean send(byte netmsg, byte[] buf) {
		if(!connected) throw new IllegalStateException("Not connected to the server");
		
		//construct message
		byte[] data = new byte[buf.length+2];
		data[0] = netmsg;
		data[1] = netID;
		System.arraycopy(buf, 0, data, 2, buf.length);
		packet.setData(data);
		
		try {
			socket.send(packet);
			return true;
		} catch (IOException e) {
			Gdx.app.log("ServerInterface.send(byte, byte[])", "Failed to send a {type:"+netmsg+", id:"+netID+"}", e);
			return false;
		}
	}
	
	/**
	 * Attempts to receive data from the server only if the client is connected.
	 * @param tries
	 * @return The data received from the server or <tt>null</tt> if no data is received.
	 * @throws IllegalStateException The client isn't connected to the server yet.
	 */
	public static byte[] receive(int tries) {
		if(!connected) throw new IllegalStateException("Not connected to the server");
		for(int i = 0; i < tries; i++) {
			try {
				socket.receive(packet);
				byte[] buf = new byte[packet.getLength()];
				System.arraycopy(packet.getData(), packet.getOffset(), buf, 0, packet.getLength());
				return buf;
			} catch(SocketException e) {
				Gdx.app.log("ServerInterface.receive(int)", "Most likely caused by a call to disconnect", e);
				break;
			} catch(IOException e) {
				Gdx.app.log("ServerInterface.receive(int)", "Skipped a packet. Trying again, attempt " + (i+1), e);
			}
		}
		Gdx.app.log("ServerInterface.receive(int)", "Failed to receive a message");
		return null;
	}
	
	public static byte[] receive() {
		return receive(5);
	}
	
	public static boolean isConnected() {
		return connected;
	}
	
	/**
	 * <p>Disconnects this clients from the server.
	 * <p><b>Postcondition:</b> <tt>connected</tt> is set to false.
	 */
	public static void disconnect() {
		if(socket != null) {
			send(NetMessage.DISCONNECT, new byte[] {});
			socket.disconnect();
		}
		netID = -1;
		socket = null;
		packet = null;
		connected = false;
	}
}
