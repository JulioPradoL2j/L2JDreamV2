package com.dream.game.model.actor.instance;

import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.tools.random.Rnd;

public class L2WatcherInstance extends L2MonsterInstance
{
	private class Debuff implements Runnable
	{
		private final L2WatcherInstance _watcher;
		private final int _skillsId[] =
		{
			1064,
			1160,
			1170,
			1169,
			1164,
			1165,
			1167,
			1168
		};
		private final int _skillsLvl[] =
		{
			14,
			15,
			13,
			14,
			19,
			3,
			6,
			7
		};

		public Debuff(L2WatcherInstance par)
		{
			_watcher = par;
		}

		@Override
		public void run()
		{
			for (L2Character ch : _watcher.getKnownList().getKnownCharactersInRadius(500))
				if (ch instanceof L2PcInstance)
				{
					int skillRnd = Rnd.get(0, 7);
					L2Skill skill = SkillTable.getInstance().getInfo(_skillsId[skillRnd], _skillsLvl[skillRnd]);
					if (skill != null)
					{
						skill.getEffects(ch, ch);
					}
				}
			ThreadPoolManager.getInstance().scheduleGeneral(new Debuff(_watcher), 3000);
		}
	}

	public L2WatcherInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		if (getNpcId() == 18601)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new Debuff(this), 3000);
		}
	}
}