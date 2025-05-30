/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <[url="http://www.gnu.org/licenses/>."]http://www.gnu.org/licenses/>.[/url]
 */
package com.dream.game.model.zone;

import com.dream.Config;
import com.dream.game.datatables.xml.MapRegionTable;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;

public class L2AllowAioZone extends L2DefaultZone
{
	@Override
	protected void onEnter(L2Character character)
	{
		if (!Config.ALLOW_AIO_LEAVE_TOWN)
		{
			if (character instanceof L2PcInstance)
			{
				L2PcInstance activeChar = ((L2PcInstance) character);
				if (activeChar.isAio() && !character.isInsideZone(L2Zone.FLAG_ALLOWAIO))
				{
					activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
				}
			}
		}
		character.setInsideZone(this, FLAG_ALLOWAIO, true);
		super.onEnter(character);
	}

	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(this, FLAG_ALLOWAIO, false);
		super.onExit(character);			
		if (character instanceof L2PcInstance)
		{
			L2PcInstance activeChar = ((L2PcInstance) character);
			if (activeChar.isAio() && !character.isInsideZone(L2Zone.FLAG_ALLOWAIO))
			{
				activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
			}
		}
	}

	@Override
	public void onDieInside(L2Character character)
	{

	}

	@Override
	public void onReviveInside(L2Character character)
	{
		onEnter(character);
	}

}