package com.fwumdesoft.shoot.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.badlogic.gdx.Gdx;

/**
 * Allows clients to send/receive messages to the server.
 * <li>The first byte in each packet identifies the message.
 * <li>
 * <li>
 */
public class ServerInterface {
	private static final String HOST = "45.33.68.145";
	private static final int PORT = 5555;
	
	private static boolean connected;
	private static DatagramSocket socket;
	private static DatagramPacket packet;
	
	/**
	 * Connects to the server. If the client is already connected, then the client will<br>
	 * disconnect and then attempt to reconnect.
	 * @return <tt>true</tt> if the client is connected with the server, otherwise <tt>false</tt>.
	 * @postcondition <tt>connected</tt> will be set to an appropriate value.
	 */
	public static boolean connect() {
		if(connected) {
			disconnect();
			connect();
		} else {
			try {
				socket = new DatagramSocket();
				
				//set socket options
				socket.setReceiveBufferSize(256);
				socket.setSendBufferSize(256);
				socket.setSoTimeout(5000);
				
				InetAddress remoteHost = InetAddress.getByName(HOST);
				socket.connect(remoteHost, PORT);
				connected = true;
				
				packet = new DatagramPacket(new byte[256], 256, remoteHost, PORT);
				byte[] data = receive();
				Gdx.app.log("Connect Test", Byte.toString(data[0]));
			} catch(SocketException e) {
				Gdx.app.error("ServerInterface.connect()", "Failed to connect to instantiate DatagramSocket", e);
				connected = false;
			} catch(UnknownHostException e) {
				Gdx.app.error("ServerInterface.connect()", "Unknown host address", e);
				connected = false;
			}
		}
		return connected;
	}
	
	/**
	 * Sends data to the server.
	 * @param buf data buffer
	 * @return <tt>true</tt> if the packet sent successfully, otherwise <tt>false</tt>.
	 */
	public static boolean send(byte[] buf) {
		if(!connected) throw new IllegalStateException("Not connected to the server");
		packet.setData(buf);
		try {
			socket.send(packet);
			return true;
		} catch (IOException e) {
			Gdx.app.log("ServerInterface.send(byte[])", "Failed to send a packet", e);
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
		return null;
	}
	
	public static byte[] receive() {
		return receive(5);
	}
	
	public static boolean isConnected() {
		return connected;
	}
	
	public static void disconnect() {
		if(!connected) return;
		socket.disconnect();
		socket = null;
		packet = null;
		connected = false;
	}
}
