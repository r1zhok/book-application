create database if not exists catalogue;

CREATE TABLE IF NOT EXISTS catalogue.t_book
(
    id INT AUTO_INCREMENT PRIMARY KEY,
    c_title VARCHAR(50) NOT NULL,
    c_details VARCHAR(1000),
    CONSTRAINT chk_title_length CHECK (CHAR_LENGTH(TRIM(c_title)) >= 3)
);