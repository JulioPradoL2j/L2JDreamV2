package com.dream.game.network.clientpackets;

import com.dream.game.manager.RecipeController;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;

public class RequestRecipeBookOpen extends L2GameClientPacket
{
	private boolean _isDwarvenCraft;

	@Override
	protected void readImpl()
	{
		_isDwarvenCraft = readD() == 0;
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if (activeChar.isCastingNow() || activeChar.isAllSkillsDisabled())
		{
			activeChar.sendPacket(SystemMessageId.NO_RECIPE_BOOK_WHILE_CASTING);
			return;
		}

		activeChar._bbsMultisell = 0;
		if (activeChar.getPrivateStoreType() != 0)
		{
			activeChar.sendPacket(SystemMessageId.PRIVATE_STORE_UNDER_WAY);
			return;
		}

		RecipeController.getInstance().requestBookOpen(activeChar, _isDwarvenCraft);
	}

}