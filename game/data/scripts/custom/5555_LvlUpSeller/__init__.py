import sys
from com.dream.game.model.actor.instance import L2PcInstance
from java.util import Iterator
from com.dream.game.datatables.xml import SkillTable
from com.dream.tools.random import Rnd
from com.dream import L2DatabaseFactory
from com.dream.game.model.quest import State
from com.dream.game.model.quest import QuestState
from com.dream.game.model.quest.jython import QuestJython as JQuest

qn = "5555_LvlUp"

NPC=[50033]

PriceIDlvlUp= 9215
PriceCountlvlUp= 1
LVL_UP_COUNT= 1

PriceIDlvlUp1= 9215
PriceCountlvlUp1= 5
LVL_UP_COUNT1= 5

PriceIDlvlUp2= 9215
PriceCountlvlUp2= 7
LVL_UP_COUNT2= 10

PriceIDlvlUp3= 9215
PriceCountlvlUp3= 15
LVL_UP_COUNT3= 20

PriceIDlvlUp4= 9215
PriceCountlvlUp4= 20
LVL_UP_COUNT4= 81

QuestId     = 5555
QuestName   = "LvlUp"
QuestDesc   = "custom"
InitialHtml = "1.htm"

print "Lvl Up Seller Enabled..."

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)
 
 def onAdvEvent (self,event,npc,player) :
    htmltext = event
    st = player.getQuestState(qn)
    level = st.getPlayer().getLevel()
    if not st: return
	
    if event == "LvlUp" :
      if st.getPlayer().getLevel() >= 1:	
       if not st.getPlayer().getLevel() == 80 :	 
        if st.getQuestItemsCount(PriceIDlvlUp) >= PriceCountlvlUp :
                  delexp = (st.player.getStat().getExp() - st.player.getStat() .getExpForLevel(level+LVL_UP_COUNT))			  
                  st.player.getStat().addExp(-delexp)			  
                  st.playSound("ItemSound.quest_finish")			  
                  st.player.sendMessage("Congratulations you acquired 1 Lvl!")			  
                  st.takeItems(PriceIDlvlUp,PriceCountlvlUp)				  
                  return "Thanks.htm"			  
        else:
         return "NoItem.htm"
       else:
         return "HighLvl.htm"
      else:
         return "LowLvl.htm"	

    if event == "LvlUp1" :
      if st.getPlayer().getLevel() >= 1:	
       if not st.getPlayer().getLevel() >= 75 :	 
        if st.getQuestItemsCount(PriceIDlvlUp1) >= PriceCountlvlUp1 :
                  delexp = (st.player.getStat().getExp() - st.player.getStat() .getExpForLevel(level+LVL_UP_COUNT1))			  
                  st.player.getStat().addExp(-delexp)			  
                  st.playSound("ItemSound.quest_finish")			  
                  st.player.sendMessage("Congratulations you acquired 5 Lvl!")			  
                  st.takeItems(PriceIDlvlUp1,PriceCountlvlUp1)				  
                  return "Thanks.htm"			  
        else:
         return "NoItem.htm"
       else:
         return "HighLvl.htm"
      else:
         return "LowLvl.htm"	

    if event == "LvlUp2" :
      if st.getPlayer().getLevel() >= 1:	
       if not st.getPlayer().getLevel() > 70 :	 
        if st.getQuestItemsCount(PriceIDlvlUp2) >= PriceCountlvlUp2 :
                  delexp = (st.player.getStat().getExp() - st.player.getStat() .getExpForLevel(level+LVL_UP_COUNT2))			  
                  st.player.getStat().addExp(-delexp)			  
                  st.playSound("ItemSound.quest_finish")			  
                  st.player.sendMessage("Congratulations you acquired 10 Lvl!")			  
                  st.takeItems(PriceIDlvlUp2,PriceCountlvlUp2)				  
                  return "Thanks.htm"			  
        else:
         return "NoItem.htm"
       else:
         return "HighLvl.htm"
      else:
         return "LowLvl.htm"	
		 
    if event == "LvlUp3" :
      if st.getPlayer().getLevel() >= 1:	
       if not st.getPlayer().getLevel() >= 60 :	 
        if st.getQuestItemsCount(PriceIDlvlUp3) >= PriceCountlvlUp3 :
                  delexp = (st.player.getStat().getExp() - st.player.getStat() .getExpForLevel(level+LVL_UP_COUNT3))			  
                  st.player.getStat().addExp(-delexp)			  
                  st.playSound("ItemSound.quest_finish")			  
                  st.player.sendMessage("Congratulations you acquired 20 Lvl!")			  
                  st.takeItems(PriceIDlvlUp3,PriceCountlvlUp3)				  
                  return "Thanks.htm"			  
        else:
         return "NoItem.htm"
       else:
         return "HighLvl.htm"
      else:
         return "LowLvl.htm"	

    if event == "LvlUp4" :
      if st.getPlayer().getLevel() >= 1:	
       if not st.getPlayer().getLevel() == 80 :	 
        if st.getQuestItemsCount(PriceIDlvlUp4) >= PriceCountlvlUp4 :
                  delexp = (st.player.getStat().getExp() - st.player.getStat() .getExpForLevel(LVL_UP_COUNT4))			  
                  st.player.getStat().addExp(-delexp)			  
                  st.playSound("ItemSound.quest_finish")			  
                  st.player.sendMessage("Congratulations you acquired 80 Lvl!")			  
                  st.takeItems(PriceIDlvlUp4,PriceCountlvlUp4)				  
                  return "Thanks.htm"			  
        else:
         return "NoItem.htm"
       else:
         return "HighLvl.htm"
      else:
         return "LowLvl.htm"	
		 
    return htmltext		
	
 def onTalk (self,npc,player):
		htmltext = "<html><head><body>You can't buy while you are</body></html>"
		st = player.getQuestState(qn)
		if not st : return htmltext
		npcId = npc.getNpcId()		
		if st.player.getPvpFlag() > 0 :#Player Flag
			htmltext = "Flag.htm"
			st.exitQuest(1)
		elif st.player.getKarma() > 0 :#Player com Karma
			st.exitQuest(1)
			htmltext = "Karma.htm"
		elif st.player.isInCombat() :#Player modo de Combate
			st.exitQuest(1)
			htmltext = "Combat.htm"
		else:
			
			return InitialHtml
		return htmltext

QUEST       = Quest(QuestId,str(QuestId) + "_" + QuestName,QuestDesc)

for npcId in NPC:
 QUEST.addStartNpc(npcId)
 QUEST.addTalkId(npcId)	