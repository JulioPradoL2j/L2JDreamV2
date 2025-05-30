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
package com.dream.game.model.base;

import com.dream.Config;

public class SoulCrystal
{
	public static final int[][] HighSoulConvert =
	{
		{
			4639,
			5577
		},
		{
			5577,
			5580
		},
		{
			5580,
			5908
		},
		{
			5908,
			9570
		},
		{
			9570,
			10480
		},

		{
			4650,
			5578
		},
		{
			5578,
			5581
		},
		{
			5581,
			5911
		},
		{
			5911,
			9572
		},
		{
			9572,
			10482
		},

		{
			4661,
			5579
		},
		{
			5579,
			5582
		},
		{
			5582,
			5914
		},
		{
			5914,
			9571
		},
		{
			9571,
			10481
		}
	};

	public static final short[] SoulCrystalTable =
	{
		4629,
		4630,
		4631,
		4632,
		4633,
		4634,
		4635,
		4636,
		4637,
		4638,
		4639,
		5577,
		5580,
		5908,
		9570,
		10480,
		4640,
		4641,
		4642,
		4643,
		4644,
		4645,
		4646,
		4647,
		4648,
		4649,
		4650,
		5578,
		5581,
		5911,
		9572,
		10482,
		4651,
		4652,
		4653,
		4654,
		4655,
		4656,
		4657,
		4658,
		4659,
		4660,
		4661,
		5579,
		5582,
		5914,
		9571,
		10481
	};

	public static final byte MAX_CRYSTALS_LEVEL = 15;
	public static final int LEVEL_CHANCE = Config.CHANCE_LEVEL;

	public static final short RED_NEW_CRYSTAL = 4629;
	public static final short GRN_NEW_CYRSTAL = 4640;
	public static final short BLU_NEW_CRYSTAL = 4651;
}