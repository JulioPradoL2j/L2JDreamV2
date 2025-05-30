import sys
from com.dream.game.model.actor.instance import L2PcInstance
from java.util import Iterator
from com.dream.game.datatables.xml import SkillTable
from com.dream.tools.random import Rnd
from com.dream import L2DatabaseFactory
from com.dream.game.model.base import Experience
from com.dream.game.model.quest import State
from com.dream.game.model.quest import QuestState
from com.dream.game.model.quest.jython import QuestJython as JQuest

qn = "5555_LvlDown"

NPC=[50033]

PriceIDlvlDown= 9215
PriceCountlvlDown= 1
LVL_DOWN_COUNT=1

PriceIDlvlDown1= 9215
PriceCountlvlDown1= 5
LVL_DOWN_COUNT1=5

PriceIDlvlDown2= 9215
PriceCountlvlDown2= 7
LVL_DOWN_COUNT2=10

PriceIDlvlDown3= 9215
PriceCountlvlDown3= 15
LVL_DOWN_COUNT3=20

PriceIDlvlDown4= 9215
PriceCountlvlDown4= 20
LVL_DOWN_COUNT4=1

QuestId     = 5555
QuestName   = "LvlDown"
QuestDesc   = "custom"
InitialHtml = "1.htm"

print "Lvl Down Seller Enabled..."

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)
 
 def onAdvEvent (self,event,npc,player) :
    htmltext = event
    st = player.getQuestState(qn)
    level = st.getPlayer().getLevel()
    if not st: return
	
    if event == "LvlDown" :
      if st.getPlayer().getLevel() > 1 :	
       if not st.getPlayer().getLevel() == 1 :	 
        if st.getQuestItemsCount(PriceIDlvlDown) >= PriceCountlvlDown :
                  st.getPlayer().getStat().removeExpAndSp((st.getPlayer().getExp() - Experience.LEVEL[st.getPlayer().getStat().getLevel() - LVL_DOWN_COUNT]), 0)			  	  
                  st.playSound("ItemSound.quest_finish")			  
                  st.player.sendMessage("Congratulations you are reduced 1 Lvl!")			  
                  st.takeItems(PriceIDlvlDown,PriceCountlvlDown)				  
                  return "Thanks.htm"			  
        else:
         return "NoItem.htm"
       else:
         return "HighLvl.htm"
      else:
         return "LowLvl.htm"	

    if event == "LvlDown1" :
      if st.getPlayer().getLevel() >= 6:	
       if not st.getPlayer().getLevel() <= 5 :	 
        if st.getQuestItemsCount(PriceIDlvlDown1) >= PriceCountlvlDown1 :
                  st.getPlayer().getStat().removeExpAndSp((st.getPlayer().getExp() - Experience.LEVEL[st.getPlayer().getStat().getLevel() - LVL_DOWN_COUNT1]), 0)			  	  		  
                  st.playSound("ItemSound.quest_finish")			  
                  st.player.sendMessage("Congratulations you are reduced 5 Lvl's!")			  
                  st.takeItems(PriceIDlvlDown1,PriceCountlvlDown1)				  
                  return "Thanks.htm"			  
        else:
         return "NoItem.htm"
       else:
         return "HighLvl.htm"
      else:
         return "LowLvl.htm"	

    if event == "LvlDown2" :
      if st.getPlayer().getLevel() >= 11:	
       if not st.getPlayer().getLevel() <= 10 :	 
        if st.getQuestItemsCount(PriceIDlvlDown2) >= PriceCountlvlDown2 :
                  st.getPlayer().getStat().removeExpAndSp((st.getPlayer().getExp() - Experience.LEVEL[st.getPlayer().getStat().getLevel() - LVL_DOWN_COUNT2]), 0)			  	  		  			  
                  st.playSound("ItemSound.quest_finish")			  
                  st.player.sendMessage("Congratulations you are reduced 10 Lvl's!")			  
                  st.takeItems(PriceIDlvlDown2,PriceCountlvlDown2)				  
                  return "Thanks.htm"			  
        else:
         return "NoItem.htm"
       else:
         return "HighLvl.htm"
      else:
         return "LowLvl.htm"	

    if event == "LvlDown3" :
      if st.getPlayer().getLevel() >= 21:	
       if not st.getPlayer().getLevel() <= 20 :	 
        if st.getQuestItemsCount(PriceIDlvlDown3) >= PriceCountlvlDown3 :
                  st.getPlayer().getStat().removeExpAndSp((st.getPlayer().getExp() - Experience.LEVEL[st.getPlayer().getStat().getLevel() - LVL_DOWN_COUNT3]), 0)			  	  		  	  
                  st.playSound("ItemSound.quest_finish")			  
                  st.player.sendMessage("Congratulations you are reduced 20 Lvl's!")			  
                  st.takeItems(PriceIDlvlDown3,PriceCountlvlDown3)				  
                  return "Thanks.htm"			  
        else:
         return "NoItem.htm"
       else:
         return "HighLvl.htm"
      else:
         return "LowLvl.htm"	

    if event == "LvlDown4" :
      if st.getPlayer().getLevel() > 1 :	
       if not st.getPlayer().getLevel() == 1 :	 
        if st.getQuestItemsCount(PriceIDlvlDown4) >= PriceCountlvlDown4 :
                  delexp = (st.player.getStat().getExp() - st.player.getStat() .getExpForLevel(LVL_DOWN_COUNT4))			  
                  st.player.getStat().addExp(-delexp)			  
                  st.playSound("ItemSound.quest_finish")			  
                  st.player.sendMessage("Congratulations you are reduced to lvl 1!")			  
                  st.takeItems(PriceIDlvlDown4,PriceCountlvlDown4)				  
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