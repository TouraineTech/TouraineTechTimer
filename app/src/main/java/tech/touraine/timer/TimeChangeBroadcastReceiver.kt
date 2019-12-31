package tech.touraine.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TimeChangeBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.getAction();

        if (action == Intent.ACTION_TIME_CHANGED ||
                action == Intent.ACTION_TIMEZONE_CHANGED) {
            val restartIntent = Intent(context, MainActivity::class.java).apply{
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                setAction(Intent.ACTION_MAIN)
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            context.startActivity(restartIntent)
        }
    }
}