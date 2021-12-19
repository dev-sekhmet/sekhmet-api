package com.sekhmet.sekhmetapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.sekhmet.sekhmetapi.web.rest.TestUtil;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ChatMemberTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ChatMember.class);
        ChatMember chatMember1 = new ChatMember();
        chatMember1.setId(UUID.randomUUID());
        ChatMember chatMember2 = new ChatMember();
        chatMember2.setId(chatMember1.getId());
        assertThat(chatMember1).isEqualTo(chatMember2);
        chatMember2.setId(UUID.randomUUID());
        assertThat(chatMember1).isNotEqualTo(chatMember2);
        chatMember1.setId(null);
        assertThat(chatMember1).isNotEqualTo(chatMember2);
    }
}
