package com.dream.game.model.quest.pack.ai;

import com.dream.game.manager.grandbosses.VanHalterManager;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.quest.Quest;

public class Vanhalter extends Quest
{
	private final int ANDREAS_VAN_HALTER = 29062;
	private final int ANDREAS_CAPTAIN = 22188;
	private final int TRIOLS[] =
	{
		32058,
		32059,
		32060,
		32061,
		32062,
		32063,
		32064,
		32065,
		32066
	};

	public Vanhalter()
	{
		super(-1, "vanhalter", "ai");
		addAttackId(ANDREAS_VAN_HALTER);
		addKillId(ANDREAS_VAN_HALTER);
		addKillId(ANDREAS_CAPTAIN);
		for (int Triol : TRIOLS)
		{
			addKillId(Triol);
		}
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		int npcId = npc.getNpcId();
		if (npcId == ANDREAS_VAN_HALTER)
		{
			int maxHp = npc.getMaxHp();
			double curHp = npc.getStatus().getCurrentHp();
			if (curHp / maxHp * 100 <= 20)
			{
				VanHalterManager.getInstance().callRoyalGuardHelper();
			}
		}
		return null;
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (TRIOLS.equals(npcId))
		{
			VanHalterManager.getInstance().removeBleeding(npcId);
			VanHalterManager.getInstance().checkTriolRevelationDestroy();
		}
		if (npcId == ANDREAS_CAPTAIN)
		{
			VanHalterManager.getInstance().checkRoyalGuardCaptainDestroy();
		}
		if (npcId == ANDREAS_VAN_HALTER)
		{
			VanHalterManager.getInstance().enterInterval();
		}
		return null;
	}
}