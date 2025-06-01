package com.dream.auth;

import com.dream.AuthConfig;
import com.dream.L2AuthDatabaseFactory;
import com.dream.annotations.L2Properties;
import com.dream.auth.manager.AuthManager;
import com.dream.auth.manager.BanManager;
import com.dream.auth.manager.GameServerManager;
import com.dream.auth.thread.GameServerListener;
import com.dream.mmocore.SelectorConfig;
import com.dream.mmocore.SelectorThread;
import com.dream.tools.network.Util;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class L2AuthServer
{
	
	public static final int PROTOCOL_REV = 0x102;
	private static final Logger LOGGER = Logger.getLogger(L2AuthServer.class);
	
	private static L2AuthServer instance;
	private static long startupTime;
	
	private SelectorThread<L2AuthClient> selectorThread;
	private GameServerListener gameServerListener;
	
	public static void main(String[] args) throws SQLException
	{
		if (!GraphicsEnvironment.isHeadless())
		{
			new L2InterfaceLS();
		}
		
		parseArguments(args);
		instance = new L2AuthServer();
	}
	
	private static void parseArguments(String[] args)
	{
		for (String arg : args)
		{
			if (arg.startsWith("--config-dir"))
			{
				try
				{
					String configDir = arg.split("=")[1];
					File configFolder = new File(configDir);
					if (configFolder.isDirectory())
					{
						System.out.println("Using configuration folder: " + configDir);
						L2Properties.CONFIG_DIR = configDir;
					}
				}
				catch (Exception e)
				{
					LOGGER.debug("Failed to parse config-dir argument", e);
				}
			}
		}
	}
	
	public L2AuthServer() throws SQLException
	{
		startupTime = System.currentTimeMillis();
		
		setupLogging();
		loadConfiguration();
		registerShutdownHook();
		
		initializeNetwork();
		startGameServerListener();
		bindAndStartSelector();
		
		printServerInfo();
	}
	
	public void setupLogging()
	{
		new File("log").mkdirs();
		DOMConfigurator.configure("./config/log4j.xml");
	}
	
	public void loadConfiguration() throws SQLException
	{
		AuthConfig.load();
		L2AuthDatabaseFactory.getInstance();
		GameServerManager.getInstance();
		ClientManager.getInstance();
		AuthManager.load();
		BanManager.getInstance();
	}
	
	public void registerShutdownHook()
	{
		Runtime.getRuntime().addShutdownHook(new Thread(() -> LOGGER.info("Auth server shutting down")));
	}
	
	private void initializeNetwork()
	{
		SelectorConfig config = new SelectorConfig();
		config.MAX_READ_PER_PASS = AuthConfig.MMO_MAX_READ_PER_PASS;
		config.MAX_SEND_PER_PASS = AuthConfig.MMO_MAX_SEND_PER_PASS;
		config.SLEEP_TIME = AuthConfig.MMO_SELECTOR_SLEEP_TIME;
		config.HELPER_BUFFER_COUNT = AuthConfig.MMO_HELPER_BUFFER_COUNT;
		
		try
		{
			SelectorHelper helper = new SelectorHelper();
			selectorThread = new SelectorThread<>(config, helper, new L2AuthPacketHandler(), helper, helper);
		}
		catch (IOException e)
		{
			LOGGER.fatal("Failed to initialize network selector", e);
			throw new RuntimeException("Failed to start selector thread", e);
		}
	}
	
	private void startGameServerListener()
	{
		gameServerListener = new GameServerListener();
		gameServerListener.start();
		LOGGER.info("Listening for GameServers on " + AuthConfig.LOGIN_HOSTNAME + ":" + AuthConfig.LOGIN_PORT);
	}
	
	private void bindAndStartSelector()
	{
		InetAddress bindAddress = null;
		
		if (!"*".equals(AuthConfig.LOGIN_SERVER_HOSTNAME))
		{
			try
			{
				bindAddress = InetAddress.getByName(AuthConfig.LOGIN_SERVER_HOSTNAME);
			}
			catch (UnknownHostException e)
			{
				LOGGER.warn("Invalid bind address, defaulting to all interfaces: " + e.getMessage());
			}
		}
		
		try
		{
			selectorThread.openServerSocket(bindAddress, AuthConfig.LOGIN_SERVER_PORT);
			selectorThread.start();
		}
		catch (IOException e)
		{
			LOGGER.fatal("Failed to open server socket.", e);
			throw new RuntimeException("Failed to bind server socket", e);
		}
		
		
	}
	
	private static void printServerInfo()
	{
		long loadTime = (System.currentTimeMillis() - startupTime) / 1000;
		
		Util.printSection("Server Info");
		LOGGER.info("Memory: Free " + getFreeMemory() + " MB of " + getTotalMemory() + " MB. Used " + getUsedMemory() + " MB.");
		LOGGER.info("Ready on IP: " + AuthConfig.LOGIN_SERVER_HOSTNAME + ":" + AuthConfig.LOGIN_SERVER_PORT);
		LOGGER.info("Load time: " + loadTime + " seconds.");
		Util.printSection("Live");
		LOGGER.info("Auth Server successfully started.");
	}
	
	public static L2AuthServer getInstance()
	{
		return instance;
	}
	
	public GameServerListener getGameServerListener()
	{
		return gameServerListener;
	}
	
	/** @return Free memory in MB */
	public static long getFreeMemory()
	{
		return (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory()) / 1048576;
	}
	
	/** @return Used memory in MB */
	public static long getUsedMemory()
	{
		return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
	}
	
	/** @return Total max memory available to the JVM in MB */
	public static long getTotalMemory()
	{
		return Runtime.getRuntime().maxMemory() / 1048576;
	}
}
