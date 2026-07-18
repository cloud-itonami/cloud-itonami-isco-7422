# cloud-itonami-isco-7422

Open Occupation Blueprint for **ISCO-08 7422**: Information and
Communications Technology Installers and Servicers.

This repository designs a forkable OSS business for an ICT
installation/servicing site scheduling and logistics coordination
practice: a site scheduling and supply-coordination robot manages
installer/site operating records under a governor-gated actor, so an
ICT installer/servicer crew keeps its own operating records instead
of renting a closed field-service-management SaaS.

**Maturity: `:implemented`.** `src/ictinstall/` implements the
`ICTInstallActor` as a `langgraph.graph/state-graph`
(`ictinstall.actor`) wired to an `ICT Installation Advisor`
(`ictinstall.advisor`) and an independent `ICTInstallGovernor`
(`ictinstall.governor`), following the itonami actor pattern
(ADR-2607121000): `:intake -> :advise -> :govern -> :decide -+-> :commit
(:ok?) +-> :request-approval (:escalate?, human-in-the-loop interrupt)
+-> :hold (:hard?)`. 27 tests / 59 assertions green (`clojure -M:test`).
HARD invariants (always hold, never overridable):
installer provenance, site provenance, no-actuation (`:effect` must
be `:propose`), a closed op-allowlist (`:log-work-record`,
`:schedule-crew-operation`, `:flag-safety-concern`,
`:coordinate-supply-order` — nothing else may ever be proposed), and
a permanent, unconditional block on any proposal that would directly
finalize an installation-execution decision (e.g. deciding to
proceed with a specific cable/equipment installation), authorize or
finalize a network/electrical-compliance-clearance determination, or
override a site safety officer's judgment. Always-escalate paths
(human sign-off regardless of confidence, mapping this repo's Trust
Controls in [`docs/business-model.md`](docs/business-model.md)):
`:flag-safety-concern` (always) and `:coordinate-supply-order` above
the registered cost threshold.

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot performs
the physical domain work**. Here a site scheduling/logistics coordination robot performs installer/crew scheduling, installation-job/progress record logging and cabling/networking-equipment supply-order coordination for an ICT installer/servicer crew, under an actor that proposes actions and an independent **ICT Installation Governor** that gates them. The governor never
dispatches hardware itself, never performs installation work, and never finalizes an installation-execution decision, authorizes a network/electrical-compliance-clearance determination, or overrides a site safety officer's judgment; `:high`/`:safety-critical` actions (such as a flagged electrical-shock-risk/working-at-height/equipment-condition concern, or an above-threshold supply order) require human sign-off. **This actor coordinates site scheduling/logistics only — it never performs installation work or makes compliance-clearance decisions itself.**

## Core Contract

```text
installer roster + site registration + safety-reporting policy
        |
        v
ICT Installation Advisor -> ICTInstallGovernor -> log/schedule/coordinate, or human sign-off
        |
        v
robot actions (gated) + operating records + audit ledger
```

No automated advice can dispatch a robot action the governor refuses, finalize
an installation-execution decision, authorize or finalize a
network/electrical-compliance-clearance determination, override a site safety
officer's judgment, suppress an operating record, or disclose sensitive data
without governor approval and audit evidence.

## Capability layer

Resolves via [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation)
(ISCO-08 `7422`). Required capabilities:

- :robotics
- :identity
- :audit-ledger

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## License

AGPL-3.0-or-later.
