package com.dream.game.network.clientpackets;

import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.util.Util;
import com.dream.tools.geometry.Point3D;

public final class RequestExMagicSkillUseGround extends L2GameClientPacket
{
	private int _x, _y, _z, _skillId;
	private boolean _ctrlPressed, _shiftPressed;

	@Override
	protected void readImpl()
	{
		_x = readD();
		_y = readD();
		_z = readD();
		_skillId = readD();
		_ctrlPressed = readD() != 0;
		_shiftPressed = readC() != 0;
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
			return;

		int level = activeChar.getSkillLevel(_skillId);
		if (level <= 0)
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2Skill skill = SkillTable.getInstance().getInfo(_skillId, level);

		if (skill != null)
		{
			activeChar.setCurrentSkillWorldPosition(new Point3D(_x, _y, _z));

			activeChar.setHeading(Util.calculateHeadingFrom(activeChar.getX(), activeChar.getY(), _x, _y));
			activeChar.broadcastPacket(new ValidateLocation(activeChar));
			activeChar.useMagic(skill, _ctrlPressed, _shiftPressed);
		}
		else
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

}