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
package com.dream.game.model.world;

import java.io.Serializable;

public class L2Polygon implements Serializable
{
	private static final long serialVersionUID = -6460061437900069969L;
	private int _nPoints;
	private int _xPoints[];

	private int _yPoints[];

	public L2Polygon()
	{
		_xPoints = new int[3];
		_yPoints = new int[3];
	}

	public void addPoint(int x, int y)
	{
		if (_nPoints == _xPoints.length)
		{
			int tmp[];

			tmp = new int[_nPoints + 1];
			System.arraycopy(_xPoints, 0, tmp, 0, _nPoints);
			_xPoints = tmp;

			tmp = new int[_nPoints + 1];
			System.arraycopy(_yPoints, 0, tmp, 0, _nPoints);
			_yPoints = tmp;
		}
		_xPoints[_nPoints] = x;
		_yPoints[_nPoints] = y;
		_nPoints++;
	}

	private final boolean cn_PnPoly(int x, int y)
	{
		int cn = 0;

		for (int i = 0; i < _nPoints - 1; i++)
			if (_yPoints[i] <= y && _yPoints[i + 1] > y || _yPoints[i] > y && _yPoints[i + 1] <= y)
			{
				float vt = (float) (y - _yPoints[i]) / (_yPoints[i + 1] - _yPoints[i]);
				if (x < _xPoints[i] + vt * (_xPoints[i + 1] - _xPoints[i]))
				{
					++cn;
				}
			}
		return (cn & 1) == 1;
	}

	public boolean contains(int x, int y)
	{
		return cn_PnPoly(x, y);
	}

	public int[] getXPoints()
	{
		return _xPoints;
	}

	public int[] getYPoints()
	{
		return _yPoints;
	}

	public int size()
	{
		return _nPoints;
	}
}