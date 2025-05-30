package com.dream.game.network.serverpackets;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.sevensigns.SevenSigns;
import com.dream.game.model.entity.sevensigns.SevenSignsFestival;
import com.dream.game.network.SystemMessageId;
import com.dream.util.StatsSet;

public class SSQStatus extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private final int _page;

	public SSQStatus(L2PcInstance player, int recordPage)
	{
		_activeChar = player;
		_page = recordPage;
	}

	@Override
	protected final void writeImpl()
	{
		int winningCabal = SevenSigns.getInstance().getCabalHighestScore();
		int totalDawnMembers = SevenSigns.getInstance().getTotalMembers(SevenSigns.CABAL_DAWN);
		int totalDuskMembers = SevenSigns.getInstance().getTotalMembers(SevenSigns.CABAL_DUSK);

		writeC(0xF5);

		writeC(_page);
		writeC(SevenSigns.getInstance().getCurrentPeriod()); // current period?

		int dawnPercent = 0;
		int duskPercent = 0;

		switch (_page)
		{
			case 1:
				writeD(SevenSigns.getInstance().getCurrentCycle());

				int currentPeriod = SevenSigns.getInstance().getCurrentPeriod();

				switch (currentPeriod)
				{
					case SevenSigns.PERIOD_COMP_RECRUITING:
						writeD(SystemMessageId.INITIAL_PERIOD.getId());
						break;
					case SevenSigns.PERIOD_COMPETITION:
						writeD(SystemMessageId.QUEST_EVENT_PERIOD.getId());
						break;
					case SevenSigns.PERIOD_COMP_RESULTS:
						writeD(SystemMessageId.RESULTS_PERIOD.getId());
						break;
					case SevenSigns.PERIOD_SEAL_VALIDATION:
						writeD(SystemMessageId.VALIDATION_PERIOD.getId());
						break;
				}

				switch (currentPeriod)
				{
					case SevenSigns.PERIOD_COMP_RECRUITING:
					case SevenSigns.PERIOD_COMP_RESULTS:
						writeD(SystemMessageId.UNTIL_TODAY_6PM.getId());
						break;
					case SevenSigns.PERIOD_COMPETITION:
					case SevenSigns.PERIOD_SEAL_VALIDATION:
						writeD(SystemMessageId.UNTIL_MONDAY_6PM.getId());
						break;
				}

				writeC(SevenSigns.getInstance().getPlayerCabal(_activeChar));
				writeC(SevenSigns.getInstance().getPlayerSeal(_activeChar));

				writeD(SevenSigns.getInstance().getPlayerStoneContrib(_activeChar));
				writeD(SevenSigns.getInstance().getPlayerAdenaCollect(_activeChar));

				double dawnStoneScore = SevenSigns.getInstance().getCurrentStoneScore(SevenSigns.CABAL_DAWN);
				int dawnFestivalScore = SevenSigns.getInstance().getCurrentFestivalScore(SevenSigns.CABAL_DAWN);

				double duskStoneScore = SevenSigns.getInstance().getCurrentStoneScore(SevenSigns.CABAL_DUSK);
				int duskFestivalScore = SevenSigns.getInstance().getCurrentFestivalScore(SevenSigns.CABAL_DUSK);

				double totalStoneScore = duskStoneScore + dawnStoneScore;

				int duskStoneScoreProp = 0;
				int dawnStoneScoreProp = 0;

				if (totalStoneScore != 0)
				{
					duskStoneScoreProp = Math.round((float) duskStoneScore / (float) totalStoneScore * 500);
					dawnStoneScoreProp = Math.round((float) dawnStoneScore / (float) totalStoneScore * 500);
				}

				int duskTotalScore = SevenSigns.getInstance().getCurrentScore(SevenSigns.CABAL_DUSK);
				int dawnTotalScore = SevenSigns.getInstance().getCurrentScore(SevenSigns.CABAL_DAWN);

				int totalOverallScore = duskTotalScore + dawnTotalScore;

				if (totalOverallScore != 0)
				{
					dawnPercent = Math.round((float) dawnTotalScore / (float) totalOverallScore * 100);
					duskPercent = Math.round((float) duskTotalScore / (float) totalOverallScore * 100);
				}

				writeD(duskStoneScoreProp);
				writeD(duskFestivalScore);
				writeD(duskTotalScore);
				writeC(duskPercent);

				writeD(dawnStoneScoreProp);
				writeD(dawnFestivalScore);
				writeD(dawnTotalScore);
				writeC(dawnPercent);
				break;
			case 2:
				writeH(1);

				writeC(5);

				for (int i = 0; i < 5; i++)
				{
					writeC(i + 1);
					writeD(SevenSignsFestival.FESTIVAL_LEVEL_SCORES[i]);

					int duskScore = SevenSignsFestival.getInstance().getHighestScore(SevenSigns.CABAL_DUSK, i);
					int dawnScore = SevenSignsFestival.getInstance().getHighestScore(SevenSigns.CABAL_DAWN, i);

					writeD(duskScore);

					StatsSet highScoreData = SevenSignsFestival.getInstance().getHighestScoreData(SevenSigns.CABAL_DUSK, i);
					String[] partyMembers = highScoreData.getString("members").split(",");

					if (partyMembers != null)
					{
						writeC(partyMembers.length);

						for (String partyMember : partyMembers)
						{
							writeS(partyMember);
						}
					}
					else
					{
						writeC(0);
					}

					writeD(dawnScore);

					highScoreData = SevenSignsFestival.getInstance().getHighestScoreData(SevenSigns.CABAL_DAWN, i);
					partyMembers = highScoreData.getString("members").split(",");

					if (partyMembers != null)
					{
						writeC(partyMembers.length);

						for (String partyMember : partyMembers)
						{
							writeS(partyMember);
						}
					}
					else
					{
						writeC(0);
					}
				}
				break;
			case 3:
				writeC(10);
				writeC(35);
				writeC(3);

				for (int i = 1; i < 4; i++)
				{
					int dawnProportion = SevenSigns.getInstance().getSealProportion(i, SevenSigns.CABAL_DAWN);
					int duskProportion = SevenSigns.getInstance().getSealProportion(i, SevenSigns.CABAL_DUSK);

					writeC(i);
					writeC(SevenSigns.getInstance().getSealOwner(i));

					if (totalDuskMembers == 0)
					{
						if (totalDawnMembers == 0)
						{
							writeC(0);
							writeC(0);
						}
						else
						{
							writeC(0);
							writeC(Math.round((float) dawnProportion / (float) totalDawnMembers * 100));
						}
					}
					else if (totalDawnMembers == 0)
					{
						writeC(Math.round((float) duskProportion / (float) totalDuskMembers * 100));
						writeC(0);
					}
					else
					{
						writeC(Math.round((float) duskProportion / (float) totalDuskMembers * 100));
						writeC(Math.round((float) dawnProportion / (float) totalDawnMembers * 100));
					}
				}
				break;
			case 4:
				writeC(winningCabal);
				writeC(3);

				for (int i = 1; i < 4; i++)
				{
					int dawnProportion = SevenSigns.getInstance().getSealProportion(i, SevenSigns.CABAL_DAWN);
					int duskProportion = SevenSigns.getInstance().getSealProportion(i, SevenSigns.CABAL_DUSK);
					dawnPercent = Math.round(dawnProportion / (totalDawnMembers == 0 ? 1 : (float) totalDawnMembers) * 100);
					duskPercent = Math.round(duskProportion / (totalDuskMembers == 0 ? 1 : (float) totalDuskMembers) * 100);
					int sealOwner = SevenSigns.getInstance().getSealOwner(i);

					writeC(i);

					switch (sealOwner)
					{
						case SevenSigns.CABAL_NULL:
							switch (winningCabal)
							{
								case SevenSigns.CABAL_NULL:
									writeC(SevenSigns.CABAL_NULL);
									writeH(SystemMessageId.COMPETITION_TIE_SEAL_NOT_AWARDED.getId());
									break;
								case SevenSigns.CABAL_DAWN:
									if (dawnPercent >= 35)
									{
										writeC(SevenSigns.CABAL_DAWN);
										writeH(SystemMessageId.SEAL_NOT_OWNED_35_MORE_VOTED.getId());
									}
									else
									{
										writeC(SevenSigns.CABAL_NULL);
										writeH(SystemMessageId.SEAL_NOT_OWNED_35_LESS_VOTED.getId());
									}
									break;
								case SevenSigns.CABAL_DUSK:
									if (duskPercent >= 35)
									{
										writeC(SevenSigns.CABAL_DUSK);
										writeH(SystemMessageId.SEAL_NOT_OWNED_35_MORE_VOTED.getId());
									}
									else
									{
										writeC(SevenSigns.CABAL_NULL);
										writeH(SystemMessageId.SEAL_NOT_OWNED_35_LESS_VOTED.getId());
									}
									break;
							}
							break;
						case SevenSigns.CABAL_DAWN:
							switch (winningCabal)
							{
								case SevenSigns.CABAL_NULL:
									if (dawnPercent >= 10)
									{
										writeC(SevenSigns.CABAL_DAWN);
										writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
									}
									else
									{
										writeC(SevenSigns.CABAL_NULL);
										writeH(SystemMessageId.COMPETITION_TIE_SEAL_NOT_AWARDED.getId());
									}
									break;
								case SevenSigns.CABAL_DAWN:
									if (dawnPercent >= 10)
									{
										writeC(sealOwner);
										writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
									}
									else
									{
										writeC(SevenSigns.CABAL_NULL);
										writeH(SystemMessageId.SEAL_OWNED_10_LESS_VOTED.getId());
									}
									break;
								case SevenSigns.CABAL_DUSK:
									if (duskPercent >= 35)
									{
										writeC(SevenSigns.CABAL_DUSK);
										writeH(SystemMessageId.SEAL_NOT_OWNED_35_MORE_VOTED.getId());
									}
									else if (dawnPercent >= 10)
									{
										writeC(SevenSigns.CABAL_DAWN);
										writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
									}
									else
									{
										writeC(SevenSigns.CABAL_NULL);
										writeH(SystemMessageId.SEAL_OWNED_10_LESS_VOTED.getId());
									}
									break;
							}
							break;
						case SevenSigns.CABAL_DUSK:
							switch (winningCabal)
							{
								case SevenSigns.CABAL_NULL:
									if (duskPercent >= 10)
									{
										writeC(SevenSigns.CABAL_DUSK);
										writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
									}
									else
									{
										writeC(SevenSigns.CABAL_NULL);
										writeH(SystemMessageId.COMPETITION_TIE_SEAL_NOT_AWARDED.getId());
									}
									break;
								case SevenSigns.CABAL_DAWN:
									if (dawnPercent >= 35)
									{
										writeC(SevenSigns.CABAL_DAWN);
										writeH(SystemMessageId.SEAL_NOT_OWNED_35_MORE_VOTED.getId());
									}
									else if (duskPercent >= 10)
									{
										writeC(sealOwner);
										writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
									}
									else
									{
										writeC(SevenSigns.CABAL_NULL);
										writeH(SystemMessageId.SEAL_OWNED_10_LESS_VOTED.getId());
									}
									break;
								case SevenSigns.CABAL_DUSK:
									if (duskPercent >= 10)
									{
										writeC(sealOwner);
										writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
									}
									else
									{
										writeC(SevenSigns.CABAL_NULL);
										writeH(SystemMessageId.SEAL_OWNED_10_LESS_VOTED.getId());
									}
									break;
							}
							break;
					}
					writeH(0);
				}
				break;
		}
	}

}