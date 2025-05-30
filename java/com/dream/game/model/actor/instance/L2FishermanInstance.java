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

import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.datatables.xml.SkillTreeTable;
import com.dream.game.manager.games.fishingChampionship;
import com.dream.game.model.L2Skill;
import com.dream.game.model.L2SkillLearn;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.AcquireSkillList;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2FishermanInstance extends L2MerchantInstance
{
	public L2FishermanInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String filename = "";

		if (val == 0)
		{
			filename = "" + npcId;
		}
		else
		{
			filename = npcId + "-" + val;
		}

		return "data/html/fisherman/" + filename + ".htm";
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("FishSkillList"))
		{
			player.setSkillLearningClassId(player.getClassId());
			showSkillList(player);
		}
		if (command.startsWith("fishingChampionship"))
		{
			showChampScreen(player);
		}
		if (command.startsWith("fishingReward"))
		{
			fishingChampionship.getInstance().getReward(player);
		}

		StringTokenizer st = new StringTokenizer(command, " ");
		String cmd = st.nextToken();

		if (cmd.equalsIgnoreCase("Buy"))
		{
			if (st.countTokens() < 1)
				return;
			int val = Integer.parseInt(st.nextToken());
			showBuyWindow(player, val);
		}
		else if (cmd.equalsIgnoreCase("Sell"))
		{
			showSellWindow(player);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	public void showChampScreen(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		String str;
		str = "<html><head><title>Royal tournament fishing</title></head>";
		str += "Member Of The Guild Of Fishermen:<br><br>";
		str += "Hello! I have a list of winners of the fishing tournament last week!<br>";
		str += "Your name is on the list? If Yes, then I will hand you a prize!<br>";
		str += "Remember that you can only pick it up<font color=\"LEVEL\"> During this week</font>.<br>";
		str += "Do not get discouraged if you cannot win! Better luck next time!<br>";
		str += "This message will be updated through " + fishingChampionship.getInstance().getTimeRemaining() + " min!<br>";
		str += "<center><a action=\"bypass -h npc_%objectId%_fishingReward\">Get a prize</a><br></center>";
		str += "<table width=280 border=0 bgcolor=\"000000\"><tr><td width=70 align=center>Place</td><td width=110 align=center>Fisherman</td><td width=80 align=center>Length</td></tr></table><table width=280>";
		for (int x = 1; x <= 5; x++)
		{
			str += "<tr><td width=70 align=center>" + x + " Place:</td>";
			str += "<td width=110 align=center>" + fishingChampionship.getInstance().getWinnerName(x) + "</td>";
			str += "<td width=80 align=center>" + fishingChampionship.getInstance().getFishLength(x) + "</td></tr>";
		}
		str += "<td width=80 align=center>0</td></tr></table><br>";
		str += "List of prizes<br><table width=280 border=0 bgcolor=\"000000\"><tr><td width=70 align=center>Place</td><td width=110 align=center>Prize</td><td width=80 align=center>Number</td></tr></table><table width=280>";
		str += "<tr><td width=70 align=center>1 Place:</td><td width=110 align=center>Adena</td><td width=80 align=center>800000</td></tr><tr><td width=70 align=center>2 Place:</td><td width=110 align=center>аден</td><td width=80 align=center>500000</td></tr><tr><td width=70 align=center>3 Place:</td><td width=110 align=center>Adena</td><td width=80 align=center>300000</td></tr>";
		str += "<tr><td width=70 align=center>4 Place:</td><td width=110 align=center>Adena</td><td width=80 align=center>200000</td></tr><tr><td width=70 align=center>5 Place:</td><td width=110 align=center>аден</td><td width=80 align=center>100000</td></tr></table></body></html>";
		html.setHtml(str);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}

	public void showSkillList(L2PcInstance player)
	{
		L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(player);
		AcquireSkillList asl = new AcquireSkillList(AcquireSkillList.SkillType.Fishing);

		int counts = 0;

		for (L2SkillLearn s : skills)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());

			if (sk == null)
			{
				continue;
			}

			counts++;
			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getSpCost(), 1);
		}

		if (counts == 0)
		{
			SystemMessage sm;
			int minlevel = SkillTreeTable.getInstance().getMinLevelForNewSkill(player);
			if (minlevel > 0)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_COME_BACK_WHEN_REACHED_S1).addNumber(minlevel);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.NO_MORE_SKILLS_TO_LEARN);
			}

			player.sendPacket(sm);
		}
		else
		{
			player.sendPacket(asl);
		}

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}