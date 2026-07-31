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
        "终于等到你了~",
        "主人好~今天也要元气满满哦~",
        "哇~是主人~",
        "回来啦~抱抱~",
        "今天过得怎么样呀~",
        "看到主人就开心~"
    ), personalityTags)

    fun pet(personalityTags: List<PersonalityTag>): String = pick(listOf(
        "好舒服呀~",
        "再摸摸我嘛~",
        "最喜欢主人了~",
        "嘿嘿~好开心~",
        "主人的手好温暖~",
        "嗯~好幸福~",
        "再来一次嘛~",
        "舒服得想睡觉了~",
        "主人最好了~",
        "摸摸头~乖~"
    ), personalityTags)

    fun feed(personalityTags: List<PersonalityTag>): String = pick(listOf(
        "小鱼干！我最爱了~",
        "好香好香~",
        "谢谢主人~",
        "开饭啦~好期待~",
        "真好吃~",
        "啊呜~吃光光~",
        "主人最疼我啦~",
        "好好吃~再来一口嘛~",
        "吃饱饱~好满足~",
        "今天的饭饭真香~"
    ), personalityTags)

    fun periodLink(personalityTags: List<PersonalityTag>): String = pick(listOf(
        "我会陪着你的~",
        "主人辛苦了~多休息哦~",
        "抱抱~很快就好了~",
        "记得喝热水哦~",
        "不要勉强自己~我守着你~",
        "肚子暖暖的~会舒服一些~",
        "今天的你也要被温柔对待~",
        "难受就说出来~我听着呢~"
    ), personalityTags)

    fun dailyQuote(personalityTags: List<PersonalityTag>, quote: String): String {
        return pick(listOf(
            "今天想跟主人说：$quote",
            "看到一句话觉得很适合现在：$quote",
            "主人~$quote",
            "送给主人一句话：$quote",
            "悄悄告诉你~$quote"
        ), personalityTags)
    }

    fun checkin(personalityTags: List<PersonalityTag>): String = pick(listOf(
        "主人真棒~",
        "又打卡啦~好习惯~",
        "坚持就是胜利~",
        "主人越来越健康了~",
        "今日任务完成~夸夸主人~",
        "又进步一点点~",
        "持之以恒~最厉害了~",
        "记录每一天~真好~"
    ), personalityTags)

    fun travelReturn(personalityTags: List<PersonalityTag>): String = pick(listOf(
        "我回来啦！想我了吗？",
        "旅途好开心~带了礼物哦~",
        "终于回到家了~还是家里好~",
        "外面的世界好精彩~",
        "回来第一件事就是找主人~",
        "行李里全是给你的小惊喜~",
        "走了好多路~脚丫子酸酸的~",
        "下次带我一起去嘛~"
    ), personalityTags)

    fun sleepy(personalityTags: List<PersonalityTag>): String = pick(listOf(
        "困了~想睡觉了~",
        "打个大哈欠~",
        "眼皮好沉~",
        "主人~陪我睡一会儿嘛~",
        "梦里见哦~",
        "呜~好困好困~",
        "蜷成一团~晚安啦~",
        "打个盹~充充电~"
    ), personalityTags)

    fun hungry(personalityTags: List<PersonalityTag>): String = pick(listOf(
        "肚子饿了~",
        "什么时候吃饭呀~",
        "咕噜咕噜~肚子在叫~",
        "主人~投喂时间到~",
        "饿得没力气了~",
        "想吃小鱼干~",
        "饭饭呢~饭饭在哪里~",
        "再不吃就饿扁啦~"
    ), personalityTags)

    fun playing(personalityTags: List<PersonalityTag>): String = pick(listOf(
        "一起玩吧~",
        "好开心呀~",
        "追尾巴~好好玩~",
        "主人陪我玩嘛~",
        "蹦蹦跳跳~停不下来~",
        "小球球~我最爱~",
        "再来一局~再来一局~",
        "今天精力满满~"
    ), personalityTags)
}
