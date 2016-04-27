package com.fwumdesoft.shoot.net;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Allows clients to send/receive messages to the server.
 */
public class ServerInterface {
	private static final String HOST = "45.33.68.145";
	private static final int PORT = 5555;
	
	private static boolean connected;
	private static Socket socket;
	
	/**
	 * Connects to the server.
	 * @return <tt>true</tt> if connection succeeded, otherwise <tt>false</tt>.
	 */
	public static boolean connect() {
		try {
			SocketHints hints = new SocketHints();
			socket = Gdx.net.newClientSocket(Protocol.TCP, HOST, PORT, hints);
			//TODO create some io stream here
			connected = socket != null;
		} catch(GdxRuntimeException e) {
			Gdx.app.log("ERROR", "Failed to connect to server");
			connected = false;
		}
		return connected;
	}
	
	public static void dispose() {
		if(socket != null)
			socket.dispose();
		connected = false;
		socket = null;
	}
}
