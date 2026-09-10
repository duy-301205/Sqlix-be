-- Kích hoạt extension hỗ trợ sinh UUID ngẫu nhiên
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TYPE user_occupation AS ENUM ('student', 'professional', 'other');
CREATE TYPE user_system_role AS ENUM ('user', 'instructor', 'admin');
CREATE TYPE oauth_provider AS ENUM ('google', 'github');

CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NULL, -- NULL nếu chỉ đăng nhập bằng Google/GitHub
                       full_name VARCHAR(100) NOT NULL,
                       avatar_url TEXT NULL,
                       occupation user_occupation NOT NULL DEFAULT 'other',
                       system_role user_system_role NOT NULL DEFAULT 'user',
                       terms_accepted BOOLEAN NOT NULL DEFAULT FALSE,
                       is_active BOOLEAN NOT NULL DEFAULT TRUE,
                       created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);


-- 3. USER PROFILES & GAMIFICATION (Streak, Target)
CREATE TABLE user_profiles (
                               user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                               current_streak INT NOT NULL DEFAULT 0,
                               longest_streak INT NOT NULL DEFAULT 0,
                               last_active_date DATE NULL,
                               daily_target_minutes INT NOT NULL DEFAULT 15,
                               bio TEXT NULL,
                               updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tự động tạo bản ghi user_profiles khi có user mới đăng ký
CREATE OR REPLACE FUNCTION handle_new_user_profile()
RETURNS TRIGGER AS $$
BEGIN
INSERT INTO user_profiles (user_id)
VALUES (NEW.id);
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_create_user_profile
    AFTER INSERT ON users
    FOR EACH ROW
    EXECUTE FUNCTION handle_new_user_profile();

-- 4. OAUTH ACCOUNTS
CREATE TABLE oauth_accounts (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                provider oauth_provider NOT NULL,
                                provider_user_id VARCHAR(255) NOT NULL,
                                created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                CONSTRAINT uq_provider_account UNIQUE (provider, provider_user_id)
);

CREATE INDEX idx_oauth_accounts_user_id ON oauth_accounts(user_id);

-- 5. REFRESH TOKENS (Quản lý phiên đăng nhập)
CREATE TABLE refresh_tokens (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                token_hash VARCHAR(255) NOT NULL UNIQUE,
                                user_agent VARCHAR(500) NULL,
                                ip_address VARCHAR(45) NULL,
                                expires_at TIMESTAMPTZ NOT NULL,
                                revoked_at TIMESTAMPTZ NULL,
                                created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_hash ON refresh_tokens(token_hash);

-- 6. PASSWORD RESET TOKENS
CREATE TABLE password_reset_tokens (
                                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                       user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                       token_hash VARCHAR(255) NOT NULL UNIQUE,
                                       expires_at TIMESTAMPTZ NOT NULL,
                                       used_at TIMESTAMPTZ NULL,
                                       created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_pwd_reset_tokens_hash ON password_reset_tokens(token_hash);

-- 7. EMAIL VERIFICATION TOKENS
CREATE TABLE email_verification_tokens (
                                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                           user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                           token_hash VARCHAR(255) NOT NULL UNIQUE,
                                           expires_at TIMESTAMPTZ NOT NULL,
                                           used_at TIMESTAMPTZ NULL,
                                           created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_email_verify_tokens_hash ON email_verification_tokens(token_hash);