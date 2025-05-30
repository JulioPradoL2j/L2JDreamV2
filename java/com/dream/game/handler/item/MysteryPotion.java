package com.dream.game.handler.item;

import com.dream.game.handler.IItemHandler;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.MagicSkillUse;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.skills.AbnormalEffect;

public class MysteryPotion implements IItemHandler
{
	public class MysteryPotionStop implements Runnable
	{
		private final L2Playable _playable;

		public MysteryPotionStop(L2Playable playable)
		{
			_playable = playable;
		}

		@Override
		public void run()
		{
			try
			{
				if (!(_playable instanceof L2PcInstance))
					return;

				_playable.stopAbnormalEffect(AbnormalEffect.BIG_HEAD);
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}
		}
	}

	private static final int[] ITEM_IDS =
	{
		5234
	};
	private static final int MYSTERY_POTION_SKILL = 2103;

	private static final int EFFECT_DURATION = 1200000; // 20 mins

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

		// Use a summon skill effect for fun ;)
		activeChar.sendPacket(new MagicSkillUse(playable, playable, 2103, 1, 0, 0, false));
		activeChar.broadcastPacket(new MagicSkillUse(playable, playable, 2103, 1, 0, 0, false));

		activeChar.startAbnormalEffect(AbnormalEffect.BIG_HEAD);
		activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);

		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.USE_S1).addSkillName(MYSTERY_POTION_SKILL));
		ThreadPoolManager.getInstance().scheduleEffect(new MysteryPotionStop(playable), EFFECT_DURATION);
	}

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item, boolean par)
	{
	}
}