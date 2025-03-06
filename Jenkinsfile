pipeline {
    agent any
tools {
        terraform 'terra'  // This will install Terraform automatically
    }
    environment {
        SONAR_URL = 'http://34.27.111.131:9000'
        SONAR_TOKEN = credentials('sonar-token')  // Add this in Jenkins credentials
        IMAGE_NAME = "saicharan12121/javasample1"
        BUILD_NUMBER = "${env.BUILD_NUMBER}"
        GOOGLE_APPLICATION_CREDENTIALS = credentials('gcp.json')
        ARTIFACT_REPO = "asia-south2-docker.pkg.dev/saicharan-452306/devopsjava1"
   
    }

    stages {

        stage('Build with Maven') {
            steps {
                sh 'mvn clean package'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonar') {  // This automatically sets SONAR_URL and SONAR_TOKEN
                sh '''
                mvn sonar:sonar \
                    -Dsonar.projectKey=my-project \
                    -Dsonar.token=${SONAR_TOKEN}
                '''
               }
            }
        }
       stage("Quality Gate") {
            steps {
              timeout(time: 10, unit: 'MINUTES') {
                waitForQualityGate abortPipeline: true
              }
            }
          }
       
        stage('Build Docker Image') {
    steps {
        sh '''
        docker build -t ${IMAGE_NAME}:${BUILD_NUMBER} .
        docker tag ${IMAGE_NAME}:${BUILD_NUMBER} ${IMAGE_NAME}:${BUILD_NUMBER}
    #docker tag ${IMAGE_NAME}:${BUILD_NUMBER} ${IMAGE_NAME}:latest
        docker images

        '''
    }
}
        stage("Google Cloud Login") {
            steps {
                sh '''
                gcloud auth activate-service-account --key-file=$GOOGLE_APPLICATION_CREDENTIALS 
                gcloud auth configure-docker asia-south2-docker.pkg.dev
                '''
            }
        }
       stage('Docker Login & Push') {
    steps {
        withCredentials([usernamePassword(credentialsId: 'dockerhub', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
            sh '''
            echo $PASSWORD | docker login -u $USERNAME --password-stdin
            docker push ${IMAGE_NAME}:${BUILD_NUMBER} || echo "Push Failed, Retrying..."
           # docker push ${IMAGE_NAME}:latest

            docker tag ${IMAGE_NAME}:${BUILD_NUMBER} ${ARTIFACT_REPO}/${IMAGE_NAME}:${BUILD_NUMBER}
            docker push ${ARTIFACT_REPO}/${IMAGE_NAME}:${BUILD_NUMBER}
            '''
        }
    }
}
        



        stage('Terraform Workspace Setup') {
    steps {
        sh '''
        terraform init 
        terraform plan
        terraform apply -auto-approve
        '''
    }
}

        //  stage('Terraform Apply') {
        //     steps {
           
        //          sh 'terraform init '
        //          sh 'terraform plan'
        //         sh 'terraform apply -auto-approve'
        
        //        // sh 'terraform destroy -auto-approve' //
        //     }
        // }
          
        
        stage("Check Connection") {
            steps {
                sh '''
                ansible-inventory --graph
                '''
            }
        }

        stage("Ping Ansible") {
            steps {
                sh '''
                sleep 20
                ansible all -m ping
                '''
            }
        }
        stage('Ansible Deployment') {
            steps {
                sh '''
                ansible-playbook ansible.yml -e build_number=$BUILD_NUMBER   
                '''
            }
        }


        

      
    }
        

    post {
        success {
            echo "Application successfully deployed in a Docker container!"
        }
        failure {
            echo "Build failed!"
        }
    }
}
