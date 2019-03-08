package app.tennispartner.tenispartner.models;

import java.util.List;

public class User {
    public static final String FB_AVATAR = "https://graph.facebook.com/%s/picture?width=300";
    private String id;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String gender;
    private String birthday;
    private String provider;
    private String providerId;
    private String avatarUrl;
    private long lastLogin;
    private String g;
    private List<Double> l;

    public User() {
    }

    public User(String phoneNumber, String firstName, String lastName, String gender, String birthday, String provider, String providerId, String avatarUrl, long lastLogin) {
        this.phoneNumber = phoneNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.birthday = birthday;
        this.provider = provider;
        this.providerId = providerId;
        this.avatarUrl = avatarUrl;
        this.lastLogin = lastLogin;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhoneNumber() {

        return phoneNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getGender() {
        return gender;
    }

    public String getProviderId() {
        return providerId;
    }


    public String getBirthday() {
        return birthday;
    }

    public String getProvider() {
        return provider;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getAvatarUrl() {
        if (provider != null) {
            if (provider.equals("facebook.com")) {
                avatarUrl = String.format(FB_AVATAR, providerId);
            }
        }
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getG() {
        return g;
    }

    public void setG(String g) {
        this.g = g;
    }

    public List<Double> getL() {
        return l;
    }

    public void setL(List<Double> l) {
        this.l = l;
    }
}
