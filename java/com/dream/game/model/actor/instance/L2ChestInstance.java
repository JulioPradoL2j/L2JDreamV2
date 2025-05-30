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

import com.dream.Message;
import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.manager.clanhallsiege.RainbowSpringSiege;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.network.serverpackets.MagicSkillUse;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.tools.random.Rnd;

public final class L2ChestInstance extends L2MonsterInstance
{
	private volatile boolean _isInteracted;
	private volatile boolean _specialDrop;

	public L2ChestInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		_isInteracted = false;
		_specialDrop = false;
	}

	public void chestTrap(L2Character player)
	{
		int trapSkillId = 0;
		int rnd = Rnd.get(120);

		if (getTemplate().getLevel() >= 61)
		{
			if (rnd >= 90)
			{
				trapSkillId = 4139;
			}
			else if (rnd >= 50)
			{
				trapSkillId = 4118;
			}
			else if (rnd >= 20)
			{
				trapSkillId = 1167;
			}
			else
			{
				trapSkillId = 223;
			}
		}
		else if (getTemplate().getLevel() >= 41)
		{
			if (rnd >= 90)
			{
				trapSkillId = 4139;
			}
			else if (rnd >= 60)
			{
				trapSkillId = 96;
			}
			else if (rnd >= 20)
			{
				trapSkillId = 1167;
			}
			else
			{
				trapSkillId = 4118;
			}
		}
		else if (getTemplate().getLevel() >= 21)
		{
			if (rnd >= 80)
			{
				trapSkillId = 4139;
			}
			else if (rnd >= 50)
			{
				trapSkillId = 96;
			}
			else if (rnd >= 20)
			{
				trapSkillId = 1167;
			}
			else
			{
				trapSkillId = 129;
			}
		}
		else if (rnd >= 80)
		{
			trapSkillId = 4139;
		}
		else if (rnd >= 50)
		{
			trapSkillId = 96;
		}
		else
		{
			trapSkillId = 129;
		}

		if (player instanceof L2PcInstance)
		{
			player.sendMessage(Message.getMessage((L2PcInstance) player, Message.MessageId.MSG_HERE_TRAP));
		}
		handleCast(player, trapSkillId);
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (getTemplate().getNpcId() == 35593)
		{
			RainbowSpringSiege.getInstance().chestDie(killer, this);
		}
		return super.doDie(killer);
	}

	@Override
	public void doItemDrop(L2NpcTemplate npcTemplate, L2Character lastAttacker)
	{
		int id = getTemplate().getNpcId();

		if (!_specialDrop)
			if (id >= 18265 && id <= 18286)
			{
				id += 3536;
			}
			else if (id == 18287 || id == 18288)
			{
				id = 21671;
			}
			else if (id == 18289 || id == 18290)
			{
				id = 21694;
			}
			else if (id == 18291 || id == 18292)
			{
				id = 21717;
			}
			else if (id == 18293 || id == 18294)
			{
				id = 21740;
			}
			else if (id == 18295 || id == 18296)
			{
				id = 21763;
			}
			else if (id == 18297 || id == 18298)
			{
				id = 21786;
			}
			else
				return;
		super.doItemDrop(NpcTable.getInstance().getTemplate(id), lastAttacker);
	}

	private boolean handleCast(L2Character player, int skillId)
	{
		int skillLevel = 1;
		byte lvl = getTemplate().getLevel();
		if (lvl > 20 && lvl <= 40)
		{
			skillLevel = 3;
		}
		else if (lvl > 40 && lvl <= 60)
		{
			skillLevel = 5;
		}
		else if (lvl > 60)
		{
			skillLevel = 6;
		}

		if (player.isDead() || !player.isVisible() || !player.isInsideRadius(this, getDistanceToWatchObject(player), false, false))
			return false;

		L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);

		if (player.getFirstEffect(skill) == null)
		{
			skill.getEffects(this, player);
			broadcastPacket(new MagicSkillUse(this, player, skill.getId(), skillLevel, skill.getHitTime(), 0, skill.isPositive()));
			return true;
		}
		return false;
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}

	public synchronized boolean isInteracted()
	{
		return _isInteracted;
	}

	@Override
	public boolean isMovementDisabled()
	{
		return super.isMovementDisabled() && !isInteracted();
	}

	public synchronized boolean isSpecialDrop()
	{
		return _specialDrop;
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		_isInteracted = false;
		_specialDrop = false;
		setMustRewardExpSp(true);
	}

	public synchronized void setInteracted()
	{
		_isInteracted = true;
	}

	public synchronized void setSpecialDrop()
	{
		_specialDrop = true;
	}
}