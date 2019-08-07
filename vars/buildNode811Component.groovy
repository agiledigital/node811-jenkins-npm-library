/*
 * Toolform-compatible Jenkins 2 Pipeline build step for NodeJS 8.11 apps using the node811 builder
 */

def call(Map config) {

  def artifactDir = "${config.project}-${config.component}-artifacts"
  def testOutput = "${config.project}-${config.component}-tests.xml"
  def testVideoOutput = "${config.project}-${config.component}-test-videos"

  final npm = { cmd ->
    ansiColor('xterm') {
      dir(config.baseDir) {
        sh "JEST_JUNIT_OUTPUT=${testOutput} npm ${cmd}"
      }
    }
  }
  
  container("node811-builder") {

    stage('Build Details') {
      echo "Project:   ${config.project}"
      echo "Component: ${config.component}"
      echo "BuildNumber: ${config.buildNumber}"
    }

    stage('Install dependencies') {
      npm "install"
      
      // Bootstrap and build the packages, because projects may depend on other component's artefacts.
      npm 'run build'
    }

    stage('Test') {
      steps {
        catchError {
          withEnv([
            "CI=true",
            "TZ=UTC"
          ]) {
            npm 'test'
            junit allowEmptyResults: true, testResults: testOutput
          }
        }
        
        sh "mkdir -p ${testVideoOutput}"
        
        if(fileExists("${config.baseDir}/cypress/videos")) {
          sh "mv ${config.baseDir}/cypress/videos ${testVideoOutput}"
        }
        
        def integrationTestTarName = "${config.project}-${config.component}-${config.buildNumber}-e2e.tar.gz"
        sh "tar -czf \"${integrationTestTarName}\" -C \"${testVideoOutput}\" ."
        archiveArtifacts integrationTestTarName  
      }
    }
  }

  if(config.stage == 'dist') {

    container('node811-builder') {
      stage('Build') {
        npm "run build"
      }

      stage('Package') {
        sh "mkdir -p ${artifactDir}"

        npm "install --production --ignore-scripts --prefer-offline"
        sh "mv ${config.baseDir}/node_modules ${config.baseDir}/package.json ${artifactDir}"

        // The build and dist folders may exisit depending on builder.
        // Copy them into the artifact if they exist. e.g. React uses build, NodeJS defualt is dist.
        if(fileExists("${config.baseDir}/dist")) {
          sh "mv ${config.baseDir}/dist ${artifactDir}"
        }
        
        if(fileExists("${config.baseDir}/build")) {
          sh "mv ${config.baseDir}/build ${artifactDir}"
        }
        
        if(fileExists("${config.baseDir}/serverless.yml")) {
          sh "mv ${config.baseDir}/serverless.yml ${artifactDir}"
        }

        // The static folder and application specific config files 
        // should also be staged if they exist.
        if(fileExists("${config.baseDir}/static")) {
          sh "mv ${config.baseDir}/static ${artifactDir}"
        }

        if(fileExists("${config.baseDir}/next.config.js")) {
          sh "mv ${config.baseDir}/next.config.js ${artifactDir}"
        }
      }
    }

    stage('Archive to Jenkins') {
      def tarName = "${config.project}-${config.component}-${config.buildNumber}.tar.gz"
      sh "tar -czf \"${tarName}\" -C \"${artifactDir}\" ."
      archiveArtifacts tarName
    }

  }

}
