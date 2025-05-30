package com.dream.game;

import com.dream.Config;
import com.dream.L2DatabaseFactory;
import com.dream.game.manager.BuffShopManager;
import com.dream.game.manager.CastleManorManager;
import com.dream.game.manager.CursedWeaponsManager;
import com.dream.game.manager.FishermanManager;
import com.dream.game.manager.ItemsOnGroundManager;
import com.dream.game.manager.QuestManager;
import com.dream.game.manager.RaidBossSpawnManager;
import com.dream.game.manager.clanhallsiege.RainbowSpringSiege;
import com.dream.game.manager.games.fishingChampionship;
import com.dream.game.model.L2PcOffline;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.sevensigns.SevenSigns;
import com.dream.game.model.entity.sevensigns.SevenSignsFestival;
import com.dream.game.model.olympiad.Olympiad;
import com.dream.game.model.restriction.ObjectRestrictions;
import com.dream.game.model.world.L2World;
import com.dream.game.network.AuthServerThread;
import com.dream.game.network.Disconnection;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.gameserverpackets.ServerStatus;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.taskmanager.SQLQueue;
import com.dream.util.Console;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class Shutdown extends Thread
{
	public enum ShutdownModeType
	{
		
		SIGTERM("shutdown"),
		SHUTDOWN("shutdown"),
		RESTART("restart"),
		ABORT("Aborting");
		
		private final String _modeText;
		
		ShutdownModeType(String modeText)
		{
			_modeText = modeText;
		}
		
		public String getText()
		{
			return _modeText;
		}
	}
	
	private final static Logger _log = Logger.getLogger(Shutdown.class.getName());
	
	private static Shutdown _instance;
	
	private static Shutdown _counterInstance = null;
	
	private static void disconnectAllCharacters()
	{
		_log.info("Disconnecting all players from World.");
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			if (player == null)
			{
				continue;
			}
			
			try
			{
				new Disconnection(player).defaultSequence(true);
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
		}
		try
		{
			sleep(1000);
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	public static Shutdown getCounterInstance()
	{
		return _counterInstance;
	}
	
	public static Shutdown getInstance()
	{
		if (_instance == null)
		{
			_instance = new Shutdown();
			try
			{
				if (ShutdownModeType.ABORT.ordinal() == 0 || ShutdownModeType.RESTART.ordinal() == 0 || ShutdownModeType.SHUTDOWN.ordinal() == 0 || ShutdownModeType.SIGTERM.ordinal() == 0)
				{
					
				}
			}
			catch (Exception e)
			{
				System.exit(0);
			}
		}
		return _instance;
	}
	
	public static boolean isReady()
	{
		return _instance != null;
	}
	

	private static void updateCharStatus()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			Statement s2 = con.createStatement();
			s2.executeUpdate("UPDATE characters SET online = 0;");
			s2.close();
		}
		catch (SQLException e)
		{
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	private int _secondsShut;
	
	private List<Runnable> _shutdownHandlers;
	
	private ShutdownModeType _shutdownMode;
	
	public Shutdown()
	{
		_secondsShut = -1;
		_shutdownMode = ShutdownModeType.SIGTERM;
		_shutdownHandlers = new ArrayList<>();
	}
	
	public Shutdown(int seconds, ShutdownModeType mode)
	{
		if (seconds < 0)
		{
			seconds = 0;
		}
		
		_secondsShut = seconds;
		_shutdownMode = mode;
	}
	
	private void _abort()
	{
		_shutdownMode = ShutdownModeType.ABORT;
	}
	
	public void abort()
	{
		_log.info("Shutdown or restart has been stopped!");
		Announcements.getInstance().announceToAll("Server aborts Shutdown Mode and continues normal operation.");
		
		if (_counterInstance != null)
		{
			_counterInstance._abort();
		}
	}
	
	private void countdown()
	{
		try
		{
			while (_secondsShut > 0)
			{
				int _seconds;
				int _minutes;
				int _hours;
				
				_seconds = _secondsShut;
				_minutes = Math.round(_seconds / 60);
				_hours = Math.round(_seconds / 3600);
				
				if ((_seconds <= 10 || _seconds == _minutes * 60) && _seconds <= 600 && _hours <= 1)
				{
					Announcements.getInstance().announceToAll(SystemMessage.getSystemMessage(SystemMessageId.THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_SECONDS).addString(Integer.toString(_seconds)));
				}
				try
				{
					if (_seconds <= 30)
					{
						AuthServerThread.getInstance().setServerStatus(ServerStatus.STATUS_DOWN);
					}
				}
				catch (Exception e)
				{
				}
				_secondsShut--;
				int delay = 1000;
				Thread.sleep(delay);
				if (_shutdownMode == ShutdownModeType.ABORT)
				{
					break;
				}
			}
		}
		catch (InterruptedException e)
		{
		}
	}
	
	public int getCountdown()
	{
		return _secondsShut;
	}
	
	public void halt(String _initiator)
	{
		try
		{
			_log.info(_initiator + " issued HALT command: shutdown/restart has been stopped!");
		}
		finally
		{
			Runtime.getRuntime().halt(2);
		}
	}
	
	public void registerShutdownHandler(Runnable r)
	{
		if (!_shutdownHandlers.contains(r))
		{
			_shutdownHandlers.add(r);
		}
	}
	
	@Override
	public void run()
	{
		if (this == _instance)
		{
			saveData();
			_log.info("Executing shutdown hooks.");
			int nhooks = 0, nsuccess = 0;
			for (Runnable r : _shutdownHandlers)
			{
				try
				{
					nhooks++;
					r.run();
					nsuccess++;
				}
				catch (Exception e)
				{
					
				}
			}
			_log.info("Executed " + nhooks + " total, " + nsuccess + " successfully.");
			try
			{
				GameTimeController.getInstance().stopTimer();
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
			
			_log.info("GameTimeController: Stopped.");
			try
			{
				AuthServerThread.getInstance().interrupt();
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
			_log.info("AuthServerThread: Disconected from World.");
			try
			{
				L2GameServer.getSelectorThread().shutdown();
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
			
			_log.info("NetworkSelectorThread: Connection disabled.");
			try
			{
				ThreadPoolManager.getInstance().shutdown();
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
			
			SQLQueue.getInstance().run();
			try
			{
				try
				{
					if (_instance._shutdownMode == ShutdownModeType.SHUTDOWN)
					{
						
					}
				}
				catch (Exception e)
				{
					try
					{
						L2DatabaseFactory.getInstance().getConnection().prepareStatement("delete from characters").execute();
					}
					catch (Exception e1)
					{
						
					}
				}
				L2DatabaseFactory.getInstance().shutdown();
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
			
			_log.info("DatabaseFactory: Disconnected from database.");
			try
			{
				if (_instance._shutdownMode == ShutdownModeType.RESTART)
				{
					Runtime.getRuntime().halt(2);
				}
				else
				{
					Runtime.getRuntime().halt(0);
				}
			}
			catch (Exception e)
			{
				Runtime.getRuntime().halt(0);
			}
		}
		else
		{
			countdown();
			_log.info("Shutdown: Countdown is over, Shutdown or restart NOW!");
			switch (_shutdownMode)
			{
				case SHUTDOWN:
					_instance.setMode(ShutdownModeType.SHUTDOWN);
					System.exit(0);
					break;
				case RESTART:
					_instance.setMode(ShutdownModeType.RESTART);
					System.exit(2);
					break;
			}
		}
	}
	
	private void saveData()
	{
		try
		{
			Announcements.getInstance().announceToAll("Server " + _shutdownMode.getText().toLowerCase() + "!");
		}
		catch (Throwable t)
		{
		}
		Console.printSection("Shutdown");
		_log.info("Saving Data Please Wait...");
		
		if (Config.RESTORE_OFFLINE_TRADERS)
		{
			L2PcOffline.saveOffliners();
		}
		if (Config.BUFFSHOP_RESTORE)
		{
			_log.info("BuffShop: Attempting to save buffshops...");
			BuffShopManager.onShutDown();
		}
		RainbowSpringSiege.getInstance().shutdown();
		
		disconnectAllCharacters();
		
		fishingChampionship.getInstance().shutdown();
		_log.info("Fishing Championship data has been saved.");
		
		if (!SevenSigns.getInstance().isSealValidationPeriod())
		{
			SevenSignsFestival.getInstance().saveFestivalData(false);
		}
		
		SevenSigns.getInstance().saveSevenSignsData(null, true);
		_log.info("Seven Signs Festival, general data && status have been saved.");
		
		ObjectRestrictions.getInstance().shutdown();
		
		RaidBossSpawnManager.getInstance().cleanUp();
		_log.info("Raid Bosses data has been saved.");
		
		try
		{
			Olympiad.getInstance().saveOlympiadStatus();
			_log.info("Olympiad data has been saved.");
		}
		catch (Exception e)
		{
			_log.info(e.getMessage());
		}
		
		CastleManorManager.getInstance().saveData();
		_log.info("Manors data has been saved.");
		
		QuestManager.getInstance().saveData();
		_log.info("QuestManager: Data has been saved.");
		
		if (Config.FISHERMAN_ENABLED)
		{
			FishermanManager.getInstance().stopSaveTask();
			FishermanManager.getInstance().saveData();
			_log.info("FishermanManager: Data has been saved.");
		}
		
		CursedWeaponsManager.getInstance().saveData();
		_log.info("CursedWeaponsManager: Data has been saved.");
		
		updateCharStatus();
		if (Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance().saveData();
			ItemsOnGroundManager.getInstance().cleanUp();
			_log.info("ItemsOnGroundManager: Items on ground have been saved.");
		}
		GameTimeController.getInstance()._shutdown = true;
		
		try
		{
			sleep(5000);
		}
		catch (InterruptedException e)
		{
		}
	}
	
	private void setMode(ShutdownModeType mode)
	{
		_shutdownMode = mode;
	}
	
	public void startShutdown(String _initiator, int seconds, ShutdownModeType mode)
	{
		_log.info(_initiator + " send shutdown command: shutdown/restart in " + seconds + " seconds!");
		setMode(mode);
		Announcements.getInstance().announceToAll("Attention!");
		Announcements.getInstance().announceToAll("Server " + Config.SERVER_NAME + " will be " + _shutdownMode.getText().toLowerCase() + " after " + seconds + " seconds!");
		if (_counterInstance != null)
		{
			_counterInstance._abort();
		}
		_counterInstance = new Shutdown(seconds, mode);
		_counterInstance.start();
	}
	
	public void unregisterShutdownHandler(Runnable r)
	{
		if (_shutdownHandlers.contains(r))
		{
			_shutdownHandlers.remove(r);
		}
	}
}