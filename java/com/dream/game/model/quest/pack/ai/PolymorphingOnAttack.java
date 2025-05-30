package com.dream.game.model.quest.pack.ai;

import java.util.HashMap;
import java.util.Map;

import com.dream.game.ai.CtrlIntention;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.serverpackets.CreatureSay;
import com.dream.tools.random.Rnd;

public class PolymorphingOnAttack extends L2AttackableAIScript
{
	private static final Map<Integer, Integer[]> MOBSPAWNS = new HashMap<>();

	static
	{
		MOBSPAWNS.put(21258, new Integer[]
		{
			21259,
			100,
			100,
			-1
		});
		MOBSPAWNS.put(21261, new Integer[]
		{
			21262,
			100,
			20,
			0
		});
		MOBSPAWNS.put(21262, new Integer[]
		{
			21263,
			100,
			10,
			1
		});
		MOBSPAWNS.put(21263, new Integer[]
		{
			21264,
			100,
			5,
			2
		});
		MOBSPAWNS.put(21265, new Integer[]
		{
			21271,
			100,
			33,
			0
		});
		MOBSPAWNS.put(21266, new Integer[]
		{
			21269,
			100,
			100,
			-1
		});
		MOBSPAWNS.put(21267, new Integer[]
		{
			21270,
			100,
			100,
			-1
		});
		MOBSPAWNS.put(21271, new Integer[]
		{
			21272,
			66,
			10,
			1
		});
		MOBSPAWNS.put(21272, new Integer[]
		{
			21273,
			33,
			5,
			2
		});
		MOBSPAWNS.put(21521, new Integer[]
		{
			21522,
			100,
			30,
			-1
		});
		MOBSPAWNS.put(21527, new Integer[]
		{
			21528,
			100,
			30,
			-1
		});
		MOBSPAWNS.put(21533, new Integer[]
		{
			21534,
			100,
			30,
			-1
		});
		MOBSPAWNS.put(21537, new Integer[]
		{
			21538,
			100,
			30,
			-1
		});
	}

	protected static final String[][] MOBTEXTS =
	{
		new String[]
		{
			"Enough fooling around. Get ready to die!",
			"You idiot! I've just been toying with you!",
			"Now the fun starts!"
		},
		new String[]
		{
			"I must admit, no one makes my blood boil quite like you do!",
			"Now the battle begins!",
			"Witness my true power!"
		},
		new String[]
		{
			"Prepare to die!",
			"I'll double my strength!",
			"You have more skill than I thought."
		}
	};

	public PolymorphingOnAttack()
	{
		super(-1, "polymorphing_on_attack", "ai");
		for (int id : MOBSPAWNS.keySet())
		{
			super.addAttackId(id);
		}
	}

	@Override
	public synchronized String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (npc.getStatus().getCurrentHp() <= damage)
			return super.onAttack(npc, attacker, damage, isPet);
		if (MOBSPAWNS.containsKey(npc.getNpcId()))
		{
			Integer[] tmp = MOBSPAWNS.get(npc.getNpcId());
			if (npc.getStatus().getCurrentHp() <= npc.getMaxHp() * tmp[1] / 100 && Rnd.get(100) < tmp[2])
			{
				if (tmp[3] >= 0)
				{
					String text = MOBTEXTS[tmp[3]][Rnd.get(MOBTEXTS[tmp[3]].length)];
					npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Normal, npc.getName(), text));
				}
				npc.getSpawn().decreaseCount(npc);
				npc.deleteMe();
				L2Attackable newNpc = (L2Attackable) addSpawn(tmp[0], npc.getX(), npc.getY(), npc.getZ() + 10, npc.getHeading(), false, 0, true);
				L2Character originalAttacker = isPet ? attacker.getPet() : attacker;
				newNpc.setRunning();
				newNpc.addDamageHate(originalAttacker, 0, 500);
				newNpc.getAI().setIntention(CtrlIntention.ATTACK, originalAttacker);
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
}