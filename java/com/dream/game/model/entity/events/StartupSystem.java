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
package com.dream.game.model.entity.events;

import com.dream.Config;
import com.dream.game.cache.HtmCache;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.base.ClassId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.CameraMode;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.network.serverpackets.ItemList;
import com.dream.game.network.serverpackets.NormalCamera;
import com.dream.game.network.serverpackets.SocialAction;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.network.serverpackets.TutorialCloseHtml;
import com.dream.game.network.serverpackets.TutorialShowHtml;
import com.dream.game.util.Util;
import com.dream.tools.random.Rnd;

public class StartupSystem
{
	public static void startSetup(L2PcInstance activeChar)
	{
		if (activeChar.getVar("select_class") == null && Config.STARTUP_SYSTEM_SELECTCLASS)
			SelectClass(activeChar);

		else if (activeChar.getVar("select_armor") == null && Config.STARTUP_SYSTEM_SELECTARMOR)
			SelectArmor(activeChar);

		else if (activeChar.getVar("select_weapon") == null && Config.STARTUP_SYSTEM_SELECTWEAP)
			SelectWeapon(activeChar);

		else if (activeChar.getVar("startupfinish") == null)
			endStartup(activeChar);
	}

	public static void handleCommands(final L2PcInstance activeChar, String _command)
	{
		// Classes
		// Human Fighter
		if (_command.startsWith("Duelist") && activeChar.getVar("select_class") == null)
		{
			if (activeChar.getClassId() == ClassId.duelist || activeChar.getClassId() == ClassId.dreadnought || activeChar.getClassId() == ClassId.phoenixKnight || activeChar.getClassId() == ClassId.hellKnight || activeChar.getClassId() == ClassId.sagittarius || activeChar.getClassId() == ClassId.adventurer || activeChar.getClassId() == ClassId.archmage || activeChar.getClassId() == ClassId.soultaker || activeChar.getClassId() == ClassId.arcanaLord || activeChar.getClassId() == ClassId.cardinal || activeChar.getClassId() == ClassId.hierophant || activeChar.getClassId() == ClassId.evaTemplar || activeChar.getClassId() == ClassId.swordMuse || activeChar.getClassId() == ClassId.windRider || activeChar.getClassId() == ClassId.moonlightSentinel || activeChar.getClassId() == ClassId.mysticMuse || activeChar.getClassId() == ClassId.elementalMaster || activeChar.getClassId() == ClassId.evaSaint || activeChar.getClassId() == ClassId.shillienTemplar || activeChar.getClassId() == ClassId.spectralDancer || activeChar.getClassId() == ClassId.ghostHunter || activeChar.getClassId() == ClassId.ghostSentinel || activeChar.getClassId() == ClassId.stormScreamer || activeChar.getClassId() == ClassId.spectralMaster || activeChar.getClassId() == ClassId.shillienSaint || activeChar.getClassId() == ClassId.titan || activeChar.getClassId() == ClassId.grandKhauatari || activeChar.getClassId() == ClassId.dominator || activeChar.getClassId() == ClassId.doomcryer || activeChar.getClassId() == ClassId.fortuneSeeker || activeChar.getClassId() == ClassId.maestro)
			{
				Util.handleIllegalPlayerAction(activeChar, "StartupSystem: player [" + activeChar.getName() + "] trying to change class exploit.", 2);
				return;
			}

			if (activeChar.getVar("select_armor") == null && Config.STARTUP_SYSTEM_SELECTARMOR)
			{
			SelectArmor(activeChar);
			}
			else if (activeChar.getVar("select_armor") != null || !Config.STARTUP_SYSTEM_SELECTARMOR)
			{
				if (Config.STARTUP_SYSTEM_SELECTWEAP && activeChar.getVar("select_weapon") == null)
					SelectWeapon(activeChar);
				else
					endStartup(activeChar);
			}

			activeChar.setClassId(88);
			activeChar.setVar("select_class", "1");
			activeChar.broadcastUserInfo();
			activeChar.setBaseClass(activeChar.getActiveClass());
			activeChar.store();

			if (Config.CHECK_SKILLS_ON_ENTER)
				activeChar.checkAllowedSkills();
		}
		else if (_command.startsWith("DreadNought") && activeChar.getVar("select_class") == null)
		{
			if (activeChar.getClassId() == ClassId.duelist || activeChar.getClassId() == ClassId.dreadnought || activeChar.getClassId() == ClassId.phoenixKnight || activeChar.getClassId() == ClassId.hellKnight || activeChar.getClassId() == ClassId.sagittarius || activeChar.getClassId() == ClassId.adventurer || activeChar.getClassId() == ClassId.archmage || activeChar.getClassId() == ClassId.soultaker || activeChar.getClassId() == ClassId.arcanaLord || activeChar.getClassId() == ClassId.cardinal || activeChar.getClassId() == ClassId.hierophant || activeChar.getClassId() == ClassId.evaTemplar || activeChar.getClassId() == ClassId.swordMuse || activeChar.getClassId() == ClassId.windRider || activeChar.getClassId() == ClassId.moonlightSentinel || activeChar.getClassId() == ClassId.mysticMuse || activeChar.getClassId() == ClassId.elementalMaster || activeChar.getClassId() == ClassId.evaSaint || activeChar.getClassId() == ClassId.shillienTemplar || activeChar.getClassId() == ClassId.spectralDancer || activeChar.getClassId() == ClassId.ghostHunter || activeChar.getClassId() == ClassId.ghostSentinel || activeChar.getClassId() == ClassId.stormScreamer || activeChar.getClassId() == ClassId.spectralMaster || activeChar.getClassId() == ClassId.shillienSaint || activeChar.getClassId() == ClassId.titan || activeChar.getClassId() == ClassId.grandKhauatari || activeChar.getClassId() == ClassId.dominator || activeChar.getClassId() == ClassId.doomcryer || activeChar.getClassId() == ClassId.fortuneSeeker || activeChar.getClassId() == ClassId.maestro)
			{
				Util.handleIllegalPlayerAction(activeChar, "StartupSystem: player [" + activeChar.getName() + "] trying to change class exploit.", 2);
				return;
			}

			if (activeChar.getVar("select_armor") == null && Config.STARTUP_SYSTEM_SELECTARMOR)
			{
			SelectArmor(activeChar);
			}
			else if (activeChar.getVar("select_armor") != null || !Config.STARTUP_SYSTEM_SELECTARMOR)
			{
				if (Config.STARTUP_SYSTEM_SELECTWEAP && activeChar.getVar("select_weapon") == null)
					SelectWeapon(activeChar);
				else
					endStartup(activeChar);
			}

			activeChar.setClassId(89);
			activeChar.setVar("select_class", "1");
			activeChar.broadcastUserInfo();
			activeChar.setBaseClass(activeChar.getActiveClass());
			activeChar.store();

			if (Config.CHECK_SKILLS_ON_ENTER)
				activeChar.checkAllowedSkills();
		}
		else if (_command.startsWith("Phoenix_Knight") && activeChar.getVar("select_class") == null)
		{
			if (activeChar.getClassId() == ClassId.duelist || activeChar.getClassId() == ClassId.dreadnought || activeChar.getClassId() == ClassId.phoenixKnight || activeChar.getClassId() == ClassId.hellKnight || activeChar.getClassId() == ClassId.sagittarius || activeChar.getClassId() == ClassId.adventurer || activeChar.getClassId() == ClassId.archmage || activeChar.getClassId() == ClassId.soultaker || activeChar.getClassId() == ClassId.arcanaLord || activeChar.getClassId() == ClassId.cardinal || activeChar.getClassId() == ClassId.hierophant || activeChar.getClassId() == ClassId.evaTemplar || activeChar.getClassId() == ClassId.swordMuse || activeChar.getClassId() == ClassId.windRider || activeChar.getClassId() == ClassId.moonlightSentinel || activeChar.getClassId() == ClassId.mysticMuse || activeChar.getClassId() == ClassId.elementalMaster || activeChar.getClassId() == ClassId.evaSaint || activeChar.getClassId() == ClassId.shillienTemplar || activeChar.getClassId() == ClassId.spectralDancer || activeChar.getClassId() == ClassId.ghostHunter || activeChar.getClassId() == ClassId.ghostSentinel || activeChar.getClassId() == ClassId.stormScreamer || activeChar.getClassId() == ClassId.spectralMaster || activeChar.getClassId() == ClassId.shillienSaint || activeChar.getClassId() == ClassId.titan || activeChar.getClassId() == ClassId.grandKhauatari || activeChar.getClassId() == ClassId.dominator || activeChar.getClassId() == ClassId.doomcryer || activeChar.getClassId() == ClassId.fortuneSeeker || activeChar.getClassId() == ClassId.maestro)
			{
				Util.handleIllegalPlayerAction(activeChar, "StartupSystem: player [" + activeChar.getName() + "] trying to change class exploit.", 2);
				return;
			}

			if (activeChar.getVar("select_armor") == null && Config.STARTUP_SYSTEM_SELECTARMOR)
			{
			SelectArmor(activeChar);
			}
			else if (activeChar.getVar("select_armor") != null || !Config.STARTUP_SYSTEM_SELECTARMOR)
			{
				if (Config.STARTUP_SYSTEM_SELECTWEAP && activeChar.getVar("select_weapon") == null)
					SelectWeapon(activeChar);
				else
					endStartup(activeChar);
			}

			activeChar.setClassId(90);
			activeChar.setVar("select_class", "1");
			activeChar.broadcastUserInfo();
			activeChar.setBaseClass(activeChar.getActiveClass());
			activeChar.store();

			if (Config.CHECK_SKILLS_ON_ENTER)
				activeChar.checkAllowedSkills();
		}
		else if (_command.startsWith("Hell_Knight") && activeChar.getVar("select_class") == null)
		{
			if (activeChar.getClassId() == ClassId.duelist || activeChar.getClassId() == ClassId.dreadnought || activeChar.getClassId() == ClassId.phoenixKnight || activeChar.getClassId() == ClassId.hellKnight || activeChar.getClassId() == ClassId.sagittarius || activeChar.getClassId() == ClassId.adventurer || activeChar.getClassId() == ClassId.archmage || activeChar.getClassId() == ClassId.soultaker || activeChar.getClassId() == ClassId.arcanaLord || activeChar.getClassId() == ClassId.cardinal || activeChar.getClassId() == ClassId.hierophant || activeChar.getClassId() == ClassId.evaTemplar || activeChar.getClassId() == ClassId.swordMuse || activeChar.getClassId() == ClassId.windRider || activeChar.getClassId() == ClassId.moonlightSentinel || activeChar.getClassId() == ClassId.mysticMuse || activeChar.getClassId() == ClassId.elementalMaster || activeChar.getClassId() == ClassId.evaSaint || activeChar.getClassId() == ClassId.shillienTemplar || activeChar.getClassId() == ClassId.spectralDancer || activeChar.getClassId() == ClassId.ghostHunter || activeChar.getClassId() == ClassId.ghostSentinel || activeChar.getClassId() == ClassId.stormScreamer || activeChar.getClassId() == ClassId.spectralMaster || activeChar.getClassId() == ClassId.shillienSaint || activeChar.getClassId() == ClassId.titan || activeChar.getClassId() == ClassId.grandKhauatari || activeChar.getClassId() == ClassId.dominator || activeChar.getClassId() == ClassId.doomcryer || activeChar.getClassId() == ClassId.fortuneSeeker || activeChar.getClassId() == ClassId.maestro)
			{
				Util.handleIllegalPlayerAction(activeChar, "StartupSystem: player [" + activeChar.getName() + "] trying to change class exploit.", 2);
				return;
			}

			if (activeChar.getVar("select_armor") == null && Config.STARTUP_SYSTEM_SELECTARMOR)
			{
			SelectArmor(activeChar);
			}
			else if (activeChar.getVar("select_armor") != null || !Config.STARTUP_SYSTEM_SELECTARMOR)
			{
				if (Config.STARTUP_SYSTEM_SELECTWEAP && activeChar.getVar("select_weapon") == null)
					SelectWeapon(activeChar);
				else
					endStartup(activeChar);
			}

			activeChar.setClassId(91);
			activeChar.setVar("select_class", "1");
			activeChar.broadcastUserInfo();
			activeChar.setBaseClass(activeChar.getActiveClass());
			activeChar.store();

			if (Config.CHECK_SKILLS_ON_ENTER)
				activeChar.checkAllowedSkills();
		}
		else if (_command.startsWith("Adventurer") && activeChar.getVar("select_class") == null)
		{
			if (activeChar.getClassId() == ClassId.duelist || activeChar.getClassId() == ClassId.dreadnought || activeChar.getClassId() == ClassId.phoenixKnight || activeChar.getClassId() == ClassId.hellKnight || activeChar.getClassId() == ClassId.sagittarius || activeChar.getClassId() == ClassId.adventurer || activeChar.getClassId() == ClassId.archmage || activeChar.getClassId() == ClassId.soultaker || activeChar.getClassId() == ClassId.arcanaLord || activeChar.getClassId() == ClassId.cardinal || activeChar.getClassId() == ClassId.hierophant || activeChar.getClassId() == ClassId.evaTemplar || activeChar.getClassId() == ClassId.swordMuse || activeChar.getClassId() == ClassId.windRider || activeChar.getClassId() == ClassId.moonlightSentinel || activeChar.getClassId() == ClassId.mysticMuse || activeChar.getClassId() == ClassId.elementalMaster || activeChar.getClassId() == ClassId.evaSaint || activeChar.getClassId() == ClassId.shillienTemplar || activeChar.getClassId() == ClassId.spectralDancer || activeChar.getClassId() == ClassId.ghostHunter || activeChar.getClassId() == ClassId.ghostSentinel || activeChar.getClassId() == ClassId.stormScreamer || activeChar.getClassId() == ClassId.spectralMaster || activeChar.getClassId() == ClassId.shillienSaint || activeChar.getClassId() == ClassId.titan || activeChar.getClassId() == ClassId.grandKhauatari || activeChar.getClassId() == ClassId.dominator || activeChar.getClassId() == ClassId.doomcryer || activeChar.getClassId() == ClassId.fortuneSeeker || activeChar.getClassId() == ClassId.maestro)
			{
				Util.handleIllegalPlayerAction(activeChar, "StartupSystem: player [" + activeChar.getName() + "] trying to change class exploit.", 2);
				return;
			}

			if (activeChar.getVar("select_armor") == null && Config.STARTUP_SYSTEM_SELECTARMOR)
			{
			SelectArmor(activeChar);
			}
			else if (activeChar.getVar("select_armor") != null || !Config.STARTUP_SYSTEM_SELECTARMOR)
			{
				if (Config.STARTUP_SYSTEM_SELECTWEAP && activeChar.getVar("select_weapon") == null)
					SelectWeapon(activeChar);
				else
					endStartup(activeChar);
			}

			activeChar.setClassId(93);
			activeChar.setVar("select_class", "1");
			activeChar.broadcastUserInfo();
			activeChar.setBaseClass(activeChar.getActiveClass());
			activeChar.store();

			if (Config.CHECK_SKILLS_ON_ENTER)
				activeChar.checkAllowedSkills();
		}
		else if (_command.startsWith("Sagittarius") && activeChar.getVar("select_class") == null)
		{
			if (activeChar.getClassId() == ClassId.duelist || activeChar.getClassId() == ClassId.dreadnought || activeChar.getClassId() == ClassId.phoenixKnight || activeChar.getClassId() == ClassId.hellKnight || activeChar.getClassId() == ClassId.sagittarius || activeChar.getClassId() == ClassId.adventurer || activeChar.getClassId() == ClassId.archmage || activeChar.getClassId() == ClassId.soultaker || activeChar.getClassId() == ClassId.arcanaLord || activeChar.getClassId() == ClassId.cardinal || activeChar.getClassId() == ClassId.hierophant || activeChar.getClassId() == ClassId.evaTemplar || activeChar.getClassId() == ClassId.swordMuse || activeChar.getClassId() == ClassId.windRider || activeChar.getClassId() == ClassId.moonlightSentinel || activeChar.getClassId() == ClassId.mysticMuse || activeChar.getClassId() == ClassId.elementalMaster || activeChar.getClassId() == ClassId.evaSaint || activeChar.getClassId() == ClassId.shillienTemplar || activeChar.getClassId() == ClassId.spectralDancer || activeChar.getClassId() == ClassId.ghostHunter || activeChar.getClassId() == ClassId.ghostSentinel || activeChar.getClassId() == ClassId.stormScreamer || activeChar.getClassId() == ClassId.spectralMaster || activeChar.getClassId() == ClassId.shillienSaint || activeChar.getClassId() == ClassId.titan || activeChar.getClassId() == ClassId.grandKhauatari || activeChar.getClassId() == ClassId.dominator || activeChar.getClassId() == ClassId.doomcryer || activeChar.getClassId() == ClassId.fortuneSeeker || activeChar.getClassId() == ClassId.maestro)
			{
				Util.handleIllegalPlayerAction(activeChar, "StartupSystem: player [" + activeChar.getName() + "] trying to change class exploit.", 2);
				return;
			}

			if (activeChar.getVar("select_armor") == null && Config.STARTUP_SYSTEM_SELECTARMOR)
			{
			SelectArmor(activeChar);
			}
			else if (activeChar.getVar("select_armor") != null || !Config.STARTUP_SYSTEM_SELECTARMOR)
			{
				if (Config.STARTUP_SYSTEM_SELECTWEAP && activeChar.getVar("select_weapon") == null)
					SelectWeapon(activeChar);
				else
					endStartup(activeChar);
			}

			activeChar.setClassId(92);
			activeChar.setVar("select_class", "1");
			activeChar.broadcastUserInfo();
			activeChar.setBaseClass(activeChar.getActiveClass());
			activeChar.store();

			if (Config.CHECK_SKILLS_ON_ENTER)
				activeChar.checkAllowedSkills();
		}
		// Human Mystic
		else if (_command.startsWith("Archmage") && activeChar.getVar("select_class") == null)
		{
			if (activeChar.getClassId() == ClassId.duelist || activeChar.getClassId() == ClassId.dreadnought || activeChar.getClassId() == ClassId.phoenixKnight || activeChar.getClassId() == ClassId.hellKnight || activeChar.getClassId() == ClassId.sagittarius || activeChar.getClassId() == ClassId.adventurer || activeChar.getClassId() == ClassId.archmage || activeChar.getClassId() == ClassId.soultaker || activeChar.getClassId() == ClassId.arcanaLord || activeChar.getClassId() == ClassId.cardinal || activeChar.getClassId() == ClassId.hierophant || activeChar.getClassId() == ClassId.evaTemplar || activeChar.getClassId() == ClassId.swordMuse || activeChar.getClassId() == ClassId.windRider || activeChar.getClassId() == ClassId.moonlightSentinel || activeChar.getClassId() == ClassId.mysticMuse || activeChar.getClassId() == ClassId.elementalMaster || activeChar.getClassId() == ClassId.evaSaint || activeChar.getClassId() == ClassId.shillienTemplar || activeChar.getClassId() == ClassId.spectralDancer || activeChar.getClassId() == ClassId.ghostHunter || activeChar.getClassId() == ClassId.ghostSentinel || activeChar.getClassId() == ClassId.stormScreamer || activeChar.getClassId() == ClassId.spectralMaster || activeChar.getClassId() == ClassId.shillienSaint || activeChar.getClassId() == ClassId.titan || activeChar.getClassId() == ClassId.grandKhauatari || activeChar.getClassId() == ClassId.dominator || activeChar.getClassId() == ClassId.doomcryer || activeChar.getClassId() == ClassId.fortuneSeeker || activeChar.getClassId() == ClassId.maestro)
			{
				Util.handleIllegalPlayerAction(activeChar, "StartupSystem: player [" + activeChar.getName() + "] trying to change class exploit.", 2);
				return;
			}

			if (activeChar.getVar("select_armor") == null && Config.STARTUP_SYSTEM_SELECTARMOR)
			{
			SelectArmor(activeChar);
			}
			else if (activeChar.getVar("select_armor") != null || !Config.STARTUP_SYSTEM_SELECTARMOR)
			{
				if (Config.STARTUP_SYSTEM_SELECTWEAP && activeChar.getVar("select_weapon") == null)
					SelectWeapon(activeChar);
				else
					endStartup(activeChar);
			}

			activeChar.setClassId(94);
			activeChar.setVar("select_class", "1");
			activeChar.broadcastUserInfo();
			activeChar.setBaseClass(activeChar.getActiveClass());
			activeChar.store();

			if (Config.CHECK_SKILLS_ON_ENTER)
				activeChar.checkAllowedSkills();
		}
		else if (_command.startsWith("Soultaker") && activeChar.getVar("select_class") == null)
		{
			if (activeChar.getClassId() == ClassId.duelist || activeChar.getClassId() == ClassId.dreadnought || activeChar.getClassId() == ClassId.phoenixKnight || activeChar.getClassId() == ClassId.hellKnight || activeChar.getClassId() == ClassId.sagittarius || activeChar.getClassId() == ClassId.adventurer || activeChar.getClassId() == ClassId.archmage || activeChar.getClassId() == ClassId.soultaker || activeChar.getClassId() == ClassId.arcanaLord || activeChar.getClassId() == ClassId.cardinal || activeChar.getClassId() == ClassId.hierophant || activeChar.getClassId() == ClassId.evaTemplar || activeChar.getClassId() == ClassId.swordMuse || activeChar.getClassId() == ClassId.windRider || activeChar.getClassId() == ClassId.moonlightSentinel || activeChar.getClassId() == ClassId.mysticMuse || activeChar.getClassId() == ClassId.elementalMaster || activeChar.getClassId() == ClassId.evaSaint || activeChar.getClassId() == ClassId.shillienTemplar || activeChar.getClassId() == ClassId.spectralDancer || activeChar.getClassId() == ClassId.ghostHunter || activeChar.getClassId() == ClassId.ghostSentinel || activeChar.getClassId() == ClassId.stormScreamer || activeChar.getClassId() == ClassId.spectralMaster || activeChar.getClassId() == ClassId.shillienSaint || activeChar.getClassId() == ClassId.titan || activeChar.getClassId() == ClassId.grandKhauatari || activeChar.getClassId() == ClassId.dominator || activeChar.getClassId() == ClassId.doomcryer || activeChar.getClassId() == ClassId.fortuneSeeker || activeChar.getClassId() == ClassId.maestro)
			{
				Util.handleIllegalPlayerAction(activeChar, "StartupSystem: player [" + activeChar.getName() + "] trying to change class exploit.", 2);
				return;
			}

			if (activeChar.getVar("select_armor") == null && Config.STARTUP_SYSTEM_SELECTARMOR)
			{
			SelectArmor(activeChar);
			}
			else if (activeChar.getVar("select_armor") != null || !Config.STARTUP_SYSTEM_SELECTARMOR)
			{
				if (Config.STARTUP_SYSTEM_SELECTWEAP && activeChar.getVar("select_weapon") == null)
					SelectWeapon(activeChar);
				else
					endStartup(activeChar);
			}

			activeChar.setClassId(95);
			activeChar.setVar("select_class", "1");
			activeChar.broadcastUserInfo();
			activeChar.setBaseClass(activeChar.getActiveClass());
			activeChar.store();

			if (Config.CHECK_SKILLS_ON_ENTER)
				activeChar.checkAllowedSkills();
		}
		else if (_command.startsWith("Arcana_Lord") && activeChar.getVar("select_class") == null)
		{
			if (activeChar.getClassId() == ClassId.duelist || activeChar.getClassId() == ClassId.dreadnought || activeChar.getClassId() == ClassId.phoenixKnight || activeChar.getClassId() == ClassId.hellKnight || activeChar.getClassId() == ClassId.sagittarius || activeChar.getClassId() == ClassId.adventurer || activeChar.getClassId() == ClassId.archmage || activeChar.getClassId() == ClassId.soultaker || activeChar.getClassId() == ClassId.arcanaLord || activeChar.getClassId() == ClassId.cardinal || activeChar.getClassId() == ClassId.hierophant || activeChar.getClassId() == ClassId.evaTemplar || activeChar.getClassId() == ClassId.swordMuse || activeChar.getClassId() == ClassId.windRider || activeChar.getClassId() == ClassId.moonlightSentinel || activeChar.getClassId() == ClassId.mysticMuse || activeChar.getClassId() == ClassId.elementalMaster || activeChar.getClassId() == ClassId.evaSaint || activeChar.getClassId() == ClassId.shillienTemplar || activeChar.getClassId() == ClassId.spectralDancer || activeChar.getClassId() == ClassId.ghostHunter || activeChar.getClassId() == ClassId.ghostSentinel || activeChar.getClassId() == ClassId.stormScreamer || activeChar.getClassId() == ClassId.spectralMaster || activeChar.getClassId() == ClassId.shillienSaint || activeChar.getClassId() == ClassId.titan || activeChar.getClassId() == ClassId.grandKhauatari || activeChar.getClassId() == ClassId.dominator || activeChar.getClassId() == ClassId.doomcryer || activeChar.getClassId() == ClassId.fortuneSeeker || activeChar.getClassId() == ClassId.maestro)
			{
				Util.handleIllegalPlayerAction(activeChar, "StartupSystem: player [" + activeChar.getName() + "] trying to change class exploit.", 2);
				return;
			}

			if (activeChar.getVar("select_armor") == null && Config.STARTUP_SYSTEM_SELECTARMOR)
			{
			SelectArmor(activeChar);
			}
			else if (activeChar.getVar("select_armor") != null || !Config.STARTUP_SYSTEM_SELECTARMOR)
			{
				if (Config.STARTUP_SYSTEM_SELECTWEAP && activeChar.getVar("select_weapon") == null)
					SelectWeapon(activeChar);
				else
					endStartup(activeChar);
			}

			activeChar.setClassId(96);
			activeChar.setVar("select_class", "1");
			activeChar.broadcastUserInfo();
			activeChar.setBaseClass(activeChar.getActiveClass());
			activeChar.store();

			if (Config.CHECK_SKILLS_ON_ENTER)
				activeChar.checkAllowedSkills();
		}
		else if (_command.startsWith("Cardinal") && activeChar.getVar("select_class") == null)
		{
			if (activeChar.getClassId() == ClassId.duelist || activeChar.getClassId() == ClassId.dreadnought || activeChar.getClassId() == ClassId.phoenixKnight || activeChar.getClassId() == ClassId.hellKnight || activeChar.getClassId() == ClassId.sagittarius || activeChar.getClassId() == ClassId.adventurer || activeChar.getClassId() == ClassId.archmage || activeChar.getClassId() == ClassId.soultaker || activeChar.getClassId() == ClassId.arcanaLord || activeChar.getClassId() == ClassId.cardinal || activeChar.getClassId() == ClassId.hierophant || activeChar.getClassId() == ClassId.evaTemplar || activeChar.getClassId() == ClassId.swordMuse || activeChar.getClassId() == ClassId.windRider || activeChar.getClassId() == ClassId.moonlightSentinel || activeChar.getClassId() == ClassId.mysticMuse || activeChar.getClassId() == ClassId.elementalMaster || activeChar.getClassId() == ClassId.evaSaint || activeChar.getClassId() == ClassId.shillienTemplar || activeChar.getClassId() == ClassId.spectralDancer || activeChar.getClassId() == ClassId.ghostHunter || activeChar.getClassId() == ClassId.ghostSentinel || activeChar.getClassId() == ClassId.stormScreamer || activeChar.getClassId() == ClassId.spectralMaster || activeChar.getClassId() == ClassId.shillienSaint || activeChar.getClassId() == ClassId.titan || activeChar.getClassId() == ClassId.grandKhauatari || activeChar.getClassId() == ClassId.dominator || activeChar.getClassId() == ClassId.doomcryer || activeChar.getClassId() == ClassId.fortuneSeeker || activeChar.getClassId() == ClassId.maestro)
			{
				Util.handleIllegalPlayerAction(activeChar, "StartupSystem: player [" + activeChar.getName() + "] trying to change class exploit.", 2);
				return;
			}

			if (activeChar.getVar("select_armor") == null && Config.STARTUP_SYSTEM_SELECTARMOR)
			{
			SelectArmor(activeChar);
			}
			else if (activeChar.getVar("select_armor") != null || !Config.STARTUP_SYSTEM_SELECTARMOR)
			{
				if (Config.STARTUP_SYSTEM_SELECTWEAP && activeChar.getVar("select_weapon") == null)
					SelectWeapon(activeChar);
				else
					endStartup(activeChar);
			}

			activeChar.setClassId(97);
			activeChar.setVar("select_class", "1");
			activeChar.broadcastUserInfo();
			activeChar.setBaseClass(activeChar.getActiveClass());
			activeChar.store();

			if (Config.CHECK_SKILLS_ON_ENTER)
				activeChar.checkAllowedSkills();
		}
		else if (_command.startsWith("Hierophant") && activeChar.getVar("select_class") == null)
		{
			if (activeChar.getClassId() == ClassId.duelist || activeChar.getClassId() == ClassId.dreadnought || activeChar.getClassId() == ClassId.phoenixKnight || activeChar.getClassId() == ClassId.hellKnight || activeChar.getClassId() == ClassId.sagittarius || activeChar.getClassId() == ClassId.adventurer || activeChar.getClassId() == ClassId.archmage || activeChar.getClassId() == ClassId.soultaker || activeChar.getClassId() == ClassId.arcanaLord || activeChar.getClassId() == ClassId.cardinal || activeChar.getClassId() == ClassId.hierophant || activeChar.getClassId() == ClassId.evaTemplar || activeChar.getClassId() == ClassId.swordMuse || activeChar.getClassId() == ClassId.windRider || activeChar.getClassId() == ClassId.moonlightSentinel || activeChar.getClassId() == ClassId.mysticMuse || activeChar.getClassId() == ClassId.elementalMaster || activeChar.getClassId() == ClassId.evaSaint || activeChar.getClassId() == ClassId.shillienTemplar || activeChar.getClassId() == ClassId.spectralDancer || activeChar.getClassId() == ClassId.ghostHunter || activeChar.getClassId() == ClassId.ghostSentinel || activeChar.getClassId() == ClassId.stormScreamer || activeChar.getClassId() == ClassId.spectralMaster || activeChar.getClassId() == ClassId.shillienSaint || activeChar.getClassId() == ClassId.titan || activeChar.getClassId() == ClassId.grandKhauatari || activeChar.getClassId() == ClassId.dominator || activeChar.getClassId() == ClassId.doomcryer || activeChar.getClassId() == ClassId.fortuneSeeker || activeChar.getClassId() == ClassId.maestro)
			{
				Util.handleIllegalPlayerAction(activeChar, "StartupSystem: player [" + activeChar.getName() + "] trying to change class exploit.", 2);
				return;
			}

			if (activeChar.getVar("select_armor") == null && Config.STARTUP_SYSTEM_SELECTARMOR)
			{
			SelectArmor(activeChar);
			}
			else if (activeChar.getVar("select_armor") != null || !Config.STARTUP_SYSTEM_SELECTARMOR)
			{
				if (Config.STARTUP_SYSTEM_SELECTWEAP && activeChar.getVar("select_weapon") == null)
					SelectWeapon(activeChar);
				else
					endStartup(activeChar);
			}

			activeChar.setClassId(98);
			activeChar.setVar("select_class", "1");
			activeChar.broadcastUserInfo();
			activeChar.setBaseClass(activeChar.getActiveClass());
			activeChar.store();

			if (Config.CHECK_SKILLS_ON_ENTER)
				activeChar.checkAllowedSkills();
		}
		// Elven Fighter
		else if (_command.startsWith("Eva_Templar") && activeChar.getVar("select_class") == null)
		{
			if (activeChar.getClassId() == ClassId.duelist || activeChar.getClassId() == ClassId.dreadnought || activeChar.getClassId() == ClassId.phoenixKnight || activeChar.getClassId() == ClassId.hellKnight || activeChar.getClassId() == ClassId.sagittarius || activeChar.getClassId() == ClassId.adventurer || activeChar.getClassId() == ClassId.archmage || activeChar.getClassId() == ClassId.soultaker || activeChar.getClassId() == ClassId.arcanaLord || activeChar.getClassId() == ClassId.cardinal || activeChar.getClassId() == ClassId.hierophant || activeChar.getClassId() == ClassId.evaTemplar || activeChar.getClassId() == ClassId.swordMuse || activeChar.getClassId() == ClassId.windRider || activeChar.getClassId() == ClassId.moonlightSentinel || activeChar.getClassId() == ClassId.mysticMuse || activeChar.getClassId() == ClassId.elementalMaster || activeChar.getClassId() == ClassId.evaSaint || activeChar.getClassId() == ClassId.shillienTemplar || activeChar.getClassId() == ClassId.spectralDancer || activeChar.getClassId() == ClassId.ghostHunter || activeChar.getClassId() == ClassId.ghostSentinel || activeChar.getClassId() == ClassId.stormScreamer || activeChar.getClassId() == ClassId.spectralMaster || activeChar.getClassId() == ClassId.shillienSaint || activeChar.getClassId() == ClassId.titan || activeChar.getClassId() == ClassId.grandKhauatari || activeChar.getClassId() == ClassId.dominator || activeChar.getClassId() == ClassId.doomcryer || activeChar.getClassId() == ClassId.fortuneSeeker || activeChar.getClassId() == ClassId.maestro)
			{
				Util.handleIllegalPlayerAction(activeChar, "StartupSystem: player [" + activeChar.getName() + "] trying to change class exploit.", 2);
				return;
			}

			if (activeChar.getVar("select_armor") == null && Config.STARTUP_SYSTEM_SELECTARMOR)
			{
			SelectArmor(activeChar);
			}
			else if (activeChar.getVar("select_armor") != null || !Config.STARTUP_SYSTEM_SELECTARMOR)
			{
				if (Config.STARTUP_SYSTEM_SELECTWEAP && activeChar.getVar("select_weapon") == null)
					SelectWeapon(activeChar);
				else
					endStartup(activeChar);
			}

			activeChar.setClassId(99);
			activeChar.setVar("select_class", "1");
			activeChar.broadcastUserInfo();
			activeChar.setBaseClass(activeChar.getActiveClass());
			activeChar.store();

			if (Config.CHECK_SKILLS_ON_ENTER)
				activeChar.checkAllowedSkills();
		}
		else if (_command.startsWith("Sword_Muse") && activeChar.getVar("select_class") == null)
		{
			if (activeChar.getClassId() == ClassId.duelist || activeChar.getClassId() == ClassId.dreadnought || activeChar.getClassId() == ClassId.phoenixKnight || activeChar.getClassId() == ClassId.hellKnight || activeChar.getClassId() == ClassId.sagittarius || activeChar.getClassId() == ClassId.adventurer || activeChar.getClassId() == ClassId.archmage || activeChar.getClassId() == ClassId.soultaker || activeChar.getClassId() == ClassId.arcanaLord || activeChar.getClassId() == ClassId.cardinal || activeChar.getClassId() == ClassId.hierophant || activeChar.getClassId() == ClassId.evaTemplar || activeChar.getClassId() == ClassId.swordMuse || activeChar.getClassId() == ClassId.windRider || activeChar.getClassId() == ClassId.moonlightSentinel || activeChar.getClassId() == ClassId.mysticMuse || activeChar.getClassId() == ClassId.elementalMaster || activeChar.getClassId() == ClassId.evaSaint || activeChar.getClassId() == ClassId.shillienTemplar || activeChar.getClassId() == ClassId.spectralDancer || activeChar.getClassId() == ClassId.ghostHunter || activeChar.getClassId() == ClassId.ghostSentinel || activeChar.getClassId() == ClassId.stormScreamer || activeChar.getClassId() == ClassId.spectralMaster || activeChar.getClassId() == ClassId.shillienSaint || activeChar.getClassId() == ClassId.titan || activeChar.getClassId() == ClassId.grandKhauatari || activeChar.getClassId() == ClassId.dominator || activeChar.getClassId() == ClassId.doomcryer || activeChar.getClassId() == ClassId.fortuneSeeker || activeChar.getClassId() == ClassId.maestro)
			{
				Util.handleIllegalPlayerAction(activeChar, "StartupSystem: player [" + activeChar.getName() + "] trying to change class exploit.", 2);
				return;
			}

			if (activeChar.getVar("select_armor") == null && Config.STARTUP_SYSTEM_SELECTARMOR)
			{
			SelectArmor(activeChar);
			}
			else if (activeChar.getVar("select_armor") != null || !Config.STARTUP_SYSTEM_SELECTARMOR)
			{
				if (Config.STARTUP_SYSTEM_SELECTWEAP && activeChar.getVar("select_weapon") == null)
					SelectWeapon(activeChar);
				else
					endStartup(activeChar);
			}

			activeChar.setClassId(100);
			activeChar.setVar("select_class", "1");
			activeChar.broadcastUserInfo();
			activeChar.setBaseClass(activeChar.getActiveClass());
			activeChar.store();

			if (Config.CHECK_SKILLS_ON_ENTER)
				activeChar.checkAllowedSkills();
		}
		else if (_command.startsWith("Wind_Rider") && activeChar.getVar("select_class") == null)
		{
			if (activeChar.getClassId() == ClassId.duelist || activeChar.getClassId() == ClassId.dreadnought || activeChar.getClassId() == ClassId.phoenixKnight || activeChar.getClassId() == ClassId.hellKnight || activeChar.getClassId() == ClassId.sagittarius || activeChar.getClassId() == ClassId.adventurer || activeChar.getClassId() == ClassId.archmage || activeChar.getClassId() == ClassId.soultaker || activeChar.getClassId() == ClassId.arcanaLord || activeChar.getClassId() == ClassId.cardinal || activeChar.getClassId() == ClassId.hierophant || activeChar.getClassId() == ClassId.evaTemplar || activeChar.getClassId() == ClassId.swordMuse || activeChar.getClassId() == ClassId.windRider || activeChar.getClassId() == ClassId.moonlightSentinel || activeChar.getClassId() == ClassId.mysticMuse || activeChar.getClassId() == ClassId.elementalMaster || activeChar.getClassId() == ClassId.evaSaint || activeChar.getClassId() == ClassId.shillienTemplar || activeChar.getClassId() == ClassId.spectralDancer || activeChar.getClassId() == ClassId.ghostHunter || activeChar.getClassId() == ClassId.ghostSentinel || activeChar.getClassId() == ClassId.stormScreamer || activeChar.getClassId() == ClassId.spectralMaster || activeChar.getClassId() == ClassId.shillienSaint || activeChar.getClassId() == ClassId.titan || activeChar.getClassId() == ClassId.grandKhauatari || activeChar.getClassId() == ClassId.dominator || activeChar.getClassId() == ClassId.doomcryer || activeChar.getClassId() == ClassId.fortuneSeeker || activeChar.getClassId() == ClassId.maestro)
			{
				Util.handleIllegalPlayerAction(activeChar, "StartupSystem: player [" + activeChar.getName() + "] trying to change class exploit.", 2);
				return;
			}

			if (activeChar.getVar("select_armor") == null && Config.STARTUP_SYSTEM_SELECTARMOR)
			{
			SelectArmor(activeChar);
			}
			else if (activeChar.getVar("select_armor") != null || !Config.STARTUP_SYSTEM_SELECTARMOR)
			{
				if (Config.STARTUP_SYSTEM_SELECTWEAP && activeChar.getVar("select_weapon") == null)
					SelectWeapon(activeChar);
				else
					endStartup(activeChar);
			}

			activeChar.setClassId(101);
			activeChar.setVar("select_class", "1");
			activeChar.broadcastUserInfo();
			activeChar.setBaseClass(activeChar.getActiveClass());
			activeChar.store();

			if (Config.CHECK_SKILLS_ON_ENTER)
				activeChar.checkAllowedSkills();
		}
		else if (_command.startsWith("Moonlight_Sentinel") && activeChar.getVar("select_class") == null)
		{
			if (activeChar.getClassId() == ClassId.duelist || activeChar.getClassId() == ClassId.dreadnought || activeChar.getClassId() == ClassId.phoenixKnight || activeChar.getClassId() == ClassId.hellKnight || activeChar.getClassId() == ClassId.sagittarius || activeChar.getClassId() == ClassId.adventurer || activeChar.getClassId() == ClassId.archmage || activeChar.getClassId() == ClassId.soultaker || activeChar.getClassId() == ClassId.arcanaLord || activeChar.getClassId() == ClassId.cardinal || activeChar.getClassId() == ClassId.hierophant || activeChar.getClassId() == ClassId.evaTemplar || activeChar.getClassId() == ClassId.swordMuse || activeChar.getClassId() == ClassId.windRider || activeChar.getClassId() == ClassId.moonlightSentinel || activeChar.getClassId() == ClassId.mysticMuse || activeChar.getClassId() == ClassId.elementalMaster || activeChar.getClassId() == ClassId.evaSaint || activeChar.getClassId() == ClassId.shillienTemplar || activeChar.getClassId() == ClassId.spectralDancer || activeChar.getClassId() == ClassId.ghostHunter || activeChar.getClassId() == ClassId.ghostSentinel || activeChar.getClassId() == ClassId.stormScreamer || activeChar.getClassId() == ClassId.spectralMaster || activeChar.getClassId() == ClassId.shillienSaint || activeChar.getClassId() == ClassId.titan || activeChar.getClassId() == ClassId.grandKhauatari || activeChar.getClassId() == ClassId.dominator || activeChar.getClassId() == ClassId.doomcryer || activeChar.getClassId() == ClassId.fortuneSeeker || activeChar.getClassId() == ClassId.maestro)
			{
				Util.handleIllegalPlayerAction(activeChar, "StartupSystem: player [" + activeChar.getName() + "] trying to change class exploit.", 2);
				return;
			}

			if (activeChar.getVar("select_armor") == null && Config.STARTUP_SYSTEM_SELECTARMOR)
			{
			SelectArmor(activeChar);
			}
			else if (activeChar.getVar("select_armor") != null || !Config.STARTUP_SYSTEM_SELECTARMOR)
			{
				if (Config.STARTUP_SYSTEM_SELECTWEAP && activeChar.getVar("select_weapon") == null)
					SelectWeapon(activeChar);
				else
					endStartup(activeChar);
			}

			activeChar.setClassId(102);
			activeChar.setVar("select_class", "1");
			activeChar.broadcastUserInfo();
			activeChar.setBaseClass(activeChar.getActiveClass());
			activeChar.store();

			if (Config.CHECK_SKILLS_ON_ENTER)
				activeChar.checkAllowedSkills();
		}
		// Elven Mystic
		else if (_command.startsWith("Mystic_Muse") && activeChar.getVar("select_class") == null)
		{
			if (activeChar.getClassId() == ClassId.duelist || activeChar.getClassId() == ClassId.dreadnought || activeChar.getClassId() == ClassId.phoenixKnight || activeChar.getClassId() == ClassId.hellKnight || activeChar.getClassId() == ClassId.sagittarius || activeChar.getClassId() == ClassId.adventurer || activeChar.getClassId() == ClassId.archmage || activeChar.getClassId() == ClassId.soultaker || activeChar.getClassId() == ClassId.arcanaLord || activeChar.getClassId() == ClassId.cardinal || activeChar.getClassId() == ClassId.hierophant || activeChar.getClassId() == ClassId.evaTemplar || activeChar.getClassId() == ClassId.swordMuse || activeChar.getClassId() == ClassId.windRider || activeChar.getClassId() == ClassId.moonlightSentinel || activeChar.getClassId() == ClassId.mysticMuse || activeChar.getClassId() == ClassId.elementalMaster || activeChar.getClassId() == ClassId.evaSaint || activeChar.getClassId() == ClassId.shillienTemplar || activeChar.getClassId() == ClassId.spectralDancer || activeChar.getClassId() == ClassId.ghostHunter || activeChar.getClassId() == ClassId.ghostSentinel || activeChar.getClassId() == ClassId.stormScreamer || activeChar.getClassId() == ClassId.spectralMaster || activeChar.getClassId() == ClassId.shillienSaint || activeChar.getClassId() == ClassId.titan || activeChar.getClassId() == ClassId.grandKhauatari || activeChar.getClassId() == ClassId.dominator || activeChar.getClassId() == ClassId.doomcryer || activeChar.getClassId() == ClassId.fortuneSeeker || activeChar.getClassId() == ClassId.maestro)
			{
				Util.handleIllegalPlayerAction(activeChar, "StartupSystem: player [" + activeChar.getName() + "] trying to change class exploit.", 2);
				return;
			}

			if (activeChar.getVar("select_armor") == null && Config.STARTUP_SYSTEM_SELECTARMOR)
			{
			SelectArmor(activeChar);
			}
			else if (activeChar.getVar("select_armor") != null || !Config.STARTUP_SYSTEM_SELECTARMOR)
			{
				if (Config.STARTUP_SYSTEM_SELECTWEAP && activeChar.getVar("select_weapon") == null)
					SelectWeapon(activeChar);
				else
					endStartup(activeChar);
			}

			activeChar.setClassId(103);
			activeChar.setVar("select_class", "1");
			activeChar.broadcastUserInfo();
			activeChar.setBaseClass(activeChar.getActiveClass());
			activeChar.store();

			if (Config.CHECK_SKILLS_ON_ENTER)
				activeChar.checkAllowedSkills();
		}
		else if (_command.startsWith("Elemental_Master") && activeChar.getVar("select_class") == null)
		{
			if (activeChar.getClassId() == ClassId.duelist || activeChar.getClassId() == ClassId.dreadnought || activeChar.getClassId() == ClassId.phoenixKnight || activeChar.getClassId() == ClassId.hellKnight || activeChar.getClassId() == ClassId.sagittarius || activeChar.getClassId() == ClassId.adventurer || activeChar.getClassId() == ClassId.archmage || activeChar.getClassId() == ClassId.soultaker || activeChar.getClassId() == ClassId.arcanaLord || activeChar.getClassId() == ClassId.cardinal || activeChar.getClassId() == ClassId.hierophant || activeChar.getClassId() == ClassId.evaTemplar || activeChar.getClassId() == ClassId.swordMuse || activeChar.getClassId() == ClassId.windRider || activeChar.getClassId() == ClassId.moonlightSentinel || activeChar.getClassId() == ClassId.mysticMuse || activeChar.getClassId() == ClassId.elementalMaster || activeChar.getClassId() == ClassId.evaSaint || activeChar.getClassId() == ClassId.shillienTemplar || activeChar.getClassId() == ClassId.spectralDancer || activeChar.getClassId() == ClassId.ghostHunter || activeChar.getClassId() == ClassId.ghostSentinel || activeChar.getClassId() == ClassId.stormScreamer || activeChar.getClassId() == ClassId.spectralMaster || activeChar.getClassId() == ClassId.shillienSaint || activeChar.getClassId() == ClassId.titan || activeChar.getClassId() == ClassId.grandKhauatari || activeChar.getClassId() == ClassId.dominator || activeChar.getClassId() == ClassId.doomcryer || activeChar.getClassId() == ClassId.fortuneSeeker || activeChar.getClassId() == ClassId.maestro)
			{
				Util.handleIllegalPlayerAction(activeChar, "StartupSystem: player [" + activeChar.getName() + "] trying to change class exploit.", 2);
				return;
			}

			if (activeChar.getVar("select_armor") == null && Config.STARTUP_SYSTEM_SELECTARMOR)
			{
			SelectArmor(activeChar);
			}
			else if (activeChar.getVar("select_armor") != null || !Config.STARTUP_SYSTEM_SELECTARMOR)
			{
				if (Config.STARTUP_SYSTEM_SELECTWEAP && activeChar.getVar("select_weapon") == null)
					SelectWeapon(activeChar);
				else
					endStartup(activeChar);
			}

			activeChar.setClassId(104);
			activeChar.setVar("select_class", "1");
			activeChar.broadcastUserInfo();
			activeChar.setBaseClass(activeChar.getActiveClass());
			activeChar.store();

			if (Config.CHECK_SKILLS_ON_ENTER)
				activeChar.checkAllowedSkills();
		}
		else if (_command.startsWith("Eva_Saint") && activeChar.getVar("select_class") == null)
		{
			if (activeChar.getClassId() == ClassId.duelist || activeChar.getClassId() == ClassId.dreadnought || activeChar.getClassId() == ClassId.phoenixKnight || activeChar.getClassId() == ClassId.hellKnight || activeChar.getClassId() == ClassId.sagittarius || activeChar.getClassId() == ClassId.adventurer || activeChar.getClassId() == ClassId.archmage || activeChar.getClassId() == ClassId.soultaker || activeChar.getClassId() == ClassId.arcanaLord || activeChar.getClassId() == ClassId.cardinal || activeChar.getClassId() == ClassId.hierophant || activeChar.getClassId() == ClassId.evaTemplar || activeChar.getClassId() == ClassId.swordMuse || activeChar.getClassId() == ClassId.windRider || activeChar.getClassId() == ClassId.moonlightSentinel || activeChar.getClassId() == ClassId.mysticMuse || activeChar.getClassId() == ClassId.elementalMaster || activeChar.getClassId() == ClassId.evaSaint || activeChar.getClassId() == ClassId.shillienTemplar || activeChar.getClassId() == ClassId.spectralDancer || activeChar.getClassId() == ClassId.ghostHunter || activeChar.getClassId() == ClassId.ghostSentinel || activeChar.getClassId() == ClassId.stormScreamer || activeChar.getClassId() == ClassId.spectralMaster || activeChar.getClassId() == ClassId.shillienSaint || activeChar.getClassId() == ClassId.titan || activeChar.getClassId() == ClassId.grandKhauatari || activeChar.getClassId() == ClassId.dominator || activeChar.getClassId() == ClassId.doomcryer || activeChar.getClassId() == ClassId.fortuneSeeker || activeChar.getClassId() == ClassId.maestro)
			{
				Util.handleIllegalPlayerAction(activeChar, "StartupSystem: player [" + activeChar.getName() + "] trying to change class exploit.", 2);
				return;
			}

			if (activeChar.getVar("select_armor") == null && Config.STARTUP_SYSTEM_SELECTARMOR)
			{
			SelectArmor(activeChar);
			}
			else if (activeChar.getVar("select_armor") != null || !Config.STARTUP_SYSTEM_SELECTARMOR)
			{
				if (Config.STARTUP_SYSTEM_SELECTWEAP && activeChar.getVar("select_weapon") == null)
					SelectWeapon(activeChar);
				else
					endStartup(activeChar);
			}

			activeChar.setClassId(105);
			activeChar.setVar("select_class", "1");
			activeChar.broadcastUserInfo();
			activeChar.setBaseClass(activeChar.getActiveClass());
			activeChar.store();

			if (Config.CHECK_SKILLS_ON_ENTER)
				activeChar.checkAllowedSkills();
		}
		// Dark Fighter
		else if (_command.startsWith("Shillien_Templar") && activeChar.getVar("select_class") == null)
		{
			if (activeChar.getClassId() == ClassId.duelist || activeChar.getClassId() == ClassId.dreadnought || activeChar.getClassId() == ClassId.phoenixKnight || activeChar.getClassId() == ClassId.hellKnight || activeChar.getClassId() == ClassId.sagittarius || activeChar.getClassId() == ClassId.adventurer || activeChar.getClassId() == ClassId.archmage || activeChar.getClassId() == ClassId.soultaker || activeChar.getClassId() == ClassId.arcanaLord || activeChar.getClassId() == ClassId.cardinal || activeChar.getClassId() == ClassId.hierophant || activeChar.getClassId() == ClassId.evaTemplar || activeChar.getClassId() == ClassId.swordMuse || activeChar.getClassId() == ClassId.windRider || activeChar.getClassId() == ClassId.moonlightSentinel || activeChar.getClassId() == ClassId.mysticMuse || activeChar.getClassId() == ClassId.elementalMaster || activeChar.getClassId() == ClassId.evaSaint || activeChar.getClassId() == ClassId.shillienTemplar || activeChar.getClassId() == ClassId.spectralDancer || activeChar.getClassId() == ClassId.ghostHunter || activeChar.getClassId() == ClassId.ghostSentinel || activeChar.getClassId() == ClassId.stormScreamer || activeChar.getClassId() == ClassId.spectralMaster || activeChar.getClassId() == ClassId.shillienSaint || activeChar.getClassId() == ClassId.titan || activeChar.getClassId() == ClassId.grandKhauatari || activeChar.getClassId() == ClassId.dominator || activeChar.getClassId() == ClassId.doomcryer || activeChar.getClassId() == ClassId.fortuneSeeker || activeChar.getClassId() == ClassId.maestro)
			{
				Util.handleIllegalPlayerAction(activeChar, "StartupSystem: player [" + activeChar.getName() + "] trying to change class exploit.", 2);
				return;
			}

			if (activeChar.getVar("select_armor") == null && Config.STARTUP_SYSTEM_SELECTARMOR)
			{
			SelectArmor(activeChar);
			}
			else if (activeChar.getVar("select_armor") != null || !Config.STARTUP_SYSTEM_SELECTARMOR)
			{
				if (Config.STARTUP_SYSTEM_SELECTWEAP && activeChar.getVar("select_weapon") == null)
					SelectWeapon(activeChar);
				else
					endStartup(activeChar);
			}

			activeChar.setClassId(106);
			activeChar.setVar("select_class", "1");
			activeChar.broadcastUserInfo();
			activeChar.setBaseClass(activeChar.getActiveClass());
			activeChar.store();

			if (Config.CHECK_SKILLS_ON_ENTER)
				activeChar.checkAllowedSkills();
		}
		else if (_command.startsWith("Spectral_Dancer") && activeChar.getVar("select_class") == null)
		{
			if (activeChar.getClassId() == ClassId.duelist || activeChar.getClassId() == ClassId.dreadnought || activeChar.getClassId() == ClassId.phoenixKnight || activeChar.getClassId() == ClassId.hellKnight || activeChar.getClassId() == ClassId.sagittarius || activeChar.getClassId() == ClassId.adventurer || activeChar.getClassId() == ClassId.archmage || activeChar.getClassId() == ClassId.soultaker || activeChar.getClassId() == ClassId.arcanaLord || activeChar.getClassId() == ClassId.cardinal || activeChar.getClassId() == ClassId.hierophant || activeChar.getClassId() == ClassId.evaTemplar || activeChar.getClassId() == ClassId.swordMuse || activeChar.getClassId() == ClassId.windRider || activeChar.getClassId() == ClassId.moonlightSentinel || activeChar.getClassId() == ClassId.mysticMuse || activeChar.getClassId() == ClassId.elementalMaster || activeChar.getClassId() == ClassId.evaSaint || activeChar.getClassId() == ClassId.shillienTemplar || activeChar.getClassId() == ClassId.spectralDancer || activeChar.getClassId() == ClassId.ghostHunter || activeChar.getClassId() == ClassId.ghostSentinel || activeChar.getClassId() == ClassId.stormScreamer || activeChar.getClassId() == ClassId.spectralMaster || activeChar.getClassId() == ClassId.shillienSaint || activeChar.getClassId() == ClassId.titan || activeChar.getClassId() == ClassId.grandKhauatari || activeChar.getClassId() == ClassId.dominator || activeChar.getClassId() == ClassId.doomcryer || activeChar.getClassId() == ClassId.fortuneSeeker || activeChar.getClassId() == ClassId.maestro)
			{
				Util.handleIllegalPlayerAction(activeChar, "StartupSystem: player [" + activeChar.getName() + "] trying to change class exploit.", 2);
				return;
			}

			if (activeChar.getVar("select_armor") == null && Config.STARTUP_SYSTEM_SELECTARMOR)
			{
			SelectArmor(activeChar);
			}
			else if (activeChar.getVar("select_armor") != null || !Config.STARTUP_SYSTEM_SELECTARMOR)
			{
				if (Config.STARTUP_SYSTEM_SELECTWEAP && activeChar.getVar("select_weapon") == null)
					SelectWeapon(activeChar);
				else
					endStartup(activeChar);
			}

			activeChar.setClassId(107);
			activeChar.setVar("select_class", "1");
			activeChar.broadcastUserInfo();
			activeChar.setBaseClass(activeChar.getActiveClass());
			activeChar.store();

			if (Config.CHECK_SKILLS_ON_ENTER)
				activeChar.checkAllowedSkills();
		}
		else if (_command.startsWith("Ghost_Hunter") && activeChar.getVar("select_class") == null)
		{
			if (activeChar.getClassId() == ClassId.duelist || activeChar.getClassId() == ClassId.dreadnought || activeChar.getClassId() == ClassId.phoenixKnight || activeChar.getClassId() == ClassId.hellKnight || activeChar.getClassId() == ClassId.sagittarius || activeChar.getClassId() == ClassId.adventurer || activeChar.getClassId() == ClassId.archmage || activeChar.getClassId() == ClassId.soultaker || activeChar.getClassId() == ClassId.arcanaLord || activeChar.getClassId() == ClassId.cardinal || activeChar.getClassId() == ClassId.hierophant || activeChar.getClassId() == ClassId.evaTemplar || activeChar.getClassId() == ClassId.swordMuse || activeChar.getClassId() == ClassId.windRider || activeChar.getClassId() == ClassId.moonlightSentinel || activeChar.getClassId() == ClassId.mysticMuse || activeChar.getClassId() == ClassId.elementalMaster || activeChar.getClassId() == ClassId.evaSaint || activeChar.getClassId() == ClassId.shillienTemplar || activeChar.getClassId() == ClassId.spectralDancer || activeChar.getClassId() == ClassId.ghostHunter || activeChar.getClassId() == ClassId.ghostSentinel || activeChar.getClassId() == ClassId.stormScreamer || activeChar.getClassId() == ClassId.spectralMaster || activeChar.getClassId() == ClassId.shillienSaint || activeChar.getClassId() == ClassId.titan || activeChar.getClassId() == ClassId.grandKhauatari || activeChar.getClassId() == ClassId.dominator || activeChar.getClassId() == ClassId.doomcryer || activeChar.getClassId() == ClassId.fortuneSeeker || activeChar.getClassId() == ClassId.maestro)
			{
				Util.handleIllegalPlayerAction(activeChar, "StartupSystem: player [" + activeChar.getName() + "] trying to change class exploit.", 2);
				return;
			}

			if (activeChar.getVar("select_armor") == null && Config.STARTUP_SYSTEM_SELECTARMOR)
			{
			SelectArmor(activeChar);
			}
			else if (activeChar.getVar("select_armor") != null || !Config.STARTUP_SYSTEM_SELECTARMOR)
			{
				if (Config.STARTUP_SYSTEM_SELECTWEAP && activeChar.getVar("select_weapon") == null)
					SelectWeapon(activeChar);
				else
					endStartup(activeChar);
			}

			activeChar.setClassId(108);
			activeChar.setVar("select_class", "1");
			activeChar.broadcastUserInfo();
			activeChar.setBaseClass(activeChar.getActiveClass());
			activeChar.store();

			if (Config.CHECK_SKILLS_ON_ENTER)
				activeChar.checkAllowedSkills();
		}
		else if (_command.startsWith("Ghost_Sentinel") && activeChar.getVar("select_class") == null)
		{
			if (activeChar.getClassId() == ClassId.duelist || activeChar.getClassId() == ClassId.dreadnought || activeChar.getClassId() == ClassId.phoenixKnight || activeChar.getClassId() == ClassId.hellKnight || activeChar.getClassId() == ClassId.sagittarius || activeChar.getClassId() == ClassId.adventurer || activeChar.getClassId() == ClassId.archmage || activeChar.getClassId() == ClassId.soultaker || activeChar.getClassId() == ClassId.arcanaLord || activeChar.getClassId() == ClassId.cardinal || activeChar.getClassId() == ClassId.hierophant || activeChar.getClassId() == ClassId.evaTemplar || activeChar.getClassId() == ClassId.swordMuse || activeChar.getClassId() == ClassId.windRider || activeChar.getClassId() == ClassId.moonlightSentinel || activeChar.getClassId() == ClassId.mysticMuse || activeChar.getClassId() == ClassId.elementalMaster || activeChar.getClassId() == ClassId.evaSaint || activeChar.getClassId() == ClassId.shillienTemplar || activeChar.getClassId() == ClassId.spectralDancer || activeChar.getClassId() == ClassId.ghostHunter || activeChar.getClassId() == ClassId.ghostSentinel || activeChar.getClassId() == ClassId.stormScreamer || activeChar.getClassId() == ClassId.spectralMaster || activeChar.getClassId() == ClassId.shillienSaint || activeChar.getClassId() == ClassId.titan || activeChar.getClassId() == ClassId.grandKhauatari || activeChar.getClassId() == ClassId.dominator || activeChar.getClassId() == ClassId.doomcryer || activeChar.getClassId() == ClassId.fortuneSeeker || activeChar.getClassId() == ClassId.maestro)
			{
				Util.handleIllegalPlayerAction(activeChar, "StartupSystem: player [" + activeChar.getName() + "] trying to change class exploit.", 2);
				return;
			}

			if (activeChar.getVar("select_armor") == null && Config.STARTUP_SYSTEM_SELECTARMOR)
			{
			SelectArmor(activeChar);
			}
			else if (activeChar.getVar("select_armor") != null || !Config.STARTUP_SYSTEM_SELECTARMOR)
			{
				if (Config.STARTUP_SYSTEM_SELECTWEAP && activeChar.getVar("select_weapon") == null)
					SelectWeapon(activeChar);
				else
					endStartup(activeChar);
			}

			activeChar.setClassId(109);
			activeChar.setVar("select_class", "1");
			activeChar.broadcastUserInfo();
			activeChar.setBaseClass(activeChar.getActiveClass());
			activeChar.store();

			if (Config.CHECK_SKILLS_ON_ENTER)
				activeChar.checkAllowedSkills();
		}
		// Dark Mystic
		else if (_command.startsWith("Storm_Screamer") && activeChar.getVar("select_class") == null)
		{
			if (activeChar.getClassId() == ClassId.duelist || activeChar.getClassId() == ClassId.dreadnought || activeChar.getClassId() == ClassId.phoenixKnight || activeChar.getClassId() == ClassId.hellKnight || activeChar.getClassId() == ClassId.sagittarius || activeChar.getClassId() == ClassId.adventurer || activeChar.getClassId() == ClassId.archmage || activeChar.getClassId() == ClassId.soultaker || activeChar.getClassId() == ClassId.arcanaLord || activeChar.getClassId() == ClassId.cardinal || activeChar.getClassId() == ClassId.hierophant || activeChar.getClassId() == ClassId.evaTemplar || activeChar.getClassId() == ClassId.swordMuse || activeChar.getClassId() == ClassId.windRider || activeChar.getClassId() == ClassId.moonlightSentinel || activeChar.getClassId() == ClassId.mysticMuse || activeChar.getClassId() == ClassId.elementalMaster || activeChar.getClassId() == ClassId.evaSaint || activeChar.getClassId() == ClassId.shillienTemplar || activeChar.getClassId() == ClassId.spectralDancer || activeChar.getClassId() == ClassId.ghostHunter || activeChar.getClassId() == ClassId.ghostSentinel || activeChar.getClassId() == ClassId.stormScreamer || activeChar.getClassId() == ClassId.spectralMaster || activeChar.getClassId() == ClassId.shillienSaint || activeChar.getClassId() == ClassId.titan || activeChar.getClassId() == ClassId.grandKhauatari || activeChar.getClassId() == ClassId.dominator || activeChar.getClassId() == ClassId.doomcryer || activeChar.getClassId() == ClassId.fortuneSeeker || activeChar.getClassId() == ClassId.maestro)
			{
				Util.handleIllegalPlayerAction(activeChar, "StartupSystem: player [" + activeChar.getName() + "] trying to change class exploit.", 2);
				return;
			}

			if (activeChar.getVar("select_armor") == null && Config.STARTUP_SYSTEM_SELECTARMOR)
			{
			SelectArmor(activeChar);
			}
			else if (activeChar.getVar("select_armor") != null || !Config.STARTUP_SYSTEM_SELECTARMOR)
			{
				if (Config.STARTUP_SYSTEM_SELECTWEAP && activeChar.getVar("select_weapon") == null)
					SelectWeapon(activeChar);
				else
					endStartup(activeChar);
			}

			activeChar.setClassId(110);
			activeChar.setVar("select_class", "1");
			activeChar.broadcastUserInfo();
			activeChar.setBaseClass(activeChar.getActiveClass());
			activeChar.store();

			if (Config.CHECK_SKILLS_ON_ENTER)
				activeChar.checkAllowedSkills();
		}
		else if (_command.startsWith("Spectral_Master") && activeChar.getVar("select_class") == null)
		{
			if (activeChar.getClassId() == ClassId.duelist || activeChar.getClassId() == ClassId.dreadnought || activeChar.getClassId() == ClassId.phoenixKnight || activeChar.getClassId() == ClassId.hellKnight || activeChar.getClassId() == ClassId.sagittarius || activeChar.getClassId() == ClassId.adventurer || activeChar.getClassId() == ClassId.archmage || activeChar.getClassId() == ClassId.soultaker || activeChar.getClassId() == ClassId.arcanaLord || activeChar.getClassId() == ClassId.cardinal || activeChar.getClassId() == ClassId.hierophant || activeChar.getClassId() == ClassId.evaTemplar || activeChar.getClassId() == ClassId.swordMuse || activeChar.getClassId() == ClassId.windRider || activeChar.getClassId() == ClassId.moonlightSentinel || activeChar.getClassId() == ClassId.mysticMuse || activeChar.getClassId() == ClassId.elementalMaster || activeChar.getClassId() == ClassId.evaSaint || activeChar.getClassId() == ClassId.shillienTemplar || activeChar.getClassId() == ClassId.spectralDancer || activeChar.getClassId() == ClassId.ghostHunter || activeChar.getClassId() == ClassId.ghostSentinel || activeChar.getClassId() == ClassId.stormScreamer || activeChar.getClassId() == ClassId.spectralMaster || activeChar.getClassId() == ClassId.shillienSaint || activeChar.getClassId() == ClassId.titan || activeChar.getClassId() == ClassId.grandKhauatari || activeChar.getClassId() == ClassId.dominator || activeChar.getClassId() == ClassId.doomcryer || activeChar.getClassId() == ClassId.fortuneSeeker || activeChar.getClassId() == ClassId.maestro)
			{
				Util.handleIllegalPlayerAction(activeChar, "StartupSystem: player [" + activeChar.getName() + "] trying to change class exploit.", 2);
				return;
			}

			if (activeChar.getVar("select_armor") == null && Config.STARTUP_SYSTEM_SELECTARMOR)
			{
			SelectArmor(activeChar);
			}
			else if (activeChar.getVar("select_armor") != null || !Config.STARTUP_SYSTEM_SELECTARMOR)
			{
				if (Config.STARTUP_SYSTEM_SELECTWEAP && activeChar.getVar("select_weapon") == null)
					SelectWeapon(activeChar);
				else
					endStartup(activeChar);
			}

			activeChar.setClassId(111);
			activeChar.setVar("select_class", "1");
			activeChar.broadcastUserInfo();
			activeChar.setBaseClass(activeChar.getActiveClass());
			activeChar.store();

			if (Config.CHECK_SKILLS_ON_ENTER)
				activeChar.checkAllowedSkills();
		}
		else if (_command.startsWith("Shillen_Saint") && activeChar.getVar("select_class") == null)
		{
			if (activeChar.getClassId() == ClassId.duelist || activeChar.getClassId() == ClassId.dreadnought || activeChar.getClassId() == ClassId.phoenixKnight || activeChar.getClassId() == ClassId.hellKnight || activeChar.getClassId() == ClassId.sagittarius || activeChar.getClassId() == ClassId.adventurer || activeChar.getClassId() == ClassId.archmage || activeChar.getClassId() == ClassId.soultaker || activeChar.getClassId() == ClassId.arcanaLord || activeChar.getClassId() == ClassId.cardinal || activeChar.getClassId() == ClassId.hierophant || activeChar.getClassId() == ClassId.evaTemplar || activeChar.getClassId() == ClassId.swordMuse || activeChar.getClassId() == ClassId.windRider || activeChar.getClassId() == ClassId.moonlightSentinel || activeChar.getClassId() == ClassId.mysticMuse || activeChar.getClassId() == ClassId.elementalMaster || activeChar.getClassId() == ClassId.evaSaint || activeChar.getClassId() == ClassId.shillienTemplar || activeChar.getClassId() == ClassId.spectralDancer || activeChar.getClassId() == ClassId.ghostHunter || activeChar.getClassId() == ClassId.ghostSentinel || activeChar.getClassId() == ClassId.stormScreamer || activeChar.getClassId() == ClassId.spectralMaster || activeChar.getClassId() == ClassId.shillienSaint || activeChar.getClassId() == ClassId.titan || activeChar.getClassId() == ClassId.grandKhauatari || activeChar.getClassId() == ClassId.dominator || activeChar.getClassId() == ClassId.doomcryer || activeChar.getClassId() == ClassId.fortuneSeeker || activeChar.getClassId() == ClassId.maestro)
			{
				Util.handleIllegalPlayerAction(activeChar, "StartupSystem: player [" + activeChar.getName() + "] trying to change class exploit.", 2);
				return;
			}

			if (activeChar.getVar("select_armor") == null && Config.STARTUP_SYSTEM_SELECTARMOR)
			{
			SelectArmor(activeChar);
			}
			else if (activeChar.getVar("select_armor") != null || !Config.STARTUP_SYSTEM_SELECTARMOR)
			{
				if (Config.STARTUP_SYSTEM_SELECTWEAP && activeChar.getVar("select_weapon") == null)
					SelectWeapon(activeChar);
				else
					endStartup(activeChar);
			}

			activeChar.setClassId(112);
			activeChar.setVar("select_class", "1");
			activeChar.broadcastUserInfo();
			activeChar.setBaseClass(activeChar.getActiveClass());
			activeChar.store();

			if (Config.CHECK_SKILLS_ON_ENTER)
				activeChar.checkAllowedSkills();
		}
		// Orc Fighter
		else if (_command.startsWith("Titan") && activeChar.getVar("select_class") == null)
		{
			if (activeChar.getClassId() == ClassId.duelist || activeChar.getClassId() == ClassId.dreadnought || activeChar.getClassId() == ClassId.phoenixKnight || activeChar.getClassId() == ClassId.hellKnight || activeChar.getClassId() == ClassId.sagittarius || activeChar.getClassId() == ClassId.adventurer || activeChar.getClassId() == ClassId.archmage || activeChar.getClassId() == ClassId.soultaker || activeChar.getClassId() == ClassId.arcanaLord || activeChar.getClassId() == ClassId.cardinal || activeChar.getClassId() == ClassId.hierophant || activeChar.getClassId() == ClassId.evaTemplar || activeChar.getClassId() == ClassId.swordMuse || activeChar.getClassId() == ClassId.windRider || activeChar.getClassId() == ClassId.moonlightSentinel || activeChar.getClassId() == ClassId.mysticMuse || activeChar.getClassId() == ClassId.elementalMaster || activeChar.getClassId() == ClassId.evaSaint || activeChar.getClassId() == ClassId.shillienTemplar || activeChar.getClassId() == ClassId.spectralDancer || activeChar.getClassId() == ClassId.ghostHunter || activeChar.getClassId() == ClassId.ghostSentinel || activeChar.getClassId() == ClassId.stormScreamer || activeChar.getClassId() == ClassId.spectralMaster || activeChar.getClassId() == ClassId.shillienSaint || activeChar.getClassId() == ClassId.titan || activeChar.getClassId() == ClassId.grandKhauatari || activeChar.getClassId() == ClassId.dominator || activeChar.getClassId() == ClassId.doomcryer || activeChar.getClassId() == ClassId.fortuneSeeker || activeChar.getClassId() == ClassId.maestro)
			{
				Util.handleIllegalPlayerAction(activeChar, "StartupSystem: player [" + activeChar.getName() + "] trying to change class exploit.", 2);
				return;
			}

			if (activeChar.getVar("select_armor") == null && Config.STARTUP_SYSTEM_SELECTARMOR)
			{
			SelectArmor(activeChar);
			}
			else if (activeChar.getVar("select_armor") != null || !Config.STARTUP_SYSTEM_SELECTARMOR)
			{
				if (Config.STARTUP_SYSTEM_SELECTWEAP && activeChar.getVar("select_weapon") == null)
					SelectWeapon(activeChar);
				else
					endStartup(activeChar);
			}

			activeChar.setClassId(113);
			activeChar.setVar("select_class", "1");
			activeChar.broadcastUserInfo();
			activeChar.setBaseClass(activeChar.getActiveClass());
			activeChar.store();

			if (Config.CHECK_SKILLS_ON_ENTER)
				activeChar.checkAllowedSkills();
		}
		else if (_command.startsWith("Grand_Khauatari") && activeChar.getVar("select_class") == null)
		{
			if (activeChar.getClassId() == ClassId.duelist || activeChar.getClassId() == ClassId.dreadnought || activeChar.getClassId() == ClassId.phoenixKnight || activeChar.getClassId() == ClassId.hellKnight || activeChar.getClassId() == ClassId.sagittarius || activeChar.getClassId() == ClassId.adventurer || activeChar.getClassId() == ClassId.archmage || activeChar.getClassId() == ClassId.soultaker || activeChar.getClassId() == ClassId.arcanaLord || activeChar.getClassId() == ClassId.cardinal || activeChar.getClassId() == ClassId.hierophant || activeChar.getClassId() == ClassId.evaTemplar || activeChar.getClassId() == ClassId.swordMuse || activeChar.getClassId() == ClassId.windRider || activeChar.getClassId() == ClassId.moonlightSentinel || activeChar.getClassId() == ClassId.mysticMuse || activeChar.getClassId() == ClassId.elementalMaster || activeChar.getClassId() == ClassId.evaSaint || activeChar.getClassId() == ClassId.shillienTemplar || activeChar.getClassId() == ClassId.spectralDancer || activeChar.getClassId() == ClassId.ghostHunter || activeChar.getClassId() == ClassId.ghostSentinel || activeChar.getClassId() == ClassId.stormScreamer || activeChar.getClassId() == ClassId.spectralMaster || activeChar.getClassId() == ClassId.shillienSaint || activeChar.getClassId() == ClassId.titan || activeChar.getClassId() == ClassId.grandKhauatari || activeChar.getClassId() == ClassId.dominator || activeChar.getClassId() == ClassId.doomcryer || activeChar.getClassId() == ClassId.fortuneSeeker || activeChar.getClassId() == ClassId.maestro)
			{
				Util.handleIllegalPlayerAction(activeChar, "StartupSystem: player [" + activeChar.getName() + "] trying to change class exploit.", 2);
				return;
			}

			if (activeChar.getVar("select_armor") == null && Config.STARTUP_SYSTEM_SELECTARMOR)
			{
			SelectArmor(activeChar);
			}
			else if (activeChar.getVar("select_armor") != null || !Config.STARTUP_SYSTEM_SELECTARMOR)
			{
				if (Config.STARTUP_SYSTEM_SELECTWEAP && activeChar.getVar("select_weapon") == null)
					SelectWeapon(activeChar);
				else
					endStartup(activeChar);
			}

			activeChar.setClassId(114);
			activeChar.setVar("select_class", "1");
			activeChar.broadcastUserInfo();
			activeChar.setBaseClass(activeChar.getActiveClass());
			activeChar.store();

			if (Config.CHECK_SKILLS_ON_ENTER)
				activeChar.checkAllowedSkills();
		}
		// Orc Mystic
		else if (_command.startsWith("Dominator") && activeChar.getVar("select_class") == null)
		{
			if (activeChar.getClassId() == ClassId.duelist || activeChar.getClassId() == ClassId.dreadnought || activeChar.getClassId() == ClassId.phoenixKnight || activeChar.getClassId() == ClassId.hellKnight || activeChar.getClassId() == ClassId.sagittarius || activeChar.getClassId() == ClassId.adventurer || activeChar.getClassId() == ClassId.archmage || activeChar.getClassId() == ClassId.soultaker || activeChar.getClassId() == ClassId.arcanaLord || activeChar.getClassId() == ClassId.cardinal || activeChar.getClassId() == ClassId.hierophant || activeChar.getClassId() == ClassId.evaTemplar || activeChar.getClassId() == ClassId.swordMuse || activeChar.getClassId() == ClassId.windRider || activeChar.getClassId() == ClassId.moonlightSentinel || activeChar.getClassId() == ClassId.mysticMuse || activeChar.getClassId() == ClassId.elementalMaster || activeChar.getClassId() == ClassId.evaSaint || activeChar.getClassId() == ClassId.shillienTemplar || activeChar.getClassId() == ClassId.spectralDancer || activeChar.getClassId() == ClassId.ghostHunter || activeChar.getClassId() == ClassId.ghostSentinel || activeChar.getClassId() == ClassId.stormScreamer || activeChar.getClassId() == ClassId.spectralMaster || activeChar.getClassId() == ClassId.shillienSaint || activeChar.getClassId() == ClassId.titan || activeChar.getClassId() == ClassId.grandKhauatari || activeChar.getClassId() == ClassId.dominator || activeChar.getClassId() == ClassId.doomcryer || activeChar.getClassId() == ClassId.fortuneSeeker || activeChar.getClassId() == ClassId.maestro)
			{
				Util.handleIllegalPlayerAction(activeChar, "StartupSystem: player [" + activeChar.getName() + "] trying to change class exploit.", 2);
				return;
			}

			if (activeChar.getVar("select_armor") == null && Config.STARTUP_SYSTEM_SELECTARMOR)
			{
			SelectArmor(activeChar);
			}
			else if (activeChar.getVar("select_armor") != null || !Config.STARTUP_SYSTEM_SELECTARMOR)
			{
				if (Config.STARTUP_SYSTEM_SELECTWEAP && activeChar.getVar("select_weapon") == null)
					SelectWeapon(activeChar);
				else
					endStartup(activeChar);
			}

			activeChar.setClassId(115);
			activeChar.setVar("select_class", "1");
			activeChar.broadcastUserInfo();
			activeChar.setBaseClass(activeChar.getActiveClass());
			activeChar.store();

			if (Config.CHECK_SKILLS_ON_ENTER)
				activeChar.checkAllowedSkills();
		}
		else if (_command.startsWith("Doomcryer") && activeChar.getVar("select_class") == null)
		{
			if (activeChar.getClassId() == ClassId.duelist || activeChar.getClassId() == ClassId.dreadnought || activeChar.getClassId() == ClassId.phoenixKnight || activeChar.getClassId() == ClassId.hellKnight || activeChar.getClassId() == ClassId.sagittarius || activeChar.getClassId() == ClassId.adventurer || activeChar.getClassId() == ClassId.archmage || activeChar.getClassId() == ClassId.soultaker || activeChar.getClassId() == ClassId.arcanaLord || activeChar.getClassId() == ClassId.cardinal || activeChar.getClassId() == ClassId.hierophant || activeChar.getClassId() == ClassId.evaTemplar || activeChar.getClassId() == ClassId.swordMuse || activeChar.getClassId() == ClassId.windRider || activeChar.getClassId() == ClassId.moonlightSentinel || activeChar.getClassId() == ClassId.mysticMuse || activeChar.getClassId() == ClassId.elementalMaster || activeChar.getClassId() == ClassId.evaSaint || activeChar.getClassId() == ClassId.shillienTemplar || activeChar.getClassId() == ClassId.spectralDancer || activeChar.getClassId() == ClassId.ghostHunter || activeChar.getClassId() == ClassId.ghostSentinel || activeChar.getClassId() == ClassId.stormScreamer || activeChar.getClassId() == ClassId.spectralMaster || activeChar.getClassId() == ClassId.shillienSaint || activeChar.getClassId() == ClassId.titan || activeChar.getClassId() == ClassId.grandKhauatari || activeChar.getClassId() == ClassId.dominator || activeChar.getClassId() == ClassId.doomcryer || activeChar.getClassId() == ClassId.fortuneSeeker || activeChar.getClassId() == ClassId.maestro)
			{
				Util.handleIllegalPlayerAction(activeChar, "StartupSystem: player [" + activeChar.getName() + "] trying to change class exploit.", 2);
				return;
			}

			if (activeChar.getVar("select_armor") == null && Config.STARTUP_SYSTEM_SELECTARMOR)
			{
			SelectArmor(activeChar);
			}
			else if (activeChar.getVar("select_armor") != null || !Config.STARTUP_SYSTEM_SELECTARMOR)
			{
				if (Config.STARTUP_SYSTEM_SELECTWEAP && activeChar.getVar("select_weapon") == null)
					SelectWeapon(activeChar);
				else
					endStartup(activeChar);
			}

			activeChar.setClassId(116);
			activeChar.setVar("select_class", "1");
			activeChar.broadcastUserInfo();
			activeChar.setBaseClass(activeChar.getActiveClass());
			activeChar.store();

			if (Config.CHECK_SKILLS_ON_ENTER)
				activeChar.checkAllowedSkills();
		}
		else if (_command.startsWith("Fortune_Seeker") && activeChar.getVar("select_class") == null)
		{
			if (activeChar.getClassId() == ClassId.duelist || activeChar.getClassId() == ClassId.dreadnought || activeChar.getClassId() == ClassId.phoenixKnight || activeChar.getClassId() == ClassId.hellKnight || activeChar.getClassId() == ClassId.sagittarius || activeChar.getClassId() == ClassId.adventurer || activeChar.getClassId() == ClassId.archmage || activeChar.getClassId() == ClassId.soultaker || activeChar.getClassId() == ClassId.arcanaLord || activeChar.getClassId() == ClassId.cardinal || activeChar.getClassId() == ClassId.hierophant || activeChar.getClassId() == ClassId.evaTemplar || activeChar.getClassId() == ClassId.swordMuse || activeChar.getClassId() == ClassId.windRider || activeChar.getClassId() == ClassId.moonlightSentinel || activeChar.getClassId() == ClassId.mysticMuse || activeChar.getClassId() == ClassId.elementalMaster || activeChar.getClassId() == ClassId.evaSaint || activeChar.getClassId() == ClassId.shillienTemplar || activeChar.getClassId() == ClassId.spectralDancer || activeChar.getClassId() == ClassId.ghostHunter || activeChar.getClassId() == ClassId.ghostSentinel || activeChar.getClassId() == ClassId.stormScreamer || activeChar.getClassId() == ClassId.spectralMaster || activeChar.getClassId() == ClassId.shillienSaint || activeChar.getClassId() == ClassId.titan || activeChar.getClassId() == ClassId.grandKhauatari || activeChar.getClassId() == ClassId.dominator || activeChar.getClassId() == ClassId.doomcryer || activeChar.getClassId() == ClassId.fortuneSeeker || activeChar.getClassId() == ClassId.maestro)
			{
				Util.handleIllegalPlayerAction(activeChar, "StartupSystem: player [" + activeChar.getName() + "] trying to change class exploit.", 2);
				return;
			}

			if (activeChar.getVar("select_armor") == null && Config.STARTUP_SYSTEM_SELECTARMOR)
			{
			SelectArmor(activeChar);
			}
			else if (activeChar.getVar("select_armor") != null || !Config.STARTUP_SYSTEM_SELECTARMOR)
			{
				if (Config.STARTUP_SYSTEM_SELECTWEAP && activeChar.getVar("select_weapon") == null)
					SelectWeapon(activeChar);
				else
					endStartup(activeChar);
			}

			activeChar.setClassId(117);
			activeChar.setVar("select_class", "1");
			activeChar.broadcastUserInfo();
			activeChar.setBaseClass(activeChar.getActiveClass());
			activeChar.store();

			if (Config.CHECK_SKILLS_ON_ENTER)
				activeChar.checkAllowedSkills();
		}
		else if (_command.startsWith("Maestro") && activeChar.getVar("select_class") == null)
		{
			if (activeChar.getClassId() == ClassId.duelist || activeChar.getClassId() == ClassId.dreadnought || activeChar.getClassId() == ClassId.phoenixKnight || activeChar.getClassId() == ClassId.hellKnight || activeChar.getClassId() == ClassId.sagittarius || activeChar.getClassId() == ClassId.adventurer || activeChar.getClassId() == ClassId.archmage || activeChar.getClassId() == ClassId.soultaker || activeChar.getClassId() == ClassId.arcanaLord || activeChar.getClassId() == ClassId.cardinal || activeChar.getClassId() == ClassId.hierophant || activeChar.getClassId() == ClassId.evaTemplar || activeChar.getClassId() == ClassId.swordMuse || activeChar.getClassId() == ClassId.windRider || activeChar.getClassId() == ClassId.moonlightSentinel || activeChar.getClassId() == ClassId.mysticMuse || activeChar.getClassId() == ClassId.elementalMaster || activeChar.getClassId() == ClassId.evaSaint || activeChar.getClassId() == ClassId.shillienTemplar || activeChar.getClassId() == ClassId.spectralDancer || activeChar.getClassId() == ClassId.ghostHunter || activeChar.getClassId() == ClassId.ghostSentinel || activeChar.getClassId() == ClassId.stormScreamer || activeChar.getClassId() == ClassId.spectralMaster || activeChar.getClassId() == ClassId.shillienSaint || activeChar.getClassId() == ClassId.titan || activeChar.getClassId() == ClassId.grandKhauatari || activeChar.getClassId() == ClassId.dominator || activeChar.getClassId() == ClassId.doomcryer || activeChar.getClassId() == ClassId.fortuneSeeker || activeChar.getClassId() == ClassId.maestro)
			{
				Util.handleIllegalPlayerAction(activeChar, "StartupSystem: player [" + activeChar.getName() + "] trying to change class exploit.", 2);
				return;
			}

			if (activeChar.getVar("select_armor") == null && Config.STARTUP_SYSTEM_SELECTARMOR)
			{
			SelectArmor(activeChar);
			}
			else if (activeChar.getVar("select_armor") != null || !Config.STARTUP_SYSTEM_SELECTARMOR)
			{
				if (Config.STARTUP_SYSTEM_SELECTWEAP && activeChar.getVar("select_weapon") == null)
					SelectWeapon(activeChar);
				else
					endStartup(activeChar);
			}

			activeChar.setClassId(118);
			activeChar.setVar("select_class", "1");
			activeChar.broadcastUserInfo();
			activeChar.setBaseClass(activeChar.getActiveClass());
			activeChar.store();

			if (Config.CHECK_SKILLS_ON_ENTER)
				activeChar.checkAllowedSkills();
		}
		// Armors
		if (_command.startsWith(Config.BYBASS_HEAVY_ITEMS))
		{
			if (Config.STARTUP_SYSTEM_SELECTWEAP)
			{
			SelectWeapon(activeChar);
			}
			else
				endStartup(activeChar);

			for (int[] reward : Config.SET_HEAVY_ITEMS)
			{
				L2ItemInstance PhewPew1 = activeChar.getInventory().addItem("Heavy Armor: ", reward[0], reward[1], activeChar, null);
				activeChar.getInventory().equipItemAndRecord(PhewPew1);
			}

			activeChar.setVar("select_armor", "1");
		}
		else if (_command.startsWith(Config.BYBASS_LIGHT_ITEMS))
		{
			if (Config.STARTUP_SYSTEM_SELECTWEAP)
			{
			SelectWeapon(activeChar);
			}
			else
				endStartup(activeChar);

			for (int[] reward : Config.SET_LIGHT_ITEMS)
			{
				L2ItemInstance PhewPew1 = activeChar.getInventory().addItem("Light Armor: ", reward[0], reward[1], activeChar, null);
				activeChar.getInventory().equipItemAndRecord(PhewPew1);
			}

			activeChar.setVar("select_armor", "1");
		}
		else if (_command.startsWith(Config.BYBASS_ROBE_ITEMS))
		{
			if (Config.STARTUP_SYSTEM_SELECTWEAP)
			{
			SelectWeapon(activeChar);
			}
			else
				endStartup(activeChar);

			for (int[] reward : Config.SET_ROBE_ITEMS)
			{
				L2ItemInstance PhewPew1 = activeChar.getInventory().addItem("Robe Armor: ", reward[0], reward[1], activeChar, null);
				activeChar.getInventory().equipItemAndRecord(PhewPew1);
			}

			activeChar.setVar("select_armor", "1");
		}
		// Weapons
		else if (_command.startsWith(Config.BYBASS_WP_01_ITEM) && activeChar.getVar("select_weapon") == null)
		{
			endStartup(activeChar);

			L2ItemInstance item = activeChar.getInventory().addItem("Weapon", Config.WP_01_ID, 1, activeChar, null);
			L2ItemInstance item1 = activeChar.getInventory().addItem("Shield", Config.WP_SHIELD, 1, activeChar, null);

			activeChar.getInventory().equipItemAndRecord(item);
			activeChar.getInventory().equipItemAndRecord(item1);

			activeChar.setVar("select_weapon", "1");
		}
		else if (_command.startsWith(Config.BYBASS_WP_02_ITEM) && activeChar.getVar("select_weapon") == null)
		{
			endStartup(activeChar);

			L2ItemInstance item = activeChar.getInventory().addItem("Weapon", Config.WP_02_ID, 1, activeChar, null);
			L2ItemInstance item1 = activeChar.getInventory().addItem("Shield", Config.WP_SHIELD, 1, activeChar, null);

			activeChar.getInventory().equipItemAndRecord(item);
			activeChar.getInventory().equipItemAndRecord(item1);

			activeChar.setVar("select_weapon", "1");
		}
		else if (_command.startsWith(Config.BYBASS_WP_03_ITEM) && activeChar.getVar("select_weapon") == null)
		{
			endStartup(activeChar);

			L2ItemInstance item = activeChar.getInventory().addItem("Weapon", Config.WP_03_ID, 1, activeChar, null);
			L2ItemInstance item1 = activeChar.getInventory().addItem("Shield", Config.WP_SHIELD, 1, activeChar, null);

			activeChar.getInventory().equipItemAndRecord(item);
			activeChar.getInventory().equipItemAndRecord(item1);

			activeChar.setVar("select_weapon", "1");
		}
		else if (_command.startsWith(Config.BYBASS_WP_04_ITEM) && activeChar.getVar("select_weapon") == null)
		{
			endStartup(activeChar);

			L2ItemInstance item = activeChar.getInventory().addItem("Weapon", Config.WP_04_ID, 1, activeChar, null);

			activeChar.getInventory().equipItemAndRecord(item);

			activeChar.setVar("select_weapon", "1");
		}
		else if (_command.startsWith(Config.BYBASS_WP_05_ITEM) && activeChar.getVar("select_weapon") == null)
		{
			endStartup(activeChar);

			L2ItemInstance item = activeChar.getInventory().addItem("Weapon", Config.WP_05_ID, 1, activeChar, null);

			activeChar.getInventory().equipItemAndRecord(item);

			activeChar.setVar("select_weapon", "1");
		}
		else if (_command.startsWith(Config.BYBASS_WP_06_ITEM) && activeChar.getVar("select_weapon") == null)
		{
			endStartup(activeChar);

			L2ItemInstance item = activeChar.getInventory().addItem("Weapon", Config.WP_06_ID, 1, activeChar, null);

			activeChar.getInventory().equipItemAndRecord(item);

			activeChar.setVar("select_weapon", "1");
		}
		else if (_command.startsWith(Config.BYBASS_WP_07_ITEM) && activeChar.getVar("select_weapon") == null)
		{
			endStartup(activeChar);

			L2ItemInstance item = activeChar.getInventory().addItem("Weapon", Config.WP_07_ID, 1, activeChar, null);
			L2ItemInstance item1 = activeChar.getInventory().addItem("Shield", Config.WP_SHIELD, 1, activeChar, null);

			activeChar.getInventory().equipItemAndRecord(item);
			activeChar.getInventory().equipItemAndRecord(item1);

			activeChar.setVar("select_weapon", "1");
		}
		else if (_command.startsWith(Config.BYBASS_WP_08_ITEM) && activeChar.getVar("select_weapon") == null)
		{
			endStartup(activeChar);

			L2ItemInstance item = activeChar.getInventory().addItem("Weapon", Config.WP_08_ID, 1, activeChar, null);
			L2ItemInstance item1 = activeChar.getInventory().addItem("Shield", Config.WP_SHIELD, 1, activeChar, null);

			activeChar.getInventory().equipItemAndRecord(item);
			activeChar.getInventory().equipItemAndRecord(item1);

			activeChar.setVar("select_weapon", "1");
		}
		else if (_command.startsWith(Config.BYBASS_WP_09_ITEM) && activeChar.getVar("select_weapon") == null)
		{
			endStartup(activeChar);

			L2ItemInstance item = activeChar.getInventory().addItem("Weapon", Config.WP_09_ID, 1, activeChar, null);
			L2ItemInstance item1 = activeChar.getInventory().addItem("Shield", Config.WP_SHIELD, 1, activeChar, null);

			activeChar.getInventory().equipItemAndRecord(item);
			activeChar.getInventory().equipItemAndRecord(item1);

			activeChar.setVar("select_weapon", "1");
		}
		else if (_command.startsWith(Config.BYBASS_WP_10_ITEM) && activeChar.getVar("select_weapon") == null)
		{
			endStartup(activeChar);

			L2ItemInstance item = activeChar.getInventory().addItem("Weapon", Config.WP_10_ID, 1, activeChar, null);

			activeChar.getInventory().equipItemAndRecord(item);

			activeChar.setVar("select_weapon", "1");
		}
		else if (_command.startsWith(Config.BYBASS_WP_11_ITEM) && activeChar.getVar("select_weapon") == null)
		{
			endStartup(activeChar);

			L2ItemInstance item = activeChar.getInventory().addItem("Weapon", Config.WP_11_ID, 1, activeChar, null);

			activeChar.getInventory().equipItemAndRecord(item);

			activeChar.setVar("select_weapon", "1");
		}
		else if (_command.startsWith(Config.BYBASS_WP_12_ITEM) && activeChar.getVar("select_weapon") == null)
		{
			endStartup(activeChar);

			L2ItemInstance item = activeChar.getInventory().addItem("Weapon", Config.WP_12_ID, 1, activeChar, null);

			activeChar.getInventory().equipItemAndRecord(item);

			activeChar.setVar("select_weapon", "1");
		}
		else if (_command.startsWith(Config.BYBASS_WP_13_ITEM) && activeChar.getVar("select_weapon") == null)
		{
			endStartup(activeChar);

			L2ItemInstance item = activeChar.getInventory().addItem("Weapon", Config.WP_13_ID, 1, activeChar, null);

			activeChar.getInventory().equipItemAndRecord(item);

			activeChar.setVar("select_weapon", "1");
		}
		else if (_command.startsWith(Config.BYBASS_WP_14_ITEM) && activeChar.getVar("select_weapon") == null)
		{
			endStartup(activeChar);

			L2ItemInstance item = activeChar.getInventory().addItem("Weapon", Config.WP_14_ID, 1, activeChar, null);

			activeChar.getInventory().equipItemAndRecord(item);

			activeChar.setVar("select_weapon", "1");
		}
		else if (_command.startsWith(Config.BYBASS_WP_15_ITEM) && activeChar.getVar("select_weapon") == null)
		{
			endStartup(activeChar);

			L2ItemInstance item = activeChar.getInventory().addItem("Weapon", Config.WP_15_ID, 1, activeChar, null);

			activeChar.getInventory().equipItemAndRecord(item);

			activeChar.setVar("select_weapon", "1");
		}
		else if (_command.startsWith(Config.BYBASS_WP_16_ITEM) && activeChar.getVar("select_weapon") == null)
		{
			endStartup(activeChar);

			L2ItemInstance item = activeChar.getInventory().addItem("Weapon", Config.WP_16_ID, 1, activeChar, null);

			activeChar.getInventory().addItem("Arrow", Config.WP_ARROW, 5000, activeChar, null);
			activeChar.getInventory().equipItemAndRecord(item);

			activeChar.setVar("select_weapon", "1");
		}
		else if (_command.startsWith(Config.BYBASS_WP_17_ITEM) && activeChar.getVar("select_weapon") == null)
		{
			endStartup(activeChar);

			L2ItemInstance item = activeChar.getInventory().addItem("Weapon", Config.WP_17_ID, 1, activeChar, null);

			activeChar.getInventory().addItem("Arrow", Config.WP_ARROW, 5000, activeChar, null);
			activeChar.getInventory().equipItemAndRecord(item);

			activeChar.setVar("select_weapon", "1");
		}
		else if (_command.startsWith(Config.BYBASS_WP_18_ITEM) && activeChar.getVar("select_weapon") == null)
		{
			endStartup(activeChar);

			L2ItemInstance item = activeChar.getInventory().addItem("Weapon", Config.WP_18_ID, 1, activeChar, null);

			activeChar.getInventory().addItem("Arrow", Config.WP_ARROW, 5000, activeChar, null);
			activeChar.getInventory().equipItemAndRecord(item);

			activeChar.setVar("select_weapon", "1");
		}
		else if (_command.startsWith(Config.BYBASS_WP_19_ITEM) && activeChar.getVar("select_weapon") == null)
		{
			endStartup(activeChar);

			L2ItemInstance item = activeChar.getInventory().addItem("Weapon", Config.WP_19_ID, 1, activeChar, null);

			activeChar.getInventory().equipItemAndRecord(item);

			activeChar.setVar("select_weapon", "1");
		}
		else if (_command.startsWith(Config.BYBASS_WP_20_ITEM) && activeChar.getVar("select_weapon") == null)
		{
			endStartup(activeChar);

			L2ItemInstance item = activeChar.getInventory().addItem("Weapon", Config.WP_20_ID, 1, activeChar, null);

			activeChar.getInventory().equipItemAndRecord(item);

			activeChar.setVar("select_weapon", "1");
		}
		else if (_command.startsWith(Config.BYBASS_WP_21_ITEM) && activeChar.getVar("select_weapon") == null)
		{
			endStartup(activeChar);

			L2ItemInstance item = activeChar.getInventory().addItem("Weapon", Config.WP_21_ID, 1, activeChar, null);

			activeChar.getInventory().equipItemAndRecord(item);

			activeChar.setVar("select_weapon", "1");
		}
		else if (_command.startsWith(Config.BYBASS_WP_22_ITEM) && activeChar.getVar("select_weapon") == null)
		{
			endStartup(activeChar);

			L2ItemInstance item = activeChar.getInventory().addItem("Weapon", Config.WP_22_ID, 1, activeChar, null);

			activeChar.getInventory().equipItemAndRecord(item);

			activeChar.setVar("select_weapon", "1");
		}
		else if (_command.startsWith(Config.BYBASS_WP_23_ITEM) && activeChar.getVar("select_weapon") == null)
		{
			endStartup(activeChar);

			L2ItemInstance item = activeChar.getInventory().addItem("Weapon", Config.WP_23_ID, 1, activeChar, null);

			activeChar.getInventory().equipItemAndRecord(item);

			activeChar.setVar("select_weapon", "1");
		}
		else if (_command.startsWith(Config.BYBASS_WP_24_ITEM) && activeChar.getVar("select_weapon") == null)
		{
			endStartup(activeChar);

			L2ItemInstance item = activeChar.getInventory().addItem("Weapon", Config.WP_24_ID, 1, activeChar, null);

			activeChar.getInventory().equipItemAndRecord(item);

			activeChar.setVar("select_weapon", "1");
		}
		else if (_command.startsWith(Config.BYBASS_WP_25_ITEM) && activeChar.getVar("select_weapon") == null)
		{
			endStartup(activeChar);

			L2ItemInstance item = activeChar.getInventory().addItem("Weapon", Config.WP_25_ID, 1, activeChar, null);

			activeChar.getInventory().equipItemAndRecord(item);

			activeChar.setVar("select_weapon", "1");
		}
		else if (_command.startsWith(Config.BYBASS_WP_26_ITEM) && activeChar.getVar("select_weapon") == null)
		{
			endStartup(activeChar);

			L2ItemInstance item = activeChar.getInventory().addItem("Weapon", Config.WP_26_ID, 1, activeChar, null);

			activeChar.getInventory().equipItemAndRecord(item);

			activeChar.setVar("select_weapon", "1");
		}
		else if (_command.startsWith(Config.BYBASS_WP_27_ITEM) && activeChar.getVar("select_weapon") == null)
		{
			endStartup(activeChar);

			L2ItemInstance item = activeChar.getInventory().addItem("Weapon", Config.WP_27_ID, 1, activeChar, null);

			activeChar.getInventory().equipItemAndRecord(item);

			activeChar.setVar("select_weapon", "1");
		}
		else if (_command.startsWith(Config.BYBASS_WP_28_ITEM) && activeChar.getVar("select_weapon") == null)
		{
			endStartup(activeChar);

			L2ItemInstance item = activeChar.getInventory().addItem("Weapon", Config.WP_28_ID, 1, activeChar, null);

			activeChar.getInventory().equipItemAndRecord(item);

			activeChar.setVar("select_weapon", "1");
		}
		else if (_command.startsWith(Config.BYBASS_WP_29_ITEM) && activeChar.getVar("select_weapon") == null)
		{
			endStartup(activeChar);

			L2ItemInstance item = activeChar.getInventory().addItem("Weapon", Config.WP_29_ID, 1, activeChar, null);

			activeChar.getInventory().equipItemAndRecord(item);

			activeChar.setVar("select_weapon", "1");
		}
		else if (_command.startsWith(Config.BYBASS_WP_30_ITEM) && activeChar.getVar("select_weapon") == null)
		{
			endStartup(activeChar);

			L2ItemInstance item = activeChar.getInventory().addItem("Weapon", Config.WP_30_ID, 1, activeChar, null);

			activeChar.getInventory().equipItemAndRecord(item);

			activeChar.setVar("select_weapon", "1");
		}
		else if (_command.startsWith(Config.BYBASS_WP_31_ITEM) && activeChar.getVar("select_weapon") == null)
		{
			endStartup(activeChar);

			L2ItemInstance item = activeChar.getInventory().addItem("Weapon", Config.WP_31_ID, 1, activeChar, null);

			activeChar.getInventory().equipItemAndRecord(item);

			activeChar.setVar("select_weapon", "1");
		}
		else if (_command.startsWith("end_setup"))
		{
			activeChar.sendPacket(TutorialCloseHtml.STATIC_PACKET);
			activeChar.sendPacket(new SocialAction(activeChar, 3));
			activeChar.giveAvailableSkills();
			if (activeChar.isMageClass())
			{
				if (Config.STARTUP_SYSTEM_BUFF_MAGE)
				{
				for (Integer skillid : Config.MAGE_BUFF_LIST)
				{
					L2Skill skill = SkillTable.getInstance().getInfo(skillid, SkillTable.getInstance().getMaxLevel(skillid));
					if (skill != null)
						skill.getEffects(activeChar, activeChar);
				}
				}
				activeChar.setCurrentHpMp(activeChar.getMaxHp(), activeChar.getMaxMp());
				activeChar.setCurrentCp(activeChar.getMaxCp());

				/* activeChar.getInventory().addItem("Mana Potion", 728, 50, activeChar, null); activeChar.getInventory().addItem("Greater Healing Potion", 1539, 50, activeChar, null); activeChar.getInventory().addItem("Scroll of Scape", 736, 50, activeChar, null); activeChar.getInventory().addItem("Blessed Soul Shot", 2512, 2000, activeChar, null); */
				if (Config.CHECK_SKILLS_ON_ENTER)
					activeChar.checkAllowedSkills();
			}
			else
			{
				if (Config.STARTUP_SYSTEM_BUFF_FIGHT)
				{
				for (Integer skillid : Config.FIGHTER_BUFF_LIST)
				{
					L2Skill skill = SkillTable.getInstance().getInfo(skillid, SkillTable.getInstance().getMaxLevel(skillid));
					if (skill != null)
						skill.getEffects(activeChar, activeChar);
				}
				}
				activeChar.setCurrentHpMp(activeChar.getMaxHp(), activeChar.getMaxMp());
				activeChar.setCurrentCp(activeChar.getMaxCp());

				/* activeChar.getInventory().addItem("Mana Potion", 728, 50, activeChar, null); activeChar.getInventory().addItem("Greater Healing Potion", 1539, 50, activeChar, null); activeChar.getInventory().addItem("Scroll of Scape", 736, 50, activeChar, null); activeChar.getInventory().addItem("Soul Shot", 1465, 2000, activeChar, null); */
				if (Config.CHECK_SKILLS_ON_ENTER)
					activeChar.checkAllowedSkills();
			}
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					activeChar.sendPacket(new CameraMode(0));
					activeChar.sendPacket(NormalCamera.STATIC_PACKET);

					activeChar.sendPacket(new InventoryUpdate());
					activeChar.sendPacket(new ItemList(activeChar, false));
					activeChar.sendPacket(new StatusUpdate(activeChar));

					activeChar.getInventory().reloadEquippedItems();


					// Make the character appears
					activeChar.getAppearance().setVisible();
					if (activeChar.getVar("startupfinish") == null)
					RandomTeleport(activeChar);
					activeChar.broadcastUserInfo();
				}
			}, 5000L);
		}
	}

	public static void SelectClass(L2PcInstance activeChar)
	{
		activeChar.sendPacket(new SocialAction(activeChar, 2));

		if (activeChar.getVar("select_class") == null && Config.STARTUP_SYSTEM_SELECTCLASS)
		{
			if (activeChar.getClassId().getId() == 0)
			{
				String msg = HtmCache.getInstance().getHtm("data/html/mods/Startup/Classes/Human_Fighter.htm");

				msg = msg.replace("%name%", activeChar.getName());
				activeChar.sendPacket(new TutorialShowHtml(msg));
			}
		}
		if (activeChar.getVar("select_class") == null && Config.STARTUP_SYSTEM_SELECTCLASS)
		{
			if (activeChar.getClassId().getId() == 10)
			{
				String msg = HtmCache.getInstance().getHtm("data/html/mods/Startup/Classes/Human_Mystic.htm");

				msg = msg.replace("%name%", activeChar.getName());
				activeChar.sendPacket(new TutorialShowHtml(msg));
			}
		}
		if (activeChar.getVar("select_class") == null && Config.STARTUP_SYSTEM_SELECTCLASS)
		{
			if (activeChar.getClassId().getId() == 18)
			{
				String msg = HtmCache.getInstance().getHtm("data/html/mods/Startup/Classes/Elven_Fighter.htm");

				msg = msg.replace("%name%", activeChar.getName());
				activeChar.sendPacket(new TutorialShowHtml(msg));
			}
		}
		if (activeChar.getVar("select_class") == null && Config.STARTUP_SYSTEM_SELECTCLASS)
		{
			if (activeChar.getClassId().getId() == 25)
			{
				String msg = HtmCache.getInstance().getHtm("data/html/mods/Startup/Classes/Elven_Mystic.htm");

				msg = msg.replace("%name%", activeChar.getName());
				activeChar.sendPacket(new TutorialShowHtml(msg));
			}
		}
		if (activeChar.getVar("select_class") == null && Config.STARTUP_SYSTEM_SELECTCLASS)
		{
			if (activeChar.getClassId().getId() == 31)
			{
				String msg = HtmCache.getInstance().getHtm("data/html/mods/Startup/Classes/Dark_Fighter.htm");

				msg = msg.replace("%name%", activeChar.getName());
				activeChar.sendPacket(new TutorialShowHtml(msg));
			}
		}
		if (activeChar.getVar("select_class") == null && Config.STARTUP_SYSTEM_SELECTCLASS)
		{
			if (activeChar.getClassId().getId() == 38)
			{
				String msg = HtmCache.getInstance().getHtm("data/html/mods/Startup/Classes/Dark_Mystic.htm");

				msg = msg.replace("%name%", activeChar.getName());
				activeChar.sendPacket(new TutorialShowHtml(msg));
			}
		}
		if (activeChar.getVar("select_class") == null && Config.STARTUP_SYSTEM_SELECTCLASS)
		{
			if (activeChar.getClassId().getId() == 44)
			{
				String msg = HtmCache.getInstance().getHtm("data/html/mods/Startup/Classes/Orc_Fighter.htm");

				msg = msg.replace("%name%", activeChar.getName());
				activeChar.sendPacket(new TutorialShowHtml(msg));
			}
		}
		if (activeChar.getVar("select_class") == null && Config.STARTUP_SYSTEM_SELECTCLASS)
		{
			if (activeChar.getClassId().getId() == 49)
			{
				String msg = HtmCache.getInstance().getHtm("data/html/mods/Startup/Classes/Orc_Mystic.htm");

				msg = msg.replace("%name%", activeChar.getName());
				activeChar.sendPacket(new TutorialShowHtml(msg));
			}
		}
		if (activeChar.getVar("select_class") == null && Config.STARTUP_SYSTEM_SELECTCLASS)
		{
			if (activeChar.getClassId().getId() == 53)
			{
				String msg = HtmCache.getInstance().getHtm("data/html/mods/Startup/Classes/Dwarf_Fighter.htm");

				msg = msg.replace("%name%", activeChar.getName());
				activeChar.sendPacket(new TutorialShowHtml(msg));
			}
		}
	}

	public static void SelectArmor(L2PcInstance activeChar)
	{
		activeChar.sendPacket(new SocialAction(activeChar, 11));

		if (activeChar.getVar("select_armor") == null && Config.STARTUP_SYSTEM_SELECTARMOR)
		{
			String msg = HtmCache.getInstance().getHtm("data/html/mods/Startup/StartArmor.htm");

			msg = msg.replace("%name%", activeChar.getName());
			activeChar.sendPacket(new TutorialShowHtml(msg));
		}
		else
		{
			if (activeChar.getVar("select_armor") != null && Config.STARTUP_SYSTEM_SELECTWEAP)
			{
				SelectWeapon(activeChar);
			}
			else
				endStartup(activeChar);
		}
	}

	public static void SelectWeapon(L2PcInstance activeChar)
	{
		activeChar.sendPacket(new SocialAction(activeChar, 12));

		if (activeChar.getVar("select_weapon") == null && Config.STARTUP_SYSTEM_SELECTWEAP)
		{
			String msg = HtmCache.getInstance().getHtm("data/html/mods/Startup/StartWeapon.htm");

			msg = msg.replace("%name%", activeChar.getName());
			activeChar.sendPacket(new TutorialShowHtml(msg));
		}
		else
			endStartup(activeChar);
	}

	public static void endStartup(L2PcInstance activeChar)
	{
		activeChar.sendPacket(new SocialAction(activeChar, 9));

		if (activeChar.getVar("startupfinish") == null)
		{
			String msg = HtmCache.getInstance().getHtm("data/html/mods/Startup/Finish.htm");

			msg = msg.replace("%name%", activeChar.getName());
			activeChar.sendPacket(new TutorialShowHtml(msg));
		}
		else
			return;
	}

	public static void RandomTeleport(L2PcInstance activeChar)
	{
		activeChar.setVar("startupfinish", "1");
		switch (Rnd.get(5))
		{
			case 0:
			{
				int x = 82533 + Rnd.get(100);
				int y = 149122 + Rnd.get(100);
				activeChar.teleToLocation(x, y, -3474);
				break;
			}
			case 1:
			{
				int x = 82571 + Rnd.get(100);
				int y = 148060 + Rnd.get(100);
				activeChar.teleToLocation(x, y, -3467);
				break;
			}
			case 2:
			{
				int x = 81376 + Rnd.get(100);
				int y = 148042 + Rnd.get(100);
				activeChar.teleToLocation(x, y, -3474);
				break;
			}
			case 3:
			{
				int x = 81359 + Rnd.get(100);
				int y = 149218 + Rnd.get(100);
				activeChar.teleToLocation(x, y, -3474);
				break;
			}
			case 4:
			{
				int x = 82862 + Rnd.get(100);
				int y = 148606 + Rnd.get(100);
				activeChar.teleToLocation(x, y, -3474);
				break;
			}
		}
	}
}