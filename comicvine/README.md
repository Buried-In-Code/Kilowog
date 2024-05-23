# Kilowog - Comicvine

A Java/Kotlin wrapper for the [Comicvine](https://comicvine.gamespot.com) API.

## Example Usage

```kotlin
import github.comiccorps.comicvine.Comicvine

fun main(vararg args: String) {
    val apiKey = loadConfig()  // Load your credentials from config file or env variables
    
    val session = Comicvine(apiKey = apiKey)
    TODO()
}
```
