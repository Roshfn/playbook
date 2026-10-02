package com.playbook.auth.application.service;

import com.playbook.auth.domain.model.*;
import com.playbook.auth.domain.repository.UserIdentityRepository;
import com.playbook.auth.domain.repository.UserRepository;
import com.playbook.auth.exception.AccountNotActiveException;
import org.springframework.stereotype.Service;

@Service
public class UserResolutionService {

    private final UserRepository users;
    private final UserIdentityRepository identities;

    public UserResolutionService(UserRepository users, UserIdentityRepository identities) {
        this.users = users;
        this.identities = identities;
    }

    public User resolve(VerifiedIdentity identity) {
        User user = identity.provider() == AuthProvider.MOBILE_OTP
                ? resolveByPhone(identity)
                : resolveBySocial(identity);

        if (!user.isActive()) throw new AccountNotActiveException();
        return user;
    }

    private User resolveByPhone(VerifiedIdentity id) {
        return users.findByPhone(id.phoneNumber())
                .map(u -> u.phoneVerified() ? u : users.save(u.withPhoneVerified(id.phoneNumber())))
                .orElseGet(() -> users.save(new User(
                        null, null, id.phoneNumber(), null, null,
                        UserStatus.ACTIVE, false, true)));
    }

    private User resolveBySocial(VerifiedIdentity id) {
        return identities.findByProviderAndProviderUserId(id.provider(), id.providerUserId())
                .flatMap(i -> users.findById(i.userId()))
                .orElseGet(() -> linkOrCreate(id));
    }

    private User linkOrCreate(VerifiedIdentity id) {
        // Link to an existing account by email ONLY if the provider verified that email.
        User user = (id.emailVerified() && id.email() != null)
                ? users.findByEmail(id.email()).orElseGet(() -> create(id))
                : create(id);

        identities.save(new UserIdentity(
                null, user.id(), id.provider(), id.providerUserId(),
                id.email(), id.displayName(), id.pictureUrl()));
        return user;
    }

    private User create(VerifiedIdentity id) {
        return users.save(new User(
                null, id.email(), null, id.firstName(), id.lastName(),
                UserStatus.ACTIVE, id.emailVerified(), false));
    }
}