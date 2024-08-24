package com.visualpathit.account.controller;

import com.visualpathit.account.model.User;
import com.visualpathit.account.service.ProducerService;
import com.visualpathit.account.service.SecurityService;
import com.visualpathit.account.service.UserService;
import com.visualpathit.account.utils.MemcachedUtils;
import com.visualpathit.account.validator.UserValidator;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private UserValidator userValidator;

    @Autowired
    private ProducerService producerService;

    @RequestMapping(value = "/registration", method = RequestMethod.GET)
    public String showRegistrationForm(Model model) {
        model.addAttribute("userForm", new User());
        return "registration";
    }

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public String registerUser(@ModelAttribute("userForm") User userForm,
                               BindingResult bindingResult, Model model) {
        userValidator.validate(userForm, bindingResult);
        if (bindingResult.hasErrors()) {
            return "registration";
        }

        userService.save(userForm);
        securityService.autologin(userForm.getUsername(), userForm.getPasswordConfirm());
        return "redirect:/welcome";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String showLoginForm(Model model, String error, String logout) {
        if (error != null) {
            model.addAttribute("error", "Your username and password is invalid.");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }
        return "login";
    }

    @RequestMapping(value = {"/", "/welcome"}, method = RequestMethod.GET)
    public String showWelcomePage(Model model) {
        return "welcome";
    }

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String showIndexHomePage(Model model) {
        return "index_home";
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public String getAllUsers(Model model) {
        List<User> users = userService.getList();
        model.addAttribute("users", users);
        return "userList";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String getUserById(@PathVariable("id") String id, Model model) {
        String result;
        try {
            User userData = MemcachedUtils.memcachedGetData(id);
            if (userData != null) {
                result = "Data is From Cache";
                model.addAttribute("user", userData);
            } else {
                User user = userService.findById(Long.parseLong(id));
                result = MemcachedUtils.memcachedSetData(user, id);
                model.addAttribute("user", user);
            }
            model.addAttribute("result", result);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "user";
    }

    @RequestMapping(value = "/{username}", method = RequestMethod.GET)
    public String showUserUpdateForm(@PathVariable("username") String username, Model model) {
        User user = userService.findByUsername(username);
        model.addAttribute("user", user);
        return "userUpdate";
    }

    @RequestMapping(value = "/{username}", method = RequestMethod.POST)
    public String updateUser(@PathVariable("username") String username,
                             @ModelAttribute("user") User userForm, Model model) {
        User user = userService.findByUsername(username);
        updateUserDetails(user, userForm);
        userService.save(user);
        return "redirect:/welcome";
    }

    @RequestMapping(value = "/rabbit", method = RequestMethod.GET)
    public String setupRabbitMQ() {
        for (int i = 0; i < 20; i++) {
            producerService.produceMessage(generateString());
        }
        return "rabbitmq";
    }

    private void updateUserDetails(User user, User userForm) {
        user.setUsername(userForm.getUsername());
        user.setUserEmail(userForm.getUserEmail());
        user.setDateOfBirth(userForm.getDateOfBirth());
        user.setFatherName(userForm.getFatherName());
        user.setMotherName(userForm.getMotherName());
        user.setGender(userForm.getGender());
        user.setLanguage(userForm.getLanguage());
        user.setMaritalStatus(userForm.getMaritalStatus());
        user.setNationality(userForm.getNationality());
        user.setPermanentAddress(userForm.getPermanentAddress());
        user.setTempAddress(userForm.getTempAddress());
        user.setPhoneNumber(userForm.getPhoneNumber());
        user.setSecondaryPhoneNumber(userForm.getSecondaryPhoneNumber());
        user.setPrimaryOccupation(userForm.getPrimaryOccupation());
        user.setSecondaryOccupation(userForm.getSecondaryOccupation());
        user.setSkills(userForm.getSkills());
        user.setWorkingExperience(userForm.getWorkingExperience());
    }

    private static String generateString() {
        return "uuid = " + UUID.randomUUID().toString();
    }
}
