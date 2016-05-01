package com.fwumdesoft.shoot.net;

import static com.fwumdesoft.shoot.net.NetConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.UUID;

import com.badlogic.gdx.Gdx;

/**
 * Provides a means for the client to communicate with the server.
 */
public class ServerInterface {
	private static UUID clientId;
	
	private static DatagramSocket socket;
	/**
	 * Packet with information to be sent to the server.
	 */
	private static DatagramPacket sndPacket;
	/**
	 * Packet with information received from the server.
	 */
	private static DatagramPacket rcvPacket;
	private static ByteBuffer sndBuffer;
	
	static {
		try {
			socket = new DatagramSocket();
			socket.setReceiveBufferSize(PACKET_SIZE);
			socket.setSendBufferSize(PACKET_SIZE);
			clientId = UUID.randomUUID();
			rcvPacket = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
			sndPacket = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE, SERVER_ADDR);
			sndBuffer = ByteBuffer.wrap(sndPacket.getData());
			Gdx.app.log("ServerInterface", "Client ID: " + clientId);
		} catch(SocketException e) {
			Gdx.app.log("ServerInterface", "Socket could not be created");
		}
	}
	
	/**
	 * Sends a {@code NetConstants.MSG_CONNECT} packet to the server.
	 * <p><b>Precondition:</b> Client isn't connected to the server.
	 */
	public static void connect() {
		if(isConnected()) throw new IllegalStateException("Client is already connected to the server");
		try {
			socket.connect(SERVER_ADDR);
		} catch(SocketException e) {
			Gdx.app.log("ServerInterface", "Socket could not connect to the server");
			throw new IllegalStateException("Failed to establish a connection to the server");
		}
		
		//send a MSG_CONNECT to the server
		synchronized(sndPacket.getData()) {
			sndBuffer.rewind();
			sndBuffer.putInt(0);
			sndBuffer.put(MSG_CONNECT);
			sndBuffer.putLong(clientId.getMostSignificantBits());
			sndBuffer.putLong(clientId.getLeastSignificantBits());
			sndPacket.setLength(HEADER_LENGTH);
			send(MSG_CONNECT);
		}
		
		Gdx.app.log("ServerInterface", "Sent a MSG_CONNECT packet");
	}
	
	/**
	 * Sends a {@code NetConstants.MSG_DISCONNECT} packet to the server.
	 * <p><b>Precondition:</b> Client is connected to the server.
	 */
	public static void disconnect() {
		if(!isConnected()) throw new IllegalStateException("Client isn't connected to the server");
		
		//send a MSG_DISCONNECT to the server
		synchronized(sndPacket.getData()) {
			sndBuffer.rewind();
			sndBuffer.putInt(0);
			sndBuffer.put(MSG_DISCONNECT);
			sndBuffer.putLong(clientId.getMostSignificantBits());
			sndBuffer.putLong(clientId.getLeastSignificantBits());
			sndPacket.setLength(HEADER_LENGTH);
			send(MSG_DISCONNECT);
		}
		
		Gdx.app.log("ServerInterface", "Sent a MSG_DISCONNECT packet");
		socket.disconnect();
	}
	
	/**
	 * Sends a {@link NetConstants#MSG_HEARTBEAT} packet to the server.
	 * <p><b>Precondition:</b> Client is connected to the server.
	 */
	public static void heartbeat() {
		if(!isConnected()) throw new IllegalStateException("Client isn't connected to the server");
		
		//send a MSG_HEARTBEAT to the server
		synchronized(sndPacket.getData()) {
			sndBuffer.rewind();
			sndBuffer.putInt(0);
			sndBuffer.put(MSG_HEARTBEAT);
			sndBuffer.putLong(clientId.getMostSignificantBits());
			sndBuffer.putLong(clientId.getLeastSignificantBits());
			sndPacket.setLength(HEADER_LENGTH);
			send(MSG_HEARTBEAT);
		}
		
		Gdx.app.log("ServerInterface", "Sent a MSG_HEARTBEAT packet");
	}
	
	/**
	 * Sends the packet to the server.
	 * <p><b>Precondition:</b> Client is connected to the server.
	 * @param msgId gives the method a hint to know what type of message it is sending.
	 * Useful for debugging.
	 */
	private static void send(byte msgId) {
		try {
			socket.send(sndPacket);
		} catch(IOException e) {
			Gdx.app.log("ServerInterface", "Failed to send a packet. id: " + msgId);
		}
	}
	
	/**
	 * Data in this ByteBuffer is read only. This method is blocking.
	 * @return The ByteBuffer of the data in the {@link #rcvPacket} or null is the message failed to be received.
	 */
	public static ByteBuffer getRcvData() {
		if(!isConnected()) throw new IllegalStateException("Client isn't connected to the server");
		try {
			socket.receive(rcvPacket);
		} catch(IOException e) {
			Gdx.app.log("ServerInterface", "Failed to receive a packet");
			return null;
		}
		return ByteBuffer.wrap(rcvPacket.getData()).asReadOnlyBuffer();
	}
	
	/**
	 * @return The connection state of the DatagramSocket.
	 * @see DatagramSocket#isConnected()
	 */
	public static boolean isConnected() {
		return socket != null && socket.isConnected();
	}
	
	public static UUID getClientId() {
		if(!isConnected()) throw new IllegalStateException("Client isn't connected to the server");
		return clientId;
	}
}
