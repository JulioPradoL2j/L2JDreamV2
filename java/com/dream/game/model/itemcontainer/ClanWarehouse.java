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
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.dream.game.model.itemcontainer;

import com.dream.Config;
import com.dream.game.model.L2Clan;
import com.dream.game.model.actor.instance.L2ItemInstance.ItemLocation;
import com.dream.game.model.actor.instance.L2PcInstance;

public final class ClanWarehouse extends Warehouse
{
	public static String getLocationId()
	{
		return "0";
	}

	public static int getLocationId(boolean dummy)
	{
		return 0;
	}

	private final L2Clan _clan;

	public ClanWarehouse(L2Clan clan)
	{
		_clan = clan;
	}

	@Override
	public ItemLocation getBaseLocation()
	{
		return ItemLocation.CLANWH;
	}

	@Override
	public L2PcInstance getOwner()
	{
		return _clan.getLeader().getPlayerInstance();
	}

	@Override
	public int getOwnerId()
	{
		return _clan.getClanId();
	}

	public void setLocationId(L2PcInstance dummy)
	{

	}

	@Override
	public boolean validateCapacity(int slots)
	{
		return _items.size() + slots <= Config.WAREHOUSE_SLOTS_CLAN;
	}
}