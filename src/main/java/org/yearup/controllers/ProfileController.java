package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.yearup.data.ProfileDao;
import org.yearup.data.UserDao;
import org.yearup.models.Profile;

import java.security.Principal;

@RestController
@RequestMapping("/profile")
@CrossOrigin
public class ProfileController
{
    // dao for reading/updating profile info
    private final ProfileDao profileDao;

    // dao used to map logged in username
    private final UserDao userDao;


    @Autowired
    public ProfileController(ProfileDao profileDao, UserDao userDao)
    {
        this.profileDao = profileDao;
        this.userDao = userDao;
    }


    // returns the profile for the currently logged-in user
    @GetMapping
    public Profile getProfile(Principal principal)
    {
        // pull user id from the logged in principal
        int userId = userDao.getIdByUsername(principal.getName());

        return profileDao.getByUserId(userId);
    }

    // updates the current user's profile
    @PutMapping
    public void updateProfile(@RequestBody Profile profile, Principal principal)
    {
        // get user id from auth context
        int userId = userDao.getIdByUsername(principal.getName());

        // force profile to belong to the logged in user
        // prevents someone from updating someone else’s profile
        profile.setUserId(userId);

        // persist updated profile data
        profileDao.update(profile);
    }
}