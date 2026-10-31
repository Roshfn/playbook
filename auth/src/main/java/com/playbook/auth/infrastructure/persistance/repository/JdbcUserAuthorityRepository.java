package com.playbook.auth.infrastructure.persistance.repository;

import com.playbook.auth.domain.repository.UserAuthorityRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Repository
public class JdbcUserAuthorityRepository implements UserAuthorityRepository {

    private final JdbcTemplate jdbc;

    public JdbcUserAuthorityRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public void assignRole(UUID userId, String roleName) {
        jdbc.update("""
                INSERT INTO user_roles (user_id, role_id)
                SELECT ?, id FROM roles WHERE LOWER(name) = LOWER(?)
                ON CONFLICT DO NOTHING
                """, userId, roleName);
    }

    @Override
    public Set<String> findRoleNames(UUID userId) {
        return new HashSet<>(jdbc.queryForList("""
                SELECT r.name FROM roles r
                JOIN user_roles ur ON ur.role_id = r.id
                WHERE ur.user_id = ?
                """, String.class, userId));
    }

    @Override
    public Set<String> findPermissionNames(UUID userId) {
        return new HashSet<>(jdbc.queryForList("""
                SELECT DISTINCT p.name FROM permissions p
                JOIN role_permissions rp ON rp.permission_id = p.id
                JOIN user_roles ur ON ur.role_id = rp.role_id
                WHERE ur.user_id = ?
                """, String.class, userId));
    }
}