package com.dream.game.model.actor.instance;

import com.dream.game.ai.L2BaiumAngelAI;
import com.dream.game.ai.L2CharacterAI;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2BaiumAngelInstance extends L2MonsterInstance
{

	private L2GrandBossInstance _boss;

	public L2BaiumAngelInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public L2CharacterAI getAI()
	{
		if (_ai == null)
		{
			_ai = new L2BaiumAngelAI(new AIAccessor());
		}
		return _ai;
	}

	public L2GrandBossInstance getBoss()
	{
		return _boss;
	}

	@Override
	public void returnHome()
	{

	}

	public void setBoss(L2GrandBossInstance boss)
	{
		_boss = boss;
	}

}