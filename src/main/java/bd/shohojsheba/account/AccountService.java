package bd.shohojsheba.account;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService implements UserDetailsService {
    private final AppUserRepository users;
    private final PasswordEncoder passwords;

    public AccountService(AppUserRepository users, PasswordEncoder passwords) {
        this.users = users;
        this.passwords = passwords;
    }

    @Transactional
    public void register(String displayName, String email, String password, String confirmation) {
        String name = displayName == null ? "" : displayName.strip();
        String normalizedEmail = normalizeEmail(email);
        if (name.length() < 2 || name.length() > 100
                || name.codePoints().anyMatch(Character::isISOControl)) {
            throw new RegistrationException("নাম ২ থেকে ১০০ অক্ষরের মধ্যে লিখুন।",
                    "Enter a name between 2 and 100 characters.");
        }
        if (normalizedEmail.isEmpty() || normalizedEmail.length() > 254
                || !normalizedEmail.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new RegistrationException("সঠিক ইমেইল ঠিকানা লিখুন।",
                    "Enter a valid email address.");
        }
        if (password == null || password.length() < 12
                || password.getBytes(StandardCharsets.UTF_8).length > 72
                || password.codePoints().anyMatch(Character::isISOControl)) {
            throw new RegistrationException("পাসওয়ার্ড কমপক্ষে ১২ অক্ষরের এবং সর্বোচ্চ ৭২ বাইটের হতে হবে।",
                    "Use a password of at least 12 characters and at most 72 bytes.");
        }
        if (!password.equals(confirmation)) {
            throw new RegistrationException("পাসওয়ার্ড দুটি মিলছে না।",
                    "The passwords do not match.");
        }
        if (users.findByEmail(normalizedEmail).isPresent()) {
            throw new RegistrationException("এই ইমেইল দিয়ে ইতিমধ্যে অ্যাকাউন্ট আছে।",
                    "An account with this email already exists.");
        }

        try {
            users.saveAndFlush(new AppUser(UUID.randomUUID().toString(), normalizedEmail, name,
                    passwords.encode(password), Instant.now()));
        } catch (DataIntegrityViolationException exception) {
            // The unique database constraint also handles concurrent registration attempts.
            throw new RegistrationException("এই ইমেইল দিয়ে ইতিমধ্যে অ্যাকাউন্ট আছে।",
                    "An account with this email already exists.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return users.findByEmail(normalizeEmail(email))
                .map(AccountPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found"));
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.strip().toLowerCase(Locale.ROOT);
    }
}
