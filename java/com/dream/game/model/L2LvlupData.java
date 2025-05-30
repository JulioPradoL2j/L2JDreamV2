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

public class L2LvlupData
{
	private int _classid;
	private int _classLvl;
	private float _classHpAdd;
	private float _classHpBase;
	private float _classHpModifier;
	private float _classCpAdd;
	private float _classCpBase;
	private float _classCpModifier;
	private float _classMpAdd;
	private float _classMpBase;
	private float _classMpModifier;

	@Deprecated
	public float getClassCpAdd()
	{
		return _classCpAdd;
	}

	@Deprecated
	public float getClassCpBase()
	{
		return _classCpBase;
	}

	@Deprecated
	public float getClassCpModifier()
	{
		return _classCpModifier;
	}

	@Deprecated
	public float getClassHpAdd()
	{
		return _classHpAdd;
	}

	@Deprecated
	public float getClassHpBase()
	{
		return _classHpBase;
	}

	@Deprecated
	public float getClassHpModifier()
	{
		return _classHpModifier;
	}

	public int getClassid()
	{
		return _classid;
	}

	@Deprecated
	public int getClassLvl()
	{
		return _classLvl;
	}

	@Deprecated
	public float getClassMpAdd()
	{
		return _classMpAdd;
	}

	@Deprecated
	public float getClassMpBase()
	{
		return _classMpBase;
	}

	@Deprecated
	public float getClassMpModifier()
	{
		return _classMpModifier;
	}

	public void setClassCpAdd(float cpAdd)
	{
		_classCpAdd = cpAdd;
	}

	public void setClassCpBase(float cpBase)
	{
		_classCpBase = cpBase;
	}

	public void setClassCpModifier(float cpModifier)
	{
		_classCpModifier = cpModifier;
	}

	public void setClassHpAdd(float hpAdd)
	{
		_classHpAdd = hpAdd;
	}

	public void setClassHpBase(float hpBase)
	{
		_classHpBase = hpBase;
	}

	public void setClassHpModifier(float hpModifier)
	{
		_classHpModifier = hpModifier;
	}

	public void setClassid(int pClassid)
	{
		_classid = pClassid;
	}

	public void setClassLvl(int lvl)
	{
		_classLvl = lvl;
	}

	public void setClassMpAdd(float mpAdd)
	{
		_classMpAdd = mpAdd;
	}

	public void setClassMpBase(float mpBase)
	{
		_classMpBase = mpBase;
	}

	public void setClassMpModifier(float mpModifier)
	{
		_classMpModifier = mpModifier;
	}
}