package com.fwumdesoft.shoot.net.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.UUID;

/**
 * Represents a Client that the server can communicate with. Only the server may instantiate/use Clients.
 */
class Client {
	final UUID clientId;
	final SocketAddress address;
	long timeSinceLastHeartbeat;
	
	Client(final UUID id, final SocketAddress address) {
		clientId = id;
		this.address = address;
		timeSinceLastHeartbeat = 0L;
	}
	
	/**
	 * Sends a message to this client from the server.
	 * @param socket socket used to send the message
	 * @param msg
	 */
	void send(final DatagramSocket socket, final DatagramPacket packet) {
		synchronized(packet) {
			packet.setSocketAddress(address);
			try {
				socket.send(packet);
			} catch(IOException e) {
				Server.logFile.writeString("Failed to send a packet to ID: " + clientId + "\n", true);
			}
		}
	}
}
