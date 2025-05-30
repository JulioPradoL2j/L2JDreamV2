package com.dream.game.templates.chars;

import com.dream.util.StatsSet;

public class L2CharTemplate
{
	private int baseSTR;
	private int baseCON;
	private int baseDEX;
	private int baseINT;
	private int baseWIT;
	private int baseMEN;
	private float baseHpMax;
	private float baseCpMax;
	private float baseMpMax;

	private float baseHpReg;

	private float baseMpReg;

	private int basePAtk;
	private int baseMAtk;
	private int basePDef;
	private int baseMDef;
	private int basePAtkSpd;
	private int baseMAtkSpd;
	public float baseMReuseRate;
	private int baseShldDef;
	private int baseAtkRange;
	private int baseShldRate;
	private int baseCritRate;
	private int baseMCritRate;
	public int baseRunSpd;
	private int baseWalkSpd;

	private int baseBreath;
	private int baseAggression;
	private int baseBleed;
	private int basePoison;
	private int baseStun;
	private int baseRoot;
	private int baseMovement;
	private int baseConfusion;
	private int baseSleep;
	private int baseFire;
	private int baseWind;
	private int baseWater;
	private int baseEarth;
	private int baseHoly;
	private int baseDark;

	public double baseAggressionVuln;
	public double baseBleedVuln;
	public double basePoisonVuln;
	public double baseStunVuln;
	public double baseRootVuln;
	public double baseMovementVuln;
	public double baseConfusionVuln;
	public double baseSleepVuln;
	public double baseFireVuln;
	public double baseWindVuln;
	public double baseWaterVuln;
	public double baseEarthVuln;
	public double baseHolyVuln;
	public double baseDarkVuln;
	public double baseCritVuln;

	public double baseCancelVuln;

	private boolean isUndead;

	private int baseMpConsumeRate;
	private int baseHpConsumeRate;

	private double collisionRadius;
	private double collisionHeight;
	private double fCollisionRadius;
	private double fCollisionHeight;

	public L2CharTemplate()
	{

	}

	public L2CharTemplate(StatsSet set)
	{

		baseSTR = set.getInteger("baseSTR");
		baseCON = set.getInteger("baseCON");
		baseDEX = set.getInteger("baseDEX");
		baseINT = set.getInteger("baseINT");
		baseWIT = set.getInteger("baseWIT");
		baseMEN = set.getInteger("baseMEN");
		baseHpMax = set.getFloat("baseHpMax");
		baseCpMax = set.getFloat("baseCpMax");
		baseMpMax = set.getFloat("baseMpMax");
		baseHpReg = set.getFloat("baseHpReg");
		baseMpReg = set.getFloat("baseMpReg");
		basePAtk = set.getInteger("basePAtk");
		baseMAtk = set.getInteger("baseMAtk");
		basePDef = set.getInteger("basePDef");
		baseMDef = set.getInteger("baseMDef");
		basePAtkSpd = set.getInteger("basePAtkSpd");
		baseMAtkSpd = set.getInteger("baseMAtkSpd");
		baseMReuseRate = set.getFloat("baseMReuseDelay", 1.f);
		baseShldDef = set.getInteger("baseShldDef");
		baseAtkRange = set.getInteger("baseAtkRange");
		baseShldRate = set.getInteger("baseShldRate");
		baseCritRate = set.getInteger("baseCritRate");
		baseMCritRate = set.getInteger("baseMCritRate", 8);
		baseRunSpd = set.getInteger("baseRunSpd");
		baseWalkSpd = set.getInteger("baseWalkSpd");

		baseBreath = set.getInteger("baseBreath", 100);
		baseAggression = set.getInteger("baseAggression", 0);
		baseBleed = set.getInteger("baseBleed", 0);
		basePoison = set.getInteger("basePoison", 0);
		baseStun = set.getInteger("baseStun", 0);
		baseRoot = set.getInteger("baseRoot", 0);
		baseMovement = set.getInteger("baseMovement", 0);
		baseConfusion = set.getInteger("baseConfusion", 0);
		baseSleep = set.getInteger("baseSleep", 0);
		baseFire = set.getInteger("baseFire", 0);
		baseWind = set.getInteger("baseWind", 0);
		baseWater = set.getInteger("baseWater", 0);
		baseEarth = set.getInteger("baseEarth", 0);
		baseHoly = set.getInteger("baseHoly", 0);
		baseDark = set.getInteger("baseDark", 0);
		baseAggressionVuln = set.getInteger("baseAggressionVuln", 1);
		baseBleedVuln = set.getInteger("baseBleedVuln", 1);
		basePoisonVuln = set.getInteger("basePoisonVuln", 1);
		baseStunVuln = set.getInteger("baseStunVuln", 1);
		baseRootVuln = set.getInteger("baseRootVuln", 1);
		baseMovementVuln = set.getInteger("baseMovementVuln", 1);
		baseConfusionVuln = set.getInteger("baseConfusionVuln", 1);
		baseSleepVuln = set.getInteger("baseSleepVuln", 1);
		baseFireVuln = set.getInteger("baseFireVuln", 1);
		baseWindVuln = set.getInteger("baseWindVuln", 1);
		baseWaterVuln = set.getInteger("baseWaterVuln", 1);
		baseEarthVuln = set.getInteger("baseEarthVuln", 1);
		baseHolyVuln = set.getInteger("baseHolyVuln", 1);
		baseDarkVuln = set.getInteger("baseDarkVuln", 1);
		baseCritVuln = set.getInteger("baseCritVuln", 1);
		baseCancelVuln = set.getInteger("baseCancelVuln", 1);

		isUndead = set.getInteger("isUndead", 0) == 1;

		baseMpConsumeRate = set.getInteger("baseMpConsumeRate", 0);
		baseHpConsumeRate = set.getInteger("baseHpConsumeRate", 0);

		collisionRadius = set.getDouble("collision_radius", 10);
		collisionHeight = set.getDouble("collision_height", 10);
		fCollisionRadius = set.getDouble("fcollision_radius", 10);
		fCollisionHeight = set.getDouble("fcollision_height", 10);

	}

	public int getBaseAggression()
	{
		return baseAggression;
	}

	public double getBaseAggressionVuln()
	{
		return baseAggressionVuln;
	}

	public int getBaseAtkRange()
	{
		return baseAtkRange;
	}

	public int getBaseBleed()
	{
		return baseBleed;
	}

	public double getBaseBleedVuln()
	{
		return baseBleedVuln;
	}

	public int getBaseBreath()
	{
		return baseBreath == 0 ? 100 : baseBreath;
	}

	public int getBaseCON()
	{
		return baseCON;
	}

	public int getBaseConfusion()
	{
		return baseConfusion;
	}

	public double getBaseConfusionVuln()
	{
		return baseConfusionVuln;
	}

	public float getBaseCpMax()
	{
		return baseCpMax;
	}

	public int getBaseCritRate()
	{
		return baseCritRate;
	}

	public int getBaseDark()
	{
		return baseDark;
	}

	public double getBaseDarkVuln()
	{
		return baseDarkVuln;
	}

	public int getBaseDEX()
	{
		return baseDEX;
	}

	public int getBaseEarth()
	{
		return baseEarth;
	}

	public double getBaseEarthVuln()
	{
		return baseEarthVuln;
	}

	public int getBaseFire()
	{
		return baseFire;
	}

	public double getBaseFireVuln()
	{
		return baseFireVuln;
	}

	public int getBaseHoly()
	{
		return baseHoly;
	}

	public double getBaseHolyVuln()
	{
		return baseHolyVuln;
	}

	public int getBaseHpConsumeRate()
	{
		return baseHpConsumeRate;
	}

	public float getBaseHpMax()
	{
		return baseHpMax;
	}

	public float getBaseHpReg()
	{
		return baseHpReg;
	}

	public int getBaseINT()
	{
		return baseINT;
	}

	public int getBaseMAtk()
	{
		return baseMAtk;
	}

	public int getBaseMAtkSpd()
	{
		return baseMAtkSpd;
	}

	public int getBaseMCritRate()
	{
		return baseMCritRate;
	}

	public int getBaseMDef()
	{
		return baseMDef;
	}

	public int getBaseMEN()
	{
		return baseMEN;
	}

	public int getBaseMovement()
	{
		return baseMovement;
	}

	public double getBaseMovementVuln()
	{
		return baseMovementVuln;
	}

	public int getBaseMpConsumeRate()
	{
		return baseMpConsumeRate;
	}

	public float getBaseMpMax()
	{
		return baseMpMax;
	}

	public float getBaseMpReg()
	{
		return baseMpReg;
	}

	public float getBaseMReuseRate()
	{
		return baseMReuseRate == 0.f ? 1.f : baseMReuseRate;
	}

	public int getBasePAtk()
	{
		return basePAtk;
	}

	public int getBasePAtkSpd()
	{
		return basePAtkSpd;
	}

	public int getBasePDef()
	{
		return basePDef;
	}

	public int getBasePoison()
	{
		return basePoison;
	}

	public double getBasePoisonVuln()
	{
		return basePoisonVuln;
	}

	public int getBaseRoot()
	{
		return baseRoot;
	}

	public double getBaseRootVuln()
	{
		return baseRootVuln;
	}

	public int getBaseRunSpd()
	{
		return baseRunSpd;
	}

	public int getBaseShldDef()
	{
		return baseShldDef;
	}

	public int getBaseShldRate()
	{
		return baseShldRate;
	}

	public int getBaseSleep()
	{
		return baseSleep;
	}

	public double getBaseSleepVuln()
	{
		return baseSleepVuln;
	}

	public int getBaseSTR()
	{
		return baseSTR;
	}

	public int getBaseStun()
	{
		return baseStun;
	}

	public double getBaseStunVuln()
	{
		return baseStunVuln;
	}

	public int getBaseWalkSpd()
	{
		return baseWalkSpd;
	}

	public int getBaseWater()
	{
		return baseWater;
	}

	public double getBaseWaterVuln()
	{
		return baseWaterVuln;
	}

	public int getBaseWind()
	{
		return baseWind;
	}

	public double getBaseWindVuln()
	{
		return baseWindVuln;
	}

	public int getBaseWIT()
	{
		return baseWIT;
	}

	public int getCollisionHeight()
	{
		return (int) collisionHeight;
	}

	public int getCollisionRadius()
	{
		return (int) collisionRadius;
	}

	public double getdCollisionHeight()
	{
		return collisionHeight;
	}

	public double getdCollisionRadius()
	{
		return collisionRadius;
	}

	public double getFCollisionHeight()
	{
		return fCollisionHeight;
	}

	public double getFCollisionRadius()
	{
		return fCollisionRadius;
	}

	public boolean isUndead()
	{
		return isUndead;
	}

	public void setBaseAggression(int _baseAggression)
	{
		baseAggression = _baseAggression;
	}

	public void setBaseAggressionRes(double _baseAggressionVuln)
	{
		baseAggressionVuln = _baseAggressionVuln;
	}

	public void setBaseAtkRange(int _baseAtkRange)
	{
		baseAtkRange = _baseAtkRange;
	}

	public void setBaseBleed(int _baseBleed)
	{
		baseBleed = _baseBleed;
	}

	public void setBaseBleedVuln(double _baseBleedVuln)
	{
		baseBleedVuln = _baseBleedVuln;
	}

	public void setBaseBreath(int _baseBreath)
	{
		baseBreath = _baseBreath;
	}

	public void setBaseCON(int _baseCON)
	{
		baseCON = _baseCON;
	}

	public void setBaseConfusion(int _baseConfusion)
	{
		baseConfusion = _baseConfusion;
	}

	public void setBaseConfusionVuln(double _baseConfusionVuln)
	{
		baseConfusionVuln = _baseConfusionVuln;
	}

	public void setBaseCpMax(float _baseCpMax)
	{
		baseCpMax = _baseCpMax;
	}

	public void setBaseCritRate(int _baseCritRate)
	{
		baseCritRate = _baseCritRate;
	}

	public void setBaseDark(int _baseDark)
	{
		baseDark = _baseDark;
	}

	public void setBaseDarkVuln(double _baseDarkVuln)
	{
		baseDarkVuln = _baseDarkVuln;
	}

	public void setBaseDEX(int _baseDEX)
	{
		baseDEX = _baseDEX;
	}

	public void setBaseEarth(int _baseEarth)
	{
		baseEarth = _baseEarth;
	}

	public void setBaseEarthVuln(double _baseEarthVuln)
	{
		baseEarthVuln = _baseEarthVuln;
	}

	public void setBaseFire(int _baseFire)
	{
		baseFire = _baseFire;
	}

	public void setBaseFireVuln(double _baseFireVuln)
	{
		baseFireVuln = _baseFireVuln;
	}

	public void setBaseHoly(int _baseHoly)
	{
		baseHoly = _baseHoly;
	}

	public void setBaseHolyVuln(double _baseHolyVuln)
	{
		baseHolyVuln = _baseHolyVuln;
	}

	public void setBaseHpConsumeRate(int _baseHpConsumeRate)
	{
		baseHpConsumeRate = _baseHpConsumeRate;
	}

	public void setBaseHpMax(float _baseHpMax)
	{
		baseHpMax = _baseHpMax;
	}

	public void setBaseHpReg(float _baseHpReg)
	{
		baseHpReg = _baseHpReg;
	}

	public void setBaseINT(int _baseINT)
	{
		baseINT = _baseINT;
	}

	public void setBaseMAtk(int _baseMAtk)
	{
		baseMAtk = _baseMAtk;
	}

	public void setBaseMAtkSpd(int _baseMAtkSpd)
	{
		baseMAtkSpd = _baseMAtkSpd;
	}

	public void setBaseMCritRate(int _baseMCritRate)
	{
		baseMCritRate = _baseMCritRate;
	}

	public void setBaseMDef(int _baseMDef)
	{
		baseMDef = _baseMDef;
	}

	public void setBaseMEN(int _baseMEN)
	{
		baseMEN = _baseMEN;
	}

	public void setBaseMovement(int _baseMovement)
	{
		baseMovement = _baseMovement;
	}

	public void setBaseMovementVuln(double _baseMovementVuln)
	{
		baseMovementVuln = _baseMovementVuln;
	}

	public void setBaseMpConsumeRate(int _baseMpConsumeRate)
	{
		baseMpConsumeRate = _baseMpConsumeRate;
	}

	public void setBaseMpMax(float _baseMpMax)
	{
		baseMpMax = _baseMpMax;
	}

	public void setBaseMpReg(float _baseMpReg)
	{
		baseMpReg = _baseMpReg;
	}

	public void setBaseMReuseRate(float _baseMReuseRate)
	{
		baseMReuseRate = _baseMReuseRate;
	}

	public void setBasePAtk(int _basePAtk)
	{
		basePAtk = _basePAtk;
	}

	public void setBasePAtkSpd(int _basePAtkSpd)
	{
		basePAtkSpd = _basePAtkSpd;
	}

	public void setBasePDef(int _basePDef)
	{
		basePDef = _basePDef;
	}

	public void setBasePoison(int _basePoison)
	{
		basePoison = _basePoison;
	}

	public void setBasePoisonVuln(double _basePoisonVuln)
	{
		basePoisonVuln = _basePoisonVuln;
	}

	public void setBaseRoot(int _baseRoot)
	{
		baseRoot = _baseRoot;
	}

	public void setBaseRootVuln(double _baseRootVuln)
	{
		baseRootVuln = _baseRootVuln;
	}

	public void setBaseRunSpd(int _baseRunSpd)
	{
		baseRunSpd = _baseRunSpd;
	}

	public void setBaseShldDef(int _baseShldDef)
	{
		baseShldDef = _baseShldDef;
	}

	public void setBaseShldRate(int _baseShldRate)
	{
		baseShldRate = _baseShldRate;
	}

	public void setBaseSleep(int _baseSleep)
	{
		baseSleep = _baseSleep;
	}

	public void setBaseSleepVuln(double _baseSleepVuln)
	{
		baseSleepVuln = _baseSleepVuln;
	}

	public void setBaseSTR(int _baseSTR)
	{
		baseSTR = _baseSTR;
	}

	public void setBaseStun(int _baseStun)
	{
		baseStun = _baseStun;
	}

	public void setBaseStunVuln(double _baseStunVuln)
	{
		baseStunVuln = _baseStunVuln;
	}

	public void setBaseWater(int _baseWater)
	{
		baseWater = _baseWater;
	}

	public void setBaseWaterVuln(double _baseWaterVuln)
	{
		baseWaterVuln = _baseWaterVuln;
	}

	public void setBaseWind(int _baseWind)
	{
		baseWind = _baseWind;
	}

	public void setBaseWindVuln(double _baseWindVuln)
	{
		baseWindVuln = _baseWindVuln;
	}

	public void setBaseWIT(int _baseWIT)
	{
		baseWIT = _baseWIT;
	}

	public void setCollisionHeight(double _collisionHeight)
	{
		collisionHeight = _collisionHeight;
	}

	public void setCollisionRadius(double _collisionRadius)
	{
		collisionRadius = _collisionRadius;
	}

	public void setUndead(boolean _isUndead)
	{
		isUndead = _isUndead;
	}
}