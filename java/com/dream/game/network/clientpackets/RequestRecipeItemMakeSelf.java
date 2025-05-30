package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.Shutdown;
import com.dream.game.manager.RecipeController;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;

public class RequestRecipeItemMakeSelf extends L2GameClientPacket
{
	private int _id;

	@Override
	protected void readImpl()
	{
		_id = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if (Config.SAFE_REBOOT && Config.SAFE_REBOOT_DISABLE_CREATEITEM && Shutdown.getCounterInstance() != null && Shutdown.getCounterInstance().getCountdown() <= Config.SAFE_REBOOT_TIME)
		{
			activeChar.sendPacket(SystemMessageId.FUNCTION_INACCESSIBLE_NOW);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (activeChar.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_MANUFACTURE || activeChar.isInCraftMode())
		{
			activeChar.sendPacket(SystemMessageId.PRIVATE_STORE_UNDER_WAY);
			return;
		}

		RecipeController.getInstance().requestMakeItem(activeChar, _id);
	}

}