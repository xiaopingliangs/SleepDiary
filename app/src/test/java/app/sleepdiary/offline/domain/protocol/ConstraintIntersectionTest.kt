package app.sleepdiary.offline.domain.protocol

import org.junit.Assert.*
import org.junit.Test

class ConstraintIntersectionTest {
    private val actions = setOf("A", "B", "C")
    private fun layers() = listOf(
        ConstraintLayer("protocol",ConstraintRole.PROTOCOL,actions,setOf("P")),
        ConstraintLayer("safety",ConstraintRole.SAFETY,setOf("A","B"),setOf("S")),
        ConstraintLayer("exception",ConstraintRole.EXCEPTION,setOf("B","C"),setOf("E"))
    )
    @Test fun intersectsInsteadOfLastRuleWins() {
        assertEquals(listOf("B"), (ConstraintIntersection.evaluate(actions,layers()) as ConstraintResult.Candidates).actions)
    }
    @Test fun orderDoesNotChangeActionsOrTrace() {
        assertEquals(ConstraintIntersection.evaluate(actions,layers()),ConstraintIntersection.evaluate(actions.reversed().toSet(),layers().reversed()))
    }
    @Test fun clinicianScopeCannotExpandSafety() {
        val result=ConstraintIntersection.evaluate(actions,layers()+ConstraintLayer("clinician",ConstraintRole.CLINICIAN_SCOPE,actions,setOf("D")))
        assertEquals(listOf("B"),(result as ConstraintResult.Candidates).actions)
    }
    @Test fun stricterScopeCanSuppressAllActions() {
        assertTrue(ConstraintIntersection.evaluate(actions,layers()+ConstraintLayer("clinician",ConstraintRole.CLINICIAN_SCOPE,setOf("C"),setOf("D"))) is ConstraintResult.NoCompatibleAction)
    }
    @Test fun missingSafetyIsInvalid() {
        assertTrue(ConstraintIntersection.evaluate(actions,layers().filter { it.role!=ConstraintRole.SAFETY }) is ConstraintResult.Invalid)
    }
    @Test fun unsupportedLayerCannotBecomeUnrestricted() {
        layers().indices.forEach { i ->
            val input=layers().toMutableList(); input[i]=input[i].copy(allowedActions=null)
            assertTrue(ConstraintIntersection.evaluate(actions,input) is ConstraintResult.Unresolved)
        }
    }
    @Test fun evaluatedEmptySetDoesNotBecomeUnknownOrContinue() {
        val input=layers().toMutableList();input[1]=input[1].copy(allowedActions=emptySet())
        assertTrue(ConstraintIntersection.evaluate(actions,input) is ConstraintResult.NoCompatibleAction)
    }
    @Test fun duplicateRoleOrSourceIsRejected() {
        assertTrue(ConstraintIntersection.evaluate(actions,layers()+layers()[0]) is ConstraintResult.Invalid)
        assertTrue(ConstraintIntersection.evaluate(actions,layers()+layers()[0].copy(sourceId="other")) is ConstraintResult.Invalid)
    }
    @Test fun unknownActionsAreRejectedNotIgnored() {
        assertTrue(ConstraintIntersection.evaluate(actions,layers()+ConstraintLayer("bad",ConstraintRole.CLINICIAN_SCOPE,setOf("NEW"),emptySet())) is ConstraintResult.Invalid)
    }
    @Test fun malformedIdentifiersAndEmptyCatalogAreRejected() {
        assertTrue(ConstraintIntersection.evaluate(emptySet(),layers()) is ConstraintResult.Invalid)
        assertTrue(ConstraintIntersection.evaluate(actions,layers().map { it.copy(sourceId=" ") }) is ConstraintResult.Invalid)
    }
    @Test fun tracesPreserveEveryRestrictionReason() {
        val result=ConstraintIntersection.evaluate(actions,layers()) as ConstraintResult.Candidates
        assertEquals(setOf("P","S","E"),result.trace.flatMap { it.reasonRefs }.toSet())
    }
    @Test fun returnedResultDoesNotAliasMutableInputs() {
        val mutable=mutableSetOf("A","B")
        val input=layers().map { it.copy(allowedActions=mutable) }
        val result=ConstraintIntersection.evaluate(actions,input) as ConstraintResult.Candidates
        mutable.clear()
        assertEquals(listOf("A","B"),result.actions)
        assertTrue(result.trace.all { it.allowedActions==listOf("A","B") })
    }
    private val stamp=ProposalWatermark("restore-epoch",1,2,"a".repeat(64),"b".repeat(64))
    @Test fun unchangedWatermarkNeverGrantsTreatmentAuthority() {
        assertEquals(ProposalFreshness.UNCHANGED_NOT_AUTHORIZED,ProposalFreshnessGuard.check(stamp,stamp))
    }
    @Test fun everyChangedWatermarkComponentInvalidatesProposal() {
        listOf(stamp.copy(backupEpoch="restored"),stamp.copy(stateRevision=2),stamp.copy(safetyRevision=3),
            stamp.copy(inputHash="c".repeat(64)),stamp.copy(pinnedBundleHash="d".repeat(64))).forEach {
            assertEquals(ProposalFreshness.STALE,ProposalFreshnessGuard.check(stamp,it))
        }
    }
    @Test fun restoringOldRevisionsStillInvalidatesProposal() {
        assertEquals(ProposalFreshness.STALE,ProposalFreshnessGuard.check(stamp,stamp.copy(backupEpoch="new",stateRevision=0,safetyRevision=0)))
    }
    @Test fun corruptWatermarkIsNotSimplyFresh() {
        listOf(stamp.copy(inputHash=""),stamp.copy(pinnedBundleHash="x".repeat(64)),stamp.copy(stateRevision=-1),
            stamp.copy(safetyRevision=-1),stamp.copy(backupEpoch="")).forEach {
            assertEquals(ProposalFreshness.INVALID,ProposalFreshnessGuard.check(it,it))
            assertEquals(ProposalFreshness.INVALID,ProposalFreshnessGuard.check(stamp,it))
        }
    }
}

