package com.dream.auth;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.dream.AuthConfig;
import com.dream.auth.network.serverpackets.AuthFailReason;

public class ClientManager extends Thread
{
	private static Logger _log = Logger.getLogger("ClientManager");

	private static ClientManager _instance = null;

	public static ClientManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new ClientManager();
		}
		return _instance;
	}

	private final Map<L2AuthClient, Long> _clients;

	public boolean _running = true;

	private ClientManager()
	{
		_clients = new ConcurrentHashMap<>();
		if (AuthConfig.DDOS_PROTECTION_ENABLED)
		{
			_log.info("DDoS-Proof: Started client manager for " + AuthConfig.MAX_SESSIONS + " sessions");
			_log.info("DDoS-Proof: Session time set to " + AuthConfig.SESSION_TTL / 1000 + " seconds");
			Runtime.getRuntime().addShutdownHook(new Thread()
			{
				@Override
				public void run()
				{
					_running = false;
				}
			});
			start();
		}
	}

	public void addClient(L2AuthClient cl)
	{
		synchronized (_clients)
		{
			_clients.put(cl, System.currentTimeMillis());
		}
	}

	public void removeClient(L2AuthClient cl)
	{
		synchronized (_clients)
		{
			if (_clients.containsKey(cl))
			{
				_clients.remove(cl);
			}
		}
	}

	@Override
	public void run()
	{
		while (_running)
		{
			synchronized (_clients)
			{
				if (_clients.size() > AuthConfig.MAX_SESSIONS)
				{
					_log.warn("DDoS-Proof: To many connections. Flushing all");
					for (L2AuthClient cl : _clients.keySet())
					{
						cl.close(AuthFailReason.REASON_ACCESS_FAILED);
					}
					_clients.clear();
				}
				else
				{
					for (L2AuthClient cl : _clients.keySet())
						if (_clients.get(cl) + AuthConfig.SESSION_TTL < System.currentTimeMillis())
						{
							cl.close(AuthFailReason.REASON_ACCESS_FAILED);
							_clients.remove(cl);
						}
				}
			}
			try
			{
				Thread.sleep(AuthConfig.SESSION_TTL / 2);
			}
			catch (Exception e)
			{

			}
		}
		System.out.println("DDoS-Proof stopped");
	}
}