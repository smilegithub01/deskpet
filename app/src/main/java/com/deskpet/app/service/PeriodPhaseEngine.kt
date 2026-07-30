package com.deskpet.app.service

import com.deskpet.app.data.db.PeriodLogDao
import com.deskpet.app.data.model.PeriodLog
import kotlinx.coroutines.flow.first
import java.util.Calendar

/**
 * Period cycle phases that influence pet behavior.
 */
enum class PeriodPhase(val displayName: String, val petMessage: String?) {
    NONE("非经期", null),
    MENSTRUAL("经期期", "我会陪着你的~"),
    FOLLICULAR("经后期", "主人今天气色好好~"),
    OVULATION("排卵期", null),
    LUTEAL("经前期", "主人想吃点什么吗？")
}

/**
 * Calculates the current period phase based on logged PeriodLog data.
 *
 * Requires at least 2 period records with 21-35 day intervals for
 * full 4-phase tracking. Otherwise only tracks menstrual phase (days 1-5).
 */
class PeriodPhaseEngine(private val periodLogDao: PeriodLogDao) {

    /**
     * Returns all period logs as a snapshot (non-reactive).
     */
    private suspend fun getAllLogs(): List<PeriodLog> = periodLogDao.getAll().first()

    /**
     * Calculates the current period phase.
     */
    suspend fun getCurrentPhase(): PeriodPhase {
        val logs = getAllLogs().sortedBy { it.date }
        if (logs.isEmpty()) return PeriodPhase.NONE

        val now = Calendar.getInstance()
        val todayStart = now.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // Find the most recent period log on or before today
        val recentLogs = logs.filter { it.date <= todayStart }
        if (recentLogs.isEmpty()) return PeriodPhase.NONE

        val lastPeriodDate = recentLogs.maxOf { it.date }
        val daysSinceLastPeriod = ((todayStart - lastPeriodDate) / (24 * 60 * 60 * 1000)).toInt()

        // Days 1-5: menstrual phase
        if (daysSinceLastPeriod in 0..4) {
            return PeriodPhase.MENSTRUAL
        }

        // Check if we have enough data for full cycle prediction
        val sortedDates = logs.map { it.date }.sorted()
        val cycleLengths = mutableListOf<Int>()
        for (i in 1 until sortedDates.size) {
            val diff = ((sortedDates[i] - sortedDates[i - 1]) / (24 * 60 * 60 * 1000)).toInt()
            if (diff in 21..35) {
                cycleLengths.add(diff)
            }
        }

        // If we don't have enough cycle data, only track menstrual phase
        if (cycleLengths.isEmpty()) return PeriodPhase.NONE

        val avgCycleLength = cycleLengths.average().toInt()

        // Calculate day in cycle (0-indexed from last period start)
        val dayInCycle = daysSinceLastPeriod

        // Don't track beyond the average cycle length
        if (dayInCycle >= avgCycleLength) return PeriodPhase.NONE

        return when {
            dayInCycle in 0..4 -> PeriodPhase.MENSTRUAL
            dayInCycle in 5..(avgCycleLength / 2 - 1) -> PeriodPhase.FOLLICULAR
            dayInCycle in (avgCycleLength / 2 - 2)..(avgCycleLength / 2 + 2) -> PeriodPhase.OVULATION
            dayInCycle >= avgCycleLength - 7 -> PeriodPhase.LUTEAL
            else -> PeriodPhase.FOLLICULAR
        }
    }

    /**
     * Returns days until next predicted period start, or -1 if cannot predict.
     */
    suspend fun daysUntilNextPeriod(): Int {
        val logs = getAllLogs().sortedBy { it.date }
        if (logs.size < 2) return -1

        val sortedDates = logs.map { it.date }.sorted()
        val cycleLengths = mutableListOf<Int>()
        for (i in 1 until sortedDates.size) {
            val diff = ((sortedDates[i] - sortedDates[i - 1]) / (24 * 60 * 60 * 1000)).toInt()
            if (diff in 21..35) {
                cycleLengths.add(diff)
            }
        }
        if (cycleLengths.isEmpty()) return -1

        val avgCycleLength = cycleLengths.average().toInt()
        val lastPeriodDate = sortedDates.last()

        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val daysSinceLastPeriod = ((now - lastPeriodDate) / (24 * 60 * 60 * 1000)).toInt()
        val daysUntilNext = avgCycleLength - daysSinceLastPeriod

        return if (daysUntilNext in 0..35) daysUntilNext else -1
    }

    /**
     * Returns true if we're within 3 days before the predicted next period.
     */
    suspend fun isApproachingPeriod(): Boolean {
        val days = daysUntilNextPeriod()
        return days in 0..3
    }
}
