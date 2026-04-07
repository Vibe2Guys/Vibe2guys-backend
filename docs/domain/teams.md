# Teams Domain

## Core Entities

- Team
- TeamMember
- TeamTask
- TeamActivityEvent
- TeamChatMessage

## Rules

- Teams are scoped to a course.
- Team membership history should be preserved if teams are reconfigured.
- Participation metrics should be based on recorded activity, not inferred membership alone.

## Key Behaviors

- auto-create teams
- assign team tasks
- record team chat and task activity
- compute participation signals

## Open Questions

- Will team chat be native or integrated from an external service?
- How will contribution be measured for shared submissions in MVP?
