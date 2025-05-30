import sys
from java.lang import System
from java.lang import Integer
from com.dream.game.model.quest import State
from com.dream.game.model.quest import QuestState
from com.dream.game.model.quest.jython import QuestJython as JQuest
from com.dream.game.network.serverpackets import SocialAction;
from com.dream import L2DatabaseFactory
from com.dream.game.network import Disconnection

qn = "5555_HeroSeller"

NPC=[50033]

#Quantidade de dias e item para a opcao 1.
DIAS_HERO1= 30
Moeda1= 9215
ItemCount1=30
#Quantidade de dias e item para a opcao 2.
DIAS_HERO2= 60
Moeda2= 9215
ItemCount2=55
#Quantidade de dias e item para a opcao 3.
DIAS_HERO3= 90
Moeda3= 9215
ItemCount3=70

#Id Reward Item Hero
DESTINY_CIRCLET = 6842

ClassId=88,89,90,91,92,93,94,95,96,97,98,99,100,101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,118
Race=0,1,2,3,4

QuestId     = 5555
QuestName   = "HeroSeller"
QuestDesc   = "custom"
InitialHtml = "1.htm"

print "Hero Seller Enabled..."

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)
 
 def onAdvEvent (self,event,npc,player) :
    htmltext = event
    st = player.getQuestState(qn)
    race = player.getRace().ordinal()
    classId = player.getClassId()
    id = classId.getId()	
    if not st: return

    def doHero(player,timeduration):
     heroTime = timeduration * 24 * 60 * 60 * 1000;
     try:
         con = L2DatabaseFactory.getInstance().getConnection()	
         upd = con.prepareStatement("REPLACE INTO character_herolist (charId, enddate) VALUES (?,?)")	
         upd.setInt(1, player.getObjectId())	
         upd.setLong(2, System.currentTimeMillis() + heroTime)	
         upd.execute()	
         DatabaseUtils.closeStatement(upd)	
         con.close()	
     except:
         player.sendMessage("Restart your account to change the hero status!")			 
         return	
	 
    if event == "1" :
      if not st.getPlayer().isHero():	
       if st.getPlayer().getLevel() >= 76 :	 
        if race in Race and id in ClassId :
         if st.getPlayer().isNoble() :
          if st.getQuestItemsCount(Moeda1) >= ItemCount1 :
                  st.takeItems(Moeda1,ItemCount1)			  
                  #st.giveItems(DESTINY_CIRCLET,1)			  
                  doHero(st.player, DIAS_HERO1)				  		  			  		  
                  Disconnection(player).defaultSequence(1000) ;				  		  
                  return "Thanks.htm"			  
          else:
           return "NoItem.htm"
         else:
          return "NoNobles.htm" 
        else:
         return "NoClass.htm"
       else:
         return "NoLvl.htm"
      else:
         return "Hero.htm"		 

    if event == "2" :
      if not st.getPlayer().isHero():	
       if st.getPlayer().getLevel() >= 76 :	 
        if race in Race and id in ClassId :
         if st.getPlayer().isNoble() :
          if st.getQuestItemsCount(Moeda2) >= ItemCount2 :
                  st.takeItems(Moeda2,ItemCount2)			  
                  #st.giveItems(DESTINY_CIRCLET,1)			  
                  doHero(st.player, DIAS_HERO2)				  		  
                  Disconnection(player).defaultSequence(1000) ;	
                  return "Thanks.htm"			  
          else:
           return "NoItem.htm"
         else:
          return "NoNobles.htm" 
        else:
         return "NoClass.htm"
       else:
         return "NoLvl.htm"
      else:
         return "Hero.htm"		

    if event == "3" :
      if not st.getPlayer().isHero():	
       if st.getPlayer().getLevel() >= 76 :	 
        if race in Race and id in ClassId :
         if st.getPlayer().isNoble() :
          if st.getQuestItemsCount(Moeda3) >= ItemCount3 :
                  st.takeItems(Moeda3,ItemCount3)			  
                  #st.giveItems(DESTINY_CIRCLET,1)			  
                  doHero(st.player, DIAS_HERO3)				  		  
                  Disconnection(player).defaultSequence(1000) ;	
                  return "Thanks.htm"			  
          else:
           return "NoItem.htm"
         else:
          return "NoNobles.htm" 
        else:
         return "NoClass.htm"
       else:
         return "NoLvl.htm"
      else:
         return "Hero.htm"				 
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