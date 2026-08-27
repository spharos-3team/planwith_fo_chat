package com.planwith.planwith_fo_chat.domain.chat;

import java.util.Locale;

public enum FileType {
	IMAGE,
	VIDEO,
	AUDIO,
	DOCUMENT,
	ETC;

	public static FileType from(String contentType, String fileName) {
		String type = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT).trim();
		String name = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT).trim();
		if (type.startsWith("image/") || hasExtension(name, ".png", ".jpg", ".jpeg", ".webp", ".gif", ".bmp")) {
			return IMAGE;
		}
		if (type.startsWith("video/") || hasExtension(name, ".mp4", ".webm", ".mov", ".mkv")) {
			return VIDEO;
		}
		if (type.startsWith("audio/") || hasExtension(name, ".mp3", ".wav", ".aac", ".m4a", ".ogg")) {
			return AUDIO;
		}
		if (type.equals("application/pdf")
				|| type.contains("msword")
				|| type.contains("officedocument")
				|| type.contains("ms-excel")
				|| type.contains("ms-powerpoint")
				|| type.equals("text/plain")
				|| type.equals("text/csv")
				|| hasExtension(name, ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt", ".csv", ".hwp")) {
			return DOCUMENT;
		}
		return ETC;
	}

	private static boolean hasExtension(String fileName, String... extensions) {
		for (String extension : extensions) {
			if (fileName.endsWith(extension)) {
				return true;
			}
		}
		return false;
	}
}
