pipeline {
    agent any
    
    tools {
        // Ensure these tools are configured in Jenkins "Global Tool Configuration"
        maven 'Maven 3.9'
        jdk 'Java 21'
    }
    
    environment {
        // Defines the image name for Docker build
        IMAGE_NAME = 'dcs-app'
        IMAGE_TAG = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                // Checkout code from the SCM configured in the Jenkins job
                checkout scm
                echo 'Source code checked out successfully.'
            }
        }

        stage('Compile') {
            steps {
                // Windows-friendly batch command
                bat 'mvn clean compile'
            }
        }

        stage('Unit Test') {
            steps {
                // Runs standard unit tests
                bat 'mvn test -DskipITs=true'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Integration Test') {
            steps {
                // Assuming Integration tests are named *IT.java or run via Spring Boot Test
                // For this project, standard 'mvn test' covers our @SpringBootTest integration tests
                // To keep the pipeline semantic, we explicitly run it again or specifically target ITs
                bat 'mvn test -Dtest=*IntegrationTest'
            }
        }

        stage('Package') {
            steps {
                // Package JAR without re-running tests
                bat 'mvn package -DskipTests'
            }
        }

        stage('Docker Build') {
            steps {
                // Build Docker images using the docker-compose build step
                bat 'docker compose build'
            }
        }

        stage('Docker Validation') {
            steps {
                // Validate images were created
                bat 'docker images | findstr dcs_app'
                bat 'docker images | findstr mysql'
            }
        }

        stage('Deployment') {
            steps {
                // Deploy via Ansible (non-interactive: credentials must be set in Jenkins credential store)
                // Note: inventory uses ansible_connection=local; become password should be injected via 
                // Jenkins' withCredentials or sshagent block in production.
                // For WSL2-based local deployment (default), 'become: yes' may require NOPASSWD sudoers entry.
                bat 'wsl ansible-playbook -i ansible/inventory.ini ansible/site.yml'
                echo 'Ansible orchestration complete.'
                // Pause to let MySQL and Spring Boot boot up
                
            }
        }

        stage('Health Check') {
            steps {
                // Check if the actuator health endpoint is responding
                bat 'curl -f http://localhost:8080/actuator/health || exit 1'
                echo 'Application health verified!'
            }
        }
    }
    
    post {
        success {
            echo 'Pipeline executed successfully! Application is LIVE.'
        }
        failure {
            echo 'Pipeline FAILED. Executing rollback/cleanup...'
            bat 'docker compose down'
        }
        aborted {
            echo 'Pipeline ABORTED.'
        }
    }
}

