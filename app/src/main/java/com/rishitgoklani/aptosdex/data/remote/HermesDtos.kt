package com.rishitgoklani.aptosdex.data.remote

data class HermesPriceFeedDto(
    val id: String?,
    val attributes: AttributesDto?
)

data class AttributesDto(
    val asset_type: String?,
    val base: String?,
    val description: String?,
    val display_symbol: String?,
    val generic_symbol: String?,
    val quote_currency: String?,
    val symbol: String?
)


