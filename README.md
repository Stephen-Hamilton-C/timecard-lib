# timecard-lib
The backend used in [timecard-cli](https://github.com/Stephen-Hamilton-C/timecard-cli),
[timecard-web](https://github.com/Stephen-Hamilton-C/timecard-web), and
[timecard-compose](https://github.com/Stephen-Hamilton-C/timecard-compose)

I need the same application on different platforms,
and I have been wanting to mess around with Kotlin Multiplatform,
and thus, timecard-lib was born so I only had to write this once.

If you want to use this, feel free! The library is under everyone's favorite MIT License.

## Install
I hope to get this on Maven Central when I have more time,
but for now you have to get the library onto your local repo:
1. Clone this repository to your machine
2. Run `./gradlew publishToMavenLocal` (or `.\gradlew.bat publishToMavenLocal` on Windows)
3. In your project's `build.gradle`, add the Maven Local repository
```
repositories {
  mavenLocal()
}
```
5. Add the dependency to your project
  - For Multiplatform Projects:
  
  ```
  kotlin {
      sourceSets {
          commonMain {
               dependencies {
                   implementation("com.github.stephenhamiltonc:timecard-lib:1.0.0")
               }
          }
      }
  }
  ```

  - For Single-Platform Projects:

  ```
  dependencies {
      implementation("com.github.stephenhamiltonc:timecard-lib:1.0.0")
  }
  ```
