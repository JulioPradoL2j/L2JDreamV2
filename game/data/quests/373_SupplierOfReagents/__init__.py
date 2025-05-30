# Supplier of Reagents version 0.2
# by DrLecter for the Official L2J Datapack Project.
# Visit http://forum.l2jdp.com for more details.
# Rate fix by Gnat
import sys
from com.dream import Config
from com.dream.game.model.quest import State
from com.dream.game.model.quest import QuestState
from com.dream.game.model.quest.jython import QuestJython as JQuest
#Quest info
QUEST_NUMBER,QUEST_NAME,QUEST_DESCRIPTION = 373,"SupplierOfReagents","Supplier of Reagents"
qn = "373_SupplierOfReagents"

#Quest items
REAGENT_POUCH1,   REAGENT_POUCH2,REAGENT_POUCH3, REAGENT_BOX, \
WYRMS_BLOOD,      LAVA_STONE,    MOONSTONE_SHARD,ROTTEN_BONE, \
DEMONS_BLOOD,     INFERNIUM_ORE, BLOOD_ROOT,     VOLCANIC_ASH,\
QUICKSILVER,      SULFUR,        DRACOPLASM,     MAGMA_DUST,  \
MOON_DUST,        NECROPLASM,    DEMONPLASM,     INFERNO_DUST,\
DRACONIC_ESSENCE, FIRE_ESSENCE,  LUNARGENT,      MIDNIGHT_OIL,\
DEMONIC_ESSENCE,  ABYSS_OIL,     HELLFIRE_OIL,   NIGHTMARE_OIL=range(6007,6035)
MIXING_STONE1 = 5904
#Mimir's Elixir items
BLOOD_FIRE, MIMIRS_ELIXIR, PURE_SILVER, TRUE_GOLD = range(6318,6322)

MATS=range(6011,6032)+range(6320,6322)
#Messages
default   = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>"
#NPCs
WESLEY,URN=30166,31149
#Mobs & Drop
#index = mobId, array = [ TotalChance, (item0, chance0),(item1, chance1),...]
DROPLIST = {
20813: [100, (QUICKSILVER,60),(ROTTEN_BONE,40)],
20822: [100, (VOLCANIC_ASH,40),(REAGENT_POUCH1,60)],
21061: [90, (DEMONS_BLOOD,70),(MOONSTONE_SHARD,20)],
20828: [100, (REAGENT_POUCH2,70),(QUICKSILVER,30)],
21066: [40, (REAGENT_BOX,40)],
21111: [50, (WYRMS_BLOOD,50),(LAVA_STONE,24)],
21115: [50, (REAGENT_POUCH3,50)]
}

class Quest (JQuest) :

 def __init__(self,id,name,descr): 
     JQuest.__init__(self,id,name,descr)
     self.questItemIds = [5904]

 def onEvent (self,event,st) :
    id = st.getState()
    htmltext = event
    if event == "30166-4.htm" :
       st.setState(State.STARTED)
       st.giveItems(5904,1)
       st.playSound("ItemSound.quest_accept")
    elif event == "30166-5.htm" :
       for i in range(6007,6035)+[6317,5904] :
          st.takeItems(i,-1)
       st.exitQuest(1)
       st.playSound("ItemSound.quest_finish")

    if event == "Initial" :
      if st.getQuestItemsCount(5904) == 1 :
        if st.getPlayer().getQuestState("235_MimirsElixir") and st.getQuestItemsCount(6320) >= 1 and st.getQuestItemsCount(6321) >= 1 and st.getQuestItemsCount(6318) == 1  :
          htmltext = "3.htm"		
        else :
          return "2.htm"		
      else :
        return "NoMixing.htm"	
		  
    elif event == "PureSilver" :
      if st.getQuestItemsCount(6320) >= 1 :
        htmltext = "PureSilver.htm"
      else:
        return "NoItem.htm"				

    elif event == "TrueGold" :
      if st.getQuestItemsCount(6321) >= 1 :
        htmltext = "TrueGold.htm"
      else:
        return "NoItem.htm"				
		
    elif event == "BloodFire" :
      if st.getQuestItemsCount(6318) >= 1 :
        htmltext = "BloodFire.htm"
      else:
        return "NoItem.htm"				

    elif event == "MimirElixirTemp" :
      if st.getQuestItemsCount(6320) >= 1 and st.getQuestItemsCount(6321) >= 1 and st.getQuestItemsCount(6318) >= 1 :
        htmltext = "MimirElixirTemp.htm"
      else:
        return "NoItem.htm"			

    elif event == "MimirElixirTemp1" :
      mimirs = st.getPlayer().getQuestState("235_MimirsElixir")	
      if st.getQuestItemsCount(6320) >= 1 and st.getQuestItemsCount(6321) >= 1 and st.getQuestItemsCount(6318) >= 1 :
        st.takeItems(6320,1)
        st.takeItems(6321,1)
        st.takeItems(6318,1)		
        if st.getRandom(100) < 100 :
          st.giveItems(6319,1)
          mimirs.set("cond","8")
          htmltext = "Ok.htm"		
          st.playSound("SkillSound5.liquid_success_01")		
		  
    elif event == "Wyrm" :
      if st.getQuestItemsCount(6011) >= 10 :
        htmltext = "Wyrm.htm"
      else:
        return "NoItem.htm"				
		  
    elif event == "BloodRoot" :
      if st.getQuestItemsCount(6017) >= 1 :
        htmltext = "BloodRoot.htm"
      else:
        return "NoItem.htm"		

    elif event == "Temperature1" :
      if st.getQuestItemsCount(6011) >= 10 and st.getQuestItemsCount(6017) >= 1 :
        htmltext = "Temperature1.htm"
      else:
        return "NoItem.htm"		

    elif event == "TempDracoplasm1" :
      if st.getQuestItemsCount(6011) >= 10 and st.getQuestItemsCount(6017) >= 1 :
        st.takeItems(6011,10)
        st.takeItems(6017,1)
        if st.getRandom(100) < 100 :
          st.giveItems(6021,1)
          st.playSound("SkillSound5.liquid_success_01")		
          htmltext = "New.htm"		

		  
    elif event == "TempDracoplasm2" :
      if st.getQuestItemsCount(6011) >= 10 and st.getQuestItemsCount(6017) >= 1 :
        st.takeItems(6011,10)
        st.takeItems(6017,1)
        if st.getRandom(100) < 45 :
          st.giveItems(6021,3)
          st.playSound("SkillSound5.liquid_success_01")
          htmltext = "New.htm"
        else:
          st.playSound("SkillSound5.liquid_fail_01")			
          return "New.htm"	
		  
    elif event == "TempDracoplasm3" :
      if st.getQuestItemsCount(6011) >= 10 and st.getQuestItemsCount(6017) >= 1 :
        st.takeItems(6011,10)
        st.takeItems(6017,1)	  
        if st.getRandom(100) < 15 :
          st.giveItems(6021,5)
          st.playSound("SkillSound5.liquid_success_01")
          htmltext = "New.htm"
        else:
          st.playSound("SkillSound5.liquid_fail_01")			
          return "New.htm"			  

    elif event == "LavaStone" :
      if st.getQuestItemsCount(6012) >= 10 :
        htmltext = "LavaStone.htm"
      else:
        return "NoItem.htm"		

    elif event == "VolcanicAsh" :
      if st.getQuestItemsCount(6018) >= 1 :
        htmltext = "VolcanicAsh.htm"
      else:
        return "NoItem.htm"		

    elif event == "Temperature2" :
      if st.getQuestItemsCount(6012) >= 10 and st.getQuestItemsCount(6018) >= 1 :
        htmltext = "Temperature2.htm"
      else:
        return "NoItem.htm"		

    elif event == "TempMagmaDust1" :
      if st.getQuestItemsCount(6012) >= 10 and st.getQuestItemsCount(6018) >= 1 :
        st.takeItems(6012,10)
        st.takeItems(6018,1)
        if st.getRandom(100) < 100 :
          st.giveItems(6022,1)
          st.playSound("SkillSound5.liquid_success_01")		
          htmltext = "New.htm"		
		  
    elif event == "TempMagmaDust2" :
      if st.getQuestItemsCount(6012) >= 10 and st.getQuestItemsCount(6018) >= 1 :
        st.takeItems(6012,10)
        st.takeItems(6018,1)
        if st.getRandom(100) < 45 :
          st.giveItems(6022,3)
          st.playSound("SkillSound5.liquid_success_01")
          htmltext = "New.htm"
        else:
          st.playSound("SkillSound5.liquid_fail_01")			
          return "New.htm"	
		  
    elif event == "TempMagmaDust3" :
      if st.getQuestItemsCount(6012) >= 10 and st.getQuestItemsCount(6018) >= 1 :
        st.takeItems(6012,10)
        st.takeItems(6018,1)	  
        if st.getRandom(100) < 15 :
          st.giveItems(6022,5)
          st.playSound("SkillSound5.liquid_success_01")
          htmltext = "New.htm"
        else:
          st.playSound("SkillSound5.liquid_fail_01")			
          return "New.htm"		  
			  
    elif event == "Moonstones" :
      if st.getQuestItemsCount(6013) >= 10 :
        htmltext = "Moonstones.htm"
      else:
        return "NoItem.htm"		

    elif event == "Volcanic" :
      if st.getQuestItemsCount(6018) >= 1 :
        htmltext = "Volcanic.htm"
      else:
        return "NoItem.htm"		

    elif event == "Temperature3" :
      if st.getQuestItemsCount(6018) >= 1 and st.getQuestItemsCount(6013) >= 10 :
        htmltext = "Temperature3.htm"
      else:
        return "NoItem.htm"		

    elif event == "TempMoonDust1" :
      if st.getQuestItemsCount(6018) >= 1 and st.getQuestItemsCount(6013) >= 10 :
        if st.getRandom(100) < 100 :
          st.takeItems(6013,10)
          st.takeItems(6018,1)
          st.giveItems(6023,1)
          st.playSound("SkillSound5.liquid_success_01")
          htmltext = "New.htm"
		  
    elif event == "TempMoonDust2" :
      if st.getQuestItemsCount(6018) >= 1 and st.getQuestItemsCount(6013) >= 10 :
        st.takeItems(6013,10)
        st.takeItems(6018,1)	  
        if st.getRandom(100) < 45 :
          st.giveItems(6023,3)
          st.playSound("SkillSound5.liquid_success_01")
          htmltext = "New.htm"
        else:
          st.playSound("SkillSound5.liquid_fail_01")			
          return "New.htm"	

    elif event == "TempMoonDust3" :
      if st.getQuestItemsCount(6018) >= 1 and st.getQuestItemsCount(6013) >= 10 :
        st.takeItems(6013,10)
        st.takeItems(6018,1)	
        if st.getRandom(100) < 15 :
          st.giveItems(6023,5)
          st.playSound("SkillSound5.liquid_success_01")
          htmltext = "New.htm"
        else:
          st.playSound("SkillSound5.liquid_fail_01")			
          return "New.htm"			  

    elif event == "RottenBone" :
      if st.getQuestItemsCount(6014) >= 10 :
        htmltext = "RottenBone.htm"
      else:
        return "NoItem.htm"		

    elif event == "BloodRoot1" :
      if st.getQuestItemsCount(6017) >= 1 :
        htmltext = "BloodRoot1.htm"
      else:
        return "NoItem.htm"		

    elif event == "Temperature4" :
      if st.getQuestItemsCount(6014) >= 10 and st.getQuestItemsCount(6017) >= 1 :
        htmltext = "Temperature4.htm"
      else:
        return "NoItem.htm"		

    elif event == "TempNecroPlasm1" :
      if st.getQuestItemsCount(6014) >= 10 and st.getQuestItemsCount(6017) >= 1 :
        st.takeItems(6014,10)
        st.takeItems(6017,1)
        if st.getRandom(100) < 100 :
          st.giveItems(6024,1)
          st.playSound("SkillSound5.liquid_success_01")		
          htmltext = "New.htm"		

    elif event == "TempNecroPlasm2" :
      if st.getQuestItemsCount(6014) >= 10 and st.getQuestItemsCount(6017) >= 1 :
        st.takeItems(6014,10)
        st.takeItems(6017,1)
        if st.getRandom(100) < 45 :
          st.giveItems(6024,3)
          st.playSound("SkillSound5.liquid_success_01")
          htmltext = "New.htm"
        else:
          st.playSound("SkillSound5.liquid_fail_01")			
          return "New.htm"	
		  
    elif event == "TempNecroPlasm3" :
      if st.getQuestItemsCount(6014) >= 10 and st.getQuestItemsCount(6017) >= 1 :
        st.takeItems(6014,10)
        st.takeItems(6017,1)	  
        if st.getRandom(100) < 15 :
          st.giveItems(6024,5)
          st.playSound("SkillSound5.liquid_success_01")
          htmltext = "New.htm"
        else:
          st.playSound("SkillSound5.liquid_fail_01")			
          return "New.htm"		  

    elif event == "DemonsBlood" :
      if st.getQuestItemsCount(6015) >= 10 :
        htmltext = "DemonsBlood.htm"
      else:
        return "NoItem.htm"		

    elif event == "BloodRoot2" :
      if st.getQuestItemsCount(6017) >= 1 :
        htmltext = "BloodRoot2.htm"
      else:
        return "NoItem.htm"		

    elif event == "Temperature5" :
      if st.getQuestItemsCount(6015) >= 10 and st.getQuestItemsCount(6017) >= 1 :
        htmltext = "Temperature5.htm"
      else:
        return "NoItem.htm"		

    elif event == "TempDemonPlasm1" :
      if st.getQuestItemsCount(6015) >= 10 and st.getQuestItemsCount(6017) >= 1 :
        st.takeItems(6015,10)
        st.takeItems(6017,1)
        if st.getRandom(100) < 100 :
          st.giveItems(6025,1)
          htmltext = "New.htm"		
          st.playSound("SkillSound5.liquid_success_01")		

    elif event == "TempDemonPlasm2" :
      if st.getQuestItemsCount(6015) >= 10 and st.getQuestItemsCount(6017) >= 1 :
        st.takeItems(6015,10)
        st.takeItems(6017,1)
        if st.getRandom(100) < 45 :
          st.giveItems(6025,3)
          st.playSound("SkillSound5.liquid_success_01")
          htmltext = "New.htm"
        else:
          st.playSound("SkillSound5.liquid_fail_01")			
          return "New.htm"		
		  
    elif event == "TempDemonPlasm3" :
      if st.getQuestItemsCount(6015) >= 10 and st.getQuestItemsCount(6017) >= 1 :
        st.takeItems(6015,10)
        st.takeItems(6017,1)	  
        if st.getRandom(100) < 15 :
          st.giveItems(6025,5)
          st.playSound("SkillSound5.liquid_success_01")
          htmltext = "New.htm"
        else:
          st.playSound("SkillSound5.liquid_fail_01")			
          return "New.htm"				  

    elif event == "InferniumOre" :
      if st.getQuestItemsCount(6016) >= 10 :
        htmltext = "InferniumOre.htm"
      else:
        return "NoItem.htm"		

    elif event == "VolcanicAsh1" :
      if st.getQuestItemsCount(6018) >= 1 :
        htmltext = "VolcanicAsh1.htm"
      else:
        return "NoItem.htm"		

    elif event == "Temperature6" :
      if st.getQuestItemsCount(6016) >= 10 and st.getQuestItemsCount(6018) >= 1 :
        htmltext = "Temperature6.htm"
      else:
        return "NoItem.htm"		

    elif event == "TempInferniumDust1" :
      if st.getQuestItemsCount(6016) >= 10 and st.getQuestItemsCount(6018) >= 1 :
        st.takeItems(6016,10)
        st.takeItems(6018,1)
        if st.getRandom(100) < 100 :
          st.giveItems(6026,1)
          htmltext = "New.htm"		
          st.playSound("SkillSound5.liquid_success_01")		

    elif event == "TempInferniumDust2" :
      if st.getQuestItemsCount(6016) >= 10 and st.getQuestItemsCount(6018) >= 1 :
        st.takeItems(6016,10)
        st.takeItems(6018,1)
        if st.getRandom(100) < 45 :
          st.giveItems(6026,3)
          st.playSound("SkillSound5.liquid_success_01")
          htmltext = "New.htm"
        else:
          st.playSound("SkillSound5.liquid_fail_01")			
          return "New.htm"	
		  
    elif event == "TempInferniumDust3" :
      if st.getQuestItemsCount(6016) >= 10 and st.getQuestItemsCount(6018) >= 1 :
        st.takeItems(6016,10)
        st.takeItems(6018,1)	  
        if st.getRandom(100) < 15 :
          st.giveItems(6026,5)
          st.playSound("SkillSound5.liquid_success_01")
          htmltext = "New.htm"
        else:
          st.playSound("SkillSound5.liquid_fail_01")			
          return "New.htm"			  

    elif event == "Dracoplasm" :
      if st.getQuestItemsCount(6021) >= 10 :
        htmltext = "Dracoplasm.htm"
      else:
        return "NoItem.htm"		

    elif event == "Quicksilver" :
      if st.getQuestItemsCount(6019) >= 1 :
        htmltext = "Quicksilver.htm"
      else:
        return "NoItem.htm"		

    elif event == "Temperature7" :
      if st.getQuestItemsCount(6021) >= 10 and st.getQuestItemsCount(6019) >= 1 :
        htmltext = "Temperature7.htm"
      else:
        return "NoItem.htm"		

    elif event == "TempDraconicEssence1" :
      if st.getQuestItemsCount(6021) >= 10 and st.getQuestItemsCount(6019) >= 1 :
        st.takeItems(6021,10)
        st.takeItems(6019,1)
        if st.getRandom(100) < 100 :
          st.giveItems(6027,1)
          st.playSound("SkillSound5.liquid_success_01")		
          htmltext = "New.htm"		

    elif event == "TempDraconicEssence2" :
      if st.getQuestItemsCount(6021) >= 10 and st.getQuestItemsCount(6019) >= 1 :
        st.takeItems(6021,10)
        st.takeItems(6019,1)
        if st.getRandom(100) < 45 :
          st.giveItems(6027,3)
          st.playSound("SkillSound5.liquid_success_01")
          htmltext = "New.htm"
        else:
          st.playSound("SkillSound5.liquid_fail_01")			
          return "New.htm"		
		  
    elif event == "TempDraconicEssence3" :
      if st.getQuestItemsCount(6021) >= 10 and st.getQuestItemsCount(6019) >= 1 :
        st.takeItems(6021,10)
        st.takeItems(6019,1)	  
        if st.getRandom(100) < 15 :
          st.giveItems(6027,5)
          st.playSound("SkillSound5.liquid_success_01")
          htmltext = "New.htm"
        else:
          st.playSound("SkillSound5.liquid_fail_01")			
          return "New.htm"		

    elif event == "MagmaDust" :
      if st.getQuestItemsCount(6022) >= 10 :
        htmltext = "MagmaDust.htm"
      else:
        return "NoItem.htm"		

    elif event == "Sulfur" :
      if st.getQuestItemsCount(6020) >= 1 :
        htmltext = "Sulfur.htm"
      else:
        return "NoItem.htm"		

    elif event == "Temperature8" :
      if st.getQuestItemsCount(6022) >= 10 and st.getQuestItemsCount(6020) >= 1 :
        htmltext = "Temperature8.htm"
      else:
        return "NoItem.htm"		

    elif event == "TempFireEssence1" :
      if st.getQuestItemsCount(6022) >= 10 and st.getQuestItemsCount(6020) >= 1 :
        st.takeItems(6022,10)
        st.takeItems(6020,1)
        if st.getRandom(100) < 100 :
          st.giveItems(6028,1)
          st.playSound("SkillSound5.liquid_success_01")		
          htmltext = "New.htm"		

    elif event == "TempFireEssence2" :
      if st.getQuestItemsCount(6022) >= 10 and st.getQuestItemsCount(6020) >= 1 :
        st.takeItems(6022,10)
        st.takeItems(6020,1)
        if st.getRandom(100) < 45 :
          st.giveItems(6028,3)
          st.playSound("SkillSound5.liquid_success_01")
          htmltext = "New.htm"
        else:
          st.playSound("SkillSound5.liquid_fail_01")			
          return "New.htm"		
		  
    elif event == "TempFireEssence3" :
      if st.getQuestItemsCount(6022) >= 10 and st.getQuestItemsCount(6020) >= 1 :
        st.takeItems(6022,10)
        st.takeItems(6020,1)	  
        if st.getRandom(100) < 15 :
          st.giveItems(6028,5)
          st.playSound("SkillSound5.liquid_success_01")
          htmltext = "New.htm"
        else:
          st.playSound("SkillSound5.liquid_fail_01")			
          return "New.htm"		

    elif event == "MoonDust" :
      if st.getQuestItemsCount(6023) >= 10 :
        htmltext = "MoonDust.htm"
      else:
        return "NoItem.htm"		

    elif event == "Quicksilver1" :
      if st.getQuestItemsCount(6019) >= 1 :
        htmltext = "Quicksilver1.htm"
      else:
        return "NoItem.htm"		

    elif event == "Temperature9" :
      if st.getQuestItemsCount(6023) >= 10 and st.getQuestItemsCount(6019) >= 1 :
        htmltext = "Temperature9.htm"
      else:
        return "NoItem.htm"		

    elif event == "TempLunargent1" :
      if st.getQuestItemsCount(6023) >= 10 and st.getQuestItemsCount(6019) >= 1 :
        st.takeItems(6023,10)
        st.takeItems(6019,1)
        if st.getRandom(100) < 100 :
          st.giveItems(6029,1)
          st.playSound("SkillSound5.liquid_success_01")		
          htmltext = "New.htm"		

    elif event == "TempLunargent2" :
      if st.getQuestItemsCount(6023) >= 10 and st.getQuestItemsCount(6019) >= 1 :
        st.takeItems(6023,10)
        st.takeItems(6019,1)
        if st.getRandom(100) < 45 :
          st.giveItems(6029,3)
          st.playSound("SkillSound5.liquid_success_01")
          htmltext = "New.htm"
        else:
          st.playSound("SkillSound5.liquid_fail_01")			
          return "New.htm"		
		  
    elif event == "TempLunargent3" :
      if st.getQuestItemsCount(6023) >= 10 and st.getQuestItemsCount(6019) >= 1 :
        st.takeItems(6023,10)
        st.takeItems(6019,1)	  
        if st.getRandom(100) < 15 :
          st.giveItems(6029,5)
          st.playSound("SkillSound5.liquid_success_01")
          htmltext = "New.htm"
        else:
          st.playSound("SkillSound5.liquid_fail_01")			
          return "New.htm"				   

    elif event == "Necroplasm" :
      if st.getQuestItemsCount(6024) >= 10 :
        htmltext = "Necroplasm.htm"
      else:
        return "NoItem.htm"		

    elif event == "Quicksilver2" :
      if st.getQuestItemsCount(6019) >= 1 :
        htmltext = "Quicksilver2.htm"
      else:
        return "NoItem.htm"		

    elif event == "Temperature10" :
      if st.getQuestItemsCount(6024) >= 10 and st.getQuestItemsCount(6019) >= 1 :
        htmltext = "Temperature10.htm"
      else:
        return "NoItem.htm"		

    elif event == "TempMidnight1" :
      if st.getQuestItemsCount(6024) >= 10 and st.getQuestItemsCount(6019) >= 1 :
        st.takeItems(6024,10)
        st.takeItems(6019,1)
        if st.getRandom(100) < 100 :
          st.giveItems(6030,1)
          st.playSound("SkillSound5.liquid_success_01")		
          htmltext = "New.htm"		

    elif event == "TempMidnight2" :
      if st.getQuestItemsCount(6024) >= 10 and st.getQuestItemsCount(6019) >= 1 :
        st.takeItems(6024,10)
        st.takeItems(6019,1)
        if st.getRandom(100) < 45 :
          st.giveItems(6030,3)
          st.playSound("SkillSound5.liquid_success_01")
          htmltext = "New.htm"
        else:
          st.playSound("SkillSound5.liquid_fail_01")			
          return "New.htm"		
		  
    elif event == "TempMidnight3" :
      if st.getQuestItemsCount(6024) >= 10 and st.getQuestItemsCount(6019) >= 1 :
        st.takeItems(6024,10)
        st.takeItems(6019,1)	  
        if st.getRandom(100) < 15 :
          st.giveItems(6030,5)
          st.playSound("SkillSound5.liquid_success_01")
          htmltext = "New.htm"
        else:
          st.playSound("SkillSound5.liquid_fail_01")			
          return "New.htm"		

    elif event == "Demonplasm" :
      if st.getQuestItemsCount(6025) >= 10 :
        htmltext = "Demonplasm.htm"
      else:
        return "NoItem.htm"		

    elif event == "Sulfur1" :
      if st.getQuestItemsCount(6020) >= 1 :
        htmltext = "Sulfur1.htm"
      else:
        return "NoItem.htm"		

    elif event == "Temperature11" :
      if st.getQuestItemsCount(6025) >= 10 and st.getQuestItemsCount(6020) >= 1 :
        htmltext = "Temperature11.htm"
      else:
        return "NoItem.htm"		

    elif event == "TempDemonicEssence1" :
      if st.getQuestItemsCount(6025) >= 10 and st.getQuestItemsCount(6020) >= 1 :
        st.takeItems(6025,10)
        st.takeItems(6020,1)
        if st.getRandom(100) < 100 :
          st.giveItems(6031,1)
          st.playSound("SkillSound5.liquid_success_01")		
          htmltext = "New.htm"		

    elif event == "TempDemonicEssence2" :
      if st.getQuestItemsCount(6025) >= 10 and st.getQuestItemsCount(6020) >= 1 :
        st.takeItems(6025,10)
        st.takeItems(6020,1)
        if st.getRandom(100) < 45 :
          st.giveItems(6031,3)
          st.playSound("SkillSound5.liquid_success_01")
          htmltext = "New.htm"
        else:
          st.playSound("SkillSound5.liquid_fail_01")			
          return "New.htm"		
		  
    elif event == "TempDemonicEssence3" :
      if st.getQuestItemsCount(6025) >= 10 and st.getQuestItemsCount(6020) >= 1 :
        st.takeItems(6025,10)
        st.takeItems(6020,1)	  
        if st.getRandom(100) < 15 :
          st.giveItems(6031,5)
          st.playSound("SkillSound5.liquid_success_01")
          htmltext = "New.htm"
        else:
          st.playSound("SkillSound5.liquid_fail_01")			
          return "New.htm"		

    elif event == "InfernoDust" :
      if st.getQuestItemsCount(6026) >= 10 :
        htmltext = "InfernoDust.htm"
      else:
        return "NoItem.htm"		

    elif event == "Sulfur2" :
      if st.getQuestItemsCount(6020) >= 1 :
        htmltext = "Sulfur2.htm"
      else:
        return "NoItem.htm"		

    elif event == "Temperature12" :
      if st.getQuestItemsCount(6026) >= 10 and st.getQuestItemsCount(6020) >= 1 :
        htmltext = "Temperature12.htm"
      else:
        return "NoItem.htm"		

    elif event == "TempAbyssOil1" :
      if st.getQuestItemsCount(6026) >= 10 and st.getQuestItemsCount(6020) >= 1 :
        st.takeItems(6026,10)
        st.takeItems(6020,1)
        if st.getRandom(100) < 100 :
          st.giveItems(6032,1)
          st.playSound("SkillSound5.liquid_success_01")		
          htmltext = "New.htm"		

    elif event == "TempAbyssOil2" :
      if st.getQuestItemsCount(6026) >= 10 and st.getQuestItemsCount(6020) >= 1 :
        st.takeItems(6026,10)
        st.takeItems(6020,1)
        if st.getRandom(100) < 45 :
          st.giveItems(6032,3)
          st.playSound("SkillSound5.liquid_success_01")
          htmltext = "New.htm"
        else:
          st.playSound("SkillSound5.liquid_fail_01")			
          return "New.htm"		
		  
    elif event == "TempAbyssOil3" :
      if st.getQuestItemsCount(6026) >= 10 and st.getQuestItemsCount(6020) >= 1 :
        st.takeItems(6026,10)
        st.takeItems(6020,1)	  
        if st.getRandom(100) < 15 :
          st.giveItems(6032,5)
          st.playSound("SkillSound5.liquid_success_01")
          htmltext = "New.htm"
        else:
          st.playSound("SkillSound5.liquid_fail_01")			
          return "New.htm"		

    elif event == "FireEssence" :
      if st.getQuestItemsCount(6028) >= 1 :
        htmltext = "FireEssence.htm"
      else:
        return "NoItem.htm"		

    elif event == "DemonicEssence" :
      if st.getQuestItemsCount(6031) >= 1 :
        htmltext = "DemonicEssence.htm"
      else:
        return "NoItem.htm"		

    elif event == "Temperature13" :
      if st.getQuestItemsCount(6028) >= 1 and st.getQuestItemsCount(6031) >= 1 :
        htmltext = "Temperature13.htm"
      else:
        return "NoItem.htm"		

    elif event == "TempHellFireOil1" :
      if st.getQuestItemsCount(6028) >= 1 and st.getQuestItemsCount(6031) >= 1 :
        st.takeItems(6028,1)
        st.takeItems(6031,1)
        if st.getRandom(100) < 100 :
          st.giveItems(6033,1)
          st.playSound("SkillSound5.liquid_success_01")		
          htmltext = "New.htm"		

    elif event == "TempHellFireOil2" :
      if st.getQuestItemsCount(6028) >= 1 and st.getQuestItemsCount(6031) >= 1 :
        st.takeItems(6028,1)
        st.takeItems(6031,1)
        if st.getRandom(100) < 45 :
          st.giveItems(6033,3)
          st.playSound("SkillSound5.liquid_success_01")
          htmltext = "New.htm"
        else:
          st.playSound("SkillSound5.liquid_fail_01")			
          return "New.htm"		
		  
    elif event == "TempHellFireOil3" :
      if st.getQuestItemsCount(6028) >= 1 and st.getQuestItemsCount(6031) >= 1 :
        st.takeItems(6028,1)
        st.takeItems(6031,1)	  
        if st.getRandom(100) < 15 :
          st.giveItems(6033,5)
          st.playSound("SkillSound5.liquid_success_01")
          htmltext = "New.htm"
        else:
          st.playSound("SkillSound5.liquid_fail_01")			
          return "New.htm"	

    elif event == "Lunargent" :
      if st.getQuestItemsCount(6029) >= 1 :
        htmltext = "Lunargent.htm"
      else:
        return "NoItem.htm"		

    elif event == "Midnight" :
      if st.getQuestItemsCount(6030) >= 1 :
        htmltext = "Midnight.htm"
      else:
        return "NoItem.htm"		

    elif event == "Temperature14" :
      if st.getQuestItemsCount(6029) >= 1 and st.getQuestItemsCount(6030) >= 1 :
        htmltext = "Temperature14.htm"
      else:
        return "NoItem.htm"		

    elif event == "TempNightMareOil1" :
      if st.getQuestItemsCount(6029) >= 1 and st.getQuestItemsCount(6030) >= 1 :
        st.takeItems(6029,1)
        st.takeItems(6030,1)
        if st.getRandom(100) < 100 :
          st.giveItems(6034,1)
          st.playSound("SkillSound5.liquid_success_01")		
          htmltext = "New.htm"		

    elif event == "TempNightMareOil2" :
      if st.getQuestItemsCount(6029) >= 1 and st.getQuestItemsCount(6030) >= 1 :
        st.takeItems(6029,1)
        st.takeItems(6030,1)
        if st.getRandom(100) < 45 :
          st.giveItems(6034,3)
          st.playSound("SkillSound5.liquid_success_01")
          htmltext = "New.htm"
        else:
          st.playSound("SkillSound5.liquid_fail_01")			
          return "New.htm"		
		  
    elif event == "TempNightMareOil3" :
      if st.getQuestItemsCount(6029) >= 1 and st.getQuestItemsCount(6030) >= 1 :
        st.takeItems(6029,1)
        st.takeItems(6030,1)	  
        if st.getRandom(100) < 15 :
          st.giveItems(6034,5)
          st.playSound("SkillSound5.liquid_success_01")
          htmltext = "New.htm"
        else:
          st.playSound("SkillSound5.liquid_fail_01")			
          return "New.htm"	

    elif event == "Quicksilver3" :
      if st.getQuestItemsCount(6019) >= 1 :
        htmltext = "Quicksilver3.htm"
      else:
        return "NoItem.htm"		

    elif event == "Temperature15" :
      if st.getQuestItemsCount(6029) >= 1 and st.getQuestItemsCount(6019) >= 1 :
        htmltext = "Temperature15.htm"
      else:
        return "NoItem.htm"		

    elif event == "TempPureSilver1" :
      if st.getQuestItemsCount(6029) >= 1 and st.getQuestItemsCount(6019) >= 1 :
        st.takeItems(6029,1)
        st.takeItems(6019,1)
        if st.getRandom(100) < 100 :
          st.giveItems(6320,1)
          st.playSound("SkillSound5.liquid_success_01")		
          htmltext = "New.htm"		

    elif event == "TempPureSilver2" :
      if st.getQuestItemsCount(6029) >= 1 and st.getQuestItemsCount(6019) >= 1 :
        st.takeItems(6029,1)
        st.takeItems(6019,1)
        if st.getRandom(100) < 45 :
          st.giveItems(6320,3)
          st.playSound("SkillSound5.liquid_success_01")
          htmltext = "New.htm"
        else:
          st.playSound("SkillSound5.liquid_fail_01")			
          return "New.htm"		
		  
    elif event == "TempPureSilver3" :
      if st.getQuestItemsCount(6029) >= 1 and st.getQuestItemsCount(6019) >= 1 :
        st.takeItems(6029,1)
        st.takeItems(6019,1)	  
        if st.getRandom(100) < 15 :
          st.giveItems(6320,5)
          st.playSound("SkillSound5.liquid_success_01")
          htmltext = "New.htm"
        else:
          st.playSound("SkillSound5.liquid_fail_01")			
          return "New.htm"	
		  
    return htmltext

 def onTalk (self,npc,player):
   htmltext = default
   st = player.getQuestState(qn)
   if not st : return htmltext

   npcId = npc.getNpcId()
   id = st.getState()
   if npcId == WESLEY :
      if id == State.CREATED :
         st.set("cond","0")
         htmltext = "30166-1.htm"
         if player.getLevel() < 57 :
            st.exitQuest(1)
            htmltext = "30166-2.htm"
      else :
         htmltext = "30166-3.htm"
   elif id == State.STARTED :
      htmltext = "1.htm"
   return htmltext

 def onKill(self,npc,player,isPet) :
     partyMember = self.getRandomPartyMemberState(player, State.STARTED)
     if not partyMember : return
     st = partyMember.getQuestState(qn)
     npcId = npc.getNpcId()
     # The quest rates increase the rates of dropping "something", but only one
     # entry will be chosen to drop per kill.  In order to not overshadow entries
     # that appear later in the list, first check with the sum of all entries to
     # see if any one of them will drop, then select which one...
     totalDropChance = DROPLIST[npcId][0]
     if totalDropChance > st.getRandom(100) :
        # At this point, we decided that one entry from this list will definitely be dropped
        # to select which one, get a random value in the range of the total chance and find
        # the first item that passes this range.
        itemToDrop = st.getRandom(totalDropChance)
        indexChance = 0
        for i in range(1,len(DROPLIST[npcId])) :
           item, chance = DROPLIST[npcId][i]
           indexChance += chance
           if indexChance > itemToDrop :
              # Now, we have selected which item to drop.  However, the quest rates are also
              # capable of giving this item a bonus amount, if its individual chance surpases
              # 100% after rates.  Apply rates to see for bonus amounts...
              # definitely give at least 1 item.  If the chance exceeds 100%, then give some
              # additional bonus...
              numItems, chance = divmod(chance*Config.RATE_DROP_QUEST,100)
              if st.getRandom(100) < chance : 
                 numItems += 1
              st.giveItems(item,int(numItems))
              st.playSound("ItemSound.quest_itemget")
              break
     return

QUEST       = Quest(QUEST_NUMBER, str(QUEST_NUMBER)+"_"+QUEST_NAME, QUEST_DESCRIPTION)

QUEST.addStartNpc(WESLEY)

QUEST.addTalkId(WESLEY)

QUEST.addTalkId(URN)

for i in DROPLIST.keys():
  QUEST.addKillId(i)