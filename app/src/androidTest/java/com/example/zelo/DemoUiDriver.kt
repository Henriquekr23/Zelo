package com.example.zelo

import android.app.Instrumentation
import android.os.Build
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import org.junit.Assert.fail

/** Walk Compose virtual nodes, refreshing the automation cache after recompositions. */
internal class DemoUiDriver(private val instrumentation: Instrumentation) {
    fun findText(text: String): AccessibilityNodeInfo? {
        if (Build.VERSION.SDK_INT >= 33) instrumentation.uiAutomation.clearCache()
        return findNode(instrumentation.uiAutomation.rootInActiveWindow, text)
    }

    fun awaitText(text: String) = search(text, click = false)
    fun click(text: String) = search(text, click = true)

    private fun search(text: String, click: Boolean) {
        val deadline = SystemClock.uptimeMillis() + 15000
        var attempt = 0
        while (SystemClock.uptimeMillis() < deadline) {
            var node = findText(text)
            if (node != null) {
                if (!click) return
                while (node != null && !node.isClickable) node = node.parent
                if (node?.isEnabled == true && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    instrumentation.waitForIdleSync()
                    SystemClock.sleep(250)
                    return
                }
            }
            // Allow the screen's own scroll effect to finish before searching other pages.
            if (attempt >= 4) {
                val action = if ((attempt - 4) % 8 < 4) AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
                    else AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
                if (scroll(instrumentation.uiAutomation.rootInActiveWindow, action)) SystemClock.sleep(500)
            }
            attempt++
            SystemClock.sleep(200)
        }
        fail("Expected ${if (click) "action" else "text"}: $text. Visible nodes: ${texts(instrumentation.uiAutomation.rootInActiveWindow)}")
    }

    private fun findNode(node: AccessibilityNodeInfo?, text: String): AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.isVisibleToUser && (node.text?.toString() == text || node.contentDescription?.toString() == text)) return node
        for (index in 0 until node.childCount) findNode(node.getChild(index), text)?.let { return it }
        return null
    }

    private fun scroll(node: AccessibilityNodeInfo?, action: Int): Boolean {
        if (node == null) return false
        if (node.isScrollable && node.performAction(action)) return true
        for (index in 0 until node.childCount) if (scroll(node.getChild(index), action)) return true
        return false
    }

    private fun texts(node: AccessibilityNodeInfo?): List<String> {
        if (node == null) return emptyList()
        return listOfNotNull(node.text?.toString()) +
            (0 until node.childCount).flatMap { texts(node.getChild(it)) }
    }
}
