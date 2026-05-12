package net.vanolex

import java.sql.DriverManager
import java.sql.Statement

fun getLink(publicId: String): String? {
    checkConnection()

    val internalId = b64URLToId(publicId)

    val sql = "SELECT link FROM Links WHERE id = ?"
    return mysqlConnection.prepareStatement(sql).use { stmt ->
        stmt.setInt(1, internalId)
        stmt.executeQuery().use { rs ->
            if (rs.next()) rs.getString("link") else null
        }
    }
}

fun addLink(link: String, ip: String): String {
    checkConnection()

    val insertSql = "INSERT INTO Links (link, ip) VALUES (?, ?)"

    val stmt = mysqlConnection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)
    stmt.setString(1, link)
    stmt.setString(2, ip)
    stmt.executeUpdate()

    val rs = stmt.generatedKeys
    if (!rs.next()) throw RuntimeException("Error while fetching link ID.")

    return idToB64URL(rs.getInt(1))
}

fun checkConnection() {
    try {
        mysqlConnection.prepareStatement("DO 0").execute()
    } catch (e: Exception) {
        mysqlConnection.close()
        mysqlConnection = DriverManager.getConnection(connectionURL, config.dbUsername, config.dbPassword)
    }
}
