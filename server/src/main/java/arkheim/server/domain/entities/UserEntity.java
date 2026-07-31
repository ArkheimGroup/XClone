package arkheim.server.domain.entities;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a registered user
 * */
public class UserEntity {
    private final UUID id;
    private String username;
    private String name;
    private String email;
    private String passwordHash;
    private String biography;
    private LocalDateTime dateOfBirth;
    private String pfpUrl;
    private String bannerUrl;
    private int followerCount;
    private int followingCount;
    private LocalDateTime createdAt;
    private UUID pinnedPostId;
    private boolean isVerified;


    /**
     * Used for registrations of new users
     * @param username the unique username chosen by the user
     * @param name user's display name
     * @param passwordHash already hashed password
     * @param email valid email
     * @param dateOfBirth user's date of birth
     * */
    public UserEntity(String username, String passwordHash, String name, String email, LocalDateTime dateOfBirth) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.name = name;
        this.email = email;
        this.dateOfBirth = dateOfBirth;

        this.id = UUID.randomUUID();
        this.biography = "";
        this.createdAt = LocalDateTime.now();
        this.pfpUrl = "uploads/profile_pictures/default_pfp.png";
        this.bannerUrl = "uploads/banners/default_banner.png";
        this.followerCount = 0;
        this.followingCount = 0;
        this.pinnedPostId = null;
        this.isVerified = false;
    }

    /**
     * Used for already registered users in database
     * */
    public UserEntity(UUID id, String username, String passwordHash, String name, String email, String bioGraphy, LocalDateTime createdAt, String pfpUrl, String bannerUrl, int followerCount, int followingCount, UUID pinnedPostId, LocalDateTime dateOfBirth, boolean isVerified) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.name = name;
        this.email = email;
        this.biography = bioGraphy;
        this.createdAt = createdAt;
        this.pfpUrl = pfpUrl;
        this.bannerUrl = bannerUrl;
        this.followerCount = followerCount;
        this.followingCount = followingCount;
        this.pinnedPostId = pinnedPostId;
        this.dateOfBirth = dateOfBirth;
        this.isVerified = isVerified;
    }

    public UserEntity(UUID id, String username, String passwordHash, String name, String email, String bioGraphy, LocalDateTime createdAt, String pfpUrl, int followerCount, int followingCount, UUID pinnedPostId, LocalDateTime dateOfBirth) {
        this(id, username, passwordHash, name, email, bioGraphy, createdAt, pfpUrl, "uploads/banners/default_banner.png", followerCount, followingCount, pinnedPostId, dateOfBirth, false);
    }



    public UUID getId() {
        return id;
    }

    public LocalDateTime getDateOfBirth() {
        return dateOfBirth;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getBiography() {
        return biography;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getPfpUrl() {
        return pfpUrl;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public UUID getPinnedPostId() {
        return pinnedPostId;
    }
}
