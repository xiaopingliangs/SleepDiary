package app.sleepdiary.offline.application

import android.content.Context
import android.content.ContextWrapper
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.sleepdiary.offline.SleepDiaryApplication
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.repository.SaveResult
import app.sleepdiary.offline.diary
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AppContainerTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test fun manifestUsesApplicationOwnedContainer() {
        val application = context as SleepDiaryApplication
        assertSame(application.container, application.container)
    }

    @Test fun diaryAndBackupShareOneLazyDatabaseUsingApplicationContext() = runBlocking {
        var opened = 0
        val container = AppContainer(ContextWrapper(context)) { supplied ->
            assertSame(context, supplied)
            opened++
            Room.inMemoryDatabaseBuilder(supplied, SleepDiaryDatabase::class.java).build()
        }
        assertEquals(0, opened)
        try {
            val store = container.diaryStore
            val services = container.services
            assertSame(store, container.diaryStore)
            assertSame(services, container.services)
            assertEquals(1, opened)
            val saved = store.save(diary().copy(comment = "中文 English 123"), false) as SaveResult.Success
            val exported = services.backup.preview(services.backup.export())
            assertEquals(listOf(saved.entry), exported.entries)
        } finally {
            container.database.close()
        }
    }

    @Test fun replacingContainerPreservesSavedDataAndOnboardingOnDisk() = runBlocking {
        val databaseName = "container-test-${UUID.randomUUID()}.db"
        fun newContainer() = AppContainer(context) {
            Room.databaseBuilder(it, SleepDiaryDatabase::class.java, databaseName).build()
        }
        val first = newContainer()
        try {
            first.diaryStore.completeOnboarding()
            val saved = first.diaryStore.save(diary(), false) as SaveResult.Success
            first.database.close()
            val second = newContainer()
            try {
                assertEquals(listOf(saved.entry), second.diaryStore.entries())
                assertTrue(second.diaryStore.state().hasCompletedOnboarding)
                assertEquals(listOf(saved.entry), second.services.backup.snapshot().entries)
            } finally {
                second.database.close()
            }
        } finally {
            first.database.close()
            context.deleteDatabase(databaseName)
        }
    }
}
