import java.io.File

/**
 * Kotlin Script to configure MySQL Database credentials in application.properties.
 * 
 * How to run this script:
 * 1. Install kotlin compiler
 * 2. Run the script:
 *    kotlin configure-db.kts
 * 
 * Alternatively, open this project in IntelliJ IDEA, right-click this script,
 * and select "Run 'configure-db.kts'".
 */

val propertiesPath = "server/src/main/resources/application.properties"
val propertiesFile = File(propertiesPath)

if (!propertiesFile.exists()) {
    println("Error: application.properties not found at: ${propertiesFile.absolutePath}")
    System.exit(1)
}

val lines = propertiesFile.readLines()

// Extract default values from current configurations if they aren't placeholders
var currentHost = "localhost"
var currentPort = "3306"
var currentDb = "x_clone"
var currentUser = "root"
var currentPassword = ""

// Regex to capture host, port, and db from the existing mysql connection string
val urlPattern = """^spring\.datasource\.url\s*=\s*jdbc:mysql://([^:/]+)(?::(\d+))?/([^?]+)(?:\?.*)?""".toRegex()
val userPattern = """^spring\.datasource\.username\s*=\s*(.+)""".toRegex()
val passwordPattern = """^spring\.datasource\.password\s*=\s*(.*)""".toRegex()

for (line in lines) {
    val trimmed = line.trim()
    urlPattern.find(trimmed)?.let { match ->
        val host = match.groupValues[1]
        val port = match.groupValues[2]
        val db = match.groupValues[3]
        
        if (host != "\${DB_HOST}") currentHost = host
        if (port.isNotEmpty() && port != "\${DB_PORT}") currentPort = port
        if (db != "\${DB_NAME}") currentDb = db
    }
    userPattern.find(trimmed)?.let { match ->
        val user = match.groupValues[1].trim()
        if (user != "\${DB_USER}") currentUser = user
    }
    passwordPattern.find(trimmed)?.let { match ->
        val pwd = match.groupValues[1].trim()
        if (pwd != "\${DB_PASSWORD}") currentPassword = pwd
    }
}

println("==================================================")
println("      MySQL Database Configuration Setup")
println("==================================================")
println("Please enter the MySQL database credentials.")
println("Press [Enter] to accept the default value shown in brackets.")
println()

fun prompt(message: String, default: String): String {
    print("$message [$default]: ")
    val input = readLine()?.trim() ?: ""
    return if (input.isEmpty()) default else input
}

fun promptPassword(message: String, default: String): String {
    val displayDefault = if (default.isEmpty()) "empty" else "********"
    print("$message [$displayDefault]: ")
    val input = readLine()?.trim() ?: ""
    return if (input.isEmpty()) default else input
}

val host = prompt("MySQL Host", currentHost)
val port = prompt("MySQL Port", currentPort)
val dbName = prompt("Database Name", currentDb)
val username = prompt("Username", currentUser)
val password = promptPassword("Password", currentPassword)

var urlUpdated = false
var userUpdated = false
var passwordUpdated = false

val updatedLines = lines.map { line ->
    val trimmed = line.trim()
    when {
        trimmed.startsWith("spring.datasource.url=") -> {
            urlUpdated = true
            "spring.datasource.url=jdbc:mysql://$host:$port/$dbName?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
        }
        trimmed.startsWith("spring.datasource.username=") -> {
            userUpdated = true
            "spring.datasource.username=$username"
        }
        trimmed.startsWith("spring.datasource.password=") -> {
            passwordUpdated = true
            "spring.datasource.password=$password"
        }
        else -> line
    }
}.toMutableList()

if (!urlUpdated) {
    updatedLines.add("spring.datasource.url=jdbc:mysql://$host:$port/$dbName?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC")
}
if (!userUpdated) {
    updatedLines.add("spring.datasource.username=$username")
}
if (!passwordUpdated) {
    updatedLines.add("spring.datasource.password=$password")
}

try {
    propertiesFile.writeText(updatedLines.joinToString("\n") + "\n")
    println()
    println("[SUCCESS] Configuration written to $propertiesPath")
    println("Database URL: jdbc:mysql://$host:$port/$dbName")
    println("Username:     $username")
    println("==================================================")
} catch (e: Exception) {
    println()
    println("[ERROR] Failed to write to application.properties: ${e.message}")
    println("==================================================")
}
