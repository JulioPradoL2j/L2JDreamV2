package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.Shutdown;
import com.dream.game.datatables.sql.HennaTreeTable;
import com.dream.game.datatables.xml.HennaTable;
import com.dream.game.model.actor.instance.L2HennaInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.templates.item.L2Henna;
import com.dream.game.util.Util;
import com.dream.util.StringUtil;

public class RequestHennaEquip extends L2GameClientPacket
{
	private static void showMessageErrorEquip(L2PcInstance activeChar)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		final StringBuilder strBuffer = StringUtil.startAppend(3500, "<html><title>Henna</title><body><center>");
		{
			strBuffer.append("<img src=\"L2UI_CH3.onscrmsg_pattern01_2\" width=300 height=32 align=left>");
			strBuffer.append("<font color=\"LEVEL\">%charname%</font> I am sorry but you can't <br>" + "equip your dyes right now!:<br>");
			strBuffer.append("<table width=300>");
			strBuffer.append("</table><img src=\"L2UI_CH3.onscrmsg_pattern01_2\" width=300 height=32 align=left>");
		}
		strBuffer.append("</center></body></html>");
		html.setHtml(strBuffer.toString());
		html.replace("%charname%", activeChar.getName());
		activeChar.sendPacket(html);
	}

	private static void showMessageErrorRestart(L2PcInstance activeChar)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		final StringBuilder strBuffer = StringUtil.startAppend(3500, "<html><title>Henna</title><body><center>");
		{
			strBuffer.append("<img src=\"L2UI_CH3.onscrmsg_pattern01_2\" width=300 height=32 align=left>");
			strBuffer.append("<font color=\"LEVEL\">%charname%</font> I am sorry but you can't <br>" + "equip when restarting / shutdown of the server!:<br>");
			strBuffer.append("<table width=300>");
			strBuffer.append("</table><img src=\"L2UI_CH3.onscrmsg_pattern01_2\" width=300 height=32 align=left>");
		}
		strBuffer.append("</center></body></html>");
		html.setHtml(strBuffer.toString());
		html.replace("%charname%", activeChar.getName());
		activeChar.sendPacket(html);
	}

	private int _symbolId;

	@Override
	protected void readImpl()
	{
		_symbolId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if (Shutdown.getCounterInstance() != null)
		{
			showMessageErrorRestart(activeChar);
			return;
		}

		L2Henna template = HennaTable.getInstance().getTemplate(_symbolId);

		if (template == null)
			return;

		L2HennaInstance henna = new L2HennaInstance(template);
		int _count = 0;

		boolean cheater = true;

		for (L2HennaInstance h : HennaTreeTable.getInstance().getAvailableHenna(activeChar.getClassId()))
			if (h.getSymbolId() == henna.getSymbolId())
			{
				cheater = false;
				break;
			}
		if (activeChar.isCastingNow() || activeChar.isFlying() || activeChar.isMounted() || activeChar.isMuted() || activeChar.isInCombat() || activeChar.getActiveEnchantItem() != null || activeChar.getActiveTradeList() != null || activeChar.getActiveWarehouse() != null)
		{
			showMessageErrorEquip(activeChar);
			return;
		}
		if (activeChar.getInventory() != null && activeChar.getInventory().getItemByItemId(henna.getItemIdDye()) != null)
		{
			_count = activeChar.getInventory().getItemByItemId(henna.getItemIdDye()).getCount();
		}
		if (activeChar.getHennaEmptySlots() == 0)
		{
			activeChar.sendPacket(SystemMessageId.SYMBOLS_FULL);
		}
		if (!cheater && _count >= henna.getAmountDyeRequire() && activeChar.getAdena() >= henna.getPrice() && activeChar.addHenna(henna))
		{
			activeChar.destroyItemByItemId("Henna", henna.getItemIdDye(), henna.getAmountDyeRequire(), activeChar, true);
			activeChar.destroyItemByItemId("Henna Price", 57, henna.getPrice(), activeChar, true);

			// update inventory
			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(activeChar.getInventory().getAdenaInstance());
			activeChar.sendPacket(iu);

			activeChar.sendPacket(SystemMessageId.SYMBOL_ADDED);
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.CANT_DRAW_SYMBOL);

			if (!activeChar.isGM() && cheater)
			{
				Util.handleIllegalPlayerAction(activeChar, "Exploit attempt: Character " + activeChar.getName() + " of account " + activeChar.getAccountName() + " tryed to add a forbidden henna.", Config.DEFAULT_PUNISH);
			}
		}
	}

}