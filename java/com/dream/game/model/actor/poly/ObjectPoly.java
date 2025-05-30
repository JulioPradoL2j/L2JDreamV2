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
package com.dream.game.model.actor.poly;

public class ObjectPoly
{
	private int _polyId;
	private String _polyType;
	private int _baseId;
	private boolean _firstMorph;

	public ObjectPoly()
	{

	}

	public final int getBaseId()
	{
		return _baseId;
	}

	public final boolean getFirstMorph()
	{
		return _firstMorph;
	}

	public final int getPolyId()
	{
		return _polyId;
	}

	public final String getPolyType()
	{
		return _polyType;
	}

	public final boolean isFirstMorph()
	{
		return getFirstMorph();
	}

	public final boolean isMorphed()
	{
		return getPolyType() != null;
	}

	public final void setBaseId(int value)
	{
		_baseId = value;
	}

	public final void setFirstMorph(boolean value)
	{
		_firstMorph = value;
	}

	public final void setNotMorphed()
	{
		_polyType = null;
	}

	public final void setPolyId(int value)
	{
		_polyId = value;
	}

	public void setPolyInfo(String polyType, String polyId)
	{
		setPolyId(Integer.parseInt(polyId));
		setPolyType(polyType);
	}

	public void setPolyInfo(String polyType, String polyId, String baseId)
	{
		setPolyId(Integer.parseInt(polyId));
		setPolyType(polyType);
		setBaseId(Integer.parseInt(baseId));
		setFirstMorph(true);
	}

	public final void setPolyType(String value)
	{
		_polyType = value;
	}
}