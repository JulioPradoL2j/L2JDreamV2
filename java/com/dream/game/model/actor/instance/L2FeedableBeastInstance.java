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

import com.dream.game.model.L2Spawn;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2FeedableBeastInstance extends L2MonsterInstance
{
	private L2Spawn _baseBeast;
	private boolean _isPoly = false;

	public L2FeedableBeastInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	public L2Spawn getBaseBeast()
	{
		return _baseBeast;
	}

	public boolean isPoly()
	{
		return _isPoly;
	}

	public void setBaseBeast(L2Spawn par)
	{
		_baseBeast = par;
	}

	public void setPoly(boolean par)
	{
		_isPoly = par;
	}
}