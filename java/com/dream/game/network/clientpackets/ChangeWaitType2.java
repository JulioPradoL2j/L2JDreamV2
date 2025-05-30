package com.dream.game.network.clientpackets;

import com.dream.game.model.L2Object;
import com.dream.game.model.actor.instance.L2StaticObjectInstance;
import com.dream.game.network.SystemChatChannelId;

public class ChangeWaitType2 extends L2GameClientPacket
{
	private boolean _typeStand;

	@Override
	protected void readImpl()
	{
		_typeStand = readD() == 1;
	}

	@Override
	protected void runImpl()
	{
		L2Object target = getClient().getActiveChar().getTarget();
		if (getClient().getActiveChar() != null)
		{
			if (getClient().getActiveChar().getMountType() != 0)
				return;

			if (target != null && target instanceof L2StaticObjectInstance && !getClient().getActiveChar().isSitting())
			{
				if (!((L2StaticObjectInstance) target).useThrone(getClient().getActiveChar()))
				{
					getClient().getActiveChar().sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "You cannot take the throne.");
				}

				return;
			}
			if (_typeStand)
			{
				if (getClient().getActiveChar().getObjectSittingOn() != null)
				{
					getClient().getActiveChar().getObjectSittingOn().setBusyStatus(null);
					getClient().getActiveChar().setObjectSittingOn(null);
				}
				getClient().getActiveChar().standUp(false);
			}
			else
			{
				getClient().getActiveChar().sitDown(false);
			}
		}
	}

}