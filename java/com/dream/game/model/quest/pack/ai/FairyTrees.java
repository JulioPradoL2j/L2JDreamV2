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
package com.dream.game.model.quest.pack.ai;

import com.dream.game.ai.CtrlIntention;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.quest.Quest;
import com.dream.tools.random.Rnd;

public class FairyTrees extends Quest implements Runnable
{
	private static final int[] trees =
	{
		27185,
		27186,
		27187,
		27188
	};

	public FairyTrees(int questId, String name, String descr)
	{
		super(questId, name, descr);

		for (int mob : trees)
		{
			addEventId(mob, QuestEventType.ON_KILL);
		}
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		for (int treeId : trees)
			if (npcId == treeId)
			{
				for (int i = 0; i < 20; i++)
				{
					L2Attackable newNpc = (L2Attackable) addSpawn(27189, npc.getX(), npc.getY(), npc.getZ(), 0, false, 30000);
					L2Character originalKiller = isPet ? killer.getPet() : killer;
					newNpc.setRunning();
					newNpc.addDamageHate(originalKiller, 0, 999);
					newNpc.getAI().setIntention(CtrlIntention.ATTACK, originalKiller);
					if (Rnd.nextBoolean())
						if (originalKiller != null)
						{
							final L2Skill skill = SkillTable.getInstance().getInfo(4243, 1);
							if (skill != null)
							{
								skill.getEffects(newNpc, originalKiller);
							}
						}
				}
			}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public void run()
	{

	}
}