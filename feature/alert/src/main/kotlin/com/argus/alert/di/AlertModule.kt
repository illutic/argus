package com.argus.alert.di

import com.argus.alert.console.ConsoleAlertSink
import com.argus.alert.sink.AlertSink
import com.argus.alert.slack.SlackAlertSink
import org.koin.core.module.Module
import org.koin.dsl.module

val alertModule: Module = alertSlackModule()

fun alertSlackModule(botTokenProvider: () -> String = { "" }): Module =
    module {
        single<AlertSink> { SlackAlertSink(botTokenProvider()) }
    }

val consoleAlertModule: Module =
    module {
        single<AlertSink> { ConsoleAlertSink() }
    }
