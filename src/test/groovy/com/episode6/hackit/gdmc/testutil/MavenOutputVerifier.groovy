package com.episode6.hackit.gdmc.testutil
/**
 * Verifies maven output
 */
class MavenOutputVerifier {

  File repo

  String groupId
  String artifactId
  String versionName

  boolean isRelease() {
    return !versionName.contains("SNAPSHOT")
  }

  File getMavenProjectDir() {
    return getRepo().newFolderFromPackage(groupId).newFolder(artifactId)
  }

  File getMavenVersionDir() {
    return getMavenProjectDir().newFolder(versionName)
  }

  File getArtifactFile(String extension, String descriptor = null) {
    return getMavenVersionDir().newFile(getArtifactFileName(extension, descriptor))
  }

  boolean verifyStandardOutput(String artifactPackaging = "jar") {
    return verifyRootMavenMetaData() &&
        verifyVersionSpecificMavenMetaData() &&
        verifyPomData()
  }

  boolean verifyRootMavenMetaData() {
    def mavenMetaData = getMavenProjectDir().newFile("maven-metadata.xml").asXml()

    assert mavenMetaData.groupId.text() == groupId
    assert mavenMetaData.artifactId.text() == artifactId
    assert mavenMetaData.versioning.versions.size() == 1
    assert mavenMetaData.versioning.versions.version.text() == versionName
    assert mavenMetaData.versioning.lastUpdated != null

    if (isRelease()) {
      assert mavenMetaData.versioning.release.text() == versionName
    }
    return true
  }

  boolean verifyVersionSpecificMavenMetaData() {
    if (isRelease()) {
      // this file is only generated for snaphots, skipping
      return true
    }
    def mavenMetaData = getMavenVersionDir().newFile("maven-metadata.xml").asXml()

    assert mavenMetaData.groupId.text() == groupId
    assert mavenMetaData.artifactId.text() == artifactId
    assert mavenMetaData.version.text() == versionName
    assert mavenMetaData.versioning.snapshot.size() == 1
    assert mavenMetaData.versioning.snapshot.timestamp != null
    assert mavenMetaData.versioning.snapshot.buildNumber.text() == "1"
    assert mavenMetaData.versioning.lastUpdated != null
    return true
  }

  boolean verifyPomData() {
    File pomFile = getArtifactFile("pom")
    def pom = pomFile.asXml()

    assert pom.groupId.text() == groupId
    assert pom.artifactId.text() == artifactId
    assert pom.version.text() == versionName
    assert pom.name.text() == artifactId
    return true
  }

  boolean verifyPomDependency(String groupId, String artifactId, String version, String scope = "compile") {
    def pom = getArtifactFile("pom").asXml()
    def pomDep = pom.dependencies.dependency.find { pd ->
      pd.groupId.text() == groupId &&
          pd.artifactId.text() == artifactId &&
          pd.version.text() == version &&
          pd.scope.text() == scope
    }
    assert pomDep.groupId.text() == groupId &&
        pomDep.artifactId.text() == artifactId &&
        pomDep.version.text() == version &&
        pomDep.scope.text() == scope
    return true
  }

  private String getArtifactFileName(String extension, String descriptor = null) {
    String endOfFileName = descriptor == null ? ".${extension}" : "-${descriptor}.${extension}"
    if (isRelease()) {
      return "${artifactId}-${versionName}${endOfFileName}"
    }

    def versionSpecificMavenMetaData = getMavenVersionDir().newFile("maven-metadata.xml").asXml()
    String snapshotTimestamp = versionSpecificMavenMetaData.versioning.snapshot.timestamp.text()
    String snapshotBuildNumber = versionSpecificMavenMetaData.versioning.snapshot.buildNumber.text()
    return "${artifactId}-${versionName.replace("-SNAPSHOT", "")}-${snapshotTimestamp}-${snapshotBuildNumber}${endOfFileName}"
  }
}
