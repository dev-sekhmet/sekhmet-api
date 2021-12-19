package com.sekhmet.sekhmetapi.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.persistence.*;
import javax.validation.constraints.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * A Chat.
 */
@Entity
@Table(name = "chat")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "chat")
public class Chat implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, unique = true)
    @Field(type = FieldType.Keyword)
    private UUID id;

    @Column(name = "icon")
    @Field(type = FieldType.Keyword)
    private String icon;

    @Column(name = "name")
    @Field(type = FieldType.Keyword)
    private String name;

    @OneToMany(mappedBy = "chat")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "chat" }, allowSetters = true)
    private Set<ChatMember> members = new HashSet<>();

    @OneToMany(mappedBy = "chat")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "chat" }, allowSetters = true)
    private Set<Message> messsages = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public UUID getId() {
        return this.id;
    }

    public Chat id(UUID id) {
        this.setId(id);
        return this;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getIcon() {
        return this.icon;
    }

    public Chat icon(String icon) {
        this.setIcon(icon);
        return this;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return this.name;
    }

    public Chat name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<ChatMember> getMembers() {
        return this.members;
    }

    public void setMembers(Set<ChatMember> chatMembers) {
        if (this.members != null) {
            this.members.forEach(i -> i.setChat(null));
        }
        if (chatMembers != null) {
            chatMembers.forEach(i -> i.setChat(this));
        }
        this.members = chatMembers;
    }

    public Chat members(Set<ChatMember> chatMembers) {
        this.setMembers(chatMembers);
        return this;
    }

    public Chat addMembers(ChatMember chatMember) {
        this.members.add(chatMember);
        chatMember.setChat(this);
        return this;
    }

    public Chat removeMembers(ChatMember chatMember) {
        this.members.remove(chatMember);
        chatMember.setChat(null);
        return this;
    }

    public Set<Message> getMesssages() {
        return this.messsages;
    }

    public void setMesssages(Set<Message> messages) {
        if (this.messsages != null) {
            this.messsages.forEach(i -> i.setChat(null));
        }
        if (messages != null) {
            messages.forEach(i -> i.setChat(this));
        }
        this.messsages = messages;
    }

    public Chat messsages(Set<Message> messages) {
        this.setMesssages(messages);
        return this;
    }

    public Chat addMesssages(Message message) {
        this.messsages.add(message);
        message.setChat(this);
        return this;
    }

    public Chat removeMesssages(Message message) {
        this.messsages.remove(message);
        message.setChat(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Chat)) {
            return false;
        }
        return id != null && id.equals(((Chat) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Chat{" +
            "id=" + getId() +
            ", icon='" + getIcon() + "'" +
            ", name='" + getName() + "'" +
            "}";
    }
}
