package app.sleepdiary.offline.application

import android.content.Context
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.repository.DiaryUiStore
import app.sleepdiary.offline.data.repository.RoomDiaryUiStore

/** Process-scoped dependencies. Durable state remains in Room, not this container. */
class AppContainer internal constructor(
    context: Context,
    databaseFactory: (Context) -> SleepDiaryDatabase,
) {
    constructor(context: Context) : this(context, SleepDiaryDatabase::open)

    private val applicationContext = context.applicationContext
    internal val database: SleepDiaryDatabase by lazy { databaseFactory(applicationContext) }
    val diaryStore: DiaryUiStore by lazy { RoomDiaryUiStore(database) }
    val services: AppServices by lazy { AppServices(applicationContext, database) }
}
