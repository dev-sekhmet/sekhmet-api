package com.sekhmet.api.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;
import javax.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A Message.
 */
@Table("message")
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @NotNull(message = "must not be null")
    @Column("uid")
    private UUID uid;

    @NotNull(message = "must not be null")
    @Column("created_at")
    private LocalDate createdAt;

    @Column("image")
    private String image;

    @Column("video")
    private String video;

    @Column("audio")
    private String audio;

    @Column("system")
    private Boolean system;

    @Column("sent")
    private Boolean sent;

    @Column("received")
    private Boolean received;

    @Column("pending")
    private Boolean pending;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Message id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getUid() {
        return this.uid;
    }

    public Message uid(UUID uid) {
        this.setUid(uid);
        return this;
    }

    public void setUid(UUID uid) {
        this.uid = uid;
    }

    public LocalDate getCreatedAt() {
        return this.createdAt;
    }

    public Message createdAt(LocalDate createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(LocalDate createdAt) {
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

    public void setPending(Boolean pending) {
        this.pending = pending;
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
            ", uid='" + getUid() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", image='" + getImage() + "'" +
            ", video='" + getVideo() + "'" +
            ", audio='" + getAudio() + "'" +
            ", system='" + getSystem() + "'" +
            ", sent='" + getSent() + "'" +
            ", received='" + getReceived() + "'" +
            ", pending='" + getPending() + "'" +
            "}";
    }
}
