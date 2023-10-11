package model

import database.AutoIncrement
import database.PrimaryKey

data class Ingredient (
    @PrimaryKey
    @AutoIncrement
    val id: Int,
    val name: String
)