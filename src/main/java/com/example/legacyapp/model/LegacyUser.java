package com.example.legacyapp.model;

import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.QueryHint;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "legacy_users",
    indexes = {
        @Index(columnList = "username", unique = true),
        @Index(columnList = "email", unique = true)
    }
)
// Deprecated: Using legacy cache annotations
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "userCache")
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
// Deprecated: Using @Where for soft deletes (better to use @SQLDelete and @Where together)
@Where(clause = "deleted = false")
// Deprecated: Using @Filter (API changes in Hibernate 6)
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = "string"))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
// Deprecated: Using @NamedQuery with HQL (should migrate to @NamedQuery with JPQL)
@javax.persistence.NamedQueries({
    @javax.persistence.NamedQuery(
        name = "LegacyUser.findActive",
        query = "FROM LegacyUser WHERE active = true",
        hints = {
            @QueryHint(name = "org.hibernate.cacheable", value = "true"),
            @QueryHint(name = "org.hibernate.cacheRegion", value = "queryCache")
        }
    ),
    @javax.persistence.NamedQuery(
        name = "LegacyUser.deleteInactive",
        query = "DELETE FROM LegacyUser WHERE active = false AND lastLogin < :date"
    )
})
// Deprecated: Using @NamedNativeQuery with old-style result mapping
@javax.persistence.NamedNativeQueries({
    @javax.persistence.NamedNativeQuery(
        name = "LegacyUser.findBySQL",
        query = "SELECT * FROM legacy_users WHERE YEAR(created_at) = ?1",
        resultClass = LegacyUser.class
    )
})
@SqlResultSetMapping(
    name = "UserStatsMapping",
    columns = {
        @ColumnResult(name = "username", type = String.class),
        @ColumnResult(name = "login_count", type = Long.class)
    }
)
// Deprecated: Using @Loader and @SQLInsert (Hibernate specific)
@Loader(namedQuery = "customUserLoader")
@SQLInsert(sql = "INSERT INTO legacy_users (username, email, password, created_at, id) VALUES (?, ?, ?, ?, ?)")
@SQLUpdate(sql = "UPDATE legacy_users SET username=?, email=?, password=?, updated_at=NOW() WHERE id=?")
@SQLDelete(sql = "UPDATE legacy_users SET deleted=true WHERE id=?")
public class LegacyUser implements Serializable {

    @Id
    // Deprecated: Using AUTO generation strategy (should be explicit)
    @GeneratedValue(strategy = GenerationType.AUTO)
    // Deprecated: Using @GenericGenerator (Hibernate specific)
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    @Column(unique = true)
    // Deprecated: Using @NaturalId (API changes in Hibernate 6)
    @NaturalId(mutable = false)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(unique = true)
    private String email;

    // Deprecated: Using @Formula for computed columns
    @Formula("(SELECT COUNT(*) FROM user_logins ul WHERE ul.user_id = id)")
    private Integer loginCount;

    @NotBlank(message = "Password is required")
    @Size(min = 6)
    // Deprecated: Using @ColumnTransformer (Hibernate specific)
    @ColumnTransformer(
        read = "AES_DECRYPT(password, 'secret-key')",
        write = "AES_ENCRYPT(?, 'secret-key')"
    )
    private String password;

    // Deprecated: Using Date instead of LocalDateTime
    @Temporal(TemporalType.TIMESTAMP)
    // Deprecated: Using @Generated (Hibernate specific)
    @Generated(GenerationTime.INSERT)
    @Column(insertable = false, updatable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Generated(GenerationTime.ALWAYS)
    @Column(insertable = false, updatable = false)
    private Date updatedAt;

    // Deprecated: Using @Type annotation (removed in Hibernate 6)
    @Type(type = "yes_no")
    private boolean active = true;

    // Deprecated: Using @LazyCollection instead of JPA's fetch attribute
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.EXTRA)
    // Deprecated: Using @Fetch (Hibernate specific)
    @Fetch(FetchMode.SUBSELECT)
    // Deprecated: Using @BatchSize (Hibernate specific)
    @BatchSize(size = 10)
    // Deprecated: Using @OnDelete (Hibernate specific)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Role> roles = new ArrayList<>();

    // Deprecated: Using @Any mapping (Hibernate specific, complex to migrate)
    @Any(metaColumn = @Column(name = "property_type"))
    @AnyMetaDef(
        idType = "long",
        metaType = "string",
        metaValues = {
            @MetaValue(value = "S", targetEntity = StringProperty.class),
            @MetaValue(value = "I", targetEntity = IntegerProperty.class)
        }
    )
    @JoinColumn(name = "property_id")
    private Property userProperty;

    // Deprecated: Using @CollectionOfElements (old Hibernate annotation)
    @ElementCollection
    @CollectionTable(name = "user_tags", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "tag")
    // Deprecated: Using @OrderBy with SQL fragment
    @javax.persistence.OrderBy("tag ASC")
    private Set<String> tags = new HashSet<>();

    // Deprecated: Using @MapKey with complex mapping
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "user_settings")
    @MapKey(name = "key")
    // Deprecated: Using @MapKeyType (Hibernate specific)
    @MapKeyType(@Type(type = "string"))
    private Map<String, Setting> settings = new HashMap<>();

    // Deprecated: Using @AccessType (changed in JPA 2.1+)
    @Access(javax.persistence.AccessType.PROPERTY)
    private String computedField;

    // Deprecated: Using @Immutable (Hibernate specific)
    // Note: @Immutable removed - was causing startup error (only allowed at entity level)
    @Column(name = "tenant_id")
    private String tenantId;

    // Deprecated: Using @DynamicUpdate (Hibernate specific)
    private Date lastLogin;

    // Deprecated: Using @SelectBeforeUpdate (Hibernate specific)
    private boolean deleted = false;

    // Deprecated: Lifecycle callbacks with throws Throwable
    @PrePersist
    protected void onCreate() throws Throwable {
        if (createdAt == null) {
            createdAt = new Date();
        }
        updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() throws Throwable {
        updatedAt = new Date();
    }

    // Deprecated: Using finalize() method
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    // Property interface for @Any mapping
    public interface Property {
        Long getId();
        String getValue();
    }

    // Implementation classes for @Any mapping
    @Entity
    public static class StringProperty implements Property {
        @Id @GeneratedValue
        private Long id;
        private String value;

        public Long getId() { return id; }
        public String getValue() { return value; }
    }

    @Entity
    public static class IntegerProperty implements Property {
        @Id @GeneratedValue
        private Long id;
        private Integer value;

        public Long getId() { return id; }
        public String getValue() { return String.valueOf(value); }
    }

    @Entity
    public static class Role {
        @Id @GeneratedValue
        private Long id;
        private String name;

        @ManyToOne
        @JoinColumn(name = "user_id")
        private LegacyUser user;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public LegacyUser getUser() { return user; }
        public void setUser(LegacyUser user) { this.user = user; }
    }

    @Entity
    public static class Setting {
        @Id @GeneratedValue
        private Long id;
        private String key;
        private String value;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    // Standard getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public List<Role> getRoles() { return roles; }
    public void setRoles(List<Role> roles) { this.roles = roles; }
    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) { this.tags = tags; }
    public Map<String, Setting> getSettings() { return settings; }
    public void setSettings(Map<String, Setting> settings) { this.settings = settings; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public Date getLastLogin() { return lastLogin; }
    public void setLastLogin(Date lastLogin) { this.lastLogin = lastLogin; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    public Property getUserProperty() { return userProperty; }
    public void setUserProperty(Property userProperty) { this.userProperty = userProperty; }
    public Integer getLoginCount() { return loginCount; }
    public void setLoginCount(Integer loginCount) { this.loginCount = loginCount; }

    @Access(javax.persistence.AccessType.PROPERTY)
    public String getComputedField() {
        return username + "@" + tenantId;
    }

    public void setComputedField(String computedField) {
        this.computedField = computedField;
    }
}