package com.sekhmet.sekhmetapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.sekhmet.sekhmetapi.web.rest.TestUtil;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ChatTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Chat.class);
        Chat chat1 = new Chat();
        chat1.setId(UUID.randomUUID());
        Chat chat2 = new Chat();
        chat2.setId(chat1.getId());
        assertThat(chat1).isEqualTo(chat2);
        chat2.setId(UUID.randomUUID());
        assertThat(chat1).isNotEqualTo(chat2);
        chat1.setId(null);
        assertThat(chat1).isNotEqualTo(chat2);
    }
}
