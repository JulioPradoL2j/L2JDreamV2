/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.dream.auth.thread;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.dream.AuthConfig;

public abstract class FloodProtectedListener extends Thread
{
	protected static class ForeignConnection
	{
		public int connectionNumber;
		public long lastConnection;
		public boolean isFlooding = false;

		public ForeignConnection(long time)
		{
			lastConnection = time;
			connectionNumber = 1;
		}
	}

	private final Logger _log = Logger.getLogger(FloodProtectedListener.class.getName());
	private final Map<String, ForeignConnection> _floodProtection = new HashMap<>();
	private final String _listenIp;
	private final int _port;
	private ServerSocket _serverSocket;

	private Socket connection;

	public FloodProtectedListener(String listenIp, int port)
	{
		_port = port;
		_listenIp = listenIp;
		try
		{
			if (_listenIp.equals("*"))
			{
				_serverSocket = new ServerSocket(_port);
			}
			else
			{
				_serverSocket = new ServerSocket(_port, 50, InetAddress.getByName(_listenIp));
			}
		}
		catch (IOException e)
		{
			try
			{
				_serverSocket.close();
			}
			catch (Exception e2)
			{
			}
			_log.warn("Error creating ServerSocket: " + e);
		}
	}

	public abstract void addClient(Socket s);

	public void close()
	{
		try
		{
			_serverSocket.close();
		}
		catch (IOException e)
		{
			_log.warn(e.getMessage(), e);
		}
	}

	public void removeFloodProtection(String ip)
	{
		if (!AuthConfig.FLOOD_PROTECTION)
			return;
		ForeignConnection fConnection = _floodProtection.get(ip);
		if (fConnection != null)
		{
			fConnection.connectionNumber -= 1;
			if (fConnection.connectionNumber == 0)
			{
				_floodProtection.remove(ip);
			}
		}
		else
		{
			_log.warn("Removing a flood protection for a GameServer that was not in the connection map??? :" + ip);
		}
	}

	@Override
	public void run()
	{
		if (_serverSocket == null)
			return;

		connection = null;

		while (true)
		{
			try
			{
				connection = _serverSocket.accept();
				if (AuthConfig.FLOOD_PROTECTION)
				{
					ForeignConnection fConnection = _floodProtection.get(connection.getInetAddress().getHostAddress());
					if (fConnection != null)
					{
						fConnection.connectionNumber += 1;
						if (fConnection.connectionNumber > AuthConfig.FAST_CONNECTION_LIMIT && System.currentTimeMillis() - fConnection.lastConnection < AuthConfig.NORMAL_CONNECTION_TIME || System.currentTimeMillis() - fConnection.lastConnection < AuthConfig.FAST_CONNECTION_TIME || fConnection.connectionNumber > AuthConfig.MAX_CONNECTION_PER_IP)
						{
							fConnection.lastConnection = System.currentTimeMillis();
							connection.close();
							fConnection.connectionNumber -= 1;
							if (!fConnection.isFlooding)
							{
								_log.warn("Potential Flood from " + connection.getInetAddress().getHostAddress());
							}
							fConnection.isFlooding = true;
							continue;
						}
						if (fConnection.isFlooding)
						{
							fConnection.isFlooding = false;
							_log.info(connection.getInetAddress().getHostAddress() + " is not considered as flooding anymore.");
						}
						fConnection.lastConnection = System.currentTimeMillis();
					}
					else
					{
						fConnection = new ForeignConnection(System.currentTimeMillis());
						_floodProtection.put(connection.getInetAddress().getHostAddress(), fConnection);
					}
				}
				addClient(connection);
			}
			catch (Exception e)
			{
				try
				{
					connection.close();
				}
				catch (Exception e2)
				{
				}
				if (isInterrupted())
				{
					try
					{
						_serverSocket.close();
					}
					catch (IOException io)
					{
						_log.info(io.getMessage(), io);
					}
					break;
				}
			}
		}
	}
}