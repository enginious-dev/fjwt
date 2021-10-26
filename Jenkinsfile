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
                    pom = readMavenPom file: "pom.xml";
                    jar = "target/${pom.artifactId}-${pom.version}.${pom.packaging}";
                    sources = "target/${pom.artifactId}-${pom.version}-sources.${pom.packaging}";
                    jarExists = fileExists jar;
                    sourcesExists = fileExists sources;
                    if(jarExists && sourcesExists) {
                        echo "*** Artifact: ${pom.artifactId}, group: ${pom.groupId}, packaging: ${pom.packaging}, version ${pom.version}";
                        if(!env.CHANGE_ID && ((BRANCH == "master" && !pom.version.contains("SNAPSHOT")) || (BRANCH != "master" && pom.version.contains("SNAPSHOT")))){
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
                                                            file: jar,
                                                            type: pom.packaging],
                                                            [artifactId: pom.artifactId,
                                                            classifier: 'sources',
                                                            file: sources,
                                                            type: pom.packaging],
                                                            [artifactId: pom.artifactId,
                                                            classifier: '',
                                                            file: "pom.xml",
                                                            type: "pom"]
                                                        ]
                                                    );
                        }else{
                            echo "*** branch name mismatch with pom version, publish to nexus skipped";
                            Utils.markStageSkippedForConditional("nexus publish")
                        }
                    } else {
                        if(!jarExists) {
                            error "*** File: ${jar}, could not be found";
                        }
                        if(!sourcesExists) {
                            error "*** File: ${sources}, could not be found";
                        }
                    }
                }
            }
        }
    }
}