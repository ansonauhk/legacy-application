package com.example.legacyapp.repository;

import com.example.legacyapp.model.User;
import org.hibernate.*;
import org.hibernate.criterion.*;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

@Repository
@SuppressWarnings("deprecation")
public class CustomUserRepositoryImpl {

    @PersistenceContext
    private EntityManager entityManager;

    // Deprecated: Using Hibernate Session and old Criteria API
    public List<User> findUsersUsingCriteria(String username, String email) {
        Session session = entityManager.unwrap(Session.class);
        Criteria criteria = session.createCriteria(User.class);

        if (username != null) {
            criteria.add(Restrictions.like("username", username, MatchMode.ANYWHERE));
        }
        if (email != null) {
            criteria.add(Restrictions.eq("email", email));
        }

        criteria.addOrder(Order.desc("createdAt"));
        criteria.setMaxResults(10);
        criteria.setCacheable(true);

        return criteria.list();
    }

    // Deprecated: Using DetachedCriteria
    public List<User> findWithDetachedCriteria() {
        Session session = entityManager.unwrap(Session.class);

        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(User.class)
            .add(Restrictions.eq("active", true))
            .addOrder(org.hibernate.criterion.Order.asc("username"));

        Criteria criteria = detachedCriteria.getExecutableCriteria(session);
        return criteria.list();
    }

    // Deprecated: Using Projections with old Criteria API
    public Long countActiveUsers() {
        Session session = entityManager.unwrap(Session.class);
        Criteria criteria = session.createCriteria(User.class);
        criteria.add(Restrictions.eq("active", true));
        criteria.setProjection(Projections.rowCount());
        return (Long) criteria.uniqueResult();
    }

    // Deprecated: Using ScrollableResults (changed in Hibernate 6)
    public void processLargeResultSet() {
        Session session = entityManager.unwrap(Session.class);
        org.hibernate.query.Query query = session.createQuery("FROM User");
        query.setFetchSize(50);

        ScrollableResults results = ((org.hibernate.query.Query) query).scroll(ScrollMode.FORWARD_ONLY);
        while (results.next()) {
            User user = (User) results.get(0);
            // Process user
            if (results.getRowNumber() % 100 == 0) {
                session.flush();
                session.clear();
            }
        }
        results.close();
    }

    // Deprecated: Using SQLQuery with addScalar
    public List<Map<String, Object>> getUserStatistics() {
        Session session = entityManager.unwrap(Session.class);
        SQLQuery query = session.createSQLQuery(
            "SELECT username, COUNT(*) as login_count FROM user_logins GROUP BY username"
        );

        query.addScalar("username", StandardBasicTypes.STRING);
        query.addScalar("login_count", StandardBasicTypes.LONG);
        query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

        return query.list();
    }

    // Deprecated: Using legacy batch processing
    public void batchUpdateUsers(List<User> users) {
        Session session = entityManager.unwrap(Session.class);
        session.setCacheMode(CacheMode.IGNORE);
        session.setFlushMode(FlushMode.MANUAL);

        int batchSize = 25;
        for (int i = 0; i < users.size(); i++) {
            session.update(users.get(i));
            if (i % batchSize == 0) {
                session.flush();
                session.clear();
            }
        }
        session.flush();
    }

    // Deprecated: Using Hibernate Interceptor pattern
    public void saveWithInterceptor(User user) {
        Session session = entityManager.unwrap(Session.class);
        session.save(user);
        // Old style interceptor usage
    }

    // Deprecated: Using MultiIdentifierLoadAccess (API changed)
    public List<User> loadMultipleUsers(List<Long> ids) {
        Session session = entityManager.unwrap(Session.class);
        MultiIdentifierLoadAccess<User> multiLoadAccess = session.byMultipleIds(User.class);
        return multiLoadAccess.multiLoad(ids);
    }

    // Deprecated: Using legacy lock modes
    public User findAndLock(Long id) {
        Session session = entityManager.unwrap(Session.class);
        return session.get(User.class, id, LockMode.PESSIMISTIC_WRITE);
    }

    // Deprecated: Using Query.iterate() which is removed in Hibernate 6
    public void iterateUsers() {
        Session session = entityManager.unwrap(Session.class);
        Query query = session.createQuery("FROM User WHERE active = true");
        Iterator<User> iterator = ((org.hibernate.query.Query) query).iterate();

        while (iterator.hasNext()) {
            User user = iterator.next();
            // Process user
        }
    }

    // Deprecated: Using Hibernate-specific query hints
    public List<User> findWithQueryHints(String username) {
        Query query = entityManager.createQuery("SELECT u FROM User u WHERE u.username = :username");
        query.setParameter("username", username);
        query.setHint("org.hibernate.cacheable", true);
        query.setHint("org.hibernate.cacheRegion", "userQuery");
        query.setHint("org.hibernate.timeout", 5);
        query.setHint("org.hibernate.fetchSize", 20);
        query.setHint("org.hibernate.flushMode", "MANUAL");

        return query.getResultList();
    }

    // Deprecated: Using legacy natural ID API
    public User findByNaturalId(String email) {
        Session session = entityManager.unwrap(Session.class);
        return session.byNaturalId(User.class)
            .using("email", email)
            .load();
    }

    // Deprecated: Using Filter API (changed in Hibernate 6)
    public List<User> findWithFilter(String tenantId) {
        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
        Query query = session.createQuery("FROM User");
        return query.getResultList();
    }

    // Deprecated: Using ResultTransformer (deprecated in Hibernate 5.2+)
    public List<UserDTO> findUserDTOs() {
        Session session = entityManager.unwrap(Session.class);
        Query query = session.createQuery(
            "SELECT u.id as id, u.username as username, u.email as email FROM User u"
        );
        query.unwrap(org.hibernate.query.Query.class)
            .setResultTransformer(Transformers.aliasToBean(UserDTO.class));
        return query.getResultList();
    }

    // DTO class for transformation
    public static class UserDTO {
        private Long id;
        private String username;
        private String email;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}