# Kilowog - Metron

A Java/Kotlin wrapper for the [metron.cloud](https://metron.cloud) API.

## Example Usage

```kotlin
import github.comiccorps.metron.Metron

fun main(vararg args: String) {
    val (username, password) = loadConfig()  // Load your credentials from config file or env variables
    
    val session = Metron(username = username, password = password)
    
    // Get all Marvel comics for the week of 2023-Apr-15
    val weeklyComics = session.listIssues(params = mapOf(
        "store_date_range_after" to "2023-04-14",
        "store_date_range_before" to "2023-04-22",
        "publisher_name" to "marvel"
    ))
    // Print the results
    weeklyComics.forEach(println("${it.id} ${it.issue_name}"))
    
    // Retrieve the details for an individual issue
    val spiderMan68 = session.getIssue(id = 31660)
    // Print the issue Description
    println(spiderMan68.description)
}
```
