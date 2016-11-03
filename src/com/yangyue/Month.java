package com.yangyue;

/**
 * Created by yangyue on 2016/11/3.
 */
public enum Month {
    January(31),
    February(28),
    March(31),
    April(30),
    May(31),
    June(30),
    July(31),
    August(31),
    September(30),
    October(31),
    November(30),
    December(31);


    /**
     * 每月天数
     */
    private final int dayCount;

    /**
     * 私有化构造器
     * @param dayCount
     */
    Month(int dayCount){
        this.dayCount=dayCount;
    }

    /**
     * 获取每月天数
     * @return
     */
    public int getDayCount() {
        return dayCount;
    }
}
