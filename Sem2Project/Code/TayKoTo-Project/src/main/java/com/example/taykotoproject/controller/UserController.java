package com.example.taykotoproject.controller;

import com.example.taykotoproject.common.Constans;
import com.example.taykotoproject.model.Customer;
import com.example.taykotoproject.model.Roles;
import com.example.taykotoproject.model.Users;
import com.example.taykotoproject.payload.RegisterUser;
import com.example.taykotoproject.service.CustomerServiceImpl;
import com.example.taykotoproject.service.EmailService;
import com.example.taykotoproject.service.RoleServiceImpl;
import com.example.taykotoproject.service.UsersServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Controller
public class UserController {

    @Autowired
    private UsersServiceImpl usersService;

    @Autowired
    private RoleServiceImpl roleService;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private CustomerServiceImpl customerService;

    @Autowired
    private  EmailService emailService;

    @GetMapping(path = "/login-register")
    public String loginRegister() {
        return "login-register";
    }

    @PostMapping(value = "/register")
    public String registerSubmit(@ModelAttribute RegisterUser registerUser) {
        if (usersService.existsByUsername(registerUser.getUsername())) {

        } else {
            Users users = new Users();
            users.setUsername(registerUser.getUsername());
            users.setEmail(registerUser.getEmail());
            String password = encoder.encode(registerUser.getPassword());
            users.setPassword(password);

            Set<Roles> roles = new HashSet<>();
            Roles r = roleService.findByRoleName(Constans.ROLE_CUSTOMER).get();
            roles.add(r);

            users.setRoles(roles);

            Customer customer = new Customer();
            customer.setCustomerEmail(users.getEmail());
            customerService.save(customer);

            users.setCustomerId(customer.getCustomerId());
            usersService.saveUsers(users);
        }
        return "redirect:/login-register";
    }

    @GetMapping("/forgot")
    public String forgot(){
        return "Forget";
    }

    @PostMapping("/check")
    public String checkEmail(@RequestParam("email")String email,
                             ModelMap modelMap){
        Optional<Users> users = usersService.findByEmail(email);
        if (users.isPresent()){
            emailService.sendMail(email,"Thông báo xác reset mật khẩu",
                    "Ấn vào link này để reset mật khẩu: http://localhost:8080/change/user/"+users.get().getUserId());

            return "CheckMail";
        }
        else {
            return "NotExist";
        }
    }
    @RequestMapping ("/change/user/{id}")
    public String change(@PathVariable Long id,
                         ModelMap modelMap){
        Users users = usersService.findUserById(id);
        modelMap.addAttribute("users", users);
        return "ChangePassword";
    }

    @PostMapping ("/encode/{id}")
    public String encode(@RequestParam("password")String password,
                         @PathVariable Long id){
        Users users = usersService.findUserById(id);
        String newPass = encoder.encode(password);
        users.setUsername(users.getUsername());
        users.setCustomerId(users.getCustomerId());
        users.setPassword(newPass);
        usersService.saveUsers(users);
        return "Done";
    }


}
