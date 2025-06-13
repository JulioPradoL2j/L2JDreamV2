package com.dream.game.towerwars;

import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.datatables.sql.SpawnTable;
import com.dream.game.datatables.xml.TowerWarsData;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.game.towerwars.holder.TowerWarsCrystals;
import com.dream.game.towerwars.holder.TowerWarsRoute;
import com.dream.game.towerwars.holder.TowerWarsStatsHolder;
import com.dream.game.towerwars.holder.TowerWarsTower;
import com.dream.tools.random.Rnd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

public class InitSpawns
{
	public static Logger LOGGER = Logger.getLogger(InitSpawns.class.getName());
	
	private static List<L2Spawn> activeSpawns = Collections.synchronizedList(new ArrayList<>());
	

	public static InitSpawns getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		private static final InitSpawns INSTANCE = new InitSpawns();
	}
	
	public void isInvalidTarget(L2PcInstance player)
	{
		for (L2Spawn spawn : activeSpawns)
		{
			if (spawn.getLastSpawn().isInvul())
			{
				if (player.getTarget() == spawn.getLastSpawn())
				{
					player.abortAttack();
					player.abortCast();
					player.setTarget(null);
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
			}
			else
			{
				spawn.getLastSpawn().onAction(player);
			}
		}
	}
	
	public void clean()
	{
		for (L2Spawn spawn : activeSpawns)
		{
			if (spawn != null && spawn.getLastSpawn() != null)
				spawn.getLastSpawn().deleteMe();
			
			SpawnTable.getInstance().deleteSpawn(spawn, false);
		}
	}
	
	public void spawnRoutes()
	{
		List<TowerWarsRoute> routes = TowerWarsData.getInstance().getRoutes();
		List<TowerWarsCrystals> crystals = TowerWarsData.getInstance().getCrystals();
		
		for (TowerWarsRoute route : routes)
		{
			for (TowerWarsTower tower : route.getTowers())
			{
				spawnTowerAndInhibitorAndCrystal(tower, false);
			}
			
		}
		
		for (TowerWarsCrystals crystal : crystals)
		{
			TowerWarsTower inhibitor = crystal.getInhibitor();
			if (inhibitor != null)
			{
				spawnTowerAndInhibitorAndCrystal(inhibitor, false);
			}
			
			TowerWarsTower nexus = crystal.getCrystal();
			if (nexus != null)
			{
				spawnTowerAndInhibitorAndCrystal(nexus, true);
			}
		}

	}
	
	private static void spawnTowerAndInhibitorAndCrystal(TowerWarsTower holder, boolean crystal)
	{
		TowerWarsStatsHolder stats = TowerWarsData.getInstance().getStats();
		
		try
		{
			final L2NpcTemplate template = NpcTable.getInstance().getTemplate(holder.getNpcId());
			if (template == null)
			{
				LOGGER.warn("[TowerWarsManager] Template not found for npcId: " + holder.getNpcId());
				return;
			}
			
			L2Spawn spawn = new L2Spawn(template);
			spawn.setLocx(holder.getX());
			spawn.setLocy(holder.getY());
			spawn.setLocz(holder.getZ());
			spawn.setAmount(1);
			spawn.setHeading(Rnd.get(65535));
			
			if (!crystal)
			{
				if (stats.isRespawnTowerTime() != 0)
				{
					spawn.setRespawnDelay(stats.isRespawnTowerTime() * 1000);
				}
				
				if (stats.isRespawnInhibitorTime() != 0)
				{
					spawn.setRespawnDelay(stats.isRespawnInhibitorTime() * 1000);
				}
			}
			
			spawn.doSpawn();
			SpawnTable.getInstance().addNewSpawn(spawn, false);
			activeSpawns.add(spawn);
		}
		catch (Exception e)
		{
			LOGGER.error("[TowerWarsManager] Failed to spawn NPC with ID: " + holder.getNpcId(), e);
		}
	}
}
