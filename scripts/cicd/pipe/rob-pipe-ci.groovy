pipeline {
  agent {
    kubernetes {      
            yaml """
kind: Pod
apiVersion: v1
metadata:
  name: cicdpod
spec:
  containers:
  - name: jnlp
    image: jenkins/jnlp-slave:3.27-1
    args: ['\$(JENKINS_SECRET)', '\$(JENKINS_NAME)']
  - name: maven
    image: maven:3.3.9-jdk-8-alpine
    imagePullPolicy: IfNotPresent
    command:
    - cat
    tty: true
  restartPolicy: Never
  volumes:
    - name: myregistry-cred
      projected:
        sources:
        - secret:
            name: myregistry-creds
            items:
                - key: .dockerconfigjson
                  path: config.json
"""
    }
  }

parameters {
    string(name: 'gitUrl', defaultValue: 'https://github.com/robinfoe/spring-microservices-k8s', description: 'Github URL')
    string(name: 'gitModule', defaultValue: 'gateway-service', description: 'Git Module to build')
    string(name: 'sonarURL', defaultValue: 'http://sonarqube.lab.app.10.16.202.119.nip.io/', description: 'Sonar URL')

}  


environment {

    GIT_URL = "${params.gitUrl}"
    SONAR_URL = "${params.sonarURL}"
    // APP_TYPE = "${params.appType}"
   
  }

    stages {
        stage('Checkout SourceCode') {
            steps{
                git "${GIT_URL}"
            }
        }

        stage('Code Analysis ') {
            steps{
                container("maven"){
                    script{
                        sh "mvn clean package sonar:sonar -Dsonar.host.url=${SONAR_URL} -DskipTests=true -X"
                    }
                }
            }
        }

   }
}

