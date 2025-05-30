package com.dream.game.network.clientpackets;

import com.dream.game.model.L2Object;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;

public class AttackRequest extends L2GameClientPacket
{
	private int _objectId;
	@SuppressWarnings("unused")
	private int _originX, _originY, _originZ;
	@SuppressWarnings("unused")
	private int _attackId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_originX = readD();
		_originY = readD();
		_originZ = readD();
		_attackId = readC();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

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
		final L2Object target;
		if (activeChar.getTargetId() == _objectId)
		{
			target = activeChar.getTarget();
		}
		else
		{
			target = L2World.getInstance().findObject(_objectId);
		}

		if (target == null)
			return;

		if (activeChar.isAttackingNow() && activeChar.isMoving())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (target instanceof L2PcInstance && ((L2PcInstance) target).getAppearance().isInvisible() && !activeChar.isGM())
			return;

		if (activeChar.getTarget() != target)
		{
			target.onAction(activeChar);
		}
		else if (target.getObjectId() != activeChar.getObjectId() && activeChar.getPrivateStoreType() == 0 && activeChar.getActiveRequester() == null)
		{
			target.onForcedAttack(activeChar);
		}
		else
		{
			sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

}