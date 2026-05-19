package com.example.qismaplus.Controller;

import com.example.qismaplus.API.ApiResponse;
import com.example.qismaplus.External.WhatsAppService;
import com.example.qismaplus.Model.User;
import com.example.qismaplus.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final WhatsAppService whatsAppService;

    @GetMapping("/get")
    public ResponseEntity<?> getAllUsers(){
        return ResponseEntity.status(200).body(userService.getAllUsers());
    }

    @PostMapping("/add")
    public ResponseEntity<?> addUser(@RequestBody @Valid User user){
        userService.addUser(user);
        return ResponseEntity.status(200).body(new ApiResponse("User added successfully"));
    }

    // UPDATE
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @RequestBody @Valid User user){
        userService.updateUser(id,user);
        return ResponseEntity.status(200).body(new ApiResponse("User updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id){
        userService.deleteUser(id);
        return ResponseEntity.status(200).body(new ApiResponse("User deleted successfully"));
    }

    // detect user financial risk
    @GetMapping("/risk/{userId}")
    public ResponseEntity<?> getUserRisk(@PathVariable Integer userId){
        return ResponseEntity.ok(userService.detectUserRisk(userId));
    }
    @GetMapping("/{userId}/dashboard")
    public ResponseEntity<?> getDashboard(@PathVariable Integer userId) {
        return ResponseEntity.ok(userService.getDashboard(userId));
    }

    @GetMapping("smart-saving-tip/{userId}")
    public ResponseEntity<?> getSmartSavingTip(@PathVariable Integer userId) {
        return ResponseEntity.ok(userService.getSmartSavingTip(userId));
    }

}
