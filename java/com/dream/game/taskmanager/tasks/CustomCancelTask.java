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
package com.dream.game.taskmanager.tasks;

import java.util.Vector;

import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.instance.L2PcInstance;
/**
 * @author Anarchy
 *
 */
public class CustomCancelTask implements Runnable
{
	private L2PcInstance _player = null;
	private Vector<L2Skill> _buffs = null;
	
	public CustomCancelTask(L2PcInstance _player, Vector<L2Skill> _buffs)
	{
		this._player = _player;
		this._buffs = _buffs;
	}
	
	@Override
	public void run()
	{
		if (_player == null)
		{
			return;
		}
		for (L2Skill s : _buffs)
		{
			if (s == null)
			{
				continue;
			}
			
			s.getEffects(_player, _player);
		}
	}
}