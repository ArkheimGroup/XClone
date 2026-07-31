import java.io.File

/**
 * Kotlin Script to configure Server (application.properties) and Client (config.properties) settings or reset them.
 * 
 * How to run this script:
 * 1. Install kotlin compiler
 * 2. Run the script:
 *    kotlin configure-properties.kts
 *    or to configure client directly:
 *    kotlin configure-properties.kts client
 *    or to reset server:
 *    kotlin configure-properties.kts reset-server
 *    or to reset client:
 *    kotlin configure-properties.kts reset-client
 * 
 * Alternatively, open this project in IntelliJ IDEA, right-click this script,
 * and select "Run 'configure-properties.kts'".
 */

val serverPropertiesPath = "server/src/main/resources/application.properties"
val clientPropertiesPath = "client/src/main/resources/config.properties"

val serverPropertiesFile = File(serverPropertiesPath)
val clientPropertiesFile = File(clientPropertiesPath)

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

fun resetServerProperties() {
    if (!serverPropertiesFile.exists()) {
        println("Error: $serverPropertiesPath not found at: ${serverPropertiesFile.absolutePath}")
        return
    }
    val lines = serverPropertiesFile.readLines()
    val defaultUrl = "spring.datasource.url=jdbc:mysql://\${DB_HOST}:\${DB_PORT}/\${DB_NAME}?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
    val defaultUsername = "spring.datasource.username=\${DB_USER}"
    val defaultPassword = "spring.datasource.password=\${DB_PASSWORD}"

    var urlUpdated = false
    var userUpdated = false
    var passwordUpdated = false

    val updatedLines = lines.map { line ->
        val trimmed = line.trim()
        when {
            trimmed.startsWith("spring.datasource.url=") -> {
                urlUpdated = true
                defaultUrl
            }
            trimmed.startsWith("spring.datasource.username=") -> {
                userUpdated = true
                defaultUsername
            }
            trimmed.startsWith("spring.datasource.password=") -> {
                passwordUpdated = true
                defaultPassword
            }
            else -> line
        }
    }.toMutableList()

    if (!urlUpdated) updatedLines.add(defaultUrl)
    if (!userUpdated) updatedLines.add(defaultUsername)
    if (!passwordUpdated) updatedLines.add(defaultPassword)

    try {
        serverPropertiesFile.writeText(updatedLines.joinToString("\n") + "\n")
        println()
        println("[SUCCESS] Server application.properties has been reset to default placeholders.")
        println("Database URL: jdbc:mysql://\${DB_HOST}:\${DB_PORT}/\${DB_NAME}...")
        println("Username:     \${DB_USER}")
        println("Password:     \${DB_PASSWORD}")
        println("==================================================")
    } catch (e: Exception) {
        println()
        println("[ERROR] Failed to write to $serverPropertiesPath: ${e.message}")
        println("==================================================")
    }
}

fun configureServerProperties() {
    if (!serverPropertiesFile.exists()) {
        println("Error: $serverPropertiesPath not found at: ${serverPropertiesFile.absolutePath}")
        return
    }
    val lines = serverPropertiesFile.readLines()

    var currentHost = "localhost"
    var currentPort = "3306"
    var currentDb = "x_clone"
    var currentUser = "root"
    var currentPassword = ""

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

    println()
    println("Please enter the MySQL database credentials.")
    println("Press [Enter] to accept the default value shown in brackets.")
    println()

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

    if (!urlUpdated) updatedLines.add("spring.datasource.url=jdbc:mysql://$host:$port/$dbName?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC")
    if (!userUpdated) updatedLines.add("spring.datasource.username=$username")
    if (!passwordUpdated) updatedLines.add("spring.datasource.password=$password")

    try {
        serverPropertiesFile.writeText(updatedLines.joinToString("\n") + "\n")
        println()
        println("[SUCCESS] Server configuration written to $serverPropertiesPath")
        println("Database URL: jdbc:mysql://$host:$port/$dbName")
        println("Username:     $username")
        println("==================================================")
    } catch (e: Exception) {
        println()
        println("[ERROR] Failed to write to $serverPropertiesPath: ${e.message}")
        println("==================================================")
    }
}

fun resetClientProperties() {
    val defaultContent = """
        # Client Network Configuration
        server.baseUrl=http://127.0.0.1:8080
        feed.socket.host=localhost
        feed.socket.port=8082
    """.trimIndent()

    try {
        clientPropertiesFile.parentFile?.mkdirs()
        clientPropertiesFile.writeText(defaultContent + "\n")
        println()
        println("[SUCCESS] Client config.properties has been reset to default values.")
        println("Server Base URL:  http://127.0.0.1:8080")
        println("Feed Socket Host: localhost")
        println("Feed Socket Port: 8082")
        println("==================================================")
    } catch (e: Exception) {
        println()
        println("[ERROR] Failed to write to $clientPropertiesPath: ${e.message}")
        println("==================================================")
    }
}

fun configureClientProperties() {
    var currentBaseUrl = "http://127.0.0.1:8080"
    var currentSocketHost = "localhost"
    var currentSocketPort = "8082"

    val lines = if (clientPropertiesFile.exists()) clientPropertiesFile.readLines() else emptyList()

    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.startsWith("server.baseUrl=")) {
            val v = trimmed.substringAfter("=").trim()
            if (v.isNotEmpty()) currentBaseUrl = v
        } else if (trimmed.startsWith("feed.socket.host=")) {
            val v = trimmed.substringAfter("=").trim()
            if (v.isNotEmpty()) currentSocketHost = v
        } else if (trimmed.startsWith("feed.socket.port=")) {
            val v = trimmed.substringAfter("=").trim()
            if (v.isNotEmpty()) currentSocketPort = v
        }
    }

    println()
    println("Please enter the Client network configuration.")
    println("Press [Enter] to accept the default value shown in brackets.")
    println()

    val baseUrl = prompt("Server Base URL", currentBaseUrl)
    val socketHost = prompt("Feed Socket Host", currentSocketHost)
    val socketPort = prompt("Feed Socket Port", currentSocketPort)

    var baseUrlUpdated = false
    var socketHostUpdated = false
    var socketPortUpdated = false

    val updatedLines = lines.map { line ->
        val trimmed = line.trim()
        when {
            trimmed.startsWith("server.baseUrl=") -> {
                baseUrlUpdated = true
                "server.baseUrl=$baseUrl"
            }
            trimmed.startsWith("feed.socket.host=") -> {
                socketHostUpdated = true
                "feed.socket.host=$socketHost"
            }
            trimmed.startsWith("feed.socket.port=") -> {
                socketPortUpdated = true
                "feed.socket.port=$socketPort"
            }
            else -> line
        }
    }.toMutableList()

    if (!baseUrlUpdated) updatedLines.add("server.baseUrl=$baseUrl")
    if (!socketHostUpdated) updatedLines.add("feed.socket.host=$socketHost")
    if (!socketPortUpdated) updatedLines.add("feed.socket.port=$socketPort")

    if (updatedLines.isEmpty() || !updatedLines.any { it.trim().startsWith("#") }) {
        updatedLines.add(0, "# Client Network Configuration")
    }

    try {
        clientPropertiesFile.parentFile?.mkdirs()
        clientPropertiesFile.writeText(updatedLines.joinToString("\n") + "\n")
        println()
        println("[SUCCESS] Client configuration written to $clientPropertiesPath")
        println("Server Base URL:  $baseUrl")
        println("Feed Socket Host: $socketHost")
        println("Feed Socket Port: $socketPort")
        println("==================================================")
    } catch (e: Exception) {
        println()
        println("[ERROR] Failed to write to $clientPropertiesPath: ${e.message}")
        println("==================================================")
    }
}

val arg = args.firstOrNull()?.lowercase()

when (arg) {
    "reset", "reset-server", "--reset-server" -> {
        println("==================================================")
        println("      Resetting Server application.properties")
        println("==================================================")
        resetServerProperties()
        System.exit(0)
    }
    "reset-client", "--reset-client" -> {
        println("==================================================")
        println("      Resetting Client config.properties")
        println("==================================================")
        resetClientProperties()
        System.exit(0)
    }
    "client", "config-client", "--client" -> {
        println("==================================================")
        println("      Client Network Configuration Setup")
        println("==================================================")
        configureClientProperties()
        System.exit(0)
    }
    "server", "config-server", "--server" -> {
        println("==================================================")
        println("      Server MySQL Database Configuration Setup")
        println("==================================================")
        configureServerProperties()
        System.exit(0)
    }
}

println("==================================================")
println("          Project Configuration Setup")
println("==================================================")
println("Select an operation:")
println("1. Configure Server application.properties (MySQL Database)")
println("2. Configure Client config.properties (Server URL & Socket)")
println("3. Reset Server application.properties to default placeholders (\${DB_HOST}, etc.)")
println("4. Reset Client config.properties to default values")
print("Enter choice [1/2/3/4] (default 1): ")
val choice = readLine()?.trim() ?: ""

when (choice) {
    "2", "client" -> configureClientProperties()
    "3", "reset-server", "reset" -> resetServerProperties()
    "4", "reset-client" -> resetClientProperties()
    else -> configureServerProperties()
}
