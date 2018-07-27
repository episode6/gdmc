#! groovy

node {
  stage('checkout') {
    checkout scm
    sh 'git submodule update --init'
  }

  def gradleRunner
  stage('pipeline') {
    gradleRunner = fileLoader.fromGit(
        'gradle/GradleRunner',
        'git@github.com:episode6/jenkins-pipelines.git',
        'feature/force-deploy', //''v0.0.7',
        null,
        '')
  }

  gradleRunner.buildAndTest()
  gradleRunner.deploy()
}
