package com.dream.game.skills.l2skills;

import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.tools.random.Rnd;
import com.dream.util.StatsSet;

public class L2SkillCreateItem extends L2Skill
{
	private final int[] _createItemId;
	private final int _createItemCount;
	private final int _randomCount;

	public L2SkillCreateItem(StatsSet set)
	{
		super(set);
		_createItemId = set.getIntegerArray("create_item_id");
		_createItemCount = set.getInteger("create_item_count", 0);
		_randomCount = set.getInteger("random_count", 1);
	}

	public void giveItems(L2PcInstance activeChar, int itemId, int count)
	{

		activeChar.addItem(getName(), itemId, count, activeChar, true);
	}

	@Override
	public void useSkill(L2Character activeChar, L2Character... targets)
	{
		if (activeChar.isAlikeDead())
			return;
		if (_createItemId == null || _createItemCount == 0)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE).addSkillName(this));
			return;
		}
		L2PcInstance player = (L2PcInstance) activeChar;
		if (activeChar instanceof L2PcInstance)
		{
			int rnd = Rnd.nextInt(_randomCount) + 1;
			int count = _createItemCount * rnd;
			int rndid = Rnd.nextInt(_createItemId.length);
			giveItems(player, _createItemId[rndid], count);
		}
	}
}