import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
}
repositories {
    google()
    mavenCentral()
}

val korimVersion = "3.0.0-Beta6"
val versionPytorch = "0.5.0"
version = "1.0"

kotlin {
    android()
   /* ios("native") {
        binaries {
            version = "1.0"
            framework {
                baseName = "Demo"

            }
        }
    }

    */
    //linuxX64("native") { // on Linux
         ios("native") { // on x86_64 macOS
        // macosArm64("native") { // on Apple Silicon macOS
        // mingwX64("native") { // on Windows
        binaries {
            sharedLib {
                baseName = "native" // on Linux and macOS
                // baseName = "libnative" // on Windows
            }
        }
    }

    /*iosX64("native") {
        binaries {
            version = "1.0"
            framework {
                baseName = "Demo"

            }
        }
    }

     */

  /*  val xcf = XCFramework(rootProject.name)

    val nativeTargets = listOf(iosArm64(),iosX64())
    nativeTargets.forEach { target ->
        target.binaries.framework {
            baseName = rootProject.name
            xcf.add(this)
            transitiveExport = true
        }

        target.binaries.staticLib {
            baseName = rootProject.name
        }

   */

      /* target.binaries.all {
            linkerOpts("-framework","MqttWrapper","-F/build/cocoapods/publish/release/shared.xcframework")
        }*/



    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        //  version = "1.0"
        ios.deploymentTarget = "14.1"
        //   name = "ObjectDetect"
      //  podfile = project.file("../iosApp/Podfile")
      /*  framework {
            baseName = "ObjectDetection"


            embedBitcode(org.jetbrains.kotlin.gradle.plugin.mpp.BitcodeEmbeddingMode.BITCODE)

        }

       */
      /*  pod("PLMLibTorchWrapper") {
            version = versionPytorch
        }

        /* pod ("LibTorch") {
              version = libTorchVersion
          }*/


       */

        useLibraries()



    }

/*
    kotlinArtifacts {
        Native.XCFramework("sdk") {
            targets(iosX64, iosArm64, iosSimulatorArm64)
          /*  setModules(
                project(":shared"),
                project(":Demo")
            )

           */
        }
    }

 */




    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.soywiz.korlibs.korim:korim:$korimVersion")
                implementation("de.voize:pytorch-lite-multiplatform:$versionPytorch")
              //  implementation ("org.pytorch:pytorch_android_lite:1.9.0")
                //implementation ("org.pytorch:pytorch_android_torchvision:1.9.0")

                implementation("com.suparnatural.kotlin:fs:1.1.0")

            }
        }
      /*  val iosArm64Main by getting
        val iosX64Main by getting

        val iosMain by creating {
            dependsOn(commonMain)
            //iosSimulatorArm64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosX64Main.dependsOn(this)
        }
        
       */
       /* val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }

        */


    }
}
/*
tasks.named<org.jetbrains.kotlin.gradle.tasks.DefFileTask>("generateDefPLMLibTorchWrapper").configure {
    doLast {
        outputFile.writeText("""
            language = Objective-C
            headers = LibTorchWrapper.h
        """.trimIndent())
    }
}

 */

android {
    namespace = "com.example.objectdetectionlibrary"
    compileSdk = 32
    defaultConfig {
        minSdk = 21
        targetSdk = 32
    }
}