package com.dream.auth.manager;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import javolution.util.FastMap;

public class BanManager
{
	private static BanManager _instance = null;

	private static final Logger _log = Logger.getLogger(BanManager.class);
	public static String BAN_LIST = "config/banned_ip.cfg";

	private static final String ENCODING = "UTF-8";

	public static BanManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new BanManager();
			return _instance;
		}
		return _instance;
	}

	private final Map<String, Long> _bannedIps = new FastMap<String, Long>().shared();

	private BanManager()
	{
		load();
	}

	public void addBanForAddress(InetAddress address, long duration)
	{
		_bannedIps.put(address.getHostAddress(), System.currentTimeMillis() + duration);
	}

	public void addBanForAddress(String address, long expiration)
	{
		_bannedIps.put(address, expiration);
	}

	private void addBannedIP(String line)
	{
		String[] parts;
		parts = line.split("#");

		line = parts[0];

		parts = line.split(" ");

		String address = parts[0];

		long duration = 0;

		if (parts.length > 1)
		{
			try
			{
				duration = Long.parseLong(parts[1]);
			}
			catch (NumberFormatException e)
			{
				_log.warn("Skipped: Incorrect ban duration (" + parts[1] + ") on Line: " + line);
				return;
			}
		}

		addBanForAddress(address, duration);
	}

	public int getNbOfBannedIp()
	{
		return _bannedIps.size();
	}

	public boolean isBannedAddress(InetAddress address)
	{

		if (_bannedIps.containsKey(address.getHostAddress()) && _bannedIps.get(address.getHostAddress()) > System.currentTimeMillis())
			return true;
		if (_bannedIps.containsKey(address.getHostAddress()))
		{
			_bannedIps.remove(address.getHostAddress());
		}
		return false;
	}

	public void load()
	{
		try
		{
			_bannedIps.clear();
			File file = new File(BAN_LIST);
			List<?> lines = FileUtils.readLines(file, ENCODING);

			for (int i = 0; i < lines.size(); i++)
			{
				String line = (String) lines.get(i);
				line = line.trim();
				if (line.length() > 0 && !line.startsWith("#"))
				{
					addBannedIP(line);
				}
			}
			_log.info("BanManager: Loaded " + getNbOfBannedIp() + " banned ip/subnet(s).");
		}
		catch (IOException e)
		{
			_log.warn("error while reading banned file:" + e);
		}
	}

	public boolean removeBanForAddress(InetAddress address)
	{
		return _bannedIps.remove(address.getHostAddress()) != null;
	}

	public boolean removeBanForAddress(String address)
	{
		try
		{
			return this.removeBanForAddress(InetAddress.getByName(address));
		}
		catch (UnknownHostException e)
		{
			return false;
		}
	}
}