# Metron

![Java Version](https://img.shields.io/badge/Temurin-17-green?style=flat-square&logo=eclipse-adoptium)
![Kotlin Version](https://img.shields.io/badge/Kotlin-2.0.0-green?style=flat-square&logo=kotlin)
![Status](https://img.shields.io/badge/Status-Beta-yellowgreen?style=flat-square)

[![Gradle](https://img.shields.io/badge/Gradle-8.7-informational?style=flat-square&logo=gradle)](https://github.com/gradle/gradle)
[![Ktlint](https://img.shields.io/badge/Ktlint-1.2.1-informational?style=flat-square)](https://github.com/pinterest/ktlint)

[![Github - Version](https://img.shields.io/github/v/tag/Buried-In-Code/Kalibak?logo=Github&label=Version&style=flat-square)](https://github.com/Buried-In-Code/Kalibak/tags)
[![Github - License](https://img.shields.io/github/license/Buried-In-Code/Kalibak?logo=Github&label=License&style=flat-square)](https://opensource.org/licenses/MIT)
[![Github - Contributors](https://img.shields.io/github/contributors/Buried-In-Code/Kalibak?logo=Github&label=Contributors&style=flat-square)](https://github.com/Buried-In-Code/Kalibak/graphs/contributors)

A Java/Kotlin wrapper for the [metron.cloud](https://metron.cloud) API.

## Installation

```kotlin
dependencies {
    implementation("github.buriedincode", "kalibak", "0.2.0")
}
```

## Example Usage

```kotlin
import github.buriedincode.kalibak.Metron

fun main(vararg args: String) {
    val (username, password) = loadConfig()  // Load your credentials from config file or env variables
    
    val session = Metron(username = username, password = password)
    
    // Get all Marvel comics for the week of 2023-Apr-15
    val weeklyComics = session.listIssues(params = mapOf("store_date_range_after" to "2023-04-14", "store_date_range_before" to "2023-04-22", "publisher_name" to "marvel"))
    // Print the results
    weeklyComics.forEach(println("${it.id} ${it.issue_name}"))
    
    // Retrieve the details for an individual issue
    val spiderMan68 = session.getIssue(id = 31660)
    // Print the issue Description
    println(spiderMan68.description)
}
```
