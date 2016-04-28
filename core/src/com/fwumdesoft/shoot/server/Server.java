package com.fwumdesoft.shoot.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map.Entry;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.fwumdesoft.shoot.net.NetMessage;

public class Server extends ApplicationAdapter {
	public static final int PORT = 5555;
	public static final int HEARTBEAT_TIMEOUT = 15000;
	
	byte nextID = 1;
	FileHandle logFile;
	Thread serverThread, timeThread;
	DatagramSocket serverSocket = null;
	HashMap<Byte, DatagramPacket> packets;
	HashMap<Byte, Long> timeSinceHeartbeat;
	
	@Override
	public void create() {
		logFile = Gdx.files.local("log");
		logFile.delete();
		
		timeThread = new Thread(() -> {
			long prevTime = System.currentTimeMillis(), deltaTime = 0;
			while(!Thread.currentThread().isInterrupted()) {
				deltaTime = System.currentTimeMillis() - prevTime;
				prevTime = System.currentTimeMillis();
				
				for(Entry<Byte, Long> timeEntry : timeSinceHeartbeat.entrySet()) {
					timeEntry.setValue(timeEntry.getValue() + deltaTime);
					if(timeEntry.getValue() > HEARTBEAT_TIMEOUT) {
						for(Entry<Byte, DatagramPacket> packetEntry : packets.entrySet()) {
							
							send(NetMessage.DISCONNECT, timeEntry.getKey(), new byte[] {}, packetEntry.getKey());
						}
					}
				}
			}
		}, "time");
		timeThread.start();
		
		serverThread = new Thread(() -> {
			try {
				serverSocket = new DatagramSocket(PORT);
				//set socket options
				serverSocket.setReceiveBufferSize(256);
				serverSocket.setSendBufferSize(256);
			} catch(SocketException e) {
				logFile.writeString("Failed to instantiate the serverSocket\n" + e + "\n", true);
				Gdx.app.exit();
			}
			
			packets = new HashMap<>();
			DatagramPacket serverPacket = new DatagramPacket(new byte[256], 256);
			while(!Thread.interrupted()) {
				try {
					serverSocket.receive(serverPacket);
					if(serverPacket.getData()[1] == -1 && serverPacket.getData()[0] != NetMessage.CONNECT) {
						logFile.writeString("Lingering Client with no id is trying to do something other than CONNECT\n", true);
						continue;
					}
					
					switch(serverPacket.getData()[0])
					{
					case NetMessage.CONNECT:
						DatagramPacket newPacket = new DatagramPacket(new byte[256], 256, serverPacket.getAddress(), serverPacket.getPort());
						packets.put(nextID, newPacket);
						timeSinceHeartbeat.put(nextID, 0L);
						if(send(NetMessage.ID_REPLY, nextID, new byte[] {nextID}, nextID)) { //make sure the new client has received its ID
							for(Entry<Byte, DatagramPacket> entry : packets.entrySet()) {
								if(entry.getKey() != nextID-1) {
									send(NetMessage.CONNECT, nextID, new byte[] {}, entry.getKey());
								}
							}
							nextID++;
						} else {
							packets.remove(nextID);
							timeSinceHeartbeat.remove(nextID);
						}
						break;
					case NetMessage.HEARTBEAT:
						byte heartbeatID = serverPacket.getData()[1];
						timeSinceHeartbeat.get(heartbeatID);
						//TODO finish heartbeat impl
						break;
					case NetMessage.DISCONNECT:
						byte deletedID = serverPacket.getData()[1];
						packets.remove(deletedID);
					default:
						byte netmsg = serverPacket.getData()[0];
						byte id = serverPacket.getData()[1];
						byte[] data = new byte[serverPacket.getLength()-2];
						System.arraycopy(serverPacket.getData(), 2, data, 0, serverPacket.getLength());
						for(Entry<Byte, DatagramPacket> entry : packets.entrySet()) {
							if(entry.getKey() != id) {
								send(netmsg, id, data, entry.getKey());
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
	 * <p><b>precondition:</b> the recipientNetID must be in the packets HashMap.
	 * @param netmsg type of message.
	 * @param netID who is sending the message.
	 * @param buf data.
	 * @param recipientNetID who is receiving the message.
	 * @return <tt>true</tt> if the message was sent, otherwise <tt>false</tt>.
	 */
	private boolean send(byte netmsg, byte netID, byte[] buf, byte recipientNetID) {
		//construct message
		byte[] data = new byte[buf.length+2];
		data[0] = netmsg;
		data[1] = netID;
		System.arraycopy(buf, 0, data, 2, buf.length);
		
		DatagramPacket recipient = packets.get(recipientNetID);
		recipient.setData(data);
		try {
			serverSocket.send(recipient);
			return true;
		} catch(IOException e) {
			logFile.writeString("Failed to send a {type:"+netmsg+", id:"+netID+"} message to "+recipientNetID+"\n", true);
			return false;
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
