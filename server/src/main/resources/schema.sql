# This is a template file for database schema; should be run initially.

# --------Users table--------
CREATE TABLE users(
    # Credentials
    id BINARY(16) PRIMARY KEY,
    username VARCHAR(15) NOT NULL UNIQUE ,
    CHECK (CHAR_LENGTH(username) >= 4),
    name VARCHAR(50) NOT NULL,
    email VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255),

    # Personal Information
    biography VARCHAR(160),
    date_of_birth DATE,
    pfp_url TEXT DEFAULT 'uploads/profile_pictures/default_pfp.png',

    # Server side Information
    follower_count BIGINT DEFAULT 0,
    following_count BIGINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    # etc.
    pinned_post_id BINARY(16),

    FOREIGN KEY (pinned_post_id) REFERENCES posts(id)
);

# --------Posts table--------
CREATE TABLE posts(
    id BINARY(16) PRIMARY KEY,
    author_username VARCHAR(15) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    description VARCHAR(280),

    reply_post_id BINARY(16) DEFAULT NULL,
    repost_post_id BINARY(16) DEFAULT NULL,

    FOREIGN KEY (reply_post_id) REFERENCES posts(id),
    FOREIGN KEY (repost_post_id) REFERENCES posts(id),
    FOREIGN KEY (author_username) REFERENCES users(username)
);

# --------Media--------
CREATE TABLE media(
    id BINARY(16) PRIMARY KEY,
    url TEXT NOT NULL,
    width INT NOT NULL,
    height INT NOT NULL,
    file_size BIGINT NOT NULL,
    uploaded_by BINARY(16),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (uploaded_by) REFERENCES users(id)
);

# --------Post media--------

CREATE TABLE post_media(
    post_id BINARY(16) NOT NULL,
    media_id BINARY(16) NOT NULL,

    PRIMARY KEY (post_id, media_id),
    FOREIGN KEY (post_id) REFERENCES posts(id),
    FOREIGN KEY (media_id) REFERENCES media(id)
);

# --------Hashtags--------
CREATE TABLE hashtags(
    id BINARY(16) PRIMARY KEY,
    name varchar(50) NOT NULL UNIQUE
);

# --------Post Hashtag--------
CREATE TABLE post_hashtags(
    hashtag_id BINARY(16) NOT NULL,
    post_id BINARY(16) NOT NULL,

    PRIMARY KEY (hashtag_id, post_id),

    FOREIGN KEY (hashtag_id) REFERENCES hashtags(id),
    FOREIGN KEY (post_id) REFERENCES posts(id)
);

# --------Like--------
CREATE TABLE likes(
    user_id BINARY(16) NOT NULL,
    post_id BINARY(16) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (user_id,post_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (post_id) REFERENCES posts(id)
);

# --------Follows--------
CREATE TABLE follows(
    follower_id BINARY(16) NOT NULL,
    following_id BINARY(16) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (follower_id, following_id),
    FOREIGN KEY (follower_id) REFERENCES users(id),
    FOREIGN KEY (following_id) REFERENCES users(id)
);
