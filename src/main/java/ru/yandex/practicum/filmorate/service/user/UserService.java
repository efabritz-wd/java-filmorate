package ru.yandex.practicum.filmorate.service.user;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class UserService {

    @Qualifier("dbUserStorage")
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> findAllUsers() {
        return this.userStorage.findAllUsers();
    }

    public User findUser(long id) {
        return this.userStorage.findUserById(id);
    }

    public User createUser(User user) {
        return this.userStorage.createUser(user);
    }

    public User updateUser(User user) {
        return this.userStorage.updateUser(user);
    }

    public void addToFriends(long userId, long friendId) {
        addFriend(userId, friendId);
        addFriend(friendId, userId);
    }

    public void addFriend(long userId, long friendId) {
        User user = this.userStorage.findUserById(userId);
        User friend = this.userStorage.findUserById(friendId);
        if (user.getFriends().isEmpty()) {
            Set<Long> friendsSet = new HashSet<>();
            friendsSet.add(friend.getId());
            user.setFriends(friendsSet);
        } else {
            Set<Long> friendsSet = user.getFriends();
            friendsSet.add(friend.getId());
            user.setFriends(friendsSet);
        }
        this.userStorage.updateUser(user);
        log.info("Пользователь " + user.getId() + " добавил в друзья " + friend.getId());
    }

    public void deleteFromFriends(long userId, long friendId) {
        User user = this.userStorage.findUserById(userId);
        if (user.getId() == 13) {
            System.out.println("error");
        }
        User friend = this.userStorage.findUserById(friendId);

        if (user.getFriends().isEmpty() && friend.getFriends().isEmpty()) {
            log.warn("Список друзей пуст");
        } else if (!user.getFriends().contains(friend.getId())) {
            throw new ConditionsNotMetException("Ошибка при удалении пользователя из друзей. Пользователя нет в друзьях");
        } else {
            deleteFriend(userId, friendId);
            deleteFriend(friendId, userId);
        }
        log.info("Пользователь удален из друзей");
    }

    public void deleteFriend(long userId, long friendId) {
        User user = this.userStorage.findUserById(userId);
        User friend = this.userStorage.findUserById(friendId);
        Set<Long> friendsSet = user.getFriends();
        friendsSet.remove(friend.getId());
        user.setFriends(friendsSet);

        this.userStorage.updateUser(user);
    }

    public Set<Long> getCommonFriends(long userId, long friendId) {
        User user = this.userStorage.findUserById(userId);
        User friend = this.userStorage.findUserById(friendId);
        Set<Long> userFriends = user.getFriends();
        Set<Long> friendFriends = friend.getFriends();
        Set<Long> intersectSet = new HashSet<>(userFriends);
        intersectSet.retainAll(friendFriends);
        return intersectSet;
    }
}
