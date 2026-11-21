---
description: Fork branching and personal develop release workflow. Read before creating branches, merging into develop, or opening PRs.
---

# Fork Branching & Release Workflow

Read [docs/FORK_RELEASE_HOWTO.md](../../docs/FORK_RELEASE_HOWTO.md) before creating branches, merging into `develop`, or opening PRs.

- Always branch `feat/*`, `fix/*`, `ci/*`, and `docs/*` off `main` (`git checkout -b <branch> main`), never off `develop`.
- Never commit directly to `main`, never merge `develop` into `main`, and never PR `fork/ci-develop-release` upstream.
