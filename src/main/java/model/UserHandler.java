package model;

import db.Database;

public class UserHandler {

    public void registerUser(User user) {
        Database.addUser(user);
    }

    public void login(String userId, String password) {
        /*
        * if 유저 있으면
        *   성공
        * else
        *  예외
        * */
    }
}
