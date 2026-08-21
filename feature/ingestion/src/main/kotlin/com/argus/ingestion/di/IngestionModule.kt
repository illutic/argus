package com.argus.ingestion.di

import com.argus.ingestion.normalizer.AlertNormalizer
import com.argus.ingestion.normalizer.DefaultAlertNormalizer
import com.argus.ingestion.queue.AlertQueue
import com.argus.ingestion.queue.ChannelAlertQueue
import com.argus.ingestion.service.AlertIngestor
import com.argus.ingestion.service.ConsoleLoggingAlertIngestor
import com.argus.ingestion.service.DefaultAlertIngestor
import org.koin.core.module.Module
import org.koin.dsl.module

val ingestionModule: Module =
    module {
        single<AlertQueue> { ChannelAlertQueue() }
        single<AlertNormalizer> { DefaultAlertNormalizer() }
        single<AlertIngestor> { DefaultAlertIngestor(get()) }
    }

val consoleIngestionModule: Module =
    module {
        single<AlertQueue> { ChannelAlertQueue() }
        single<AlertNormalizer> { DefaultAlertNormalizer() }
        single<AlertIngestor> { ConsoleLoggingAlertIngestor() }
    }
