package com.dream.game.handler.item;

import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.handler.IItemHandler;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2MonsterInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.ActionFailed;

public class SoulCrystals implements IItemHandler
{
	static class CrystalFinalizer implements Runnable
	{
		private final L2PcInstance _activeChar;
		private final L2Attackable _target;
		private final int _crystalId;

		CrystalFinalizer(L2PcInstance activeChar, L2Object target, int crystalId)
		{
			_activeChar = activeChar;
			_target = (L2Attackable) target;
			_crystalId = crystalId;
		}

		@Override
		public void run()
		{
			if (_activeChar.isDead() || _target.isDead())
				return;
			_activeChar.enableAllSkills();
			try
			{
				_target.addAbsorber(_activeChar, _crystalId);
				_activeChar.setTarget(_target);
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}
		}
	}

	private static final int[] ITEM_IDS =
	{
		4629,
		4630,
		4631,
		4632,
		4633,
		4634,
		4635,
		4636,
		4637,
		4638,
		4639,
		5577,
		5580,
		5908,
		9570,
		4640,
		4641,
		4642,
		4643,
		4644,
		4645,
		4646,
		4647,
		4648,
		4649,
		4650,
		5578,
		5581,
		5911,
		9572,
		4651,
		4652,
		4653,
		4654,
		4655,
		4656,
		4657,
		4658,
		4659,
		4660,
		4661,
		5579,
		5582,
		5914
	};

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;

		L2PcInstance activeChar = (L2PcInstance) playable;
		L2Object target = activeChar.getTarget();
		if (!(target instanceof L2MonsterInstance))
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (((L2MonsterInstance) target).getStatus().getCurrentHp() > ((L2MonsterInstance) target).getMaxHp() / 2.0)
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		int crystalId = item.getItemId();

		L2Skill skill = SkillTable.getInstance().getInfo(2096, 1);
		activeChar.useMagic(skill, false, true);
		CrystalFinalizer cf = new CrystalFinalizer(activeChar, target, crystalId);
		ThreadPoolManager.getInstance().scheduleEffect(cf, skill.getHitTime());

	}

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item, boolean par)
	{
	}
}