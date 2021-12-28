package com.sekhmet.sekhmetapi.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sekhmet.sekhmetapi.domain.enumeration.ChatMemberScope;
import java.io.Serializable;
import java.util.UUID;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * A ChatMember.
 */
@Entity
@Table(name = "chat_member")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "chatmember")
public class ChatMember implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, unique = true)
    @Field(type = FieldType.Object, enabled = false)
    private UUID id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false)
    @Field(type = FieldType.Object, enabled = false)
    private ChatMemberScope scope;

    @OneToOne
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne
    @JsonIgnore
    private Chat chat;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public UUID getId() {
        return this.id;
    }

    public ChatMember id(UUID id) {
        this.setId(id);
        return this;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public Chat getChat() {
        return this.chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ChatMember user(User user) {
        this.setUser(user);
        return this;
    }

    public ChatMember chat(Chat chat) {
        this.setChat(chat);
        return this;
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
            ", scope='" + getScope() + "'" +
            "}";
    }
}
