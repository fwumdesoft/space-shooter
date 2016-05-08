package com.fwumdesoft.shoot.net.server;

import static com.fwumdesoft.shoot.net.NetConstants.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.fwumdesoft.shoot.model.Bolt;
import com.fwumdesoft.shoot.model.NetActor;
import com.fwumdesoft.shoot.model.Player;

public class Server extends ApplicationAdapter {
	public static FileHandle logFile;
	
	private Thread heartbeatThread, ioThread;
	private DatagramSocket socket;
	
	private HashMap<UUID, Client> clients;
	private HeadlessStage stage;
	
	/** Only reference this packet from within the render loop! */
	private DatagramPacket renderPacket = new DatagramPacket(new byte[PACKET_LENGTH], PACKET_LENGTH);
	/** Only reference this buffer from within the render loop! */
	private ByteBuffer renderBuffer = ByteBuffer.wrap(renderPacket.getData());
	
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
			logFile.writeString("Failed to create a DatagramSocket. Exiting app...\n", true);
			Gdx.app.exit();
		}
		
		clients = new HashMap<>();
		stage = new HeadlessStage();
		
		//handles incoming messages
		//THE ONLY THREAD THAT CAN ADD OR REMOVE CLIENTS
		ioThread = new Thread(() -> {
			DatagramPacket packet = new DatagramPacket(new byte[PACKET_LENGTH], PACKET_LENGTH);
			ByteBuffer buffer = ByteBuffer.wrap(packet.getData());
			while(!Thread.interrupted()) {
				try {
					packet.setLength(PACKET_LENGTH);
					socket.receive(packet);
				} catch(Exception e) {
					logFile.writeString("Failed to receive a packet\n", true);
					continue;
				}
				
				//unpack buffer
				buffer.rewind();
				@SuppressWarnings("unused")
				final int dataLength = buffer.getInt();
				final byte msgId = buffer.get();
				final UUID senderId = new UUID(buffer.getLong(), buffer.getLong());
				final ByteBuffer data = buffer;
				
				switch(msgId) //choose what to do with the message id
				{
				case MSG_CONNECT:
					if(clients.containsKey(senderId)) {
						logFile.writeString("Client with duplicate ID tried to connect ID: " + senderId + "\n", true);
						break;
					}
					
					Client newClient = new Client(senderId, packet.getSocketAddress());
					synchronized(clients) {
						clients.put(senderId, newClient);
					}
					synchronized(stage) {
						stage.addActor(new Player(senderId));
					}
					
					//respond to new Client with a MSG_CONNECT_HANDSHAKE
					buffer.rewind();
					buffer.putInt(0);
					buffer.put(MSG_CONNECT_HANDSHAKE);
					buffer.putLong(senderId.getMostSignificantBits());
					buffer.putLong(senderId.getLeastSignificantBits());
					packet.setLength(HEADER_LENGTH);
					newClient.send(socket, packet);
					
					//Tell new client about all netActors in the game right now
					synchronized(stage) {
						for(Actor actor : stage.getActors()) {
							if(actor instanceof Bolt) {
								Bolt b = (Bolt)actor;
								buffer.rewind();
								buffer.putInt(32); //2 longs 4 floats
								buffer.put(MSG_SPAWN_BOLT);
								buffer.putLong(b.getShooterId().getMostSignificantBits());
								buffer.putLong(b.getShooterId().getLeastSignificantBits());
								buffer.putFloat(b.getX());
								buffer.putFloat(b.getY());
								buffer.putFloat(b.getRotation());
								buffer.putFloat(b.getSpeed());
								newClient.send(socket, packet);
							} else if(actor instanceof Player) {
								Player p = (Player)actor;
								if(p.getNetId().equals(senderId))
									continue;
								buffer.rewind();
								buffer.putInt(0);
								buffer.put(MSG_CONNECT);
								buffer.putLong(p.getNetId().getMostSignificantBits());
								buffer.putLong(p.getNetId().getLeastSignificantBits());
								packet.setLength(HEADER_LENGTH);
								newClient.send(socket, packet);
							}
						}
					}
					
					//tell other clients about the new connection
					for(Client c : clients.values()) {
						if(!c.clientId.equals(senderId)) {
							buffer.rewind();
							buffer.putInt(0);
							buffer.put(MSG_CONNECT);
							buffer.putLong(senderId.getMostSignificantBits());
							buffer.putLong(senderId.getLeastSignificantBits());
							packet.setLength(HEADER_LENGTH);
							c.send(socket, packet);
						}
					}
					
					logFile.writeString("Added a new client ID: " + senderId + "\n", true);
					break;
				case MSG_DISCONNECT:
					if(!clients.containsKey(senderId)) {
						logFile.writeString("Client with ID: " + senderId + " tried to disconnect but is already disconnected\n", true);
						break;
					}
					
					//remove Client from clients HashMap and stage
					synchronized(clients) {
						clients.remove(senderId);
					}
					synchronized(stage) {
						Iterator<Actor> iter = stage.getActors().iterator();
						while(iter.hasNext()) {
							Actor actor = iter.next();
							if(actor instanceof NetActor) {
								NetActor n = (NetActor)actor;
								if(n.getNetId().equals(senderId)) {
									iter.remove();
									break;
								}
							}
						}
					}
					
					//send message to all clients
					for(Client c : clients.values()) {
						if(!c.clientId.equals(senderId)) {
							c.send(socket, packet);
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
					
					float playerX = data.getFloat();
					float playerY = data.getFloat();
					float playerRot = data.getFloat();
					synchronized(stage) {
						for(Actor actor : stage.getActors()) {
							if(actor instanceof Player) {
								Player p = (Player)actor;
								p.setPosition(playerX, playerY);
								p.setRotation(playerRot);
							}
						}
					}
					
					//tell all clients of the sender's new position
					for(Client c : clients.values()) {
						if(!c.clientId.equals(senderId)) {
							c.send(socket, packet);
						}
					}
					break;
				case MSG_SPAWN_BOLT:
					//make sure the client exists
					if(!clients.containsKey(senderId)) {
						logFile.writeString("Client with ID: " + senderId + " tried to fire a bolt before connecting\n", true);
						break;
					}
					
					//add bolt to list of actors
					UUID boltNetId = new UUID(data.getLong(), data.getLong());
					float boltX = data.getFloat();
					float boltY = data.getFloat();
					float boltRot = data.getFloat();
					float boltSpeed = data.getFloat();
					Bolt newBolt = Pools.get(Bolt.class).obtain().setShooterId(senderId).setSpeed(boltSpeed).setNetId(boltNetId);
					newBolt.setPosition(boltX, boltY);
					newBolt.setRotation(boltRot);
					logFile.writeString("Added a bolt ID:" + newBolt.getNetId() + "\n", true);
					synchronized(stage) {
						stage.addActor(newBolt);
					}
					
					//tell all clients that a bolt was spawned
					for(Client c : clients.values()) {
						if(!c.clientId.equals(senderId)) {
							c.send(socket, packet);
						}
					}
				}
			}
		}, "io_thread");
		
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
		heartbeatThread.start();
	}
	
	@Override
	public void render() {
		synchronized(stage) {
			Array<Actor> removedActors = null;
			for(Actor actor : stage.getActors()) {
				if(actor instanceof Bolt) { //TODO implement player-bolt collision
					Bolt bolt = (Bolt)actor;
					bolt.addAction(Actions.moveBy(bolt.getSpeedCompX() * Gdx.graphics.getDeltaTime(), bolt.getSpeedCompY() * Gdx.graphics.getDeltaTime()));
					//remove the bolt if its out of bounds
					if(bolt.getX() < 0 || bolt.getX() > 2000 || bolt.getY() < 0 || bolt.getY() > 1000) { //TODO make better boundary management
						logFile.writeString("Removed a bolt ID:" + bolt.getNetId() + "\n", true);
						//Send a MSG_REMOVE_BOLT packet to all clients
						renderBuffer.rewind();
						int dataLength = 32; //4 longs
						renderBuffer.putInt(dataLength);
						renderBuffer.put(MSG_REMOVE_BOLT);
						renderBuffer.putLong(bolt.getShooterId().getMostSignificantBits());
						renderBuffer.putLong(bolt.getShooterId().getLeastSignificantBits());
						renderBuffer.putLong(bolt.getNetId().getMostSignificantBits());
						renderBuffer.putLong(bolt.getNetId().getLeastSignificantBits());
						renderBuffer.putLong(0);
						renderBuffer.putLong(0);
						renderPacket.setLength(HEADER_LENGTH + dataLength);
						
						Pools.free(bolt);
						removedActors = removedActors == null ? new Array<Actor>() : removedActors;
						removedActors.add(bolt);
						
						synchronized(clients) {
							for(Client c : clients.values()) {
								c.send(socket, renderPacket);
							}
						}
					}
				}
			}
			if(removedActors != null) {
				for(Actor actor : removedActors) {
					actor.remove();
				}
			}
		}
		
		stage.act();
		
		//send packets to update the actors for clients
		synchronized(clients) {
			for(Client client : clients.values()) {
				synchronized(stage) {
					for(Actor actor : stage.getActors()) {
						if(actor instanceof Bolt) {
							Bolt bolt = (Bolt)actor;
							renderBuffer.rewind();
							int dataLength = 28; //2 longs & 3 floats
							renderBuffer.putInt(dataLength);
							renderBuffer.put(MSG_UPDATE);
							renderBuffer.putLong(bolt.getShooterId().getMostSignificantBits());
							renderBuffer.putLong(bolt.getShooterId().getLeastSignificantBits());
							renderBuffer.putLong(bolt.getNetId().getMostSignificantBits());
							renderBuffer.putLong(bolt.getNetId().getLeastSignificantBits());
							renderBuffer.putFloat(bolt.getX());
							renderBuffer.putFloat(bolt.getY());
							renderBuffer.putFloat(bolt.getRotation());
							renderPacket.setLength(HEADER_LENGTH + dataLength);
							client.send(socket, renderPacket);
						}
					}
				}
			}
		}
	}
	
	@Override
	public void dispose() {
		logFile.writeString("Disposing...\n", true);
		
		heartbeatThread.interrupt();
		ioThread.interrupt();
		
		//wait for each thread to finish
		try {
			heartbeatThread.join();
			ioThread.join();
		} catch(InterruptedException e) {}
		
		logFile.writeString("Server disposed\n", true);
	}
}
