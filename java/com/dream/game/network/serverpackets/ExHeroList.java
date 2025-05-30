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
package com.dream.game.network.serverpackets;

import java.util.Collection;

import com.dream.game.model.entity.Hero;
import com.dream.game.model.olympiad.Olympiad;
import com.dream.util.StatsSet;

public class ExHeroList extends L2GameServerPacket
{
	private final Collection<StatsSet> _heroList;

	public ExHeroList()
	{
		_heroList = Hero.getInstance().getHeroes().values();
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x23);

		writeD(_heroList.size());
		for (StatsSet hero : _heroList)
		{
			writeS(hero.getString(Olympiad.CHAR_NAME));
			writeD(hero.getInteger(Olympiad.CLASS_ID));
			writeS(hero.getString(Hero.CLAN_NAME, ""));
			writeD(hero.getInteger(Hero.CLAN_CREST, 0));
			writeS(hero.getString(Hero.ALLY_NAME, ""));
			writeD(hero.getInteger(Hero.ALLY_CREST, 0));
			writeD(hero.getInteger(Hero.COUNT));
		}
	}

}