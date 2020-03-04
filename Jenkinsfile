
pipeline {
    agent { label 'master' }
    stages {
        stage('build') {
            steps {
                echo "Hello World this is my first pipeline test!"
                bat "echo Hello from the shell"
                bat "hostname"
                echo "Finish pipeline test!"
            }
        }
    }
}
