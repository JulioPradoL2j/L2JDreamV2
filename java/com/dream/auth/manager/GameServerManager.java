package com.dream.auth.manager;

import com.dream.L2AuthDatabaseFactory;
import com.dream.auth.model.GameServerInfo;
import com.dream.tools.random.Rnd;
import com.dream.tools.util.HexUtil;

import java.io.File;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.RSAKeyGenParameterSpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class GameServerManager
{
	private static final Logger _log = Logger.getLogger(GameServerManager.class);

	private static GameServerManager __instance = null;

	private static Map<Integer, String> _serverNames = new HashMap<>();
	private static final int KEYS_SIZE = 10;

	public static GameServerManager getInstance()
	{
		if (__instance == null)
		{
			try
			{
				__instance = new GameServerManager();
			}
			catch (NoSuchAlgorithmException e)
			{
				_log.fatal("FATAL: Failed loading GameServerManager. Reason: " + e.getMessage(), e);
				System.exit(1);
			}
			catch (InvalidAlgorithmParameterException e)
			{
				_log.fatal("FATAL: Failed loading GameServerManager. Reason: " + e.getMessage(), e);
				System.exit(1);
			}
		}
		return __instance;
	}

	private final Map<Integer, GameServerInfo> _gameServerTable = new ConcurrentHashMap<>();

	private KeyPair[] _keyPairs;

	private GameServerManager() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException
	{
		load();
		loadRSAKeys();
	}

	public void deleteAllServer()
	{
		for (Integer serverId : _gameServerTable.keySet())
		{
			deleteServer(serverId);
		}

	}


	public void deleteServer(int id)
	{
		try
		{
			Connection con = L2AuthDatabaseFactory.getInstance().getConnection();
			PreparedStatement stm = con.prepareStatement("DELETE FROM gameservers where server_id = ?");
			stm.setInt(1, id);
			stm.execute();
			stm.close();
			con.close();
			_gameServerTable.remove(id);
		}
		catch (SQLException e)
		{
			_log.error("ServerManager: Unable to delete gameserver.", e);
		}
	}

	public KeyPair getKeyPair()
	{
		return _keyPairs[Rnd.nextInt(10)];
	}

	public GameServerInfo getRegisteredGameServerById(int id)
	{
		return _gameServerTable.get(id);
	}

	public Map<Integer, GameServerInfo> getRegisteredGameServers()
	{
		return _gameServerTable;
	}

	public String getServerNameById(int id)
	{
		return getServerNames().get(id);
	}

	public Map<Integer, String> getServerNames()
	{
		return _serverNames;
	}

	public boolean hasRegisteredGameServerOnId(int id)
	{
		return _gameServerTable.containsKey(id);
	}


	private void load()
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		try
		{
			Document doc = factory.newDocumentBuilder().parse(new File("./config/servername.xml"));
			Node names = doc.getFirstChild();
			if (names != null)
			{
				for (Node n = names.getFirstChild(); n != null; n = n.getNextSibling())
					if (n.getNodeName().equals("server"))
					{
						NamedNodeMap attr = n.getAttributes();
						_serverNames.put(Integer.parseInt(attr.getNamedItem("id").getNodeValue()), attr.getNamedItem("name").getNodeValue());
					}
			}
			_log.info("ServerManager: Loaded " + _serverNames.size() + " server name(s)");
		}
		catch (Exception e)
		{
			_log.warn("ServerManager: Unable to load  servernames.xml", e);
		}

		try (Connection con = L2AuthDatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM gameservers");
			ResultSet rset = statement.executeQuery();

			int id;
			while (rset.next())
			{
				id = rset.getInt("server_id");
				_gameServerTable.put(id, new GameServerInfo(id, HexUtil.stringToHex(rset.getString("hexid"))));
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("GameServerTable: Error loading registered game servers!");
		}
		_log.info("ServerManager: Loaded " + _gameServerTable.size() + " server(s)");
	}

	private void loadRSAKeys() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException
	{
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(512, RSAKeyGenParameterSpec.F4);
		keyGen.initialize(spec);

		_keyPairs = new KeyPair[KEYS_SIZE];
		for (int i = 0; i < KEYS_SIZE; i++)
		{
			_keyPairs[i] = keyGen.genKeyPair();
		}

		_log.info("Cached " + _keyPairs.length + " RSA keys for Game Server communication.");
	}

	public boolean register(int id, GameServerInfo gsi)
	{
		synchronized (_gameServerTable)
		{
			if (!_gameServerTable.containsKey(id))
			{
				_gameServerTable.put(id, gsi);
				gsi.setId(id);
				return true;
			}
		}
		return false;
	}


	public void registerServerOnDB(byte[] hexId, int id, String externalHost)
	{
		try (Connection con = L2AuthDatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("INSERT INTO gameservers (hexid,server_id,host) values (?,?,?)");
			statement.setString(1, HexUtil.hexToString(hexId));
			statement.setInt(2, id);
			statement.setString(3, externalHost);
			statement.executeUpdate();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warn("GameServerTable: SQL error while saving gameserver: " + e);
		}
	}

	public void registerServerOnDB(GameServerInfo gsi)
	{
		this.registerServerOnDB(gsi.getHexId(), gsi.getId(), gsi.getExternalHost());
	}

	public boolean registerWithFirstAvailableId(GameServerInfo gsi)
	{
		return false;
	}

}