package com.dream.game.model;

import com.dream.game.datatables.xml.TowerWarsData;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.towerwars.ListMath;
import com.dream.game.towerwars.holder.TowerWarsSettings;
import com.dream.game.util.Broadcast;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

public class TowerWarsManager
{
	public static Logger LOGGER = Logger.getLogger(TowerWarsManager.class.getName());
	
	private static ScheduledFuture<?> eventChecker;
	private static boolean isRunning;
	private static String lastEventTime;
	
	public void start()
	{
		if (eventChecker == null || eventChecker.isCancelled())
			eventChecker = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() -> checkAndStartEvent(), 500, 1000);
	}
	
	private static void checkAndStartEvent()
	{
		LocalDateTime now = LocalDateTime.now();
		int currentDay = now.getDayOfWeek().getValue() % 7; // 0 = domingo
		
		TowerWarsSettings config = TowerWarsData.getInstance().getConfig();
		
		if (!config.isEnabled() || !config.getDays().contains(currentDay))
			return;
		
		String nowStr = new SimpleDateFormat("HH:mm").format(new Date());
		
		for (String time : config.getTimes())
		{
			if (nowStr.equals(time) && !isRunning && !nowStr.equals(lastEventTime))
			{
				isRunning = true;
				lastEventTime = nowStr;
				Broadcast.announceToOnlinePlayers("The Tower Wars will start in " + config.getPreparation() + " minutes!");
				ThreadPoolManager.getInstance().scheduleGeneral(() -> finish(), 1000 * 60 * config.getPreparation());
				break;
			}
		}
	}
	
	private static void finish()
	{
		ListMath.getInstance().startMath();
		
		TowerWarsSettings config = TowerWarsData.getInstance().getConfig();
		Broadcast.announceToOnlinePlayers("The Tower Wars  event ends in " + config.getDuration() + " minutes!");
		ThreadPoolManager.getInstance().scheduleGeneral(() -> endEvent(), 1000 * 60 * config.getDuration());
	}
	
	private static void endEvent()
	{
		Broadcast.announceToOnlinePlayers("The Tower Wars event has ended!");
		isRunning = false;
		lastEventTime = "";
		ListMath.getInstance().reset();	
	}
	
	public boolean isRunning()
	{
		return isRunning;
	}
	
	public String lastEvent()
	{
		return lastEventTime;
	}
	
	public void reset()
	{
		if (eventChecker != null)
		{
			eventChecker.cancel(false);
			eventChecker = null;
		}
	}
	
	public static TowerWarsManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		private static final TowerWarsManager INSTANCE = new TowerWarsManager();
	}
}