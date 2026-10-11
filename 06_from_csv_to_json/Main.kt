import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// Словарь перевода
val categoriesRu = mapOf(
    "ART_AND_DESIGN" to "Искусство и дизайн",
    "AUTO_AND_VEHICLES" to "Авто и транспорт",
    "BEAUTY" to "Красота",
    "BOOKS_AND_REFERENCE" to "Книги и справочники",
    "BUSINESS" to "Бизнес",
    "COMICS" to "Комиксы",
    "COMMUNICATION" to "Связь",
    "DATING" to "Знакомства",
    "EDUCATION" to "Образование",
    "ENTERTAINMENT" to "Развлечения",
    "EVENTS" to "События",
    "FAMILY" to "Семья",
    "FINANCE" to "Финансы",
    "FOOD_AND_DRINK" to "Еда и напитки",
    "GAME" to "Игры",
    "HEALTH_AND_FITNESS" to "Здоровье и фитнес",
    "HOUSE_AND_HOME" to "Дом и интерьер",
    "LIBRARIES_AND_DEMO" to "Библиотеки и демо",
    "LIFESTYLE" to "Стиль жизни",
    "MAPS_AND_NAVIGATION" to "Карты и навигация",
    "MEDICAL" to "Медицина",
    "NEWS_AND_MAGAZINES" to "Новости и журналы",
    "PARENTING" to "Для родителей",
    "PERSONALIZATION" to "Персонализация",
    "PHOTOGRAPHY" to "Фотография",
    "PRODUCTIVITY" to "Продуктивность",
    "SHOPPING" to "Покупки",
    "SOCIAL" to "Социальные сети",
    "SPORTS" to "Спорт",
    "TOOLS" to "Инструменты",
    "TRAVEL_AND_LOCAL" to "Путешествия",
    "VIDEO_PLAYERS" to "Видеоплееры",
    "WEATHER" to "Погода"
)

// Таблица ОС Android - API
val androidApiMap = mapOf(
    "1.0" to 1, "1.1" to 2, "1.5" to 3, "1.6" to 4,
    "2.0" to 5, "2.0.1" to 6, "2.1" to 7, "2.2" to 8, "2.3" to 9, "2.3.3" to 10,
    "3.0" to 11, "3.1" to 12, "3.2" to 13,
    "4.0" to 14, "4.0.3" to 15, "4.1" to 16, "4.2" to 17, "4.3" to 18, "4.4" to 19, "4.4W" to 20,
    "5.0" to 21, "5.1" to 22,
    "6.0" to 23,
    "7.0" to 24, "7.1" to 25,
    "8.0" to 26, "8.1" to 27
)

// Парсинг CSV-строки
fun parseCsvLine(line: String): List<String> {
    val tokens = mutableListOf<String>()
    var inQuotes = false
    val sb = StringBuilder()

    for (c in line) {
        when {
            c == '"' -> inQuotes = !inQuotes
            c == ',' && !inQuotes -> {
                tokens.add(sb.toString().trim().removeSurrounding("\"").replace("\"\"", "\""))
                sb.clear()
            }
            else -> sb.append(c)
        }
    }
    tokens.add(sb.toString().trim().removeSurrounding("\"").replace("\"\"", "\""))
    return tokens
}

// Мин. API по Android
fun parseMinApi(raw: String): Int? {
    val match = Regex("""\d+(\.\d+)*W?""").find(raw) ?: return null
    var version = match.value
    if (version !in androidApiMap) {
        val parts = version.split(".")
        if (parts.size >= 2) {
            version = "${parts[0]}.${parts[1]}"
        }
    }
    return androidApiMap[version]
}

// Дата в формат ISO 8601
val inputDateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH)

fun parseIsoDate(raw: String): String? {
    return try {
        val date = LocalDate.parse(raw.trim(), inputDateFormatter)
        date.format(DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (e: Exception) {
        null
    }
}

fun escapeJson(text: String): String {
    return text.replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\b", "\\b")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
}

// Модель данных приложения
data class App(
    val app: String,
    val rating: Float?,
    val reviews: Long?,
    val size: String,
    val installs: Long?,
    val isPaid: Boolean,
    val contentRating: String,
    val genres: List<String>,
    val lastUpdated: String?,
    val currentVer: String,
    val minApi: Int?
) {
    fun toJson(): String {
        val genresJson = genres.joinToString(", ") { "\"${escapeJson(it)}\"" }
        val ratingStr = rating?.toString() ?: "null"
        val reviewsStr = reviews?.toString() ?: "null"
        val installsStr = installs?.toString() ?: "null"
        val minApiStr = minApi?.toString() ?: "null"
        val lastUpdatedStr = lastUpdated?.let { "\"$it\"" } ?: "null"

        return """      {
        "App": "${escapeJson(app)}",
        "Rating": $ratingStr,
        "Reviews": $reviewsStr,
        "Size": "${escapeJson(size)}",
        "Installs": $installsStr,
        "Price": $isPaid,
        "Content Rating": "${escapeJson(contentRating)}",
        "Genres": [$genresJson],
        "Last Updated": $lastUpdatedStr,
        "Current Ver": "${escapeJson(currentVer)}",
        "Android Ver": $minApiStr
      }"""
    }
}

fun main() {
    val csvFile = File("06_from_csv_to_json/googleplaystore.csv")
    val actualCsv = if (csvFile.exists()) csvFile else File("googleplaystore.csv")
    val outputFile = File(actualCsv.parentFile ?: File("."), "googleplaystore.json")

    val grouped = mutableMapOf<String, MutableList<App>>()
    var skipped = 0
    var processed = 0

    val lines = actualCsv.readLines(Charsets.UTF_8)

    // Пропускаем заголовок
    for (i in 1 until lines.size) {
        val line = lines[i]
        if (line.isBlank()) continue

        val row = parseCsvLine(line)
        // Если в строке меньше 13 колонок, пропускаем
        if (row.size < 13) {
            skipped++
            continue
        }

        val rawCategory = row[1]
        val categoryRu = categoriesRu[rawCategory]
        // Пропускаем строки с некорректной категорией
        if (categoryRu == null) {
            skipped++
            continue
        }

        val app = App(
            app = row[0],
            rating = row[2].toFloatOrNull(),
            reviews = row[3].toLongOrNull(),
            size = row[4],
            installs = row[5].replace(",", "").replace("+", "").trim().toLongOrNull(),
            isPaid = row[7].trim() !in listOf("0", "0.0", "Free", ""),
            contentRating = row[8],
            genres = row[9].split(";").map { it.trim() },
            lastUpdated = parseIsoDate(row[10]),
            currentVer = row[11],
            minApi = parseMinApi(row[12])
        )

        grouped.computeIfAbsent(categoryRu) { mutableListOf() }.add(app)
        processed++
    }

    val sb = StringBuilder()
    sb.append("{\n")
    val entries = grouped.entries.toList()
    for (i in entries.indices) {
        val (category, apps) = entries[i]
        sb.append("  \"${escapeJson(category)}\": [\n")
        for (j in apps.indices) {
            sb.append(apps[j].toJson())
            if (j < apps.size - 1) sb.append(",")
            sb.append("\n")
        }
        sb.append("  ]")
        if (i < entries.size - 1) sb.append(",")
        sb.append("\n")
    }
    sb.append("}\n")

    outputFile.writeText(sb.toString(), Charsets.UTF_8)

    println("Обработано приложений: $processed")
    println("Категорий создано: ${grouped.size}")
    println("Пропущено битых строк: $skipped")
    println("Результат записан в: ${outputFile.name}")
}