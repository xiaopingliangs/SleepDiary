package app.sleepdiary.offline.application

import android.content.Context
import app.sleepdiary.offline.data.backup.BackupRepository
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.share.DiaryShare

class AppServices(context: Context, database: SleepDiaryDatabase) {
    private val educationIndex by lazy { app.sleepdiary.offline.data.education.EducationRegistryLoader.fromAssets(context.applicationContext.assets) }
    val backup = BackupRepository(database, context.applicationContext)
    val share = DiaryShare(context.applicationContext, database)
    val clinicianSummary = ClinicianSummaryService(database)
    val workspace = app.sleepdiary.offline.data.repository.WorkspaceRepository(database)
    val education = app.sleepdiary.offline.data.repository.EducationRepository(database, educationIndex)
    val weeklySummaries = app.sleepdiary.offline.data.repository.WeeklySummaryRepository(database)
}
