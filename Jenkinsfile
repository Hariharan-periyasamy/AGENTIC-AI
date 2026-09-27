pipeline {
    agent any

    environment {
        MVN_CMD = 'C:\\apache-maven-3.9.16\\bin\\mvn.cmd'
        IMAGE_NAME = 'digital-certificate-system'
        IMAGE_TAG = "${env.BUILD_NUMBER ?: 'latest'}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                echo 'Source code checked out successfully.'
            }
        }

        stage('Compile') {
            steps {
                bat "\"%MVN_CMD%\" clean compile"
            }
        }

        stage('Unit Test') {
            steps {
                bat "\"%MVN_CMD%\" test -DskipITs=true"
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Integration Test') {
            steps {
                bat "\"%MVN_CMD%\" test -Dtest=*IntegrationTest"
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                bat "\"%MVN_CMD%\" package -DskipTests"
            }
        }

        stage('Docker Build') {
            steps {
                bat 'docker compose build'
            }
        }

        stage('Docker Deploy') {
            steps {
                bat 'docker compose up -d'
                echo 'Docker containers deployed and running.'
            }
        }

        stage('Health Check') {
            steps {
                script {
                    def healthy = false
                    for (int i = 0; i < 24; i++) {
                        sleep(time: 5, unit: 'SECONDS')
                        def exitCode = bat(script: 'powershell -Command "try { $r = Invoke-RestMethod -Uri \'http://localhost:8080/actuator/health\' -TimeoutSec 5; if ($r.status -eq \'UP\') { exit 0 } else { exit 1 } } catch { exit 1 }"', returnStatus: true)
                        if (exitCode == 0) {
                            healthy = true
                            break
                        }
                        echo "Waiting for application health... attempt ${i + 1}/24"
                    }
                    if (!healthy) {
                        bat(script: 'docker compose logs --tail=50', returnStatus: true)
                        error('Health check failed: application did not return status UP within 120 seconds')
                    }
                    echo 'Application health verified: status=UP'
                }
            }
        }

        stage('Ansible Verification') {
            steps {
                script {
                    // Verify Docker containers are running (Ansible-style verification)
                    bat 'docker ps --filter "name=dcs_app" --filter "status=running" | findstr dcs_app'
                    bat 'docker ps --filter "name=dcs_mysql" --filter "status=running" | findstr dcs_mysql'

                    // Verify health endpoint returns UP
                    bat 'powershell -Command "$r = Invoke-RestMethod -Uri \'http://localhost:8080/actuator/health\' -TimeoutSec 10; if ($r.status -ne \'UP\') { throw \'Health check failed\' }; Write-Host \'Ansible Verification: Application is HEALTHY\'"'

                    echo 'Ansible-style container management verification complete.'
                }
            }
        }

        stage('Final Verification') {
            steps {
                script {
                    // Smoke test: Login page accessible
                    bat 'powershell -Command "$r = Invoke-WebRequest -Uri \'http://localhost:8080/login\' -UseBasicParsing -TimeoutSec 10; if ($r.StatusCode -ne 200) { throw \'Login page not accessible\' }; Write-Host \'Login page: OK (HTTP 200)\'"'

                    // Smoke test: Register page accessible
                    bat 'powershell -Command "$r = Invoke-WebRequest -Uri \'http://localhost:8080/register\' -UseBasicParsing -TimeoutSec 10; if ($r.StatusCode -ne 200) { throw \'Register page not accessible\' }; Write-Host \'Register page: OK (HTTP 200)\'"'

                    // Smoke test: Actuator health details
                    bat 'powershell -Command "$r = Invoke-RestMethod -Uri \'http://localhost:8080/actuator/health\' -TimeoutSec 10; Write-Host \'Health Status:\' $r.status"'

                    echo 'All smoke tests passed. Application is LIVE and healthy.'
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline executed successfully! Application is LIVE at http://localhost:8080'
        }
        failure {
            echo 'Pipeline FAILED. Collecting container logs...'
            bat(script: 'docker compose logs --tail=50', returnStatus: true)
        }
        aborted {
            echo 'Pipeline ABORTED.'
        }
    }
}
