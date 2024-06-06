package github.comiccorps.kilowog

data class Details(
    val series: Identifications,
    val issue: Identifications,
) {
    data class Identifications(
        val comicvine: Long? = null,
        val league: Long? = null,
        val marvel: Long? = null,
        var metron: Long? = null,
        val search: String? = null,
    )
}
