# Operator Guide

## First Deployment

1. Define the operator's site coverage and installer intake process.
2. Define consent and purpose categories for installer/site records.
3. Run synthetic operating cases (work-record entry, crew-operation
   scheduling, supply coordination, safety-concern flagging).
4. Enable human-reviewed sign-off for `:high`/`:safety-critical`
   actions (all flagged safety concerns, above-threshold supply
   orders).
5. Measure operating outcomes and audit coverage.

## Minimum Production Controls

- consent and disclosure log
- safety-critical escalation path (electrical-shock risk,
  working-at-height, equipment condition)
- provenance for all operating records (installer and site both
  independently registered, installer record including certification
  status)
- human review for high-risk cases
- audit export for all gated actions
- a hard, unconditional block on any attempt to route an
  installation-execution decision, a network/electrical-compliance-
  clearance determination, or a site-safety-officer-judgment
  override, through this actor — those decisions stay a site safety
  officer's exclusive authority end to end

## Certification

Certified operators must prove that the governor gates every
safety-critical robot action, that safety-critical risks escalate to
humans, and that no deployment configuration can route an
installation-execution decision, a network/electrical-compliance-
clearance determination, or a site-safety-officer-judgment override,
through this actor.
