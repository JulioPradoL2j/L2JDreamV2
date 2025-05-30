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
 * 
 */
package com.dream.game.model;

public class L2SkinItem
{
	private int _itemId;
	private final int _hairId;
	private final int _chestId;
	private final int _legsId;
	private final int _glovesId;
	private final int _feetId;

	public L2SkinItem(int itemId, int hairId, int chestId, int legsId, int glovesId, int feetId)
	{
		_itemId = itemId;
		_hairId = hairId;
		_chestId = chestId;
		_legsId = legsId;
		_glovesId = glovesId;
		_feetId = feetId;
	}


	public int getItemId()
	{
		return _itemId;
	}
	
	public int getItemIdd()
	{
		return _itemId;
	}
	
	public void setItemIdd(int val)
	{
		_itemId = val;
	}

	public int getHairId()
	{
		return _hairId;
	}

	public int getChestId()
	{
		return _chestId;
	}

	public int getLegsId()
	{
		return _legsId;
	}

	public int getGlovesId()
	{
		return _glovesId;
	}

	public int getBootsId()
	{
		return _feetId;
	}


}