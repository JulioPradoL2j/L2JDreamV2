package com.dream.game.access;

import com.dream.Config;
import com.dream.annotations.L2Properties;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

public class gmCache
{
	private static gmCache _instance = null;

	public static Logger _log = Logger.getLogger(gmCache.class.getName());

	private static boolean checkFile(File file)
	{
		if (file == null)
			return false;
		if (file.isDirectory())
			return false;
		if (file.isHidden())
			return false;
		if (!file.getName().endsWith(".cfg"))
			return false;
		if (file.getName().startsWith("example.cfg"))
			return false;

		return true;
	}

	public static gmCache getInstance()
	{
		if (_instance == null)
		{
			_instance = new gmCache();
		}
		return _instance;
	}


	private static void loadCommands(GmPlayer gm, File file)
	{
		BufferedReader in = null;
		try
		{
			in = new BufferedReader(new FileReader(file));
			String str;

			while ((str = in.readLine()) != null)
			{
				if (str.startsWith("#") || str.length() == 0)
				{
					continue;
				}

				if (str.contains("#"))
				{
					str = str.split("#")[0];
				}

				if (str.startsWith("//"))
				{
					gm.putCommand(str.substring("//".length()));
				}
			}
		}
		catch (FileNotFoundException e)
		{
			_log.info("GmController: Error on read acces for Gm: " + gm.getName());
		}
		catch (IOException e)
		{
			_log.info("GmController: Error on read acces for Gm: " + gm.getName());
		}
		finally
		{
			try
			{
				if (in != null)
				{
					in.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	private Map<Integer, GmPlayer> _access = new HashMap<>();

	private GmPlayer gm;

	private gmCache()
	{
		loadAccess(false);
	}

	public GmPlayer getGmPlayer(int id)
	{

		return _access.get(id);
	}

	public void info()
	{
		_log.info("============= GM LIST =============");
		int y = _access.size();
		for (GmPlayer gm : _access.values())
		{
			_log.info("GM title: " + gm.getName());
			_log.info("GM objId: " + gm.getObjId());
			_log.info("GM admin: " + (gm.isRoot() ? "True" : "False"));
			_log.info("GM event: " + (gm.isGm() ? "True" : "False"));
			_log.info("GM fix.r: " + (gm.allowFixRes() ? "True" : "False"));
			_log.info("Gm alt.g: " + (gm.allowAltG() ? "True" : "False"));
			y--;
			if (y != 0)
			{
				_log.info("-----------------------------------");
			}
		}
		_log.info("============= GM LIST =============");
	}

	public boolean isGm(int id)
	{
		if (!_access.containsKey(id))
			return false;
		return _access.get(id).isGm();
	}

	public void loadAccess(boolean reload)
	{
		File dir = new File("./config/admin/access");
		if (!dir.exists())
		{
			dir.mkdirs();
			return;
		}

		_access.clear();
		_access = new HashMap<>();
		File[] files = dir.listFiles();
		gm = null;
		for (File file : files)
			if (checkFile(file))
			{
				try
				{
					Properties cfg = new L2Properties("./config/admin/access/" + file.getName());
					gm = new GmPlayer(Integer.parseInt(cfg.getProperty("CharId")));
					gm.setName(file.getName().substring(0, file.getName().length() - 4));
					gm.setGm(Boolean.parseBoolean(cfg.getProperty("isAdmin")));
					gm.seFixRes(Boolean.parseBoolean(cfg.getProperty("FixedRes")));
					gm.seAltG(Boolean.parseBoolean(cfg.getProperty("AllowAltG")));
					gm.setPeaceAtk(Boolean.parseBoolean(cfg.getProperty("AllowPeaceAtk")));
					gm.setRoot(Boolean.parseBoolean(cfg.getProperty("isRoot")));
					gm.setCheckIp(Boolean.parseBoolean(cfg.getProperty("CheckIp")));
					gm.setIP(cfg.getProperty("SecureIp").split(";"));
					loadCommands(gm, file);
					if (!gm.isRoot() && !gm.isGm())
					{
						_log.info("GmController: Acces for player: " + gm.getName() + " incorrect");
						continue;
					}
					_access.put(gm.getObjId(), gm);
				}
				catch (Exception e)
				{
					_log.info("GmController: Acces for player: " + gm.getName() + " incorrect");
					continue;
				}
			}
		_log.info("Admin Manager: Loaded " + _access.size() + " Admin player(s).");

		if (Config.DEBUG)
		{
			info();
		}
		int max = _access.size();
		int cur = 0;
		if (reload)
		{
			gmController.getInstance().checkAdmins();

			for (L2PcInstance pl : L2World.getInstance().getAllPlayers())
			{
				if (pl == null || pl.isGM() || pl.isOfflineTrade())
				{
					continue;
				}
				if (_access.get(pl.getObjectId()) != null)
				{
					gmController.getInstance().checkPrivs(pl);
					cur++;
				}
				if (cur == max)
				{
					break;
				}
			}
		}
	}

	public void removeGM(int charId)
	{
		_access.remove(charId);
	}

	public void setGm(int charId)
	{
		GmPlayer gm = new GmPlayer(charId);
		gm.setRoot(true);
		_access.put(charId, gm);
		gmController.getInstance().checkPrivs(L2World.getInstance().findPlayer(charId));
		gm.setIsTemp(true);

	}
}