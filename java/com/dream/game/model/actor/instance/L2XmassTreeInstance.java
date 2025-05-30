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
package com.dream.game.model.actor.instance;

import java.util.concurrent.ScheduledFuture;

import com.dream.Config;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.MagicSkillUse;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.tools.random.Rnd;

public class L2XmassTreeInstance extends L2Npc
{
	class XmassAI implements Runnable
	{
		private final L2XmassTreeInstance _caster;

		protected XmassAI(L2XmassTreeInstance caster)
		{
			_caster = caster;
		}

		private boolean handleCast(L2PcInstance player, int skillId)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(skillId, 1);

			if (player.getFirstEffect(skill) == null)
			{
				setTarget(player);

				broadcastPacket(new MagicSkillUse(_caster, player, skill.getId(), 1, skill.getHitTime(), 0, skill.isPositive()));
				player.getStatus().setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
				return true;
			}
			return false;
		}

		@Override
		public void run()
		{
			for (L2PcInstance player : getKnownList().getKnownPlayersInRadius(500))
			{
				int i = Rnd.nextInt(3);
				handleCast(player, 4262 + i);
			}
		}
	}

	class XmassDelete implements Runnable
	{
		L2XmassTreeInstance _tree;

		public XmassDelete(L2XmassTreeInstance par)
		{
			_tree = par;
		}

		@Override
		public void run()
		{
			if (_tree != null)
			{
				_tree.deleteMe();
			}
		}
	}

	private final ScheduledFuture<?> _aiTask;

	public L2XmassTreeInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		_aiTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new XmassAI(this), 3000, 30000);
		ThreadPoolManager.getInstance().scheduleGeneral(new XmassDelete(this), Config.CRISTMAS_TREE_TIME * 60000);
	}

	@Override
	public void deleteMe()
	{
		if (_aiTask != null)
		{
			_aiTask.cancel(true);
		}
		super.deleteMe();
	}

	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		return 900;
	}
}