package com.sekhmet.api.domain;

import com.sekhmet.api.domain.enumeration.ChatMemberScope;
import java.io.Serializable;
import java.util.UUID;
import javax.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A ChatMember.
 */
@Table("chat_member")
public class ChatMember implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @NotNull(message = "must not be null")
    @Column("uid")
    private UUID uid;

    @Column("scope")
    private ChatMemberScope scope;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public ChatMember id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getUid() {
        return this.uid;
    }

    public ChatMember uid(UUID uid) {
        this.setUid(uid);
        return this;
    }

    public void setUid(UUID uid) {
        this.uid = uid;
    }

    public ChatMemberScope getScope() {
        return this.scope;
    }

    public ChatMember scope(ChatMemberScope scope) {
        this.setScope(scope);
        return this;
    }

    public void setScope(ChatMemberScope scope) {
        this.scope = scope;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChatMember)) {
            return false;
        }
        return id != null && id.equals(((ChatMember) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ChatMember{" +
            "id=" + getId() +
            ", uid='" + getUid() + "'" +
            ", scope='" + getScope() + "'" +
            "}";
    }
}
