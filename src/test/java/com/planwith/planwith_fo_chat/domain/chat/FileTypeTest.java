package com.planwith.planwith_fo_chat.domain.chat;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FileTypeTest {

	@Test
	void infersFromContentTypeAndName() {
		assertThat(FileType.from("image/png", "a.bin")).isEqualTo(FileType.IMAGE);
		assertThat(FileType.from("application/octet-stream", "photo.jpg")).isEqualTo(FileType.IMAGE);
		assertThat(FileType.from("video/mp4", "clip.mp4")).isEqualTo(FileType.VIDEO);
		assertThat(FileType.from("audio/mpeg", "voice.mp3")).isEqualTo(FileType.AUDIO);
		assertThat(FileType.from("application/pdf", "guide.pdf")).isEqualTo(FileType.DOCUMENT);
		assertThat(FileType.from("application/zip", "archive.zip")).isEqualTo(FileType.ETC);
	}
}
