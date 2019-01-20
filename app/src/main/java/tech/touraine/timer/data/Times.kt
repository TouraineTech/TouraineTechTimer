package tech.touraine.timer.data

data class Times(val times: List<Time>, val rooms: List<String>)

data class Time(val time: String, val talk: Boolean)