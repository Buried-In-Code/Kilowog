package github.comiccorps.kilowog.console

import org.apache.logging.log4j.kotlin.Logging

internal object Console : Logging {
    private val HEADING: Colour = Colour.BLUE
    private val HIGHLIGHT: Colour = Colour.MAGENTA
    private val PROMPT: Colour = Colour.CYAN
    private val STANDARD: Colour = Colour.WHITE

    internal fun title(value: String, colour: Colour = HEADING) {
        colourOutput(value = "=".repeat(value.length + 4), colour = colour)
        heading(value = value, colour = colour)
        colourOutput(value = "=".repeat(value.length + 4), colour = colour)
    }

    internal fun heading(value: String, colour: Colour = HEADING) {
        colourOutput(value = "  $value  ", colour = colour)
    }

    internal fun menu(
        title: String? = null,
        titleColour: Colour = HEADING,
        choices: List<String>,
        choiceColours: Pair<Colour, Colour> = HIGHLIGHT to STANDARD,
        default: String? = null,
        prompt: String = "Select",
        promptColour: Colour = PROMPT,
    ): Int {
        if (!title.isNullOrBlank()) heading(value = title, colour = titleColour)
        if (choices.isEmpty()) return 0
        val padCount = choices.size.toString().length
        choices.indices.forEach {
            colourPair((it + 1).toString().padStart(padCount) to choices[it], colours = choiceColours)
        }
        if (!default.isNullOrBlank()) colourPair("0" to default, colours = choiceColours)
        val selected = prompt(prompt = prompt, promptColour = promptColour)?.toIntOrNull() ?: 0
        if ((default.isNullOrBlank() && selected == 0) || selected > choices.size) {
            logger.error("Invalid Option: `$selected`")
            return menu(
                title = title,
                titleColour = titleColour,
                choices = choices,
                choiceColours = choiceColours,
                default = default,
                prompt = prompt,
                promptColour = promptColour,
            )
        }
        return selected
    }

    internal fun confirm(prompt: String, promptColour: Colour = PROMPT): Boolean {
        return prompt(prompt = "$prompt (Y/N)", promptColour = promptColour)?.equals("y", ignoreCase = true) ?: false
    }

    internal fun prompt(prompt: String, promptColour: Colour = PROMPT): String? {
        System.out.print("${promptColour}$prompt >> ${Colour.RESET}")
        val output = readlnOrNull()?.trim()
        if (output.isNullOrBlank()) {
            return null
        }
        return output
    }

    private fun colourPair(value: Pair<String, String?>, colours: Pair<Colour, Colour> = HIGHLIGHT to STANDARD) {
        colourOutput(value = "${value.first}: ${colours.second}${value.second}", colour = colours.first)
    }

    private fun colourOutput(value: String?, colour: Colour) = println("$colour$value${Colour.RESET}")
}
