package com.sekhmet.sekhmetapi.web.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sekhmet.sekhmetapi.domain.User;
import com.sekhmet.sekhmetapi.security.jwt.JWTFilter;
import com.sekhmet.sekhmetapi.security.jwt.TokenProvider;
import com.sekhmet.sekhmetapi.service.SmsService;
import com.sekhmet.sekhmetapi.service.UserService;
import com.sekhmet.sekhmetapi.service.dto.sms.CheckPhoneVerificationRequest;
import com.sekhmet.sekhmetapi.service.dto.sms.StartPhoneVerificationRequest;
import com.sekhmet.sekhmetapi.service.dto.sms.VerificationStatus;
import com.sekhmet.sekhmetapi.web.rest.errors.BadRequestAlertException;
import com.sekhmet.sekhmetapi.web.rest.vm.LoginVM;
import java.util.Optional;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller to authenticate users.
 */
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class UserJWTController {

    private static final String ENTITY_NAME = "user";
    private final TokenProvider tokenProvider;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SmsService smsService;
    private final UserService userService;

    @PostMapping("/authenticate")
    public ResponseEntity<JWTToken> authorize(@Valid @RequestBody LoginVM loginVM) {
        String jwt = createToken(loginVM.getUsername(), loginVM.getPassword(), loginVM.isRememberMe());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
        return new ResponseEntity<>(new JWTToken(jwt), httpHeaders, HttpStatus.OK);
    }

    @GetMapping("/login")
    public ResponseEntity<VerificationStatus> authenticate(StartPhoneVerificationRequest request) {
        // for GOOGLE and APPLE verification
        if (request.getPhoneNumber().startsWith("+23799999")) {
            return ResponseEntity.ok(VerificationStatus.PENDING);
        }

        VerificationStatus status = smsService.sendVerificationCode(request);
        if (status == null) {
            throw new BadRequestAlertException("An error occur during Verification Code Send", ENTITY_NAME, "errorSendVerificationCode");
        }
        if (status == VerificationStatus.CANCELED) {
            throw new BadRequestAlertException("An error occurred request canceled", ENTITY_NAME, "errorSendVerificationCodeCanceled");
        }
        return ResponseEntity.ok(status);
    }

    /**
     * Login or signup via phone number
     *
     * @param request
     * @return
     */
    @GetMapping("/verify")
    public ResponseEntity<JWTToken> verify(CheckPhoneVerificationRequest request) {
        // for GOOGLE and APPLE verification
        if (request.getPhoneNumber().startsWith("+23799999") && request.getToken().startsWith("9999")) {
            Optional<User> userOptional = userService.getUserByPhoneNumber(request.getPhoneNumber());
            User user = userOptional.orElseGet(() -> userService.registerUserByPhoneNumber(request));
            String phoneLogin = userService.buildPhoneLogin(request);
            String password = userService.buildPhoneLoginPassword(phoneLogin);

            String jwt = createToken(phoneLogin, password, true);
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
            return new ResponseEntity<>(new JWTToken(jwt), httpHeaders, HttpStatus.OK);
        }

        VerificationStatus status = smsService.checkVerificationCode(request);
        if (status == null) {
            throw new BadRequestAlertException("An error occur during Verification Code Send", ENTITY_NAME, "errorCheckVerificationCode");
        }
        if (status == VerificationStatus.CANCELED) {
            throw new BadRequestAlertException("An error occurred request canceled", ENTITY_NAME, "errorCheckVerificationCodeCanceled");
        }
        if (status == VerificationStatus.PENDING) {
            throw new BadRequestAlertException(
                "An error occurred request pending (wrong code)",
                ENTITY_NAME,
                "errorCheckVerificationIncorrectCode"
            );
        }

        Optional<User> userOptional = userService.getUserByPhoneNumber(request.getPhoneNumber());
        User user = userOptional.orElseGet(() -> userService.registerUserByPhoneNumber(request));
        String phoneLogin = userService.buildPhoneLogin(request);
        String password = userService.buildPhoneLoginPassword(phoneLogin);

        String jwt = createToken(phoneLogin, password, true);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
        return new ResponseEntity<>(new JWTToken(jwt), httpHeaders, HttpStatus.OK);
    }

    private String createToken(String login, String password, boolean rememberMe) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(login, password);
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return tokenProvider.createToken(authentication, rememberMe);
    }

    /**
     * Object to return as body in JWT Authentication.
     */
    static class JWTToken {

        private String idToken;

        JWTToken(String idToken) {
            this.idToken = idToken;
        }

        @JsonProperty("id_token")
        String getIdToken() {
            return idToken;
        }

        void setIdToken(String idToken) {
            this.idToken = idToken;
        }
    }
}
