package com.andruid.magic.helpfulsense.application

import android.app.Application
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.andruid.magic.helpfulsense.BuildConfig
import com.andruid.magic.helpfulsense.R
import com.andruid.magic.helpfulsense.data.ACTION_SHORTCUT_LAUNCH
import com.andruid.magic.helpfulsense.data.EXTRA_SHORTCUT_MESSAGE
import com.andruid.magic.helpfulsense.data.SHORTCUT_ACTION
import com.andruid.magic.helpfulsense.data.SHORTCUT_ALERT
import com.andruid.magic.helpfulsense.database.DbRepository
import com.andruid.magic.helpfulsense.ui.activity.HomeActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import splitties.systemservices.shortcutManager
import timber.log.Timber
import timber.log.Timber.DebugTree

@Suppress("unused")
class MyApplication : Application(), LifecycleObserver {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG)
            Timber.plant(DebugTree())
        DbRepository.init(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            Timber.d("onMoveToBackground: init shortcuts")
            initShortcuts()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun initShortcuts() {
        GlobalScope.launch {
            val shortcuts = mutableListOf<ShortcutInfo>()
            shortcuts.add(buildAlertShortcut())
            shortcuts.addAll(buildActionsShortcuts())
            shortcutManager?.dynamicShortcuts = shortcuts.toList()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private suspend fun buildActionsShortcuts(): List<ShortcutInfo> {
        val shortcuts = mutableListOf<ShortcutInfo>()
        val actionsList = DbRepository.getInstance().fetchActions()
        val actionMap = actionsList.groupBy { action -> action.category.name }
        actionMap.values.forEachIndexed { index, list ->
            val action = list.random()
            val id = SHORTCUT_ACTION.plus(index)
            val intent = Intent(this, HomeActivity::class.java)
                    .setAction(ACTION_SHORTCUT_LAUNCH)
                    .putExtra(ShortcutManagerCompat.EXTRA_SHORTCUT_ID, id)
                    .putExtra(EXTRA_SHORTCUT_MESSAGE, action.message)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            val shortcut = ShortcutInfo.Builder(this, id)
                    .setShortLabel(action.category.name)
                    .setLongLabel(action.message)
                    .setDisabledMessage(getString(R.string.action_disabled_message))
                    .setIcon(Icon.createWithResource(this, action.category.icon))
                    .setIntent(intent)
                    .build()
            shortcuts.add(shortcut)
            Timber.d("buildActionsShortcuts: added ${action.message}")
        }
        Timber.d("buildActionsShortcuts: size = ${shortcuts.size}")
        return shortcuts.toList()
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun buildAlertShortcut(): ShortcutInfo {
        val id = SHORTCUT_ALERT
        val intent = Intent(this, HomeActivity::class.java)
                .setAction(ACTION_SHORTCUT_LAUNCH)
                .putExtra(ShortcutManagerCompat.EXTRA_SHORTCUT_ID, id)
                .putExtra(EXTRA_SHORTCUT_MESSAGE, getString(R.string.shake_msg))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        return ShortcutInfo.Builder(this, id)
                .setShortLabel(getString(R.string.shortcut_alert_short_label))
                .setLongLabel(getString(R.string.shortcut_alert_long_label))
                .setDisabledMessage(getString(R.string.alert_disabled_message))
                .setIcon(Icon.createWithResource(this, R.drawable.ic_alert))
                .setIntent(intent)
                .build()
    }
}