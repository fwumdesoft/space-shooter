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
			
		} else {
			try {
				socket = new DatagramSocket();
				
				//set socket options
				socket.setReceiveBufferSize(10);
				socket.setSendBufferSize(10);
				socket.setSoTimeout(5000);
				
				InetAddress remoteHost = InetAddress.getByName(HOST);
				socket.connect(remoteHost, PORT);
				connected = true;
				
				packet = new DatagramPacket(new byte[1024], 1024, remoteHost, PORT);
				byte[] data = receive();
			} catch(SocketException e) {
				Gdx.app.error("ServerInterface.connect()", "Failed to connect to server", e);
				connected = false;
			} catch(UnknownHostException e) {
				Gdx.app.error("ServerInterface.connect()", "Unknown host address", e);
				connected = false;
			}
		}
		return connected;
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
			} catch(IOException e) {
				Gdx.app.log("ServerInterace.receive(int)", "Skipped a packet. Trying again, attempt " + (i+1), e);
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
	
	public static void dispose() {
		socket.close();
		socket = null;
		connected = false;
	}
}
