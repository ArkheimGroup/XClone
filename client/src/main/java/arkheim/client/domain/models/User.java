package arkheim.client.domain.models;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public class User {

    private final UUID id;
    private String username;
    private String name;
    private String email;
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
     * @param email valid email
     * @param dateOfBirth user's date of birth
     * */
    public User(String username, String name, String email, LocalDateTime dateOfBirth) {
        this.username = username;
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
     * Used for already registered users,
     * */
    public User(UUID id, String username, String name, String email, String biography, LocalDateTime dateOfBirth, String pfpUrl, String bannerUrl, int followerCount, int followingCount, LocalDateTime createdAt, UUID pinnedPostId, boolean isVerified){
        this.id = id;
        this.username = username;
        this.name = name;
        this.email = email;
        this.biography = biography;
        this.dateOfBirth = dateOfBirth;
        this.pfpUrl = pfpUrl;
        this.bannerUrl = bannerUrl;
        this.followerCount = followerCount;
        this.followingCount = followingCount;
        this.createdAt = createdAt;
        this.pinnedPostId = pinnedPostId;
        this.isVerified = isVerified;
    }


    /**
     * @param viewerId viewer Id
     * @return true if the viewer profile is this user's profile
     */
    public boolean isOwnProfile(UUID viewerId){
        return this.id.equals(viewerId);
    }

    /**
     * displays initials
     * avatar initials when pfpUrl fails to load or is default
     * @return "firstname + surname"
     */
    public String getInitials() {
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(2, parts.length); i++) {
            sb.append(Character.toUpperCase(parts[i].charAt(0)));
        }
        return sb.toString();
    }

    /**
     * Used to check whether this user has pinned post at all.
     * UI decides whether to bother rendering the pinned post section.
     * @return true if user has pinned a curtain post
     */
    public boolean hasPinnedPost() {
        return pinnedPostId != null;
    }

    /**
     * @return "@username"
     */
    public String getHandle() {
        return "@" + username;
    }

    /**
     * Used to check whether the currently logged-in viewer follows THIS user.
     * Controls the Follow/Unfollow button.
     * @param viewerFollowingIds the set of user IDs the current viewer follows
     * @return true if the viewer follows this user
     * */
    public boolean isFollowedByViewer(Set<UUID> viewerFollowingIds){
        return viewerFollowingIds.contains(this.id);
    }

    /**
     * Used to check whether THIS user follows the currently logged-in viewer back.
     * Used for "Follows you" badge on their profile.
     * @param viewerId the ID of the currently logged-in user
     * @param thisUsersFollowingIds the set of user IDs that this user follows
     * @return true if this user follows the viewer
     */
    public boolean isFollowingViewer(UUID viewerId, Set<UUID> thisUsersFollowingIds){
        return thisUsersFollowingIds.contains(viewerId);
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        return id.equals(((User) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "UserModel{" +
                "id=" + id +
                ", username='" + username + '\'' +
                '}';
    }

    // Getters

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
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

    public LocalDateTime getDateOfBirth() {
        return dateOfBirth;
    }

    public String getPfpUrl() {
        return pfpUrl;
    }

    public String getBannerUrl() { return bannerUrl; }

    public boolean isVerified() { return isVerified; }

    public int getFollowerCount() {
        return followerCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public UUID getPinnedPostId() {
        return pinnedPostId;
    }
}
