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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import com.dream.Config;
import com.dream.Message;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.datatables.xml.PetDataTable;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.manager.ClanHallManager;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2Object;
import com.dream.game.model.entity.ClanHall;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2DoormenInstance extends L2NpcInstance
{
	private static final int COND_ALL_FALSE = 0;

	private static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	private static final int COND_CASTLE_OWNER = 2;
	private static final int COND_HALL_OWNER = 3;
	private static final int COND_FORT_OWNER = 4;

	private static boolean validatePrivileges(L2PcInstance player, int privilege)
	{
		if ((player.getClanPrivileges() & privilege) != privilege)
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return false;
		}
		return true;
	}

	private ClanHall _clanHall;

	private L2DoorInstance door;

	public L2DoormenInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
	}

	public final ClanHall getClanHall()
	{
		if (_clanHall == null)
		{
			_clanHall = ClanHallManager.getInstance().getNearbyClanHall(getX(), getY(), 500);
		}
		return _clanHall;
	}

	private Collection<L2DoorInstance> getKnownDoors(L2PcInstance player)
	{
		List<L2DoorInstance> _doors = new ArrayList<>();

		for (L2Object object : getKnownList().getKnownObjects().values())
			if (object instanceof L2DoorInstance)
			{
				door = null;
				door = (L2DoorInstance) object;

				if (door != null)
					if (door.getCastle() != null && door.getCastle().getOwnerId() == player.getClanId() || door.getFort() != null && door.getFort().getOwnerId() == player.getClanId())
					{
						_doors.add(door);
					}
			}
		return _doors;
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player))
			return;

		if (this != player.getTarget())
		{
			player.setTarget(this);

			player.sendPacket(new MyTargetSelected(getObjectId(), 0));

			player.sendPacket(new ValidateLocation(this));
		}
		else if (!canInteract(player))
		{
			player.getAI().setIntention(CtrlIntention.INTERACT, this);
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
		int condition = validateCondition(player);
		if (condition <= COND_ALL_FALSE)
			return;
		if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
			return;
		else if (condition == COND_CASTLE_OWNER || condition == COND_HALL_OWNER || condition == COND_FORT_OWNER)
			if (command.startsWith("Chat"))
			{
				showMessageWindow(player);
				return;
			}
			else if (command.startsWith("open_doors"))
			{
				if (condition == COND_HALL_OWNER)
				{
					getClanHall().openCloseDoors(true);
					player.sendPacket(new NpcHtmlMessage(getObjectId(), "<html><body>You have <font color=\"FF9955\">opened</font> the clan hall door.<br> Outsiders may enter the clan hall while the door is open. Please close it when you've finished your business.<br><center><button value=\"Close\" action=\"bypass -h npc_" + getObjectId() + "_close_doors\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center></body></html>"));
				}
				else if (condition == COND_CASTLE_OWNER)
				{
					if (!validatePrivileges(player, L2Clan.CP_CS_OPEN_DOOR))
						return;
					if (!Config.SIEGE_GATE_CONTROL && getCastle().getSiege().getIsInProgress())
					{
						player.sendPacket(SystemMessageId.GATES_NOT_OPENED_CLOSED_DURING_SIEGE);
						return;
					}
					StringTokenizer st = new StringTokenizer(command.substring(10), ", ");
					st.nextToken();

					while (st.hasMoreTokens())
					{
						getCastle().openDoor(player, Integer.parseInt(st.nextToken()));
					}
					return;
				}
				else if (condition == COND_FORT_OWNER)
				{
					StringTokenizer st = new StringTokenizer(command.substring(10), ", ");
					st.nextToken();

					while (st.hasMoreTokens())
					{
						getFort().openDoor(Integer.parseInt(st.nextToken()));
					}
					return;
				}
			}
			else if (command.startsWith("close_doors"))
			{
				if (condition == COND_HALL_OWNER)
				{
					getClanHall().openCloseDoors(false);
					player.sendPacket(new NpcHtmlMessage(getObjectId(), "<html><body>You have <font color=\"FF9955\">closed</font> the clan hall door.<br>Good day!<br><center><button value=\"To Beginning\" action=\"bypass -h npc_" + getObjectId() + "_Chat\" width=80 height=27 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center></body></html>"));
				}
				else if (condition == COND_CASTLE_OWNER)
				{
					if (!validatePrivileges(player, L2Clan.CP_CS_OPEN_DOOR))
						return;
					if (!Config.SIEGE_GATE_CONTROL && getCastle().getSiege().getIsInProgress())
					{
						player.sendPacket(SystemMessageId.GATES_NOT_OPENED_CLOSED_DURING_SIEGE);
						return;
					}
					StringTokenizer st = new StringTokenizer(command.substring(11), ", ");
					st.nextToken();

					while (st.hasMoreTokens())
					{
						getCastle().closeDoor(player, Integer.parseInt(st.nextToken()));
					}
					return;
				}
				else if (condition == COND_FORT_OWNER)
				{
					StringTokenizer st = new StringTokenizer(command.substring(11), ", ");
					st.nextToken();

					while (st.hasMoreTokens())
					{
						getFort().closeDoor(Integer.parseInt(st.nextToken()));
					}
					return;
				}
			}
			else if (command.startsWith("RideWyvern"))
			{
				if (!player.isClanLeader())
				{
					player.sendPacket(SystemMessageId.ONLY_THE_CLAN_LEADER_IS_ENABLED);
					return;
				}

				int petItemId = 0;
				L2ItemInstance petItem = null;

				if (player.getPet() == null)
				{
					if (player.isMounted())
					{
						petItem = player.getInventory().getItemByObjectId(player.getMountObjectID());
						if (petItem != null)
						{
							petItemId = petItem.getItemId();
						}
					}
				}
				else
				{
					petItemId = player.getPet().getControlItemId();
				}

				if (petItemId == 0 || !player.isMounted() || !PetDataTable.isStrider(PetDataTable.getPetIdByItemId(petItemId)))
				{
					player.sendPacket(SystemMessageId.YOU_MAY_ONLY_RIDE_WYVERN_WHILE_RIDING_STRIDER);
					return;
				}
				else if (player.isMounted() && PetDataTable.isStrider(PetDataTable.getPetIdByItemId(petItemId)) && petItem != null && petItem.getEnchantLevel() < 55)
				{
					player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_WRONG_STRIDER_LEVEL));
					return;
				}

				if (player.getInventory().getItemByItemId(1460) != null && player.getInventory().getItemByItemId(1460).getCount() >= Config.MANAGER_CRYSTAL_COUNT)
				{
					if (!player.disarmWeapons())
						return;

					if (player.isMounted())
					{
						player.dismount();
					}

					if (player.getPet() != null)
					{
						player.getPet().unSummon(player);
					}

					if (player.mount(12621, 0, true))
					{
						L2ItemInstance cryB = player.getInventory().getItemByItemId(1460);
						player.getInventory().destroyItemByItemId("Wyvern", 1460, Config.MANAGER_CRYSTAL_COUNT, player, player.getTarget());
						InventoryUpdate iu = new InventoryUpdate();
						iu.addModifiedItem(cryB);
						player.sendPacket(iu);
						player.addSkill(SkillTable.getInstance().getInfo(4289, 1));
					}
				}
				else
				{
					player.sendMessage(String.format(Message.getMessage(player, Message.MessageId.MSG_NEED_ITEM), Config.MANAGER_CRYSTAL_COUNT + " Crystals: B Grade."));
				}
			}
			else if (condition == COND_FORT_OWNER && command.startsWith("open_near_doors"))
			{
				for (L2DoorInstance door : getKnownDoors(player))
				{
					door.openMe();
				}
			}
			else if (condition == COND_FORT_OWNER && command.startsWith("close_near_doors"))
			{
				for (L2DoorInstance door : getKnownDoors(player))
				{
					door.closeMe();
				}
			}

		super.onBypassFeedback(player, command);
	}

	public void showMessageWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/doormen/" + getTemplate().getNpcId() + "-no.htm";

		int condition = validateCondition(player);
		if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
		{
			filename = "data/html/doormen/" + getTemplate().getNpcId() + "-busy.htm";
		}
		else if (condition == COND_CASTLE_OWNER || condition == COND_FORT_OWNER)
		{
			filename = "data/html/doormen/" + getTemplate().getNpcId() + ".htm";
		}

		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		String str;
		if (getClanHall() != null)
		{
			if (condition == COND_HALL_OWNER)
			{
				str = "<html><body>Hello!<br><font color=\"55FFFF\">" + getName() + "</font>, I am honored to serve your clan.<br>How may i assist you?<br>";
				str += "<center><br><button value=\"Open Door\" action=\"bypass -h npc_%objectId%_open_doors\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"><br>";
				str += "<button value=\"Close Door" + "\" action=\"bypass -h npc_%objectId%_close_doors\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"><br>";
				if (getClanHall().getId() >= 36 && getClanHall().getId() <= 41)
				{
					str += "<button value=\"Wyvern\" action=\"bypass -h npc_%objectId%_Link clanHallManager/wyvern.htm \" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center></body></html>";
				}
				else
				{
					str += "</center></body></html>";
				}
			}
			else
			{
				final L2Clan owner = ClanTable.getInstance().getClan(getClanHall().getOwnerId());
				if (owner != null && owner.getLeader() != null)
				{
					str = "<html><body>Hello there!<br>This clan hall is owned by <font color=\"55FFFF\">" + owner.getLeader().getName() + " who is the Lord of the ";
					str += owner.getName() + "</font> clan.<br>";
					str += "I am sorry, but only the clan members who belong to the <font color=\"55FFFF\">" + owner.getName() + "</font> clan can enter the clan hall.</body></html>";
				}
				else
				{
					int ClanHallID = _clanHall.getId();
					if (ClanHallID == 21 || ClanHallID == 34 || ClanHallID == 35 || ClanHallID == 62 || ClanHallID == 63 || ClanHallID == 64)
					{
						str = "<html><body>Clan Hall <font color=\"LEVEL\">" + getClanHall().getName() + "</font> has no owner.<br> To take over the Hall clan, you should grab it.</body></html>";
					}
					else
					{
						str = "<html><body>Clan Hall <font color=\"LEVEL\">" + getClanHall().getName() + "</font> has no owner.<br> for purchase go to auction.</body></html>";
					}
				}
			}
			html.setHtml(str);
		}
		else
		{
			html.setFile(filename);
		}

		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);

		filename = null;
		html = null;
		str = null;
	}

	private int validateCondition(L2PcInstance player)
	{
		if (player.getClan() != null)
		{
			int clanId = player.getClanId();
			if (getClanHall() != null)
				if (getClanHall().getOwnerId() == clanId)
					return COND_HALL_OWNER;
			if (getCastle() != null && getCastle().getCastleId() > 0)
			{
				if (getCastle().getSiege().getIsInProgress())
					return COND_BUSY_BECAUSE_OF_SIEGE;
				if (getCastle().getOwnerId() == clanId)
					return COND_CASTLE_OWNER;
			}
			if (getFort() != null && getFort().getFortId() > 0)
			{
				if (getFort().getSiege().getIsInProgress())
					return COND_BUSY_BECAUSE_OF_SIEGE;
				if (getFort().getOwnerId() == clanId)
					return COND_FORT_OWNER;
			}
			if (player.isGM())
				return COND_HALL_OWNER;
		}
		return COND_ALL_FALSE;
	}
}