package com.sekhmet.sekhmetapi.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * A Message.
 */
@Entity
@Table(name = "message")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "message")
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, unique = true)
    @Field(type = FieldType.Keyword)
    private UUID id;

    @Column(name = "text")
    @Field(type = FieldType.Keyword)
    private String text;

    @Column(name = "created_at")
    @Field(type = FieldType.Keyword)
    private LocalDateTime createdAt;

    @Column(name = "image")
    @Field(type = FieldType.Keyword)
    private String image;

    @Column(name = "video")
    @Field(type = FieldType.Keyword)
    private String video;

    @Column(name = "audio")
    @Field(type = FieldType.Keyword)
    private String audio;

    @Column(name = "system")
    @Field(type = FieldType.Object, enabled = false)
    private Boolean system;

    @Column(name = "sent")
    @Field(type = FieldType.Object, enabled = false)
    private Boolean sent;

    @Column(name = "received")
    @Field(type = FieldType.Object, enabled = false)
    private Boolean received;

    @Column(name = "pending")
    @Field(type = FieldType.Object, enabled = false)
    private Boolean pending;

    @ManyToOne
    @JsonIgnoreProperties(value = { "members", "messsages" }, allowSetters = true)
    private Chat chat;

    @OneToOne
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public UUID getId() {
        return this.id;
    }

    public Message id(UUID id) {
        this.setId(id);
        return this;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getText() {
        return this.text;
    }

    public Message text(String text) {
        this.setText(text);
        return this;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public Message createdAt(LocalDateTime createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getImage() {
        return this.image;
    }

    public Message image(String image) {
        this.setImage(image);
        return this;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getVideo() {
        return this.video;
    }

    public Message video(String video) {
        this.setVideo(video);
        return this;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getAudio() {
        return this.audio;
    }

    public Message audio(String audio) {
        this.setAudio(audio);
        return this;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public Boolean getSystem() {
        return this.system;
    }

    public Message system(Boolean system) {
        this.setSystem(system);
        return this;
    }

    public void setSystem(Boolean system) {
        this.system = system;
    }

    public Boolean getSent() {
        return this.sent;
    }

    public Message sent(Boolean sent) {
        this.setSent(sent);
        return this;
    }

    public void setSent(Boolean sent) {
        this.sent = sent;
    }

    public Boolean getReceived() {
        return this.received;
    }

    public Message received(Boolean received) {
        this.setReceived(received);
        return this;
    }

    public void setReceived(Boolean received) {
        this.received = received;
    }

    public Boolean getPending() {
        return this.pending;
    }

    public Message pending(Boolean pending) {
        this.setPending(pending);
        return this;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setPending(Boolean pending) {
        this.pending = pending;
    }

    public Chat getChat() {
        return this.chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public Message chat(Chat chat) {
        this.setChat(chat);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Message)) {
            return false;
        }
        return id != null && id.equals(((Message) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Message{" +
            "id=" + getId() +
            ", text='" + getText() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", image='" + getImage() + "'" +
            ", video='" + getVideo() + "'" +
            ", audio='" + getAudio() + "'" +
            ", system='" + getSystem() + "'" +
            ", sent='" + getSent() + "'" +
            ", received='" + getReceived() + "'" +
            ", pending='" + getPending() + "'" +
            ", user='" + getUser() + "'" +
            "}";
    }
}
