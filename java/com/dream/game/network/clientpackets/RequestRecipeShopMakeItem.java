package com.dream.game.network.clientpackets;

import com.dream.game.manager.RecipeController;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.util.Util;

public class RequestRecipeShopMakeItem extends L2GameClientPacket
{
	private int _id;
	private int _recipeId;
	@SuppressWarnings("unused")
	private int _unknown;

	@Override
	protected void readImpl()
	{
		_id = readD();
		_recipeId = readD();
		_unknown = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		L2Object object = null;

		if (activeChar.getTargetId() == _id)
		{
			object = activeChar.getTarget();
		}

		if (object == null)
		{
			object = L2World.getInstance().getPlayer(_id);
		}

		if (!(object instanceof L2PcInstance))
			return;

		L2PcInstance manufacturer = (L2PcInstance) object;

		if (activeChar.getPrivateStoreType() != 0)
		{
			activeChar.sendPacket(SystemMessageId.PRIVATE_STORE_UNDER_WAY);
			return;
		}
		if (manufacturer.getPrivateStoreType() != 5)
			return;

		if (activeChar.isInCraftMode() || manufacturer.isInCraftMode())
			return;
		if (manufacturer.isInDuel() || activeChar.isInDuel())
		{
			activeChar.sendPacket(SystemMessageId.CANT_CRAFT_DURING_COMBAT);
			return;
		}
		if (activeChar.getInventoryLimit() - activeChar.getInventory().getSize() <= 1)
		{
			activeChar.sendPacket(SystemMessageId.SLOTS_FULL);
			return;
		}

		if (Util.checkIfInRange(150, activeChar, manufacturer, true))
		{
			RecipeController.getInstance().requestManufactureItem(manufacturer, _recipeId, activeChar);
		}
	}

}