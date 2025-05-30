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

import java.text.SimpleDateFormat;
import java.util.StringTokenizer;

import com.dream.Config;
import com.dream.Message;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.datatables.xml.TeleportLocationTable;
import com.dream.game.manager.ClanHallManager;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.position.L2TeleportLocation;
import com.dream.game.model.entity.ClanHall;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.AgitDecoInfo;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.network.serverpackets.WareHouseDepositList;
import com.dream.game.network.serverpackets.WareHouseWithdrawalList;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.game.templates.skills.L2SkillType;

public class L2ClanHallManagerInstance extends L2MerchantInstance
{
	protected static final int COND_OWNER_FALSE = 0;

	protected static final int COND_ALL_FALSE = 1;

	protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 2;
	protected static final int COND_OWNER = 3;

	private static void revalidateDeco(L2PcInstance player)
	{
		ClanHall ch = ClanHallManager.getInstance().getClanHallByOwner(player.getClan());
		if (ch != null)
		{
			player.sendPacket(new AgitDecoInfo(ch));
		}
	}

	private static void showVaultWindowDeposit(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		player.setActiveWarehouse(player.getClan().getWarehouse());
		player.sendPacket(new WareHouseDepositList(player, WareHouseDepositList.CLAN));
	}

	private int _clanHallId = -1;

	public L2ClanHallManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	private void doTeleport(L2PcInstance player, int val)
	{
		L2TeleportLocation list = TeleportLocationTable.getInstance().getTemplate(val);
		if (list != null)
		{
			if (player.reduceAdena("Teleport", list.getPrice(), this, true))
			{
				player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ());
			}
		}
		else
		{
			_log.info("No teleport destination with id:" + val);
		}

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public final ClanHall getClanHall()
	{
		if (_clanHallId < 0)
		{
			ClanHall temp = ClanHallManager.getInstance().getNearbyClanHall(getX(), getY(), 500);

			if (temp != null)
			{
				_clanHallId = temp.getId();
			}

			if (_clanHallId < 0)
				return null;
		}
		return ClanHallManager.getInstance().getClanHallById(_clanHallId);
	}

	private synchronized void magicSupport(L2PcInstance player, String val, StringTokenizer st)
	{
		setTarget(player);
		L2Skill skill;
		if (val.isEmpty())
			return;

		try
		{
			int skill_id = Integer.parseInt(val);
			try
			{
				int skill_lvl = 0;
				if (st.countTokens() >= 1)
				{
					skill_lvl = Integer.parseInt(st.nextToken());
				}
				skill = SkillTable.getInstance().getInfo(skill_id, skill_lvl);

				if (skill == null)
					return;

				int mpConsume = skill.getMpConsume();
				if (mpConsume > getStatus().getCurrentMp())
				{
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile("data/html/clanHallManager/support-no_mana.htm");
					html.replace("%mp%", String.valueOf((int) getStatus().getCurrentMp()));
					sendHtmlMessage(player, html);
					return;
				}
				getStatus().reduceMp(mpConsume);
				if (skill.getSkillType() == L2SkillType.SUMMON)
				{
					player.doSimultaneousCast(skill);
				}
				else
				{
					doCast(skill);
				}

				if (getClanHall().getFunction(ClanHall.FUNC_SUPPORT) == null)
					return;

				NpcHtmlMessage html = new NpcHtmlMessage(1);
				if (getClanHall().getFunction(ClanHall.FUNC_SUPPORT).getLvl() == 0)
					return;

				if (getClanHall().getFunction(ClanHall.FUNC_SUPPORT).getLvl() == 0)
					return;
				html.setFile("data/html/clanHallManager/support" + getClanHall().getFunction(ClanHall.FUNC_SUPPORT).getLvl() + ".htm");
				html.replace("%mp%", String.valueOf((int) getStatus().getCurrentMp()));
				sendHtmlMessage(player, html);
			}
			catch (Exception e)
			{
				player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_WRONG_SKILL_LEVEL_CONTACT_ADMINISTRATOR));
			}
		}
		catch (Exception e)
		{
			player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_WRONG_SKILL_LEVEL_CONTACT_ADMINISTRATOR));
		}
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		player.setLastFolkNPC(this);

		if (!canTarget(player))
			return;

		if (this != player.getTarget())
		{
			player.setTarget(this);

			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);

			player.sendPacket(new ValidateLocation(this));
		}
		else if (!canInteract(player))
		{
		}
		else
		{
			showMessageWindow(player);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		int condition = validateCondition(player);
		if (condition <= COND_ALL_FALSE)
			return;
		else if (condition == COND_OWNER)
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			String actualCommand = st.nextToken();
			String val = "";
			if (st.countTokens() >= 1)
			{
				val = st.nextToken();
			}

			if (actualCommand.equalsIgnoreCase("banish_foreigner"))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				if ((player.getClanPrivileges() & L2Clan.CP_CH_DISMISS) == L2Clan.CP_CH_DISMISS)
				{
					if (val.equalsIgnoreCase("list"))
					{
						html.setFile("data/html/clanHallManager/banish-list.htm");
					}
					else if (val.equalsIgnoreCase("banish"))
					{
						getClanHall().banishForeigners();
						html.setFile("data/html/clanHallManager/banish.htm");
					}
				}
				else
				{
					html.setFile("data/html/clanHallManager/not_authorized.htm");
				}
				sendHtmlMessage(player, html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("manage_vault"))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				if ((player.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE) == L2Clan.CP_CL_VIEW_WAREHOUSE)
				{
					if (val.equalsIgnoreCase("deposit"))
					{
						showVaultWindowDeposit(player);
					}
					else if (val.equalsIgnoreCase("withdraw"))
					{
						showVaultWindowWithdraw(player);
					}
					else
					{
						html.setFile("data/html/clanHallManager/vault.htm");
						html.replace("%rent%", String.valueOf(getClanHall().getLease()));
						html.replace("%date%", format.format(getClanHall().getPaidUntil()));
						sendHtmlMessage(player, html);
					}
				}
				else
				{
					html.setFile("data/html/clanHallManager/not_authorized.htm");
					sendHtmlMessage(player, html);
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("door"))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				if ((player.getClanPrivileges() & L2Clan.CP_CH_OPEN_DOOR) == L2Clan.CP_CH_OPEN_DOOR)
				{
					if (val.equalsIgnoreCase("open"))
					{
						getClanHall().openCloseDoors(true);
						html.setFile("data/html/clanHallManager/door-open.htm");
					}
					else if (val.equalsIgnoreCase("close"))
					{
						getClanHall().openCloseDoors(false);
						html.setFile("data/html/clanHallManager/door-close.htm");
					}
					else
					{
						html.setFile("data/html/clanHallManager/door.htm");
					}
					sendHtmlMessage(player, html);
				}
				else
				{
					html.setFile("data/html/clanHallManager/not_authorized.htm");
					sendHtmlMessage(player, html);
				}
			}
			else if (actualCommand.equalsIgnoreCase("functions"))
			{
				if (val.equalsIgnoreCase("tele"))
				{

					NpcHtmlMessage html = new NpcHtmlMessage(1);
					if (getClanHall().getFunction(ClanHall.FUNC_TELEPORT) == null)
					{
						html.setFile("data/html/clanHallManager/chamberlain-nac.htm");
					}
					else
					{
						html.setFile("data/html/clanHallManager/tele" + getClanHall().getLocation() + getClanHall().getFunction(ClanHall.FUNC_TELEPORT).getLvl() + ".htm");
					}
					sendHtmlMessage(player, html);
				}
				else if (val.equalsIgnoreCase("teleBandit"))
				{

					NpcHtmlMessage html = new NpcHtmlMessage(1);
					if (getClanHall().getFunction(ClanHall.FUNC_TELEPORT) == null)
					{
						html.setFile("data/html/clanHallManager/chamberlain-nac.htm");
					}
					else
					{
						html.setFile("data/html/clanHallManager/teleBandit" + getClanHall().getFunction(ClanHall.FUNC_TELEPORT).getLvl() + ".htm");
					}
					sendHtmlMessage(player, html);
				}
				else if (val.equalsIgnoreCase("teleBeastFarm"))
				{

					NpcHtmlMessage html = new NpcHtmlMessage(1);
					if (getClanHall().getFunction(ClanHall.FUNC_TELEPORT) == null)
					{
						html.setFile("data/html/clanHallManager/chamberlain-nac.htm");
					}
					else
					{
						html.setFile("data/html/clanHallManager/teleBeastFarm" + getClanHall().getFunction(ClanHall.FUNC_TELEPORT).getLvl() + ".htm");
					}
					sendHtmlMessage(player, html);
				}
				else if (val.equalsIgnoreCase("teleDeadForest"))
				{

					NpcHtmlMessage html = new NpcHtmlMessage(1);
					if (getClanHall().getFunction(ClanHall.FUNC_TELEPORT) == null)
					{
						html.setFile("data/html/clanHallManager/chamberlain-nac.htm");
					}
					else
					{
						html.setFile("data/html/clanHallManager/teleDeadForest" + getClanHall().getFunction(ClanHall.FUNC_TELEPORT).getLvl() + ".htm");
					}
					sendHtmlMessage(player, html);
				}
				else if (val.equalsIgnoreCase("teleDevasted"))
				{

					NpcHtmlMessage html = new NpcHtmlMessage(1);
					if (getClanHall().getFunction(ClanHall.FUNC_TELEPORT) == null)
					{
						html.setFile("data/html/clanHallManager/chamberlain-nac.htm");
					}
					else
					{
						html.setFile("data/html/clanHallManager/teleDevastated" + getClanHall().getFunction(ClanHall.FUNC_TELEPORT).getLvl() + ".htm");
					}
					sendHtmlMessage(player, html);
				}
				else if (val.equalsIgnoreCase("teleHotSpring"))
				{

					NpcHtmlMessage html = new NpcHtmlMessage(1);
					if (getClanHall().getFunction(ClanHall.FUNC_TELEPORT) == null)
					{
						html.setFile("data/html/clanHallManager/chamberlain-nac.htm");
					}
					else
					{
						html.setFile("data/html/clanHallManager/teleHotSpring" + getClanHall().getFunction(ClanHall.FUNC_TELEPORT).getLvl() + ".htm");
					}
					sendHtmlMessage(player, html);
				}
				else if (val.equalsIgnoreCase("telePartisan"))
				{

					NpcHtmlMessage html = new NpcHtmlMessage(1);
					if (getClanHall().getFunction(ClanHall.FUNC_TELEPORT) == null)
					{
						html.setFile("data/html/clanHallManager/chamberlain-nac.htm");
					}
					else
					{
						html.setFile("data/html/clanHallManager/telePartisan" + getClanHall().getFunction(ClanHall.FUNC_TELEPORT).getLvl() + ".htm");
					}
					sendHtmlMessage(player, html);
				}
				else if (val.equalsIgnoreCase("item_creation"))
				{
					if (getClanHall().getFunction(ClanHall.FUNC_ITEM_CREATE) == null)
					{
						NpcHtmlMessage html = new NpcHtmlMessage(1);
						html.setFile("data/html/clanHallManager/chamberlain-nac.htm");
						sendHtmlMessage(player, html);
						return;
					}
					if (st.countTokens() < 1)
						return;
					int valbuy = Integer.parseInt(st.nextToken()) + getClanHall().getFunction(ClanHall.FUNC_ITEM_CREATE).getLvl() * 100000;
					showBuyWindow(player, valbuy);
				}
				else if (val.equalsIgnoreCase("support"))
				{

					NpcHtmlMessage html = new NpcHtmlMessage(1);
					if (getClanHall().getFunction(ClanHall.FUNC_SUPPORT) == null)
					{
						html.setFile("data/html/clanHallManager/chamberlain-nac.htm");
					}
					else
					{
						html.setFile("data/html/clanHallManager/support" + getClanHall().getFunction(ClanHall.FUNC_SUPPORT).getLvl() + ".htm");
						html.replace("%mp%", String.valueOf((int) getStatus().getCurrentMp()));
					}
					sendHtmlMessage(player, html);
				}
				else if (val.equalsIgnoreCase("back"))
				{
					showMessageWindow(player);
				}
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					if (_clanHallId == 21)
					{
						html.setFile("data/html/clanHallManager/functions-partisan.htm");
					}
					else if (_clanHallId == 34)
					{
						html.setFile("data/html/clanHallManager/functions-devasted.htm");
					}
					else if (_clanHallId == 35)
					{
						html.setFile("data/html/clanHallManager/functions-bandit.htm");
					}
					else if (_clanHallId == 62)
					{
						html.setFile("data/html/clanHallManager/functions-hotspring.htm");
					}
					else if (_clanHallId == 63)
					{
						html.setFile("data/html/clanHallManager/functions-beast.htm");
					}
					else if (_clanHallId == 64)
					{
						html.setFile("data/html/clanHallManager/functions-dead.htm");
					}
					else
					{
						html.setFile("data/html/clanHallManager/functions.htm");
					}

					if (getClanHall().getFunction(ClanHall.FUNC_RESTORE_EXP) != null)
					{
						html.replace("%xp_regen%", String.valueOf(getClanHall().getFunction(ClanHall.FUNC_RESTORE_EXP).getLvl()));
					}
					else
					{
						html.replace("%xp_regen%", "0");
					}
					if (getClanHall().getFunction(ClanHall.FUNC_RESTORE_HP) != null)
					{
						html.replace("%hp_regen%", String.valueOf(getClanHall().getFunction(ClanHall.FUNC_RESTORE_HP).getLvl()));
					}
					else
					{
						html.replace("%hp_regen%", "0");
					}
					if (getClanHall().getFunction(ClanHall.FUNC_RESTORE_MP) != null)
					{
						html.replace("%mp_regen%", String.valueOf(getClanHall().getFunction(ClanHall.FUNC_RESTORE_MP).getLvl()));
					}
					else
					{
						html.replace("%mp_regen%", "0");
					}
					sendHtmlMessage(player, html);
				}
			}
			else if (actualCommand.equalsIgnoreCase("manage"))
			{
				if ((player.getClanPrivileges() & L2Clan.CP_CH_SET_FUNCTIONS) == L2Clan.CP_CH_SET_FUNCTIONS)
				{
					if (val.equalsIgnoreCase("recovery"))
					{
						if (st.countTokens() >= 1)
						{
							if (getClanHall().getOwnerId() == 0)
							{
								player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_NO_RIGHTS_TO_CONTROLL_HALL));
								return;
							}
							val = st.nextToken();
							if (val.equalsIgnoreCase("hp_cancel"))
							{
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile("data/html/clanHallManager/functions-cancel.htm");
								html.replace("%apply%", "recovery hp 0");
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("mp_cancel"))
							{
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile("data/html/clanHallManager/functions-cancel.htm");
								html.replace("%apply%", "recovery mp 0");
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("exp_cancel"))
							{
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile("data/html/clanHallManager/functions-cancel.htm");
								html.replace("%apply%", "recovery exp 0");
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("edit_hp"))
							{
								val = st.nextToken();
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile("data/html/clanHallManager/functions-apply.htm");
								html.replace("%name%", "fireplace (recovery HP)");
								int percent = Integer.valueOf(val);
								int cost;
								switch (percent)
								{
									case 20:
										cost = Config.CH_HPREG1_FEE;
										break;
									case 40:
										cost = Config.CH_HPREG2_FEE;
										break;
									case 80:
										cost = Config.CH_HPREG3_FEE;
										break;
									case 100:
										cost = Config.CH_HPREG4_FEE;
										break;
									case 120:
										cost = Config.CH_HPREG5_FEE;
										break;
									case 140:
										cost = Config.CH_HPREG6_FEE;
										break;
									case 160:
										cost = Config.CH_HPREG7_FEE;
										break;
									case 180:
										cost = Config.CH_HPREG8_FEE;
										break;
									case 200:
										cost = Config.CH_HPREG9_FEE;
										break;
									case 220:
										cost = Config.CH_HPREG10_FEE;
										break;
									case 240:
										cost = Config.CH_HPREG11_FEE;
										break;
									case 260:
										cost = Config.CH_HPREG12_FEE;
										break;
									default:
										cost = Config.CH_HPREG13_FEE;
										break;
								}

								html.replace("%cost%", String.valueOf(cost) + "(</font> Adena /" + String.valueOf(Config.CH_HPREG_FEE_RATIO / 1000 / 60 / 60 / 24) + " Days</font>)");
								html.replace("%use%", "Provides additional restore HP for members of the clan in the clan Hall. <font color=\"00FFFF\">" + String.valueOf(percent) + "%</font>");
								html.replace("%apply%", "recovery hp " + String.valueOf(percent));
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("edit_mp"))
							{
								val = st.nextToken();
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile("data/html/clanHallManager/functions-apply.htm");
								html.replace("%name%", "Carpet (MP)");
								int percent = Integer.valueOf(val);
								int cost;
								switch (percent)
								{
									case 5:
										cost = Config.CH_MPREG1_FEE;
										break;
									case 10:
										cost = Config.CH_MPREG2_FEE;
										break;
									case 15:
										cost = Config.CH_MPREG3_FEE;
										break;
									case 30:
										cost = Config.CH_MPREG4_FEE;
										break;
									default:
										cost = Config.CH_MPREG5_FEE;
										break;
								}
								html.replace("%cost%", String.valueOf(cost) + "(</font> Adena /" + String.valueOf(Config.CH_MPREG_FEE_RATIO / 1000 / 60 / 60 / 24) + " Days</font>)");
								html.replace("%use%", "Provides additional recovery of MP for the clan in the clan Hall.<font color=\"00FFFF\">" + String.valueOf(percent) + "%</font>");
								html.replace("%apply%", "recovery mp " + String.valueOf(percent));
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("edit_exp"))
							{
								val = st.nextToken();
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile("data/html/clanHallManager/functions-apply.htm");
								html.replace("%name%", "Chandelier (restoration of the lost experience)");
								int percent = Integer.valueOf(val);
								int cost;
								switch (percent)
								{
									case 5:
										cost = Config.CH_EXPREG1_FEE;
										break;
									case 10:
										cost = Config.CH_EXPREG2_FEE;
										break;
									case 15:
										cost = Config.CH_EXPREG3_FEE;
										break;
									case 25:
										cost = Config.CH_EXPREG4_FEE;
										break;
									case 35:
										cost = Config.CH_EXPREG5_FEE;
										break;
									case 40:
										cost = Config.CH_EXPREG6_FEE;
										break;
									default:
										cost = Config.CH_EXPREG7_FEE;
										break;
								}
								html.replace("%cost%", String.valueOf(cost) + "(</font>Adena /" + String.valueOf(Config.CH_EXPREG_FEE_RATIO / 1000 / 60 / 60 / 24) + " Days</font>)");
								html.replace("%use%", "Вosstanavlivaet experience of any member of the voskrešonnogo clan in the clan Hall.<font color=\"00FFFF\">" + String.valueOf(percent) + "%</font>");
								html.replace("%apply%", "recovery exp " + String.valueOf(percent));
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("hp"))
							{
								if (st.countTokens() >= 1)
								{
									int fee;
									val = st.nextToken();
									NpcHtmlMessage html = new NpcHtmlMessage(1);
									html.setFile("data/html/clanHallManager/functions-apply_confirmed.htm");
									if (getClanHall().getFunction(ClanHall.FUNC_RESTORE_HP) != null)
										if (getClanHall().getFunction(ClanHall.FUNC_RESTORE_HP).getLvl() == Integer.valueOf(val))
										{
											html.setFile("data/html/clanHallManager/functions-used.htm");
											html.replace("%val%", String.valueOf(val) + "%");
											sendHtmlMessage(player, html);
											return;
										}
									int percent = Integer.valueOf(val);
									switch (percent)
									{
										case 0:
											fee = 0;
											html.setFile("data/html/clanHallManager/functions-cancel_confirmed.htm");
											break;
										case 20:
											fee = Config.CH_HPREG1_FEE;
											break;
										case 40:
											fee = Config.CH_HPREG2_FEE;
											break;
										case 80:
											fee = Config.CH_HPREG3_FEE;
											break;
										case 100:
											fee = Config.CH_HPREG4_FEE;
											break;
										case 120:
											fee = Config.CH_HPREG5_FEE;
											break;
										case 140:
											fee = Config.CH_HPREG6_FEE;
											break;
										case 160:
											fee = Config.CH_HPREG7_FEE;
											break;
										case 180:
											fee = Config.CH_HPREG8_FEE;
											break;
										case 200:
											fee = Config.CH_HPREG9_FEE;
											break;
										case 220:
											fee = Config.CH_HPREG10_FEE;
											break;
										case 240:
											fee = Config.CH_HPREG11_FEE;
											break;
										case 260:
											fee = Config.CH_HPREG12_FEE;
											break;
										default:
											fee = Config.CH_HPREG13_FEE;
											break;
									}
									if (!getClanHall().updateFunctions(player, ClanHall.FUNC_RESTORE_HP, percent, fee, Config.CH_HPREG_FEE_RATIO, getClanHall().getFunction(ClanHall.FUNC_RESTORE_HP) == null))
									{
										html.setFile("data/html/clanHallManager/low_adena.htm");
										sendHtmlMessage(player, html);
									}
									else
									{
										revalidateDeco(player);
									}
									sendHtmlMessage(player, html);
								}
								return;
							}
							else if (val.equalsIgnoreCase("mp"))
							{
								if (st.countTokens() >= 1)
								{
									int fee;
									val = st.nextToken();
									NpcHtmlMessage html = new NpcHtmlMessage(1);
									html.setFile("data/html/clanHallManager/functions-apply_confirmed.htm");
									if (getClanHall().getFunction(ClanHall.FUNC_RESTORE_MP) != null)
										if (getClanHall().getFunction(ClanHall.FUNC_RESTORE_MP).getLvl() == Integer.valueOf(val))
										{
											html.setFile("data/html/clanHallManager/functions-used.htm");
											html.replace("%val%", String.valueOf(val) + "%");
											sendHtmlMessage(player, html);
											return;
										}
									int percent = Integer.valueOf(val);
									switch (percent)
									{
										case 0:
											fee = 0;
											html.setFile("data/html/clanHallManager/functions-cancel_confirmed.htm");
											break;
										case 5:
											fee = Config.CH_MPREG1_FEE;
											break;
										case 10:
											fee = Config.CH_MPREG2_FEE;
											break;
										case 15:
											fee = Config.CH_MPREG3_FEE;
											break;
										case 30:
											fee = Config.CH_MPREG4_FEE;
											break;
										default:
											fee = Config.CH_MPREG5_FEE;
											break;
									}
									if (!getClanHall().updateFunctions(player, ClanHall.FUNC_RESTORE_MP, percent, fee, Config.CH_MPREG_FEE_RATIO, getClanHall().getFunction(ClanHall.FUNC_RESTORE_MP) == null))
									{
										html.setFile("data/html/clanHallManager/low_adena.htm");
										sendHtmlMessage(player, html);
									}
									else
									{
										revalidateDeco(player);
									}
									sendHtmlMessage(player, html);
								}
								return;
							}
							else if (val.equalsIgnoreCase("exp"))
							{
								if (st.countTokens() >= 1)
								{
									int fee;
									val = st.nextToken();
									NpcHtmlMessage html = new NpcHtmlMessage(1);
									html.setFile("data/html/clanHallManager/functions-apply_confirmed.htm");
									if (getClanHall().getFunction(ClanHall.FUNC_RESTORE_EXP) != null)
										if (getClanHall().getFunction(ClanHall.FUNC_RESTORE_EXP).getLvl() == Integer.valueOf(val))
										{
											html.setFile("data/html/clanHallManager/functions-used.htm");
											html.replace("%val%", String.valueOf(val) + "%");
											sendHtmlMessage(player, html);
											return;
										}
									int percent = Integer.valueOf(val);
									switch (percent)
									{
										case 0:
											fee = 0;
											html.setFile("data/html/clanHallManager/functions-cancel_confirmed.htm");
											break;
										case 5:
											fee = Config.CH_EXPREG1_FEE;
											break;
										case 10:
											fee = Config.CH_EXPREG2_FEE;
											break;
										case 15:
											fee = Config.CH_EXPREG3_FEE;
											break;
										case 25:
											fee = Config.CH_EXPREG4_FEE;
											break;
										case 35:
											fee = Config.CH_EXPREG5_FEE;
											break;
										case 40:
											fee = Config.CH_EXPREG6_FEE;
											break;
										default:
											fee = Config.CH_EXPREG7_FEE;
											break;
									}
									if (!getClanHall().updateFunctions(player, ClanHall.FUNC_RESTORE_EXP, percent, fee, Config.CH_EXPREG_FEE_RATIO, getClanHall().getFunction(ClanHall.FUNC_RESTORE_EXP) == null))
									{
										html.setFile("data/html/clanHallManager/low_adena.htm");
										sendHtmlMessage(player, html);
									}
									else
									{
										revalidateDeco(player);
									}
									sendHtmlMessage(player, html);
								}
								return;
							}
						}
						NpcHtmlMessage html = new NpcHtmlMessage(1);
						html.setFile("data/html/clanHallManager/edit_recovery.htm");
						String hp_grade0 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 20\">20%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 40\">40%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 220\">220%</a>]";
						String hp_grade1 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 40\">40%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 100\">100%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 160\">160%</a>]";
						String hp_grade2 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 80\">80%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 140\">140%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 200\">200%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 260\">260%</a>]";
						String hp_grade3 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 80\">80%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 120\">120%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 180\">180%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 240\">240%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 300\">300%</a>]";
						String exp_grade0 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 10\">10%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>]";
						String exp_grade1 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 30\">30%</a>]";
						String exp_grade2 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 40\">40%</a>]";
						String exp_grade3 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 35\">35%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 50\">50%</a>]";
						String mp_grade0 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 10\">10%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 25\">25%</a>]";
						String mp_grade1 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 25\">25%</a>]";
						String mp_grade2 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 30\">30%</a>]";
						String mp_grade3 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 30\">30%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 40\">40%</a>]";
						if (getClanHall().getFunction(ClanHall.FUNC_RESTORE_HP) != null)
						{
							html.replace("%hp_recovery%", String.valueOf(getClanHall().getFunction(ClanHall.FUNC_RESTORE_HP).getLvl()) + "%</font> (<font color=\"FFAABB\">" + String.valueOf(getClanHall().getFunction(ClanHall.FUNC_RESTORE_HP).getLease()) + "(</font>Adena /" + String.valueOf(Config.CH_HPREG_FEE_RATIO / 1000 / 60 / 60 / 24) + " Days)");
							html.replace("%hp_period%", "Date of collecting rent" + format.format(getClanHall().getFunction(ClanHall.FUNC_RESTORE_HP).getEndTime()));
							int grade = getClanHall().getGrade();
							switch (grade)
							{
								case 0:
									html.replace("%change_hp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery hp_cancel\">Cancel</a>]" + hp_grade0);
									break;
								case 1:
									html.replace("%change_hp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery hp_cancel\">Cancel</a>]" + hp_grade1);
									break;
								case 2:
									html.replace("%change_hp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery hp_cancel\">Cancel/a>]" + hp_grade2);
									break;
								case 3:
									html.replace("%change_hp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery hp_cancel\">Cancel</a>]" + hp_grade3);
									break;
							}
						}
						else
						{
							html.replace("%hp_recovery%", "Suspended");
							html.replace("%hp_period%", "Suspended");
							int grade = getClanHall().getGrade();
							switch (grade)
							{
								case 0:
									html.replace("%change_hp%", hp_grade0);
									break;
								case 1:
									html.replace("%change_hp%", hp_grade1);
									break;
								case 2:
									html.replace("%change_hp%", hp_grade2);
									break;
								case 3:
									html.replace("%change_hp%", hp_grade3);
									break;
							}
						}
						if (getClanHall().getFunction(ClanHall.FUNC_RESTORE_EXP) != null)
						{
							html.replace("%exp_recovery%", String.valueOf(getClanHall().getFunction(ClanHall.FUNC_RESTORE_EXP).getLvl()) + "%</font> (<font color=\"FFAABB\">" + String.valueOf(getClanHall().getFunction(ClanHall.FUNC_RESTORE_EXP).getLease()) + "(</font>Adena /" + String.valueOf(Config.CH_EXPREG_FEE_RATIO / 1000 / 60 / 60 / 24) + " Days)");
							html.replace("%exp_period%", "Date of collecting rent " + format.format(getClanHall().getFunction(ClanHall.FUNC_RESTORE_EXP).getEndTime()));
							int grade = getClanHall().getGrade();
							switch (grade)
							{
								case 0:
									html.replace("%change_exp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery exp_cancel\">Cancel</a>]" + exp_grade0);
									break;
								case 1:
									html.replace("%change_exp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery exp_cancel\">Cancel</a>]" + exp_grade1);
									break;
								case 2:
									html.replace("%change_exp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery exp_cancel\">Cancel</a>]" + exp_grade2);
									break;
								case 3:
									html.replace("%change_exp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery exp_cancel\">Cancel</a>]" + exp_grade3);
									break;
							}
						}
						else
						{
							html.replace("%exp_recovery%", "Suspended");
							html.replace("%exp_period%", "Suspended");
							int grade = getClanHall().getGrade();
							switch (grade)
							{
								case 0:
									html.replace("%change_exp%", exp_grade0);
									break;
								case 1:
									html.replace("%change_exp%", exp_grade1);
									break;
								case 2:
									html.replace("%change_exp%", exp_grade2);
									break;
								case 3:
									html.replace("%change_exp%", exp_grade3);
									break;
							}
						}
						if (getClanHall().getFunction(ClanHall.FUNC_RESTORE_MP) != null)
						{
							html.replace("%mp_recovery%", String.valueOf(getClanHall().getFunction(ClanHall.FUNC_RESTORE_MP).getLvl()) + "%</font> (<font color=\"FFAABB\">" + String.valueOf(getClanHall().getFunction(ClanHall.FUNC_RESTORE_MP).getLease()) + "(</font>Adena /" + String.valueOf(Config.CH_MPREG_FEE_RATIO / 1000 / 60 / 60 / 24) + " Days)");
							html.replace("%mp_period%", "Date of collecting rent " + format.format(getClanHall().getFunction(ClanHall.FUNC_RESTORE_MP).getEndTime()));
							int grade = getClanHall().getGrade();
							switch (grade)
							{
								case 0:
									html.replace("%change_mp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery mp_cancel\">Cancel</a>]" + mp_grade0);
									break;
								case 1:
									html.replace("%change_mp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery mp_cancel\">Cancel</a>]" + mp_grade1);
									break;
								case 2:
									html.replace("%change_mp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery mp_cancel\">Cancel</a>]" + mp_grade2);
									break;
								case 3:
									html.replace("%change_mp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery mp_cancel\">Cancel</a>]" + mp_grade3);
									break;
							}
						}
						else
						{
							html.replace("%mp_recovery%", "Suspended");
							html.replace("%mp_period%", "Suspended");
							int grade = getClanHall().getGrade();
							switch (grade)
							{
								case 0:
									html.replace("%change_mp%", mp_grade0);
									break;
								case 1:
									html.replace("%change_mp%", mp_grade1);
									break;
								case 2:
									html.replace("%change_mp%", mp_grade2);
									break;
								case 3:
									html.replace("%change_mp%", mp_grade3);
									break;
							}
						}
						sendHtmlMessage(player, html);
					}
					else if (val.equalsIgnoreCase("other"))
					{
						if (st.countTokens() >= 1)
						{
							if (getClanHall().getOwnerId() == 0)
							{
								player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_NO_RIGHTS_TO_CONTROLL_HALL));
								return;
							}
							val = st.nextToken();
							if (val.equalsIgnoreCase("item_cancel"))
							{
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile("data/html/clanHallManager/functions-cancel.htm");
								html.replace("%apply%", "other item 0");
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("tele_cancel"))
							{
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile("data/html/clanHallManager/functions-cancel.htm");
								html.replace("%apply%", "other tele 0");
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("support_cancel"))
							{
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile("data/html/clanHallManager/functions-cancel.htm");
								html.replace("%apply%", "other support 0");
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("edit_item"))
							{
								val = st.nextToken();
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile("data/html/clanHallManager/functions-apply.htm");
								html.replace("%name%", "Magical equipment (The possibility of production)");
								int stage = Integer.valueOf(val);
								int cost;
								switch (stage)
								{
									case 1:
										cost = Config.CH_ITEM1_FEE;
										break;
									case 2:
										cost = Config.CH_ITEM2_FEE;
										break;
									default:
										cost = Config.CH_ITEM3_FEE;
										break;
								}
								html.replace("%cost%", String.valueOf(cost) + "(</font>Adena /" + String.valueOf(Config.CH_ITEM_FEE_RATIO / 1000 / 60 / 60 / 24) + " Days</font>)");
								html.replace("%use%", "Allows for special items, depending on the level.");
								html.replace("%apply%", "other item " + String.valueOf(stage));
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("edit_support"))
							{
								val = st.nextToken();
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile("data/html/clanHallManager/functions-apply.htm");
								html.replace("%name%", "Standards (Magic)");
								int stage = Integer.valueOf(val);
								int cost;
								switch (stage)
								{
									case 1:
										cost = Config.CH_SUPPORT1_FEE;
										break;
									case 2:
										cost = Config.CH_SUPPORT2_FEE;
										break;
									case 3:
										cost = Config.CH_SUPPORT3_FEE;
										break;
									case 4:
										cost = Config.CH_SUPPORT4_FEE;
										break;
									case 5:
										cost = Config.CH_SUPPORT5_FEE;
										break;
									case 6:
										cost = Config.CH_SUPPORT6_FEE;
										break;
									case 7:
										cost = Config.CH_SUPPORT7_FEE;
										break;
									default:
										cost = Config.CH_SUPPORT8_FEE;
										break;
								}
								html.replace("%cost%", String.valueOf(cost) + "(</font>Adena /" + String.valueOf(Config.CH_SUPPORT_FEE_RATIO / 1000 / 60 / 60 / 24) + " Days</font>)");
								html.replace("%use%", "Allows you to use magic.");
								html.replace("%apply%", "other support " + String.valueOf(stage));
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("edit_tele"))
							{
								val = st.nextToken();
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile("data/html/clanHallManager/functions-apply.htm");
								html.replace("%name%", "Mirror (teleportation)");
								int stage = Integer.valueOf(val);
								int cost;
								switch (stage)
								{
									case 1:
										cost = Config.CH_TELE1_FEE;
										break;
									default:
										cost = Config.CH_TELE2_FEE;
										break;
								}
								html.replace("%cost%", String.valueOf(cost) + "(</font>Adena /" + String.valueOf(Config.CH_TELE_FEE_RATIO / 1000 / 60 / 60 / 24) + " Days</font>)");
								html.replace("%use%", "Teleports clan members in the zone <font color=\"00FFFF\">" + String.valueOf(stage) + "- the level </font>");
								html.replace("%apply%", "other tele " + String.valueOf(stage));
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("item"))
							{
								if (st.countTokens() >= 1)
								{
									if (getClanHall().getOwnerId() == 0)
									{
										player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_NO_RIGHTS_TO_CONTROLL_HALL));
										return;
									}
									val = st.nextToken();
									NpcHtmlMessage html = new NpcHtmlMessage(1);
									html.setFile("data/html/clanHallManager/functions-apply_confirmed.htm");
									if (getClanHall().getFunction(ClanHall.FUNC_ITEM_CREATE) != null)
										if (getClanHall().getFunction(ClanHall.FUNC_ITEM_CREATE).getLvl() == Integer.valueOf(val))
										{
											html.setFile("data/html/clanHallManager/functions-used.htm");
											html.replace("%val%", "The Level Of " + String.valueOf(val));
											sendHtmlMessage(player, html);
											return;
										}
									int fee;
									int lvl = Integer.valueOf(val);
									switch (lvl)
									{
										case 0:
											fee = 0;
											html.setFile("data/html/clanHallManager/functions-cancel_confirmed.htm");
											break;
										case 1:
											fee = Config.CH_ITEM1_FEE;
											break;
										case 2:
											fee = Config.CH_ITEM2_FEE;
											break;
										default:
											fee = Config.CH_ITEM3_FEE;
											break;
									}
									if (!getClanHall().updateFunctions(player, ClanHall.FUNC_ITEM_CREATE, lvl, fee, Config.CH_ITEM_FEE_RATIO, getClanHall().getFunction(ClanHall.FUNC_ITEM_CREATE) == null))
									{
										html.setFile("data/html/clanHallManager/low_adena.htm");
										sendHtmlMessage(player, html);
									}
									else
									{
										revalidateDeco(player);
									}
									sendHtmlMessage(player, html);
								}
								return;
							}
							else if (val.equalsIgnoreCase("tele"))
							{
								if (st.countTokens() >= 1)
								{
									int fee;
									val = st.nextToken();
									NpcHtmlMessage html = new NpcHtmlMessage(1);
									html.setFile("data/html/clanHallManager/functions-apply_confirmed.htm");
									if (getClanHall().getFunction(ClanHall.FUNC_TELEPORT) != null)
										if (getClanHall().getFunction(ClanHall.FUNC_TELEPORT).getLvl() == Integer.valueOf(val))
										{
											html.setFile("data/html/clanHallManager/functions-used.htm");
											html.replace("%val%", "The Level Of " + String.valueOf(val));
											sendHtmlMessage(player, html);
											return;
										}
									int lvl = Integer.valueOf(val);
									switch (lvl)
									{
										case 0:
											fee = 0;
											html.setFile("data/html/clanHallManager/functions-cancel_confirmed.htm");
											break;
										case 1:
											fee = Config.CH_TELE1_FEE;
											break;
										default:
											fee = Config.CH_TELE2_FEE;
											break;
									}
									if (!getClanHall().updateFunctions(player, ClanHall.FUNC_TELEPORT, lvl, fee, Config.CH_TELE_FEE_RATIO, getClanHall().getFunction(ClanHall.FUNC_TELEPORT) == null))
									{
										html.setFile("data/html/clanHallManager/low_adena.htm");
										sendHtmlMessage(player, html);
									}
									else
									{
										revalidateDeco(player);
									}
									sendHtmlMessage(player, html);
								}
								return;
							}
							else if (val.equalsIgnoreCase("support"))
							{
								if (st.countTokens() >= 1)
								{
									int fee;
									val = st.nextToken();
									NpcHtmlMessage html = new NpcHtmlMessage(1);
									html.setFile("data/html/clanHallManager/functions-apply_confirmed.htm");
									if (getClanHall().getFunction(ClanHall.FUNC_SUPPORT) != null)
										if (getClanHall().getFunction(ClanHall.FUNC_SUPPORT).getLvl() == Integer.valueOf(val))
										{
											html.setFile("data/html/clanHallManager/functions-used.htm");
											html.replace("%val%", "The Level Of " + String.valueOf(val));
											sendHtmlMessage(player, html);
											return;
										}
									int lvl = Integer.valueOf(val);
									switch (lvl)
									{
										case 0:
											fee = 0;
											html.setFile("data/html/clanHallManager/functions-cancel_confirmed.htm");
											break;
										case 1:
											fee = Config.CH_SUPPORT1_FEE;
											break;
										case 2:
											fee = Config.CH_SUPPORT2_FEE;
											break;
										case 3:
											fee = Config.CH_SUPPORT3_FEE;
											break;
										case 4:
											fee = Config.CH_SUPPORT4_FEE;
											break;
										case 5:
											fee = Config.CH_SUPPORT5_FEE;
											break;
										case 6:
											fee = Config.CH_SUPPORT6_FEE;
											break;
										case 7:
											fee = Config.CH_SUPPORT7_FEE;
											break;
										default:
											fee = Config.CH_SUPPORT8_FEE;
											break;
									}
									if (!getClanHall().updateFunctions(player, ClanHall.FUNC_SUPPORT, lvl, fee, Config.CH_SUPPORT_FEE_RATIO, getClanHall().getFunction(ClanHall.FUNC_SUPPORT) == null))
									{
										html.setFile("data/html/clanHallManager/low_adena.htm");
										sendHtmlMessage(player, html);
									}
									else
									{
										revalidateDeco(player);
									}
									sendHtmlMessage(player, html);
								}
								return;
							}
						}
						NpcHtmlMessage html = new NpcHtmlMessage(1);
						html.setFile("data/html/clanHallManager/edit_other.htm");
						String tele = "[<a action=\"bypass -h npc_%objectId%_manage other edit_tele 1\">1st Level</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_tele 2\">2nd Level</a>]";
						String support_grade0 = "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 1\">1st Level</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 2\">2nd Level</a>]";
						String support_grade1 = "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 1\">1st Level</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 2\">2nd Level</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 4\"4th Level</a>]";
						String support_grade2 = "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 3\">3rd Level</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 4\">4th Level</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 5\">5th Level</a>]";
						String support_grade3 = "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 3\">3rd Level</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 5\">5th Level</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 7\">7th Level</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 8\">8th Level</a>]";
						String item = "[<a action=\"bypass -h npc_%objectId%_manage other edit_item 1\">MP. 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_item 2\">MP. 2</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_item 3\">MP. 3</a>]";
						if (getClanHall().getFunction(ClanHall.FUNC_TELEPORT) != null)
						{
							html.replace("%tele%", "The Level Of " + String.valueOf(getClanHall().getFunction(ClanHall.FUNC_TELEPORT).getLvl()) + "</font> (<font color=\"FFAABB\">" + String.valueOf(getClanHall().getFunction(ClanHall.FUNC_TELEPORT).getLease()) + "(</font>Lease /" + String.valueOf(Config.CH_TELE_FEE_RATIO / 1000 / 60 / 60 / 24) + " Days)");
							html.replace("%tele_period%", "Date of collecting rent " + format.format(getClanHall().getFunction(ClanHall.FUNC_TELEPORT).getEndTime()));
							html.replace("%change_tele%", "[<a action=\"bypass -h npc_%objectId%_manage other tele_cancel\">Cancel</a>]" + tele);
						}
						else
						{
							html.replace("%tele%", "Suspended");
							html.replace("%tele_period%", "Suspended");
							html.replace("%change_tele%", tele);
						}
						if (getClanHall().getFunction(ClanHall.FUNC_SUPPORT) != null)
						{
							html.replace("%support%", "The Level Of " + String.valueOf(getClanHall().getFunction(ClanHall.FUNC_SUPPORT).getLvl()) + "</font> (<font color=\"FFAABB\">" + String.valueOf(getClanHall().getFunction(ClanHall.FUNC_SUPPORT).getLease()) + "(</font>Lease /" + String.valueOf(Config.CH_SUPPORT_FEE_RATIO / 1000 / 60 / 60 / 24) + " Days)");
							html.replace("%support_period%", "Date of collecting rent " + format.format(getClanHall().getFunction(ClanHall.FUNC_SUPPORT).getEndTime()));
							int grade = getClanHall().getGrade();
							switch (grade)
							{
								case 0:
									html.replace("%change_support%", "[<a action=\"bypass -h npc_%objectId%_manage other support_cancel\">Cancel</a>]" + support_grade0);
									break;
								case 1:
									html.replace("%change_support%", "[<a action=\"bypass -h npc_%objectId%_manage other support_cancel\">Cancel</a>]" + support_grade1);
									break;
								case 2:
									html.replace("%change_support%", "[<a action=\"bypass -h npc_%objectId%_manage other support_cancel\">Cancel</a>]" + support_grade2);
									break;
								case 3:
									html.replace("%change_support%", "[<a action=\"bypass -h npc_%objectId%_manage other support_cancel\">Cancel</a>]" + support_grade3);
									break;
							}
						}
						else
						{
							html.replace("%support%", "Suspended");
							html.replace("%support_period%", "Suspended");
							int grade = getClanHall().getGrade();
							switch (grade)
							{
								case 0:
									html.replace("%change_support%", support_grade0);
									break;
								case 1:
									html.replace("%change_support%", support_grade1);
									break;
								case 2:
									html.replace("%change_support%", support_grade2);
									break;
								case 3:
									html.replace("%change_support%", support_grade3);
									break;
							}
						}
						if (getClanHall().getFunction(ClanHall.FUNC_ITEM_CREATE) != null)
						{
							html.replace("%item%", "The Level Of " + String.valueOf(getClanHall().getFunction(ClanHall.FUNC_ITEM_CREATE).getLvl()) + "</font> (<font color=\"FFAABB\">" + String.valueOf(getClanHall().getFunction(ClanHall.FUNC_ITEM_CREATE).getLease()) + "(</font>Lease /" + String.valueOf(Config.CH_ITEM_FEE_RATIO / 1000 / 60 / 60 / 24) + " Days)");
							html.replace("%item_period%", "Date of collecting rent " + format.format(getClanHall().getFunction(ClanHall.FUNC_ITEM_CREATE).getEndTime()));
							html.replace("%change_item%", "[<a action=\"bypass -h npc_%objectId%_manage other item_cancel\">Cancel</a>]" + item);
						}
						else
						{
							html.replace("%item%", "Suspended");
							html.replace("%item_period%", "Suspended");
							html.replace("%change_item%", item);
						}
						sendHtmlMessage(player, html);
					}
					else if (val.equalsIgnoreCase("deco"))
					{
						if (st.countTokens() >= 1)
						{
							if (getClanHall().getOwnerId() == 0)
							{
								player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_NO_RIGHTS_TO_CONTROLL_HALL));
								return;
							}
							val = st.nextToken();
							if (val.equalsIgnoreCase("curtains_cancel"))
							{
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile("data/html/clanHallManager/functions-cancel.htm");
								html.replace("%apply%", "deco curtains 0");
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("fixtures_cancel"))
							{
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile("data/html/clanHallManager/functions-cancel.htm");
								html.replace("%apply%", "deco fixtures 0");
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("edit_curtains"))
							{
								val = st.nextToken();
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile("data/html/clanHallManager/functions-apply.htm");
								html.replace("%name%", "Was No Fun (Scenery)");
								int stage = Integer.valueOf(val);
								int cost;
								switch (stage)
								{
									case 1:
										cost = Config.CH_CURTAIN1_FEE;
										break;
									default:
										cost = Config.CH_CURTAIN2_FEE;
										break;
								}
								html.replace("%cost%", String.valueOf(cost) + "</font>Adena /" + String.valueOf(Config.CH_CURTAIN_FEE_RATIO / 1000 / 60 / 60 / 24) + " days</font>)");
								html.replace("%use%", "Allows you to decorate the Hall Clan.");
								html.replace("%apply%", "deco curtains " + String.valueOf(stage));
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("edit_fixtures"))
							{
								val = st.nextToken();
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile("data/html/clanHallManager/functions-apply.htm");
								html.replace("%name%", "Foya (Scenery)");
								int stage = Integer.valueOf(val);
								int cost;
								switch (stage)
								{
									case 1:
										cost = Config.CH_FRONT1_FEE;
										break;
									default:
										cost = Config.CH_FRONT2_FEE;
										break;
								}
								html.replace("%cost%", String.valueOf(cost) + "(</font>Adena /" + String.valueOf(Config.CH_FRONT_FEE_RATIO / 1000 / 60 / 60 / 24) + " days</font>)");
								html.replace("%use%", "Allows you to decorate the Hall Clan.");
								html.replace("%apply%", "deco fixtures " + String.valueOf(stage));
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("curtains"))
							{
								if (st.countTokens() >= 1)
								{
									int fee;
									val = st.nextToken();
									NpcHtmlMessage html = new NpcHtmlMessage(1);
									html.setFile("data/html/clanHallManager/functions-apply_confirmed.htm");
									if (getClanHall().getFunction(ClanHall.FUNC_DECO_CURTAINS) != null)
										if (getClanHall().getFunction(ClanHall.FUNC_DECO_CURTAINS).getLvl() == Integer.valueOf(val))
										{
											html.setFile("data/html/clanHallManager/functions-used.htm");
											html.replace("%val%", "The Level Of " + String.valueOf(val));
											sendHtmlMessage(player, html);
											return;
										}
									int lvl = Integer.valueOf(val);
									switch (lvl)
									{
										case 0:
											fee = 0;
											html.setFile("data/html/clanHallManager/functions-cancel_confirmed.htm");
											break;
										case 1:
											fee = Config.CH_CURTAIN1_FEE;
											break;
										default:
											fee = Config.CH_CURTAIN2_FEE;
											break;
									}
									if (!getClanHall().updateFunctions(player, ClanHall.FUNC_DECO_CURTAINS, lvl, fee, Config.CH_CURTAIN_FEE_RATIO, getClanHall().getFunction(ClanHall.FUNC_DECO_CURTAINS) == null))
									{
										html.setFile("data/html/clanHallManager/low_adena.htm");
										sendHtmlMessage(player, html);
									}
									else
									{
										revalidateDeco(player);
									}
									sendHtmlMessage(player, html);
								}
								return;
							}
							else if (val.equalsIgnoreCase("fixtures"))
							{
								if (st.countTokens() >= 1)
								{
									int fee;
									val = st.nextToken();
									NpcHtmlMessage html = new NpcHtmlMessage(1);
									html.setFile("data/html/clanHallManager/functions-apply_confirmed.htm");
									if (getClanHall().getFunction(ClanHall.FUNC_DECO_FRONTPLATEFORM) != null)
										if (getClanHall().getFunction(ClanHall.FUNC_DECO_FRONTPLATEFORM).getLvl() == Integer.valueOf(val))
										{
											html.setFile("data/html/clanHallManager/functions-used.htm");
											html.replace("%val%", "The Level Of " + String.valueOf(val));
											sendHtmlMessage(player, html);
											return;
										}
									int lvl = Integer.valueOf(val);
									switch (lvl)
									{
										case 0:
											fee = 0;
											html.setFile("data/html/clanHallManager/functions-cancel_confirmed.htm");
											break;
										case 1:
											fee = Config.CH_FRONT1_FEE;
											break;
										default:
											fee = Config.CH_FRONT2_FEE;
											break;
									}
									if (!getClanHall().updateFunctions(player, ClanHall.FUNC_DECO_FRONTPLATEFORM, lvl, fee, Config.CH_FRONT_FEE_RATIO, getClanHall().getFunction(ClanHall.FUNC_DECO_FRONTPLATEFORM) == null))
									{
										html.setFile("data/html/clanHallManager/low_adena.htm");
										sendHtmlMessage(player, html);
									}
									else
									{
										revalidateDeco(player);
									}
									sendHtmlMessage(player, html);
								}
								return;
							}
						}
						NpcHtmlMessage html = new NpcHtmlMessage(1);
						html.setFile("data/html/clanHallManager/deco.htm");
						String curtains = "[<a action=\"bypass -h npc_%objectId%_manage deco edit_curtains 1\">1st Stage</a>][<a action=\"bypass -h npc_%objectId%_manage deco edit_curtains 2\">2nd Stage</a>]";
						String fixtures = "[<a action=\"bypass -h npc_%objectId%_manage deco edit_fixtures 1\">1st Stage</a>][<a action=\"bypass -h npc_%objectId%_manage deco edit_fixtures 2\">2nd Stage</a>]";
						if (getClanHall().getFunction(ClanHall.FUNC_DECO_CURTAINS) != null)
						{
							html.replace("%curtain%", "The Level Of " + String.valueOf(getClanHall().getFunction(ClanHall.FUNC_DECO_CURTAINS).getLvl()) + "</font> (<font color=\"FFAABB\">" + String.valueOf(getClanHall().getFunction(ClanHall.FUNC_DECO_CURTAINS).getLease()) + "(</font>Adena /" + String.valueOf(Config.CH_CURTAIN_FEE_RATIO / 1000 / 60 / 60 / 24) + " Days)");
							html.replace("%curtain_period%", "Date of collecting rent " + format.format(getClanHall().getFunction(ClanHall.FUNC_DECO_CURTAINS).getEndTime()));
							html.replace("%change_curtain%", "[<a action=\"bypass -h npc_%objectId%_manage deco curtains_cancel\">Cancel</a>]" + curtains);
						}
						else
						{
							html.replace("%curtain%", "Suspended");
							html.replace("%curtain_period%", "Suspended");
							html.replace("%change_curtain%", curtains);
						}
						if (getClanHall().getFunction(ClanHall.FUNC_DECO_FRONTPLATEFORM) != null)
						{
							html.replace("%fixture%", "The Level Of " + String.valueOf(getClanHall().getFunction(ClanHall.FUNC_DECO_FRONTPLATEFORM).getLvl()) + "</font> (<font color=\"FFAABB\">" + String.valueOf(getClanHall().getFunction(ClanHall.FUNC_DECO_FRONTPLATEFORM).getLease()) + "(</font>Adena /" + String.valueOf(Config.CH_FRONT_FEE_RATIO / 1000 / 60 / 60 / 24) + " Days)");
							html.replace("%fixture_period%", "Date of collecting rent " + format.format(getClanHall().getFunction(ClanHall.FUNC_DECO_FRONTPLATEFORM).getEndTime()));
							html.replace("%change_fixture%", "[<a action=\"bypass -h npc_%objectId%_manage deco fixtures_cancel\">Cancel</a>]" + fixtures);
						}
						else
						{
							html.replace("%fixture%", "Suspended");
							html.replace("%fixture_period%", "Suspended");
							html.replace("%change_fixture%", fixtures);
						}
						sendHtmlMessage(player, html);
					}
					else if (val.equalsIgnoreCase("back"))
					{
						showMessageWindow(player);
					}
					else
					{
						NpcHtmlMessage html = new NpcHtmlMessage(1);
						html.setFile("data/html/clanHallManager/manage.htm");
						sendHtmlMessage(player, html);
					}
				}
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile("data/html/clanHallManager/not_authorized.htm");
					sendHtmlMessage(player, html);
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("support"))
			{
				if (player.isCursedWeaponEquipped())
				{
					player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_CURSED_WEAPON_NOT_ALLOW));
					return;
				}

				magicSupport(player, val, st);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("list_back"))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile("data/html/clanHallManager/chamberlain.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%npcname%", getName());
				sendHtmlMessage(player, html);
			}
			else if (actualCommand.equalsIgnoreCase("support_back"))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				if (getClanHall().getFunction(ClanHall.FUNC_SUPPORT).getLvl() == 0)
					return;
				html.setFile("data/html/clanHallManager/support" + getClanHall().getFunction(ClanHall.FUNC_SUPPORT).getLvl() + ".htm");
				html.replace("%mp%", String.valueOf((int) getStatus().getCurrentMp()));
				sendHtmlMessage(player, html);
			}
			else if (actualCommand.equalsIgnoreCase("goto"))
			{
				int whereTo = Integer.parseInt(val);
				doTeleport(player, whereTo);
				return;
			}
		}
		super.onBypassFeedback(player, command);
	}

	private void sendHtmlMessage(L2PcInstance player, NpcHtmlMessage html)
	{
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getNpcId()));
		player.sendPacket(html);
	}

	private void showMessageWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/clanHallManager/chamberlain-no.htm";

		int condition = validateCondition(player);
		if (condition == COND_OWNER)
		{
			filename = "data/html/clanHallManager/chamberlain.htm";
		}
		if (condition == COND_OWNER_FALSE)
		{
			filename = "data/html/clanHallManager/chamberlain-of.htm";
		}
		if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
		{
			filename = "data/html/clanHallManager/chamberlain-busy.htm";
		}

		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getNpcId()));
		player.sendPacket(html);
	}

	private void showVaultWindowWithdraw(L2PcInstance player)
	{
		if (player.isClanLeader() || (player.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE) == L2Clan.CP_CL_VIEW_WAREHOUSE)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			player.setActiveWarehouse(player.getClan().getWarehouse());
			player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.CLAN));
		}
		else
		{
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setFile("data/html/clanHallManager/not_authorized.htm");
			sendHtmlMessage(player, html);
		}
	}

	protected int validateCondition(L2PcInstance player)
	{
		if (getClanHall() == null)
			return COND_ALL_FALSE;
		if (getClanHall().isUnderSiege())
			return COND_BUSY_BECAUSE_OF_SIEGE;
		if (player.isGM())
			return COND_OWNER;
		if (player.getClan() != null)
		{
			if (getClanHall().getOwnerId() == player.getClanId())
				return COND_OWNER;

			return COND_OWNER_FALSE;
		}
		return COND_ALL_FALSE;
	}

}
