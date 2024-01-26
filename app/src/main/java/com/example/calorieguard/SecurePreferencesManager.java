package com.example.calorieguard;

import android.content.Context;
import android.content.SharedPreferences;

public class SecurePreferencesManager {
    public static void saveCredentials(Context context, String email, String password) {
            final String XOR_KEY = context.getString(R.string.XOR_Key);
        try {
            String encryptedEmail = encryptString(email,XOR_KEY);
            String encryptedPassword = encryptString(password,XOR_KEY);

            SharedPreferences preferences = context.getSharedPreferences("EncryptedPass", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("encryptedEmail", encryptedEmail);
            editor.putString("encryptedPassword", encryptedPassword);
            editor.apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean checkCredentials(Context context, String email, String password) {
        final String XOR_KEY = context.getString(R.string.XOR_Key);
        try {
            String encryptedEmail = encryptString(email,XOR_KEY);
            String encryptedPassword = encryptString(password,XOR_KEY);

            SharedPreferences preferences = context.getSharedPreferences("EncryptedPass", Context.MODE_PRIVATE);
            String storedEncryptedEmail = preferences.getString("encryptedEmail", null);
            String storedEncryptedPassword = preferences.getString("encryptedPassword", null);

            return encryptedEmail.equals(storedEncryptedEmail) && encryptedPassword.equals(storedEncryptedPassword);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static String encryptString(String input,String XOR_KEY) {
        StringBuilder encrypted = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            encrypted.append((char) (input.charAt(i) ^ XOR_KEY.charAt(i % XOR_KEY.length())));
        }
        return encrypted.toString();
    }

}
