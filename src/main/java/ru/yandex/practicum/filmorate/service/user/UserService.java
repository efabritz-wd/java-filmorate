package ru.yandex.practicum.filmorate.service.user;

import lombok.Getter;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserService {
    @Getter
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addToFriends(int userId, int friendId) {
        addFriend(userId, friendId);
        addFriend(friendId, userId);
    }

    public void addFriend(int userId, int friendId) {
        User user = this.userStorage.findUserById(userId);
        User friend = this.userStorage.findUserById(friendId);
        if (user.getFriends().isEmpty()) {
            user.setFriends(new HashSet<Long>(friend.getId()));
        } else {
            Set<Long> friendsSet = user.getFriends();
            friendsSet.add(Long.valueOf(friend.getId()));
            user.setFriends(friendsSet);
        }
    }

    public void deleteFromFriends(int userId, int friendId) {
        deleteFriend(userId, friendId);
        deleteFriend(friendId, userId);
    }

    public void deleteFriend(int userId, int friendId) {
        User user = this.userStorage.findUserById(userId);
        User friend = this.userStorage.findUserById(friendId);
        if (!user.getFriends().contains(friend.getId())) {
            throw new ConditionsNotMetException("Ошибка при удалении пользователя. Пользователя нет в друзьях");
        } else {
            Set<Long> friendsSet = user.getFriends();
            friendsSet.remove(friend.getId());
            user.setFriends(friendsSet);
        }
    }

    public Set<Long> getCommonFriends(int userId, int friendId) {
        User user = this.userStorage.findUserById(userId);
        User friend = this.userStorage.findUserById(friendId);
        Set<Long> userFriends = user.getFriends();
        Set<Long> friendFriends = friend.getFriends();
        Set<Long> intersectSet = new HashSet<>(userFriends);
        intersectSet.retainAll(friendFriends);
        return intersectSet;
    }
}
