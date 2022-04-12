pipeline {
    agent any
    options {
        buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '5')
    }
    stages {
        stage('applyPatches') {
            steps {
                withGradle {
                    sh './gradlew applyPatches --no-daemon'
                }
            }
        }
        stage('paperclipJar') {
            steps {
                withGradle {
                    sh './gradlew paperclipJar --no-daemon'
                }
            }
        }
    }
    post {
        always {
            archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
            cleanWs()
        }
    }
}