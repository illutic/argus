package com.argus.ingestion.di

import com.argus.ingestion.service.AlertIngestor
import com.argus.ingestion.service.ConsoleLoggingAlertIngestor
import com.argus.ingestion.service.DefaultAlertIngestor
import org.koin.dsl.module

val ingestionModule =
    module {
        single<AlertIngestor> { DefaultAlertIngestor() }
    }

val consoleIngestionModule =
    module {
        single<AlertIngestor> { ConsoleLoggingAlertIngestor() }
    }
