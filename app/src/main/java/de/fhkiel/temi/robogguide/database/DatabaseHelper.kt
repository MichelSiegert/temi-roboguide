package de.fhkiel.temi.robogguide.database

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import de.fhkiel.temi.robogguide.Routes
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class   DatabaseHelper(context: Context, private val databaseName: String) : SQLiteOpenHelper(context, databaseName, null, 1) {

    private val databasePath = File(context.getDatabasePath(databaseName).path).absolutePath
    private val databaseFullPath = "$databasePath$databaseName"
    private var database: SQLiteDatabase? = null
    private val appContext: Context = context
    private var dbFile: File? = null

    override fun onCreate(db: SQLiteDatabase) {}

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    @Suppress("unused")
    fun getDatabase(): SQLiteDatabase? {
        return database
    }

    @Suppress("unused")
    fun getDBFile(): File? {
        return dbFile
    }

    /**
     * Method to copy the database from assets to internal storage (always overwrites existing database)
     * @return  output File or null
     */
    @Throws(IOException::class)
    private fun copyDatabase(): File {
        val inputStream: InputStream = appContext.assets.open(databaseName)
        val outFileName = databaseFullPath
        val outFile = File(outFileName)
        val outputStream: OutputStream = FileOutputStream(outFile)

        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }

        outputStream.flush()
        outputStream.close()
        inputStream.close()

        return outFile
    }

    /**
     * Method to initialize the database (always copies and overwrites any existing database)
     * @param withOpen  Set to false, to only copy the database, otherwise it is also opened (default)
     */
    @Throws(IOException::class)
    fun initializeDatabase(withOpen: Boolean = true) {
        this.readableDatabase // Create an empty database in the default system path
        dbFile = copyDatabase() // Overwrite the existing database with the one from assets

        if (withOpen) {
            openDatabase() // Open the copied database
        }
    }

    // Method to open the copied database and store its reference
    private fun openDatabase(): SQLiteDatabase? {
        database =
            SQLiteDatabase.openDatabase(databaseFullPath, null, SQLiteDatabase.OPEN_READWRITE)
        return database
    }

    // Method to close the opened database
    fun closeDatabase() {
        database?.close()
    }

    // Method to retrieve the table structure dynamically, including the primary key
    private fun getTableStructure(tableName: String): Pair<List<String>, String?> {
        val columns = mutableListOf<String>()
        var primaryKey: String? = null

        database?.let { db ->
            val cursor = db.rawQuery("PRAGMA table_info(`$tableName`)", null)

            if (cursor.moveToFirst()) {
                do {
                    // Retrieve the column name from the result
                    val columnName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                    columns.add(columnName)

                    // Check if this column is the primary key
                    val isPrimaryKey = cursor.getInt(cursor.getColumnIndexOrThrow("pk")) == 1
                    if (isPrimaryKey) {
                        primaryKey = columnName
                    }
                } while (cursor.moveToNext())
            }
            cursor.close()
        }

        return Pair(columns, primaryKey)
    }

    // Method to read data dynamically and convert it into a JSON Map using the primary key as the key
    @Suppress("unused")
    fun getTableDataAsJson(tableName: String): Map<String, JSONObject> {
        val (columns, primaryKey) = getTableStructure(tableName)

        if (primaryKey == null) {
            throw IllegalArgumentException("Table $tableName has no primary key.")
        }

        val jsonMap = mutableMapOf<String, JSONObject>()

        database?.let { db ->
            val cursor = db.rawQuery("SELECT * FROM `$tableName`", null)

            if (cursor.moveToFirst()) {
                do {
                    val jsonObject = JSONObject()
                    var primaryKeyValue: String? = null

                    for (column in columns) {
                        val value = cursor.getString(cursor.getColumnIndexOrThrow(column))
                        jsonObject.put(column, value)

                        // Check if this column is the primary key and save its value
                        if (column == primaryKey) {
                            primaryKeyValue = value
                        }
                    }

                    // Ensure the primary key value is not null before adding to the map
                    primaryKeyValue?.let {
                        jsonMap[it] = jsonObject
                    }
                } while (cursor.moveToNext())
            }
            cursor.close()
        }

        return jsonMap
    }

    @SuppressLint("Range")
    fun getTextsOfLocation(location: String, isAusführlich: Boolean): List<String> {
        val resultList = mutableListOf<String>() // To store the results

        database?.let { db ->
            val cursor = db.rawQuery(
                "WITH a AS (" +
                        "SELECT id FROM locations WHERE LOWER(locations.name) = LOWER( ? ) ) " +
                        "SELECT texts.text, texts.title, texts.id " +
                        "FROM texts " +
                        "INNER JOIN a ON texts.locations_id = a.id " +
                        "ORDER BY texts.detailed  "+if(isAusführlich)"DESC " else "ASC " +
                        "LIMIT 1",
                arrayOf(location)
            )

            if (cursor == null || cursor.count == 0) {
                cursor?.close()
                return listOf()
            }

            if (cursor.moveToFirst()) {
                do {
                    resultList.add(cursor.getString(cursor.getColumnIndex("text")))
                    resultList.add(
                        cursor.getString(cursor.getColumnIndex("title")))
                    resultList.add(
                        cursor.getString(cursor.getColumnIndex("id")))
                } while (cursor.moveToNext())
            }

            cursor.close()
        }
        return resultList
    }

    @SuppressLint("Range")
    fun getMediaOfText(id: String): Array<String> {
        val resultList = mutableListOf<String>() // To store the results

        database?.let { db ->
            val cursor = db.rawQuery(
                "SELECT media.url " +
                        "FROM media " +
                        "INNER JOIN texts ON texts.id = media.texts_id " +
                        "WHERE texts.id = ? ",
                arrayOf(id)
            )

            if (cursor == null || cursor.count == 0) {
                cursor?.close()
                return arrayOf()
            }

            if (cursor.moveToFirst()) {
                do {
                    val url = cursor.getString(cursor.getColumnIndex("url"))
                    resultList.add(url)
                } while (cursor.moveToNext())
            }

            cursor.close()
        }
        return resultList.toTypedArray()
    }

    @SuppressLint("Range")
    fun getTextsOfItems(location: String, isAusführlich: Boolean): Array<List<String>> {
        val resultList = mutableListOf<List<String>>() // To store the results

        database?.let { db ->
            val cursor = db.rawQuery(
                "WITH a AS (SELECT id FROM locations WHERE LOWER(locations.name) = LOWER( ? ) ) " +
                        "SELECT texts.text, texts.title, texts.id " +
                        "FROM items " +
                        "INNER JOIN a ON items.locations_id = a.id " +
                        "INNER JOIN texts ON texts.items_id = items.id " +
                        "GROUP BY texts.id " +
                        "HAVING  " + (if(isAusführlich)"MAX " else "MIN ") + "(texts.detailed)",
                arrayOf(location)
            )

            if (cursor == null || cursor.count == 0) {
                cursor?.close()
                return arrayOf()
            }

            if (cursor.moveToFirst()) {
                do {
                    val text =
                        cursor.getString(cursor.getColumnIndex("text"))
                    val title =
                        cursor.getString(cursor.getColumnIndex("title"))
                    val id =
                        cursor.getString(cursor.getColumnIndex("id"))
                    resultList.add(listOf(text, title, id))
                } while (cursor.moveToNext())
            }

            cursor.close()
        }
        return resultList.toTypedArray()
    }

    @SuppressLint("Range")
    fun getTextsOfTransfer(location: String, isAusführlich: Boolean): List<String> {
        val resultList = mutableListOf<String>() // To store the results

        database?.let { db ->
            val cursor = db.rawQuery(
                "WITH a AS (SELECT id FROM locations WHERE LOWER(locations.name) = LOWER( ? ) ) " +
                        "SELECT texts.text, texts.title, texts.id " +
                        "FROM transfers " +
                        "INNER JOIN a ON transfers.location_to = a.id " +
                        "INNER JOIN texts ON texts.transfers_id = transfers.id " +
                        "ORDER BY texts.detailed  "+if(isAusführlich)"DESC " else "ASC " +
                        "LIMIT 1",
                arrayOf(location)
            )

            if (cursor == null || cursor.count == 0) {
                cursor?.close()
                return listOf("", "", "")
            }

            if (cursor.moveToFirst()) {
                do {
                    val text =
                        cursor.getString(cursor.getColumnIndex("text"))
                    val title =
                        cursor.getString(cursor.getColumnIndex("title"))
                    val id =
                        cursor.getString(cursor.getColumnIndex("id"))
                    resultList.add(text)
                    resultList.add(title)
                    resultList.add(id)
                } while (cursor.moveToNext())
            }

            cursor.close()
        }
        return resultList
    }

    @SuppressLint("Range")
    fun getAllTransfers(): Array<Pair<String, String>> {
        val resultList = mutableListOf<Pair<String, String>>()

        database?.let { db ->
            val cursor = db.rawQuery(
                "SELECT location_from, location_to " +
                        "FROM transfers;", arrayOf()
            )


            if (cursor == null || cursor.count == 0) {
                cursor?.close()
                return arrayOf()
            }

            if (cursor.moveToFirst()) {
                do {
                    val text =
                        cursor.getString(cursor.getColumnIndex("location_from"))
                    val title =
                        cursor.getString(cursor.getColumnIndex("location_to"))
                    resultList.add(Pair(text, title))
                } while (cursor.moveToNext())
            }

            cursor.close()
        }
        return resultList.toTypedArray()
    }

    @SuppressLint("Range")
    fun getLocationMap(): Map<String, String>{
        val resultList = mutableMapOf<String,String>()

        database?.let { db ->
            val cursor = db.rawQuery(
                "SELECT id, name " +
                        "FROM locations;", arrayOf()
            )


            if (cursor == null || cursor.count == 0) {
                cursor?.close()
                return mapOf()
            }

            if (cursor.moveToFirst()) {
                do {
                    val text =
                        cursor.getString(cursor.getColumnIndex("id"))
                    val title =
                        cursor.getString(cursor.getColumnIndex("name"))
                    resultList[text] = title
                } while (cursor.moveToNext())
            }

            cursor.close()
        }
        return resultList.toMap()
    }

    @SuppressLint("Range")
    fun getAllImportantStations(): Array<String> {
        val resultList = mutableListOf<String>()

        database?.let { db ->
            val cursor = db.rawQuery(
                "SELECT name " +
                        "FROM locations " +
                        "WHERE important=1;", arrayOf()
            )

            if (cursor == null || cursor.count == 0) {
                cursor?.close()
                return arrayOf("")
            }

            if (cursor.moveToFirst()) {
                do {
                    resultList.add(cursor.getString(cursor.getColumnIndex("name"))
                    )
                } while (cursor.moveToNext())
            }

            cursor.close()
        }
        return resultList.toTypedArray()
    }

    @SuppressLint("Range")
    fun getImageOfLocation(location: String): String {
        var resultList = ""
        val locationID = Routes.map.filter { it.value == location}.toList()[0].first

        database?.let { db ->
            val cursor = db.rawQuery(
                "SELECT url\n" +
                        "FROM MEDIA " +
                        "INNER JOIN TEXTS ON TEXTS.id = MEDIA.texts_id "+
                        "WHERE texts.locations_id = ? AND " +
                        "MEDIA.url NOT LIKE '%youtube%' "+
                        "LIMIT 1;", arrayOf(locationID)
            )

            if (cursor == null || cursor.count == 0) {
                cursor?.close()
                return ""
            }

            if (cursor.moveToFirst()) {
                do {
                    resultList= (cursor.getString(cursor.getColumnIndex("url"))
                    )
                } while (cursor.moveToNext())
            }

            cursor.close()
        }
        return resultList
    }
}
