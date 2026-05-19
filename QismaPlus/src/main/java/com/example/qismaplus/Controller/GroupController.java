package com.example.qismaplus.Controller;

import com.example.qismaplus.API.ApiResponse;
import com.example.qismaplus.Model.Group;
import com.example.qismaplus.Service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    // get all groups
    @GetMapping("/get")
    public ResponseEntity<?> getAllGroups() {
        return ResponseEntity.ok(groupService.getAllGroups());
    }

    // create group , creaters will be the admin
    @PostMapping("/create/{userId}")
    public ResponseEntity<?> createGroup(@PathVariable Integer userId, @RequestBody @Valid Group group) {
        groupService.createGroup(userId, group);
        return ResponseEntity.status(200).body(new ApiResponse("Group created successfully"));
    }
    //just admin who can update
    @PutMapping("/update/{adminId}/{groupId}")
    public ResponseEntity<?> updateGroup(@PathVariable Integer adminId, @PathVariable Integer groupId, @RequestBody @Valid Group group) {
        groupService.updateGroup(adminId, groupId, group);
        return ResponseEntity.status(200).body(new ApiResponse("Group updated successfully"));
    }

    // delete group
    @DeleteMapping("/delete/{adminId}/{groupId}")
    public ResponseEntity<?> deleteGroup(@PathVariable Integer adminId, @PathVariable Integer groupId) {
        groupService.deleteGroup(adminId, groupId);
        return ResponseEntity.status(200).body(new ApiResponse("Group deleted successfully"));
    }

    // add member to group
    @PostMapping("/add-member/{adminId}/{groupId}/{memberId}")
    public ResponseEntity<?> addMember(@PathVariable Integer adminId, @PathVariable Integer groupId, @PathVariable Integer memberId) {
        Map<String, Object> result = groupService.addMemberToGroup(adminId, groupId, memberId);
        return ResponseEntity.ok(result);
    }

    // remove member from group
    @DeleteMapping("/remove-member/{adminId}/{groupId}/{memberId}")
    public ResponseEntity<?> removeMember(@PathVariable Integer adminId, @PathVariable Integer groupId, @PathVariable Integer memberId) {
        groupService.removeMemberFromGroup(adminId, groupId, memberId);
        return ResponseEntity.ok(new ApiResponse("Member removed and amounts redistributed successfully"));
    }

    // get groups by admin
    @GetMapping("/admin/{adminId}")
    public ResponseEntity<?> getGroupsByAdmin(@PathVariable Integer adminId) {
        return ResponseEntity.ok(groupService.getGroupsByAdminId(adminId));
    }

    @GetMapping("/{groupId}/preview/{userId}")
    public ResponseEntity<?> previewGroup(@PathVariable Integer groupId, @PathVariable Integer userId) {
        return ResponseEntity.ok(groupService.previewGroup(groupId, userId));
    }
}
