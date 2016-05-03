package com.fwumdesoft.shoot.net.server;

import static com.fwumdesoft.shoot.net.NetConstants.*;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

public class Server extends ApplicationAdapter {
	public static FileHandle logFile;
	
	private Thread heartbeatThread, ioThread, simulationThread;
	private DatagramSocket socket;
	
	private HashMap<UUID, Client> clients;
	
	@Override
	public void create() {
		//setup log file
		logFile = Gdx.files.local("log");
		logFile.delete();

		try {
			socket = new DatagramSocket(SERVER_ADDR);
			socket.setReceiveBufferSize(PACKET_LENGTH);
			socket.setSendBufferSize(PACKET_LENGTH);
		} catch(SocketException e) {
			logFile.writeString("Failed to create a DatagramSocket. exiting app...\n", true);
			Gdx.app.exit();
		}
		clients = new HashMap<>();
		
		//handles incoming messages
		ioThread = new Thread(() -> {
			DatagramPacket packet = new DatagramPacket(new byte[PACKET_LENGTH], PACKET_LENGTH);
			ByteBuffer buffer = ByteBuffer.wrap(packet.getData());
			while(!Thread.interrupted()) {
				try {
					packet.setLength(PACKET_LENGTH);
					socket.receive(packet);
					buffer.rewind();
					@SuppressWarnings("unused")
					final int dataLength = buffer.getInt();
					final byte msgId = buffer.get();
					final UUID senderId = new UUID(buffer.getLong(), buffer.getLong());
					@SuppressWarnings("unused")
					final ByteBuffer data = buffer;
					
//					logFile.writeString("Received a packet from ID: " + senderId + " with message ID: " + msgId + " with dataLength of " + dataLength + "\n", true);
					
					switch(msgId) {
					case MSG_CONNECT:
						//make sure client doesn't already exist
						if(clients.containsKey(senderId)) {
							logFile.writeString("Client with duplicate ID tried to connect ID: " + senderId + "\n", true);
							break;
						}
						
						Client newClient = new Client(senderId, packet.getSocketAddress());
						clients.put(senderId, newClient);
						//respond to new Client with a MSG_CONNECT_HANDSHAKE
						buffer.rewind();
						buffer.putInt(0);
						buffer.put(MSG_CONNECT_HANDSHAKE);
						buffer.putLong(senderId.getMostSignificantBits());
						buffer.putLong(senderId.getLeastSignificantBits());
						packet.setLength(HEADER_LENGTH);
						newClient.send(socket, packet);
						
						//send connection info to all clients
						//send all clients currently connected, to the sender 
						for(Entry<UUID, Client> entry : clients.entrySet()) {
							if(!entry.getKey().equals(senderId)) {
								//construct message for all clients
								buffer.rewind();
								buffer.putInt(0);
								buffer.put(MSG_CONNECT);
								buffer.putLong(senderId.getMostSignificantBits());
								buffer.putLong(senderId.getLeastSignificantBits());
								packet.setLength(HEADER_LENGTH);
								entry.getValue().send(socket, packet);
								
								//construct message for the newClient
								buffer.rewind();
								buffer.putInt(0);
								buffer.put(MSG_CONNECT);
								buffer.putLong(entry.getKey().getMostSignificantBits());
								buffer.putLong(entry.getKey().getLeastSignificantBits());
								packet.setLength(HEADER_LENGTH);
								newClient.send(socket, packet);
							}
						}
						
						logFile.writeString("Added a new client ID: " + senderId + "\n", true);
						break;
					case MSG_DISCONNECT:
						//check if the client exists
						if(!clients.containsKey(senderId)) {
							logFile.writeString("Client with ID: " + senderId + " tried to disconnect but is already disconnected\n", true);
							break;
						}
						
						clients.remove(senderId);
						
						//construct message
						buffer.rewind();
						buffer.putInt(0);
						buffer.put(MSG_DISCONNECT);
						buffer.putLong(senderId.getMostSignificantBits());
						buffer.putLong(senderId.getLeastSignificantBits());
						packet.setLength(HEADER_LENGTH);
						
						//send message to all clients
						for(Entry<UUID, Client> entry : clients.entrySet()) {
							if(!entry.getKey().equals(senderId)) {
								clients.get(entry.getKey()).send(socket, packet);
							}
						}
						
						logFile.writeString("Disconnected a client ID: " + senderId + "\n", true);
						break;
					case MSG_HEARTBEAT:
						//make sure the client exists
						if(!clients.containsKey(senderId)) {
							logFile.writeString("Client with ID: " + senderId + " tried to send a heartbeat before connecting\n", true);
							break;
						}
						
						clients.get(senderId).timeSinceLastHeartbeat = 0L;
						break;
					case MSG_UPDATE_PLAYER:
						//make sure the client exists
						if(!clients.containsKey(senderId)) {
							logFile.writeString("Client with ID: " + senderId + " tried to update its position before connecting\n", true);
							break;
						}
						
						//no need to reconstruct the update player packet
						
						//tell all clients of the sender's new position
						for(Entry<UUID, Client> entry : clients.entrySet()) {
							if(!entry.getKey().equals(senderId)) {
								entry.getValue().send(socket, packet);
							}
						}
						break;
					}
				} catch(Exception e) {
					logFile.writeString("Failed to receive a packet\n", true);
				}
			}
		}, "io_thread");
		
		//simulates movement of server authoritative entities
		simulationThread = new Thread(() -> {
			
		}, "simulation_thread");
		
		//keeps track of every clients heart beat to ensure it is still connected
		heartbeatThread = new Thread(() -> {
			final DatagramPacket packet = new DatagramPacket(new byte[PACKET_LENGTH], PACKET_LENGTH);
			final ByteBuffer buffer = ByteBuffer.wrap(packet.getData());
			
			long time = System.currentTimeMillis();
			while(!Thread.interrupted()) {
				long deltaTime = System.currentTimeMillis() - time;
				time = System.currentTimeMillis();
				
				//check each clients heartbeat
				Array<UUID> removedClients = null;
				for(Entry<UUID, Client> timeEntry : clients.entrySet()) {
					timeEntry.getValue().timeSinceLastHeartbeat += deltaTime;
					if(timeEntry.getValue().timeSinceLastHeartbeat > HEARTBEAT_TIMEOUT) { //boot a client if the have no heart beat
						logFile.writeString("Dropping client " + timeEntry.getKey() + " due to lack of heartbeat\n", true);
						
						removedClients = removedClients == null ? new Array<>() : removedClients;
						removedClients.add(timeEntry.getKey());
						
						//send a disconnect to all clients to notify them of the disconnected player
						for(Entry<UUID, Client> clientEntry : clients.entrySet()) {
							//construct disconnect message for all clients
							buffer.rewind();
							buffer.putInt(0);
							buffer.put(MSG_DISCONNECT);
							buffer.putLong(timeEntry.getKey().getMostSignificantBits());
							buffer.putLong(timeEntry.getKey().getLeastSignificantBits());
							packet.setLength(HEADER_LENGTH);
							clientEntry.getValue().send(socket, packet);
						}
					}
				}
				
				//avoid ConcurrentModificationException by removing clients outside of the loop
				if(removedClients != null) {
					for(UUID id : removedClients) {
						clients.remove(id);
					}
				}
			}
		}, "heartbeat_thread");
		
		//start threads
		ioThread.start();
		simulationThread.start();
		heartbeatThread.start();
	}

	@Override
	public void dispose() {
		logFile.writeString("Disposing...\n", true);
		
		heartbeatThread.interrupt();
		simulationThread.interrupt();
		ioThread.interrupt();
		
		//wait for each thread to finish
		try {
			heartbeatThread.join();
			simulationThread.join();
			ioThread.join();
		} catch(InterruptedException e) {}
		
		logFile.writeString("Server disposed\n", true);
	}
}
