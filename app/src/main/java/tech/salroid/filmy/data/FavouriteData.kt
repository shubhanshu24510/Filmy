package tech.salroid.filmy.data

data class FavouriteData @JvmOverloads constructor(
    var id: String,
    var title: String? = null,
    var poster: String? = null
)