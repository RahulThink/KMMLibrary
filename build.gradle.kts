plugins {
    //trick: for the same plugin versions in all sub-modules
    id("com.android.library").version("7.2.1").apply(false)
    kotlin("multiplatform").version("1.7.10").apply(false)
}


tasks.wrapper {
    gradleVersion = "6.7.1"
    distributionType = Wrapper.DistributionType.ALL
}