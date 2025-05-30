package com.dream.game.network.clientpackets;

import java.util.ArrayList;
import java.util.List;

import com.dream.game.manager.CursedWeaponsManager;
import com.dream.game.model.actor.L2Character;
import com.dream.game.network.serverpackets.ExCursedWeaponList;

public class RequestCursedWeaponList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		L2Character activeChar = getClient().getActiveChar();

		if (activeChar == null)
			return;

		List<Integer> list = new ArrayList<>();
		for (int id : CursedWeaponsManager.getInstance().getCursedWeaponsIds())
		{
			list.add(id);
		}
		activeChar.sendPacket(new ExCursedWeaponList(list));
	}

}