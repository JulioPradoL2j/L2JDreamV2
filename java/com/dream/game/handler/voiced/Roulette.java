package com.dream.game.handler.voiced;

import com.dream.game.cache.HtmCache;
import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.datatables.xml.RoletaData;
import com.dream.game.handler.IVoicedCommandHandler;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.holders.RoletaItem;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.ItemList;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.templates.item.L2Item;
import com.dream.game.tokenizer.CommandTokenizer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

public class Roulette implements IVoicedCommandHandler
{
	@Override
	public String getDescription(String command)
	{
		if (command.equals("roulette"))
			return "Displays a roulette of commands.";
		return "In detail in the roulette.";
	}
	
	private Map<Integer, Long> _nextUse = new HashMap<>();
	private Map<Integer, ScheduledFuture<?>> _activeTasks = new HashMap<>();
	
	private static final String[] VOICED_COMMANDS =
	{
		"roulette",
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance player, String target)
	{
		
		if (command.startsWith("roulette"))
		{
			final CommandTokenizer tokenizer = new CommandTokenizer(command);
			
			if (tokenizer.size() == 1)
			{
				navi(player, "roulette.htm", 1);
				return true;
			}
			
			final String param = tokenizer.getToken(1);
			
			if (param == null)
			{
				navi(player, "roulette.htm", 1);
				return true;
			}
			
			switch (param.toLowerCase())
			{
				case "navi":
					int page = tokenizer.getAsInteger(2, 0);
					navi(player, "roulette.htm", page);
					break;
				case "spin":
					trySpin(player);
					break;
			}
		}
		return true;
	}
	
	public static void navi(L2PcInstance player, String filename, int page)
	{
		String content = HtmCache.getInstance().getHtm("data/html/mods/" + filename);
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		content = content.replace("%nameServer%", "L2JFury");
		content = content.replace("%playername%", player.getName());
		html.setHtml(content);
		player.sendPacket(html);
	}
	
	private void trySpin(L2PcInstance player)
	{
		ScheduledFuture<?> activeTask = _activeTasks.get(player.getObjectId());
		if (activeTask != null && !activeTask.isDone())
		{
			player.sendMessage("You are already spinning the roulette wheel!");
			return;
		}
		
		if (_nextUse.containsKey(player.getObjectId()) && _nextUse.get(player.getObjectId()) > System.currentTimeMillis())
		{
			player.sendMessage("You must wait before spinning again.");
			return;
		}
		
		boolean canSpin = false;
		RoletaData roleta = RoletaData.getInstance();
		
		if (roleta.isUseAdena())
		{
			canSpin = player.reduceAdena("Roleta", roleta.getAdenaCost(), player, true);
			if (!canSpin)
				player.sendMessage("You don't have enough Adena!");
		}
		else
		{
			if (player.getInventory().getInventoryItemCount(roleta.getItemId(), -1) >= roleta.getItemCount())
			{
				player.destroyItemByItemId("Roleta", roleta.getItemId(), roleta.getItemCount(), player, true);
				canSpin = true;
			}
			else
			{
				player.sendMessage("You don't have enough required items!");
			}
		}
		
		if (!canSpin)
			return;
		
		_nextUse.put(player.getObjectId(), System.currentTimeMillis() + 5000); // cooldown
		
		startSpin(player);
	}
	
	private void startSpin(L2PcInstance player)
	{
		ScheduledFuture<?> task = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() -> {
			sendRollingHtml(player);
		}, 500, 1000);
		
		_activeTasks.put(player.getObjectId(), task);
		
		ThreadPoolManager.getInstance().scheduleGeneral(() -> {
			RoletaItem prize = RoletaData.getInstance().getRandomItem();
			if (prize != null)
			{
				ScheduledFuture<?> activeTask = _activeTasks.remove(player.getObjectId());
				if (activeTask != null)
				{
					activeTask.cancel(false);
				}
				
				L2ItemInstance item = null;
				
				if (prize.getEnchant() > 0)
				{
					
					item = player.getInventory().addItem("Roleta", prize.getId(), prize.getCount(), player, player);
					item.setEnchantLevel(prize.getEnchant());
					
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2_SUCCESSFULLY_ENCHANTED);
					
					sm.addNumber(prize.getEnchant());
					sm.addItemName(prize.getId());
					player.sendPacket(sm);
				}
				else
				{
					player.addItem("Roleta", prize.getId(), prize.getCount(), player, true);
					
				}
				
				player.sendPacket(new ItemList(player, false));
				showPrizeHtml(player, prize);
			}
		}, 10000); // 10 segundos
	}
	
	private void sendRollingHtml(L2PcInstance player)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<html><body><center>");
		sb.append("<font color=\"LEVEL\">Spinning...</font><br1>");
		
		for (int block = 0; block < 8; block++)
		{
			sb.append("<table width=300 bgcolor=000000><tr>");
			
			for (int i = 0; i < 5; i++)
			{
				RoletaItem randomItem = RoletaData.getInstance().getRandomVisualItem();
				if (randomItem != null)
				{
					sb.append("<td align=center>");
					sb.append("<img src=\"" + getItemIcon(randomItem.getId()) + "\" width=32 height=32>");
					sb.append("</td>");
				}
				else
				{
					
					sb.append("<td align=center>?</td>");
				}
			}
			
			sb.append("</tr></table>");
		}
		
		sb.append("<br><font color=999999>Good luck!</font>");
		sb.append("</center></body></html>");
		
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setHtml(sb.toString());
		player.sendPacket(html);
	}
	
	private void showPrizeHtml(L2PcInstance player, RoletaItem prize)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<html><body><center>");
		sb.append("<font color=\"LEVEL\">You won!</font><br>");
		sb.append("<img src=\"" + getItemIcon(prize.getId()) + "\" width=32 height=32><br>");
		sb.append("<font color=\"FFFFFF\">" + getItemNameId(prize.getId()) + "</font><br>");
		sb.append("<font color=\"LEVEL\">Amount:</font> <font color=\"FFFFFF\">" + prize.getCount() + "</font><br>");
		sb.append("<br><button value=\"Spin Again\" action=\"bypass -h voiced_roulette spin\" width=75 height=16 back=\"sek.cbui307\" fore=\"sek.cbui308\">");
		sb.append("</center></body></html>");
		
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setHtml(sb.toString());
		player.sendPacket(html);
	}
	
	public String getItemIcon(int itemId)
	{
		L2Item item = ItemTable.getInstance().getTemplate(itemId);
		return item != null ? item.getIcon() : "Unknown Item";
	}
	
	public String getItemNameId(int itemId)
	{
		L2Item item = ItemTable.getInstance().getTemplate(itemId);
		return item != null ? item.getName() : "Icon.NOIMAGE";
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
