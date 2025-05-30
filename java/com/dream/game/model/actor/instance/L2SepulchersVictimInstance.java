package com.dream.game.model.actor.instance;

import com.dream.game.ai.L2CharacterAI;
import com.dream.game.model.actor.L2Character;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.NpcSay;
import com.dream.game.network.serverpackets.Revive;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2SepulchersVictimInstance extends L2SepulcherMonsterInstance
{

	public L2SepulchersVictimInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
	}

	@Override
	public void deleteMe()
	{
		if (!isDead())
		{
			broadcastPacket(new NpcSay(getObjectId(), 1, getNpcId(), "Thanks you! Recive my blessing!"));
			for (L2PcInstance pc : _mausoleum.getPlayersInside())
			{
				if (pc.isDead())
				{
					pc.doRevive(100);
					pc.sendPacket(new Revive(pc));
				}
				pc.restoreHPMP();
				pc.resetSkillTime(true);
			}
		}
		super.deleteMe();
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (super.doDie(killer))
		{
			_mausoleum.nextStage();
			return true;
		}
		return false;
	}

	@Override
	public L2CharacterAI getAI()
	{
		if (_ai == null)
		{
			_ai = new L2CharacterAI(new AIAccessor());
		}
		return _ai;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if (attacker instanceof L2SepulcherMonsterInstance)
			if (((L2SepulcherMonsterInstance) attacker).getNpcId() == 18170)
				return true;
		return false;
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				getKnownList().updateKnownObjects();
				for (L2Character ch : getKnownList().getKnownCharactersInRadius(1000))
					if (ch instanceof L2SepulcherMonsterInstance)
						if (((L2SepulcherMonsterInstance) ch).getNpcId() == 18170)
						{
							((L2SepulcherMonsterInstance) ch).addDamageHate(L2SepulchersVictimInstance.this, 1, 100);
							break;
						}
			}
		}, 5000);
	}
}