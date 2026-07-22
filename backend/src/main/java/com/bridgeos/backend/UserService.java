package com.bridgeos.backend;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {


    private final UserRepository userRepository;

    public User createUser(User user) {
        log.info("Create new user: {}", user.getEmail());


        if(userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }

        return userRepository.save(user);
    }
    public List<User> getAlluser() {
        log.info("fetch all users");
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        log.info("Fetch user with id: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public User updateUser(Long id,User updatedUser) {
        User existingUser = getUserById(id);


        existingUser.setName(updatedUser.getName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setRole(updatedUser.getRole());

        return  userRepository.save(existingUser);
    }


    public void deleteUser(Long id) {
       log.info("Update the user with Id: {}", id);

       getUserById(id);
       userRepository.deleteById(id);
    }
}