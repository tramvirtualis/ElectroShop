package com.hometech.hometech.dto;

import lombok.Data;

@Data
public class UpdateProfileDTO {
    private String name;        // Display name from users.name
    private String fullName;    // Full name from users.full_name
    private String phone;       // Phone from users.phone
    private String email;       // Email from users.email (read-only, also in accounts)
    private String pictureUrl;  // Picture URL from users.picture_url

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }
}
