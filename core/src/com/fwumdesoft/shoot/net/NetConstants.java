package com.fwumdesoft.shoot.net;

import java.net.InetSocketAddress;

/**
 * <b>Proper Packet Structure:</b>
 * <li>4 bytes: These bytes indicate how long the data section of the packet is.
 * <li>1 byte: This byte is a message id that identifies the type of message being sent.
 * <li>16 bytes: These bytes represent the id of the client sending the message.
 * <li>Remaining bytes: The remaining bytes have a length equivalent the first 4 bytes.
 * These bytes represent any data that is required by the message being sent.
 */
public class NetConstants {
	private NetConstants() {}
	
	/** Remote game server address. */
	public static final InetSocketAddress SERVER_ADDR = new InetSocketAddress("45.33.68.145", 5555);
	/** Time in milliseconds until a user is booted from the game server. */
	public static final long HEARTBEAT_TIMEOUT = 15000L;
	
	
	//Packet constants
	/** Max length in bytes that a packet can send to and from the server. */
	public static final int PACKET_LENGTH = 256;
	/** Length of the header of packets sent to and from the server. */
	public static final int HEADER_LENGTH = 21;
	
	
	//Message Ids
	/** Used for new player connections. */
	public static final byte MSG_CONNECT = (byte)0x00;
	/** Used to remove players. */
	public static final byte MSG_DISCONNECT = (byte)0x01;
	/** Used to tell the server that a client is still connected. */
	public static final byte MSG_HEARTBEAT = (byte)0x02;
	/**
	 * <b>MSG_UPDATE_PLAYER data section structure:</b>
	 * <li>4 bytes: The local player's x position.
	 * <li>4 bytes: The local player's y position.
	 */
	public static final byte MSG_UPDATE_PLAYER = (byte)0x03;
	/** Used to by the server to acknowledge a new connection. */
	public static final byte MSG_CONNECT_HANDSHAKE = (byte)0x04;
}
