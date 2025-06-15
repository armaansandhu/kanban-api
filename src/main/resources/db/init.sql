-- Users table
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       email VARCHAR(100) UNIQUE NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       first_name VARCHAR(50),
                       last_name VARCHAR(50),
                       avatar_url VARCHAR(500),
                       is_active BOOLEAN DEFAULT true,
                       email_verified BOOLEAN DEFAULT false,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       last_login_at TIMESTAMP
);

-- Boards table
CREATE TABLE boards (
                        id BIGSERIAL PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        description TEXT,
                        owner_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                        background_color VARCHAR(7) DEFAULT '#0079bf', -- Hex colors
                        background_image_url VARCHAR(500),
                        is_public BOOLEAN DEFAULT false,
                        is_archived BOOLEAN DEFAULT false,
                        settings JSONB DEFAULT '{}', -- Board-specific settings
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Board members (many-to-many with roles)
CREATE TABLE board_members (
                               id BIGSERIAL PRIMARY KEY,
                               board_id BIGINT NOT NULL REFERENCES boards(id) ON DELETE CASCADE,
                               user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                               role VARCHAR(20) DEFAULT 'member' CHECK (role IN ('owner', 'admin', 'member', 'observer')),
                               joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               UNIQUE(board_id, user_id)
);

-- Lists (columns) table
CREATE TABLE lists (
                       id BIGSERIAL PRIMARY KEY,
                       board_id BIGINT NOT NULL REFERENCES boards(id) ON DELETE CASCADE,
                       name VARCHAR(255) NOT NULL,
                       position DECIMAL(10,5) NOT NULL, -- For flexible ordering
                       is_archived BOOLEAN DEFAULT false,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Cards table
CREATE TABLE cards (
                       id BIGSERIAL PRIMARY KEY,
                       list_id BIGINT NOT NULL REFERENCES lists(id) ON DELETE CASCADE,
                       name VARCHAR(255) NOT NULL,
                       description TEXT,
                       position DECIMAL(10,5) NOT NULL, -- For flexible ordering
                       due_date TIMESTAMP,
                       is_completed BOOLEAN DEFAULT false,
                       priority VARCHAR(10) DEFAULT 'medium' CHECK (priority IN ('low', 'medium', 'high', 'urgent')),
                       estimated_hours INTEGER,
                       actual_hours INTEGER,
                       custom_fields JSONB DEFAULT '{}', -- Flexible card metadata
                       is_archived BOOLEAN DEFAULT false,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       completed_at TIMESTAMP
);

-- Card assignments (many-to-many)
CREATE TABLE card_members (
                              id BIGSERIAL PRIMARY KEY,
                              card_id BIGINT NOT NULL REFERENCES cards(id) ON DELETE CASCADE,
                              user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                              assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              assigned_by BIGINT REFERENCES users(id),
                              UNIQUE(card_id, user_id)
);

-- Labels for categorization
CREATE TABLE labels (
                        id BIGSERIAL PRIMARY KEY,
                        board_id BIGINT NOT NULL REFERENCES boards(id) ON DELETE CASCADE,
                        name VARCHAR(100) NOT NULL,
                        color VARCHAR(7) NOT NULL, -- Hex color
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Card labels (many-to-many)
CREATE TABLE card_labels (
                             id BIGSERIAL PRIMARY KEY,
                             card_id BIGINT NOT NULL REFERENCES cards(id) ON DELETE CASCADE,
                             label_id BIGINT NOT NULL REFERENCES labels(id) ON DELETE CASCADE,
                             UNIQUE(card_id, label_id)
);

-- Comments/Activities
CREATE TABLE card_comments (
                               id BIGSERIAL PRIMARY KEY,
                               card_id BIGINT NOT NULL REFERENCES cards(id) ON DELETE CASCADE,
                               user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE SET NULL,
                               content TEXT NOT NULL,
                               is_system_comment BOOLEAN DEFAULT false, -- For automated comments
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- File attachments
CREATE TABLE card_attachments (
                                  id BIGSERIAL PRIMARY KEY,
                                  card_id BIGINT NOT NULL REFERENCES cards(id) ON DELETE CASCADE,
                                  uploaded_by BIGINT NOT NULL REFERENCES users(id) ON DELETE SET NULL,
                                  original_filename VARCHAR(255) NOT NULL,
                                  stored_filename VARCHAR(255) NOT NULL,
                                  file_size BIGINT NOT NULL,
                                  mime_type VARCHAR(100),
                                  file_path VARCHAR(500) NOT NULL,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_active ON users(is_active);

-- Board indexes
CREATE INDEX idx_boards_owner ON boards(owner_id);
CREATE INDEX idx_boards_public ON boards(is_public);
CREATE INDEX idx_boards_archived ON boards(is_archived);

-- Board members indexes
CREATE INDEX idx_board_members_board ON board_members(board_id);
CREATE INDEX idx_board_members_user ON board_members(user_id);

-- List indexes
CREATE INDEX idx_lists_board ON lists(board_id);
CREATE INDEX idx_lists_position ON lists(board_id, position);

-- Card indexes
CREATE INDEX idx_cards_list ON cards(list_id);
CREATE INDEX idx_cards_position ON cards(list_id, position);
CREATE INDEX idx_cards_due_date ON cards(due_date) WHERE due_date IS NOT NULL;
CREATE INDEX idx_cards_completed ON cards(is_completed);
CREATE INDEX idx_cards_archived ON cards(is_archived);

-- Card members indexes
CREATE INDEX idx_card_members_card ON card_members(card_id);
CREATE INDEX idx_card_members_user ON card_members(user_id);
