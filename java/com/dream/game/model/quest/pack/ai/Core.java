package com.dream.game.model.quest.pack.ai;

import java.util.ArrayList;
import java.util.List;

import com.dream.game.manager.grandbosses.CoreManager;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.NpcSay;
import com.dream.game.network.serverpackets.PlaySound;
import com.dream.tools.random.Rnd;

public class Core extends L2AttackableAIScript
{
	private static final int CORE = 29006;
	private static final int DEATH_KNIGHT = 29007;
	private static final int DOOM_WRAITH = 29008;
	private static final int SUSCEPTOR = 29011;

	private static boolean _FirstAttacked;
	private static boolean _isAlive;

	List<L2Attackable> Minions = new ArrayList<>();

	public Core()
	{
		super(-1, "core", "ai");

		int[] mobs =
		{
			CORE,
			DEATH_KNIGHT,
			DOOM_WRAITH,
			SUSCEPTOR
		};
		registerMobs(mobs);

		_FirstAttacked = false;
		_isAlive = false;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("spawn_minion"))
		{
			if (Minions.size() < CoreManager.MAX_GUARDS)
			{
				Minions.add((L2Attackable) addSpawn(npc.getNpcId(), npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0));
			}
		}
		else if (event.equalsIgnoreCase("despawn_minions"))
		{
			for (L2Attackable mob : Minions)
				if (mob != null)
				{
					mob.decayMe();
				}
			Minions.clear();
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (npc.getNpcId() == CORE)
			if (_FirstAttacked)
			{
				if (Rnd.get(100) == 0)
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), "Removing intruders."));
				}
			}
			else
			{
				_FirstAttacked = true;
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), "A non-permitted target has been discovered."));
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), "Starting intruder removal system."));
			}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (npcId == CORE)
		{
			_isAlive = false;
			int objId = npc.getObjectId();
			npc.broadcastPacket(new PlaySound(1, "BS02_D", 1, objId, npc.getX(), npc.getY(), npc.getZ()));
			npc.broadcastPacket(new NpcSay(objId, 0, npcId, "A fatal error has occurred."));
			npc.broadcastPacket(new NpcSay(objId, 0, npcId, "System is being shut down..."));
			npc.broadcastPacket(new NpcSay(objId, 0, npcId, "......"));
			_FirstAttacked = false;
			addSpawn(31842, 16502, 110165, -6394, 0, false, 900000);
			addSpawn(31842, 18948, 110166, -6397, 0, false, 900000);

			startQuestTimer("despawn_minions", 20000, null, null);
			cancelQuestTimers("spawn_minion");
		}
		else if (_isAlive && Minions.contains(npc))
		{
			Minions.remove(npc);
			startQuestTimer("spawn_minion", 60000, npc, null);
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if (npc.getNpcId() == CORE)
		{
			_isAlive = true;
			npc.broadcastPacket(new PlaySound(1, "BS01_A", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));

			for (int i = 0; i < CoreManager.MAX_GUARDS; i++)
			{
				int x = 16800 + i * 360;
				Minions.add((L2Attackable) addSpawn(DEATH_KNIGHT, x, 110000, npc.getZ(), 280 + Rnd.get(40), false, 0));
				Minions.add((L2Attackable) addSpawn(DEATH_KNIGHT, x, 109000, npc.getZ(), 280 + Rnd.get(40), false, 0));
				int x2 = 16800 + i * 600;
				Minions.add((L2Attackable) addSpawn(DOOM_WRAITH, x2, 109300, npc.getZ(), 280 + Rnd.get(40), false, 0));
			}
			for (int i = 0; i < CoreManager.MAX_GUARDS - 1; i++)
			{
				int x = 16800 + i * 450;
				Minions.add((L2Attackable) addSpawn(SUSCEPTOR, x, 110300, npc.getZ(), 280 + Rnd.get(40), false, 0));
			}
		}
		return null;
	}
}