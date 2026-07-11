# Security Policy

MicroGreensApp is a personal, single-user Android app: all data stays on
the device in a local Room database, there is no backend, no account
system, and no server-side attack surface. The only network calls are
read-only, unauthenticated requests to the public Open-Meteo API for
weather data.

## Reporting a vulnerability

If you find a security issue (e.g. something that could expose data on
the device, or a supply-chain concern in a dependency), please open a
[private security advisory](../../security/advisories/new) instead of a
public issue. This is a hobby project maintained in my spare time, so
response times aren't guaranteed, but I'll take a look.

## Scope

- App code and its direct dependencies (see `gradle/libs.versions.toml`)
- The CI workflow in `.github/workflows/`

Out of scope: this is not a production service, there's no bug bounty.
