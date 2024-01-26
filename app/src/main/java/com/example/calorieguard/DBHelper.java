package com.example.calorieguard;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "calorie_guard.db";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            // Define your SQLite tables and schema here
            db.execSQL("CREATE TABLE IF NOT EXISTS User (" +
                    "id TEXT PRIMARY KEY," +
                    "name TEXT," +
                    "DPUrl TEXT," +
                    "weight TEXT," +
                    "height TEXT," +
                    "age TEXT," +
                    "sex TEXT," +
                    "city TEXT," +
                    "aimedWeight TEXT," +
                    "plann TEXT," +
                    "activityLevel TEXT," +
                    "curcal TEXT" +
                    ")");

            db.execSQL("CREATE TABLE IF NOT EXISTS Item (" +
                    "id TEXT PRIMARY KEY," +
                    "userId TEXT," +
                    "calories TEXT," +
                    "FOREIGN KEY (userId) REFERENCES User(id)" +
                    ")");
        } catch (Exception e) {
            // Log or handle the exception
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            // Handle database upgrades (if needed) by dropping and recreating tables
            db.execSQL("DROP TABLE IF EXISTS User");
            db.execSQL("DROP TABLE IF EXISTS Item");
            onCreate(db);
        } catch (Exception e) {
            // Log or handle the exception
            e.printStackTrace();
        }
    }

    public boolean doesUserExist(String userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;

        try {
            // Query to check if a user with the given ID exists
            String query = "SELECT * FROM User WHERE id = ?";
            cursor = db.rawQuery(query, new String[]{userId});

            // Check if any results are returned
            return cursor.moveToFirst();
        } catch (Exception e) {
            // Log or handle the exception
            e.printStackTrace();
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void insertUserData(String id, String name,String dpurl, String weight, String height, String age, String city, String aimedWeight, String curcal, String plan,String sex,String activityLevel) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put("id", id);
            values.put("name", name);
            values.put("DpUrl", dpurl);
            values.put("weight", weight);
            values.put("height", height);
            values.put("age", age);
            values.put("sex", sex);
            values.put("city", city);
            values.put("activityLevel", activityLevel);
            values.put("aimedWeight", aimedWeight);
            values.put("plann", plan);
            values.put("curcal", curcal);

            // Insert data into User table
            db.insert("User", null, values);
        } catch (Exception e) {
            // Log or handle the exception
            e.printStackTrace();
        }
    }

    public void insertItemData(String userId, String id, String calories) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put("id", id);
            values.put("userId", userId);
            values.put("calories", calories);

            // Insert data into Item table
            db.insert("Item", null, values);
        } catch (Exception e) {
            // Log or handle the exception
            e.printStackTrace();
        }
    }

    public void deleteItemByUserIdAndItemId(String userId, String itemId) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            // Specify the WHERE clause to identify the item by both userId and itemId
            String whereClause = "userId = ? AND id = ?";
            String[] whereArgs = {userId, itemId};

            // Delete the item from the Item table
            db.delete("Item", whereClause, whereArgs);
        } catch (Exception e) {
            // Log or handle the exception
            e.printStackTrace();
        }
    }

    public boolean doesItemExist(String userId, String itemId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;

        try {
            // Query to check if an item with the given userId and itemId exists
            String query = "SELECT * FROM Item WHERE userId = ? AND id = ?";
            cursor = db.rawQuery(query, new String[]{userId, itemId});

            // Check if any results are returned
            return cursor.moveToFirst();
        } catch (Exception e) {
            // Log or handle the exception
            e.printStackTrace();
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    public HashMap<String, String> getAllItemsByUserId(String userId) {
        HashMap<String, String> itemMap = new HashMap<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;

        try {
            // Query to retrieve all items for the given userId
            String query = "SELECT * FROM Item WHERE userId = ?";
            cursor = db.rawQuery(query, new String[]{userId});

            // Check if the cursor has data and move to the first row
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Check if the column exists in the cursor
                    int idIndex = cursor.getColumnIndex("id");
                    int caloriesIndex = cursor.getColumnIndex("calories");

                    if (idIndex >= 0 && caloriesIndex >= 0) {
                        // Retrieve values from the cursor
                        String itemId = cursor.getString(idIndex);
                        String calories = cursor.getString(caloriesIndex);

                        // Add item details to the HashMap
                        itemMap.put(itemId, calories);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            // Log or handle the exception
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return itemMap;
    }

    public HashMap<String, String> getUserDataById(String userId) {
        HashMap<String, String> userDataMap = new HashMap<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;

        try {
            // Query to retrieve user data for the given userId
            String query = "SELECT * FROM User WHERE id = ?";
            cursor = db.rawQuery(query, new String[]{userId});

            // Check if the cursor has data and move to the first row
            if (cursor != null && cursor.moveToFirst()) {
                // Check if the columns exist in the cursor
                int nameIndex = cursor.getColumnIndex("name");
                int weightIndex = cursor.getColumnIndex("weight");
                int heightIndex = cursor.getColumnIndex("height");
                int ageIndex = cursor.getColumnIndex("age");
                int sexIndex = cursor.getColumnIndex("sex");
                int cityIndex = cursor.getColumnIndex("city");
                int aimedWeightIndex = cursor.getColumnIndex("aimedWeight");
                int activityLevelIndex = cursor.getColumnIndex("activityLevel");
                int planIndex = cursor.getColumnIndex("plann");
                int curcalIndex = cursor.getColumnIndex("curcal");
                int DpUrlIndex = cursor.getColumnIndex("DPUrl");

                if (nameIndex >= 0 && weightIndex >= 0 && heightIndex >= 0 && ageIndex >= 0
                        && cityIndex >= 0 && aimedWeightIndex >= 0 && planIndex >= 0 && curcalIndex >= 0) {
                    // Retrieve values from the cursor
                    String name = cursor.getString(nameIndex);
                    String weight = cursor.getString(weightIndex);
                    String height = cursor.getString(heightIndex);
                    String age = cursor.getString(ageIndex);
                    String sex= cursor.getString(sexIndex);
                    String city = cursor.getString(cityIndex);
                    String aimedWeight = cursor.getString(aimedWeightIndex);
                    String plan = cursor.getString(planIndex);
                    String dpurl=cursor.getString(DpUrlIndex);
                    String activityLevel=cursor.getString(activityLevelIndex);
                    String curcal = cursor.getString(curcalIndex);

                    // Add user data to the HashMap
                    userDataMap.put("name", name);
                    userDataMap.put("weight", weight);
                    userDataMap.put("height", height);
                    userDataMap.put("age", age);
                    userDataMap.put("sex", sex);
                    userDataMap.put("city", city);
                    userDataMap.put("aimedWeight", aimedWeight);
                    userDataMap.put("plan", plan);
                    userDataMap.put("DpUrl", dpurl);
                    userDataMap.put("curcal", curcal);
                    userDataMap.put("activityLevel", activityLevel);
                }
            }
        } catch (Exception e) {
            // Log or handle the exception
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return userDataMap;
    }

    public String getPlanByUserId(String userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;

        try {
            // Query to retrieve the plan for the given userId
            String query = "SELECT plann FROM User WHERE id = ?";
            cursor = db.rawQuery(query, new String[]{userId});

            // Check if the cursor has data and move to the first row
            if (cursor != null && cursor.moveToFirst()) {
                // Check if the column exists in the cursor
                int planIndex = cursor.getColumnIndex("plann");

                if (planIndex >= 0) {
                    // Retrieve the plan value from the cursor
                    return cursor.getString(planIndex);
                }
            }
        } catch (Exception e) {
            // Log or handle the exception
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        // Return an empty string if the plan is not found
        return "";
    }

    public void updatePlanByUserId(String userId, String newPlan) {
        SQLiteDatabase db = getWritableDatabase();

        try {
            // ContentValues to store the new value of the plan
            ContentValues values = new ContentValues();
            values.put("plann", newPlan);

            // Specify the WHERE clause to identify the user by userId
            String whereClause = "id = ?";
            String[] whereArgs = {userId};

            // Update the plan value in the User table
            db.update("User", values, whereClause, whereArgs);
        } catch (Exception e) {
            // Log or handle the exception
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    public String getCurcalByUserId(String userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;

        try {
            // Query to retrieve the curcal value for the given userId
            String query = "SELECT curcal FROM User WHERE id = ?";
            cursor = db.rawQuery(query, new String[]{userId});

            // Check if the cursor has data and move to the first row
            if (cursor != null && cursor.moveToFirst()) {
                // Check if the column exists in the cursor
                int curcalIndex = cursor.getColumnIndex("curcal");

                if (curcalIndex >= 0) {
                    // Retrieve the curcal value from the cursor
                    return cursor.getString(curcalIndex);
                }
            }
        } catch (Exception e) {
            // Log or handle the exception
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        // Return an empty string if curcal is not found
        return "";
    }

    public void updateUserData(String userId, String newName, String newWeight, String newHeight, String newAge, String newSex, String newCity, String newAimedWeight, String newCurcal, String newPlan, String newDpUrl, String newActivityLevel) {
        SQLiteDatabase db = getWritableDatabase();

        try {
            // ContentValues to store the new values
            ContentValues values = new ContentValues();
            values.put("name", newName);
            values.put("weight", newWeight);
            values.put("height", newHeight);
            values.put("age", newAge);
            values.put("sex", newSex);
            values.put("city", newCity);
            values.put("aimedWeight", newAimedWeight);
            values.put("curcal", newCurcal);
            values.put("plann", newPlan);
            values.put("DpUrl", newDpUrl);
            values.put("activityLevel", newActivityLevel);

            // Specify the WHERE clause to identify the user by userId
            String whereClause = "id = ?";
            String[] whereArgs = {userId};

            // Update the user data in the User table
            db.update("User", values, whereClause, whereArgs);
        } catch (Exception e) {
            // Log or handle the exception
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    public void dropItemsTable() {
        try (SQLiteDatabase db = getWritableDatabase()) {
            // Drop the Items table
            db.execSQL("DROP TABLE IF EXISTS Item");

            // Recreate the Items table
            db.execSQL("CREATE TABLE IF NOT EXISTS Item (" +
                    "id TEXT PRIMARY KEY," +
                    "userId TEXT," +
                    "calories TEXT," +
                    "FOREIGN KEY (userId) REFERENCES User(id)" +
                    ")");
        } catch (Exception e) {
            // Log or handle the exception
            e.printStackTrace();
        }
    }

}
