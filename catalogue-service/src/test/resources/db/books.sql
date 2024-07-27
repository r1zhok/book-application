CREATE TABLE IF NOT EXISTS t_book
(
    id INT AUTO_INCREMENT PRIMARY KEY,
    c_name VARCHAR(50) NOT NULL UNIQUE,
    c_author VARCHAR(50) NOT NULL,
    c_details VARCHAR(1000)
);

insert into t_book (id, c_name, c_author, c_details)
values (1, 'Гарік Потер', 'хз', 'норм'),
       (2, '.', '...', 'норм'),
       (3, '..', 'хз', 'норм'),
       (4, '...', '...', 'норм');