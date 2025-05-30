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
package com.dream.game.model;

public final class TradeItem
{
	private int _objectId;
	private int _itemId;
	private int _price;
	private int _storePrice;
	private int _count;
	private int _enchantLevel;

	public TradeItem()
	{

	}

	public int getCount()
	{
		return _count;
	}

	public int getEnchantLevel()
	{
		return _enchantLevel;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public int getObjectId()
	{
		return _objectId;
	}

	public int getOwnersPrice()
	{
		return _price;
	}

	public int getStorePrice()
	{
		return _storePrice;
	}

	public void setCount(int count)
	{
		_count = count;
	}

	public void setEnchantLevel(int enchant)
	{
		_enchantLevel = enchant;
	}

	public void setItemId(int id)
	{
		_itemId = id;
	}

	public void setObjectId(int id)
	{
		_objectId = id;
	}

	public void setOwnersPrice(int price)
	{
		_price = price;
	}

	public void setstorePrice(int price)
	{
		_storePrice = price;
	}
}