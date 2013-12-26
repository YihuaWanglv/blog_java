package fr.luya.blog.security;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import fr.luya.blog.document.Auteur;
import fr.luya.blog.service.AuteurService;

public class LocalAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    AuteurService auteurService;

    @Override
    protected void additionalAuthenticationChecks(UserDetails arg0, UsernamePasswordAuthenticationToken arg1)
            throws AuthenticationException {

    }

    @Override
    protected UserDetails retrieveUser(final String username, final UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {

        final String password = (String) authentication.getCredentials();

        if (!StringUtils.hasText(password)) {
            logger.warn("Username {}: no password provided", username);
            throw new BadCredentialsException("Please enter password");
        }

        final Auteur auteur = auteurService.findByEmail(username);
        if (auteur == null) {
            logger.warn("Username {} : username not found", username);
            throw new BadCredentialsException("Invalid Username");
        }
        if (!auteur.getPassword().equals(password)) {
            logger.warn("Password {} : password invalid", username);
            throw new BadCredentialsException("Invalid password");
        }

        final List<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
        if (auteur.isAdmin()) {
            auths.add(new GrantedAuthorityImpl("ROLE_ADMIN"));
        }
        auths.add(new GrantedAuthorityImpl("ROLE_USER"));

        return new User(auteur.getId(), "", true, true, true, true, auths);
    }

}
