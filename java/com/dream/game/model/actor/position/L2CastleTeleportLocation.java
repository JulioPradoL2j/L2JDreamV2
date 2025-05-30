/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.dream.game.model.actor.position;

public class L2CastleTeleportLocation
{
	private int _teleId;
	private int _locX;
	private int _locY;
	private int _locZ;
	private int _price;

	public int getLocX()
	{
		return _locX;
	}

	public int getLocY()
	{
		return _locY;
	}

	public int getLocZ()
	{
		return _locZ;
	}

	public int getPrice()
	{
		return _price;
	}

	public int getTeleId()
	{
		return _teleId;
	}

	public void setLocX(int locX)
	{
		_locX = locX;
	}

	public void setLocY(int locY)
	{
		_locY = locY;
	}

	public void setLocZ(int locZ)
	{
		_locZ = locZ;
	}

	public void setPrice(int price)
	{
		_price = price;
	}

	public void setTeleId(int id)
	{
		_teleId = id;
	}
}