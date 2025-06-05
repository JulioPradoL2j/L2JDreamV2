package com.dream.game.taskmanager;

import com.dream.game.ai.CtrlIntention;
import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.datatables.xml.GreetingData;
import com.dream.game.manager.CastleManager;
import com.dream.game.model.L2Clan;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.siege.Castle;
import com.dream.game.model.holders.GreetingHolder;
import com.dream.game.model.nemus.ZoneType;
import com.dream.game.model.world.L2World;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.ExShowScreenMessage;
import com.dream.game.network.serverpackets.ExShowScreenMessage.SMPOS;
import com.dream.game.network.serverpackets.SocialAction;
import com.dream.game.util.FloodProtector;
import com.dream.game.util.FloodProtector.Protected;
import com.dream.game.util.Util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

public class GreetingManager
{
	private static final int RADIUS = 500; // Raio de proximidade
	private static final Map<Integer, Long> _greetCooldown = new ConcurrentHashMap<>();
	private static ScheduledFuture<?> _greetingChecker;
	
	public static void start()
	{
		if (_greetingChecker == null || _greetingChecker.isCancelled())
		{
			_greetingChecker = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() -> checkGreetings(), 5000, 5000);
		}
	}
	
	private static void checkGreetings()
	{
		for (Castle castle : CastleManager.getInstance().getCastles().values())
		{
			GreetingHolder holder = GreetingData.getInstance().getGreeting(castle.getCastleId());
			if (holder == null || holder.getZoneType() != ZoneType.TOWN)
				continue;
			
			int ownerId = castle.getOwnerId();
			if (ownerId <= 0)
				continue;
			
			L2Clan clan = ClanTable.getInstance().getClan(ownerId);
			if (clan == null || clan.getLeader() == null)
				continue;
			
			L2PcInstance leader = clan.getLeader().getPlayerInstance();
			if (leader == null || !leader.isInsideZone(L2Zone.FLAG_PEACE))
				continue;
			
			long lastTime = _greetCooldown.getOrDefault(castle.getCastleId(), 0L);
			if (System.currentTimeMillis() - lastTime < (holder.getTime() * 1000L))
				continue;
			
			for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			{
				if (player == null || player.isDead() || !player.isInsideZone(L2Zone.FLAG_PEACE))
					continue;
				
				if (player == leader || !Util.checkIfInRange(RADIUS, leader, player, true))
					continue;
				
				if (player.getPrivateStoreType() == 0 && player.getActiveRequester() == null && !player.isAlikeDead() && !player.isCastingNow() && !player.isCastingSimultaneouslyNow() && (!player.isAllSkillsDisabled() || player.isInDuel()) && player.getAI().getIntention() == CtrlIntention.IDLE && FloodProtector.tryPerformAction(player, Protected.SOCIAL))
				{
					player.broadcastPacket(new SocialAction(player, holder.getActionUse()));
				}
				
				player.sendMessage(holder.getMessage());
				
				player.sendPacket(new ExShowScreenMessage(holder.getMessage(), 1100, SMPOS.TOP_CENTER, false, 1));
			}
			
			_greetCooldown.put(castle.getCastleId(), System.currentTimeMillis());
		}
	}
	
}
