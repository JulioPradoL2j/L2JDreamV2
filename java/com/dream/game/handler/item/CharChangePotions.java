package com.dream.game.handler.item;

import com.dream.game.handler.IItemHandler;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.MagicSkillUse;
import com.dream.game.network.serverpackets.UserInfo;

public class CharChangePotions implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		5235,
		5236,
		5237,
		5238,
		5239,
		5240,
		5241,
		5242,
		5243,
		5244,
		5245,
		5246,
		5247,
		5248
	};

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		int itemId = item.getItemId();

		if (!(playable instanceof L2PcInstance))
			return;

		L2PcInstance activeChar = (L2PcInstance) playable;

		if (activeChar.isAllSkillsDisabled() || activeChar.isCastingNow())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		switch (itemId)
		{
			case 5235:
				activeChar.getAppearance().setFace(0);
				break;
			case 5236:
				activeChar.getAppearance().setFace(1);
				break;
			case 5237:
				activeChar.getAppearance().setFace(2);
				break;
			case 5238:
				activeChar.getAppearance().setHairColor(0);
				break;
			case 5239:
				activeChar.getAppearance().setHairColor(1);
				break;
			case 5240:
				activeChar.getAppearance().setHairColor(2);
				break;
			case 5241:
				activeChar.getAppearance().setHairColor(3);
				break;
			case 5242:
				activeChar.getAppearance().setHairStyle(0);
				break;
			case 5243:
				activeChar.getAppearance().setHairStyle(1);
				break;
			case 5244:
				activeChar.getAppearance().setHairStyle(2);
				break;
			case 5245:
				activeChar.getAppearance().setHairStyle(3);
				break;
			case 5246:
				activeChar.getAppearance().setHairStyle(4);
				break;
			case 5247:
				activeChar.getAppearance().setHairStyle(5);
				break;
			case 5248:
				activeChar.getAppearance().setHairStyle(6);
				break;
		}

		activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2003, 1, 1, 0, false));
		activeChar.store();
		activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
		activeChar.broadcastPacket(new UserInfo(activeChar));
	}

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item, boolean par)
	{
	}
}