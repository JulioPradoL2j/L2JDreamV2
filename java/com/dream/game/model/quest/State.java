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
package com.dream.game.model.quest;

public class State
{
	public final static byte CREATED = 0;

	public final static byte STARTED = 1;

	public final static byte COMPLETED = 2;

	public static byte getStateId(String statename)
	{
		if (statename.equals("Started"))
			return 1;
		if (statename.equals("Completed"))
			return 2;
		return 0;
	}

	public static String getStateName(byte state)
	{
		switch (state)
		{
			case 1:
				return "Started";
			case 2:
				return "Completed";
			default:
				return "Start";
		}
	}
}