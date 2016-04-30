package com.fwumdesoft.shoot.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class Server extends ApplicationAdapter {
	public static final int PORT = 5555;
	public static final int HEARTBEAT_TIMEOUT = 15000;
	private static final int BUFFER_SIZE = 256;
	
	FileHandle logFile;
	Thread serverThread, timeThread;
	DatagramSocket serverSocket = null;
	volatile HashMap<UUID, DatagramPacket> packets;
	volatile HashMap<UUID, Long> timeSinceHeartbeat;
	
	@Override
	public void create() {
		logFile = Gdx.files.local("log");
		logFile.delete();
		
		timeThread = new Thread(() -> {
			timeSinceHeartbeat = new HashMap<>();
			long prevTime = System.currentTimeMillis(), deltaTime = 0;
			while(!Thread.currentThread().isInterrupted()) {
				deltaTime = System.currentTimeMillis() - prevTime;
				prevTime = System.currentTimeMillis();
				
				ArrayList<UUID> removedIDs = null;
				for(Entry<UUID, Long> timeEntry : timeSinceHeartbeat.entrySet()) {
					timeEntry.setValue(timeEntry.getValue() + deltaTime);
					if(timeEntry.getValue() > HEARTBEAT_TIMEOUT) {
						if(removedIDs == null) removedIDs = new ArrayList<>();
						logFile.writeString("Removed player {"+timeEntry.getKey()+"} due to lack of heartbeat\n", true);
						
						for(Entry<UUID, DatagramPacket> packetEntry : packets.entrySet()) {
							send(NetMessage.DISCONNECT, timeEntry.getKey(), null, packetEntry.getKey());
						}
						packets.remove(timeEntry.getKey());
						removedIDs.add(timeEntry.getKey());
					}
				}
				
				//avoid ConcurrentModificationException
				if(removedIDs != null) {
					for(UUID id : removedIDs) {
						timeSinceHeartbeat.remove(id);
					}
				}
			}
		}, "time");
		timeThread.start();
		
		serverThread = new Thread(() -> {
			try {
				serverSocket = new DatagramSocket(PORT);
				//set socket options
				serverSocket.setReceiveBufferSize(BUFFER_SIZE);
				serverSocket.setSendBufferSize(BUFFER_SIZE);
			} catch(SocketException e) {
				logFile.writeString("Failed to instantiate the serverSocket\n" + e + "\n", true);
				Gdx.app.exit();
			}
			
			packets = new HashMap<>();
			DatagramPacket serverPacket = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
			while(!Thread.currentThread().isInterrupted()) {
				try {
					serverSocket.receive(serverPacket);
					ByteBuffer buffer = ByteBuffer.allocate(serverPacket.getLength());
					buffer.put(serverPacket.getData(), serverPacket.getOffset(), serverPacket.getLength());
					buffer.flip();
					
					final int dataLength = buffer.getInt(); //data length
					final byte netmsg = buffer.get(); //identifier
					final UUID senderId = new UUID(buffer.getLong(), buffer.getLong()); //sender id
					final ByteBuffer data = ByteBuffer.allocate(dataLength);
					data.put(serverPacket.getData(), 21, dataLength);
					
					switch(netmsg)
					{
					case NetMessage.CONNECT:
						DatagramPacket newPacket = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE, serverPacket.getAddress(), serverPacket.getPort());
						packets.put(senderId, newPacket);
						timeSinceHeartbeat.put(senderId, 0L);
						for(Entry<UUID, DatagramPacket> entry : packets.entrySet()) {
							if(!entry.getKey().equals(senderId)) {
								send(NetMessage.CONNECT, senderId, null, entry.getKey());
								send(NetMessage.CONNECT, entry.getKey(), null, senderId);
							}
						}
						logFile.writeString("Added new player {"+senderId+"}\n", true);
						break;
					case NetMessage.HEARTBEAT:
						timeSinceHeartbeat.put(senderId, 0L);
						break;
					case NetMessage.DISCONNECT:
						packets.remove(senderId);
						timeSinceHeartbeat.remove(senderId);
						logFile.writeString("Removed a player {"+senderId+"}\n", true);
					default:
						for(Entry<UUID, DatagramPacket> entry : packets.entrySet()) {
							if(!entry.getKey().equals(senderId)) {
								send(netmsg, senderId, data, entry.getKey());
							}
						}
					}
				} catch(IOException e) {
					logFile.writeString(e + "\n", true);
				}
			}
		}, "server");
		serverThread.start();
		try {
			serverThread.join();
			timeThread.join();
		} catch(InterruptedException e) {
			serverThread.interrupt();
			timeThread.interrupt();
		}
	}
	
	/**
	 * Sends a message.
	 * @param netmsg type of message.
	 * @param senderId who is sending the message.
	 * @param data
	 * @param recipientNetId who is receiving the message.
	 * @return <tt>true</tt> if the message was sent, otherwise <tt>false</tt>.
	 */
	private synchronized void send(byte netmsg, UUID senderId, ByteBuffer data, UUID recipientNetId) {
		//construct message
		ByteBuffer msgBuf = ByteBuffer.allocate((data == null ? 0 : data.limit()) + 21);
		msgBuf.putInt(data == null ? 0 : data.limit());
		msgBuf.put(netmsg);
		msgBuf.putLong(senderId.getMostSignificantBits());
		msgBuf.putLong(senderId.getLeastSignificantBits());
		if(data != null)
			msgBuf.put(data);
		msgBuf.flip();
		
		DatagramPacket recipient = packets.get(recipientNetId);
		recipient.setData(msgBuf.array());
		try {
			serverSocket.send(recipient);
		} catch(IOException e) {
			logFile.writeString("Failed to send a {type:"+netmsg+", id:"+senderId+"} message to "+recipientNetId+"\n", true);
		}
	}
	
	@Override
	public void dispose() {
		serverThread.interrupt();
		timeThread.interrupt();
		if(serverSocket != null)
			serverSocket.close();
	}
}
