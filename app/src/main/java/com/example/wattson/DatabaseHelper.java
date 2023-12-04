package com.example.wattson;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "QuestionDatabase";
    private static final int DB_VERSION = 1;
    private Context context;
    private static DatabaseHelper instance = null;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
        // TODO:Only use in development
//        this.context.deleteDatabase(DB_NAME);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_QUESTIONS = "CREATE TABLE IF NOT EXISTS questions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "season TEXT," +
                "part TEXT," +
                "type TEXT," +
                "title TEXT," +
                "content TEXT)";
        db.execSQL(CREATE_TABLE_QUESTIONS);

        // Import data from CSV file
        importDataFromCSV(db, "questions.csv");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Upgrade database
    }

    private void importDataFromCSV(SQLiteDatabase db, String csvFilePath) {
        try {
            InputStream inputStream = context.getAssets().open(csvFilePath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            db.beginTransaction();
            try {
                String insertQueryTemplate = "INSERT INTO questions (season, part, type, title, content) VALUES (?, ?, ?, ?, ?)";
                SQLiteStatement stmt = db.compileStatement(insertQueryTemplate);

                String line;
                while ((line = reader.readLine()) != null) {
                    // Parse CSV line
                    List<String> tokens = parseCsvLineComplex(line, reader);
//                    List<String> tokens = parseCsvLineSimple(line);
                    // Check if is 5 columns
                    if (tokens.size() == 5) {
                        // Insert data into database
                        stmt.clearBindings();
                        for (int i = 0; i < tokens.size(); i++) {
                            stmt.bindString(i + 1, tokens.get(i));
                        }
                        stmt.execute();
                    }
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle error
        }
    }

    private List<String> parseCsvLineSimple(String line) {
        String[] parts = line.split(",", 5);
        return new ArrayList<>(Arrays.asList(parts));
    }

    private List<String> parseCsvLineComplex(String line, BufferedReader reader) throws IOException {
        List<String> parts = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean inQuotes = false;

        for (char ch : line.toCharArray()) {
            if (ch == '\"') {
                // Toggle the inQuotes flag
                inQuotes = !inQuotes;
            } else if (ch == ',' && !inQuotes) {
                // Add the token to the list when a comma is encountered outside of quotes
                parts.add(currentToken.toString().trim());
                currentToken.setLength(0); // Reset the token
            } else {
                // Append the character to the current token
                currentToken.append(ch);
            }
        }

        // If the end of the line is reached and we're still inside quotes, continue reading the next lines
        while (inQuotes) {
            String nextLine = reader.readLine();
            if (nextLine == null) {
                // End of file reached, break the loop
                break;
            }
            currentToken.append("\n").append(nextLine); // Append the next line with a newline character

            for (char ch : nextLine.toCharArray()) {
                if (ch == '\"') {
                    // Toggle the inQuotes flag
                    inQuotes = !inQuotes;
                    if (!inQuotes) {
                        // End of the quoted text reached
                        break;
                    }
                }
            }
        }

        if (currentToken.length() > 0) {
            // Add the last token to the list
            parts.add(currentToken.toString().trim());
        }

        return parts;
    }

    public String getRandomQuestion() {
        String question = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("questions", new String[]{"content"}, null, null, null, null, "RANDOM()", "1");

        if (cursor != null && cursor.moveToFirst()) {
            question = cursor.getString(cursor.getColumnIndexOrThrow("content"));
            cursor.close();
        }
        db.close();
        return question;
    }

    public String getQuestionDetails(String questionId, String detailName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String detailValue = null;

        try {
            cursor = db.query("questions", new String[]{detailName}, "id = ?", new String[]{questionId}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                detailValue = cursor.getString(cursor.getColumnIndexOrThrow(detailName));
            }
        } catch (Exception e) {
            // Handle any exceptions
            Log.e("DatabaseHelper", "Error while getting question details", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return detailValue;
    }

    public String getQuestionIdByContent(String questionContent) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String questionId = null;

        try {
            cursor = db.query("questions", new String[]{"id"}, "content = ?", new String[]{questionContent}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                questionId = cursor.getString(cursor.getColumnIndexOrThrow("id"));
            }
        } catch (Exception e) {
            // Handle any exceptions
            Log.e("DatabaseHelper", "Error while getting question ID", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return questionId;
    }
}
