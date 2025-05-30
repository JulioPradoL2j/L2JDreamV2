package com.dream.game.network.clientpackets;

import java.util.ArrayList;
import java.util.List;

import com.dream.game.manager.CursedWeaponsManager;
import com.dream.game.model.CursedWeapon;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.world.Location;
import com.dream.game.network.serverpackets.ExCursedWeaponLocation;
import com.dream.game.network.serverpackets.ExCursedWeaponLocation.CursedWeaponInfo;

public class RequestCursedWeaponLocation extends L2GameClientPacket
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

		List<CursedWeaponInfo> list = new ArrayList<>();
		for (CursedWeapon cw : CursedWeaponsManager.getInstance().getCursedWeapons())
		{
			if (!cw.isActive())
			{
				continue;
			}

			Location loc = cw.getCurrentLocation();

			if (loc != null)
			{
				list.add(new CursedWeaponInfo(loc, cw.getItemId(), cw.isActivated() ? 1 : 0));
			}
		}

		if (!list.isEmpty())
		{
			activeChar.sendPacket(new ExCursedWeaponLocation(list));
		}
	}

}