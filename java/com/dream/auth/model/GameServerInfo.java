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
package com.dream.auth.model;

import com.dream.auth.network.gameserverpackets.ServerStatus;
import com.dream.auth.thread.GameServerThread;

public class GameServerInfo
{
	private int _id;
	private final byte[] _hexId;
	private boolean _isAuthed;

	// status
	private GameServerThread _gst;
	private int _status;

	// network
	private String _internalIp;
	private String _externalIp;
	private String _externalHost;
	private int _port;

	// config
	private final boolean _isPvp = true;
	private boolean _isTestServer;
	private boolean _isShowingClock;
	private boolean _isShowingBrackets;
	private int _maxPlayers;

	public GameServerInfo(int id, byte[] hexId)
	{
		this(id, hexId, null);
	}

	public GameServerInfo(int id, byte[] hexId, GameServerThread gst)
	{
		_id = id;
		_hexId = hexId;
		_gst = gst;
		_status = ServerStatus.STATUS_DOWN;
	}

	public int getCurrentPlayerCount()
	{
		if (_gst == null)
			return 0;
		return _gst.getPlayerCount();
	}

	public String getExternalHost()
	{
		return _externalHost;
	}

	public String getExternalIp()
	{
		return _externalIp;
	}

	public GameServerThread getGameServerThread()
	{
		return _gst;
	}

	public byte[] getHexId()
	{
		return _hexId;
	}

	public int getId()
	{
		return _id;
	}

	public String getInternalHost()
	{
		return _internalIp;
	}

	public int getMaxPlayers()
	{
		return _maxPlayers;
	}

	public int getPort()
	{
		return _port;
	}

	public int getStatus()
	{
		return _status;
	}

	public boolean isAuthed()
	{
		return _isAuthed;
	}

	public boolean isPvp()
	{
		return _isPvp;
	}

	public boolean isShowingBrackets()
	{
		return _isShowingBrackets;
	}

	public boolean isShowingClock()
	{
		return _isShowingClock;
	}

	public boolean isTestServer()
	{
		return _isTestServer;
	}

	public void setAuthed(boolean isAuthed)
	{
		_isAuthed = isAuthed;
	}

	public void setDown()
	{
		setAuthed(false);
		setPort(0);
		setGameServerThread(null);
		setStatus(ServerStatus.STATUS_DOWN);
	}

	public void setExternalHost(String externalHost)
	{
		_externalHost = externalHost;
	}

	public void setExternalIp(String externalIp)
	{
		_externalIp = externalIp;
	}

	public void setGameServerThread(GameServerThread gst)
	{
		_gst = gst;
	}

	public void setId(int id)
	{
		_id = id;
	}

	public void setInternalIp(String internalIp)
	{
		_internalIp = internalIp;
	}

	public void setMaxPlayers(int maxPlayers)
	{
		_maxPlayers = maxPlayers;
	}

	public void setPort(int port)
	{
		_port = port;
	}

	public void setShowingBrackets(boolean val)
	{
		_isShowingBrackets = val;
	}

	public void setShowingClock(boolean clock)
	{
		_isShowingClock = clock;
	}

	public void setStatus(int status)
	{
		_status = status;
	}

	public void setTestServer(boolean val)
	{
		_isTestServer = val;
	}

}