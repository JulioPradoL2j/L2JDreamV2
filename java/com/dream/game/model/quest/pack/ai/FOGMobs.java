package com.dream.game.model.quest.pack.ai;

import java.util.ArrayList;
import java.util.List;

import com.dream.Config;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.tools.random.Rnd;

public class FOGMobs extends L2AttackableAIScript
{
	private final List<L2Character> _mobs = new ArrayList<>();

	public FOGMobs()
	{
		super(-1, "Fog_mobs", "ai");
		registerMobs(new int[]
		{
			21376,
			21377,
			21378,
			21379,
			21380,
			21381,
			21384,
			21388,
			21389,
			21390,
			21392,
			21394,
			21395,
			21652,
			21653,
			21654,
			21656
		});
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		synchronized (_mobs)
		{
			if (_mobs.contains(npc))
			{
				_mobs.remove(npc);
			}
			else if (Rnd.get(100) < Config.FOG_MOBS_CLONE_CHANCE)
			{
				for (int i = 0; i < 1 + Rnd.get(5); i++)
				{
					L2Attackable mob = (L2Attackable) addSpawn(npc.getNpcId(), npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
					_mobs.add(mob);
					mob.addDamageHate(killer, 1, 100);
				}
			}
			return null;
		}
	}
}