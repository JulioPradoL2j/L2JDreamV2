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
package com.dream.game.scripting;

import java.util.Map;

import com.dream.game.model.L2DropData;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.tools.random.DateRange;

public interface EngineInterface
{
	public void addDrop(L2NpcTemplate npc, L2DropData drop, boolean sweep);

	public void addDrop(L2NpcTemplate npc, L2DropData drop, int category);

	public void addEventDrop(int[] items, int[] count, double chance, DateRange range);

	public void addPetData(int petID, int levelStart, int levelEnd, Map<String, String> stats);

	public void addQuestDrop(int npcID, int itemID, int min, int max, int chance, String questID, String[] states);

	public void onPlayerLogin(String[] message, DateRange range);
}