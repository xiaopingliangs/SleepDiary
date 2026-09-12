package app.sleepdiary.offline.application

import android.content.Context
import app.sleepdiary.offline.data.backup.BackupRepository
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.share.DiaryShare

class AppServices(context: Context, database: SleepDiaryDatabase) {
    val backup = BackupRepository(database, context.applicationContext)
    val share = DiaryShare(context.applicationContext, database)
}
