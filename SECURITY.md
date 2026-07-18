# Security Policy

This project handles ICT installer/servicer operating workflows. Treat
vulnerabilities as potentially high impact even when the demo data is
synthetic — this domain's failure modes include electrical shock and
falls from height (rooftop/attic/ladder cable runs).

## Do Not Disclose Publicly

Report privately before opening public issues for:

- credential exposure
- real installer, site or operator data exposure
- authorization bypass
- ICTInstallGovernor bypass
- audit-ledger tampering
- over-disclosure in reports or exports
- unsafe robot action dispatch
- any path that lets a proposal reach an installation-execution
  decision, a network/electrical-compliance-clearance determination,
  or a site-safety-officer-judgment override

## Reporting

Use GitHub private vulnerability reporting when available for the repository.
If that is unavailable, contact the repository maintainers through the
cloud-itonami organization before publishing details.

Include:

- affected commit or version
- reproduction steps
- expected and actual behavior
- impact on installer/site data, policy enforcement or audit logging
- suggested fix, if known

## Production Guidance

- Store secrets outside Git.
- Keep real installer/site/operator data outside this repository.
- Run policy tests before deployment.
- Export and review audit logs regularly.
- Use least privilege for operators and service accounts.
