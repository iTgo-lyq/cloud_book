package com.itgo.book_cloud.common

import android.app.Activity
import java.util.*

fun setTimeout(delay: Long, task: (timer: Timer) -> Unit): Timer {
    val timer = Timer()
    timer.schedule(object : TimerTask() {
        override fun run() {
            task.invoke(timer)
        }
    }, delay)
    return timer
}

fun setTimeout(activity: Activity, delay: Long, task: (timer: Timer) -> Unit): Timer {
    val timer = Timer()
    timer.schedule(object : TimerTask() {
        override fun run() {
            activity.runOnUiThread {
                task.invoke(timer)
            }
        }
    }, delay)
    return timer
}

fun setInterval(interval: Long, task: (timer: Timer) -> Unit): Timer {
    return setInterval(interval, interval, task)
}

fun setInterval(interval: Long, delay: Long, task: (timer: Timer) -> Unit): Timer {
    val timer = Timer()
    timer.schedule(object : TimerTask() {
        override fun run() {
            task.invoke(timer)
        }
    }, delay, interval)
    return timer
}

fun setInterval(activity: Activity?, interval: Long, task: (timer: Timer) -> Unit): Timer {
    return setInterval(activity, interval, interval, task)
}

fun setInterval(activity: Activity?, interval: Long, delay: Long, task: (timer: Timer) -> Unit): Timer {
    val timer = Timer()
    timer.schedule(object : TimerTask() {
        override fun run() {
            activity?.runOnUiThread {
                task.invoke(timer)
            }
        }
    }, delay, interval)
    return timer
}