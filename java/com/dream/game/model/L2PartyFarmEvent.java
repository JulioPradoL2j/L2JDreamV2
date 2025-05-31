package com.dream.game.model;

import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.datatables.sql.SpawnTable;
import com.dream.game.datatables.xml.PartyFarmData;
import com.dream.game.model.holders.PTFarmHolder;
import com.dream.game.model.holders.PartyFarmConfig;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.game.util.Broadcast;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;



public class L2PartyFarmEvent
{
	public static Logger LOGGER = Logger.getLogger(L2PartyFarmEvent.class.getName());
	private static ScheduledFuture<?> eventChecker;
	private static boolean isRunning;
	private static List<L2Spawn> activeSpawns = Collections.synchronizedList(new ArrayList<>());
	private static String lastEventTime;
	
	public static void start()
	{
		if (eventChecker == null || eventChecker.isCancelled())
			eventChecker = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() -> checkAndStartEvent(), 500, 1000);
	}
	
	private static void checkAndStartEvent()
	{
		LocalDateTime now = LocalDateTime.now();
		int currentDay = now.getDayOfWeek().getValue() % 7; // 0 = domingo
		
		PartyFarmConfig config = PartyFarmData.getInstance().getConfig();
		
		if (!config.isEnabled() || !config.getDays().contains(currentDay))
			return;
		
		String nowStr = new SimpleDateFormat("HH:mm").format(new Date());
		
		for (String time : config.getTimes())
		{
			if (nowStr.equals(time) && !isRunning && !nowStr.equals(lastEventTime))
			{
				isRunning = true;
				lastEventTime = nowStr;
				Broadcast.announceToOnlinePlayers("The Party Farm will start in " + config.getPreparation() + " minutes!");
				ThreadPoolManager.getInstance().scheduleGeneral(() -> spawnMobs(), 1000 * 60 * config.getPreparation());
				unSpwan();
				break;
			}
		}
	}
	
	private static void spawnMobs()
	{
		Broadcast.announceToOnlinePlayers("The Party Farm has started! Good drops!");
		
		List<PTFarmHolder> spawns = PartyFarmData.getInstance().getSpawns("partyfarm");
		PartyFarmConfig config = PartyFarmData.getInstance().getConfig();
		
		for (PTFarmHolder holder : spawns)
		{
			for (int i = 0; i < holder.getCount(); i++)
			{
				try
				{
					final L2NpcTemplate template = NpcTable.getInstance().getTemplate(holder.getNpcId());
					if (template == null)
					{
						LOGGER.info("[PartyFarmEvent] Template not found for npcId: " + holder.getNpcId());
						continue;
					}
					
					L2Spawn spawn = new L2Spawn(template);
					
					int x = holder.getX();
					int y = holder.getY();
					int z = holder.getZ();
					
					if (holder.getCount() > 1)
					{
						int radius = 400;
						double angle = Math.random() * 2 * Math.PI;
						int randX = (int) (Math.cos(angle) * (Math.random() * radius));
						int randY = (int) (Math.sin(angle) * (Math.random() * radius));
						x += randX;
						y += randY;
					}
					
					spawn.setLocx(x);
					spawn.setLocy(y);
					spawn.setLocz(z);
					
					spawn.doSpawn();
					SpawnTable.getInstance().addNewSpawn(spawn, false);
					activeSpawns.add(spawn);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		
		Broadcast.announceToOnlinePlayers("The Party Farm event ends in " + config.getDuration() + " minutes!");
		ThreadPoolManager.getInstance().scheduleGeneral(() -> endEvent(), 1000 * 60 * config.getDuration());
	}
	
	private static void endEvent()
	{
		Broadcast.announceToOnlinePlayers("The Party Farm event has ended!");
		unSpwan();
		activeSpawns.clear();
		isRunning = false;
		lastEventTime = "";
	}
	
	private static void unSpwan()
	{
		for (L2Spawn spawn : activeSpawns)
		{
			if (spawn != null && spawn.getLastSpawn() != null)
				spawn.getLastSpawn().deleteMe();
			
			SpawnTable.getInstance().deleteSpawn(spawn, false);
		}
	}
	
	public static boolean isRunning()
	{
		return isRunning;
	}
	
	public static String lastEvent()
	{
		return lastEventTime;
	}
	
	public static void reset()
	{
		if (eventChecker != null)
		{
			eventChecker.cancel(false);
			eventChecker = null;
		}
	}
}
