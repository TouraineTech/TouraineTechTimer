package tech.touraine.timer.data

data class Talk(
        val id: String,
        val createTime: String,
        val updateTime: String,
        val name: String,
        val abstract: String,
        val level: String,
        val format: String,
        val categories: String,
        val speakers: List<String>,
        val backup: Boolean,
        val rooms: List<Int>,
        val times: List<Int>)