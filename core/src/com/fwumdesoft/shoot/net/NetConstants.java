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
	
	public static final InetSocketAddress SERVER_ADDR = new InetSocketAddress("45.33.68.145", 5555);
	public static final long HEARTBEAT_TIMEOUT = 15000L;
	public static final int CLIENT_SOCKET_TIMEOUT = 1000;
	
	//Packet constants
	public static final int PACKET_SIZE = 256;
	public static final int HEADER_LENGTH = 21;
	
	//Message Ids
	public static final byte MSG_CONNECT = (byte)0x00;
	public static final byte MSG_DISCONNECT = (byte)0x01;
	public static final byte MSG_HEARTBEAT = (byte)0x02;
	public static final byte MSG_UPDATE_PLAYER = (byte)0x03;
}
