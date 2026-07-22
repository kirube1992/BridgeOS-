package com.bridgeos.backend;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/users")
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final  UserService  userService;

    @PostMapping
     public ResponseEntity<User> createUser(@Valid @RequestBody User user){
            log.info("POST /api/users - crateing user: {}", user.getEmail());
            User createdUser = userService.createUser(user);
            return  ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
     }


     @GetMapping
     public ResponseEntity<List<User>> getAllUser() {
        log.info("GET api/user fetch all user");
        List<User> user = userService.getAlluser();

        return  ResponseEntity.ok(user);
     }


     @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        log.info("Get api/user/{} fetching - user", id);

        User user = userService.getUserById(id);

        return  ResponseEntity.ok(user);
     }

     @PutMapping("/{id}")
     public  ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody User user) {

        log.info("PUT api/user/{} Updateing User", id);

        User updateUser = userService.updateUser(id, user);

        return  ResponseEntity.ok(updateUser);
     }

     @DeleteMapping("/{id}")
     public ResponseEntity<Void>  deleteUser(@PathVariable Long id) {
        log.info("DELETE api/user/{} deleteing user", id);

        userService.deleteUser(id);
        return  ResponseEntity.noContent().build();
     }


}
