package com.dream.auth;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.interfaces.RSAPrivateKey;

import org.apache.log4j.Logger;

import com.dream.AuthConfig;
import com.dream.auth.crypt.AuthCrypt;
import com.dream.auth.manager.AuthManager;
import com.dream.auth.model.Account;
import com.dream.auth.model.SessionKey;
import com.dream.auth.network.serverpackets.AuthFail;
import com.dream.auth.network.serverpackets.AuthFailReason;
import com.dream.auth.network.serverpackets.L2AuthServerPacket;
import com.dream.auth.network.serverpackets.PlayFail;
import com.dream.auth.network.serverpackets.PlayFailReason;
import com.dream.mmocore.MMOClient;
import com.dream.mmocore.MMOConnection;
import com.dream.mmocore.SendablePacket;
import com.dream.tools.math.ScrambledKeyPair;
import com.dream.tools.random.Rnd;

public class L2AuthClient extends MMOClient<MMOConnection<L2AuthClient>>
{
	public static enum LoginClientState
	{
		CONNECTED,
		AUTHED_GG,
		AUTHED_CARD,
		AUTHED_LOGIN;
	}

	private static final Logger _log = Logger.getLogger(L2AuthClient.class);

	private LoginClientState _state = LoginClientState.CONNECTED;

	private AuthCrypt _loginCrypt;
	private final ScrambledKeyPair _scrambledPair;
	private final byte[] _blowfishKey;
	private int _lastServer;
	private String _account = null;
	private int _accessLevel;
	private int _lastServerId;
	private SessionKey _sessionKey;
	private boolean _usesInternalIP;
	private boolean _joinedGS;
	public boolean checkOK = false;
	private final int _sessionId;
	private final long _connectionStartTime;

	public Account _accInfo;

	public L2AuthClient(MMOConnection<L2AuthClient> con)
	{
		super(con);
		_state = LoginClientState.CONNECTED;
		String ip = getConnection().getInetAddress().getHostAddress();

		if (ip.startsWith("192.168") || ip.startsWith("10.0") || ip.equals("127.0.0.1"))
		{
			_usesInternalIP = true;
		}

		_scrambledPair = AuthManager.getInstance().getScrambledRSAKeyPair();
		_blowfishKey = AuthManager.getInstance().getBlowfishKey();
		_sessionId = Rnd.nextInt();
		_connectionStartTime = System.currentTimeMillis();
		_loginCrypt = new AuthCrypt();
		_loginCrypt.setKey(_blowfishKey);
		if (AuthConfig.DDOS_PROTECTION_ENABLED)
		{
			L2AuthPacketHandler.getInstance().addClient(this);
		}
		else
		{
			checkOK = true;
		}
	}

	public void close(AuthFailReason reason)
	{
		getConnection().close(new AuthFail(reason));
	}

	public void close(L2AuthServerPacket lsp)
	{
		getConnection().close(lsp);
	}

	public void close(PlayFailReason reason)
	{
		getConnection().close(new PlayFail(reason));
	}

	@Override
	public boolean decrypt(ByteBuffer buf, int size)
	{
		boolean ret = false;
		try
		{
			ret = getLoginCrypt().decrypt(buf.array(), buf.position(), size);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			super.getConnection().close((SendablePacket<L2AuthClient>) null);
			return false;
		}

		if (!ret)
		{
			byte[] dump = new byte[size];
			System.arraycopy(buf.array(), buf.position(), dump, 0, size);
			_log.warn("Wrong checksum from client: " + toString());
			super.getConnection().close((SendablePacket<L2AuthClient>) null);
		}

		return ret;
	}

	@Override
	public boolean encrypt(ByteBuffer buf, int size)
	{
		final int offset = buf.position();
		try
		{
			size = getLoginCrypt().encrypt(buf.array(), offset, size);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}

		buf.position(offset + size);
		return true;
	}

	public int getAccessLevel()
	{
		return _accessLevel;
	}

	public String getAccount()
	{
		return _account;
	}

	public byte[] getBlowfishKey()
	{
		return _blowfishKey;
	}

	public long getConnectionStartTime()
	{
		return _connectionStartTime;
	}

	public InetAddress getInetAddress()
	{
		return getConnection().getInetAddress();
	}

	public int getLastServer()
	{
		return _lastServer;
	}

	public int getLastServerId()
	{
		return _lastServerId;
	}

	private AuthCrypt getLoginCrypt()
	{
		if (_loginCrypt == null)
		{
			_loginCrypt = new AuthCrypt();
			_loginCrypt.setKey(_blowfishKey);
		}

		return _loginCrypt;
	}

	public RSAPrivateKey getRSAPrivateKey()
	{
		return (RSAPrivateKey) _scrambledPair.getPair().getPrivate();
	}

	public byte[] getScrambledModulus()
	{
		return _scrambledPair.getScrambledModulus();
	}

	public int getSessionId()
	{
		return _sessionId;
	}

	public SessionKey getSessionKey()
	{
		return _sessionKey;
	}

	public LoginClientState getState()
	{
		return _state;
	}

	public boolean hasJoinedGS()
	{
		return _joinedGS;
	}

	@Override
	public void onDisconnection()
	{
		if (getState() == LoginClientState.AUTHED_LOGIN && !hasJoinedGS())
		{
			AuthManager.getInstance().removeAuthedLoginClient(getAccount());
		}
	}

	@Override
	protected void onForcedDisconnection()
	{

	}

	public void sendPacket(L2AuthServerPacket lsp)
	{
		getConnection().sendPacket(lsp);
	}

	public void setAccessLevel(int accessLevel)
	{
		_accessLevel = accessLevel;
	}

	public void setAccount(String account)
	{
		_account = account;
		if (account != null)
		{
			_accInfo = AuthManager.getInstance().getAccount(_account);
		}
	}

	public void setJoinedGS(boolean val)
	{
		_joinedGS = val;
	}

	public void setLastServer(int lastServer)
	{
		_lastServer = lastServer;
	}

	public void setLastServerId(int lastServerId)
	{
		_lastServerId = lastServerId;
	}

	public void setSessionKey(SessionKey sessionKey)
	{
		_sessionKey = sessionKey;
	}

	public void setState(LoginClientState state)
	{
		_state = state;
	}

	@Override
	public String toString()
	{
		InetAddress address = getConnection().getInetAddress();
		if (getState() == LoginClientState.AUTHED_LOGIN)
			return "[" + getAccount() + " (" + (address == null ? "disconnected" : address.getHostAddress()) + ")]";
		return "[" + (address == null ? "disconnected" : address.getHostAddress()) + "]";
	}

	public boolean usesInternalIP()
	{
		return _usesInternalIP;
	}

}