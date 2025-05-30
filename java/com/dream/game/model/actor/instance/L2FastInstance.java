 package com.dream.game.model.actor.instance;
 
import java.util.StringTokenizer;

import com.dream.game.ai.CtrlIntention;
import com.dream.game.datatables.xml.AugmentationData;
import com.dream.game.model.L2Augmentation;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.ExShowScreenMessage;
import com.dream.game.network.serverpackets.ExVariationCancelResult;
import com.dream.game.network.serverpackets.ExVariationResult;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.SocialAction;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.game.templates.item.L2WeaponType;
import com.dream.tools.random.Rnd;
 
/**
 * Author AbsolutePower
 */
public final class L2FastInstance extends L2NpcInstance
{
   
    public L2FastInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }
   
    private static L2ItemInstance extracted(L2ItemInstance item)
    {
        return item;
    }
   
    @Override
    public void onAction(L2PcInstance player)
    {
        if (this != player.getTarget())
        {
            player.setTarget(this);
            player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
            player.sendPacket(new ValidateLocation(this));
        }
        else if (isInsideRadius(player, INTERACTION_DISTANCE, false, false))
        {
			SocialAction sa = new SocialAction(this, Rnd.get(8));
            broadcastPacket(sa);
            player.getLastFolkNPC();
            showListWindow(player);
            player.sendPacket(ActionFailed.STATIC_PACKET);
        }
        else
        {
			player.getAI().setIntention(CtrlIntention.INTERACT, this);
            player.sendPacket(ActionFailed.STATIC_PACKET);
        }
    }
   
    @Override
    public void onBypassFeedback(L2PcInstance player, String command)
    {
        if (player == null)
            return;
       
        final StringTokenizer st = new StringTokenizer(command, " ");
        final String currentcommand = st.nextToken();
       
        final String letsSliptIt = currentcommand;
        final String[] nowTheId = letsSliptIt.split("-");
       
        final String OurSplititCommand = nowTheId[0];
        final String FinallyWeHaveObjectId = nowTheId[1];
       
        if (OurSplititCommand.startsWith("showremlist"))
        {
            showListWindowForRemove(player);
            player.sendPacket(new ActionFailed());
        }
       
        if (OurSplititCommand.startsWith("showauglist"))
        {
            showListWindow(player);
            player.sendPacket(new ActionFailed());
        }
       
        if (OurSplititCommand.startsWith("tryremove"))
        {
            try
            {
               
                final L2ItemInstance item = player.getInventory().getItemByObjectId(Integer.parseInt(FinallyWeHaveObjectId));
               
                if (extracted(item) == null)
                {
                    player.sendPacket(new ActionFailed());
                    return;
                }
               
                if (extracted(item).isEquipped())
                {
                    player.disarmWeapons();
                    player.broadcastUserInfo();
                }
               
                extracted(item).removeAugmentation();
               
                player.sendPacket(new ExVariationCancelResult(1));
               
                InventoryUpdate iu = new InventoryUpdate();
                iu.addModifiedItem(extracted(item));
                player.sendPacket(iu);
               
				player.sendSkillList();
               
                SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.AUGMENTATION_HAS_BEEN_SUCCESSFULLY_REMOVED_FROM_YOUR_S1);
                player.sendPacket(sm);
                showListWindowForRemove(player);
                player.sendPacket(new ActionFailed());
            }
            catch (Exception e)
            {
                player.sendPacket(new ActionFailed());
            }
        }
        if (OurSplititCommand.startsWith("tryaug"))
        {
            try
            {
                if (player.getInventory().getInventoryItemCount(57, 0) < 200000)
                {
                    player.sendMessage("You do not have enough adena!");
                    player.sendPacket(new ActionFailed());
                    return;
                }
               
                final L2ItemInstance item = player.getInventory().getItemByObjectId(Integer.parseInt(FinallyWeHaveObjectId));
               
                if (extracted(item) == null)
                {
                    player.sendPacket(new ActionFailed());
                    return;
                }
               
                if (extracted(item).isEquipped())
                {
                    player.disarmWeapons();
                    player.broadcastUserInfo();
                }
               
				final L2Augmentation aug = AugmentationData.getInstance().generateRandomAugmentation(2, 2);
                extracted(item).setAugmentation(aug);
               
                final int stat12 = 0x0000FFFF & aug.getAugmentationId();
                final int stat34 = aug.getAugmentationId() >> 16;
                player.sendPacket(new ExVariationResult(stat12, stat34, 1));
               
                InventoryUpdate iu = new InventoryUpdate();
                iu.addModifiedItem(extracted(item));
                player.sendPacket(iu);
               
				StatusUpdate su = new StatusUpdate(player);
                su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
                player.sendPacket(su);
               
                showListWindow(player);
               
                player.getInventory().reduceAdena("FastAugh", 200000, player, null);
               
                player.sendPacket(SystemMessageId.THE_ITEM_WAS_SUCCESSFULLY_AUGMENTED);
               
                if (extracted(item).getAugmentation().getSkill() != null)
                {
                    player.sendPacket(new ExShowScreenMessage("You have " + extracted(item).getAugmentation().getSkill().getName(), 5000));
					player.sendSkillList();
                }
               
                player.sendPacket(new ActionFailed());
               
            }
            catch (Exception e)
            {
                player.sendPacket(new ActionFailed());
            }
        }
       
        super.onBypassFeedback(player, command);
    }
   
    public void showListWindow(L2PcInstance player)
    {
        NpcHtmlMessage nhm = new NpcHtmlMessage(5);
        StringBuilder tb = new StringBuilder("");
        String Rem = "RemoveAug";
       
        tb.append("<html><head><title>Argumenter Fast</title></head><body>");
        tb.append("<center>");
        tb.append("<table width=\"250\" cellpadding=\"5\" bgcolor=\"000000\">");
        tb.append("<tr>");
        tb.append("<td width=\"45\" valign=\"top\" align=\"center\"><img src=\"L2ui_ch3.menubutton4\" width=\"38\" height=\"38\"></td>");
        tb.append("<td valign=\"top\"><font color=\"FF6600\">AugmentHelper</font>");
        tb.append("<br1><font color=\"00FF00ju\">" + player.getName() + "</font>, use this menu for fast augment :)<br1></td>");
        tb.append("</tr>");
        tb.append("</table>");
        tb.append("</center>");
        tb.append("<center>");
        tb.append("<br>");
       
        for (L2ItemInstance item : player.getInventory().getItems())
        {
           
            if (!extracted(item).isAugmented() && extracted(item).getItemType() instanceof L2WeaponType && extracted(item).isEquipable() && !extracted(item).isItem())
            {
                if (extracted(item) != null)
                    tb.append("<button value=\"" + extracted(item).getItemName() + "\" action=\"bypass -h npc_" + getObjectId() + "_tryaug-" + extracted(item).getObjectId() + "\" width=204 height=20 back=\"sek.cbui75\" fore=\"sek.cbui75\"><br>");
                // tb.append("<table border=0 width=\"100%\">");
                // tb.append("<tr><td><img src=\""+IconJava.getIcon(item.getItemId())+"\"width=\"32\"height=\"32\"></td>"
                // +"<td>"+"<button value=\""+item.getItemName()+"\" action=\"bypass -h npc_"+getObjectId()+"_tryaug-"+item.getObjectId()+"\"width=204 height=21 back=\"sek.cbui75\" fore=\"sek.cbui75\"><br>"+"</td>"+"<td>+"+item.getEnchantLevel()+"</td></tr></table>");
            }
           
        }
       
        tb.append("<br>");
        tb.append("<button value=\"" + Rem + "\" action=\"bypass -h npc_" + getObjectId() + "_showremlist-1" + "\" width=204 height=20 back=\"sek.cbui75\" fore=\"sek.cbui75\"><br>");
        tb.append("</center>");
        tb.append("</body></html>");
       
        nhm.setHtml(tb.toString());
        player.sendPacket(nhm);
    }
   
    public void showListWindowForRemove(L2PcInstance player)
    {
        NpcHtmlMessage nhm = new NpcHtmlMessage(5);
        StringBuilder tb = new StringBuilder("");
        String Rem = "GobackToAugList";
       
        tb.append("<html><head><title>By AbsolutePower</title></head><body>");
        tb.append("<center>");
        tb.append("<table width=\"250\" cellpadding=\"5\" bgcolor=\"000000\">");
        tb.append("<tr>");
        tb.append("<td width=\"45\" valign=\"top\" align=\"center\"><img src=\"L2ui_ch3.menubutton4\" width=\"38\" height=\"38\"></td>");
        tb.append("<td valign=\"top\"><font color=\"FF6600\">AugmentHelper</font>");
        tb.append("<br1><font color=\"00FF00ju\">" + player.getName() + "</font>, use this menu for fast augment :)<br1></td>");
        tb.append("</tr>");
        tb.append("</table>");
        tb.append("</center>");
        tb.append("<center>");
        tb.append("<br>");
       
        for (L2ItemInstance item : player.getInventory().getItems())
        {
           
            if (extracted(item).isAugmented() && extracted(item).getItemType() instanceof L2WeaponType && extracted(item).isEquipable() && !extracted(item).isItem())
            {
                if (extracted(item) != null)
                    tb.append("<button value=\"" + extracted(item).getItemName() + "\" action=\"bypass -h npc_" + getObjectId() + "_tryremove-" + extracted(item).getObjectId() + "\" width=204 height=20 back=\"sek.cbui75\" fore=\"sek.cbui75\"><br>");
                // tb.append("<table border=0 width=\"100%\">");
                // tb.append("<tr><td><img src=\""+IconJava.getIcon(item.getItemId())+"\"width=\"32\"height=\"32\"></td>"
                // +"<td>"+"<button value=\""+item.getItemName()+"\" action=\"bypass -h npc_"+getObjectId()+"_tryaug-"+item.getObjectId()+"\"width=204 height=21 back=\"sek.cbui75\" fore=\"sek.cbui75\"><br>"+"</td>"+"<td>+"+item.getEnchantLevel()+"</td></tr></table>");
            }
           
        }
       
        tb.append("<br>");
        tb.append("<button value=\"" + Rem + "\" action=\"bypass -h npc_" + getObjectId() + "_showauglist-1" + "\" width=204 height=20 back=\"sek.cbui75\" fore=\"sek.cbui75\"><br>");
        tb.append("</center>");
        tb.append("</body></html>");
       
        nhm.setHtml(tb.toString());
        player.sendPacket(nhm);
    }
   
}