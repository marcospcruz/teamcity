import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.MavenBuildStep
import jetbrains.buildServer.configs.kotlin.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.buildSteps.nodeJS
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.kubernetesCloudImage
import jetbrains.buildServer.configs.kotlin.kubernetesCloudProfile
import jetbrains.buildServer.configs.kotlin.projectFeatures.buildReportTab
import jetbrains.buildServer.configs.kotlin.projectFeatures.githubAppConnection
import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2023.05"

project {
    description = "Contains all other projects"

    features {
        buildReportTab {
            id = "PROJECT_EXT_1"
            title = "Code Coverage"
            startPage = "coverage.zip!index.html"
        }
        githubAppConnection {
            id = "PROJECT_EXT_2"
            displayName = "GitHub App"
            appId = "384599"
            clientId = "Iv1.0fd762b602e5578c"
            clientSecret = "credentialsJSON:b1980328-97f0-487c-8129-89f0e7bf80df"
            privateKey = "credentialsJSON:30c41183-cba8-494e-9d98-255428dfa907"
            ownerUrl = "https://github.com/marcospcruz"
        }
    }

    cleanup {
        baseRule {
            preventDependencyCleanup = false
        }
    }

    subProject(Apuracao2022)
}


object Apuracao2022 : Project({
    name = "Apuracao2022"

    vcsRoot(Apuracao2022_Apuracaopleito)

    buildType(Apuracao2022_Compile)

    features {
        kubernetesCloudImage {
            id = "PROJECT_EXT_6"
            profileId = "kube-1"
            agentPoolId = "-2"
            agentNamePrefix = "nodejs"
            podSpecification = runContainer {
                dockerImage = "arm64v8/node"
            }
        }
        kubernetesCloudProfile {
            id = "kube-1"
            name = "kubernetes"
            terminateIdleMinutes = 30
            apiServerURL = "https://192.168.65.20:6443"
            caCertData = "credentialsJSON:e9731c72-6b5c-428d-bc8e-09ba38295a90"
            namespace = "ci-cd"
            authStrategy = clientCertificate {
                certificate = "credentialsJSON:7529b092-e4bf-4faf-aa28-8bf0d004f9cc"
                key = "credentialsJSON:e647a170-1e00-40e7-a099-17f6d2217b86"
            }
        }
    }
})

object Apuracao2022_Compile : BuildType({
    name = "compile"

    vcs {
        root(Apuracao2022_Apuracaopleito)
    }

    steps {
        maven {
            name = "xx"
            goals = "package"
            localRepoScope = MavenBuildStep.RepositoryScope.MAVEN_DEFAULT
            isIncremental = true
            jdkHome = "%env.JDK_17_0_ARM64%"
        }
        nodeJS {
            name = "npm"
            workingDir = "apuracao-pleito-angular"
            shellScript = """
                npm ci
                npm run test
            """.trimIndent()
            dockerImage = "node"
        }
        script {
            name = "npm (1)"
            scriptContent = "npm install"
        }
    }
})

object Apuracao2022_Apuracaopleito : GitVcsRoot({
    name = "apuracaopleito"
    url = "git@github.com:marcospcruz/apuracao-pleito-stack.git"
    branch = "master"
    authMethod = uploadedKey {
        uploadedKey = "id_rsa"
    }
})
