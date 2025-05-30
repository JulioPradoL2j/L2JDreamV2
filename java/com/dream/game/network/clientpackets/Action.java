package com.dream.game.network.clientpackets;

import com.dream.game.model.L2Object;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;

public final class Action extends L2GameClientPacket
{
	private int _objectId;
	@SuppressWarnings("unused")
	private int _originX, _originY, _originZ;
	private int _actionId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_originX = readD();
		_originY = readD();
		_originZ = readD();
		_actionId = readC();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		activeChar._bbsMultisell = 0;
		if (activeChar.isDead())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (activeChar.inObserverMode())
		{
			activeChar.sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (activeChar.getSecondRefusal())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (activeChar.getActiveRequester() != null)
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		L2Object obj = null;

		if (activeChar.getTargetId() == _objectId)
		{
			obj = activeChar.getTarget();
		}
		else if (_objectId == activeChar.getObjectId())
		{
			obj = activeChar;
		}
		if (obj == null)
		{
			obj = activeChar.getKnownList().getKnownObject(_objectId);
			if (obj == null && activeChar.getParty() != null)
			{
				obj = activeChar.getParty().getMemberById(_objectId);
			}
		}

		if (obj == null)
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (activeChar.getActiveRequester() == null)
		{
			switch (_actionId)
			{
				case 0:
					obj.onAction(activeChar);
					break;
				case 1:
					if (obj instanceof L2Character && ((L2Character) obj).isAlikeDead() && !activeChar.isGM())
					{
						obj.onAction(activeChar);
					}
					else
					{
						obj.onActionShift(activeChar);
					}
					break;
				default:
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					break;
			}
		}
		else
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

}