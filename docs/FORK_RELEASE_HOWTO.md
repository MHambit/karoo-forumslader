# Fork Workflow & Personal Release How-To

This guide describes how to keep `main` cleanly synced with `upstream/main` for pull requests while shipping personal prerelease APKs from `develop` via `.github/workflows/develop-release.yml`.

---

## 1. Branch Roles

| Branch | Remote | Purpose | Rule |
| --- | --- | --- | --- |
| `main` | `upstream/main` → `origin/main` | Exact mirror of upstream | **Never commit directly here.** Only fast-forward from `upstream/main`. |
| `fix/*`, `feat/*`, `ci/*`, `docs/*` | `origin` → PR to `upstream/main` | Focused upstream PRs | Always branch off `main`. Delete after upstream merges. |
| `fork/ci-develop-release` | `origin` only | Fork-only release workflow & this guide | Always branch off `main`. **Never PR upstream.** |
| `develop` | `origin` only | Releasable daily-driver build | Rebuilt from `main` + active unmerged branches + `fork/ci-develop-release`. **Never merge into `main` or PR upstream.** |

---

## 2. One-Time Setup

### Git remotes
Ensure `upstream` points to the original repository and `origin` points to your fork:

```bash
git remote add upstream https://github.com/<UPSTREAM_OWNER>/karoo-forumslader.git
git fetch upstream
# Optional: prevent accidental pushes to upstream
git remote set-url --push upstream DISABLED
```

### GitHub Actions signing secrets (on your fork)
`.github/workflows/develop-release.yml` builds a signed release APK using these repository secrets:
- `KEYSTORE_BASE64`
- `STORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

---

## 3. Sync `main` and Tags with Upstream

Run this before starting new work or after upstream merges one of your PRs (including `--tags` ensures your `develop` version tracks the latest upstream release tag):

```bash
git fetch upstream --tags
git checkout main
git merge --ff-only upstream/main
git push origin main --tags
```

---

## 4. Create & Submit an Upstream PR

Always branch off an up-to-date `main`:

```bash
git checkout -b fix/my-change main
# make commits following Conventional Commits (fix:, feat:, ci:, docs:)
git push -u origin fix/my-change
```

Open a PR on GitHub from `origin/fix/my-change` into `upstream/main`.

### Rebase an open PR if upstream moved
```bash
git checkout fix/my-change
git rebase main
git push --force-with-lease origin fix/my-change
```

---

## 5. Build & Release on `develop`

Because upstream squash-merges PRs, rebuild `develop` cleanly from `main` by merging your active unmerged branches **one by one** (sequential merges avoid Git's octopus-merge failure when two branches touch the same file):

```bash
git checkout -B develop main

# Merge each still-unmerged PR branch sequentially
git merge --no-edit fix/karoo3-ble-stability
git merge --no-edit fix/fl6-standby-deadband
git merge --no-edit fix/graphical-charge-state
git merge --no-edit ci/rename-release-apk

# Merge the fork-only CI workflow branch
git merge --no-edit fork/ci-develop-release

# Push to trigger GitHub Actions release
git push -u --force-with-lease origin develop
```

If a merge reports a conflict (for example, two PR branches touching the same line in `README.md`), resolve the conflict, then run:
```bash
git add <conflicted-file>
git commit --no-edit
```
and continue merging the remaining branches.

### What happens automatically on push to `develop`
`.github/workflows/develop-release.yml` mirrors the production release pipeline for `develop` (and can also be triggered manually via **Actions → Develop Release (Fork) → Run workflow**):
1. Runs `./gradlew :app:checkCoverageBaseline` (same test + coverage gate as production, without pushing bot commits back to `develop`).
2. Computes the next SemVer version from commits since the latest upstream `v*` tag and appends `-dev.<run_number>` (e.g., `v1.12.1-dev.42`).
3. Builds a signed release APK named `karoo-forumslader-<new_tag>.apk`.
4. Generates production-style release notes via `.github/scripts/generate_release_notes.kts`.
5. Publishes a full GitHub Release (`Release v<major>.<minor>.<patch>-dev.<run_number>`) on your fork with the APK attached for sideloading onto Karoo.

---

## 6. Clean Up After Upstream Merges a PR

Once upstream merges a PR (for example, `fix/fl6-standby-deadband`):

```bash
# 1. Sync main and tags (now contains the squash-merged fix)
git fetch upstream --tags
git checkout main
git merge --ff-only upstream/main
git push origin main --tags

# 2. Delete the merged PR branch locally and on your fork
git branch -D fix/fl6-standby-deadband
git push origin --delete fix/fl6-standby-deadband

# 3. Rebuild develop without the merged branch (see Section 5)
```

---

## 7. Pitfalls to Avoid

- **Never commit directly to `main`** — keeps `upstream/main` syncing fast-forward only.
- **Never merge `develop` into `main`** — prevents unmerged or fork-only commits from leaking into `main`.
- **Never branch an upstream PR off `develop`** — always branch off `main`.
- **Never pass multiple branches to a single `git merge`** when conflicts are possible — merge one branch at a time.
- **Always use `--force-with-lease`** instead of `--force` when pushing rebased or rebuilt branches.
