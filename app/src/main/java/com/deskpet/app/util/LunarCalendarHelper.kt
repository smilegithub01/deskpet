package com.deskpet.app.util

import java.util.Calendar

object LunarCalendarHelper {

    data class FestivalInfo(
        val name: String,
        val petMessage: String?
    )

    /**
     * Returns today's festival if any, null otherwise.
     * Uses Gregorian calendar dates with approximate lunar festival dates.
     */
    fun getTodayFestival(): FestivalInfo? {
        val cal = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)

        return when {
            month == 1 && day == 1 -> FestivalInfo("元旦", "新年快乐！新的一年也要一起哦~")
            month == 2 && day == 14 -> FestivalInfo("情人节", "主人今天有人陪吗？不管怎样我都在哦~")
            month == 3 && day == 8 -> FestivalInfo("妇女节", "祝主人节日快乐~")
            month == 5 && isSecondSunday(cal) -> FestivalInfo("母亲节", null)
            month == 10 && day == 31 -> FestivalInfo("万圣节", "不给糖就捣蛋~")
            month == 12 && day == 24 -> FestivalInfo("平安夜", "平安喜乐~")
            month == 12 && day == 25 -> FestivalInfo("圣诞节", "圣诞快乐！主人收到礼物了吗？")
            month == 12 && day == 31 -> FestivalInfo("跨年", "一起跨年吧！新的一年也要在一起~")
            // Approximate lunar festival dates
            month == 2 && day in 4..6 -> FestivalInfo("春节", "新年好！团子给你拜年啦~")
            month == 6 && day in 9..11 -> FestivalInfo("端午节", "端午安康~记得吃粽子哦")
            month == 8 && day in 14..16 -> FestivalInfo("中秋节", "中秋快乐！一起赏月吧~")
            month == 8 && day in 6..8 -> FestivalInfo("七夕", "主人今天有人陪吗？不管怎样我都在哦~")
            else -> null
        }
    }

    private fun isSecondSunday(cal: Calendar): Boolean {
        return cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY &&
               cal.get(Calendar.DAY_OF_MONTH) in 8..14
    }
}
