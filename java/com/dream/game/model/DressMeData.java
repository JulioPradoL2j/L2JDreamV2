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

/**
 * @author Anarchy
 *
 */
public class DressMeData
{
	private int chestId,
	legsId,
	glovesId,
	feetId,
		hairId;
	
	public DressMeData()
	{
		chestId = 0;
		legsId = 0;
		glovesId = 0;
		feetId = 0;
		hairId = 0;
	}
	
	public int getChestId()
	{
		return chestId;
	}
	
	public int getLegsId()
	{
		return legsId;
	}
	
	public int getGlovesId()
	{
		return glovesId;
	}
	
	public int getBootsId()
	{
		return feetId;
	}

	public int getHairId()
	{
		return hairId;
	}
	
	public void setChestId(int val)
	{
		chestId = val;
	}
	
	public void setLegsId(int val)
	{
		legsId = val;
	}
	
	public void setGlovesId(int val)
	{
		glovesId = val;
	}
	
	public void setBootsId(int val)
	{
		feetId = val;
	}
	
	public void setHairId(int val)
	{
		hairId = val;
	}
}