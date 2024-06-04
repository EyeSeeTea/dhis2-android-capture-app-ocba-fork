package org.dhis2.usescases.teiDashboard.ui.mapper

import org.dhis2.R
import org.dhis2.commons.data.tuples.Pair
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.usescases.teiDashboard.DashboardProgramModel
import org.dhis2.usescases.teiDashboard.ui.model.InfoBarType
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class InfoBarMapperTest {

    private val resourceManager: ResourceManager = mock()
    private lateinit var mapper: InfoBarMapper

    @Before
    fun setUp() {
        whenever(resourceManager.getString(R.string.not_synced)) doReturn "Not synced"
        whenever(resourceManager.getString(R.string.sync_warning)) doReturn "Sync warning"
        whenever(resourceManager.getString(R.string.sync_error)) doReturn "Sync Error"
        whenever(resourceManager.getString(R.string.marked_follow_up)) doReturn "Marked for follow up"
        whenever(resourceManager.getString(R.string.enrollment_completed)) doReturn "Enrollment completed"
        whenever(resourceManager.getString(R.string.enrollment_cancelled)) doReturn "Enrollment cancelled"
        whenever(resourceManager.getString(R.string.sync)) doReturn "Sync"
        whenever(resourceManager.getString(R.string.sync_retry)) doReturn "Retry sync"
        whenever(resourceManager.getString(R.string.remove)) doReturn "remove"

        mapper = InfoBarMapper(resourceManager)
    }

    @Test
    fun shouldShowSyncInfoBar() {
        val model = createFakeModel(State.TO_UPDATE, EnrollmentStatus.ACTIVE)

        val result = mapper.map(
            InfoBarType.SYNC,
            model,
            { },
            true,
        )

        assertEquals(result.text, "Not synced")
    }

    @Test
    fun shouldShowEnrollmentStatusInfoBar() {
        val model = createFakeModel(State.TO_UPDATE, EnrollmentStatus.COMPLETED)

        val result = mapper.map(
            InfoBarType.ENROLLMENT_STATUS,
            model,
            {},
            true,
        )

        assertEquals(result.text, "Enrollment completed")
    }

    @Test
    fun shouldShowSFollowUpInfoBar() {
        val model = createFakeModel(State.TO_UPDATE, EnrollmentStatus.ACTIVE, true)

        val result = mapper.map(
            InfoBarType.FOLLOW_UP,
            model,
            {},
            true,
        )

        assertEquals(result.text, "Marked for follow up")
    }

    private fun createFakeModel(
        state: State,
        status: EnrollmentStatus,
        followup: Boolean = false,
    ): DashboardProgramModel {
        val attributeValues = listOf(
            Pair.create(getTEA("uid1", "First Name"), getTEAValue("Jonah")),
            Pair.create(getTEA("uid2", "Last Name"), getTEAValue("Hill")),
        )

        val model = DashboardProgramModel(
            setTei(state),
            setEnrollment(state, status, followup),
            emptyList<ProgramStage>(),
            emptyList<Event>(),
            attributeValues,
            emptyList<TrackedEntityAttributeValue>(),
            emptyList<OrganisationUnit>(),
            setPrograms(),
        )

        model.enrollmentState = state
        model.currentEnrollmentStatus = status

        return model
    }

    private fun getTEAValue(value: String) =
        TrackedEntityAttributeValue.builder()
            .value(value)
            .build()

    private fun getTEA(uid: String, value: String) =
        TrackedEntityAttribute.builder()
            .uid(uid)
            .displayFormName(value)
            .build()

    private fun setTei(state: State) = TrackedEntityInstance.builder()
        .uid("TEIUid")
        .organisationUnit("OrgUnit")
        .aggregatedSyncState(state)
        .build()

    private fun setEnrollment(
        state: State,
        status: EnrollmentStatus,
        followup: Boolean = false,
    ) = Enrollment.builder()
        .uid("EnrollmentUid")
        .syncState(state)
        .aggregatedSyncState(state)
        .status(status)
        .followUp(followup)
        .program("Program1Uid")
        .build()

    private fun setPrograms() = listOf<Program>(
        Program.builder()
            .uid("Program1Uid")
            .displayName("Program 1")
            .build(),
        Program.builder()
            .uid("Program2Uid")
            .displayName("Program 2")
            .build(),
    )
}
