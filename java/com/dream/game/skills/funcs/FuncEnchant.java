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

import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.skills.Env;
import com.dream.game.skills.Stats;
import com.dream.game.skills.conditions.Condition;
import com.dream.game.templates.item.L2Item;
import com.dream.game.templates.item.L2WeaponType;

public final class FuncEnchant extends Func
{
	private double _value;

	public FuncEnchant(Stats pStat, int pOrder, FuncOwner pFuncOwner, double value, Condition pCondition)
	{
		super(pStat, pOrder, pFuncOwner, pCondition);
		_value = value;
	}

	public FuncEnchant(Stats pStat, int pOrder, FuncOwner pFuncOwner, String value, Condition pCondition)
	{
		super(pStat, pOrder, pFuncOwner, pCondition);
		try
		{
			_value = Double.parseDouble(value);
		}
		catch (NumberFormatException e)
		{
			_value = 0;
		}
	}

	@Override
	protected void calc(Env env)
	{
		final L2ItemInstance item = (L2ItemInstance) funcOwner;

		int enchant = item.getEnchantLevel();

		if (enchant > 0)
		{
			env.value += getEnchantAddition(_value, Math.min(enchant, 3), Math.max(0, enchant - 3), item.getItem(), enchant);
		}
	}

	private int getEnchantAddition(double base, int enchant, int overEnchant, L2Item item, int realEnchant)
	{
		switch (stat)
		{
			case MAGIC_ATTACK:
			{
				switch (item.getCrystalType())
				{
					case L2Item.CRYSTAL_S84:
					{
						return 4 * enchant + 8 * overEnchant;
					}
					case L2Item.CRYSTAL_S80:
					{
						return 4 * enchant + 8 * overEnchant;
					}
					case L2Item.CRYSTAL_R:
					{
						return 4 * enchant + 8 * overEnchant;
					}
					case L2Item.CRYSTAL_S:
					{
						return 4 * enchant + 8 * overEnchant;
					}
					case L2Item.CRYSTAL_A:
					case L2Item.CRYSTAL_B:
					case L2Item.CRYSTAL_C:
					{
						return 3 * enchant + 6 * overEnchant;
					}
					case L2Item.CRYSTAL_D:
					case L2Item.CRYSTAL_NONE:
					{
						return 2 * enchant + 4 * overEnchant;
					}
				}
				break;
			}
			case POWER_ATTACK:
			{
				boolean isBow = false;
				try
				{
					isBow = ((L2WeaponType) item.getItemType()).isBowType();
				}
				catch (Exception e)
				{
					isBow = false;
				}

				switch (item.getCrystalType())
				{
					case L2Item.CRYSTAL_S84:
					{
						if (isBow)
							return 10 * enchant + 20 * overEnchant;
						return 5 * enchant + 10 * overEnchant;
					}
					case L2Item.CRYSTAL_S80:
					{
						if (isBow)
							return 10 * enchant + 20 * overEnchant;
						return 5 * enchant + 10 * overEnchant;
					}
					case L2Item.CRYSTAL_R:
					{
						if (isBow)
							return 10 * enchant + 20 * overEnchant;
						return 5 * enchant + 10 * overEnchant;
					}
					case L2Item.CRYSTAL_S:
					{
						if (isBow)
							return 10 * enchant + 20 * overEnchant;
						return 5 * enchant + 10 * overEnchant;
					}
					case L2Item.CRYSTAL_A:
					{
						if (isBow)
							return 8 * enchant + 16 * overEnchant;
						return 4 * enchant + 8 * overEnchant;
					}
					case L2Item.CRYSTAL_B:
					case L2Item.CRYSTAL_C:
					{
						if (isBow)
							return 6 * enchant + 12 * overEnchant;
						return 3 * enchant + 6 * overEnchant;
					}
					case L2Item.CRYSTAL_D:
					case L2Item.CRYSTAL_NONE:
					{
						if (isBow)
							return 4 * enchant + 8 * overEnchant;
						return 2 * enchant + 4 * overEnchant;
					}
				}
				break;
			}
			default:
			{
				return (int) ((base == 0.0 ? 1 : base) * enchant + 3 * overEnchant);
			}

		}

		throw new IllegalStateException(stat + " " + order + " " + funcOwner);
	}
}