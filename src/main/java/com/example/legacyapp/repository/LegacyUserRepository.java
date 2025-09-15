package com.example.legacyapp.repository;

import com.example.legacyapp.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.QueryHint;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
@Transactional
public interface LegacyUserRepository extends JpaRepository<User, Long> {

    // Deprecated: Using positional parameters instead of named parameters
    @Query("SELECT u FROM User u WHERE u.username = ?1 AND u.email = ?2")
    Optional<User> findByUsernameAndEmailPositional(String username, String email);

    // Deprecated: Using old-style HQL with implicit join
    // Commented out - references Role.userId which doesn't match ManyToMany relationship
    // @Query("SELECT u FROM User u, Role r WHERE u.id = r.userId AND r.name = ?1")
    // List<User> findUsersWithRole(String roleName);

    // Deprecated: Using Hibernate-specific session.get() pattern
    @Query("FROM User WHERE id = :id")
    User getUser(@Param("id") Long id);

    // Deprecated: Using Hibernate's Query.iterate() pattern (removed in Hibernate 6)
    @Query("SELECT u FROM User u WHERE u.createdAt > :date")
    Stream<User> iterateUsersCreatedAfter(@Param("date") Date date);

    // Deprecated: Using old Hibernate Criteria API query hint
    // In Spring Boot 2.x, @QueryHints is not directly supported on repository methods
    // Would need to use EntityManager or @NamedQuery with hints
    @Query("SELECT u FROM User u WHERE u.active = true")
    List<User> findActiveUsersCached();

    // Deprecated: Using DATE function (H2 specific, not portable)
    @Query(value = "SELECT * FROM users WHERE DATE(created_at) = DATE(?1)", nativeQuery = true)
    List<User> findUsersCreatedOnDate(Date date);

    // Deprecated: Using LIMIT in native query (not portable across databases)
    @Query(value = "SELECT * FROM users ORDER BY created_at DESC LIMIT ?1", nativeQuery = true)
    List<User> findRecentUsersWithLimit(int limit);

    // Deprecated: Using Hibernate-specific flush mode
    @Modifying(flushAutomatically = false, clearAutomatically = false)
    @Query("UPDATE User u SET u.lastLogin = :date WHERE u.id = :id")
    void updateLastLogin(@Param("id") Long id, @Param("date") Date date);

    // Deprecated: Using delete with cascade in query (should use entity relationships)
    @Modifying
    @Query("DELETE FROM User u WHERE u.id = :id")
    @Transactional
    void deleteUserAndRelatedData(@Param("id") Long id);

    // Deprecated: Using Hibernate's @Formula in queries
    @Query("SELECT u FROM User u WHERE u.fullName LIKE %:search%")
    List<User> searchByFullName(@Param("search") String search);

    // Deprecated: Using collection parameter with IN clause (syntax changes in Hibernate 6)
    @Query("SELECT u FROM User u WHERE u.id IN ?1")
    List<User> findUsersByIds(List<Long> ids);

    // Deprecated: Using Hibernate-specific temporal functions
    @Query("SELECT u FROM User u WHERE YEAR(u.createdAt) = :year AND MONTH(u.createdAt) = :month")
    List<User> findUsersByYearAndMonth(@Param("year") int year, @Param("month") int month);

    // Deprecated: Using implicit type conversion
    @Query("SELECT u FROM User u WHERE u.status = :status")
    List<User> findByStatusString(@Param("status") String status);

    // Deprecated: Using old-style JOIN FETCH with multiple collections (can cause CartesianProduct)
    // Commented out - can cause MultipleBagFetchException with multiple collections
    // @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles LEFT JOIN FETCH u.permissions")
    // List<User> findAllWithRolesAndPermissions();

    // Deprecated: Using Hibernate-specific @Loader annotation pattern
    @Query(value = "SELECT * FROM users u WHERE u.tenant_id = :tenantId", nativeQuery = true)
    List<User> findByTenantId(@Param("tenantId") String tenantId);

    // Deprecated: Using COUNT(*) instead of COUNT(u)
    @Query("SELECT COUNT(*) FROM User u WHERE u.active = true")
    Long countActiveUsersOldStyle();

    // Deprecated: Using Hibernate's legacy parameter syntax
    @Query("FROM User WHERE username LIKE CONCAT('%', :#{#keyword}, '%')")
    List<User> searchUsersSpEL(@Param("keyword") String keyword);

    // Deprecated: Direct use of EntityManager with Criteria API (old style)
    default List<User> findUsersWithCriteria(String username, String email) {
        return null; // Would need @PersistenceContext EntityManager implementation
    }

    // Deprecated: Using Hibernate Session directly
    default User findByIdUsingSession(Long id) {
        return null; // Would need SessionFactory injection
    }
}