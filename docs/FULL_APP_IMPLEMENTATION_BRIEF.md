# SleepDiary Full CBT-I Implementation Brief

## Mission

Continue the existing Android project at:

D:/Projects/SleepDiary

The current build is not the final product.

Current engineering report explicitly states:

- App 0.2.2
- CBT-I Protocol = NONE
- current product = diary + records + clinician summary + limited learning
- full CBT-I treatment loop is not implemented

Your task is now to build the COMPLETE CBT-I application described below.

This is no longer a diary increment.

Do not stop after adding education screens.

The target is a complete offline CBT-I treatment system with:
Baseline → Safety → Protocol → Treatment Plan → Weekly Review →
Cognitive / Relaxation → Exception handling → Clinician Bridge →
Maintenance / Relapse Prevention.

The final product must still preserve all existing diary/history/export/privacy behavior.

---

# 0. Read before changing anything

First read completely:

- AGENTS.md
- README.md
- all current specs
- DEVELOPMENT_PROGRESS.md
- docs/ENGINEERING_REPORT_20260921.md
- docs/FULL_APP_IMPLEMENTATION_ROADMAP.md
- all Safety / Diary / Weekly / Backup specs
- all source/evidence documents
- current Room schemas
- existing InputSnapshot / LocalExecutionState implementation

Also inspect the uploaded/available DeepSeek CBT-I Learning Game V4 source/content.

Do not begin coding before producing:

docs/FULL_APP_GAP_ANALYSIS.md

It must compare:

CURRENT 0.2.2
vs
TARGET FULL APP

module by module.

Do not reimplement already-working infrastructure.

---

# 1. Non-negotiable product identity

This is an adult CBT-I application.

It supports:

1. DIARY_ONLY
2. SELF_GUIDED CBT-I
3. CLINICIAN_ASSISTED CBT-I

A user may remain in DIARY_ONLY forever.

Reading learning material must never automatically start treatment.

Treatment starts only after:

- sufficient baseline
- safety review
- explicit user action
- treatment plan creation
- explicit confirmation

Minors are not supported by the current treatment protocol.

If a user is a minor:
show only a short message recommending professional medical/professional help.

Do not apply adult CBT-I algorithms to minors.

---

# 2. Source hierarchy

Keep source roles separate.

## Measurement

Consensus Sleep Diary / existing DIARY_1.0

Raw diary remains source of truth.

## Operational treatment protocol

VA CBT-I Therapist Manual
Manber et al., 2014

Role:

PROTOCOL_PRIMARY

Used for:

- stimulus control
- sleep restriction / TIB
- fixed wake time
- earliest bedtime
- weekly review
- treatment progression
- relevant cognitive / adherence procedures

Do not mix thresholds from other guidelines into this protocol.

## Safety

VA/DoD 2025

Used for:

- adapt
- delay
- safety
- clinician review
- excessive daytime sleepiness
- bipolar
- seizures
- fall risk
- pregnancy/postpartum
- acute mental health state
- substance use disorder
- PTSD exposure treatment
- OSA signals

## Evidence validation

AASM 2021

Used for:
evidence strength and component validation.

Do not build an operational algorithm by mixing AASM thresholds with VA protocol.

## Chinese localization

Existing Chinese adult insomnia/dCBT-I sources.

Used for:
Chinese-language/local-context cross-check.

Do not overwrite VA operational TIB algorithm.

---

# 3. Four architectural planes

Implement the full product as four conceptual planes.

## Data Plane

Raw diary
Safety
Workspace
Treatment snapshots
Treatment plans
Weekly reviews
Exception events
Clinical instructions
Education records
Audit history

## Treatment Control Plane

Baseline Engine
Treatment Metrics Engine
Safety Router
Exception Router
Protocol Engine
Plan Commit
Weekly Review
Maintenance

## Education Plane

DeepSeek V4 Knowledge Map
Interactive learning
Cognitive learning
Activity references
Review queue
Evidence/source display

Education does NOT control treatment.

## External Bridge Plane

Clinician Bridge
Manual export
External AI prompt package
Future Research Mode

External AI NEVER controls treatment.

Research data NEVER controls treatment.

---

# 4. Preserve current frozen behavior

Do not break:

DIARY_1.0

SAFETY_1.0

SAFETY_RULE_1.0

WEEKLY_1.0 neutral summaries

WORKSPACE_1.0

BACKUP_2.1 compatibility

existing history

existing clinician summary

existing JSON replace-restore

existing CSV

existing PNG privacy boundary

existing FileProvider

existing SAF flow

existing Room migrations

No fallbackToDestructiveMigration.

Raw historical diary data must never be rewritten.

---

# 5. Important separation: neutral weekly vs treatment metrics

Existing WEEKLY_1.0 is a neutral user-facing weekly summary.

Do NOT modify it into a treatment calculator.

Create a separate:

TREATMENT_METRICS schema.

A TreatmentMetricSnapshot must be immutable and traceable.

It must contain at minimum:

source diary entry ids
source diary versions
date range
algorithm version
protocol version
derived treatment metrics
missing-data state
validity state
created timestamp
revision/supersession information

Treatment Engine reads metric snapshots.

It does NOT directly reinterpret mutable diary rows.

---

# 6. Baseline Engine

Create a formal Baseline Engine.

Responsibilities:

- determine whether enough diary data exists
- validate treatment metric input
- identify missing/inconsistent data
- call current safety state
- identify major context constraints
- determine whether treatment may proceed

Possible outputs:

COLLECT_MORE_DATA
READY_FOR_SAFETY_REVIEW
READY_FOR_TREATMENT
CLINICIAN_REVIEW
HOLD

Do NOT invent missing-data rules.

If the adopted protocol/local frozen specs do not define:

- number of valid diary days
- allowed missing days
- incomplete-record handling

write these as explicit medical-rule gaps and stop automatic activation for that path until resolved.

Do not guess.

---

# 7. Treatment Safety must become continuous

Keep existing SafetyAssessment data.

Do not overwrite historical SAFETY_1.0.

Add treatment-time safety checks as a new versioned concept.

Safety must be re-evaluated:

- before treatment start
- at weekly review
- after major medication change
- after new excessive sleepiness
- after falls/dizziness
- acute illness
- new major mental-state change
- user-triggered “something changed”
- other protocol-defined events

Standard result:

CONTINUE
ADAPT
HOLD
CLINICIAN_REVIEW
URGENT_HELP

Priority:

URGENT_HELP
>
HOLD
>
CLINICIAN_REVIEW
>
ADAPT
>
Protocol recommendation
>
Education

Safety always overrides Protocol Engine.

Protocol never overrides Safety.

Diary access must remain available even when treatment is held.

---

# 8. Treatment modes

TreatmentMode must be separate from TreatmentState.

Modes:

SELF_GUIDED
CLINICIAN_ASSISTED

Do not encode these as treatment-stage enum values.

Clinician-assisted treatment uses the same protocol engine but can accept a user-entered clinician instruction.

The app cannot verify clinician identity.

Use wording equivalent to:

USER_ENTERED_CLINICIAN_INSTRUCTION

Never claim:
VERIFIED_CLINICIAN_ORDER

---

# 9. Treatment state machine

Implement a formal persisted state machine.

At minimum:

DIARY_ONLY

BASELINE_COLLECTING

BASELINE_REVIEW

SAFETY_REVIEW

READY_FOR_TREATMENT

PLAN_PENDING_CONFIRMATION

ACTIVE_TREATMENT

WEEKLY_REVIEW_DUE

PLAN_ADJUSTMENT_PENDING

ACTIVE_ADAPTED

CLINICIAN_REVIEW

USER_PAUSED

SAFETY_HOLD

MEDICAL_HOLD

MAINTENANCE

COMPLETED

RELAPSE_REVIEW

Transitions must be explicit and tested.

No UI screen may directly mutate treatment state.

All transitions go through one treatment-state service/controller.

---

# 10. Treatment Episode

Treatment is stored as an Episode.

A TreatmentEpisode should preserve:

episode id
start time
mode
protocol version
metrics version
safety rule version
exception rule version
status
current plan id
completion state
created timestamp
updated timestamp

Never overwrite an old completed episode.

Relapse/restart may create a new episode or an explicit relapse continuation according to the frozen design.

---

# 11. Treatment Plan

A plan is immutable after activation.

Required conceptual fields:

episodeId
revision
protocolVersion
ruleSetVersion

validFrom
validUntil

fixedWakeTime
tibMinutes
earliestBedtime

stimulusControlRuleVersion

mode
origin

inputSnapshotId
metricSnapshotId
safetySnapshotId

reason
createdAt

supersedesPlanId

status

Origins may include:

PROTOCOL
USER_CONFIRMED
USER_ENTERED_CLINICIAN_INSTRUCTION

Never silently edit a previous plan.

New adjustment = new revision.

---

# 12. Atomic plan commit

Plan activation must be transactional.

Flow:

raw data
→ immutable InputSnapshot
→ metric snapshot
→ Safety evaluation
→ candidate plan
→ validation
→ user preview
→ explicit confirmation
→ one Room transaction
→ readback verification
→ ACTIVE

Failure:
old plan stays active.

Never leave half-written treatment state.

Reuse existing InputSnapshot / LocalExecutionState infrastructure where appropriate.

Do not rebuild equivalent machinery unnecessarily.

---

# 13. Sleep Restriction / TIB operational protocol

Primary operational source:

VA CBT-I Therapist Manual.

Frozen known rules include:

baseline:
at least one week of daily sleep diary

initial TIB:
average TST from prior week

minimum initial TIB:
5 hours

fixed wake time:
selected based on real life schedule / protocol

earliest bedtime:
fixed wake time - TIB

Earliest bedtime is NOT mandatory bedtime.

If the person is not sleepy:
Stimulus Control still applies;
they may go to bed later.

Wake time remains fixed unless Safety/Adaptation overrides.

Weekly review occurs after at least one treatment week.

SE + Sleep Need protocol then determines:
maintain
+15 minutes
+30 minutes
-15 minutes
or hold/check causes.

Do not implement:
SE < X → always reduce.

---

# 14. Sleep Need Questionnaire rights gate

Important legal/content constraint:

The VA manual indicates the Sleep Need Questionnaire is reproduced by permission.

Current project may:

- explain its role
- store score bands if legitimately supplied
- explain the protocol logic

Do NOT copy the questionnaire wording into the app unless rights are confirmed.

Do NOT invent a similar four-question questionnaire and continue to call it the VA protocol.

Design a provider abstraction such as:

WeeklySleepNeedInputProvider

Possible future implementations:

LICENSED_IN_APP
CLINICIAN_ENTERED
OTHER_APPROVED_PROTOCOL_SOURCE

Until rights are resolved:

automatic self-guided weekly adjustment requiring that questionnaire must remain gated.

Do not hide this blocker.

Continue building all other full-app infrastructure.

---

# 15. Stimulus Control as executable treatment

Stimulus Control is not only an article.

Treatment mode must expose the actual rule set.

Core concepts:

sleepy → bed

awake for a while / sleep clearly not happening / frustration building
→ leave bed

sleepy again
→ return

fixed wake time
→ remains

Do NOT build a 20-minute timer.

Do NOT prompt:
“20 minutes have passed, leave bed.”

Night clock checking should not be required.

Provide an on-demand flow:

“I’m awake and don’t know what to do”

This calls Exception/Safety routing first.

---

# 16. Exception Router

Create a versioned Exception Rule Registry.

Two broad classes:

## Safety-affecting

excessive sleepiness
unintended dozing
dangerous driving/work
dizziness
fall risk
acute illness
medication change
bipolar/mania signal
seizure issue
OSA signal
substance-use problem
other safety rules

These can alter treatment state.

## Real-world adaptation

pain
no alternative resting place
external noise
travel
temporary overtime
shift work
phone
gaming
video
audiobook/podcast
caffeine
nicotine
alcohol context
exercise
sexual activity / masturbation
temporary stress
environment changes

Most of these should educate/adapt rather than change the core protocol.

Each exception rule requires:

ruleId
riskClass
sourceId
sourceLocator
version
disposition
protocolImpact
plain-language explanation

Allowed dispositions:

CONTINUE
ADAPT
HOLD
CLINICIAN_REVIEW
URGENT_HELP

No generative AI may create a new exception rule at runtime.

---

# 17. Fatigue vs Sleepiness

Make this a real treatment/safety concept.

Suggested structured states:

FATIGUE_ONLY

SLEEPY_BUT_CONTROLLED

UNINTENDED_DOZE

DANGER_OCCURRED

Do not define fatigue/sleepiness by presumed cause.

Use actual experience.

Dangerous sleepiness must override sleep-drive goals.

Never instruct a user to “push through” unsafe sleepiness just to preserve sleep pressure.

---

# 18. Safety nap

If the adopted verified protocol defines a safety-nap exception:

implement it only from the source-locked rule.

Do not turn it into a normal daily nap recommendation.

It belongs in Exception/Safety handling.

---

# 19. Weekly Review Engine

Weekly review is the main feedback loop.

Inputs include:

TreatmentMetricSnapshot
adherence/context
daytime sleepiness
Safety events
Exception events
illness
pain
medication changes
travel
stress
schedule changes
protocol-specific sleep-need input

Outputs:

KEEP
EXTEND
SHORTEN
HOLD
ADAPT
CLINICIAN_REVIEW

Treatment recommendation must include:

rule id
source
input snapshot
reason
protocol version

User sees a preview.

No plan change until explicit confirmation or accepted clinician-entered instruction.

---

# 20. Do not create a high-burden daily treatment log

Daily burden must stay low.

Morning:
sleep diary.

Always available:
“Something changed / I’m very sleepy / I have a problem”

Optional brief context should be requested only when clinically required.

Do not add:

daily sleep score
daily treatment score
streak
coins
gamification
REM/deep score
daily trend obsession

---

# 21. Cognitive Therapy

Integrate the V4 cognitive structure.

Keep:

automatic thought
→ true part
→ hidden extra inference
→ missing alternatives
→ more accurate wording

Only FINAL COMMITTED answers can influence later education.

Exploratory option clicks do NOT become beliefs.

Create a CognitiveFocusProfile only for educational prioritization.

It must never alter:
TIB
wake time
Safety
Protocol decisions.

Do not copy proprietary questionnaires without permission.

---

# 22. Relaxation / Arousal Reduction

Support an optional module.

Examples may include source-supported:

diaphragmatic breathing
progressive muscle relaxation
body scan
grounding
meditation
imagery

No single method is mandatory.

No daily completion score.

Do not turn relaxation into:
“you must relax successfully to sleep.”

It is an auxiliary skill.

---

# 23. Integrate DeepSeek V4 Education properly

Do NOT solve this by simply putting the 729 KB HTML into a WebView and creating a second data silo.

The long-term design should have a shared Education source of truth.

Extract/generate structured assets from V4 source:

knowledge map
chapters
scenes
stable IDs
source mapping
cognitive cards
activity-profile definitions
review items
evidence metadata

Prefer generated machine-readable assets such as:

education_content_v4.json
knowledge_registry_v4.json

Android Compose renders them natively.

The public/offline web edition and Android app should be generated from the same content source where practical.

Do not hand-copy hundreds of strings into Kotlin.

---

# 24. Education map

Preserve the V4 map concept:

Level 1:
sleep drive
circadian timing
arousal / bed association

plus:
support layer
safety/professional-help outer layer

Level 2:
treatment components

Level 3:
details

The map itself must remain labeled PRODUCT SYNTHESIS.

Do not claim this is an official VA diagram.

Education progress is independent from treatment authorization.

A user may browse freely.

---

# 25. Education records

Preserve privacy principle:

Save:
learning progress
final committed reflections
activity profiles explicitly saved
questions for doctor
review queue
final summary

Do NOT save:
click trails
expand/collapse events
time spent
quiz telemetry
hidden behavioral analytics

Use Room, not a second localStorage system.

Existing Workspace records should be reused/migrated where appropriate rather than duplicating equivalent data.

---

# 26. Clinician Bridge

Expand existing clinician summary.

Export may include, only with user selection:

recent diary
baseline
current episode
current treatment plan
fixed wake time
TIB
weekly metrics
daytime sleepiness
Safety flags
Exception events
medication/context changes
pain/context notes
questions for clinician

Do not diagnose.

Do not generate a prescription.

---

# 27. User-entered clinician instructions

Store clinician advice as user-entered source material.

Free text NEVER automatically changes treatment.

If user wants to apply it:

free text
→ user selects “apply as treatment change”
→ structured form
→ Safety check
→ plan preview
→ explicit confirmation
→ new TreatmentPlan revision

Label origin:
USER_ENTERED_CLINICIAN_INSTRUCTION

Do not claim clinician identity verification.

---

# 28. Maintenance and relapse prevention

Create a real end-of-treatment phase.

Do not display:
“cured”
or
“you are fixed”.

Maintenance should help create a personal plan containing:

what patterns usually cause trouble
what CBT-I principles helped
when to resume diary
when not to self-adjust
when to seek professional help
how to handle temporary bad nights

Treatment may then enter:

MAINTENANCE
→ COMPLETED

Later recurrence:
RELAPSE_REVIEW

Do not erase previous episode history.

---

# 29. External AI Prompt Bridge

Keep AI outside the treatment control plane.

Generate:

CBTI-Knowledge-for-AI.md
AI-咨询提示词.txt

Optionally add:

My-CBTI-Context-for-AI.md

Personal context must be opt-in field by field.

Do NOT include sensitive information automatically.

Possible selected fields:

current treatment phase
current plan
recent neutral summary
selected exception
user’s question

External AI output:

cannot write back
cannot activate plan
cannot adjust TIB
cannot change medication
cannot override Safety

AI is explanation only.

---

# 30. AI export validation

The final generated knowledge pack must contain human-readable source metadata.

Build must fail if output contains:

[object Object]
[object Array]
undefined
来源：null

Validate every exported source against Source Registry.

---

# 31. Research Mode

Future wearable/EEG integrations belong to:

Research Mode.

Examples:

Wear OS
Health Connect
EEG
sleep staging
raw sensor data

Research Mode must be isolated.

Research data may:
store
export
be analyzed separately

Research data must NOT:
change TIB
change wake time
change Safety result
change treatment plan

unless a future independent validated protocol is explicitly versioned and approved.

---

# 32. Home architecture

Final top-level Android navigation target:

TODAY
DIARY
TREATMENT
LEARN
ME

Exact visual implementation is your engineering decision.

State-aware TODAY screen:

DIARY_ONLY:
morning diary
history
learn
start CBT-I option

BASELINE:
morning diary
neutral baseline progress
why baseline matters

ACTIVE:
current weekly plan
fixed wake time
earliest bedtime
short SC reminder
morning diary
“something changed”
weekly review due date

Never show:
sleep score
REM/deep
red/green judgment
daily success percentage
streak

---

# 33. Backup

Design next full backup version, likely BACKUP_3.x.

Must remain backward-compatible with:

1.0
1.1
2.0
2.1

Include treatment business data:

TreatmentEpisode
TreatmentMetricSnapshot
TreatmentPlan / revisions
TreatmentWeeklyReview
TreatmentSafetyCheck
ExceptionEvent
ClinicalInstruction
Education progress/records
MaintenancePlan
audit-relevant persistent state

Do not restore transient LocalExecutionState from backup.

On restore:
generate a new local execution generation and require review before resuming active treatment.

Replace-restore remains atomic.

No merge unless separately designed later.

---

# 34. Privacy and permissions

Continue:

offline-first

no account
no cloud
no ads
no analytics
no telemetry

Manifest must not request:

INTERNET
ACCESS_NETWORK_STATE
MANAGE_EXTERNAL_STORAGE

Official links open in external browser.

Export only through user action.

No background upload.

---

# 35. Evidence and traceability

Every treatment rule must resolve to:

ruleId
sourceId
sourceLocator
protocolVersion
medicalReviewDate

Every activated plan must resolve to:

input snapshot
metric snapshot
Safety snapshot
rule set
protocol version
plan revision

Every Exception decision must resolve to:
exceptionRuleId + source/version.

UI must support:

“Why am I seeing this recommendation?”

and
“Source”.

No long copyrighted excerpts.

---

# 36. Versions

Create separate explicit versions for at least:

TREATMENT_METRICS

CBTI_PROTOCOL

TREATMENT_SAFETY

EXCEPTION_RULES

EDUCATION_CONTENT

BACKUP

Do not reuse app version as medical rule version.

---

# 37. Migration rules

Current Room DB is already real user data.

No destructive migration.

Never:
drop existing diary
rewrite old Safety
recalculate and overwrite old neutral weekly snapshots

Add new tables/schema with explicit migrations.

All migration tests must include:
old DB → new DB
realistic diary history
old backup restore
active/non-active treatment states

---

# 38. Implementation phases

Do NOT implement everything in one uncontrolled pass.

## Phase 0
Read + gap analysis
state-machine spec
data contract spec
requirements matrix

Commit.

## Phase 1
Treatment database model
state machine
episode/plan/audit skeleton

Tests.

Commit.

## Phase 2
Treatment metrics
baseline engine
immutable metric snapshots

Tests.

Commit.

## Phase 3
Continuous Safety
Exception Router
hold/resume

Tests.

Commit.

## Phase 4
Protocol Engine
candidate plan
atomic plan commit
initial TIB flow

Tests.

Commit.

## Phase 5
Stimulus Control treatment UI
active plan home
exception quick entry

Tests.

Commit.

## Phase 6
Weekly Review
plan revisions
clinician-entered adjustment

Tests.

Commit.

## Phase 7
Cognitive
Relaxation
treatment-stage suggestions

Tests.

Commit.

## Phase 8
Full V4 Education integration
native knowledge map
education records/review

Tests.

Commit.

## Phase 9
Clinician Bridge expansion

Tests.

Commit.

## Phase 10
Maintenance / relapse

Tests.

Commit.

## Phase 11
Backup/migration/export/audit completeness

Full migration tests.

Commit.

## Phase 12
Full end-to-end + real device validation

Do not call product “complete” until device validation is done.

---

# 39. Checkpoints

After each phase:

git diff review

targeted tests

update:

docs/FULL_APP_WORKING_CONTEXT.md

docs/FULL_APP_REQUIREMENTS_MATRIX.md

docs/FULL_APP_ARCHITECTURE.md

If stable:
Git commit.

This is mandatory because Codex chats may disappear or quota may interrupt work.

The repository is the project memory.

---

# 40. REQUIREMENTS MATRIX

Every requirement needs:

REQ-ID

requirement

implementation files

database impact

medical rule dependency

test

device validation

status:

PASS
PARTIAL
BLOCKED
NOT_STARTED

Do not mark PASS because code exists.

PASS means:
real path works.

---

# 41. Prevent previous Agent failure modes

Never claim a whole requirement is done when only one renderer/path is fixed.

Never use comments as proof of implementation.

Never leave dead branches with no real data wiring.

Do not use broad unsafe text replacement.

After large automated edits:
immediately inspect git diff.

Do not run full suite after every tiny edit.

Use targeted tests during phases.

Run full test/build at phase boundary and final release.

Do not repeatedly reread the whole repository.

Use:
rg
exact files
git diff
working context

Do not re-explain the whole task in every report.

---

# 42. Token/context discipline

Maintain:

docs/FULL_APP_WORKING_CONTEXT.md

Keep it concise:

current phase
completed
next
frozen medical decisions
blockers
important commits

After interruption:
read this + git status + git log + git diff.

Do not reread the entire conversation or repository unless truly required.

Use higher reasoning only for:

medical rule conflict
state-machine design
migration
safety logic
hard cross-module bugs

Do not waste highest reasoning on:
CSS
renaming
simple navigation
mechanical refactor

Never save token by skipping medical-source validation.

---

# 43. Medical blocker policy

If a clinical rule is not frozen or source-supported:

do not guess.

Build the interface/contract and mark:

MEDICAL_RULE_BLOCKED

Continue other independent work.

The final report must list all remaining blockers.

Do not silently substitute:
your own threshold
another guideline
AI judgment.

---

# 44. Current known blocker

Sleep Need Questionnaire rights/use.

Do not copy/reconstruct it without approved rights.

Automatic self-guided weekly TIB adjustment depending on that questionnaire must remain gated until approved.

This does NOT block building:
state machine
metrics
Safety
initial plan structure
stimulus control
exception routing
clinician-assisted weekly plans
education
backup
maintenance infrastructure

---

# 45. Adult scope

Current full app treatment is for adults.

For minors:
do not calculate CBT-I treatment plans.

Display:
seek professional medical/professional help.

---

# 46. Do not regress current privacy boundaries

Daily share PNG:
still only approved diary core fields.

No Safety.
No workspace notes.
No clinician notes.
No treatment plan unless a new explicit share format is separately designed.

CSV diary:
keep existing semantics.

Full backup:
is the comprehensive private export.

---

# 47. Final completion criteria

Do NOT call the app complete merely because APK builds.

Full CBT-I target requires:

baseline works

Safety continuous

episode works

plan activation works

stimulus control works

TIB flow works

weekly review works or is explicitly gated only by the documented rights blocker

exceptions work

hold/resume works

cognitive works

relaxation works

V4 education integrated

clinician bridge works

maintenance works

backup round-trip works

migration works

full audit traceability works

real device end-to-end works

No critical PARTIAL hidden in report.

---

# 48. First action now

Do not code immediately.

First produce:

docs/FULL_APP_GAP_ANALYSIS.md

and update/create:

docs/FULL_APP_ARCHITECTURE.md

docs/FULL_APP_REQUIREMENTS_MATRIX.md

docs/FULL_APP_WORKING_CONTEXT.md

The Gap Analysis must explicitly map:

current 0.2.2 feature
→ target full feature
→ reuse
→ modify
→ new
→ blocked

Then begin Phase 1 automatically if there is no architecture conflict.

Do not stop merely to ask permission for ordinary engineering decisions.

Only stop for:

medical-rule ambiguity
data-loss risk
protocol conflict
rights/licensing blocker
safety behavior ambiguity

For all normal Kotlin/Compose/Room architecture decisions:
decide professionally and proceed.

---

# Final reporting

At the end of each phase report only:

phase
commit
implemented modules
tests
migration status
requirements matrix PASS/PARTIAL/BLOCKED
medical blockers
next phase

Do not write a long narrative.

The final target is the complete CBT-I application, not another diary increment.