package com.example.data

data class Tip(
    val id: String,
    val text: String,
    val emoji: String,
    val category: String  // "focus", "task", "wellbeing", "movement", "adhd"
)

object TipsEngine {

    fun getTipsForProfile(state: GamificationState): List<Tip> {
        val tips = mutableListOf<Tip>()
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)

        // ── Universal tips (everyone gets these) ──────────────────────
        tips += universalTips

        // ── Peak energy tips ──────────────────────────────────────────
        val isPeakTime = when (state.peakEnergyTime) {
            "Morning" -> hour in 6..11
            "Afternoon" -> hour in 12..17
            "Evening" -> hour in 17..21
            else -> true
        }
        if (isPeakTime) {
            tips += Tip("peak_now", "This is your peak energy time. " +
                "Save your hardest task for right now — your brain is ready.",
                "⚡", "focus")
        } else {
            tips += Tip("off_peak", "Not your peak time? That's okay. " +
                "Routine tasks, email, and admin are perfect for right now.",
                "📋", "task")
        }

        // ── ADHD presentation tips ────────────────────────────────────
        when (state.adhdPresentation) {
            "Inattentive" -> tips += inattentiveTips
            "Hyperactive" -> tips += hyperactiveTips
            "Combined" -> {
                tips += inattentiveTips
                tips += hyperactiveTips
            }
        }

        // ── Co-occurring condition tips ───────────────────────────────
        if (state.coOccurring.contains("Anxiety")) tips += anxietyTips
        if (state.coOccurring.contains("Depression")) tips += depressionTips
        if (state.coOccurring.contains("Sleep issues")) {
            if (hour < 10) tips += sleepMorningTips
            if (hour >= 20) tips += sleepEveningTips
        }

        // ── Medication tips ───────────────────────────────────────────
        if (state.takesMedication == "Yes") {
            tips += medicationTips
        }

        // ── Activity level tips ───────────────────────────────────────
        if (state.activityLevel == "Sedentary" || state.activityLevel == "Light") {
            tips += sedentaryTips
        }

        // ── Morning motivation ────────────────────────────────────────
        if (hour in 6..9) tips += morningTips
        if (hour in 14..16) tips += afternoonSlumpTips

        // ── Streak-based tips ─────────────────────────────────────────
        if (state.dailyFlowStreak >= 3) {
            tips += Tip("streak_${state.dailyFlowStreak}",
                "${state.dailyFlowStreak}-day streak. You're building something real. " +
                "Consistency matters more than perfection.",
                "🔥", "wellbeing")
        }

        return tips.distinctBy { it.id }.shuffled()
    }

    fun getDailyTip(state: GamificationState): Tip {
        val available = getTipsForProfile(state)
        // Use today's date as seed for consistent daily tip
        val cal = java.util.Calendar.getInstance()
        val seed = cal.get(java.util.Calendar.YEAR) * 10000 +
            (cal.get(java.util.Calendar.MONTH) + 1) * 100 +
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        return available[seed % available.size]
    }

    // ── Tip pools ─────────────────────────────────────────────────────

    private val universalTips = listOf(
        Tip("disclaimer",
            "Tips in this app are general productivity and wellbeing suggestions, " +
            "not medical advice. For mental or physical health concerns, please " +
            "consult a qualified professional.",
            "ℹ️", "wellbeing"),
        Tip("body_double", "Struggling to start? Try body doubling — " +
            "work near another person (even on video) to help your brain engage.",
            "👥", "adhd"),
        Tip("2min_rule", "If a task takes under 2 minutes, do it now. " +
            "Your brain spends more energy avoiding it than doing it.",
            "⚡", "task"),
        Tip("next_action", "Can't start a task? Ask: what is the very next physical action? " +
            "Not 'work on project' — 'open the document'.",
            "🎯", "task"),
        Tip("time_blind", "ADHD brains struggle with time blindness. " +
            "Set a timer even for tasks you think will be quick.",
            "⏱", "adhd"),
        Tip("done_not_perfect", "Done is better than perfect. " +
            "A completed task you can improve beats a perfect one you never finish.",
            "✅", "focus"),
        Tip("brain_anchor", "The Working Memory Anchor at the top isn't decoration. " +
            "Use it every time you switch tasks to stay grounded.",
            "⚓", "adhd"),
        Tip("task_size", "If a task feels overwhelming, it's probably too big. " +
            "Break it into the smallest possible next step.",
            "🔪", "task"),
        Tip("reward_first", "Tell your brain what reward comes after the task. " +
            "Even a small one. It helps with task initiation.",
            "🎁", "focus"),
        Tip("water_tip", "Dehydration is one of the most common causes of poor focus. " +
            "When did you last have water?",
            "💧", "wellbeing"),
        Tip("external_structure", "ADHD brains work better with external structure. " +
            "The timer, the anchor, the task list — use all of them, every time.",
            "🏗", "adhd")
    )

    private val inattentiveTips = listOf(
        Tip("inatt_1", "Inattentive ADHD often looks like daydreaming or slow processing. " +
            "You're not lazy — your brain filters differently.",
            "💭", "adhd"),
        Tip("inatt_2", "Lost your place again? The Working Memory Anchor is for this. " +
            "Write it before you switch tasks, not after.",
            "⚓", "adhd"),
        Tip("inatt_3", "Hyperfocus is real. Set a timer so you resurface " +
            "before you've lost two hours on the wrong thing.",
            "⏰", "adhd"),
        Tip("inatt_4", "Transition between tasks is harder with inattentive ADHD. " +
            "Give yourself 2 minutes of nothing between tasks.",
            "🌊", "focus"),
        Tip("inatt_5", "If you're avoiding a task, curiosity helps more than discipline. " +
            "Ask: what's the most interesting part of this?",
            "🔍", "task")
    )

    private val hyperactiveTips = listOf(
        Tip("hyper_1", "Restless? Stand up while you work. " +
            "Movement helps hyperactive ADHD brains process better.",
            "🚶", "movement"),
        Tip("hyper_2", "Impulsive task-switching costs you more than staying on one thing. " +
            "When you want to switch, write it down instead — then keep going.",
            "📝", "focus"),
        Tip("hyper_3", "High energy right now? Use it. " +
            "Tackle your highest-priority task while the drive is there.",
            "⚡", "focus"),
        Tip("hyper_4", "Fidgeting isn't a problem — it's your brain regulating. " +
            "A fidget tool, music, or walking while thinking are all valid.",
            "🎵", "adhd"),
        Tip("hyper_5", "Talking through a problem out loud helps hyperactive ADHD brains. " +
            "Narrate what you're doing. It sounds odd. It works.",
            "🗣", "adhd")
    )

    private val anxietyTips = listOf(
        Tip("anxiety_1", "Anxiety and ADHD together often means catastrophising before starting. " +
            "The task is almost never as bad as the dread.",
            "🌊", "wellbeing"),
        Tip("anxiety_2", "Overwhelmed by the task list? Close your eyes, pick one, " +
            "and commit to just 5 minutes on it. That's all.",
            "🎯", "focus"),
        Tip("anxiety_3", "Feeling overwhelmed? Box breathing for 4 minutes before a hard task " +
            "may help calm your mind. Give it a try.",
            "🌬", "wellbeing"),
        Tip("anxiety_4", "Perfectionism and anxiety make a painful pair. " +
            "Lower the bar on purpose. Done badly is still done.",
            "📉", "focus"),
        Tip("anxiety_5", "'I'm anxious about X.' Naming feelings out loud can help create " +
            "a small sense of distance from them.",
            "🧠", "wellbeing")
    )

    private val depressionTips = listOf(
        Tip("dep_1", "Low motivation isn't a character flaw. " +
            "ADHD and depression together make starting genuinely harder. " +
            "One tiny task still counts.",
            "💚", "wellbeing"),
        Tip("dep_2", "On hard days, the goal isn't productivity. " +
            "The goal is staying in motion. Even slowly.",
            "🐢", "wellbeing"),
        Tip("dep_3", "Light exposure helps regulate mood and focus. " +
            "If you can, sit near a window or step outside for 5 minutes.",
            "☀️", "wellbeing"),
        Tip("dep_4", "Completing even one small task can create a sense of momentum. " +
            "Choose your easiest task and do it first today.",
            "✅", "task")
    )

    private val sleepMorningTips = listOf(
        Tip("sleep_am_1", "Poor sleep hits ADHD brains harder than neurotypical ones. " +
            "Be patient with yourself this morning. Start slow.",
            "😴", "wellbeing"),
        Tip("sleep_am_2", "Caffeine is most effective 90 minutes after waking. " +
            "If you had it immediately, a second cup around now makes sense.",
            "☕", "wellbeing")
    )

    private val sleepEveningTips = listOf(
        Tip("sleep_pm_1", "ADHD brains often get a second wind at night. " +
            "If you can, wind down screens 30 minutes before bed.",
            "🌙", "wellbeing"),
        Tip("sleep_pm_2", "Write tomorrow's top 3 tasks before you sleep. " +
            "It offloads your working memory and helps you actually switch off.",
            "📋", "wellbeing")
    )

    private val medicationTips = listOf(
        Tip("med_1", "Some people find their focus is stronger 1-3 hours after " +
            "taking ADHD medication. Notice your own patterns and plan accordingly. " +
            "Always follow your prescriber's guidance.",
            "💊", "focus"),
        Tip("med_2", "Medication helps with focus, not motivation. " +
            "You still need to point yourself at the task.",
            "🎯", "adhd")
    )

    private val sedentaryTips = listOf(
        Tip("sed_1", "Sitting for more than 90 minutes straight reduces cognitive performance. " +
            "Stand up, even for 2 minutes.",
            "🚶", "movement"),
        Tip("sed_2", "A 5-minute walk can help refresh your focus. " +
            "Even a short movement break makes a difference.",
            "🧠", "movement")
    )

    private val morningTips = listOf(
        Tip("morning_1", "Before you open email or social media, do one task. " +
            "Your brain is freshest right now.",
            "🌅", "focus"),
        Tip("morning_2", "Write your top 3 tasks for today before anything else. " +
            "Decision fatigue gets worse as the day goes on.",
            "📋", "task")
    )

    private val afternoonSlumpTips = listOf(
        Tip("slump_1", "The 2-4pm slump is biological, not a willpower failure. " +
            "It's a good time for routine tasks, not new thinking.",
            "😴", "wellbeing"),
        Tip("slump_2", "A 10-minute nap or 5 minutes of box breathing can reset " +
            "your afternoon completely. Not lazy — strategic.",
            "💤", "wellbeing")
    )
}
