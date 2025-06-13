package com.dream.game.towerwars;

import com.dream.game.datatables.xml.TowerWarsData;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.towerwars.holder.TowerWarsPlayers;
import com.dream.game.towerwars.holder.TowerWarsPlayersSpot;
import com.dream.game.towerwars.instance.TowerWarsTeams;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class ListMath
{
	private static ScheduledFuture<?> fightRepeater;
	private List<L2PcInstance> waitingPlayers = new ArrayList<>();
	
	public void startMath()
	{
		load();
	}
	
	private void load()
	{
		if (fightRepeater == null || fightRepeater.isCancelled())
			fightRepeater = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() -> checkFights(), 500, 1000);
	}
	
	public void checkFights()
	{
		if (waitingPlayers.size() < 2)
			return;
		

		List<TowerWarsPlayers> players = TowerWarsData.getInstance().getPlayers();
		if (players.size() < 2)
			return;
			
		TowerWarsPlayersSpot spotA = players.get(0).getPlayers();
		TowerWarsPlayersSpot spotB = players.get(1).getPlayers();
		
		if (spotA == null || spotB == null)
		    return;
		
		// Seleciona 10 jogadores
		List<L2PcInstance> selectedPlayers = new ArrayList<>();
		for (int i = 0; i < 2; i++)
			selectedPlayers.add(waitingPlayers.remove(0));
		
		TowerWarsTeams match = new TowerWarsTeams(spotA, spotB);
		for (L2PcInstance player : selectedPlayers)
			match.registerPlayer(player);
		
		match.startMatch();
		
		System.out.println("Iniciando partida com:");
		System.out.println("Team A: " + selectedPlayers.get(0).getName());
		System.out.println("Team B: " + selectedPlayers.get(1).getName());
		
    	InitSpawns.getInstance().spawnRoutes();


	}
	
	public void registerPlayer(L2PcInstance player)
	{
		if (!waitingPlayers.contains(player))
			waitingPlayers.add(player);
	}
	
	public void reset()
	{
		if (fightRepeater != null)
		{
			fightRepeater.cancel(false);
			fightRepeater = null;
		}
	}
	
	public static ListMath getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		private static final ListMath INSTANCE = new ListMath();
	}
}
