package com.dream.game.model.actor.instance;

import java.util.Calendar;
import java.util.List;

import com.dream.Config;
import com.dream.Message;
import com.dream.game.model.L2Party;
import com.dream.game.model.entity.sevensigns.SevenSigns;
import com.dream.game.model.entity.sevensigns.SevenSignsFestival;
import com.dream.game.model.quest.QuestState;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.util.StatsSet;

public final class L2FestivalGuideInstance extends L2NpcInstance
{
	private final static String calculateDate(String milliFromEpoch)
	{
		long numMillis = Long.valueOf(milliFromEpoch);
		Calendar calCalc = Calendar.getInstance();

		calCalc.setTimeInMillis(numMillis);

		return calCalc.get(Calendar.YEAR) + "/" + calCalc.get(Calendar.MONTH) + "/" + calCalc.get(Calendar.DAY_OF_MONTH);
	}

	private final static String getBonusTable()
	{
		StringBuilder tableHtml = new StringBuilder();

		for (int i = 0; i < 5; i++)
		{
			int accumScore = SevenSignsFestival.getInstance().getAccumulatedBonus(i);
			String festivalName = SevenSignsFestival.getFestivalNameText(i);

			tableHtml.append("<tr><td align=\"center\" width=\"150\">" + festivalName + "</td><td align=\"center\" width=\"150\">" + accumScore + "</td></tr>");
		}

		return tableHtml.toString();
	}

	private final static String getStatsTable()
	{
		StringBuilder tableHtml = new StringBuilder();

		for (int i = 0; i < 5; i++)
		{
			int dawnScore = SevenSignsFestival.getInstance().getHighestScore(SevenSigns.CABAL_DAWN, i);
			int duskScore = SevenSignsFestival.getInstance().getHighestScore(SevenSigns.CABAL_DUSK, i);
			String festivalName = SevenSignsFestival.getFestivalNameText(i);
			String winningCabal = "Children Of Dusk";

			if (dawnScore > duskScore)
			{
				winningCabal = "Children Of Dawn";
			}
			else if (dawnScore == duskScore)
			{
				winningCabal = "No";
			}

			tableHtml.append("<tr><td width=\"100\" align=\"center\">" + festivalName + "</td><td align=\"center\" width=\"35\">" + duskScore + "</td><td align=\"center\" width=\"35\">" + dawnScore + "</td><td align=\"center\" width=\"130\">" + winningCabal + "</td></tr>");
		}

		return tableHtml.toString();
	}

	protected int _festivalType;
	protected int _festivalOracle;

	protected int _blueStonesNeeded;

	protected int _greenStonesNeeded;

	protected int _redStonesNeeded;

	public L2FestivalGuideInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);

		switch (getNpcId())
		{
			case 31127:
			case 31132:
				_festivalType = SevenSignsFestival.FESTIVAL_LEVEL_MAX_31;
				_festivalOracle = SevenSigns.CABAL_DAWN;
				_blueStonesNeeded = 900;
				_greenStonesNeeded = 540;
				_redStonesNeeded = 270;
				break;
			case 31128:
			case 31133:
				_festivalType = SevenSignsFestival.FESTIVAL_LEVEL_MAX_42;
				_festivalOracle = SevenSigns.CABAL_DAWN;
				_blueStonesNeeded = 1500;
				_greenStonesNeeded = 900;
				_redStonesNeeded = 450;
				break;
			case 31129:
			case 31134:
				_festivalType = SevenSignsFestival.FESTIVAL_LEVEL_MAX_53;
				_festivalOracle = SevenSigns.CABAL_DAWN;
				_blueStonesNeeded = 3000;
				_greenStonesNeeded = 1800;
				_redStonesNeeded = 900;
				break;
			case 31130:
			case 31135:
				_festivalType = SevenSignsFestival.FESTIVAL_LEVEL_MAX_64;
				_festivalOracle = SevenSigns.CABAL_DAWN;
				_blueStonesNeeded = 4500;
				_greenStonesNeeded = 2700;
				_redStonesNeeded = 1350;
				break;
			case 31131:
			case 31136:
				_festivalType = SevenSignsFestival.FESTIVAL_LEVEL_MAX_NONE;
				_festivalOracle = SevenSigns.CABAL_DAWN;
				_blueStonesNeeded = 6000;
				_greenStonesNeeded = 3600;
				_redStonesNeeded = 1800;
				break;

			case 31137:
			case 31142:
				_festivalType = SevenSignsFestival.FESTIVAL_LEVEL_MAX_31;
				_festivalOracle = SevenSigns.CABAL_DUSK;
				_blueStonesNeeded = 900;
				_greenStonesNeeded = 540;
				_redStonesNeeded = 270;
				break;
			case 31138:
			case 31143:
				_festivalType = SevenSignsFestival.FESTIVAL_LEVEL_MAX_42;
				_festivalOracle = SevenSigns.CABAL_DUSK;
				_blueStonesNeeded = 1500;
				_greenStonesNeeded = 900;
				_redStonesNeeded = 450;
				break;
			case 31139:
			case 31144:
				_festivalType = SevenSignsFestival.FESTIVAL_LEVEL_MAX_53;
				_festivalOracle = SevenSigns.CABAL_DUSK;
				_blueStonesNeeded = 3000;
				_greenStonesNeeded = 1800;
				_redStonesNeeded = 900;
				break;
			case 31140:
			case 31145:
				_festivalType = SevenSignsFestival.FESTIVAL_LEVEL_MAX_64;
				_festivalOracle = SevenSigns.CABAL_DUSK;
				_blueStonesNeeded = 4500;
				_greenStonesNeeded = 2700;
				_redStonesNeeded = 1350;
				break;
			case 31141:
			case 31146:
				_festivalType = SevenSignsFestival.FESTIVAL_LEVEL_MAX_NONE;
				_festivalOracle = SevenSigns.CABAL_DUSK;
				_blueStonesNeeded = 6000;
				_greenStonesNeeded = 3600;
				_redStonesNeeded = 1800;
				break;
		}
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("FestivalDesc"))
		{
			int val = Integer.parseInt(command.substring(13));
			showChatWindow(player, val, null, true);
		}
		else if (command.startsWith("ReturnFromRift"))
		{
			QuestState st = player.getQuestState("Quest 1103_OracleTeleport");
			if (st != null)
			{
				st.getQuest().onAdvEvent("Return", this, player);
			}
			else
			{
				player.teleToLocation(147468, 27073, -2203, true);
			}

		}
		else if (command.startsWith("Festival"))
		{
			L2Party playerParty = player.getParty();
			int val = Integer.parseInt(command.substring(9, 10));

			switch (val)
			{
				case 1:
					if (SevenSigns.getInstance().isSealValidationPeriod())
					{
						showChatWindow(player, 2, "a", false);
						return;
					}

					if (SevenSignsFestival.getInstance().isFestivalInitialized())
					{
						player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_NO_REGISTER_WHILE_EVENT));
						return;
					}

					if (playerParty == null)
					{
						showChatWindow(player, 2, "b", false);
						return;
					}

					if (!playerParty.isLeader(player))
					{
						showChatWindow(player, 2, "c", false);
						return;
					}

					if (playerParty.getMemberCount() < Config.ALT_FESTIVAL_MIN_PLAYER)
					{
						showChatWindow(player, 2, "b", false);
						return;
					}

					if (playerParty.getLevel() > SevenSignsFestival.getMaxLevelForFestival(_festivalType))
					{
						showChatWindow(player, 2, "d", false);
						return;
					}

					if (player.isFestivalParticipant())
					{
						SevenSignsFestival.getInstance().setParticipants(_festivalOracle, _festivalType, playerParty);
						showChatWindow(player, 2, "f", false);
						return;
					}

					showChatWindow(player, 1, null, false);
					break;
				case 2:
					int stoneType = Integer.parseInt(command.substring(11));
					int stonesNeeded = 0;

					switch (stoneType)
					{
						case SevenSigns.SEAL_STONE_BLUE_ID:
							stonesNeeded = _blueStonesNeeded;
							break;
						case SevenSigns.SEAL_STONE_GREEN_ID:
							stonesNeeded = _greenStonesNeeded;
							break;
						case SevenSigns.SEAL_STONE_RED_ID:
							stonesNeeded = _redStonesNeeded;
							break;
					}

					if (!player.destroyItemByItemId("SevenSigns", stoneType, stonesNeeded, this, true))
						return;

					SevenSignsFestival.getInstance().setParticipants(_festivalOracle, _festivalType, playerParty);
					SevenSignsFestival.getInstance().addAccumulatedBonus(_festivalType, stoneType, stonesNeeded);

					showChatWindow(player, 2, "e", false);
					break;
				case 3:
					if (SevenSigns.getInstance().isSealValidationPeriod())
					{
						showChatWindow(player, 3, "a", false);
						return;
					}

					if (SevenSignsFestival.getInstance().isFestivalInProgress())
					{
						player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_NO_REGISTER_WHILE_EVENT));
						return;
					}

					if (playerParty == null)
					{
						showChatWindow(player, 3, "b", false);
						return;
					}

					List<L2PcInstance> prevParticipants = SevenSignsFestival.getInstance().getPreviousParticipants(_festivalOracle, _festivalType);

					if (prevParticipants == null)
						return;

					if (!prevParticipants.contains(player))
					{
						showChatWindow(player, 3, "b", false);
						return;
					}

					if (player.getObjectId() != prevParticipants.get(0).getObjectId())
					{
						showChatWindow(player, 3, "b", false);
						return;
					}

					L2ItemInstance bloodOfferings = player.getInventory().getItemByItemId(SevenSignsFestival.FESTIVAL_OFFERING_ID);
					int offeringCount = 0;

					if (bloodOfferings == null)
					{
						player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_NO_BLOOD_OFFERINGS));
						return;
					}

					offeringCount = bloodOfferings.getCount();

					int offeringScore = offeringCount * SevenSignsFestival.FESTIVAL_OFFERING_VALUE;
					boolean isHighestScore = SevenSignsFestival.getInstance().setFinalScore(player, _festivalOracle, _festivalType, offeringScore);

					player.destroyItem("SevenSigns", bloodOfferings, this, false);

					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CONTRIB_SCORE_INCREASED_BY_S1).addNumber(offeringScore));

					if (isHighestScore)
					{
						showChatWindow(player, 3, "c", false);
					}
					else
					{
						showChatWindow(player, 3, "d", false);
					}
					break;
				case 4:
					StringBuilder strBuffer = new StringBuilder("<html><body>Festival Guide: <br> This is the best result for the festival week Sunset ");

					final StatsSet dawnData = SevenSignsFestival.getInstance().getHighestScoreData(SevenSigns.CABAL_DAWN, _festivalType);
					final StatsSet duskData = SevenSignsFestival.getInstance().getHighestScoreData(SevenSigns.CABAL_DUSK, _festivalType);
					final StatsSet overallData = SevenSignsFestival.getInstance().getOverallHighestScoreData(_festivalType);

					final int dawnScore = dawnData.getInteger("score");
					final int duskScore = duskData.getInteger("score");
					int overallScore = 0;

					if (overallData != null)
					{
						overallScore = overallData.getInteger("score");
					}

					strBuffer.append(SevenSignsFestival.getFestivalNameText(_festivalType) + ".<br>");

					if (dawnScore > 0)
					{
						strBuffer.append("Dawn: " + calculateDate(dawnData.getString("date")) + ". Points " + dawnScore + "<br>" + dawnData.getString("members") + "<br>");
					}
					else
					{
						strBuffer.append("Breaking Dawn has not yet participated. 0 points<br>");
					}

					if (duskScore > 0)
					{
						strBuffer.append("Dusk: " + calculateDate(duskData.getString("date")) + ". Points " + duskScore + "<br>" + duskData.getString("members") + "<br>");
					}
					else
					{
						strBuffer.append("Dusk: not yet participated. 0 points<br>");
					}

					if (overallScore > 0)
					{
						String cabalStr = "The Children Of Dusk";

						if (overallData != null)
						{
							if (overallData.getString("cabal").equals("dawn"))
							{
								cabalStr = "Children Of The Dawn";
							}

							strBuffer.append("Details of the best result: " + calculateDate(overallData.getString("date")) + ". Points " + overallScore + "<br>party: " + cabalStr + "<br>" + overallData.getString("members") + "<br>");
						}
					}
					else
					{
						strBuffer.append("Best result: details have not yet been identified. 0 points<br>");
					}

					strBuffer.append("<a action=\"bypass -h npc_" + getObjectId() + "_Chat 0\">Back</a></body></html>");

					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setHtml(strBuffer.toString());
					player.sendPacket(html);
					break;
				case 8:
					if (playerParty == null)
						return;

					if (!SevenSignsFestival.getInstance().isFestivalInProgress())
						return;

					if (!playerParty.isLeader(player))
					{
						showChatWindow(player, 8, "a", false);
						break;
					}

					if (SevenSignsFestival.getInstance().increaseChallenge(_festivalOracle, _festivalType))
					{
						showChatWindow(player, 8, "b", false);
					}
					else
					{
						showChatWindow(player, 8, "c", false);
					}
					break;
				case 9:
					if (playerParty == null)
						return;

					boolean isLeader = playerParty.isLeader(player);

					if (isLeader)
					{
						SevenSignsFestival.getInstance().updateParticipants(player, null);
					}

					if (playerParty.getMemberCount() > Config.ALT_FESTIVAL_MIN_PLAYER)
					{
						SevenSignsFestival.getInstance().updateParticipants(player, playerParty);
						playerParty.removePartyMember(player);
					}
					else
					{
						player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_CANT_LEAVE_FESTIVAL));
					}
					break;
				case 0:
					if (!SevenSigns.getInstance().isSealValidationPeriod())
					{
						player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_NO_PRISE_TIL_EVENT_ACTIVE));
						return;
					}

					if (SevenSignsFestival.getInstance().distribAccumulatedBonus(player) > 0)
					{
						showChatWindow(player, 0, "a", false);
					}
					else
					{
						showChatWindow(player, 0, "b", false);
					}
					break;
				default:
					showChatWindow(player, val, null, false);
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	private void showChatWindow(L2PcInstance player, int val, String suffix, boolean isDescription)
	{
		String filename = SevenSigns.SEVEN_SIGNS_HTML_PATH + "festival/";
		filename += isDescription ? "desc_" : "festival_";
		filename += suffix != null ? val + suffix + ".htm" : val + ".htm";

		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%festivalType%", SevenSignsFestival.getFestivalNameText(_festivalType));
		html.replace("%cycleMins%", String.valueOf(SevenSignsFestival.getInstance().getMinsToNextCycle()));
		if (!isDescription && "2b".equals(val + suffix))
		{
			html.replace("%minFestivalPartyMembers%", String.valueOf(Config.ALT_FESTIVAL_MIN_PLAYER));
		}

		if (val == 5)
		{
			html.replace("%statsTable%", getStatsTable());
		}
		else if (val == 6)
		{
			html.replace("%bonusTable%", getBonusTable());
		}
		else if (val == 1)
		{
			html.replace("%blueStoneNeeded%", String.valueOf(_blueStonesNeeded));
			html.replace("%greenStoneNeeded%", String.valueOf(_greenStonesNeeded));
			html.replace("%redStoneNeeded%", String.valueOf(_redStonesNeeded));
		}

		player.sendPacket(html);

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}