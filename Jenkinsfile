#! groovy

node {
  stage('checkout') {
    checkout scm
  }

  def gradleRunner
  stage('pipeline') {
    gradleRunner = fileLoader.fromGit(
        'gradle/GradleRunner',
        'git@github.com:episode6/jenkins-pipelines.git',
        'develop',
        null,
        '')
  }

  gradleRunner.buildAndTest()
}
