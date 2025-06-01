package com.dream.auth.manager;

import com.dream.AuthConfig;
import com.dream.L2AuthDatabaseFactory;
import com.dream.auth.L2AuthClient;
import com.dream.auth.model.Account;
import com.dream.auth.model.GameServerInfo;
import com.dream.auth.model.SessionKey;
import com.dream.auth.network.gameserverpackets.ServerStatus;
import com.dream.auth.services.AccountBannedException;
import com.dream.auth.services.AccountModificationException;
import com.dream.auth.services.AccountWrongPasswordException;
import com.dream.auth.thread.GameServerThread;
import com.dream.tools.codec.Base64;
import com.dream.tools.math.ScrambledKeyPair;
import com.dream.tools.random.Rnd;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.crypto.Cipher;

import org.apache.log4j.Logger;

import javolution.util.FastSet;

public class AuthManager
{
	public static enum AuthLoginResult
	{
		INVALID_PASSWORD,
		ACCOUNT_BANNED,
		ALREADY_ON_LS,
		ALREADY_ON_GS,
		AUTH_SUCCESS,
		SYSTEM_ERROR,
		TCARD_REQUIRED
	}
	
	public class ConnectionCheck extends Thread
	{
		@Override
		public void run()
		{
			for (;;)
			{
				try
				{
					PreparedStatement stm = con.prepareStatement("SELECT 1");
					stm.executeQuery().close();
					stm.close();
				}
				catch (SQLException e)
				{
					return;
				}
				try
				{
					Thread.sleep(10000);
				}
				catch (InterruptedException ie)
				{
					return;
				}
			}
		}
	}
	
	private static final Logger _log = Logger.getLogger(AuthManager.class);
	
	private static final Logger _logLogin = Logger.getLogger("auth");
	
	private static final Logger _logLoginFailed = Logger.getLogger("fail");
	
	private static AuthManager _instance;
	
	private static final int BLOWFISH_KEYS = 20;
	
	private static Map<String, Integer> _registredAccounts = new HashMap<>();
	
	private static void checkPassword(byte[] hash, Account acc) throws AccountWrongPasswordException
	{
		byte[] expected = Base64.decode(acc.getPassword());
		
		for (int i = 0; i < expected.length; i++)
			if (hash[i] != expected[i])
				throw new AccountWrongPasswordException(acc.getLogin());
	}
	
	public static AuthManager getInstance()
	{
		return _instance;
	}
	
	private static void handleBadLogin(String user, String password, InetAddress address)
	{
		_logLoginFailed.info("Auth failed for user : '" + user + "' " + (address == null ? "null" : address.getHostAddress()));
	}
	
	public static boolean isValidLogin(String text)
	{
		return isValidPattern(text, "^[A-Za-z0-9]{1,16}$");
	}
	
	public static boolean isValidPattern(String text, String regex)
	{
		Pattern pattern;
		
		try
		{
			pattern = Pattern.compile(regex);
		}
		catch (PatternSyntaxException e)
		{
			pattern = Pattern.compile(".*");
		}
		Matcher regexp = pattern.matcher(text);
		return regexp.matches();
	}
	
	public static void load()
	{
		if (_instance == null)
		{
			_instance = new AuthManager();
		}
		else
			throw new IllegalStateException("AuthManager can only be loaded a single time.");
	}
	
	private static void testCipher(RSAPrivateKey key) throws GeneralSecurityException
	{
		Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
		rsaCipher.init(Cipher.DECRYPT_MODE, key);
	}
	
	protected Map<String, L2AuthClient> _loginServerClients = new HashMap<>();
	
	protected Set<L2AuthClient> _clients = new FastSet<>();
	
	private ScrambledKeyPair[] _keyPairs;
	
	protected byte[][] _blowfishKeys;
	
	public Connection con;
	
	private KeyPairGenerator keygen;
	
	private AuthManager()
	{
		try
		{
			_log.info("AuthManager initiating");
			
			con = L2AuthDatabaseFactory.getInstance().getConnection();
			new ConnectionCheck().start();
			_keyPairs = new ScrambledKeyPair[10];
			
			keygen = null;
			
			try
			{
				keygen = KeyPairGenerator.getInstance("RSA");
				RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4);
				keygen.initialize(spec);
			}
			catch (GeneralSecurityException e)
			{
				_log.fatal("Error in RSA setup:" + e);
				_log.info("Server shutting down now");
				System.exit(1);
			}
			
			for (int i = 0; i < 10; i++)
			{
				_keyPairs[i] = new ScrambledKeyPair(keygen.generateKeyPair());
			}
			
			_log.info("Cached 10 KeyPairs for RSA communication");
			
			testCipher((RSAPrivateKey) _keyPairs[0].getPair().getPrivate());
			
			generateBlowFishKeys();
		}
		catch (Exception e)
		{
			_log.fatal("FATAL: Failed initializing LoginManager. Reason: " + e.getMessage(), e);
			System.exit(1);
		}
		
	}
	
	public synchronized void addOrUpdateAccount(Account acc)
	{
		PreparedStatement stm = null;
		try
		{
			stm = con.prepareStatement("update accounts set password = ?, lastactive = ?, accessLevel = ?, lastIP = ?, lastServerId = ? where login = ?");
			stm.setString(1, acc.getPassword());
			stm.setLong(2, acc.getLastactive());
			stm.setInt(3, acc.getAccessLevel());
			stm.setString(4, acc.getLastIp());
			stm.setInt(5, acc.getLastServerId());
			stm.setString(6, acc.getLogin());
			if (stm.executeUpdate() == 0)
			{
				stm.close();
				stm = con.prepareStatement("insert into accounts (login, password, accessLevel, lastactive) values(?, ?, ?, 0)");
				stm.setString(1, acc.getLogin());
				stm.setString(2, acc.getPassword());
				stm.setInt(3, acc.getAccessLevel());
				if (stm.executeUpdate() != 1)
					return;
			}
		}
		catch (SQLException e)
		{
			_log.error("AuthManager: Unable to modify account", e);
		}
		finally
		{
			try
			{
				if (stm != null)
				{
					stm.close();
				}
			}
			catch (SQLException e)
			{
				
			}
			
		}
	}
	
	public void addOrUpdateAccount(String _uname, String _pass, int _level) throws AccountModificationException
	{
		Account acc = getAccount(_uname);
		if (acc == null)
		{
			MessageDigest md;
			byte[] newpass;
			try
			{
				md = MessageDigest.getInstance("SHA");
				newpass = _pass.getBytes("UTF-8");
				newpass = md.digest(newpass);
			}
			catch (NoSuchAlgorithmException e1)
			{
				throw new AccountModificationException("No algorithm to encode password.", e1);
			}
			catch (UnsupportedEncodingException e1)
			{
				throw new AccountModificationException("Unsupported encoding.", e1);
			}
			
			acc = new Account(_uname, Base64.encodeBytes(newpass), _level);
			acc.setAccessLevel(_level);
		}
		addOrUpdateAccount(acc);
	}
	
	public synchronized SessionKey assignSessionKeyToClient(String account, L2AuthClient client)
	{
		SessionKey key;
		
		key = new SessionKey(Rnd.nextInt(Integer.MAX_VALUE), Rnd.nextInt(Integer.MAX_VALUE), Rnd.nextInt(Integer.MAX_VALUE), Rnd.nextInt(Integer.MAX_VALUE));
		try
		{
			_loginServerClients.put(account, client);
		}
		catch (NullPointerException e)
		{
			if (_loginServerClients == null)
			{
				_loginServerClients = new HashMap<>();
				_loginServerClients.put(account, client);
			}
			if (account == null)
				return key;
			if (client == null)
				return null;
		}
		return key;
	}
	
	public SessionKey assignSessionKeyToLogin(String account, L2AuthClient client)
	{
		SessionKey key;
		
		key = new SessionKey(Rnd.nextInt(Integer.MAX_VALUE), Rnd.nextInt(Integer.MAX_VALUE), Rnd.nextInt(Integer.MAX_VALUE), Rnd.nextInt(Integer.MAX_VALUE));
		_loginServerClients.put(account, client);
		return key;
	}
	
	public void changeAccountLevel(String _uname, int _level) throws AccountModificationException
	{
		Account acc = getAccount(_uname);
		if (acc == null)
			throw new AccountModificationException();
		acc.setAccessLevel(_level);
		addOrUpdateAccount(acc);
	}
	
	public void changeAllowedIP(String login, String host)
	{
		try
		{
			PreparedStatement statement = con.prepareStatement("UPDATE accounts SET allowed_ip = ? WHERE login = ?");
			statement.setString(1, host);
			statement.setString(2, login);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warn("ChangeAllowedIP: Could not write data. Reason: " + e);
		}
	}
	
	public void deleteAccount(String _uname) throws AccountModificationException
	{
		PreparedStatement stm = null;
		try
		{
			stm = con.prepareStatement("delete from accounts where login=?");
			stm.setString(1, _uname);
			if (stm.executeUpdate() == 0)
				throw new AccountModificationException();
		}
		catch (SQLException e)
		{
			_log.error("AuthManager: Unable to delete account", e);
		}
		finally
		{
			try
			{
				if (stm != null)
				{
					stm.close();
				}
			}
			catch (SQLException e)
			{
				
			}
		}
	}
	
	private void generateBlowFishKeys()
	{
		_blowfishKeys = new byte[BLOWFISH_KEYS][16];
		
		for (int i = 0; i < BLOWFISH_KEYS; i++)
		{
			for (int j = 0; j < _blowfishKeys[i].length; j++)
			{
				_blowfishKeys[i][j] = (byte) (Rnd.nextInt(255) + 1);
			}
		}
		_log.info("Stored " + _blowfishKeys.length + " keys for Blowfish communication");
	}
	
	public Account getAccount(String user)
	{
		Account result = null;
		
		synchronized (con)
		{
			try
			{
				PreparedStatement stm = con.prepareStatement("select * from accounts where login=?");
				stm.setString(1, user);
				ResultSet rs = stm.executeQuery();
				if (rs.next())
				{
					result = new Account(user, rs.getString("password"), rs.getLong("lastactive"), rs.getInt("accessLevel"), rs.getInt("lastServerId"), rs.getString("lastIP"));
				}
				rs.close();
				stm.close();
			}
			catch (SQLException e)
			{
				_log.warn("LoginManager: Unable to retrive account", e);
			}
			
		}
		return result;
	}
	
	public GameServerInfo getAccountOnGameServer(String account)
	{
		Collection<GameServerInfo> serverList = GameServerManager.getInstance().getRegisteredGameServers().values();
		for (GameServerInfo gsi : serverList)
		{
			GameServerThread gst = gsi.getGameServerThread();
			if (gst != null && gst.hasAccountOnGameServer(account))
				return gsi;
		}
		return null;
	}
	
	public List<Account> getAccountsInfo()
	{
		List<Account> result = new ArrayList<>();
		try
		{
			PreparedStatement stm = con.prepareStatement("select login,accessLevel from accounts");
			ResultSet rs = stm.executeQuery();
			while (rs.next())
			{
				result.add(new Account(rs.getString(1), "", rs.getInt(2)));
			}
			rs.close();
			stm.close();
		}
		catch (SQLException e)
		{
			_log.warn("AuthManager: Unable to read accounts", e);
		}
		return result;
	}
	
	public L2AuthClient getAuthedClient(String account)
	{
		return _loginServerClients.get(account);
	}
	
	public byte[] getBlowfishKey()
	{
		return _blowfishKeys[(int) (Math.random() * BLOWFISH_KEYS)];
	}
	
	public SessionKey getKeyForAccount(String account)
	{
		L2AuthClient client = _loginServerClients.get(account);
		if (client != null)
			return client.getSessionKey();
		
		return null;
	}
	
	public int getMaxAllowedOnlinePlayers(int id)
	{
		GameServerInfo gsi = GameServerManager.getInstance().getRegisteredGameServerById(id);
		if (gsi != null)
			return gsi.getMaxPlayers();
		
		return 0;
	}
	
	public int getOnlinePlayerCount(int serverId)
	{
		GameServerInfo gsi = GameServerManager.getInstance().getRegisteredGameServerById(serverId);
		if (gsi != null && gsi.isAuthed())
			return gsi.getCurrentPlayerCount();
		
		return 0;
	}
	
	public ScrambledKeyPair getScrambledRSAKeyPair()
	{
		return _keyPairs[Rnd.nextInt(10)];
	}
	
	private boolean handleAccountNotFound(String user, InetAddress address, byte[] hash)
	{
		if (AuthConfig.AUTO_CREATE_ACCOUNTS)
		{
			
			if (user.length() >= 2 && user.length() <= 14)
			{
				String ip = address.getHostAddress();
				int numTryes = _registredAccounts.getOrDefault(ip, 0);
				numTryes++;
				
				if (numTryes > AuthConfig.LOGIN_MAX_ACC_REG)
				{
					_logLogin.info("Address " + ip + " banned (too many account creations)");
					BanManager.getInstance().addBanForAddress(address, AuthConfig.LOGIN_BLOCK_AFTER_BAN * 1000);
					return false;
				}
				
				_registredAccounts.put(ip, numTryes);
				addOrUpdateAccount(new Account(user, Base64.encodeBytes(hash), System.currentTimeMillis() / 1000, 0, 0, ip));
				_logLogin.info("New account has been created for " + user);
				return true;
			}
			
			_logLogin.warn("Invalid username creation attempt: " + user);
			return false;
		}
		
		_logLogin.warn("Account missing for user " + user);
		return false;
	}
	
	private void handleGoodLogin(String user, InetAddress address)
	{
	    String ip = address.getHostAddress();
	    

	    _registredAccounts.remove(ip);


	    Account acc = getAccount(user);
	    if (acc != null)
	    {
	        acc.setLastactive(System.currentTimeMillis() / 1000);
	        acc.setLastIp(ip);
	        addOrUpdateAccount(acc); 
	    }

	}

	
	public boolean isAccountInAnyGameServer(String account)
	{
		Collection<GameServerInfo> serverList = GameServerManager.getInstance().getRegisteredGameServers().values();
		for (GameServerInfo gsi : serverList)
		{
			GameServerThread gst = gsi.getGameServerThread();
			if (gst != null && gst.hasAccountOnGameServer(account))
				return true;
		}
		return false;
	}
	
	public boolean isAccountInLoginServer(String account)
	{
		return _loginServerClients.containsKey(account);
	}
	
	public boolean isGM(Account acc)
	{
		if (acc != null)
			return acc.getAccessLevel() >= AuthConfig.GM_MIN;
		return false;
	}
	
	public boolean isLoginPossible(int access, int serverId)
	{
		GameServerInfo gsi = GameServerManager.getInstance().getRegisteredGameServerById(serverId);
		if (gsi != null && gsi.isAuthed())
			return gsi.getCurrentPlayerCount() < gsi.getMaxPlayers() && gsi.getStatus() != ServerStatus.STATUS_GM_ONLY || access >= AuthConfig.GM_MIN;
		
		return false;
	}
	
	public boolean loginValid(String user, String password, InetAddress address) throws NoSuchAlgorithmException, UnsupportedEncodingException, AccountBannedException
	{
		MessageDigest md = MessageDigest.getInstance("SHA");
		byte[] raw = password.getBytes("UTF-8");
		byte[] hash = md.digest(raw);
		
		Account acc = getAccount(user);
		
		if (!isValidLogin(user))
			return false;
		
		if (acc == null)
			return handleAccountNotFound(user, address, hash);
		if (acc.getAccessLevel() < 0)
			throw new AccountBannedException(user);
		try
		{
			checkPassword(hash, acc);
			handleGoodLogin(user, address);
		}
		catch (AccountWrongPasswordException e)
		{
			handleBadLogin(user, password, address);
			return false;
		}
		_logLogin.info("User " + user + " connected from " + address.getHostAddress());
		return true;
	}
	
	public boolean loginValid(String user, String password, L2AuthClient client) throws NoSuchAlgorithmException, UnsupportedEncodingException, AccountBannedException
	{
		InetAddress address = client.getConnection().getInetAddress();
		if (address == null || user == null)
			return false;
		
		return loginValid(user, password, address);
	}
	
	public void removeAuthedLoginClient(String account)
	{
		_loginServerClients.remove(account);
	}
	
	public void setAccountLastServerId(String account, int lastServerId)
	{
		Account acc = getAccount(account);
		if (acc != null)
		{
			acc.setLastServerId(lastServerId);
			addOrUpdateAccount(acc);
		}
	}
	
	public AuthLoginResult tryAuthLogin(String account, String password, L2AuthClient client) throws AccountBannedException
	{
		AuthLoginResult ret = AuthLoginResult.INVALID_PASSWORD;
		
		try
		{
			if (loginValid(account, password, client))
			{
				ret = AuthLoginResult.ALREADY_ON_GS;
				
				if (!isAccountInAnyGameServer(account))
				{
					ret = AuthLoginResult.ALREADY_ON_LS;
					AuthConfig.debug(account + " Already on GS");
					
					synchronized (_loginServerClients)
					{
						if (!_loginServerClients.containsKey(account))
						{
							_loginServerClients.put(account, client);
							ret = AuthLoginResult.AUTH_SUCCESS;
						}
					}
					Account acc = getAccount(account);
					
					client.setAccessLevel(acc.getAccessLevel());
					client.setLastServerId(acc.getLastServerId());
				}
				else
				{
					
				}
			}
		}
		catch (NoSuchAlgorithmException e)
		{
			_log.error("could not check password:" + e);
			ret = AuthLoginResult.SYSTEM_ERROR;
		}
		catch (UnsupportedEncodingException e)
		{
			_log.error("could not check password:" + e);
			ret = AuthLoginResult.SYSTEM_ERROR;
		}
		return ret;
	}
}