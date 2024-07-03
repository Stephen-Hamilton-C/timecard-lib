# timecard-lib
The backend used in [timecard-cli](https://github.com/Stephen-Hamilton-C/timecard-cli),
[timecard-web](https://github.com/Stephen-Hamilton-C/timecard-web), and
[timecard-compose](https://github.com/Stephen-Hamilton-C/timecard-compose)

I need the same application on different platforms,
and I have been wanting to mess around with Kotlin Multiplatform,
and thus, timecard-lib was born, so I only had to write this once.

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
                   implementation("com.github.stephenhamiltonc:timecard-lib:2.0.2")
               }
          }
      }
  }
  ```

  - For Single-Platform Projects:

  ```
  dependencies {
      implementation("com.github.stephenhamiltonc:timecard-lib:2.0.2")
  }
  ```
Note that this library requires Java 17 or higher.

## Usage
Your starting point is the `Timecard` class, which holds and manages all `TimeEntry` instances.
These `TimeEntry`s each have a start and a nullable end `Instant`.
`Timecard.clockIn()` will add a `TimeEntry` with a start `Instant` at the given time,
or the current time if no time is given.
`Timecard.clockOut()` will add an end `Instant` to the last `TimeEntry` at the given time,
or the current time if no time is given.

These two methods are the backbone to all the other methods in `Timecard`.
Their operation is described in-depth in their JavaDocs, so be sure to read those!
`clockIn()` and `clockOut()` may simply not run due to certain conditions.
You can use their return `ClockResult` to find out why they were canceled.

`Timecard` and `TimeEntry` are kotlinx `Serializable`,
so you can turn a `Timecard` into JSON or other preferred data format using
[kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)

## API
You can read up on the API [here](https://stephen-hamilton-c.github.io/timecard-lib/).
