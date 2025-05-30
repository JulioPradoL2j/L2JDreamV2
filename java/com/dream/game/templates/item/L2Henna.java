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
package com.dream.game.templates.item;

import com.dream.util.StatsSet;

public final class L2Henna
{
	public final int symbolId;
	public final String symbolName;
	public final int dye;
	public final int price;
	public final int amount;
	public final int statINT;
	public final int statSTR;
	public final int statCON;
	public final int statMEM;
	public final int statDEX;
	public final int statWIT;

	public L2Henna(StatsSet set)
	{
		symbolId = set.getInteger("symbol_id");
		symbolName = "";
		dye = set.getInteger("dye");
		price = set.getInteger("price");
		amount = set.getInteger("amount");
		statINT = set.getInteger("stat_INT");
		statSTR = set.getInteger("stat_STR");
		statCON = set.getInteger("stat_CON");
		statMEM = set.getInteger("stat_MEM");
		statDEX = set.getInteger("stat_DEX");
		statWIT = set.getInteger("stat_WIT");
	}

	public int getAmountDyeRequire()
	{
		return amount;
	}

	public int getDyeId()
	{
		return dye;
	}

	public int getPrice()
	{
		return price;
	}

	public int getStatCON()
	{
		return statCON;
	}

	public int getStatDEX()
	{
		return statDEX;
	}

	public int getStatINT()
	{
		return statINT;
	}

	public int getStatMEM()
	{
		return statMEM;
	}

	public int getStatSTR()
	{
		return statSTR;
	}

	public int getStatWIT()
	{
		return statWIT;
	}

	public int getSymbolId()
	{
		return symbolId;
	}
}