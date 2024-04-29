package me.rhunk.snapenhance.core.features.impl.ui

import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import me.rhunk.snapenhance.core.features.Feature
import me.rhunk.snapenhance.core.features.FeatureLoadParams
import me.rhunk.snapenhance.core.util.hook.HookStage
import me.rhunk.snapenhance.core.util.hook.Hooker
import me.rhunk.snapenhance.core.util.hook.hook
import me.rhunk.snapenhance.core.util.ktx.getIdentifier

class CustomizeUI: Feature("Customize UI", loadParams = FeatureLoadParams.ACTIVITY_CREATE_SYNC) {
    private fun parseColor(color: String): Int? {
        return color.takeIf { it.isNotEmpty() }?.let {
            runCatching { Color.parseColor(color) }.getOrNull()
        }
    }

    override fun onActivityCreate() {
        val isAmoledMode = context.config.userInterface.amoledDarkMode.get()
        val isCustomizeUI = context.config.userInterface.customizeUi.globalState == true

        if (!isAmoledMode && !isCustomizeUI) return

        //TODO: color picker
        val customizeUIConfig = context.config.userInterface.customizeUi
        val effectiveTextColor by lazy { parseColor(customizeUIConfig.textColor.get()) }
        val effectivesendAndReceivedTextColor by lazy { parseColor(customizeUIConfig.sendAndReceivedTextColor.get()) }
        val effectiveBackgroundColor by lazy { parseColor(customizeUIConfig.backgroundColor.get()) }
        val effectiveBackgroundColorSurface by lazy { parseColor(customizeUIConfig.backgroundColorSurface.get()) }
        val effectiveActionMenuBackgroundColor by lazy { parseColor(customizeUIConfig.actionMenuBackgroundColor.get()) }
        val effectiveActionMenuRoundBackgroundColor by lazy { parseColor(customizeUIConfig.actionMenuRoundBackgroundColor.get()) }
        
        val attributeCache = mutableMapOf<String, Int>()

        fun getAttribute(name: String): Int {
            if (attributeCache.containsKey(name)) return attributeCache[name]!!
            return context.resources.getIdentifier(name, "attr").also { attributeCache[name] = it }
        }

        context.androidContext.theme.javaClass.getMethod("obtainStyledAttributes", IntArray::class.java).hook(
            HookStage.AFTER) { param ->
            val array = param.arg<IntArray>(0)
            val result = param.getResult() as TypedArray

            fun ephemeralHook(methodName: String, content: Any) {
                Hooker.ephemeralHookObjectMethod(result::class.java, result, methodName, HookStage.BEFORE) {
                    it.setResult(content)
                }
            }

            if (isAmoledMode) {
                when (array[0]) {
                    getAttribute("sigColorTextPrimary") -> {
                        ephemeralHook("getColor", 0xFFFFFFFF.toInt())
                    }
                    getAttribute("sigColorBackgroundMain"),
                    getAttribute("sigColorBackgroundSurface") -> {
                        ephemeralHook("getColor", 0xFF000000.toInt())
                    }
                    getAttribute("actionSheetBackgroundDrawable"),
                    getAttribute("actionSheetRoundedBackgroundDrawable") -> {
                        ephemeralHook("getDrawable", ColorDrawable(0xFF000000.toInt()))
                    }
                }
            }

            if (isCustomizeUI) {
                when (array[0]) {
                    getAttribute("sigColorTextPrimary") -> {
                        ephemeralHook("getColor", effectiveTextColor ?: return@hook)
                    }

                    getAttribute("sigColorBackgroundMain") -> {
                        ephemeralHook("getColor", effectiveBackgroundColor ?: return@hook)
                    }

                    getAttribute("sigColorBackgroundSurface") -> {
                        ephemeralHook("getColor", effectiveBackgroundColorSurface ?: return@hook)
                    }

                    getAttribute("actionSheetBackgroundDrawable") -> {
                        ephemeralHook("getDrawable", ColorDrawable(effectiveActionMenuBackgroundColor ?: return@hook))
                    }

                    getAttribute("actionSheetRoundedBackgroundDrawable") -> {
                        ephemeralHook("getDrawable", ColorDrawable(effectiveActionMenuRoundBackgroundColor ?: return@hook))
                    }
                    getAttribute("sigColorChatActivity") -> {
                        ephemeralHook("getColor", effectivesendAndReceivedTextColor ?: return@hook)
                    }
                    getAttribute("sigColorChatChat") -> {
                        ephemeralHook("getColor", effectivesendAndReceivedTextColor ?: return@hook)
                    }
                    getAttribute("sigColorChatPendingSending") -> {
                        ephemeralHook("getColor", effectivesendAndReceivedTextColor ?: return@hook)
                    }
                    getAttribute("sigColorChatSnapWithSound") -> {
                        ephemeralHook("getColor", effectivesendAndReceivedTextColor ?: return@hook)
                    }
                    getAttribute("sigColorChatSnapWithoutSound") -> {
                        ephemeralHook("getColor", effectivesendAndReceivedTextColor ?: return@hook)
                    }
                }
            }
        }
    }
}

                
