package com.dream.game.model.quest.pack.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dream.game.ai.CtrlIntention;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.NpcSay;
import com.dream.tools.random.Rnd;

public class SummonMinions extends L2AttackableAIScript
{
	private static int HasSpawned;
	private static HashSet<Integer> myTrackingSet = new HashSet<>();
	private static final Map<Integer, Integer[]> MINIONS = new HashMap<>();

	static
	{
		MINIONS.put(20767, new Integer[]
		{
			20768,
			20769,
			20770
		});
		MINIONS.put(21524, new Integer[]
		{
			21525
		});
		MINIONS.put(21531, new Integer[]
		{
			21658
		});
		MINIONS.put(21539, new Integer[]
		{
			21540
		});
		MINIONS.put(22257, new Integer[]
		{
			18364,
			18364
		});
		MINIONS.put(22258, new Integer[]
		{
			18364,
			18364
		});
		MINIONS.put(22259, new Integer[]
		{
			18364,
			18364
		});
		MINIONS.put(22260, new Integer[]
		{
			18364,
			18364
		});
		MINIONS.put(22261, new Integer[]
		{
			18365,
			18365
		});
		MINIONS.put(22262, new Integer[]
		{
			18365,
			18365
		});
		MINIONS.put(22263, new Integer[]
		{
			18365,
			18365
		});
		MINIONS.put(22264, new Integer[]
		{
			18366,
			18366
		});
		MINIONS.put(22265, new Integer[]
		{
			18366,
			18366
		});
		MINIONS.put(22266, new Integer[]
		{
			18366,
			18366
		});
	}

	private final Map<Integer, List<L2PcInstance>> _attackersList = new ConcurrentHashMap<>();

	public SummonMinions()
	{
		super(-1, "SummonMinions", "ai");
		int[] temp =
		{
			20767,
			21524,
			21531,
			21539,
			22257,
			22258,
			22259,
			22260,
			22261,
			22262,
			22263,
			22264,
			22265,
			22266
		};
		registerMobs(temp);
	}

	@SuppressWarnings("null")
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		int npcId = npc.getNpcId();
		int npcObjId = npc.getObjectId();
		if (MINIONS.containsKey(npcId))
		{
			if (!myTrackingSet.contains(npcObjId))
			{
				myTrackingSet.add(npcObjId);
				HasSpawned = npcObjId;
			}
			if (HasSpawned == npcObjId)
				if (npcId == 22030 || npcId == 22032 || npcId == 22038)
				{
					if (npc.getStatus().getCurrentHp() < npc.getMaxHp() / 2)
					{
						HasSpawned = 0;
						if (Rnd.get(100) < 33)
						{
							Integer[] minions = MINIONS.get(npcId);
							for (Integer minion : minions)
							{
								L2Attackable newNpc = (L2Attackable) this.addSpawn(minion, npc.getX() + Rnd.get(-150, 150), npc.getY() + Rnd.get(-150, 150), npc.getZ(), 0, false, 0);
								newNpc.setRunning();
								newNpc.addDamageHate(attacker, 0, 999);
								newNpc.getAI().setIntention(CtrlIntention.ATTACK, attacker);
							}
							minions = null;
						}
					}
				}
				else if (npcId == 22257 || npcId == 22258 || npcId == 22259 || npcId == 22260 || npcId == 22261 || npcId == 22262 || npcId == 22263 || npcId == 22264 || npcId == 22265 || npcId == 22266)
				{
					if (isPet)
					{
						attacker = attacker.getPet().getOwner();
					}
					if (attacker.getParty() != null)
					{
						for (L2PcInstance member : attacker.getParty().getPartyMembers())
							if (_attackersList.get(npcObjId) == null)
							{
								List<L2PcInstance> player = new ArrayList<>();
								player.add(member);
								_attackersList.put(npcObjId, player);
							}
							else if (!_attackersList.get(npcObjId).contains(member))
							{
								_attackersList.get(npcObjId).add(member);
							}
					}
					else if (_attackersList.get(npcObjId) == null)
					{
						List<L2PcInstance> player = new ArrayList<>();
						player.add(attacker);
						_attackersList.put(npcObjId, player);
					}
					else if (!_attackersList.get(npcObjId).contains(attacker))
					{
						_attackersList.get(npcObjId).add(attacker);
					}
					if (attacker != null && (attacker.getParty() != null && attacker.getParty().getMemberCount() > 2 || _attackersList.get(npcObjId).size() > 2)) // Just to make
					{
						HasSpawned = 0;
						Integer[] minions = MINIONS.get(npcId);
						for (Integer minion : minions)
						{
							L2Attackable newNpc = (L2Attackable) addSpawn(minion, npc.getX() + Rnd.get(-150, 150), npc.getY() + Rnd.get(-150, 150), npc.getZ(), 0, false, 0);
							newNpc.setRunning();
							newNpc.addDamageHate(attacker, 0, 999);
							newNpc.getAI().setIntention(CtrlIntention.ATTACK, attacker);
						}
						minions = null;
					}
				}
				else
				{
					HasSpawned = 0;
					Integer[] minions = MINIONS.get(npcId);
					if (npcId != 20767)
					{
						for (Integer minion : minions)
						{
							L2Attackable newNpc = (L2Attackable) this.addSpawn(minion, npc.getX() + Rnd.get(-150, 150), npc.getY() + Rnd.get(-150, 150), npc.getZ(), 0, false, 0);
							newNpc.setRunning();
							newNpc.addDamageHate(attacker, 0, 999);
							newNpc.getAI().setIntention(CtrlIntention.ATTACK, attacker);
						}
					}
					else
					{
						for (Integer minion : minions)
						{
							this.addSpawn(minion, npc.getX() + Rnd.get(-100, 100), npc.getY() + Rnd.get(-100, 100), npc.getZ(), 0, false, 0);
						}
					}
					minions = null;
					if (npcId == 20767)
					{
						npc.broadcastPacket(new NpcSay(npcObjId, 0, npcId, "Come out, you children of darkness!"));
					}
				}
		}
		if (_attackersList.get(npcObjId) != null)
		{
			_attackersList.get(npcObjId).clear();
		}

		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		int npcObjId = npc.getObjectId();
		if (MINIONS.containsKey(npcId))
		{
			myTrackingSet.remove(npcObjId);
		}
		return super.onKill(npc, killer, isPet);
	}
}