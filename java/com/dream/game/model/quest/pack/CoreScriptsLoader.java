package com.dream.game.model.quest.pack;

import com.dream.game.model.quest.pack.ai.Benom;
import com.dream.game.model.quest.pack.ai.CatsEyeBandit;
import com.dream.game.model.quest.pack.ai.Chests;
import com.dream.game.model.quest.pack.ai.DeluLizardmanSpecialAgent;
import com.dream.game.model.quest.pack.ai.DeluLizardmanSpecialCommander;
import com.dream.game.model.quest.pack.ai.DrChaos;
import com.dream.game.model.quest.pack.ai.Evabox;
import com.dream.game.model.quest.pack.ai.FOGMobs;
import com.dream.game.model.quest.pack.ai.FairyTrees;
import com.dream.game.model.quest.pack.ai.FeedableBeasts;
import com.dream.game.model.quest.pack.ai.FindAndAttackMaster;
import com.dream.game.model.quest.pack.ai.Gordon;
import com.dream.game.model.quest.pack.ai.HotSprings;
import com.dream.game.model.quest.pack.ai.IceFairySirra;
import com.dream.game.model.quest.pack.ai.KarulBugbear;
import com.dream.game.model.quest.pack.ai.LastImperialTomb;
import com.dream.game.model.quest.pack.ai.Monastery;
import com.dream.game.model.quest.pack.ai.OlMahumGeneral;
import com.dream.game.model.quest.pack.ai.PolymorphingAngel;
import com.dream.game.model.quest.pack.ai.PolymorphingBoss;
import com.dream.game.model.quest.pack.ai.PolymorphingOnAttack;
import com.dream.game.model.quest.pack.ai.ScarletStokateNoble;
import com.dream.game.model.quest.pack.ai.SummonMinions;
import com.dream.game.model.quest.pack.ai.TimakOrcOverlord;
import com.dream.game.model.quest.pack.ai.TimakOrcTroopLeader;
import com.dream.game.model.quest.pack.ai.TurekOrcFootman;
import com.dream.game.model.quest.pack.ai.TurekOrcSupplier;
import com.dream.game.model.quest.pack.ai.TurekOrcWarlord;
import com.dream.game.model.quest.pack.ai.VarkaKetraAlly;

public class CoreScriptsLoader
{
	public static void Register()
	{
		registerAIScripts();
	}

	private static void registerAIScripts()
	{
		new CatsEyeBandit();
		new Chests();
		new DeluLizardmanSpecialAgent();
		new DeluLizardmanSpecialCommander();
		new DrChaos();
		new Evabox();
		new FairyTrees(-1, "FairyTrees", "ai_grp");
		new FeedableBeasts();
		new FindAndAttackMaster();
		new Gordon();
		new HotSprings();
		new IceFairySirra();
		new KarulBugbear();
		new LastImperialTomb();
		new Monastery();
		new OlMahumGeneral();
		new PolymorphingAngel();
		new PolymorphingBoss();
		new PolymorphingOnAttack();
		new SummonMinions();
		new TimakOrcOverlord();
		new TimakOrcTroopLeader();
		new TurekOrcFootman();
		new TurekOrcSupplier();
		new TurekOrcWarlord();
		new VarkaKetraAlly();
		new ScarletStokateNoble();
		new FOGMobs();
		new Benom();
	}
}