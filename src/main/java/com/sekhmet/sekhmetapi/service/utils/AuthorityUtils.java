package com.sekhmet.sekhmetapi.service.utils;

import com.sekhmet.sekhmetapi.domain.Authority;
import com.sekhmet.sekhmetapi.domain.User;
import com.sekhmet.sekhmetapi.security.AuthoritiesConstants;

public class AuthorityUtils {

    public static boolean isAdmin(User user) {
        return hasRole(user, AuthoritiesConstants.ADMIN);
    }

    public static boolean isCoach(User user) {
        return hasRole(user, AuthoritiesConstants.COACH);
    }

    private static boolean hasRole(User user, String coach) {
        Authority authority = new Authority();
        authority.setName(coach);
        return user.getAuthorities().contains(authority);
    }
}
