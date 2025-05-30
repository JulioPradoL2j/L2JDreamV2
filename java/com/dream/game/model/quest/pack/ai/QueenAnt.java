package com.dream.game.model.quest.pack.ai;

import java.util.ArrayList;
import java.util.List;

import com.dream.game.ai.CtrlIntention;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.manager.grandbosses.QueenAntManager;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.position.L2CharPosition;
import com.dream.game.network.serverpackets.PlaySound;
import com.dream.game.network.serverpackets.SocialAction;
import com.dream.tools.random.Rnd;

public class QueenAnt extends L2AttackableAIScript
{
	public static final int QUEEN = 29001;
	public static final int LARVA = 29002;
	public static final int NURSE = 29003;
	public static final int GUARD = 29004;
	public static final int ROYAL = 29005;

	private static boolean _isAlive = false;
	private static L2Npc larva;
	private static L2Npc queen;
	private static List<L2Attackable> _Minions = new ArrayList<>();
	private final List<L2Attackable> _Nurses = new ArrayList<>();

	public QueenAnt()
	{
		super(-1, "queen_ant", "ai");
		int[] mobs =
		{
			QUEEN,
			LARVA,
			NURSE,
			GUARD,
			ROYAL
		};
		registerMobs(mobs);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("action") && npc != null)
		{
			if (Rnd.get(3) == 0)
				if (Rnd.get(2) == 0)
				{
					npc.broadcastPacket(new SocialAction(npc, 3));
				}
				else
				{
					npc.broadcastPacket(new SocialAction(npc, 4));
				}
		}
		else if (event.equalsIgnoreCase("despawn"))
		{
			queen.decayMe();
			larva.decayMe();
			for (L2Attackable mob : _Minions)
				if (mob != null)
				{
					mob.decayMe();
				}
			_Minions.clear();

			for (L2Attackable mob : _Nurses)
				if (mob != null)
				{
					mob.decayMe();
				}
			_Nurses.clear();
		}
		else if (event.equalsIgnoreCase("spawn_royal") && _isAlive && _Minions.size() < QueenAntManager.MAX_GUARDS)
		{
			_Minions.add((L2Attackable) addSpawn(ROYAL, queen.getX(), queen.getY(), queen.getZ(), 0, false, 0));
		}
		else if (event.equalsIgnoreCase("spawn_nurse") && _isAlive && _Nurses.size() < QueenAntManager.MAX_NURSES)
		{
			L2Attackable nurse = (L2Attackable) addSpawn(NURSE, queen.getX(), queen.getY(), queen.getZ(), 0, false, 0);
			_Nurses.add(nurse);
		}
		return null;
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (npcId == LARVA)
		{
			if (npc.getCurrentHp() < npc.getMaxHp() * 0.6)
			{
				int radius = SkillTable.getInstance().getInfo(4020, 1).getCastRange();
				for (L2Attackable a : _Nurses)
					if (a.isInsideRadius(npc.getX(), npc.getY(), radius, false))
					{
						a.getAI().setIntention(CtrlIntention.ATTACK, attacker);
					}
					else if (a.getAI().getIntention() != CtrlIntention.MOVE_TO)
					{
						a.getAI().setIntention(CtrlIntention.MOVE_TO, new L2CharPosition(npc.getLoc()));
					}
			}
		}
		else if (npcId == QUEEN)
			if (npc.getCurrentHp() < npc.getMaxHp() * 0.3 && larva.getCurrentHp() > larva.getMaxHp() * 0.3)
			{
				int radius = SkillTable.getInstance().getInfo(4020, 1).getCastRange();
				for (L2Attackable a : _Nurses)
					if (a.isInsideRadius(npc.getX(), npc.getY(), radius, false))
					{
						a.getAI().setIntention(CtrlIntention.ATTACK, attacker);
					}
					else if (a.getAI().getIntention() != CtrlIntention.MOVE_TO)
					{
						a.getAI().setIntention(CtrlIntention.MOVE_TO, new L2CharPosition(npc.getLoc()));
					}
			}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (npcId == QUEEN)
		{
			_isAlive = false;
			npc.broadcastPacket(new PlaySound(1, "BS02_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
			cancelQuestTimers("spawn_royal");
			cancelQuestTimers("spawn_nurse");
			startQuestTimer("despawn", 20000, null, null);
			larva.decayMe();
		}
		else if (_isAlive)
			if (npcId == ROYAL)
			{
				_Minions.remove(npc);
				startQuestTimer("spawn_royal", (280 + Rnd.get(40)) * 1000, npc, null);
			}
			else if (npcId == NURSE)
			{
				_Nurses.remove(npc);
				startQuestTimer("spawn_nurse", 30000, npc, null);
			}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if (npc.getNpcId() == QUEEN && !_isAlive)
		{
			queen = npc;
			_isAlive = true;
			startQuestTimer("action", 10000, npc, null, true);
			npc.broadcastPacket(new PlaySound(1, "BS02_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));

			larva = addSpawn(LARVA, -21600, 179482, -5846, Rnd.get(360), false, 0);
			_Nurses.add((L2Attackable) addSpawn(NURSE, -22000, 179482, -5846, 0, false, 0));
			_Nurses.add((L2Attackable) addSpawn(NURSE, -21200, 179482, -5846, 0, false, 0));
			int radius = 400;
			for (int i = 0; i < QueenAntManager.MAX_NURSES; i++)
			{
				int x = (int) (radius * Math.cos(i * 1.407));
				int y = (int) (radius * Math.sin(i * 1.407));
				_Nurses.add((L2Attackable) addSpawn(NURSE, npc.getX() + x, npc.getY() + y, npc.getZ(), 0, false, 0));
			}
			for (int i = 0; i < QueenAntManager.MAX_GUARDS; i++)
			{
				int x = (int) (radius * Math.cos(i * .7854));
				int y = (int) (radius * Math.sin(i * .7854));
				_Minions.add((L2Attackable) addSpawn(ROYAL, npc.getX() + x, npc.getY() + y, npc.getZ(), 0, false, 0));
			}

		}

		return null;
	}
}