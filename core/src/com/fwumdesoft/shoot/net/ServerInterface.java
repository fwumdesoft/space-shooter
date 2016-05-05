package com.fwumdesoft.shoot.net;

import static com.fwumdesoft.shoot.net.NetConstants.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.UUID;
import com.badlogic.gdx.Gdx;
import com.fwumdesoft.shoot.model.Bolt;
import com.fwumdesoft.shoot.model.Player;

/**
 * Provides a means for the client to communicate with the server.
 */
public class ServerInterface {
	private static final UUID clientId;
	
	private static DatagramSocket socket;
	/** Packet with information to be sent to the server. */
	private static DatagramPacket sndPacket;
	/** Packet with information received from the server. */
	private static DatagramPacket rcvPacket;
	private static ByteBuffer sndBuffer;
	
	static {
		clientId = UUID.randomUUID();
		try {
			//create required data to establish a connection
			socket = new DatagramSocket();
			socket.setReceiveBufferSize(PACKET_LENGTH);
			socket.setSendBufferSize(PACKET_LENGTH);
			socket.setSoTimeout(1000);
			rcvPacket = new DatagramPacket(new byte[PACKET_LENGTH], PACKET_LENGTH);
			sndPacket = new DatagramPacket(new byte[PACKET_LENGTH], PACKET_LENGTH, SERVER_ADDR);
			sndBuffer = ByteBuffer.wrap(sndPacket.getData());
			Gdx.app.log("ServerInterface", "Client ID: " + clientId);
		} catch(SocketException e) {
			Gdx.app.error("ServerInterface", "Socket could not be created");
		}
	}
	
	/**
	 * Sends a {@code NetConstants.MSG_CONNECT} packet to the server.
	 * <p><b>Precondition:</b> Client isn't connected to the server.
	 */
	public static void connect() {
		if(isConnected())
			throw new IllegalStateException("Client is already connected to the server");
		try {
			socket.connect(SERVER_ADDR);
		} catch(SocketException e) {
			Gdx.app.error("ServerInterface", "Socket could not connect to the server");
			throw new IllegalStateException("Failed to establish a connection to the server");
		}
		
		//send a MSG_CONNECT to the server
		synchronized(sndPacket.getData()) {
			createHeader(0, MSG_CONNECT);
			send(MSG_CONNECT);
		}
		
		Gdx.app.log("ServerInterface", "Sent a MSG_CONNECT packet");
	}
	
	/**
	 * Sends a {@code NetConstants.MSG_DISCONNECT} packet to the server.
	 * <p><b>Precondition:</b> Client is connected to the server.
	 */
	public static void disconnect() {
		if(!isConnected())
			throw new IllegalStateException("Client isn't connected to the server");
		
		//send a MSG_DISCONNECT to the server
		synchronized(sndPacket.getData()) {
			createHeader(0, MSG_DISCONNECT);
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
		if(!isConnected())
			throw new IllegalStateException("Client isn't connected to the server");
		
		//send a MSG_HEARTBEAT to the server
		synchronized(sndPacket.getData()) {
			createHeader(0, MSG_HEARTBEAT);
			send(MSG_HEARTBEAT);
		}
		
		Gdx.app.debug("ServerInterface", "Sent a MSG_HEARTBEAT packet");
	}
	
	/**
	 * Sends a {@link NetConstants#MSG_UPDATE_PLAYER} packet to the server.
	 * <p>Sends the player location and rotation to the server so other players know
	 * the location and rotation of the localPlayer.
	 * <p><b>Precondition:</b> Client is connected to the server.
	 * @param localPlayer This computer's locally controlled player.
	 */
	public static void updateLocalPlayer(final Player localPlayer) {
		if(!isConnected())
			throw new IllegalStateException("Client isn't connected to the server");
		
		//Send a MSG_UPDATE_PLAYER to the server
		synchronized(sndPacket.getData()) {
			createHeader(12, MSG_UPDATE_PLAYER); //3 floats
			sndBuffer.putFloat(localPlayer.getX());
			sndBuffer.putFloat(localPlayer.getY());
			sndBuffer.putFloat(localPlayer.getRotation());
			send(MSG_UPDATE_PLAYER);
		}
		
		Gdx.app.debug("ServerInterface", "Sent a MSG_UPDATE_PLAYER packet");
	}
	
	/**
	 * Sends a {@link NetConstants#MSG_SPAWN_BOLT} packet to the server.
	 * <p>Tells the server that a Bolt has been fired by a player.
	 * <p><b>Precondition:</b> Client is connected to the server.
	 * @param Bolt Bolt that was spawned.
	 */
	public static void spawnBolt(Bolt bolt) {
		if(!isConnected())
			throw new IllegalStateException("Client isn't connected to the server");
		
		//Send a MSG_SPAWN_BOLT packet to the server
		synchronized(sndPacket.getData()) {
			createHeader(28, MSG_SPAWN_BOLT); //2 longs & 3 floats
			sndBuffer.putLong(bolt.getNetId().getMostSignificantBits());
			sndBuffer.putLong(bolt.getNetId().getLeastSignificantBits());
			sndBuffer.putFloat(bolt.getX());
			sndBuffer.putFloat(bolt.getY());
			sndBuffer.putFloat(bolt.getRotation());
			send(MSG_SPAWN_BOLT);
		}
		
		Gdx.app.debug("ServerInterface", "Sent a MSG_SPAWN_BOLT packet");
	}
	
	/**
	 * Sets up the header the packet to be sent to the server.
	 * <p><b>Postcondition:</b> The length of the packet will be set appropriately. 
	 * @param dataLength Length of the data chunk of the packet.
	 * @param msgId Id of the message being sent.
	 */
	private static void createHeader(int dataLength, byte msgId) {
		sndBuffer.rewind();
		sndBuffer.putInt(dataLength);
		sndBuffer.put(msgId);
		sndBuffer.putLong(clientId.getMostSignificantBits());
		sndBuffer.putLong(clientId.getLeastSignificantBits());
		sndPacket.setLength(HEADER_LENGTH + dataLength);
	}
	
	/**
	 * Sends the packet to the server.
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
	public static ByteBuffer receiveData() {
		if(!isConnected())
			throw new IllegalStateException("Client isn't connected to the server");
		try {
			rcvPacket.setLength(PACKET_LENGTH);
			socket.receive(rcvPacket);
		} catch(SocketTimeoutException e) {
			Gdx.app.debug("ServerInterface", "receiveData() timed out");
			return null;
		} catch(IOException e) {
			Gdx.app.error("ServerInterface", "Failed to receive a packet");
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
		return clientId;
	}
}
