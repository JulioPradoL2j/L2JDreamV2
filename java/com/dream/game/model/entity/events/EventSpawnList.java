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
package com.dream.game.model.entity.events;

import java.util.Date;

import org.apache.log4j.Logger;

import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.datatables.sql.SpawnTable;
import com.dream.game.model.L2Spawn;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.tools.random.DateRange;

public class EventSpawnList
{

	private static class SingletonHolder
	{
		protected static final EventSpawnList _instance = new EventSpawnList();
	}

	private static Logger _log = Logger.getLogger(EventSpawnList.class.getName());

	public static void addNewGlobalSpawn(int NpcId, int Xpos, int Ypos, int Zpos, int count, int Heading, int respavntime, DateRange DateRanges)
	{
		L2NpcTemplate template;
		Date currentDate = new Date();

		if (DateRanges.isWithinRange(currentDate))
		{
			template = NpcTable.getInstance().getTemplate(NpcId);
			try
			{
				L2Spawn spawn = new L2Spawn(template);
				spawn.setLocx(Xpos);
				spawn.setLocy(Ypos);
				spawn.setLocz(Zpos);
				spawn.setAmount(count);
				spawn.setHeading(Heading);
				spawn.setRespawnDelay(respavntime);

				SpawnTable.getInstance().addNewSpawn(spawn, false);
				spawn.init();
				System.out.println("Global Spawn :: NPCId: " + NpcId + ", Date Range From: " + DateRanges.getStartDate() + " To: " + DateRanges.getEndDate() + " Now: " + currentDate);
			}
			catch (Exception e)
			{
				_log.error("error while creating npc spawn: " + e);
			}
		}
	}

	public static EventSpawnList getInstance()
	{
		return SingletonHolder._instance;
	}
}