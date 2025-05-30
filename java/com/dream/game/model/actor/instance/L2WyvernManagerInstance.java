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

import com.dream.Config;
import com.dream.Message;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.datatables.xml.PetDataTable;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.model.entity.sevensigns.SevenSigns;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2WyvernManagerInstance extends L2CastleChamberlainInstance
{
	public L2WyvernManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
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
		if (command.startsWith("RideWyvern"))
		{
			if (!player.isClanLeader())
			{
				player.sendPacket(SystemMessageId.ONLY_THE_CLAN_LEADER_IS_ENABLED);
				return;
			}

			if (SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE) == SevenSigns.CABAL_DUSK && SevenSigns.getInstance().isSealValidationPeriod())
			{
				player.sendPacket(SystemMessageId.SEAL_OF_STRIFE_FORBIDS_SUMMONING);
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
	}

	private void showMessageWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/wyvernmanager/wyvernmanager-no.htm";

		int condition = validateCondition(player);
		if (condition > COND_ALL_FALSE)
			if (condition == COND_OWNER)
			{
				filename = "data/html/wyvernmanager/wyvernmanager.htm";
			}
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		html.replace("%count%", String.valueOf(Config.MANAGER_CRYSTAL_COUNT));
		player.sendPacket(html);
	}
}