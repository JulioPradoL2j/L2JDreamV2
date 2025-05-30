package com.dream.game.model.quest.pack.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dream.game.ai.CtrlIntention;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.NpcSay;
import com.dream.tools.random.Rnd;

public class Monastery extends L2AttackableAIScript
{
	private static boolean _isAttacked = false;

	private final Map<Integer, List<L2Character>> _attackersList = new HashMap<>();

	public Monastery()
	{
		super(-1, "Monastery", "ai");
		int[] mobs =
		{
			22124,
			22125,
			22126,
			22127,
			22129
		};
		registerMobs(mobs);
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		int npcObjId = npc.getObjectId();

		L2Character target = isPet ? player.getPet() : player;

		if (!player.isVisible())
			return null;

		if (player.getActiveWeaponItem() != null && player.getActiveWeaponItem().getItemId() > 252)
		{
			if (npc.getNpcId() == 22124)
			{
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), "Brother " + player.getName() + ", move your weapon away!"));
			}
			else if (npc.getNpcId() == 22125)
			{
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), "Brother " + player.getName() + ", move your weapon away!"));
			}
			else
			{
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), "You cannot carry a weapon without authorization!"));
			}

			switch (npc.getNpcId())
			{
				case 22124:
				case 22126:
					npc.doCast(SkillTable.getInstance().getInfo(4589, 8));
					break;
				default:
					break;
			}

			((L2Attackable) npc).addDamageHate(target, 0, 999);
			npc.getAI().setIntention(CtrlIntention.ATTACK, target);
		}
		else if (_attackersList.get(npcObjId) == null || !_attackersList.get(npcObjId).contains(target))
		{
			((L2Attackable) npc).getAggroListRP().remove(target);
		}
		else
		{
			((L2Attackable) npc).addDamageHate(target, 0, 999);
			npc.getAI().setIntention(CtrlIntention.ATTACK, target);
		}

		return super.onAggroRangeEnter(npc, player, isPet);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		int npcObjId = npc.getObjectId();

		L2Character target = isPet ? attacker.getPet() : attacker;

		if (npc.getNpcId() == 22129 && !isPet && !_isAttacked && Rnd.get(100) < 50 && attacker.getActiveWeaponItem() != null)
		{
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), "Brother " + attacker.getName() + ", move your weapon away!"));
		}

		if (_attackersList.get(npcObjId) == null)
		{
			List<L2Character> player = new ArrayList<>();
			player.add(target);
			_attackersList.put(npcObjId, player);
		}
		else if (!_attackersList.get(npcObjId).contains(target))
		{
			_attackersList.get(npcObjId).add(target);
		}

		_isAttacked = true;

		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcObjId = npc.getObjectId();
		_isAttacked = false;
		if (_attackersList.get(npcObjId) != null)
		{
			_attackersList.get(npcObjId).clear();
		}

		return super.onKill(npc, killer, isPet);
	}
}