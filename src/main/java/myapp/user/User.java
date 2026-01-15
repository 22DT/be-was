package myapp.user;

public class User {
    private String userId;
    private String password;
    private String name;
    private String profileImage;

    public User(String userId, String password, String name) {
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.profileImage = null;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }


    public String getProfileImage() {
        return profileImage;
    }

    @Override
    public String toString() {
        return "User [userId=" + userId + ", password=" + password + ", name=" + name + "]";
    }

    public void updateProfileImage(String path) {
        this.profileImage = path;
    }
    public void deleteProfileImage(){
        this.profileImage=null;
    }

}
