package com.sekhmet.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.sekhmet.api.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ChatMemberTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ChatMember.class);
        ChatMember chatMember1 = new ChatMember();
        chatMember1.setId(1L);
        ChatMember chatMember2 = new ChatMember();
        chatMember2.setId(chatMember1.getId());
        assertThat(chatMember1).isEqualTo(chatMember2);
        chatMember2.setId(2L);
        assertThat(chatMember1).isNotEqualTo(chatMember2);
        chatMember1.setId(null);
        assertThat(chatMember1).isNotEqualTo(chatMember2);
    }
}
