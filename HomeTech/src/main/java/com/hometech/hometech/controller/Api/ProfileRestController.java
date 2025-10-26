package com.hometech.hometech.controller.Api;

import com.hometech.hometech.dto.UpdateProfileDTO;
import com.hometech.hometech.model.Customer;
import com.hometech.hometech.service.ProfileService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileRestController {

    private final ProfileService profileService;

    public ProfileRestController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PutMapping("/{userId}")
    public Customer updateOrCreateProfile(@PathVariable Long userId, @RequestBody UpdateProfileDTO dto) {
        return profileService.updateOrCreateProfile(userId, dto);
    }
}
