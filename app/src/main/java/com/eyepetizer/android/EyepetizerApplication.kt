/*
 * Copyright (c) 2020. vipyinzhiwei <vipyinzhiwei@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.eyepetizer.android

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import androidx.work.WorkManager
import com.eyepetizer.android.extension.preCreateSession
import com.eyepetizer.android.ui.SplashActivity
import com.eyepetizer.android.ui.common.ui.WebViewActivity
import com.eyepetizer.android.ui.common.view.NoStatusFooter
import com.eyepetizer.android.util.DialogAppraiseTipsWorker
import com.eyepetizer.android.util.GlobalUtil
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager
import tv.danmaku.ijk.media.player.IjkMediaPlayer

/**
 * Eyepetizer自定义Application，在这里进行全局的初始化操作。
 *
 * @author vipyinzhiwei
 * @since  2020/4/28
 */
class EyepetizerApplication : Application() {

    init {
        //底层声明了一个接口作为参数，在这里可以直接使用lamda表达式作为参数传入，可以理解为一个匿名内部类（但有一些不同？）
        //这里可以直接用SmartRefreshLayout是因为他是一个静态方法去做配置，配置信息保存在类级别而不是对象级别中。
        SmartRefreshLayout.setDefaultRefreshInitializer { context, layout ->
            layout.setEnableLoadMore(true)
            layout.setEnableLoadMoreWhenContentNotFull(true)
        }

        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, layout ->
            layout.setEnableHeaderTranslationContent(true)
            MaterialHeader(context).setColorSchemeResources(R.color.blue, R.color.blue, R.color.blue)
        }

        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, layout ->
            layout.setEnableFooterFollowWhenNoMoreData(true)
            layout.setEnableFooterTranslationContent(true)
            layout.setFooterHeight(153f)
            layout.setFooterTriggerRate(0.6f)
            //静态成员属于类，可以在实例化之前先设置，设置引脚没内容加载时显示什么
            NoStatusFooter.REFRESH_FOOTER_NOTHING = GlobalUtil.getString(R.string.footer_not_more)
            NoStatusFooter(context).apply {
                setAccentColorId(R.color.colorTextPrimary)
                setTextTitleSize(16f)
            }
        }
    }

    //ContextWrapper 类的一个方法，允许您在 Context 对象创建之前对其进行初始化和自定义。
    /*
    多语言支持: 您可以在应用中切换不同的语言，而不需要重新启动应用。这通常涉及到在 attachBaseContext 中替换 Resources 对象，以便在运行时更改应用的语言。

字体大小和样式: 您可以在 attachBaseContext 中修改 Context 对象以应用自定义字体大小和样式，以满足用户的可访问性需求。

主题切换: 根据用户的主题偏好，您可以在 attachBaseContext 中设置不同的主题。

安全性: 在某些情况下，您可能需要在 attachBaseContext 中对 Context 进行安全性验证或其他自定义初始化。
    * */
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        //启用应用程序的分包（Multidex）支持,确保您的应用在使用大型依赖或库时能够正确运行
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        IjkPlayerManager.setLogLevel(if (BuildConfig.DEBUG) IjkMediaPlayer.IJK_LOG_WARN else IjkMediaPlayer.IJK_LOG_SILENT)
        WebViewActivity.DEFAULT_URL.preCreateSession()
        if (!SplashActivity.isFirstEntryApp && DialogAppraiseTipsWorker.isNeedShowDialog) {
            WorkManager.getInstance(this).enqueue(DialogAppraiseTipsWorker.showDialogWorkRequest)
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
}