package net.opendasharchive.openarchive.core.di

import net.opendasharchive.openarchive.util.Analytics
import net.opendasharchive.openarchive.util.IAnalytics
import org.koin.dsl.module

val analyticsModule = module {
    single<IAnalytics> { Analytics(get()) }
}