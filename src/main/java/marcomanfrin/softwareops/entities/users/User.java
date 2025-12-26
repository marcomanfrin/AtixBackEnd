package marcomanfrin.softwareops.entities.users;

import jakarta.persistence.*;
import marcomanfrin.softwareops.entities.WorkAssignment;
import marcomanfrin.softwareops.entities.WorksiteReferenceAssignment;
import marcomanfrin.softwareops.enums.UserRole;
import marcomanfrin.softwareops.enums.UserType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="user_type")
public abstract class User implements UserDetails {

    // User ha senso come SINGLE_TABLE perché le sottoclassi condividono quasi tutti i campi
    // e si distinguono solo per ruolo/permessi: separarle in più tabelle introdurrebbe join
    // inutili senza benefici reali.
    //Inoltre le query su utenti sono frequenti e polimorfiche (login, lista utenti),
    // e SINGLE_TABLE è la strategia più semplice e performante in questo scenario.

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "profile_image_url")
    private String profileImageUrl;


    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkAssignment> workAssignments = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorksiteReferenceAssignment> worksiteReferenceAssignments = new ArrayList<>();

    public User() {}

    public User(String email, String passwordHash, String firstName, String lastName, UserRole role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    // UserDetails

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserRole getRole() {
        return role;
    }
    public void setRole(UserRole role) {
        this.role = role;
    }

    public List<WorkAssignment> getWorkAssignments() {
        return workAssignments;
    }
    public void setWorkAssignments(List<WorkAssignment> workAssignments) {
        this.workAssignments = workAssignments;
    }

    public List<WorksiteReferenceAssignment> getWorksiteReferenceAssignments() {
        return worksiteReferenceAssignments;
    }
    public void setWorksiteReferenceAssignments(List<WorksiteReferenceAssignment> worksiteReferenceAssignments) {
        this.worksiteReferenceAssignments = worksiteReferenceAssignments;
    }

    public UserType getUserType() {
        if (this instanceof AdministrativeUser) {
            return UserType.ADMINISTRATION;
        } else if (this instanceof TechnicianUser) {
            return UserType.TECHNICIAN;
        } else if (this instanceof SellerUser) {
            return UserType.SELLER;
        }
        throw new IllegalStateException("Unknown user type");
    }
}