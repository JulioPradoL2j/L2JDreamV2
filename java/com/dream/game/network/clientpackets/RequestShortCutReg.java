package com.dream.game.network.clientpackets;

import com.dream.game.model.L2ShortCut;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.ShortCutRegister;

public class RequestShortCutReg extends L2GameClientPacket
{
	private int _type, _id, _slot, _page, _characterType;

	@Override
	protected void readImpl()
	{
		_type = readD();
		int slot = readD();
		_id = readD();
		_characterType = readD();

		_slot = slot % 12;
		_page = slot / 12;
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if (_page > 10 || _page < 0)
			return;

		switch (_type)
		{
			case 0x01: // item
			case 0x03: // action
			case 0x04: // macro
			case 0x05: // recipe
			{
				final L2ShortCut sc = new L2ShortCut(_slot, _page, _type, _id, -1, _characterType);
				sendPacket(new ShortCutRegister(sc));
				activeChar.registerShortCut(sc);
				break;
			}
			case 0x02: // skill
			{
				int level = activeChar.getSkillLevel(_id);
				if (level > 0)
				{
					final L2ShortCut sc = new L2ShortCut(_slot, _page, _type, _id, level, _characterType);
					sendPacket(new ShortCutRegister(sc));
					activeChar.registerShortCut(sc);
				}
				break;
			}
		}
	}

}