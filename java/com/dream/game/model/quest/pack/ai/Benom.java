package com.dream.game.model.quest.pack.ai;

import com.dream.L2DatabaseFactory;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.datatables.sql.SpawnTable;
import com.dream.game.datatables.xml.DoorTable;
import com.dream.game.manager.CastleManager;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.position.L2CharPosition;
import com.dream.game.model.quest.jython.QuestJython;
import com.dream.game.model.world.Location;
import com.dream.game.network.serverpackets.NpcSay;
import com.dream.game.network.serverpackets.SocialAction;
import com.dream.game.network.serverpackets.SpecialCamera;
import com.dream.tools.random.Rnd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Benom extends QuestJython
{
	public static final String QN = "Benom";

	private static final int CASTLE_ID = 8;

	private static final int BENOM = 29054;

	private static final int BENOM_TELEPORT = 13101;

	private static final String[] BENOM_SPEAK =
	{
		"You should have finished me when you had the chance!!!",
		"I will crush all of you!!!",
		"I am not finished here, come face me!!!",
		"You cowards!!! I will torture each and everyone of you!!!"
	};

	private static final Map<Integer, Location> BENON_WALK_ROUTES = new HashMap<>();
	private static final int[] WALK_TIMES =
	{
		18000,
		17000,
		4500,
		16000,
		22000,
		14000,
		10500,
		14000,
		9500,
		12500,
		20500,
		14500,
		17000,
		20000,
		22000,
		11000,
		11000,
		20000,
		8000,
		5500,
		20000,
		18000,
		25000,
		28000,
		25000,
		25000,
		25000,
		25000,
		10000,
		24000,
		7000,
		12000,
		20000
	};

	private static final byte ALIVE = 0;
	private static final byte DEAD = 1;
	private static byte BenomIsSpawned = 0;
	private static int BenomWalkRouteStep = 0;

	static
	{
		BENON_WALK_ROUTES.put(0, new Location(12565, -49739, -547));
		BENON_WALK_ROUTES.put(1, new Location(11242, -49689, -33));
		BENON_WALK_ROUTES.put(2, new Location(10751, -49702, 83));
		BENON_WALK_ROUTES.put(3, new Location(10824, -50808, 316));
		BENON_WALK_ROUTES.put(4, new Location(9084, -50786, 972));
		BENON_WALK_ROUTES.put(5, new Location(9095, -49787, 1252));
		BENON_WALK_ROUTES.put(6, new Location(8371, -49711, 1252));
		BENON_WALK_ROUTES.put(7, new Location(8423, -48545, 1252));
		BENON_WALK_ROUTES.put(8, new Location(9105, -48474, 1252));
		BENON_WALK_ROUTES.put(9, new Location(9085, -47488, 972));
		BENON_WALK_ROUTES.put(10, new Location(10858, -47527, 316));
		BENON_WALK_ROUTES.put(11, new Location(10842, -48626, 75));
		BENON_WALK_ROUTES.put(12, new Location(12171, -48464, -547));
		BENON_WALK_ROUTES.put(13, new Location(13565, -49145, -535));
		BENON_WALK_ROUTES.put(14, new Location(15653, -49159, -1059));
		BENON_WALK_ROUTES.put(15, new Location(15423, -48402, -839));
		BENON_WALK_ROUTES.put(16, new Location(15066, -47438, -419));
		BENON_WALK_ROUTES.put(17, new Location(13990, -46843, -292));
		BENON_WALK_ROUTES.put(18, new Location(13685, -47371, -163));
		BENON_WALK_ROUTES.put(19, new Location(13384, -47470, -163));
		BENON_WALK_ROUTES.put(20, new Location(14609, -48608, 346));
		BENON_WALK_ROUTES.put(21, new Location(13878, -47449, 747));
		BENON_WALK_ROUTES.put(22, new Location(12894, -49109, 980));
		BENON_WALK_ROUTES.put(23, new Location(10135, -49150, 996));
		BENON_WALK_ROUTES.put(24, new Location(12894, -49109, 980));
		BENON_WALK_ROUTES.put(25, new Location(13738, -50894, 747));
		BENON_WALK_ROUTES.put(26, new Location(14579, -49698, 347));
		BENON_WALK_ROUTES.put(27, new Location(12896, -51135, -166));
		BENON_WALK_ROUTES.put(28, new Location(12971, -52046, -292));
		BENON_WALK_ROUTES.put(29, new Location(15140, -50781, -442));
		BENON_WALK_ROUTES.put(30, new Location(15328, -50406, -603));
		BENON_WALK_ROUTES.put(31, new Location(15594, -49192, -1059));
		BENON_WALK_ROUTES.put(32, new Location(13175, -49153, -537));
	}

	
	private static int checkStatusBoss()
	{
		int checkStatus = 1;
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement("SELECT state FROM grandboss_intervallist WHERE bossId = " + BENOM);
			final ResultSet rset = statement.executeQuery();
			if (rset.next())
			{
				checkStatus = rset.getInt("state");
			}
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			if (_log.isDebugEnabled())
			{
				_log.debug("", e);
			}
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		return checkStatus;
	}

	private static void unSpawnNpc(int npcId)
	{
		for (L2Spawn spawn : SpawnTable.getInstance().getSpawnTable().values())
			if (spawn.getId() == npcId)
			{
				SpawnTable.getInstance().deleteSpawn(spawn, false);
				L2Npc npc = spawn.getLastSpawn();
				npc.deleteMe();
			}
	}

	
	private static void updateStatusBoss(int status)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement("UPDATE grandboss_intervallist SET state = ? WHERE bossId = " + BENOM);
			statement.setInt(1, status);
			statement.executeQuery();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warn("Benom: Could not update the status !", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private L2Npc _Benom;

	public Benom()
	{
		super(-1, QN, "ai");
		addStartNpc(BENOM_TELEPORT);
		addTalkId(BENOM_TELEPORT);
		addAggroRangeEnterId(BENOM);
		addKillId(BENOM);

		final int castleOwner = CastleManager.getInstance().getCastleById(CASTLE_ID).getOwnerId();
		final long siegeDate = CastleManager.getInstance().getCastleById(CASTLE_ID).getSiegeDate().getTimeInMillis();
		final long currentTime = System.currentTimeMillis();
		long benomTeleporterSpawn = siegeDate - currentTime - 86400000;
		final long benomRaidRoomSpawn = siegeDate - currentTime - 86400000;
		long benomRaidSiegeSpawn = siegeDate - currentTime;

		if (benomTeleporterSpawn < 0)
		{
			benomTeleporterSpawn = 1;
		}
		if (benomRaidSiegeSpawn < 0)
		{
			benomRaidSiegeSpawn = 1;
		}

		if (castleOwner > 0)
		{
			if (benomTeleporterSpawn >= 1)
			{
				startQuestTimer("BenomTeleSpawn", benomTeleporterSpawn, null, null);
			}

			if (siegeDate - currentTime > 0)
			{
				startQuestTimer("BenomRaidRoomSpawn", benomRaidRoomSpawn, null, null);
			}

			startQuestTimer("BenomRaidSiegeSpawn", benomRaidSiegeSpawn, null, null);
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final int statusBoss = checkStatusBoss();
		if (event.equalsIgnoreCase("BenomTeleSpawn"))
		{
			addSpawn(BENOM_TELEPORT, 11013, -49629, -547, 13400, false, 0);
		}
		else if (event.equalsIgnoreCase("BenomRaidRoomSpawn"))
		{
			if (BenomIsSpawned == 0 && statusBoss == 0)
			{
				_Benom = addSpawn(BENOM, 12047, -49211, -3009, 0, false, 0);
			}
			BenomIsSpawned = 1;
		}
		else if (event.equalsIgnoreCase("BenomRaidSiegeSpawn"))
		{
			if (statusBoss == 0)
			{

				switch (BenomIsSpawned)
				{
					case 0:
						_Benom = addSpawn(BENOM, 11025, -49152, -537, 0, false, 0);
						BenomIsSpawned = 1;
						break;
					case 1:
						_Benom.teleToLocation(11025, -49152, -537);
						break;
				}

				startQuestTimer("BenomSpawnEffect", 100, _Benom, null);
				startQuestTimer("BenomBossDespawn", 5400000, _Benom, null);
				cancelQuestTimer("BenomSpawn", _Benom, null);
				unSpawnNpc(BENOM_TELEPORT);
			}
		}
		else if (event.equalsIgnoreCase("BenomSpawnEffect"))
		{
			npc.getAI().setIntention(CtrlIntention.IDLE);
			npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 200, 0, 150, 0, 5000));
			npc.broadcastPacket(new SocialAction(npc, 3));
			startQuestTimer("BenomWalk", 5000, npc, null);
			BenomWalkRouteStep = 0;
		}
		else if (event.equalsIgnoreCase("Attacking"))
		{
			List<L2PcInstance> NumPlayers = new ArrayList<>();
			for (L2PcInstance plr : npc.getKnownList().getKnownPlayers().values())
			{
				NumPlayers.add(plr);
			}

			if (NumPlayers.size() > 0)
			{
				L2PcInstance target = NumPlayers.get(Rnd.get(NumPlayers.size()));
				((L2Attackable) npc).addDamageHate(target, 0, 999);
				npc.getAI().setIntention(CtrlIntention.ATTACK, target);
				startQuestTimer("Attacking", 2000, npc, player);
			}
			else if (NumPlayers.size() == 0)
			{
				startQuestTimer("BenomWalkFinish", 2000, npc, null);
			}
		}
		else if (event.equalsIgnoreCase("BenomWalkFinish"))
		{
			if (npc.getCastle().getSiege().getIsInProgress())
			{
				cancelQuestTimer("Attacking", npc, player);
			}
			npc.teleToLocation(BENON_WALK_ROUTES.get(BenomWalkRouteStep), false);
			npc.setWalking();
			BenomWalkRouteStep = 0;
			startQuestTimer("BenomWalk", 2200, npc, null);
		}
		else if (event.equalsIgnoreCase("BenomWalk"))
		{
			if (BenomWalkRouteStep == 33)
			{
				BenomWalkRouteStep = 0;
				startQuestTimer("BenomWalk", 100, npc, null);
			}
			else
			{
				startQuestTimer("Talk", 100, npc, null);
				switch (BenomWalkRouteStep)
				{
					case 14:
						startQuestTimer("DoorOpen", 15000, null, null);
						startQuestTimer("DoorClose", 23000, null, null);
						break;
					case 32:
						startQuestTimer("DoorOpen", 500, null, null);
						startQuestTimer("DoorClose", 4000, null, null);
						break;
				}

				npc.getAI().setIntention(CtrlIntention.IDLE);
				npc.getAI().setIntention(CtrlIntention.MOVE_TO, new L2CharPosition(BENON_WALK_ROUTES.get(BenomWalkRouteStep)));
				BenomWalkRouteStep++;
				startQuestTimer("BenomWalk", WALK_TIMES[BenomWalkRouteStep], npc, null);
			}
		}
		else if (event.equalsIgnoreCase("DoorOpen"))
		{
			DoorTable.getInstance().getDoor(20160005).openMe();
		}
		else if (event.equalsIgnoreCase("DoorClose"))
		{
			DoorTable.getInstance().getDoor(20160005).closeMe();
		}
		else if (event.equalsIgnoreCase("Talk"))
		{
			if (Rnd.get(100) < 40)
			{
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), BENOM_SPEAK[Rnd.get(BENOM_SPEAK.length)]));
			}
		}
		else if (event.equalsIgnoreCase("BenomBossDespawn"))
		{
			updateStatusBoss(ALIVE);
			BenomIsSpawned = 0;
			unSpawnNpc(BENOM);
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		cancelQuestTimer("BenomWalk", npc, null);
		cancelQuestTimer("BenomWalkFinish", npc, null);
		startQuestTimer("Attacking", 100, npc, player);
		return super.onAggroRangeEnter(npc, player, isPet);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		updateStatusBoss(DEAD);
		cancelQuestTimer("BenomWalk", npc, null);
		cancelQuestTimer("BenomWalkFinish", npc, null);
		cancelQuestTimer("BenomBossDespawn", npc, null);
		cancelQuestTimer("Talk", npc, null);
		cancelQuestTimer("Attacking", npc, null);
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		final int castleOwner = CastleManager.getInstance().getCastleById(CASTLE_ID).getOwnerId();
		final int clanId = player.getClanId();
		if (castleOwner != 0 && clanId != 0)
		{
			if (castleOwner == clanId)
			{
				player.teleToLocation(new Location(12558, -49279, -3007, 100), false);
				return htmltext;
			}
			htmltext = "<html><body>Benom's Avatar:<br>Your clan does not own this castle. Only members of this Castle's owning clan can challenge Benom.</body></html>";
		}
		else
		{
			htmltext = "<html><body>Benom's Avatar:<br>Your clan does not own this castle. Only members of this Castle's owning clan can challenge Benom.</body></html>";
		}

		return htmltext;
	}
}