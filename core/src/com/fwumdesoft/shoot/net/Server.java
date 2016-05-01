package com.fwumdesoft.shoot.net;

import static com.fwumdesoft.shoot.net.NetConstants.*;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

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
			socket.setReceiveBufferSize(PACKET_SIZE);
			socket.setSendBufferSize(PACKET_SIZE);
		} catch(SocketException e) {
			logFile.writeString("Failed to create a DatagramSocket. exiting app...\n", true);
			Gdx.app.exit();
		}
		clients = new HashMap<>();
		
		//handles incoming messages
		ioThread = new Thread(() -> {
			DatagramPacket packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
			ByteBuffer buffer = ByteBuffer.wrap(packet.getData());
			while(!Thread.currentThread().isInterrupted()) {
				try {
					socket.receive(packet);
					buffer.rewind();
					final int dataLength = buffer.getInt();
					final byte msgId = buffer.get();
					final UUID senderId = new UUID(buffer.getLong(), buffer.getLong());
					final ByteBuffer data = ByteBuffer.wrap(packet.getData(), HEADER_LENGTH, dataLength);
					
					logFile.writeString("Received a packet from ID: " + senderId + " with message ID: " + msgId + " with dataLength of " + dataLength + "\n", true);
					
					switch(msgId) {
					case MSG_CONNECT:
						//make sure client doesn't already exist
						if(clients.containsKey(senderId)) {
							logFile.writeString("Client with duplicate ID tried to connect ID: " + senderId + "\n", true);
							break;
						}
						
						Client newClient = new Client(senderId, packet.getSocketAddress());
						clients.put(senderId, newClient);
						
						//send connection info to all clients
						//send all clients currently connected, to the sender 
						for(Entry<UUID, Client> entry : clients.entrySet()) {
							if(entry.getKey().equals(senderId)) {
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
