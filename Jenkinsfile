import org.jenkinsci.plugins.pipeline.modeldefinition.Utils

pipeline {

    agent {
        label "master"
    }

    tools {
        maven "maven"

    }

    environment {
        NEXUS_VERSION = "nexus3"
        NEXUS_PROTOCOL = "https"
        NEXUS_URL = "nexus.enginious-dev.com"
        NEXUS_REPOSITORY = "${env.BRANCH_NAME == "master" ? "releases" : "snapshots"}"
        BRANCH = "${env.BRANCH_NAME}"
        NEXUS_CREDENTIAL_ID = "nexus-credentials"
    }

    stages {
        stage("build") {
            steps {
                configFileProvider([configFile(fileId: 'enginious-settings', variable: 'MAVEN_SETTINGS')]) {
                    sh 'mvn -s $MAVEN_SETTINGS clean package'
                }
            }
        }

        stage("nexus publish") {
            steps {
                script {
                    pom = readMavenPom file: "pom.xml"
                    jar = "target/${pom.artifactId}-${pom.version}.${pom.packaging}"
                    sources = "target/${pom.artifactId}-${pom.version}-sources.${pom.packaging}"
                    jarExists = fileExists jar
                    sourcesExists = fileExists sources
                    if (jarExists && sourcesExists) {
                        echo "*** Artifact: ${pom.artifactId}, group: ${pom.groupId}, packaging: ${pom.packaging}, version ${pom.version}"
                        if (!env.CHANGE_ID && ((BRANCH == "master" && !pom.version.contains("SNAPSHOT")) || (BRANCH != "master" && pom.version.contains("SNAPSHOT")))) {
                            nexusArtifactUploader(
                                    nexusVersion: NEXUS_VERSION,
                                    protocol: NEXUS_PROTOCOL,
                                    nexusUrl: NEXUS_URL,
                                    groupId: pom.groupId,
                                    version: pom.version,
                                    repository: NEXUS_REPOSITORY,
                                    credentialsId: NEXUS_CREDENTIAL_ID,
                                    artifacts: [
                                            [artifactId: pom.artifactId,
                                             classifier: '',
                                             file      : jar,
                                             type      : pom.packaging],
                                            [artifactId: pom.artifactId,
                                             classifier: 'sources',
                                             file      : sources,
                                             type      : pom.packaging],
                                            [artifactId: pom.artifactId,
                                             classifier: '',
                                             file      : "pom.xml",
                                             type      : "pom"]
                                    ]
                            )
                        } else {
                            echo "*** branch name mismatch with pom version, publish to nexus skipped"
                            Utils.markStageSkippedForConditional("nexus publish")
                        }
                    } else {
                        if (!jarExists) {
                            error "*** File: ${jar}, could not be found"
                        }
                        if (!sourcesExists) {
                            error "*** File: ${sources}, could not be found"
                        }
                    }
                }
            }
        }

        stage("mail notification") {
            steps {
                script {
                    def mail
                    def cause = currentBuild.getBuildCauses()[0]
                    if (cause._class.contains("UserIdCause")) {
                        wrap([$class: 'BuildUser']) {
                            mail = "${BUILD_USER_EMAIL}"
                        }
                    } else if (cause._class.contains("BranchEventCause")) {
                        mail = sh(script: "git show -s --format='%ae' ${env.GIT_COMMIT}", returnStdout: true)
                    } else {
                        echo "Job triggered by something else"
                    }
                    if (mail != null && !mail.isEmpty()) {
                        def job = (env.JOB_NAME.tokenize('/') as String[])[0]
                        emailext(
                                from: "jenkins@enginious-dev.com",
                                subject: "${job} - ${env.BRANCH_NAME}: Build #${env.BUILD_NUMBER}",
                                body: "Build finished with status <b>${currentBuild.currentResult}</b>.<br/>Check console output <a href='${env.BUILD_URL}'>here</a> to view the results.",
                                to: mail
                        )
                    }
                }
            }
        }
    }
}