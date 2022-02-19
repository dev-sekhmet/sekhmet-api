package com.sekhmet.sekhmetapi.service.dto;

import com.sekhmet.sekhmetapi.domain.User;
import java.util.UUID;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * A DTO representing a user, with only the public attributes.
 */
@Getter
@Setter
public class UserDTO {

    private UUID id;

    private String login;

    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    @Email
    @Size(min = 5, max = 254)
    private String email;

    @Size(max = 256)
    private String imageUrl;

    private boolean activated = false;

    @Size(min = 2, max = 10)
    private String langKey;

    private String phoneNumber;

    public UserDTO() {
        // Empty constructor needed for Jackson.
    }

    public UserDTO(User user) {
        this.id = user.getId();
        // Customize it here if you need, or not, firstName/lastName/etc
        this.login = user.getLogin();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.activated = user.isActivated();
        this.email = user.getEmail();
        this.imageUrl = user.getImageUrl();
        this.phoneNumber = user.getPhoneNumber();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "UserDTO{" +
            "id='" + id + '\'' +
            ", login='" + login + '\'' +
            "}";
    }
}
