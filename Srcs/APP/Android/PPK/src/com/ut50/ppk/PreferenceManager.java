package com.ut50.ppk;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
	public static void saveFrontId(Context ctx, String front_id)
	{
		SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.shared_preference_file_key), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		
		editor.putString(ctx.getString(R.string.saved_front_id), front_id);
		editor.commit();
	}
	
	public static String loadFrontId(Context ctx)
	{
		SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.shared_preference_file_key), Context.MODE_PRIVATE);

		return sharedPref.getString(ctx.getString(R.string.saved_front_id), "");
	}

	public static void saveNickname(Context ctx, String nickname)
	{
		SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.shared_preference_file_key), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		
		editor.putString(ctx.getString(R.string.saved_nickname), nickname);
		editor.commit();
	}
	
	public static String loadNickname(Context ctx)
	{
		SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.shared_preference_file_key), Context.MODE_PRIVATE);

		return sharedPref.getString(ctx.getString(R.string.saved_nickname), "");
	}

	public static void saveUuid(Context ctx, String uuid)
	{
		SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.shared_preference_file_key), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		
		editor.putString(ctx.getString(R.string.saved_uuid), uuid);
		editor.commit();
	}
	
	public static String loadUuid(Context ctx)
	{
		SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.shared_preference_file_key), Context.MODE_PRIVATE);

		return sharedPref.getString(ctx.getString(R.string.saved_uuid), "");
	}
	
	public static void saveAccessToken(Context ctx, String access_token)
	{
		SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.shared_preference_file_key), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		
		editor.putString(ctx.getString(R.string.saved_access_token), access_token);
		editor.commit();
	}
	
	public static String loadAccessToken(Context ctx)
	{
		SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.shared_preference_file_key), Context.MODE_PRIVATE);

		return sharedPref.getString(ctx.getString(R.string.saved_access_token), "");
	}

	public static void saveUsername(Context ctx, String username)
	{
		SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.shared_preference_file_key), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		
		editor.putString(ctx.getString(R.string.saved_username), username);
		editor.commit();
	}
	
	public static String loadUsername(Context ctx)
	{
		SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.shared_preference_file_key), Context.MODE_PRIVATE);

		return sharedPref.getString(ctx.getString(R.string.saved_username), "");
	}

	public static void savePassword(Context ctx, String password)
	{
		SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.shared_preference_file_key), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		
		editor.putString(ctx.getString(R.string.saved_password), password);
		editor.commit();
	}
	
	public static String loadPassword(Context ctx)
	{
		SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.shared_preference_file_key), Context.MODE_PRIVATE);

		return sharedPref.getString(ctx.getString(R.string.saved_password), "");
	}
}
