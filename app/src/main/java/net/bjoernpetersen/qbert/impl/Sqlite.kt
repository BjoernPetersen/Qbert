package net.bjoernpetersen.qbert.impl

import java.sql.Driver
import java.sql.DriverManager

fun setupSqlite() {
    DriverManager.registerDriver(Class.forName("org.sqldroid.SQLDroidDriver").newInstance() as Driver)
    val sqlite = DriverManager.getDrivers().asSequence()
        .filter { it::class.java.name == "org.sqlite.JDBC" }
        .toList()
    sqlite.forEach { DriverManager.deregisterDriver(it) }
}
