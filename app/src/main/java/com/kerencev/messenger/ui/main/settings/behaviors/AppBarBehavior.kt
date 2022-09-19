package com.kerencev.messenger.ui.main.settings.behaviors

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import com.google.android.material.appbar.AppBarLayout
import com.kerencev.messenger.R

private const val TAG = "AppBarBehavior"

class AppBarBehavior(context: Context, attrs: AttributeSet?=null) : CoordinatorLayout.Behavior<View>(context, attrs) {

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        return dependency is NestedScrollView
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        if (dependency is NestedScrollView) {
            child.y = parent.y + dependency.height
        }
        return super.onDependentViewChanged(parent, child, dependency)
    }
}