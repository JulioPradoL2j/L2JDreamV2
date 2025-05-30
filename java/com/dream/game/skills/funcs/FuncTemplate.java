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
package com.dream.game.skills.funcs;

import java.lang.reflect.Constructor;

import org.apache.log4j.Logger;

import com.dream.game.skills.Env;
import com.dream.game.skills.Stats;
import com.dream.game.skills.conditions.Condition;

public final class FuncTemplate
{
	private final static Logger _log = Logger.getLogger(FuncTemplate.class);

	private final Constructor<?> _constructor;
	private final Condition _attachCond;

	public String _name;
	public final Stats stat;
	public final int order;
	public final String value;
	public final Condition applayCond;

	public FuncTemplate(Condition pAttachCond, Condition pApplayCond, String pFunc, Stats pStat, int pOrder, String value)
	{
		_attachCond = pAttachCond;

		stat = pStat;
		order = pOrder;
		this.value = value;
		applayCond = pApplayCond;
		_name = pFunc;
		try
		{
			_constructor = Class.forName("com.dream.game.skills.funcs.Func" + pFunc).getConstructor(Stats.class, Integer.TYPE, FuncOwner.class, String.class, Condition.class);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public Func getFunc(Env env, FuncOwner funcOwner)
	{
		try
		{
			if (_attachCond == null || _attachCond.test(env))
				return (Func) _constructor.newInstance(stat, order, funcOwner, value, applayCond);
		}
		catch (Exception e)
		{
			_log.warn("", e);
		}

		return null;
	}

	public String toXml()
	{
		return "<" + _name.toLowerCase() + " stat=\"" + stat.getValue() + "\" order=\"0x" + String.format("%x", order) + "\" val=\"" + value + "\"/>";
	}
}