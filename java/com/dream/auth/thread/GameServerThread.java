package com.dream.auth.thread;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.dream.AuthConfig;
import com.dream.auth.L2AuthServer;
import com.dream.auth.manager.AuthManager;
import com.dream.auth.manager.GameServerManager;
import com.dream.auth.model.GameServerInfo;
import com.dream.auth.model.SessionKey;
import com.dream.auth.network.gameserverpackets.BlowFishKey;
import com.dream.auth.network.gameserverpackets.ChangeAccessLevel;
import com.dream.auth.network.gameserverpackets.ChangeIpAddres;
import com.dream.auth.network.gameserverpackets.GameServerAuth;
import com.dream.auth.network.gameserverpackets.PlayerAuthRequest;
import com.dream.auth.network.gameserverpackets.PlayerInGame;
import com.dream.auth.network.gameserverpackets.PlayerLogout;
import com.dream.auth.network.gameserverpackets.ServerStatus;
import com.dream.auth.network.loginserverpackets.AuthResponse;
import com.dream.auth.network.loginserverpackets.AuthServerFail;
import com.dream.auth.network.loginserverpackets.InitLS;
import com.dream.auth.network.loginserverpackets.KickPlayer;
import com.dream.auth.network.loginserverpackets.PlayerAuthResponse;
import com.dream.auth.network.serverpackets.ServerBasePacket;
import com.dream.tools.security.NewCrypt;

public class GameServerThread extends Thread
{
	private static final Logger _log = Logger.getLogger(GameServerThread.class.getName());

	public static boolean isBannedGameserverIP(String ipAddress)
	{
		return false;
	}

	private final Socket _connection;
	private InputStream _in;

	private OutputStream _out;
	private final RSAPublicKey _publicKey;
	private final RSAPrivateKey _privateKey;
	private NewCrypt _blowfish;

	private byte[] _blowfishKey;

	private final String _connectionIp;
	private GameServerInfo _gsi;

	private final Set<String> _accountsOnGameServer = new HashSet<>();

	private String _connectionIpAddress;

	public GameServerThread(Socket con)
	{
		_connection = con;
		_connectionIp = con.getInetAddress().getHostAddress();
		try
		{
			_in = _connection.getInputStream();
			_out = new BufferedOutputStream(_connection.getOutputStream());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		KeyPair pair = GameServerManager.getInstance().getKeyPair();
		_privateKey = (RSAPrivateKey) pair.getPrivate();
		_publicKey = (RSAPublicKey) pair.getPublic();
		_blowfish = new NewCrypt("_;v.]05-31!|+-%xT!^[$\00");
		start();
	}

	private void attachGameServerInfo(GameServerInfo gsi, GameServerAuth gameServerAuth)
	{
		setGameServerInfo(gsi);
		gsi.setGameServerThread(this);
		gsi.setPort(gameServerAuth.getPort());
		setGameHosts(gameServerAuth.getExternalHost(), gameServerAuth.getInternalHost());
		gsi.setMaxPlayers(gameServerAuth.getMaxPlayers());
		gsi.setAuthed(true);
	}

	private void forceClose(int reason)
	{
		AuthServerFail lsf = new AuthServerFail(reason);
		sendPacket(lsf);

		try
		{
			_connection.close();
		}
		catch (IOException e)
		{
			_log.info("GameServerThread: Failed disconnecting banned server, server already disconnected.");
		}
	}

	public String getConnectionIpAddress()
	{
		return _connectionIpAddress;
	}

	public GameServerInfo getGameServerInfo()
	{
		return _gsi;
	}

	public int getPlayerCount()
	{
		return _accountsOnGameServer.size();
	}

	private int getServerId()
	{
		if (getGameServerInfo() != null)
			return getGameServerInfo().getId();

		return -1;
	}

	private void handleRegProcess(GameServerAuth gameServerAuth)
	{
		GameServerManager gameServerTable = GameServerManager.getInstance();

		int id = gameServerAuth.getDesiredID();
		byte[] hexId = gameServerAuth.getHexID();

		GameServerInfo gsi = gameServerTable.getRegisteredGameServerById(id);
		if (gsi != null)
		{
			if (Arrays.equals(gsi.getHexId(), hexId))
			{
				synchronized (gsi)
				{
					if (gsi.isAuthed())
					{
						forceClose(AuthServerFail.REASON_ALREADY_LOGGED8IN);
					}
					else
					{
						attachGameServerInfo(gsi, gameServerAuth);
					}
				}
			}
			else if (AuthConfig.ACCEPT_NEW_GAMESERVER && gameServerAuth.acceptAlternateID())
			{
				gsi = new GameServerInfo(id, hexId, this);
				if (gameServerTable.registerWithFirstAvailableId(gsi))
				{
					attachGameServerInfo(gsi, gameServerAuth);
					gameServerTable.registerServerOnDB(gsi);
				}
				else
				{
					forceClose(AuthServerFail.REASON_NO_FREE_ID);
				}
			}
			else
			{
				forceClose(AuthServerFail.REASON_WRONG_HEXID);
			}
		}
		else if (AuthConfig.ACCEPT_NEW_GAMESERVER)
		{
			gsi = new GameServerInfo(id, hexId, this);
			if (gameServerTable.register(id, gsi))
			{
				attachGameServerInfo(gsi, gameServerAuth);
				gameServerTable.registerServerOnDB(gsi);
			}
			else
			{
				forceClose(AuthServerFail.REASON_ID_RESERVED);
			}
		}
		else
		{
			forceClose(AuthServerFail.REASON_WRONG_HEXID);
		}
	}

	public boolean hasAccountOnGameServer(String account)
	{
		return _accountsOnGameServer.contains(account);
	}

	public boolean isAuthed()
	{
		if (getGameServerInfo() == null)
			return false;

		return getGameServerInfo().isAuthed();
	}

	public void kickPlayer(String account)
	{
		KickPlayer kp = new KickPlayer(account);
		sendPacket(kp);
	}

	private void onGameServerAuth(byte[] data)
	{
		GameServerAuth gsa = new GameServerAuth(data);
		handleRegProcess(gsa);
		if (isAuthed())
		{
			AuthResponse ar = new AuthResponse(getGameServerInfo().getId());
			sendPacket(ar);
		}

	}

	private void onReceiveBlowfishKey(byte[] data)
	{
		BlowFishKey bfk = new BlowFishKey(data, _privateKey);
		_blowfishKey = bfk.getKey();
		_blowfish = new NewCrypt(_blowfishKey);
	}

	private void onReceiveChangeAccessLevel(byte[] data)
	{
		if (isAuthed())
		{
			ChangeAccessLevel cal = new ChangeAccessLevel(data);
			try
			{
				AuthManager.getInstance().changeAccountLevel(cal.getAccount(), cal.getLevel());
				_log.info("Changed " + cal.getAccount() + " access level to " + cal.getLevel());
			}
			catch (Exception e)
			{
				_log.warn("Access level could not be changed. Reason: " + e.getMessage());
			}
		}
		else
		{
			forceClose(AuthServerFail.NOT_AUTHED);
		}
	}

	private void onReceiveChangeAllowedIP(byte[] data)
	{
		if (isAuthed())
		{
			ChangeIpAddres cia = new ChangeIpAddres(data);
			try
			{
				AuthManager.getInstance().changeAllowedIP(cia.getAccount(), cia.getIpHost());
			}
			catch (Exception e)
			{
				_log.warn("Can't change allowedIP. Reason: " + e.getMessage());
			}
		}
		else
		{
			forceClose(AuthServerFail.NOT_AUTHED);
		}
	}

	private void onReceivePlayerAuthRequest(byte[] data)
	{
		if (isAuthed())
		{
			final PlayerAuthRequest par = new PlayerAuthRequest(data);
			final SessionKey key = AuthManager.getInstance().getKeyForAccount(par.getAccount());

			if (key != null && key.equals(par.getKey()))
			{
				AuthManager.getInstance().removeAuthedLoginClient(par.getAccount());
				sendPacket(new PlayerAuthResponse(par.getAccount(), true));
			}
			else
			{
				sendPacket(new PlayerAuthResponse(par.getAccount(), false));
			}
		}
		else
		{
			forceClose(AuthServerFail.NOT_AUTHED);
		}
	}

	private void onReceivePlayerInGame(byte[] data)
	{
		if (isAuthed())
		{
			PlayerInGame pig = new PlayerInGame(data);
			List<String> newAccounts = pig.getAccounts();
			for (String account : newAccounts)
			{
				_accountsOnGameServer.add(account);
			}

		}
		else
		{
			forceClose(AuthServerFail.NOT_AUTHED);
		}
	}

	private void onReceivePlayerLogOut(byte[] data)
	{
		if (isAuthed())
		{
			PlayerLogout plo = new PlayerLogout(data);
			_accountsOnGameServer.remove(plo.getAccount());
		}
		else
		{
			forceClose(AuthServerFail.NOT_AUTHED);
		}
	}

	private void onReceiveServerStatus(byte[] data)
	{
		if (isAuthed())
		{
			new ServerStatus(data, getServerId());
		}
		else
		{
			forceClose(AuthServerFail.NOT_AUTHED);
		}
	}

	private void onRestartRequest(byte[] data)
	{
		if (isAuthed())
		{
			_log.info("Auth Server has been restarted! Conection IP: " + _connectionIpAddress + ".");
			System.exit(2);
		}
		else
		{
			forceClose(AuthServerFail.NOT_AUTHED);
		}
	}

	public void removeAcc(String account)
	{
		_accountsOnGameServer.remove(account);
	}

	@Override
	public void run()
	{
		_connectionIpAddress = _connection.getInetAddress().getHostAddress();
		if (GameServerThread.isBannedGameserverIP(_connectionIpAddress))
		{
			_log.info("GameServerRegistration: IP Address " + _connectionIpAddress + " is on Banned IP list.");
			forceClose(AuthServerFail.REASON_IP_BANNED);
			return;
		}

		try
		{
			sendPacket(new InitLS(_publicKey.getModulus().toByteArray()));

			int lengthHi = 0;
			int lengthLo = 0;
			int length = 0;
			boolean checksumOk = false;
			for (;;)
			{
				lengthLo = _in.read();
				lengthHi = _in.read();
				length = lengthHi * 256 + lengthLo;

				if (lengthHi < 0 || _connection.isClosed())
				{
					break;
				}

				byte[] data = new byte[length - 2];

				int receivedBytes = 0;
				int newBytes = 0;
				while (newBytes != -1 && receivedBytes < length - 2)
				{
					newBytes = _in.read(data, 0, length - 2);
					receivedBytes = receivedBytes + newBytes;
				}

				if (receivedBytes != length - 2)
				{
					_log.warn("Incomplete Packet is sent to the server, closing connection.(LS)");
					break;
				}

				data = _blowfish.decrypt(data);
				checksumOk = NewCrypt.verifyChecksum(data);
				if (!checksumOk)
				{
					_log.warn("Incorrect packet checksum, closing connection (LS)");
					return;
				}

				int packetType = data[0] & 0xff;
				switch (packetType)
				{
					case 00:
						onReceiveBlowfishKey(data);
						break;
					case 01:
						onGameServerAuth(data);
						break;
					case 02:
						onReceivePlayerInGame(data);
						break;
					case 03:
						onReceivePlayerLogOut(data);
						break;
					case 04:
						onReceiveChangeAccessLevel(data);
						break;
					case 05:
						onReceivePlayerAuthRequest(data);
						break;
					case 06:
						onReceiveServerStatus(data);
						break;
					case 07:
						onReceiveChangeAllowedIP(data);
						break;
					case 8:
						onRestartRequest(data);
						break;
					default:
						_log.warn("Unknown Opcode (" + Integer.toHexString(packetType).toUpperCase() + ") from GameServer, closing connection.");
						forceClose(AuthServerFail.NOT_AUTHED);
				}
			}
		}
		catch (IOException e)
		{
			String serverName = getServerId() != -1 ? "[" + getServerId() + "] " + GameServerManager.getInstance().getServerNameById(getServerId()) : "(" + _connectionIpAddress + ")";
			String msg = "GameServer " + serverName + ": Connection lost: " + e.getMessage();
			_log.info(msg);
		}
		finally
		{
			if (isAuthed())
			{
				_gsi.setDown();
				_log.info("Server [" + getServerId() + "] " + GameServerManager.getInstance().getServerNameById(getServerId()) + " is now set as disconnected");
			}
			L2AuthServer.getInstance().getGameServerListener().removeGameServer(this);
			L2AuthServer.getInstance().getGameServerListener().removeFloodProtection(_connectionIp);
		}
	}

	public void sendPacket(ServerBasePacket sl)
	{
		try
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
		catch (IOException e)
		{
			_log.error("Error while sending packet " + sl.getClass().getSimpleName() + " to server " + _gsi.getId(), e);
		}
	}

	public void setGameHosts(String gameExternalHost, String gameInternalHost)
	{
		String oldInternal = _gsi.getInternalHost();
		String oldExternal = _gsi.getExternalHost();

		_gsi.setExternalHost(gameExternalHost);
		_gsi.setInternalIp(gameInternalHost);

		if (!gameExternalHost.equals("*"))
		{
			try
			{
				_gsi.setExternalIp(InetAddress.getByName(gameExternalHost).getHostAddress());
			}
			catch (UnknownHostException e)
			{
				_log.warn("Couldn't resolve hostname \"" + gameExternalHost + "\"");
			}
		}
		else
		{
			_gsi.setExternalIp(_connectionIp);
		}

		if (!gameInternalHost.equals("*"))
		{
			try
			{
				_gsi.setInternalIp(InetAddress.getByName(gameInternalHost).getHostAddress());
			}
			catch (UnknownHostException e)
			{
				_log.warn("Couldn't resolve hostname \"" + gameInternalHost + "\"");
			}
		}
		else
		{
			_gsi.setInternalIp(_connectionIp);
		}

		_log.info("Hooked GameServer: [" + getServerId() + "] " + GameServerManager.getInstance().getServerNameById(getServerId()));
		_log.info("Internal/External IP(s): " + (oldInternal == null ? gameInternalHost : oldInternal) + "/" + (oldExternal == null ? gameExternalHost : oldExternal));
	}

	public void setGameServerInfo(GameServerInfo gsi)
	{
		_gsi = gsi;
	}

}