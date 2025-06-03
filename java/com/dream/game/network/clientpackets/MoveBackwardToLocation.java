package com.dream.game.network.clientpackets;

import com.dream.game.ai.CtrlIntention;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.position.L2CharPosition;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.EnchantResult;
import com.dream.game.network.serverpackets.PartyMemberPosition;
import com.dream.game.network.serverpackets.StopMove;
import com.dream.game.templates.item.L2WeaponType;
import com.dream.game.util.IllegalPlayerAction;
import com.dream.game.util.Util;

import java.nio.BufferUnderflowException;

public final class MoveBackwardToLocation extends L2GameClientPacket
{
	private int _targetX, _targetY, _targetZ;
	private int _curX;
	private int _curY;
	private int _originX, _originY, _originZ;

	@SuppressWarnings("unused")
	private int _curZ;

	private Integer _moveMovement;

	@Override
	protected void readImpl()
	{
		_targetX = readD();
		_targetY = readD();
		_targetZ = readD();
		_originX = readD();
		_originY = readD();
		_originZ = readD();
		try
		{
			_moveMovement = readD();
		}
		catch (BufferUnderflowException e)
		{
			L2PcInstance activeChar = getClient().getActiveChar();
			activeChar.sendPacket(SystemMessageId.HACKING_TOOL);
			Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " trying to use l2walker!", IllegalPlayerAction.PUNISH_KICK);
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
			return;

		if (activeChar.getPrivateStoreType() != 0)
		{
			getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (activeChar.isSitting())
		{
			getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (activeChar.isTeleporting())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (activeChar.getActiveEnchantItem() != null)
		{
			activeChar.sendPacket(new EnchantResult(0));
			activeChar.setActiveEnchantItem(null);
		}

		activeChar._inWorld = true;

		if (_moveMovement == null)
		{
			Util.handleIllegalPlayerAction(activeChar, "Bot usage for movement by " + activeChar, IllegalPlayerAction.PUNISH_KICK);
			return;
		}

		if (_targetX == _originX && _targetY == _originY && _targetZ == _originZ)
		{
			activeChar.sendPacket(new StopMove(activeChar));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		_curX = activeChar.getX();
		_curY = activeChar.getY();
		_curZ = activeChar.getZ();

		if (activeChar.isInBoat())
		{
			activeChar.setInBoat(false);
		}

		if (activeChar.getTeleMode() > 0)
		{
			activeChar.teleToLocation(_targetX, _targetY, _targetZ, false);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (activeChar.isFakeDeath())
			return;

		if (activeChar.isDead())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (activeChar.isAutoFarm())
		{
			activeChar.setAutoFarm(false);
			activeChar.sendMessage("Autofarm stop.");
		}

		if (_moveMovement == 0)
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else if (activeChar.isAttackingNow() && activeChar.getActiveWeaponItem() != null && activeChar.getActiveWeaponItem().getItemType() == L2WeaponType.BOW)
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			double dx = _targetX - _curX;
			double dy = _targetY - _curY;

			if (activeChar.isOutOfControl() || dx * dx + dy * dy > 98010000)
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			if (activeChar.isMoving() && activeChar.isAttackingNow())
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			activeChar.getAI().setIntention(CtrlIntention.MOVE_TO, new L2CharPosition(_targetX, _targetY, _targetZ, 0));

			if (activeChar.getParty() != null)
			{
				activeChar.getParty().broadcastToPartyMembers(activeChar, new PartyMemberPosition(activeChar));
			}
		}
	}

}