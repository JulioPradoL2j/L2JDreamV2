/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.dream.auth.network.serverpackets;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.TreeMap;

import com.dream.auth.L2AuthClient;
import com.dream.auth.manager.GameServerManager;
import com.dream.auth.model.GameServerInfo;
import com.dream.auth.network.gameserverpackets.ServerStatus;

/**
 * ServerList Format: cc [cddcchhcdc] c: server list size (number of servers) c: ? [ (repeat for each servers) c: server id (ignored by client?) d: server ip d: server port c: age limit (used by client?) c: pvp or not (used by client?) h: current number of players h: max number of players c: 0 if server is down d: 2nd bit: clock 3rd bit: wont dsiplay server name 4th bit: test server (used by client?) c: 0 if you dont want to display brackets in front of sever name ] Server will be considered as Good when the number of online players is less than half the maximum. as Normal between half and 4/5
 * and Full when there's more than 4/5 of the maximum number of players
 */
public final class ServerList extends L2AuthServerPacket
{
	class ServerData
	{
		protected String _ip;
		protected int _port;
		protected boolean _pvp;
		protected int _currentPlayers;
		protected int _maxPlayers;
		protected boolean _testServer;
		protected boolean _brackets;
		protected boolean _clock;
		protected int _status;
		protected int _serverId;

		ServerData(String pIp, int pPort, boolean pPvp, boolean pTestServer, int pCurrentPlayers, int pMaxPlayers, boolean pBrackets, boolean pClock, int pStatus, int pServer_id)
		{
			_ip = pIp;
			_port = pPort;
			_pvp = pPvp;
			_testServer = pTestServer;
			_currentPlayers = pCurrentPlayers;
			_maxPlayers = pMaxPlayers;
			_brackets = pBrackets;
			_clock = pClock;
			_status = pStatus;
			_serverId = pServer_id;
		}
	}

	private final Map<Integer, ServerData> _servers;

	public ServerList(L2AuthClient client)
	{
		_servers = new TreeMap<>();

		for (GameServerInfo gsi : GameServerManager.getInstance().getRegisteredGameServers().values())
		{
			if (gsi.getStatus() == ServerStatus.STATUS_GM_ONLY && client.getAccessLevel() > 0)
			{
				// Server is GM-Only but you've got GM Status
				addServer(client.usesInternalIP() ? gsi.getInternalHost() : gsi.getExternalHost(), gsi.getPort(), gsi.isPvp(), gsi.isTestServer(), gsi.getCurrentPlayerCount(), gsi.getMaxPlayers(), gsi.isShowingBrackets(), gsi.isShowingClock(), gsi.getStatus(), gsi.getId());
			}
			else if (gsi.getStatus() != ServerStatus.STATUS_GM_ONLY)
			{
				// Server is not GM-Only
				addServer(client.usesInternalIP() ? gsi.getInternalHost() : gsi.getExternalHost(), gsi.getPort(), gsi.isPvp(), gsi.isTestServer(), gsi.getCurrentPlayerCount(), gsi.getMaxPlayers(), gsi.isShowingBrackets(), gsi.isShowingClock(), gsi.getStatus(), gsi.getId());
			}
			else
			{
				// Server's GM-Only and you've got no GM-Status
				addServer(client.usesInternalIP() ? gsi.getInternalHost() : gsi.getExternalHost(), gsi.getPort(), gsi.isPvp(), gsi.isTestServer(), gsi.getCurrentPlayerCount(), gsi.getMaxPlayers(), gsi.isShowingBrackets(), gsi.isShowingClock(), ServerStatus.STATUS_DOWN, gsi.getId());
			}
		}
	}

	public void addServer(String ip, int port, boolean pvp, boolean testServer, int currentPlayer, int maxPlayer, boolean brackets, boolean clock, int status, int server_id)
	{
		_servers.put(server_id, new ServerData(ip, port, pvp, testServer, currentPlayer, maxPlayer, brackets, clock, status, server_id));
	}

	@Override
	public void write()
	{
		ServerData server;

		writeC(0x04);
		writeC(_servers.size());
		server = _servers.get(getClient().getLastServerId());
		if (server != null && server._status != ServerStatus.STATUS_DOWN)
		{
			writeC(server._serverId);
		}
		else
		{
			writeC(0);
		}

		for (Integer serverId : _servers.keySet())
		{
			server = _servers.get(serverId);

			writeC(server._serverId);

			try
			{
				InetAddress i4 = InetAddress.getByName(server._ip);
				byte[] raw = i4.getAddress();
				writeC(raw[0] & 0xff);
				writeC(raw[1] & 0xff);
				writeC(raw[2] & 0xff);
				writeC(raw[3] & 0xff);
			}
			catch (UnknownHostException e)
			{
				e.printStackTrace();
				writeC(127);
				writeC(0);
				writeC(0);
				writeC(1);
			}

			writeD(server._port);
			writeC(0x00); // age limit
			writeC(server._pvp ? 0x01 : 0x00);
			writeH(server._currentPlayers);
			writeH(server._maxPlayers);
			writeC(server._status == ServerStatus.STATUS_DOWN ? 0x00 : 0x01);
			int bits = 0;
			if (server._testServer)
			{
				bits |= 0x04;
			}
			if (server._clock)
			{
				bits |= 0x02;
			}
			writeD(bits);
			writeC(server._brackets ? 0x01 : 0x00);
		}
	}

}
