package tech.touraine.timer.data

data class Speaker(
        val id: String,
        val name: String,
        val avatar: String,
        val github: String,
        val twitter: String,
        val bio: String,
        val company: String,
        val city: String,
        val confirmed: Boolean=false)