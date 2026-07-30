// app/src/main/java/com/deskpet/app/util/DialogueBank.kt
package com.deskpet.app.util

import com.deskpet.app.data.model.PersonalityTag
import kotlin.random.Random

/**
 * Scene-based dialogue pools for pet voice interaction.
 * Lines vary by personality tag for flavor.
 */
object DialogueBank {

    private fun pick(lines: List<String>, personalityTags: List<PersonalityTag>): String {
        val filtered = if (personalityTags.contains(PersonalityTag.LIVELY)) {
            lines.map { it.replace("~", "!") }
        } else if (personalityTags.contains(PersonalityTag.GENTLE)) {
            lines
        } else {
            lines
        }
        return filtered.random()
    }

    fun greeting(personalityTags: List<PersonalityTag>): String = pick(listOf(
        "主人你来啦~",
        "早安~",
        "辛苦一天啦~",
        "想你了~",
        "终于等到你了~"
    ), personalityTags)

    fun pet(personalityTags: List<PersonalityTag>): String = pick(listOf(
        "好舒服呀~",
        "再摸摸我嘛~",
        "最喜欢主人了~",
        "嘿嘿~好开心~",
        "主人的手好温暖~"
    ), personalityTags)

    fun feed(personalityTags: List<PersonalityTag>): String = pick(listOf(
        "小鱼干！我最爱了~",
        "好香好香~",
        "谢谢主人~",
        "开饭啦~好期待~",
        "真好吃~"
    ), personalityTags)

    fun periodLink(personalityTags: List<PersonalityTag>): String = pick(listOf(
        "我会陪着你的~",
        "主人辛苦了~多休息哦~",
        "抱抱~很快就好了~"
    ), personalityTags)

    fun dailyQuote(personalityTags: List<PersonalityTag>, quote: String): String {
        return pick(listOf(
            "今天想跟主人说：$quote",
            "看到一句话觉得很适合现在：$quote",
            "主人~$quote"
        ), personalityTags)
    }

    fun checkin(personalityTags: List<PersonalityTag>): String = pick(listOf(
        "主人真棒~",
        "又打卡啦~好习惯~",
        "坚持就是胜利~",
        "主人越来越健康了~"
    ), personalityTags)

    fun travelReturn(personalityTags: List<PersonalityTag>): String = pick(listOf(
        "我回来啦！想我了吗？",
        "旅途好开心~带了礼物哦~",
        "终于回到家了~还是家里好~"
    ), personalityTags)
}
