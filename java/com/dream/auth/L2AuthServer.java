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

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class L2AuthServer
{
	public static final int PROTOCOL_REV = 0x102;
	
	public static Logger _log = Logger.getLogger(L2AuthServer.class);
	
	private static double _intialTime = 0;
	
	private static L2AuthServer _instance;
	
	public static long getFreeMemory()
	{
		return (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory()) / 1048576;
	}
	
	public static L2AuthServer getInstance()
	{
		return _instance;
	}
	
	public static long getTotalMemory()
	{
		return Runtime.getRuntime().maxMemory() / 1048576;
	}
	
	public static long getUsedMemory()
	{
		return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
	}
	
	public static void main(String[] args) throws Throwable
	{
		if (!GraphicsEnvironment.isHeadless())
		{
			new L2InterfaceLS();
		}
		
		for (String arg : args)
			if (arg.startsWith("--config-dir"))
			{
				try
				{
					String cdir = arg.split("=")[1];
					File f = new File(cdir);
					if (f.exists() && f.isDirectory())
					{
						System.out.println("Using configuration folder " + cdir);
						L2Properties.CONFIG_DIR = cdir;
					}
				}
				catch (Exception e)
				{
					
				}
			}
		
		
		_instance = new L2AuthServer();
	}
	
	private static void printInfo()
	{
		double finalTime = System.currentTimeMillis();
		
		_log.info("Memory: Free " + getFreeMemory() + " MB of " + getTotalMemory() + " MB. Used " + getUsedMemory() + " MB.");
		_log.info("Ready on IP: " + AuthConfig.LOGIN_SERVER_HOSTNAME + ":" + AuthConfig.LOGIN_SERVER_PORT + ".");
		_log.info("Load time: " + (int) ((finalTime - _intialTime) / 1000) + " Seconds.");
		Util.printSection("Live");
		_log.info("Auth Server successfully started.");
	}
	
	private GameServerListener _gameServerListener;
	
	private SelectorThread<L2AuthClient> _selectorThread;
	
	public L2AuthServer() throws Throwable
	{
		
		new File("log").mkdirs();
		DOMConfigurator.configure("./config/log4j.xml");
		
		_intialTime = System.currentTimeMillis();
		
		AuthConfig.load();
		L2AuthDatabaseFactory.getInstance();
		
		GameServerManager.getInstance();
		ClientManager.getInstance();
		AuthManager.load();
		
		BanManager.getInstance();
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			@Override
			public void run()
			{
				_log.info("Auth server shutting down");
			}
		});
		initNetworkLayer();
		initGSListener();
		startServer();
		
		Util.printSection("Server Info");
		printInfo();
	}
	
	public GameServerListener getGameServerListener()
	{
		return _gameServerListener;
	}
	
	private void initGSListener()
	{
		_gameServerListener = new GameServerListener();
		_gameServerListener.start();
		_log.info("Listening for GameServers on " + AuthConfig.LOGIN_HOSTNAME + ":" + AuthConfig.LOGIN_PORT);
	}
	
	private void initNetworkLayer()
	{
		final SelectorConfig sc = new SelectorConfig();
		sc.MAX_READ_PER_PASS = AuthConfig.MMO_MAX_READ_PER_PASS;
		sc.MAX_SEND_PER_PASS = AuthConfig.MMO_MAX_SEND_PER_PASS;
		sc.SLEEP_TIME = AuthConfig.MMO_SELECTOR_SLEEP_TIME;
		sc.HELPER_BUFFER_COUNT = AuthConfig.MMO_HELPER_BUFFER_COUNT;
		
		final L2AuthPacketHandler lph = new L2AuthPacketHandler();
		final SelectorHelper sh = new SelectorHelper();
		try
		{
			_selectorThread = new SelectorThread<>(sc, sh, lph, sh, sh);
		}
		catch (IOException e)
		{
			_log.fatal("FATAL: Failed to open Selector. Reason: " + e.getMessage(), e);
			System.exit(1);
		}
	}
	
	private void startServer()
	{
		InetAddress bindAddress = null;
		if (!AuthConfig.LOGIN_HOSTNAME.equals("*"))
		{
			try
			{
				bindAddress = InetAddress.getByName(AuthConfig.LOGIN_HOSTNAME);
			}
			catch (UnknownHostException e1)
			{
				_log.fatal("WARNING: The Autherver bind address is invalid, using all available IPs. Reason: " + e1.getMessage());
			}
		}
		
		try
		{
			_selectorThread.openServerSocket(bindAddress, AuthConfig.LOGIN_SERVER_PORT);
		}
		catch (IOException e)
		{
			_log.fatal("FATAL: Failed to open server socket. Reason: " + e.getMessage(), e);
			System.exit(1);
		}
		_selectorThread.start();
	}
}