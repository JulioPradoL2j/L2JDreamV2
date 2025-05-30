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
package com.dream.game.skills.conditions;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.skills.Env;
import com.dream.game.skills.funcs.FuncOwner;

public abstract class Condition
{
	public static final Condition[] EMPTY_ARRAY = new Condition[0];

	private String _msg;
	private int _msgId;

	String getDefaultMessage()
	{
		return null;
	}

	int getDefaultMessageId()
	{
		return 0;
	}

	public final String getMessage()
	{
		return _msg != null ? _msg : getDefaultMessage();
	}

	public final int getMessageId()
	{
		return _msgId != 0 ? _msgId : getDefaultMessageId();
	}

	public void sendMessage(L2PcInstance player, FuncOwner funcOwner)
	{
		int msgId = getMessageId();
		if (msgId != 0)
		{
			SystemMessage sm = new SystemMessage(msgId);
			if (funcOwner.getFuncOwnerSkill() != null)
			{
				sm.addSkillName(funcOwner.getFuncOwnerSkill());
			}
			else if (funcOwner.getFuncOwnerName() != null)
			{
				sm.addString(funcOwner.getFuncOwnerName());
			}
			player.sendPacket(sm);
		}
		else
		{
			String msg = getMessage();
			if (msg != null)
			{
				player.sendMessage(msg);
			}
		}
	}

	public final void setMessage(String msg)
	{
		_msg = msg;
	}

	public final void setMessageId(int msgId)
	{
		_msgId = msgId;
	}

	public final boolean test(Env env)
	{
		return testImpl(env);
	}

	abstract boolean testImpl(Env env);
}