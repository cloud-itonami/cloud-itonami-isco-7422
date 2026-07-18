# Governance

`cloud-itonami-isco-7422` is an OSS open-occupation blueprint. Governance
covers both code and the operator model.

## Maintainers

Maintainers may merge changes that preserve these invariants:

- the Advisor cannot directly dispatch robot actions or disclose records.
- ICTInstallGovernor remains independent of the advisor.
- hard policy violations cannot be overridden by human approval.
- an installation-execution decision, a network/electrical-
  compliance-clearance determination, and any override of a site
  safety officer's judgment, stay permanently outside this actor's
  op-allowlist.
- every commit, hold and approval path is auditable.
- real installer/site/operator data stays outside Git.

## Decision Records

Architecture decisions live in `docs/adr/`. Changes to the trust model,
storage contract, public business model, operator certification, license, or
the closed op-allowlist should add or update an ADR.

## Operator Governance

Anyone may fork and operate independently. itonami.cloud certification is a
separate trust mark and should require security, audit, support and data-flow
review.

Certified operators can lose certification for:

- bypassing policy checks
- mishandling installer/site/operator data
- misrepresenting certification status
- failing to respond to security incidents
- hiding material changes to customer-facing operation
- attempting to route an installation-execution decision, a
  network/electrical-compliance-clearance determination, or a
  site-safety-officer-judgment override, through this actor
