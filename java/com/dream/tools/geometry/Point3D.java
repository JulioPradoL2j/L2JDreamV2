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
package com.dream.tools.geometry;

import java.io.Serializable;

import com.dream.annotations.XmlField;
import com.dream.data.xml.XMLObject;

public class Point3D extends XMLObject implements Serializable
{
	private static final long serialVersionUID = 4638345252031872576L;

	public static boolean distanceLessThan(Point3D point1, Point3D point2, double distance)
	{
		return distanceSquared(point1, point2) < distance * distance;
	}

	public static long distanceSquared(Point3D point1, Point3D point2)
	{
		long dx, dy;
		synchronized (point1)
		{
			synchronized (point2)
			{
				dx = point1.x - point2.x;
				dy = point1.y - point2.y;
			}
		}
		return dx * dx + dy * dy;
	}

	@XmlField(propertyName = "x")
	private volatile int x;

	@XmlField(propertyName = "y")
	private volatile int y;

	@XmlField(propertyName = "z")
	private volatile int z;

	public Point3D(int pX, int pY)
	{
		x = pX;
		y = pY;
		z = 0;
	}

	public Point3D(int pX, int pY, int pZ)
	{
		x = pX;
		y = pY;
		z = pZ;
	}

	public Point3D(Point3D worldPosition)
	{
		synchronized (worldPosition)
		{
			x = worldPosition.x;
			y = worldPosition.y;
			z = worldPosition.z;
		}
	}

	public synchronized long distanceSquaredTo(Point3D point)
	{
		long dx, dy;
		synchronized (point)
		{
			dx = x - point.x;
			dy = y - point.y;
		}
		return dx * dx + dy * dy;
	}

	public synchronized boolean equals(int pX, int pY, int pZ)
	{
		return x == pX && y == pY && z == pZ;
	}

	@Override
	public synchronized boolean equals(Object o)
	{
		if (o instanceof Point3D)
		{
			Point3D point3D = (Point3D) o;
			boolean ret;
			synchronized (point3D)
			{
				ret = point3D.x == x && point3D.y == y && point3D.z == z;
			}
			return ret;
		}
		return false;
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public int getZ()
	{
		return z;
	}

	@Override
	public int hashCode()
	{
		return x ^ y ^ z;
	}

	public synchronized void setTo(Point3D point)
	{
		synchronized (point)
		{
			x = point.x;
			y = point.y;
			z = point.z;
		}
	}

	public synchronized void setX(int pX)
	{
		x = pX;
	}

	public synchronized void setXYZ(int pX, int pY, int pZ)
	{
		x = pX;
		y = pY;
		z = pZ;
	}

	public synchronized void setY(int pY)
	{
		y = pY;
	}

	public synchronized void setZ(int pZ)
	{
		z = pZ;
	}

	@Override
	public String toString()
	{
		return "(" + x + ", " + y + ", " + z + ")";
	}
}