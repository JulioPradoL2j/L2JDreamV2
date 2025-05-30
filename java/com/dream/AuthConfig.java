package com.dream;

import com.dream.annotations.L2Properties;
import com.dream.tools.network.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class AuthConfig extends L2Config
{
	public static enum OnSuccessLoginAction
	{
		COMMAND,
		NOTIFY
	}
	
	public static final String NETWORK_FILE = "./config/network.properties";
	
	public static String DATABASE_DRIVER;
	
	public static String DATABASE_URL;
	
	public static String DATABASE_LOGIN;
	
	public static String DATABASE_PASSWORD;
	public static String LOGIN_SERVER_HOSTNAME;
	public static String LOGIN_HOSTNAME;
	public static int LOGIN_SERVER_PORT;
	public static int LOGIN_PORT;
	
	public static int IP_UPDATE_TIME;
	
	public static final String LOGIN_FILE = "./config/authserver.properties";
	
	public static boolean SHOW_LICENCE;
	
	public static boolean ACCEPT_NEW_GAMESERVER;
	public static boolean AUTO_CREATE_ACCOUNTS;
	public static int GM_MIN;
	public static boolean BRUT_PROTECTION_ENABLED;
	public static boolean DDOS_PROTECTION_ENABLED;
	public static long SESSION_TTL;
	public static int MAX_SESSIONS;
	public static OnSuccessLoginAction ON_SUCCESS_LOGIN_ACTION;
	public static String ON_SUCCESS_LOGIN_COMMAND;
	public static String BRUTE_ACCOUNT_NAME;
	public static int LOGIN_TRY_BEFORE_BAN;
	
	public static int LOGIN_BLOCK_AFTER_BAN;
	public static int LOGIN_MAX_ACC_REG;
	public static int INACTIVE_TIMEOUT;
	public static int FAST_CONNECTION_LIMIT;
	
	public static int NORMAL_CONNECTION_TIME;
	public static int FAST_CONNECTION_TIME;
	public static int MAX_CONNECTION_PER_IP;
	public static boolean FLOOD_PROTECTION;
	public static boolean DEBUG;
	public static int MMO_SELECTOR_SLEEP_TIME = 20; // default 20
	public static int MMO_MAX_SEND_PER_PASS = 12; // default 12
	
	public static int MMO_MAX_READ_PER_PASS = 12; // default 12
	
	public static int MMO_HELPER_BUFFER_COUNT = 20; // default 20
	
	public static void debug(String msg)
	{
		if (DEBUG)
		{
			System.out.println("!!!! " + msg);
		}
	}
	
	public static void load()
	{
		loadNetworkConfig();
		loadLoginConfig();
		Util.printSection("AuthServer DataBase Load");
	}
	
	public static void loadLoginConfig()
	{
		try
		{
			Properties serverSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(LOGIN_FILE)))
			{
				
				ACCEPT_NEW_GAMESERVER = Boolean.parseBoolean(serverSettings.getProperty("AcceptNewGameServer", "false"));
				GM_MIN = Integer.parseInt(serverSettings.getProperty("GMMinLevel", "1"));
				SHOW_LICENCE = Boolean.parseBoolean(serverSettings.getProperty("ShowLicence", "true"));
				AUTO_CREATE_ACCOUNTS = Boolean.parseBoolean(serverSettings.getProperty("AutoCreateAccounts", "false"));
				BRUT_PROTECTION_ENABLED = Boolean.parseBoolean(serverSettings.getProperty("BrutProtection", "true"));
				DDOS_PROTECTION_ENABLED = Boolean.parseBoolean(serverSettings.getProperty("DDoSProtection", "true"));
				SESSION_TTL = Long.parseLong(serverSettings.getProperty("SessionTTL", "10")) * 1000;
				MAX_SESSIONS = Integer.parseInt(serverSettings.getProperty("MaxSessions", "100"));
				ON_SUCCESS_LOGIN_ACTION = OnSuccessLoginAction.valueOf(serverSettings.getProperty("OnSelectServer", "NOTIFY").toUpperCase());
				ON_SUCCESS_LOGIN_COMMAND = serverSettings.getProperty("OnSelectServerCommand", "");
				BRUTE_ACCOUNT_NAME = serverSettings.getProperty("BruteAccountName", "");
				
				LOGIN_TRY_BEFORE_BAN = Integer.parseInt(serverSettings.getProperty("LoginTryBeforeBan", "3"));
				LOGIN_BLOCK_AFTER_BAN = Integer.parseInt(serverSettings.getProperty("LoginBlockAfterBan", "600"));
				LOGIN_MAX_ACC_REG = Integer.parseInt(serverSettings.getProperty("MaxAccountRegistration", "30"));
				
				FLOOD_PROTECTION = Boolean.parseBoolean(serverSettings.getProperty("EnableFloodProtection", "true"));
				FAST_CONNECTION_LIMIT = Integer.parseInt(serverSettings.getProperty("FastConnectionLimit", "15"));
				NORMAL_CONNECTION_TIME = Integer.parseInt(serverSettings.getProperty("NormalConnectionTime", "700"));
				FAST_CONNECTION_TIME = Integer.parseInt(serverSettings.getProperty("FastConnectionTime", "350"));
				MAX_CONNECTION_PER_IP = Integer.parseInt(serverSettings.getProperty("MaxConnectionPerIP", "50"));
				INACTIVE_TIMEOUT = Integer.parseInt(serverSettings.getProperty("InactiveTimeOut", "3"));
				if (serverSettings.containsKey("Debug"))
				{
					DEBUG = Boolean.parseBoolean(serverSettings.getProperty("Debug"));
				}
				
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + LOGIN_FILE + " File.");
		}
	}
	
	public static void loadNetworkConfig()
	{
		try
		{
			L2Properties networkSettings = new L2Properties(NETWORK_FILE);
			
			IP_UPDATE_TIME = Integer.parseInt(networkSettings.getProperty("IpUpdateTime", "0")) * 60 * 1000;
			LOGIN_SERVER_PORT = Integer.parseInt(networkSettings.getProperty("AuthServerPort", "2106"));
			LOGIN_HOSTNAME = networkSettings.getProperty("AuthHostName", "127.0.0.1");
			LOGIN_SERVER_HOSTNAME = networkSettings.getProperty("AuthServerHostName", "0.0.0.0");
			LOGIN_PORT = Integer.parseInt(networkSettings.getProperty("AuthPort", "9014"));
			DATABASE_DRIVER = networkSettings.getProperty("Driver", "com.mysql.jdbc.Driver");
			DATABASE_URL = networkSettings.getProperty("URL", "jdbc:mysql://localhost/dream");
			DATABASE_LOGIN = networkSettings.getProperty("Login", "root");
			DATABASE_PASSWORD = networkSettings.getProperty("Password", "root");
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + NETWORK_FILE + " File.");
		}
	}
}