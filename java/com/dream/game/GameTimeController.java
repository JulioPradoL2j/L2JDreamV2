package com.dream.game;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.game.ai.CtrlEvent;
import com.dream.game.datatables.sql.ServerData;
import com.dream.game.datatables.xml.DoorTable;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.manager.DayNightSpawnManager;
import com.dream.game.manager.grandbosses.ZakenManager;
import com.dream.game.model.L2Calendar;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2BoatInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2PcInstance.ConditionListenerDependency;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.ClientSetTime;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.taskmanager.AbstractFIFOPeriodicTaskManager;

public final class GameTimeController extends Thread
{
	private static final class ArrivedCharacterManager extends AbstractFIFOPeriodicTaskManager<L2Character>
	{
		private static final ArrivedCharacterManager _instance = new ArrivedCharacterManager();

		public static ArrivedCharacterManager getInstance()
		{
			return _instance;
		}

		private ArrivedCharacterManager()
		{
			super(GameTimeController.MILLIS_IN_TICK);
		}

		@Override
		protected void callTask(L2Character cha)
		{
			if (cha == null)
				return;

			cha.getKnownList().updateKnownObjects();

			if (cha instanceof L2BoatInstance)
			{
				((L2BoatInstance) cha).evtArrived();
			}

			if (cha.hasAI())
			{
				cha.getAI().notifyEvent(CtrlEvent.EVT_ARRIVED);
			}
		}

		@Override
		protected String getCalledMethodName()
		{
			return "getAI().notifyEvent(CtrlEvent.EVT_ARRIVED)";
		}
	}

	public class ClosePiratesRoom implements Runnable
	{
		@Override
		public void run()
		{
			if (ZakenManager.CLOSED_ZAKEN_DOORS)
			{
				DoorTable.getInstance().getDoor(21240006).closeMe();
			}
		}
	}

	public class MinuteCounter implements Runnable
	{
		@Override
		public void run()
		{
			boolean isNight = isNowNight();
			int oldHour = _calendar.getDate().get(Calendar.HOUR_OF_DAY);
			_calendar.getDate().add(Calendar.MINUTE, 1);
			int newHour = _calendar.getDate().get(Calendar.HOUR_OF_DAY);

			if (newHour != oldHour)
			{
				for (L2PcInstance player : L2World.getInstance().getAllPlayers())
				{
					player.sendPacket(ClientSetTime.STATIC_PACKET);
				}

				if (ZakenManager.isLoaded)
					if (ZakenManager.OPEN_DOOR_TIME_HOUR.contains(newHour))
					{
						DoorTable.getInstance().getDoor(21240006).openMe();
						ThreadPoolManager.getInstance().scheduleGeneral(new ClosePiratesRoom(), ZakenManager.DOOR_OPEN_TIME * 60 * 1000);
					}

				if (newHour == Config.DATETIME_SUNSET)
				{
					addShadowSense();
				}
				if (newHour == Config.DATETIME_SUNRISE)
				{
					removeShadowSense();
				}

				if (isNight != isNowNight())
				{
					DayNightSpawnManager.getInstance().notifyChangeMode();
					for (L2PcInstance player : L2World.getInstance().getAllPlayers())
					{
						player.refreshConditionListeners(ConditionListenerDependency.GAME_TIME);
					}
				}
			}
			saveData();
		}
	}

	public class MovingObjectArrived implements Runnable
	{
		@Override
		public void run()
		{
			for (L2Character cha; (cha = getNextEndedChar()) != null;)
			{
				try
				{
					cha.getKnownList().updateKnownObjects();
					if (cha instanceof L2BoatInstance)
					{
						((L2BoatInstance) cha).evtArrived();
					}
					if (cha.hasAI())
					{
						cha.getAI().notifyEvent(CtrlEvent.EVT_ARRIVED);
					}
				}
				catch (Exception e)
				{
					_log.warn("", e);
				}
			}
		}
	}

	public static final int TICKS_PER_SECOND = 10;

	public static final int MILLIS_IN_TICK = 1000 / TICKS_PER_SECOND;

	public static final Logger _log = Logger.getLogger(GameTimeController.class);

	public static L2Calendar _calendar;

	private static final GameTimeController _instance = new GameTimeController();

	private static List<L2Character> _movingObjects = new ArrayList<>();

	public static void addShadowSense()
	{
		L2Skill skill = SkillTable.getInstance().getInfo(294, 1);
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			if (player != null && skill != null && player.getRace().ordinal() == 2 && player.getSkillLevel(294) == 1)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_NIGHT_EFFECT_APPLIES).addSkillName(294));
			}
	}

	public static GregorianCalendar getDate()
	{
		return _calendar.getDate();
	}

	public static String getFormatedDate()
	{
		SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
		if (Config.DATETIME_SAVECAL)
		{
			format = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
		}
		return format.format(getDate().getTime());
	}

	public static int getGameTicks()
	{
		return _calendar.gameTicks;
	}

	public static int getGameTime()
	{
		return _calendar.getDate().get(Calendar.HOUR_OF_DAY) * 60 + _calendar.getDate().get(Calendar.MINUTE);
	}

	public static GameTimeController getInstance()
	{
		return _instance;
	}

	public static boolean isNowNight()
	{
		int hour = _calendar.getDate().get(Calendar.HOUR_OF_DAY);
		if (Config.DATETIME_SUNRISE > Config.DATETIME_SUNSET)
		{
			if (hour < Config.DATETIME_SUNRISE && hour >= Config.DATETIME_SUNSET)
				return true;
		}
		else if (hour < Config.DATETIME_SUNRISE || hour >= Config.DATETIME_SUNSET)
			return true;
		return false;
	}

	private static L2Calendar loadData()
	{

		L2Calendar cal = null;
		try
		{
			cal = new L2Calendar();
			cal.getDate().set(Calendar.YEAR, ServerData.getInstance().getData().getInteger("GameTime.year"));
			cal.getDate().set(Calendar.MONTH, ServerData.getInstance().getData().getInteger("GameTime.month"));
			cal.getDate().set(Calendar.DAY_OF_MONTH, ServerData.getInstance().getData().getInteger("GameTime.day"));
			cal.getDate().set(Calendar.HOUR_OF_DAY, ServerData.getInstance().getData().getInteger("GameTime.hour"));
			cal.getDate().set(Calendar.MINUTE, ServerData.getInstance().getData().getInteger("GameTime.minute"));
			cal.setGameStarted(ServerData.getInstance().getData().getLong("GameTime.started"));
			return cal;
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
	}

	public synchronized static void registerMovingObject(L2Character cha)
	{
		if (cha == null)
			return;

		if (!_movingObjects.contains(cha))
		{
			_movingObjects.add(cha);
		}
	}

	public static void removeShadowSense()
	{
		L2Skill skill = SkillTable.getInstance().getInfo(294, 1);
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			if (player != null && skill != null && player.getRace().ordinal() == 2 && player.getSkillLevel(294) == 1)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_NIGHT_EFFECT_DISAPPEARS).addSkillName(294));
			}
	}

	public static void saveData()
	{
		if (Config.DATETIME_SAVECAL)
		{
			ServerData.getInstance().getData().set("GameTime.year", _calendar.getDate().get(Calendar.YEAR));
			ServerData.getInstance().getData().set("GameTime.month", _calendar.getDate().get(Calendar.MONTH));
			ServerData.getInstance().getData().set("GameTime.day", _calendar.getDate().get(Calendar.DAY_OF_MONTH));
			ServerData.getInstance().getData().set("GameTime.hour", _calendar.getDate().get(Calendar.HOUR_OF_DAY));
			ServerData.getInstance().getData().set("GameTime.minute", _calendar.getDate().get(Calendar.MINUTE));
			ServerData.getInstance().getData().set("GameTime.started", _calendar.getGameStarted());
		}
	}

	private final Set<L2Character> _movingChars = new HashSet<>();

	private final List<L2Character> _endedChars = new ArrayList<>();

	public long _startMoveTime;

	public boolean _shutdown = false;

	private GameTimeController()
	{
		super("GameTimeController");
		setDaemon(true);
		setPriority(MAX_PRIORITY);

		if (Config.DATETIME_SAVECAL)
		{
			_calendar = loadData();
		}

		if (_calendar == null)
		{
			_calendar = new L2Calendar();
			_calendar.getDate().set(Calendar.YEAR, 1281);
			_calendar.getDate().set(Calendar.MONTH, 5);
			_calendar.getDate().set(Calendar.DAY_OF_MONTH, 5);
			_calendar.getDate().set(Calendar.HOUR_OF_DAY, 23);
			_calendar.getDate().set(Calendar.MINUTE, 45);
			_calendar.setGameStarted(System.currentTimeMillis());
			saveData();
		}

		start();
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new MovingObjectArrived(), MILLIS_IN_TICK, MILLIS_IN_TICK);
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new MinuteCounter(), 0, 1000 * Config.DATETIME_MULTI);
	}

	private L2Character[] getMovingChars()
	{
		synchronized (_movingChars)
		{
			return _movingChars.toArray(new L2Character[_movingChars.size()]);
		}
	}

	public L2Character getNextEndedChar()
	{
		synchronized (_endedChars)
		{
			return _endedChars.isEmpty() ? null : _endedChars.remove(0);
		}
	}

	private void moveObjects()
	{
		for (L2Character cha : getMovingChars())
		{
			if (!cha.updatePosition(_calendar.gameTicks))
			{
				continue;
			}

			synchronized (_movingChars)
			{
				_movingChars.remove(cha);
			}

			ArrivedCharacterManager.getInstance().add(cha);
		}
	}

	public void registerMovingChar(L2Character cha)
	{
		if (cha == null)
			return;

		synchronized (_movingChars)
		{
			_movingChars.add(cha);
		}
	}

	@Override
	public void run()
	{
		for (;;)
		{
			long currentTime = System.currentTimeMillis();
			_startMoveTime = currentTime;
			_calendar.gameTicks = (int) ((currentTime - _calendar.getGameStarted()) / MILLIS_IN_TICK);
			moveObjects();
			currentTime = System.currentTimeMillis();
			_calendar.gameTicks = (int) ((currentTime - _calendar.getGameStarted()) / MILLIS_IN_TICK);
			long sleepTime = Config.DATETIME_MOVE_DELAY - (currentTime - _startMoveTime);
			if (sleepTime > 0)
			{
				if (_shutdown == true)
				{
					break;
				}
				try
				{
					sleep(sleepTime);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public void stopTimer()
	{
		interrupt();
	}
}