## gdmc release checklist

**Start Release**

1. Ensure `develop` branch is green on Jenkins and CircleCI
2. `git flow release start v<version>`
3. Update version in `build.gradle`
4. Update version in `CHANGELOG.md`
5. Update version in `README.md`
7. Run `./gradlew gdmcImportSelf` + commit changes to submodule
8. Commit changes with message `[VERSION] Release v<version>`
9. **DO NOT PUSH**

**Bump Snapshot Version**

1. Checkout `develp` branch
2. Update SNAPSHOT version in `build.gradle`
3. Update SNAPSHOT version in `CHANGELOG.md`
4. Update SNAPSHOT version in `README.md`
6. Commit changes with message `[VERSION] Snapshot v<version>`
7. **Push both branches**

**Release**

1. Wait for green builds on Jenkins and CircleCi
2. `git flow release finish` (tag with `v<version>`)
3. Resolve merge conflicts on develop
4. Push branches
