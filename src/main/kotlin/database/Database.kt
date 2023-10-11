package database

import org.apache.commons.text.CaseUtils
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement
import kotlin.reflect.KClass

class Database(private val url: String) {

    private var connection: Connection? = null

    init {
        Class.forName("org.sqlite.JDBC")
        connection = DriverManager.getConnection("jdbc:sqlite:$url")
    }

    fun <T : Any> createTableIfNotExists(tableClass: KClass<T>) {
        var tableName = CaseUtils.toCamelCase(tableClass.simpleName, false)
        val properties = tableClass.java.declaredFields
        val statement: Statement? = connection?.createStatement()

        if(!tableName.endsWith('s')) tableName += "s"

        if (statement != null) {
            val createTableSQL = buildCreateTableSQL(tableName, properties)
            statement.executeUpdate(createTableSQL)
            statement.close()
        }
    }

    private fun buildCreateTableSQL(tableName: String?, properties: Array<java.lang.reflect.Field>): String {
        if (tableName == null || properties.isEmpty()) {
            throw IllegalArgumentException("Table name or properties cannot be null or empty.")
        }

        val columns = properties.joinToString(", ") {
            val columnName = it.name
            var columnType = getSQLType(it.type)

            if(it.isAnnotationPresent(PrimaryKey::class.java)) {
                columnType += " PRIMARY KEY"
            }

            if(it.isAnnotationPresent(AutoIncrement::class.java)) {
                columnType += " AUTOINCREMENT"
            }

            /*val columnType = if (it.isAnnotationPresent(PrimaryKey::class.java)) {
                "INTEGER PRIMARY KEY AUTOINCREMENT"
            } else {
                getSQLType(it.type)
            }*/
            "$columnName $columnType"
        }

        return "CREATE TABLE IF NOT EXISTS $tableName ($columns);"
    }

    private fun getSQLType(type: Class<*>): String {
        return when (type) {
            Int::class.java, Long::class.java -> "INTEGER"
            String::class.java -> "TEXT"
            else -> "TEXT"
        }
    }

}