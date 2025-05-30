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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import com.dream.Config;
import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.manager.RaidBossInfoManager;
import com.dream.game.model.L2DropData;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.tools.random.Rnd;

/**
 * @author rapfersan92
 */
public class L2RaidBossInfoInstance extends L2NpcInstance
{
       private final Map<Integer, Integer> _lastPage = new ConcurrentHashMap<>();
      
       private final String[][] _messages =
       {
               {
                       "<font color=\"LEVEL\">%player%</font>, are you not afraid?",
                       "Be careful <font color=\"LEVEL\">%player%</font>!"
               },
               {
                       "Here is the drop list of <font color=\"LEVEL\">%boss%</font>!",
                       "Seems that <font color=\"LEVEL\">%boss%</font> has good drops."
               },
       };
      
	public L2RaidBossInfoInstance(int objectId, L2NpcTemplate template)
       {
               super(objectId, template);
       }
      
       @Override
       public void showChatWindow(L2PcInstance player, int val)
       {
               String name = "data/html/mods/raidbossinfo/" + getNpcId() + ".htm";
               if (val != 0)
                       name = "data/html/mods/raidbossinfo/" + getNpcId() + "-" + val + ".htm";
              
               NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
               html.setFile(name);
               html.replace("%objectId%", getObjectId());
               player.sendPacket(html);
               player.sendPacket(ActionFailed.STATIC_PACKET);
       }
      
       @Override
       public void onBypassFeedback(L2PcInstance player, String command)
       {
               StringTokenizer st = new StringTokenizer(command, " ");
               String currentCommand = st.nextToken();
              
               if (currentCommand.startsWith("RaidBossInfo"))
               {
                       int pageId = Integer.parseInt(st.nextToken());
                       _lastPage.put(player.getObjectId(), pageId);
                       showRaidBossInfo(player, pageId);
               }
               else if (currentCommand.startsWith("RaidBossDrop"))
               {
                       int bossId = Integer.parseInt(st.nextToken());
                       int pageId = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
                       showRaidBossDrop(player, bossId, pageId);
               }
              
               super.onBypassFeedback(player, command);
       }
      
       private void showRaidBossInfo(L2PcInstance player, int pageId)
       {
               List<Integer> infos = new ArrayList<>();
               infos.addAll(Config.LIST_RAID_BOSS_IDS);
              
               final int limit = Config.RAID_BOSS_INFO_PAGE_LIMIT;
               final int max = infos.size() / limit + (infos.size() % limit == 0 ? 0 : 1);
               infos = infos.subList((pageId - 1) * limit, Math.min(pageId * limit, infos.size()));
              
               final StringBuilder sb = new StringBuilder();
               sb.append("<html>");
               sb.append("<center>");
               sb.append("<body>");
               sb.append("<table width=\"256\">");
               sb.append("<tr><td width=\"256\" align=\"center\">%name%</td></tr>");
               sb.append("</table>");
               sb.append("<br>");
               sb.append("<table width=\"256\">");
               sb.append("<tr><td width=\"256\" align=\"left\">" + _messages[0][Rnd.get(_messages.length)].replace("%player%", player.getName()) + "</td></tr>");
               sb.append("</table>");
               sb.append("<br>");
               sb.append("<table width=\"224\" bgcolor=\"000000\">");
               sb.append("<tr><td width=\"224\" align=\"center\">Raid Boss Infos</td></tr>");
               sb.append("</table>");
               sb.append("<br>");
               sb.append("<table width=\"256\">");
              
               for (int bossId : infos)
               {
			final L2NpcTemplate template = NpcTable.getInstance().getTemplate(bossId);
                       if (template == null)
                               continue;
                      
                       String bossName = template.getName();
                       if (bossName.length() > 23)
                               bossName = bossName.substring(0, 23) + "...";
                      
                       final long respawnTime = RaidBossInfoManager.getInstance().getRaidBossRespawnTime(bossId);
                       if (respawnTime <= System.currentTimeMillis())
                       {
                               sb.append("<tr>");
                               sb.append("<td width=\"146\" align=\"left\"><a action=\"bypass -h npc_%objectId%_RaidBossDrop " + bossId + "\">" + bossName + "</a></td>");
                               sb.append("<td width=\"110\" align=\"right\"><font color=\"9CC300\">Alive</font></td>");
                               sb.append("</tr>");
                       }
                       else
                       {
                               sb.append("<tr>");
                               sb.append("<td width=\"146\" align=\"left\"><a action=\"bypass -h npc_%objectId%_RaidBossDrop " + bossId + "\">" + bossName + "</a></td>");
                               sb.append("<td width=\"110\" align=\"right\"><font color=\"FB5858\">Dead</font> " + new SimpleDateFormat(Config.RAID_BOSS_DATE_FORMAT).format(new Date(respawnTime)) + "</td>");
                               sb.append("</tr>");
                       }
               }
              
               sb.append("</table>");
               sb.append("<br>");
               sb.append("<table width=\"224\" cellspacing=\"2\">");
               sb.append("<tr>");
              
               for (int x = 0; x < max; x++)
               {
                       final int pageNr = x + 1;
                       if (pageId == pageNr)
                               sb.append("<td align=\"center\">" + pageNr + "</td>");
                       else
                               sb.append("<td align=\"center\"><a action=\"bypass -h npc_%objectId%_RaidBossInfo " + pageNr + "\">" + pageNr + "</a></td>");
               }
              
               sb.append("</tr>");
               sb.append("</table>");
               sb.append("<br>");
               sb.append("<table width=\"160\" cellspacing=\"2\">");
               sb.append("<tr>");
               sb.append("<td width=\"160\" align=\"center\"><a action=\"bypass -h npc_%objectId%_Chat 0\">Return</a></td>");
               sb.append("</tr>");
               sb.append("</table>");
               sb.append("<br>");
               sb.append("<table width=\"256\">");
               sb.append("</table>");
               sb.append("</center>");
               sb.append("</body>");
               sb.append("</html>");
              
               final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
               html.setHtml(sb.toString());
               html.replace("%name%", getName());
               html.replace("%objectId%", getObjectId());
               player.sendPacket(html);
       }
      
       private void showRaidBossDrop(L2PcInstance player, int bossId, int pageId)
       {
		final L2NpcTemplate template = NpcTable.getInstance().getTemplate(bossId);
               if (template == null)
                       return;
              
               List<Integer> drops = new ArrayList<>();
		for (L2DropData drop : template.getAllDropData())
                       drops.add(drop.getItemId());

               final int limit = Config.RAID_BOSS_DROP_PAGE_LIMIT;
               final int max = drops.size() / limit + (drops.size() % limit == 0 ? 0 : 1);
               drops = drops.subList((pageId - 1) * limit, Math.min(pageId * limit, drops.size()));
		if (drops == null)
			return;

               final StringBuilder sb = new StringBuilder();
               sb.append("<html>");
               sb.append("<center>");
               sb.append("<body>");
               sb.append("<table width=\"256\">");
               sb.append("<tr><td width=\"256\" align=\"center\">%name%</td></tr>");
               sb.append("</table>");
               sb.append("<br>");
               sb.append("<table width=\"256\">");
               sb.append("<tr><td width=\"256\" align=\"left\">" + _messages[1][Rnd.get(_messages.length)].replace("%boss%", template.getName()) + "</td></tr>");
               sb.append("</table>");
               sb.append("<br>");
               sb.append("<table width=\"224\" bgcolor=\"000000\">");
               sb.append("<tr><td width=\"224\" align=\"center\">Raid Boss Drops</td></tr>");
               sb.append("</table>");
               sb.append("<br>");
               sb.append("<table width=\"256\">");
              
               for (int itemId : drops)
               {
                       String itemName = ItemTable.getInstance().getTemplate(itemId).getName();
                       if (itemName.length() > 47)
                               itemName = itemName.substring(0, 47) + "...";
                      
                       sb.append("<tr><td width=\"256\" align=\"center\">" + itemName + "</td></tr>");
               }
              
               sb.append("</table>");
               sb.append("<br>");
               sb.append("<table width=\"64\" cellspacing=\"2\">");
               sb.append("<tr>");
              
               for (int x = 0; x < max; x++)
               {
			final int pageNr = x + 1;
                       if (pageId == pageNr)
                               sb.append("<td align=\"center\">" + pageNr + "</td>");
                       else
                               sb.append("<td align=\"center\"><a action=\"bypass -h npc_%objectId%_RaidBossDrop " + bossId + " " + pageNr + "\">" + pageNr + "</a></td>");
               }
              
               sb.append("</tr>");
               sb.append("</table>");
               sb.append("<br>");
               sb.append("<table width=\"160\" cellspacing=\"2\">");
               sb.append("<tr>");
               sb.append("<td width=\"160\" align=\"center\"><a action=\"bypass -h npc_%objectId%_RaidBossInfo " + _lastPage.get(player.getObjectId()) + "\">Return</a></td>");
               sb.append("</tr>");
               sb.append("</table>");
               sb.append("<br>");
               sb.append("<table width=\"256\">");
               sb.append("</table>");
               sb.append("</center>");
               sb.append("</body>");
               sb.append("</html>");
              
               final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
               html.setHtml(sb.toString());
               html.replace("%name%", getName());
               html.replace("%objectId%", getObjectId());
               player.sendPacket(html);
       }
}