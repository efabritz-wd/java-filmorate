INSERT INTO genre (name) values ('documentary'), ('history');
INSERT INTO mpa (rating) values ('PG'), ('PG-2');
INSERT INTO users (email, login, name, birthday) values ('test1@mail.com', 'testy', 'test1', DATE '1999-10-03'), ('test2@mail.com', 'newtest', 'test2', DATE '1988-01-01') ;
INSERT INTO user_friend (user_id, friend_id) values (1,2);
INSERT INTO film (name, description, releaseDate, duration, rating) values ('Film1', 'lala', DATE '2023-01-02', 60, 1), ('Film2', 'desc2', DATE '2025-02-08', 90, 1);
INSERT INTO film_like (film_id, user_id) values (1, 1), (1, 2);
INSERT INTO film_genre (film_id, genre_id) values (1, 2), (2, 1);