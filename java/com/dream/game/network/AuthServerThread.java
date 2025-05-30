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
package com.dream.game.network;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.game.GameTimeController;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.L2GameClient.GameClientState;
import com.dream.game.network.gameserverpackets.AuthRequest;
import com.dream.game.network.gameserverpackets.BlowFishKey;
import com.dream.game.network.gameserverpackets.ChangeAccessLevel;
import com.dream.game.network.gameserverpackets.GameServerBasePacket;
import com.dream.game.network.gameserverpackets.PlayerAuthRequest;
import com.dream.game.network.gameserverpackets.PlayerInGame;
import com.dream.game.network.gameserverpackets.PlayerLogout;
import com.dream.game.network.gameserverpackets.ServerStatus;
import com.dream.game.network.loginserverpackets.AuthResponse;
import com.dream.game.network.loginserverpackets.InitLS;
import com.dream.game.network.loginserverpackets.KickPlayer;
import com.dream.game.network.loginserverpackets.LoginServerFail;
import com.dream.game.network.loginserverpackets.PlayerAuthResponse;
import com.dream.game.network.serverpackets.CharSelectionInfo;
import com.dream.game.network.serverpackets.LoginFail;
import com.dream.tools.random.Rnd;
import com.dream.tools.security.NewCrypt;
import com.dream.util.Console;

public class AuthServerThread extends Thread
{
	public static class SessionKey
	{
		public int playOkID1;
		public int playOkID2;
		public int loginOkID1;
		public int loginOkID2;
		public int clientKey = -1;

		public SessionKey(int loginOK1, int loginOK2, int playOK1, int playOK2)
		{
			playOkID1 = playOK1;
			playOkID2 = playOK2;
			loginOkID1 = loginOK1;
			loginOkID2 = loginOK2;
		}

		@Override
		public String toString()
		{
			return "PlayOk: " + playOkID1 + " " + playOkID2 + " LoginOk:" + loginOkID1 + " " + loginOkID2;
		}
	}

	private static class SingletonHolder
	{
		protected static final AuthServerThread _instance = new AuthServerThread();
	}

	private class WaitingClient
	{
		public String account;
		public L2GameClient gameClient;
		public SessionKey session;

		public WaitingClient(String acc, L2GameClient client, SessionKey key)
		{
			account = acc;
			GameTimeController.getGameTicks();
			gameClient = client;
			session = key;
		}
	}

	private static final Logger _log = Logger.getLogger(AuthServerThread.class);

	private static final int REVISION = 0x0102;

	public static byte[] generateHex(int size)
	{
		byte[] array = new byte[size];
		Rnd.nextBytes(array);
		return array;
	}

	public static AuthServerThread getInstance()
	{
		return SingletonHolder._instance;
	}

	private static String hexToString(byte[] hex)
	{
		return new BigInteger(hex).toString(16);
	}

	private RSAPublicKey _publicKey;
	private final String _hostname;
	private final int _port;
	private final int _gamePort;
	private Socket _loginSocket;
	private InputStream _in;
	private OutputStream _out;
	private NewCrypt _blowfish;
	private byte[] _blowfishKey;
	private byte[] _hexID;
	private final boolean _acceptAlternate;
	private int _requestID;
	private int _serverID;
	private final boolean _reserveHost;
	private int _maxPlayer;

	private final List<WaitingClient> _waitingClients;

	private final Map<String, L2GameClient> _accountsInGameServer;

	private int _status;

	private String _serverName;

	private final String _gameExternalHost;

	private final String _gameInternalHost;

	public AuthServerThread()
	{
		super("AuthServerThread");
		_port = Config.GAME_SERVER_LOGIN_PORT;
		_gamePort = Config.PORT_GAME;
		_hostname = Config.GAME_SERVER_LOGIN_HOST;
		_hexID = Config.HEX_ID;
		if (_hexID == null)
		{
			_requestID = Config.REQUEST_ID;
			_hexID = generateHex(16);
		}
		else
		{
			_requestID = Config.SERVER_ID;
		}
		_acceptAlternate = Config.ACCEPT_ALTERNATE_ID;
		_reserveHost = Config.RESERVE_HOST_ON_LOGIN;
		_gameExternalHost = Config.EXTERNAL_HOSTNAME;
		_gameInternalHost = Config.INTERNAL_HOSTNAME;
		_waitingClients = new ArrayList<>();
		_accountsInGameServer = new ConcurrentHashMap<>();
		_maxPlayer = Config.MAXIMUM_ONLINE_USERS;
	}

	public void addGameServerLogin(String account, L2GameClient client)
	{
		_accountsInGameServer.put(account, client);
	}

	public void addWaitingClientAndSendRequest(String acc, L2GameClient client, SessionKey key)
	{
		synchronized (_waitingClients)
		{
			_waitingClients.add(new WaitingClient(acc, client, key));
		}

		try
		{
			sendPacket(new PlayerAuthRequest(acc, key));
		}
		catch (IOException e)
		{
			_log.warn("Error while sending player auth request");
		}
	}

	public void doKickPlayer(String account)
	{
		if (_accountsInGameServer.get(account) != null)
		{
			_accountsInGameServer.get(account).closeNow();
			AuthServerThread.getInstance().sendLogout(account);
		}
	}

	public String getExHost()
	{
		return _gameExternalHost;
	}

	public int getMaxPlayer()
	{
		return _maxPlayer;
	}

	public String getServerName()
	{
		return _serverName;
	}

	public String getStatusString()
	{
		return ServerStatus.STATUS_STRING[_status];
	}

	public boolean isBracketShown()
	{
		return Config.SERVER_LIST_BRACKET;
	}

	public boolean isClockShown()
	{
		return Config.SERVER_LIST_CLOCK;
	}

	public void removeWaitingClient(L2GameClient client)
	{
		WaitingClient toRemove = null;

		synchronized (_waitingClients)
		{
			for (WaitingClient c : _waitingClients)
				if (c.gameClient == client)
				{
					toRemove = c;
				}

			if (toRemove != null)
			{
				_waitingClients.remove(toRemove);
			}
		}

		toRemove = null;
	}

	@Override
	public void run()
	{
		while (!isInterrupted())
		{
			int lengthHi = 0;
			int lengthLo = 0;
			int length = 0;
			boolean checksumOk = false;
			try
			{
				_log.info("Connect: Authed on " + _hostname + ":" + _port);
				_loginSocket = new Socket(_hostname, _port);
				_in = _loginSocket.getInputStream();
				_out = new BufferedOutputStream(_loginSocket.getOutputStream());
				_blowfishKey = generateHex(40);
				_blowfish = new NewCrypt("_;v.]05-31!|+-%xT!^[$\00");
				while (!isInterrupted())
				{
					lengthLo = _in.read();
					lengthHi = _in.read();
					length = lengthHi * 256 + lengthLo;

					if (lengthHi < 0)
					{
						_log.warn("AuthServerThread: Auth terminated the connection.");
						break;
					}

					byte[] incoming = new byte[length - 2];

					int receivedBytes = 0;
					int newBytes = 0;
					int left = length - 2;

					while (newBytes != -1 && receivedBytes < length - 2)
					{
						newBytes = _in.read(incoming, receivedBytes, left);
						receivedBytes = receivedBytes + newBytes;
						left -= newBytes;
					}

					if (receivedBytes != length - 2)
					{
						_log.warn("Incomplete Packet is sent to the server, closing connection.(Auth)");
						break;
					}

					byte[] decrypt = _blowfish.decrypt(incoming);
					checksumOk = NewCrypt.verifyChecksum(decrypt);

					if (!checksumOk)
					{
						_log.warn("Incorrect packet checksum, ignoring packet (Auth)");
						break;
					}

					int packetType = decrypt[0] & 0xff;
					switch (packetType)
					{
						case 00:
							InitLS init = new InitLS(decrypt);
							if (init.getRevision() != REVISION)
							{
								_log.warn("/!\\ Revision mismatch between LS and GS /!\\");
								break;
							}
							try
							{
								KeyFactory kfac = KeyFactory.getInstance("RSA");
								BigInteger modulus = new BigInteger(init.getRSAKey());
								RSAPublicKeySpec kspec1 = new RSAPublicKeySpec(modulus, RSAKeyGenParameterSpec.F4);
								_publicKey = (RSAPublicKey) kfac.generatePublic(kspec1);
							}

							catch (GeneralSecurityException e)
							{
								_log.warn("Troubles while init the public key send by login");
								break;
							}
							BlowFishKey bfk = new BlowFishKey(_blowfishKey, _publicKey);
							sendPacket(bfk);

							_blowfish = new NewCrypt(_blowfishKey);
							AuthRequest ar = new AuthRequest(_requestID, _acceptAlternate, _hexID, _gameExternalHost, _gameInternalHost, _gamePort, _reserveHost, _maxPlayer);
							sendPacket(ar);
							break;
						case 01:
							LoginServerFail lsf = new LoginServerFail(decrypt);
							_log.info("Damn! Registeration Failed: " + lsf.getReasonString());
							break;
						case 02:
							AuthResponse aresp = new AuthResponse(decrypt);
							_serverID = aresp.getServerId();
							_serverName = aresp.getServerName();
							Config.saveHexid(_serverID, hexToString(_hexID));
							_log.info("Register: Server " + _serverID + " - " + _serverName);
							Console.printSection("Live");
							ServerStatus st = new ServerStatus();
							if (Config.SERVER_LIST_BRACKET)
							{
								st.addAttribute(ServerStatus.SERVER_LIST_SQUARE_BRACKET, ServerStatus.ON);
							}
							else
							{
								st.addAttribute(ServerStatus.SERVER_LIST_SQUARE_BRACKET, ServerStatus.OFF);
							}
							if (Config.SERVER_LIST_CLOCK)
							{
								st.addAttribute(ServerStatus.SERVER_LIST_CLOCK, ServerStatus.ON);
							}
							else
							{
								st.addAttribute(ServerStatus.SERVER_LIST_CLOCK, ServerStatus.OFF);
							}
							if (Config.SERVER_LIST_TESTSERVER)
							{
								st.addAttribute(ServerStatus.TEST_SERVER, ServerStatus.ON);
							}
							else
							{
								st.addAttribute(ServerStatus.TEST_SERVER, ServerStatus.OFF);
							}
							if (Config.SERVER_GMONLY)
							{
								st.addAttribute(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_GM_ONLY);
							}
							else
							{
								st.addAttribute(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_AUTO);
							}
							sendPacket(st);
							if (L2World.getInstance().getAllPlayersCount() > 0)
							{
								List<String> playerList = new ArrayList<>();
								for (L2PcInstance player : L2World.getInstance().getAllPlayers())
								{
									if (player == null || player.getClient() == null)
									{
										continue;
									}
									if (!player.isOfflineTrade())
									{
										playerList.add(player.getAccountName());
									}
									playerList.add(player.getAccountName());
								}
								sendPacket(new PlayerInGame(playerList));
							}
							break;
						case 03:
							PlayerAuthResponse par = new PlayerAuthResponse(decrypt);
							String account = par.getAccount();
							WaitingClient wcToRemove = null;
							synchronized (_waitingClients)
							{
								for (WaitingClient wc : _waitingClients)
									if (wc.account.equals(account))
									{
										wcToRemove = wc;
									}
							}
							if (wcToRemove != null)
							{
								if (par.isAuthed())
								{
									sendPacket(new PlayerInGame(par.getAccount()));

									wcToRemove.gameClient.setState(GameClientState.AUTHED);
									wcToRemove.gameClient.setSessionId(wcToRemove.session);

									CharSelectionInfo cl = new CharSelectionInfo(wcToRemove.account, wcToRemove.gameClient.getSessionId().playOkID1);
									wcToRemove.gameClient.getConnection().sendPacket(cl);
									wcToRemove.gameClient.setCharSelection(cl.getCharInfo());
									cl = null;
								}
								else
								{
									_log.warn("session key is not correct. closing connection");
									wcToRemove.gameClient.getConnection().sendPacket(new LoginFail(1));
									wcToRemove.gameClient.closeNow();
								}
								_waitingClients.remove(wcToRemove);
							}
							break;
						case 04:
							KickPlayer kp = new KickPlayer(decrypt);
							doKickPlayer(kp.getAccount());
							kp = null;
							break;
					}
				}
			}
			catch (UnknownHostException e)
			{
			}
			catch (IOException e)
			{
				_log.info("No connection found with AuthServer, next try in 10 seconds.");
				_log.info(e.toString());
			}
			finally
			{
				try
				{
					_loginSocket.close();
					if (isInterrupted())
						return;
				}
				catch (Exception e)
				{
				}
			}
			try
			{
				Thread.sleep(10000);
			}
			catch (InterruptedException e)
			{
			}
		}
	}

	public void sendAccessLevel(String account, int level)
	{
		try
		{
			sendPacket(new ChangeAccessLevel(account, level));
		}
		catch (IOException e)
		{

		}

	}

	public void sendLogout(String account)
	{
		try
		{
			sendPacket(new PlayerLogout(account));
		}
		catch (IOException e)
		{
			_log.warn("Error while sending logout packet to login");
		}
	}

	private void sendPacket(GameServerBasePacket sl) throws IOException
	{
		byte[] data = sl.getContent();
		NewCrypt.appendChecksum(data);
		data = _blowfish.crypt(data);

		int len = data.length + 2;
		synchronized (_out)
		{
			_out.write(len & 0xff);
			_out.write(len >> 8 & 0xff);
			_out.write(data);
			_out.flush();
		}
	}

	public void sendServerStatus(int id, int value)
	{
		ServerStatus ss = new ServerStatus();
		ss.addAttribute(id, value);
		try
		{
			sendPacket(ss);
		}
		catch (IOException e)
		{
		}

		ss = null;
	}

	public void setMaxPlayer(int maxPlayer)
	{
		sendServerStatus(ServerStatus.MAX_PLAYERS, maxPlayer);
		_maxPlayer = maxPlayer;
	}

	public void setServerStatus(int status)
	{
		switch (status)
		{
			case ServerStatus.STATUS_AUTO:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_AUTO);
				_status = status;
				break;
			case ServerStatus.STATUS_DOWN:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_DOWN);
				_status = status;
				break;
			case ServerStatus.STATUS_FULL:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_FULL);
				_status = status;
				break;
			case ServerStatus.STATUS_GM_ONLY:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_GM_ONLY);
				_status = status;
				break;
			case ServerStatus.STATUS_GOOD:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_GOOD);
				_status = status;
				break;
			case ServerStatus.STATUS_NORMAL:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_NORMAL);
				_status = status;
				break;
			default:
				throw new IllegalArgumentException("Status does not exists:" + status);
		}
	}
}