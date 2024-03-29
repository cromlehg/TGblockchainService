CREATE TABLE roles (
  id                       SERIAL PRIMARY KEY,
  `name`                   VARCHAR(150) NOT NULL UNIQUE,
  descr                    TEXT
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE permissions (
  id                       SERIAL PRIMARY KEY,
  value                    VARCHAR(150) NOT NULL UNIQUE,
  descr                    TEXT
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE roles_to_targets (
  role_id                  BIGINT UNSIGNED NOT NULL,
  target_type              ENUM("account") NOT NULL,
  target_id                BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (role_id, target_type, target_id)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE permissions_to_targets (
  permission_id            BIGINT UNSIGNED NOT NULL,
  target_type              ENUM("account", "role")  NOT NULL,
  target_id                BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (permission_id, target_type, target_id)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE tags (
  id                       SERIAL PRIMARY KEY,
  name                     VARCHAR(150) NOT NULL,
  descr                    TEXT
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE tags_to_targets (
  tag_id                   BIGINT UNSIGNED NOT NULL,
  target_type              ENUM("post")  NOT NULL,
  target_id                BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (tag_id, target_type, target_id)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE sessions (
  id                        SERIAL PRIMARY KEY,
  user_id                   BIGINT UNSIGNED NOT NULL,
  ip                        VARCHAR(100) NOT NULL,
  user_agent                TINYTEXT,
  os                        TINYTEXT,
  device                    TINYTEXT,
  session_key               VARCHAR(100) NOT NULL UNIQUE,
  created                   BIGINT UNSIGNED NOT NULL,
  expire                    BIGINT UNSIGNED NOT NULL
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE accounts (
  id                        SERIAL PRIMARY KEY,
  login                     VARCHAR(100) NOT NULL UNIQUE,
  email                     VARCHAR(100) NOT NULL UNIQUE,
  hash                      VARCHAR(60),
  confirmation_status       ENUM('confirmed', 'wait confirmation') NOT NULL,
  account_status            ENUM('normal', 'locked') NOT NULL,
  registered                BIGINT UNSIGNED NOT NULL,
  confirm_code              VARCHAR(100),
  password_recovery_code    VARCHAR(100),
  password_recovery_date    BIGINT UNSIGNED
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE sn_accounts (
  id                        SERIAL PRIMARY KEY,
  owner_id                  BIGINT UNSIGNED NOT NULL,
  sn_type                   ENUM('telegram') NOT NULL,
  login                     VARCHAR(100) NOT NULL UNIQUE,
  registered                BIGINT UNSIGNED NOT NULL
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE options (
  id                        SERIAL PRIMARY KEY,
  name                      VARCHAR(100) NOT NULL,
  descr                     VARCHAR(150) NOT NULL,
  `type`                    VARCHAR(100) NOT NULL,
  `value`                   TEXT NOT NULL
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE menus (
  id                        SERIAL PRIMARY KEY,
  parent_id                 BIGINT UNSIGNED,
  menu_id                   BIGINT UNSIGNED,
  link                      VARCHAR(150),
  name                      VARCHAR(150) NOT NULL,
  content                   TEXT,
  `order`                   INT UNSIGNED NOT NULL
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE posts (
  id                        SERIAL PRIMARY KEY,
  owner_id                  BIGINT UNSIGNED NOT NULL,
  title                     VARCHAR(150) NOT NULL,
  thumbnail                 VARCHAR(150),
  content                   TEXT NOT NULL,
  status                    ENUM('draft', 'sandbox', 'published') NOT NULL,
  created                   BIGINT UNSIGNED NOT NULL
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE comments (
  id                       SERIAL PRIMARY KEY,
  owner_id                 BIGINT UNSIGNED,
  target_type              ENUM("post")  NOT NULL,
  target_id                BIGINT UNSIGNED NOT NULL,
  parent_id                BIGINT UNSIGNED,
  content_type             ENUM("text", "html", "markdown")  NOT NULL,
  content                  TEXT NOT NULL,
  status                   ENUM("normal") NOT NULL,
  created                  BIGINT UNSIGNED NOT NULL
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE changes (
  id                       SERIAL PRIMARY KEY,
  owner_id                 BIGINT UNSIGNED,
  target_type              ENUM("post", "comment")  NOT NULL,
  target_id                BIGINT UNSIGNED NOT NULL,
  changed                  BIGINT UNSIGNED NOT NULL
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE blocks (
  id                        CHAR(64) PRIMARY KEY,
  height                    BIGINT UNSIGNED NOT NULL,
  version                   INT UNSIGNED NOT NULL,
  timestamp                 BIGINT UNSIGNED NOT NULL,
  parent_id                 CHAR(64) NOT NULL,
  generator                 CHAR(74) NOT NULL,
  signature                 CHAR(128) NOT NULL
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE txs (
  id                        CHAR(64) PRIMARY KEY,
  block_id                  CHAR(64) NOT NULL,
  block_h                   BIGINT UNSIGNED NOT NULL,
  nonce                     BIGINT UNSIGNED NOT NULL,
  signer                    CHAR(74) NOT NULL,
  signature                 CHAR(128) NOT NULL,
  kind                      ENUM("change_permissions", "transfer_assets", "identify", "vote") NOT NULL,
  recepient                 CHAR(74),
  amount                    BIGINT UNSIGNED,
  timestamp                 BIGINT UNSIGNED NOT NULL
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE bc_accounts (
  id                        CHAR(74) PRIMARY KEY,
  block_id                  CHAR(64) NOT NULL,
  block_height              BIGINT UNSIGNED NOT NULL,
  name                      CHAR(12), 
  amount                    BIGINT UNSIGNED NOT NULL
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;


