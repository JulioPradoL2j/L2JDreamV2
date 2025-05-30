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

import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.game.cache.HtmCache;
import com.dream.game.model.entity.sevensigns.SevenSigns;
import com.dream.game.model.quest.QuestState;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2SignsPriestInstance extends L2NpcInstance
{
	private final static Logger _log = Logger.getLogger(L2SignsPriestInstance.class.getName());

	public L2SignsPriestInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	public String getCabalNpc()
	{
		switch (getTemplate().getNpcId())
		{
			case 31078:
			case 31079:
			case 31080:
			case 31081:
			case 31082:
			case 31083:
			case 31084:
			case 31168:
			case 31692:
			case 31694:
			case 31997:
				return "Dawn";
			case 31085:
			case 31086:
			case 31087:
			case 31088:
			case 31089:
			case 31090:
			case 31091:
			case 31169:
			case 31693:
			case 31695:
			case 31998:
				return "Dusk";
		}
		return null;
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (player.getLastFolkNPC() == null || player.getLastFolkNPC().getObjectId() != getObjectId())
			return;

		if (command.startsWith("SevenSignsDesc"))
		{
			int val = Integer.parseInt(command.substring(15));
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
				player.teleToLocation(-80555, 150337, -3040);
			}

		}
		else if (command.startsWith("SevenSigns"))
		{
			InventoryUpdate iu;
			StatusUpdate su;
			String path;
			int cabal = SevenSigns.CABAL_NULL;
			int stoneType = 0;
			L2ItemInstance ancientAdena = player.getInventory().getItemByItemId(SevenSigns.ANCIENT_ADENA_ID);
			int ancientAdenaAmount = ancientAdena == null ? 0 : ancientAdena.getCount();
			int val = Integer.parseInt(command.substring(11, 12).trim());

			if (command.length() > 12)
			{
				val = Integer.parseInt(command.substring(11, 13).trim());
			}

			if (command.length() > 13)
			{
				try
				{
					cabal = Integer.parseInt(command.substring(14, 15).trim());
				}
				catch (Exception e)
				{
					try
					{
						cabal = Integer.parseInt(command.substring(13, 14).trim());
					}
					catch (Exception e2)
					{
						try
						{
							StringTokenizer st = new StringTokenizer(command.trim());
							st.nextToken();
							cabal = Integer.parseInt(st.nextToken());
						}
						catch (Exception e3)
						{
							_log.warn("Failed to retrieve cabal from bypass command. NpcId: " + getNpcId() + "; Command: " + command);
						}
					}
				}
			}

			switch (val)
			{
				case 2:
					if (!player.getInventory().validateCapacity(1))
					{
						player.sendPacket(SystemMessageId.SLOTS_FULL);
						break;
					}

					if (!player.reduceAdena("SevenSigns", SevenSigns.RECORD_SEVEN_SIGNS_COST, this, true))
					{
						player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
						break;
					}
					L2ItemInstance recordSevenSigns = player.getInventory().addItem("SevenSigns", SevenSigns.RECORD_SEVEN_SIGNS_ID, 1, player, this);

					player.getInventory().updateInventory(recordSevenSigns);

					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1).addItemName(SevenSigns.RECORD_SEVEN_SIGNS_ID));
					if (getCabalNpc() == "Dawn")
					{
						showChatWindow(player, val, "dawn", false);
					}
					else
					{
						showChatWindow(player, val, "dusk", false);
					}
					break;
				case 33:
					int oldCabal = SevenSigns.getInstance().getPlayerCabal(player);

					if (oldCabal != SevenSigns.CABAL_NULL)
					{
						if (getCabalNpc() == "Dawn")
						{
							showChatWindow(player, val, "dawn_member", false);
						}
						else
						{
							showChatWindow(player, val, "dusk_member", false);
						}
						return;
					}
					else if (player.getClassId().level() == 0)
					{
						if (getCabalNpc() == "Dawn")
						{
							showChatWindow(player, val, "dawn_firstclass", false);
						}
						else
						{
							showChatWindow(player, val, "dusk_firstclass", false);
						}
						return;
					}
					else if (cabal == SevenSigns.CABAL_DUSK && Config.ALT_GAME_CASTLE_DUSK)
					{
						if (player.getClan() != null && player.getClan().getHasCastle() > 0)
						{
							showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_33_dusk_no.htm");
							break;
						}
					}
					else if (cabal == SevenSigns.CABAL_DAWN && Config.ALT_GAME_CASTLE_DAWN && player.getClassId().level() > 1)
						if (player.getClan() == null || player.getClan().getHasCastle() == 0)
						{
							showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_33_dawn_fee.htm");
							break;
						}
				case 34:
					if (getCabalNpc() == "Dawn" && player.getClassId().level() > 1)
					{
						boolean fee = true;
						L2ItemInstance adena = player.getInventory().getItemByItemId(57);
						L2ItemInstance certif = player.getInventory().getItemByItemId(5708);
						if (adena != null && adena.getCount() >= 50000 || certif != null && certif.getCount() >= 1)
						{
							fee = false;
						}
						if (fee)
						{
							showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_33_dawn_no.htm");
							break;
						}
					}
				case 3:
				case 8:
					showChatWindow(player, val, SevenSigns.getCabalShortName(cabal), false);
					break;
				case 4:
					int newSeal = Integer.parseInt(command.substring(15));

					if (player.getClassId().level() > 1)
					{
						if (cabal == SevenSigns.CABAL_DUSK && Config.ALT_GAME_CASTLE_DUSK)
							if (player.getClan() != null && player.getClan().getHasCastle() > 0)
							{
								showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_33_dusk_no.htm");
								return;
							}

						if (Config.ALT_GAME_CASTLE_DAWN && cabal == SevenSigns.CABAL_DAWN)
							if (player.getClan() != null && player.getClan().getHasCastle() > 0)
							{

							}
							else if (player.destroyItemByItemId("SevenSigns", SevenSigns.CERTIFICATE_OF_APPROVAL_ID, 1, this, false))
							{
								player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(SevenSigns.CERTIFICATE_OF_APPROVAL_ID).addNumber(1));
							}
							else if (player.reduceAdena("SevenSigns", Config.ALT_DAWN_JOIN_COST, this, false))
							{
								player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ADENA_DISAPPEARED).addNumber(Config.ALT_DAWN_JOIN_COST));
							}
							else
							{
								showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_33_dawn_no.htm");
								return;
							}
					}
					SevenSigns.getInstance().setPlayerInfo(player, cabal, newSeal);

					if (cabal == SevenSigns.CABAL_DAWN)
					{
						player.sendPacket(SystemMessageId.SEVENSIGNS_PARTECIPATION_DAWN);
					}
					else
					{
						player.sendPacket(SystemMessageId.SEVENSIGNS_PARTECIPATION_DUSK);
					}

					switch (newSeal)
					{
						case SevenSigns.SEAL_AVARICE:
							player.sendPacket(SystemMessageId.FIGHT_FOR_AVARICE);
							break;
						case SevenSigns.SEAL_GNOSIS:
							player.sendPacket(SystemMessageId.FIGHT_FOR_GNOSIS);
							break;
						case SevenSigns.SEAL_STRIFE:
							player.sendPacket(SystemMessageId.FIGHT_FOR_STRIFE);
							break;
					}

					showChatWindow(player, 4, SevenSigns.getCabalShortName(cabal), false);
					break;
				case 5:
					if (getCabalNpc() == "Dawn")
					{
						if (SevenSigns.getInstance().getPlayerCabal(player) == SevenSigns.CABAL_NULL)
						{
							showChatWindow(player, val, "dawn_no", false);
						}
						else
						{
							showChatWindow(player, val, "dawn", false);
						}
					}
					else if (SevenSigns.getInstance().getPlayerCabal(player) == SevenSigns.CABAL_NULL)
					{
						showChatWindow(player, val, "dusk_no", false);
					}
					else
					{
						showChatWindow(player, val, "dusk", false);
					}
					break;
				case 21:
					int contribStoneId = Integer.parseInt(command.substring(14, 18));
					L2ItemInstance contribBlueStones = player.getInventory().getItemByItemId(SevenSigns.SEAL_STONE_BLUE_ID);
					L2ItemInstance contribGreenStones = player.getInventory().getItemByItemId(SevenSigns.SEAL_STONE_GREEN_ID);
					L2ItemInstance contribRedStones = player.getInventory().getItemByItemId(SevenSigns.SEAL_STONE_RED_ID);
					int contribBlueStoneCount = contribBlueStones == null ? 0 : contribBlueStones.getCount();
					int contribGreenStoneCount = contribGreenStones == null ? 0 : contribGreenStones.getCount();
					int contribRedStoneCount = contribRedStones == null ? 0 : contribRedStones.getCount();
					int score = SevenSigns.getInstance().getPlayerContribScore(player);
					int contributionCount = 0;
					boolean contribStonesFound = false;

					int redContrib = 0;
					int greenContrib = 0;
					int blueContrib = 0;

					try
					{
						contributionCount = Integer.parseInt(command.substring(19).trim());
					}
					catch (Exception NumberFormatException)
					{
						if (getCabalNpc() == "Dawn")
						{
							showChatWindow(player, 6, "dawn_failure", false);
						}
						else
						{
							showChatWindow(player, 6, "dusk_failure", false);
						}
						break;
					}

					switch (contribStoneId)
					{
						case SevenSigns.SEAL_STONE_BLUE_ID:
							blueContrib = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - score) / SevenSigns.BLUE_CONTRIB_POINTS;
							if (blueContrib > contribBlueStoneCount)
							{
								blueContrib = contributionCount;
							}
							break;
						case SevenSigns.SEAL_STONE_GREEN_ID:
							greenContrib = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - score) / SevenSigns.GREEN_CONTRIB_POINTS;
							if (greenContrib > contribGreenStoneCount)
							{
								greenContrib = contributionCount;
							}
							break;
						case SevenSigns.SEAL_STONE_RED_ID:
							redContrib = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - score) / SevenSigns.RED_CONTRIB_POINTS;
							if (redContrib > contribRedStoneCount)
							{
								redContrib = contributionCount;
							}
							break;
					}

					if (redContrib > 0)
						if (player.destroyItemByItemId("SevenSigns", SevenSigns.SEAL_STONE_RED_ID, redContrib, this, false))
						{
							contribStonesFound = true;
							player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(SevenSigns.SEAL_STONE_RED_ID).addNumber(redContrib));
						}
					if (greenContrib > 0)
						if (player.destroyItemByItemId("SevenSigns", SevenSigns.SEAL_STONE_GREEN_ID, greenContrib, this, false))
						{
							contribStonesFound = true;
							player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(SevenSigns.SEAL_STONE_GREEN_ID).addNumber(greenContrib));
						}
					if (blueContrib > 0)
						if (player.destroyItemByItemId("SevenSigns", SevenSigns.SEAL_STONE_BLUE_ID, blueContrib, this, false))
						{
							contribStonesFound = true;
							player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(SevenSigns.SEAL_STONE_BLUE_ID).addNumber(blueContrib));
						}

					if (!contribStonesFound)
					{
						if (getCabalNpc() == "Dawn")
						{
							showChatWindow(player, 6, "dawn_low_stones", false);
						}
						else
						{
							showChatWindow(player, 6, "dusk_low_stones", false);
						}
					}
					else
					{
						score = SevenSigns.getInstance().addPlayerStoneContrib(player, blueContrib, greenContrib, redContrib);
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CONTRIB_SCORE_INCREASED_BY_S1).addNumber(score));
						if (getCabalNpc() == "Dawn")
						{
							showChatWindow(player, 6, "dawn", false);
						}
						else
						{
							showChatWindow(player, 6, "dusk", false);
						}
					}
					break;
				case 6:
					stoneType = Integer.parseInt(command.substring(13));
					L2ItemInstance redStones = player.getInventory().getItemByItemId(SevenSigns.SEAL_STONE_RED_ID);
					int redStoneCount = redStones == null ? 0 : redStones.getCount();
					L2ItemInstance greenStones = player.getInventory().getItemByItemId(SevenSigns.SEAL_STONE_GREEN_ID);
					int greenStoneCount = greenStones == null ? 0 : greenStones.getCount();
					L2ItemInstance blueStones = player.getInventory().getItemByItemId(SevenSigns.SEAL_STONE_BLUE_ID);
					int blueStoneCount = blueStones == null ? 0 : blueStones.getCount();
					int contribScore = SevenSigns.getInstance().getPlayerContribScore(player);
					boolean stonesFound = false;

					if (contribScore == Config.ALT_MAXIMUM_PLAYER_CONTRIB)
					{
						player.sendPacket(SystemMessageId.CONTRIB_SCORE_EXCEEDED);
						break;
					}
					int redContribCount = 0;
					int greenContribCount = 0;
					int blueContribCount = 0;

					String contentContr;
					String contribStoneColor = null;
					String stoneColorContr = null;
					String stoneCountContr = "";
					int stoneIdContr = 0;

					switch (stoneType)
					{
						case 1:
							contribStoneColor = "Blue";
							stoneIdContr = SevenSigns.SEAL_STONE_BLUE_ID;
							if (blueStoneCount == 1)
							{
								stoneCountContr = String.valueOf(blueStoneCount) + "-m";
								stoneColorContr = "Blue stone";
							}
							else if (blueStoneCount < 5)
							{
								stoneCountContr = String.valueOf(blueStoneCount) + "-Mya";
								stoneColorContr = "Blue stones";
							}
							else
							{
								stoneCountContr = String.valueOf(blueStoneCount) + "-Yu";
								stoneColorContr = "Blue stones";
							}
							break;
						case 2:
							contribStoneColor = "Green";
							stoneIdContr = SevenSigns.SEAL_STONE_GREEN_ID;
							if (greenStoneCount == 1)
							{
								stoneCountContr = String.valueOf(greenStoneCount) + "-m";
								stoneColorContr = "Green Stone";
							}
							else if (greenStoneCount < 5)
							{
								stoneCountContr = String.valueOf(greenStoneCount) + "-Mya";
								stoneColorContr = "Green Stone";
							}
							else
							{
								stoneCountContr = String.valueOf(greenStoneCount) + "-You";
								stoneColorContr = "Green Stone";
							}
							break;
						case 3:
							contribStoneColor = "Red";
							stoneIdContr = SevenSigns.SEAL_STONE_RED_ID;
							if (redStoneCount == 1)
							{
								stoneCountContr = String.valueOf(redStoneCount) + "-m";
								stoneColorContr = "Red Stone";
							}
							else if (redStoneCount < 5)
							{
								stoneCountContr = String.valueOf(redStoneCount) + "-Mya";
								stoneColorContr = "Red Stone";
							}
							else
							{
								stoneCountContr = String.valueOf(redStoneCount) + "-You";
								stoneColorContr = "Red Stone";
							}
							break;
						case 4:
							int tempContribScore = contribScore;
							redContribCount = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - tempContribScore) / SevenSigns.RED_CONTRIB_POINTS;
							if (redContribCount > redStoneCount)
							{
								redContribCount = redStoneCount;
							}
							tempContribScore += redContribCount * SevenSigns.RED_CONTRIB_POINTS;
							greenContribCount = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - tempContribScore) / SevenSigns.GREEN_CONTRIB_POINTS;
							if (greenContribCount > greenStoneCount)
							{
								greenContribCount = greenStoneCount;
							}
							tempContribScore += greenContribCount * SevenSigns.GREEN_CONTRIB_POINTS;
							blueContribCount = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - tempContribScore) / SevenSigns.BLUE_CONTRIB_POINTS;
							if (blueContribCount > blueStoneCount)
							{
								blueContribCount = blueStoneCount;
							}
							if (redContribCount > 0)
								if (player.destroyItemByItemId("SevenSigns", SevenSigns.SEAL_STONE_RED_ID, redContribCount, this, false))
								{
									stonesFound = true;
									player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(SevenSigns.SEAL_STONE_RED_ID).addNumber(redContribCount));
								}
							if (greenContribCount > 0)
								if (player.destroyItemByItemId("SevenSigns", SevenSigns.SEAL_STONE_GREEN_ID, greenContribCount, this, false))
								{
									stonesFound = true;
									player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(SevenSigns.SEAL_STONE_GREEN_ID).addNumber(greenContribCount));
								}
							if (blueContribCount > 0)
								if (player.destroyItemByItemId("SevenSigns", SevenSigns.SEAL_STONE_BLUE_ID, blueContribCount, this, false))
								{
									stonesFound = true;
									player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(SevenSigns.SEAL_STONE_BLUE_ID).addNumber(blueContribCount));
								}

							if (!stonesFound)
							{
								if (getCabalNpc() == "Dawn")
								{
									showChatWindow(player, val, "dawn_no_stones", false);
								}
								else
								{
									showChatWindow(player, val, "dusk_no_stones", false);
								}
							}
							else
							{
								contribScore = SevenSigns.getInstance().addPlayerStoneContrib(player, blueContribCount, greenContribCount, redContribCount);
								player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CONTRIB_SCORE_INCREASED_BY_S1).addNumber(contribScore));
								if (getCabalNpc() == "Dawn")
								{
									showChatWindow(player, 6, "dawn", false);
								}
								else
								{
									showChatWindow(player, 6, "dusk", false);
								}
							}
							return;
					}

					if (getCabalNpc() == "Dawn")
					{
						path = SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_6_dawn_contribute.htm";
					}
					else
					{
						path = SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_6_dusk_contribute.htm";
					}
					contentContr = HtmCache.getInstance().getHtm(path);

					if (contentContr != null)
					{
						contentContr = contentContr.replaceAll("%contribStoneColor%", contribStoneColor);
						contentContr = contentContr.replaceAll("%stoneColor%", stoneColorContr);
						contentContr = contentContr.replaceAll("%stoneCount%", stoneCountContr);
						contentContr = contentContr.replaceAll("%stoneItemId%", String.valueOf(stoneIdContr));
						contentContr = contentContr.replaceAll("%objectId%", String.valueOf(getObjectId()));

						NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
						html.setHtml(contentContr);
						player.sendPacket(html);
					}
					else
					{
						_log.warn("Problem with HTML text " + path);
					}
					break;
				case 7:
					int ancientAdenaConvert = 0;

					try
					{
						ancientAdenaConvert = Integer.parseInt(command.substring(13).trim());
					}
					catch (NumberFormatException e)
					{
						showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "blkmrkt_3.htm");
						break;
					}
					catch (StringIndexOutOfBoundsException e)
					{
						showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "blkmrkt_3.htm");
						break;
					}

					if (ancientAdenaConvert < 1)
					{
						showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "blkmrkt_3.htm");
						break;
					}
					if (ancientAdenaAmount < ancientAdenaConvert)
					{
						showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "blkmrkt_4.htm");
						break;
					}

					player.reduceAncientAdena("SevenSigns", ancientAdenaConvert, this, true);
					player.addAdena("SevenSigns", ancientAdenaConvert, this, true);

					iu = new InventoryUpdate();
					iu.addModifiedItem(player.getInventory().getAncientAdenaInstance());
					iu.addModifiedItem(player.getInventory().getAdenaInstance());
					showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "blkmrkt_5.htm");
					player.sendPacket(iu);
					break;
				case 9:
					int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
					int winningCabal = SevenSigns.getInstance().getCabalHighestScore();

					if (SevenSigns.getInstance().isSealValidationPeriod() && playerCabal == winningCabal)
					{
						int ancientAdenaReward = SevenSigns.getInstance().getAncientAdenaReward(player, true);

						if (ancientAdenaReward < 3)
						{
							if (getCabalNpc() == "Dawn")
							{
								showChatWindow(player, 9, "dawn_b", false);
							}
							else
							{
								showChatWindow(player, 9, "dusk_b", false);
							}
							break;
						}

						player.addAncientAdena("SevenSigns", ancientAdenaReward, this, true);

						iu = new InventoryUpdate();
						iu.addModifiedItem(player.getInventory().getAncientAdenaInstance());
						sendPacket(iu);

						su = new StatusUpdate(player);
						su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
						sendPacket(su);

						if (getCabalNpc() == "Dawn")
						{
							showChatWindow(player, 9, "dawn_a", false);
						}
						else
						{
							showChatWindow(player, 9, "dusk_a", false);
						}
					}
					break;
				case 11:
					try
					{
						String portInfo = command.substring(14).trim();

						StringTokenizer st = new StringTokenizer(portInfo);
						int x = Integer.parseInt(st.nextToken());
						int y = Integer.parseInt(st.nextToken());
						int z = Integer.parseInt(st.nextToken());
						int ancientAdenaCost = Integer.parseInt(st.nextToken());

						if (ancientAdenaCost > 0)
							if (!player.reduceAncientAdena("SevenSigns", ancientAdenaCost, this, true))
							{
								break;
							}

						player.teleToLocation(x, y, z);
					}
					catch (Exception e)
					{
						_log.warn("SevenSigns: Error occurred while teleporting player: " + e);
					}
					break;
				case 16:
					if (getCabalNpc() == "Dawn")
					{
						showChatWindow(player, val, "dawn", false);
					}
					else
					{
						showChatWindow(player, val, "dusk", false);
					}
					break;
				case 17:
					stoneType = Integer.parseInt(command.substring(14));
					int stoneId = 0;
					int stoneCount = 0;
					int stoneValue = 0;
					String stoneColor = null;
					String content;

					switch (stoneType)
					{
						case 1:
							stoneColor = "blue";
							stoneId = SevenSigns.SEAL_STONE_BLUE_ID;
							stoneValue = SevenSigns.SEAL_STONE_BLUE_VALUE;
							break;
						case 2:
							stoneColor = "green";
							stoneId = SevenSigns.SEAL_STONE_GREEN_ID;
							stoneValue = SevenSigns.SEAL_STONE_GREEN_VALUE;
							break;
						case 3:
							stoneColor = "red";
							stoneId = SevenSigns.SEAL_STONE_RED_ID;
							stoneValue = SevenSigns.SEAL_STONE_RED_VALUE;
							break;
						case 4:
							L2ItemInstance blueStonesAll = player.getInventory().getItemByItemId(SevenSigns.SEAL_STONE_BLUE_ID);
							L2ItemInstance greenStonesAll = player.getInventory().getItemByItemId(SevenSigns.SEAL_STONE_GREEN_ID);
							L2ItemInstance redStonesAll = player.getInventory().getItemByItemId(SevenSigns.SEAL_STONE_RED_ID);
							int blueStoneCountAll = blueStonesAll == null ? 0 : blueStonesAll.getCount();
							int greenStoneCountAll = greenStonesAll == null ? 0 : greenStonesAll.getCount();
							int redStoneCountAll = redStonesAll == null ? 0 : redStonesAll.getCount();
							int ancientAdenaRewardAll = 0;

							ancientAdenaRewardAll = SevenSigns.calcAncientAdenaReward(blueStoneCountAll, greenStoneCountAll, redStoneCountAll);

							if (ancientAdenaRewardAll == 0)
							{
								if (getCabalNpc() == "Dawn")
								{
									showChatWindow(player, 18, "dawn_no_stones", false);
								}
								else
								{
									showChatWindow(player, 18, "dusk_no_stones", false);
								}
								return;
							}

							if (blueStoneCountAll > 0)
							{
								player.destroyItemByItemId("SevenSigns", SevenSigns.SEAL_STONE_BLUE_ID, blueStoneCountAll, this, true);
							}
							if (greenStoneCountAll > 0)
							{
								player.destroyItemByItemId("SevenSigns", SevenSigns.SEAL_STONE_GREEN_ID, greenStoneCountAll, this, true);
							}
							if (redStoneCountAll > 0)
							{
								player.destroyItemByItemId("SevenSigns", SevenSigns.SEAL_STONE_RED_ID, redStoneCountAll, this, true);
							}

							player.addAncientAdena("SevenSigns", ancientAdenaRewardAll, this, true);

							iu = new InventoryUpdate();
							iu.addModifiedItem(player.getInventory().getAncientAdenaInstance());
							if (blueStonesAll != null)
							{
								iu.addModifiedItem(blueStonesAll);
							}
							if (greenStonesAll != null)
							{
								iu.addModifiedItem(greenStonesAll);
							}
							if (redStonesAll != null)
							{
								iu.addModifiedItem(redStonesAll);
							}
							sendPacket(iu);

							su = new StatusUpdate(player);
							su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
							sendPacket(su);

							if (getCabalNpc() == "Dawn")
							{
								showChatWindow(player, 18, "dawn", false);
							}
							else
							{
								showChatWindow(player, 18, "dusk", false);
							}
							return;
					}

					L2ItemInstance stoneInstance = player.getInventory().getItemByItemId(stoneId);

					if (stoneInstance != null)
					{
						stoneCount = stoneInstance.getCount();
					}

					if (getCabalNpc() == "Dawn")
					{
						path = SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_17_dawn.htm";
					}
					else
					{
						path = SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_17_dusk.htm";
					}
					content = HtmCache.getInstance().getHtm(path);

					if (content != null)
					{
						content = content.replaceAll("%stoneColor%", stoneColor);
						content = content.replaceAll("%stoneValue%", String.valueOf(stoneValue));
						content = content.replaceAll("%stoneCount%", String.valueOf(stoneCount));
						content = content.replaceAll("%stoneItemId%", String.valueOf(stoneId));
						content = content.replaceAll("%objectId%", String.valueOf(getObjectId()));

						NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
						html.setHtml(content);
						player.sendPacket(html);
					}
					else
					{
						_log.warn("Problem with HTML text " + SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_17.htm: " + path);
					}
					break;
				case 18:
					int convertStoneId = Integer.parseInt(command.substring(14, 18));
					int convertCount = 0;

					try
					{
						convertCount = Integer.parseInt(command.substring(19).trim());
					}
					catch (Exception NumberFormatException)
					{
						if (getCabalNpc() == "Dawn")
						{
							showChatWindow(player, 18, "dawn_failed", false);
						}
						else
						{
							showChatWindow(player, 18, "dusk_failed", false);
						}
						break;
					}

					L2ItemInstance convertItem = player.getInventory().getItemByItemId(convertStoneId);

					if (convertItem != null)
					{
						int ancientAdenaReward = 0;
						int totalCount = convertItem.getCount();

						if (convertCount <= totalCount && convertCount > 0)
						{
							switch (convertStoneId)
							{
								case SevenSigns.SEAL_STONE_BLUE_ID:
									ancientAdenaReward = SevenSigns.calcAncientAdenaReward(convertCount, 0, 0);
									break;
								case SevenSigns.SEAL_STONE_GREEN_ID:
									ancientAdenaReward = SevenSigns.calcAncientAdenaReward(0, convertCount, 0);
									break;
								case SevenSigns.SEAL_STONE_RED_ID:
									ancientAdenaReward = SevenSigns.calcAncientAdenaReward(0, 0, convertCount);
									break;
							}

							if (player.destroyItemByItemId("SevenSigns", convertStoneId, convertCount, this, true))
							{
								player.addAncientAdena("SevenSigns", ancientAdenaReward, this, true);

								iu = new InventoryUpdate();
								iu.addModifiedItem(player.getInventory().getAncientAdenaInstance());
								iu.addModifiedItem(convertItem);
								sendPacket(iu);

								su = new StatusUpdate(player);
								su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
								sendPacket(su);

								if (getCabalNpc() == "Dawn")
								{
									showChatWindow(player, 18, "dawn", false);
								}
								else
								{
									showChatWindow(player, 18, "dusk", false);
								}
							}
						}
						else
						{
							if (getCabalNpc() == "Dawn")
							{
								showChatWindow(player, 18, "dawn_low_stones", false);
							}
							else
							{
								showChatWindow(player, 18, "dusk_low_stones", false);
							}
							break;
						}
					}
					else
					{
						if (getCabalNpc() == "Dawn")
						{
							showChatWindow(player, 18, "dawn_low_stones", false);
						}
						else
						{
							showChatWindow(player, 18, "dusk_low_stones", false);
						}
						break;
					}
					break;
				case 19:
					int chosenSeal = Integer.parseInt(command.substring(16));
					String fileSuffix = SevenSigns.getSealName(chosenSeal, true) + "_" + SevenSigns.getCabalShortName(cabal);

					showChatWindow(player, val, fileSuffix, false);
					break;
				case 20:
					StringBuilder contentBuffer = new StringBuilder();
					if (getCabalNpc() == "Dawn")
					{
						contentBuffer.append("<html><body>The Priest Of Dawn:<br><font color=\"LEVEL\">[ State Seals ]</font><br>");
					}
					else
					{
						contentBuffer.append("<html><body>Priestess Of The Sunset:<br><font color=\"LEVEL\">[ State Seals ]</font><br>");
					}

					for (int i = 1; i < 4; i++)
					{
						int sealOwner = SevenSigns.getInstance().getSealOwner(i);

						if (sealOwner != SevenSigns.CABAL_NULL)
						{
							contentBuffer.append("[" + SevenSigns.getSealNameText(i, false) + ": " + SevenSigns.getCabalNameText(sealOwner) + "]<br>");
						}
						else
						{
							contentBuffer.append("[" + SevenSigns.getSealNameText(i, false) + " No owner]<br>");
						}
					}

					contentBuffer.append("<a action=\"bypass -h npc_" + getObjectId() + "_Chat 0\">Back</a></body></html>");

					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setHtml(contentBuffer.toString());
					player.sendPacket(html);
					break;
				default:
					showChatWindow(player, val, null, false);
					break;
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	private void showChatWindow(L2PcInstance player, int val, String suffix, boolean isDescription)
	{
		String filename = SevenSigns.SEVEN_SIGNS_HTML_PATH;

		filename += isDescription ? "desc_" + val : "signs_" + val;
		filename += suffix != null ? "_" + suffix + ".htm" : ".htm";

		showChatWindow(player, filename);
	}
}