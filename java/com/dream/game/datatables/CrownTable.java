package com.dream.game.datatables;

public final class CrownTable
{
	public static boolean giveCrown = false;

	private static final int[] CROWN_IDS =
	{
		6841,
		6834,
		6835,
		6836,
		6837,
		6838,
		6839,
		6840,
		8182,
		8183,
	};

	public static int getCrownId(int castleId)
	{
		switch (castleId)
		{
			case 1:
				return 6838;
			case 2:
				return 6835;
			case 3:
				return 6839;
			case 4:
				return 6837;
			case 5:
				return 6840;
			case 6:
				return 6834;
			case 7:
				return 6836;
			case 8:
				return 8182;
			case 9:
				return 8183;
		}
		return 0;
	}

	public static int[] getCrownIds()
	{
		return CROWN_IDS;
	}

}