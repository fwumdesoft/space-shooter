package com.fwumdesoft.shoot.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Queue;

/**
 * Allows clients to send/receive messages to the server.
 * <li>The first 4 bytes in each packet data array gives the length of the data section.
 * <li>The next byte in each packet data array identifies the message.
 * <li>The next 16 bytes are the netID of the sender.
 * <li>The remainder of the packet data array is the data.
 */
public class ServerInterface {
	private static final String HOST = "45.33.68.145";
	private static final int BUFFER_SIZE = 256;
	
	private static UUID myNetId = null;
	private static boolean connected;
	private static DatagramSocket socket;
	private static DatagramPacket packet;
	
	private static Queue<ByteBuffer> msgQueue;
	private static Thread sendThread;
	
	/**
	 * Connects to the server. If the client is already connected, then the client will<br>
	 * disconnect and then attempt to reconnect.
	 * <p><b>Postcondition:</b> <tt>connected</tt> will be set to an appropriate value.
	 * @return <tt>true</tt> if the client is connected with the server, otherwise <tt>false</tt>.
	 */
	public static void connect() {
		if(connected) throw new IllegalStateException("Client is already connected to the server");
		sendThread = new Thread(() -> {
			msgQueue = new Queue<>();
			while(!Thread.currentThread().isInterrupted()) {
				if(msgQueue.size == 0) continue;
				ByteBuffer buffer = msgQueue.removeFirst();
				packet.setData(buffer.array());
				try {
					socket.send(packet);
					Gdx.app.log("SEND", "Sent a packet");
				} catch(IOException e) {
					Gdx.app.log("SEND", "Failed to send a message");
				}
				Thread.yield();
			}
		}, "free_thread");
		sendThread.start();
		
		try {
			socket = new DatagramSocket();
			
			//set socket options
			socket.setReceiveBufferSize(BUFFER_SIZE);
			socket.setSendBufferSize(BUFFER_SIZE);
			socket.setSoTimeout(1000);
			
			InetAddress remoteHost = InetAddress.getByName(HOST);
			socket.connect(remoteHost, Server.PORT);
			connected = true;
			myNetId = UUID.randomUUID();
			packet = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE, remoteHost, Server.PORT);
			
			send(NetMessage.CONNECT, null);
			Gdx.app.log("ServerInterface.connect()", "Connection successful");
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
	 * <p><b>Precondition:</b> <tt>data</tt> is ready to be read from and is perfectly sized or null.
	 * <p><b>Postcondition:</b> The packet sent must conform to the specifications described 
	 * in the ServerInterface javadoc.
	 * @param buf data buffer
	 * @param netmsg type of message
	 * @return <tt>true</tt> if the packet sent successfully, otherwise <tt>false</tt>.
	 */
	public static void send(byte netmsg, ByteBuffer data) {
		if(!connected) throw new IllegalStateException("Not connected to the server");
		
		//construct message
		ByteBuffer buffer = ByteBuffer.allocate(21 + (data == null ? 0 : data.limit()));
		buffer.putInt(data == null ? 0 : data.limit()); //length of data section
		buffer.put(netmsg); //identifier
		buffer.putLong(myNetId.getMostSignificantBits()); //UUID
		buffer.putLong(myNetId.getLeastSignificantBits()); //UUID
		if(data != null)
			buffer.put(data); //Optional Data
		buffer.flip();
		msgQueue.addLast(buffer);
	}
	
	/**
	 * Attempts to receive data from the server only if the client is connected.
	 * @param tries
	 * @return The data received from the server or <tt>null</tt> if no data is received.
	 * @throws IllegalStateException The client isn't connected to the server yet.
	 */
	public static synchronized ByteBuffer receive(int tries) {
		if(!connected) throw new IllegalStateException("Not connected to the server");
		
		for(int i = 0; i < tries; i++) {
			try {
				socket.receive(packet);
				ByteBuffer buf = ByteBuffer.allocate(packet.getLength());
				buf.put(packet.getData(), packet.getOffset(), packet.getLength());
				buf.flip();
				return buf;
			} catch(SocketException e) {
				Gdx.app.log("ServerInterface.receive(int)", "Most likely caused by a call to disconnect", e);
				break;
			} catch(IOException e) {
				Gdx.app.log("ServerInterface.receive(int)", "Skipped a packet. Trying again, attempt " + (i+1));
			}
		}
		Gdx.app.log("ServerInterface.receive(int)", "Failed to receive a message");
		return null;
	}
	
	public static boolean isConnected() {
		return connected;
	}
	
	public static UUID getMyID() {
		return myNetId;
	}
	
	/**
	 * <p>Disconnects this clients from the server.
	 * <p><b>Postcondition:</b> <tt>connected</tt> is set to false.
	 */
	public static void disconnect() {
		if(socket != null) {
			send(NetMessage.DISCONNECT, null);
			socket.disconnect();
		}
		if(sendThread != null) {
			sendThread.interrupt();
			try {
				sendThread.join();
			} catch(InterruptedException e) {
				sendThread.interrupt();
			}
		}
		sendThread = null;
		myNetId = null;
		socket = null;
		packet = null;
		connected = false;
	}
}
