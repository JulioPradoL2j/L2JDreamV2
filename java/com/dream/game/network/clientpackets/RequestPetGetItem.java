package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.manager.MercTicketManager;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2PetInstance;
import com.dream.game.model.actor.instance.L2SummonInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.serverpackets.ActionFailed;

public class RequestPetGetItem extends L2GameClientPacket
{
	private int _objectId;
	private L2ItemInstance item;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		player._bbsMultisell = 0;
		L2Object obj = player.getKnownList().getKnownObject(_objectId);

		if (obj == null)
		{
			obj = L2World.getInstance().findObject(_objectId);
		}

		if (!(obj instanceof L2ItemInstance))
			return;

		if (player.getPet() == null || player.getPet() instanceof L2SummonInstance)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		item = (L2ItemInstance) obj;
		if (item == null)
			return;
		if (item.getItemId() == Config.FORTSIEGE_COMBAT_FLAG_ID)
		{
			player.sendMessage("Pet can't get the Combat Flag.");
			return;
		}

		int castleId = MercTicketManager.getTicketCastleId(item.getItemId());
		if (castleId > 0)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		L2PetInstance pet = (L2PetInstance) player.getPet();
		if (pet.isDead() || pet.isOutOfControl())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		pet.getAI().setIntention(CtrlIntention.PICK_UP, item);
	}

}