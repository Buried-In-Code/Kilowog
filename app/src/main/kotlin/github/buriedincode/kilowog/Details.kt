package github.buriedincode.kilowog

data class Details(
    val series: Identifications?,
    val issue: Identifications?,
) {
    data class Identifications(
        val search: String? = null,
        val comicvine: Long? = null,
        val league: Long? = null,
        val marvel: Long? = null,
        var metron: Long? = null,
    )
}
