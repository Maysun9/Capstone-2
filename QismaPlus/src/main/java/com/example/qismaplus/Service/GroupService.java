package com.example.qismaplus.Service;

import com.example.qismaplus.API.ApiException;
import com.example.qismaplus.Model.Contribution;
import com.example.qismaplus.Model.Group;
import com.example.qismaplus.Model.Payment;
import com.example.qismaplus.Model.User;
import com.example.qismaplus.Repository.ContributionRepository;
import com.example.qismaplus.Repository.GroupRepository;
import com.example.qismaplus.Repository.PaymentRepository;
import com.example.qismaplus.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ContributionRepository contributionRepository;
    private final PaymentRepository paymentRepository;

    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    // اللي يسوي القروب يصير GROUP_ADMIN تلقائياً
    public void createGroup(Integer userId, Group group) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("sorry, user not found");
        }
        user.setRole("GROUP_ADMIN");
        userRepository.save(user);

        group.setCreatedByUserId(userId);
        group.getMembersIds().add(userId);
        groupRepository.save(group);
    }

    public void updateGroup(Integer adminId, Integer groupId, Group group) {
        Group g = groupRepository.findGroupById(groupId);
        if (g == null) {
            throw new ApiException("sorry, group not found");
        }
        if (!g.getCreatedByUserId().equals(adminId)) {
            throw new ApiException("sorry, only the group admin can update the group");
        }
        g.setName(group.getName());
        g.setType(group.getType());
        g.setStartDate(group.getStartDate());
        g.setEndDate(group.getEndDate());
        groupRepository.save(g);
    }

    public void deleteGroup(Integer adminId, Integer groupId) {
        Group g = groupRepository.findGroupById(groupId);
        if (g == null) {
            throw new ApiException("sorry, group not found");
        }
        if (!g.getCreatedByUserId().equals(adminId)) {
            throw new ApiException("sorry, only the group admin can delete the group");
        }
        groupRepository.delete(g);
    }

    // ---------------------------------------------------------------END CRUD---------------------------------------------------------------------

    // الأدمن يضيف عضو - مع تنبيه لو ميزانيته أقل من المطلوب7
    public Map<String, Object> addMemberToGroup(Integer adminId, Integer groupId, Integer memberId) {
       //استدعي القروب
        Group g = groupRepository.findGroupById(groupId);
        if (g == null) {
            throw new ApiException("sorry, group not found");
        }
      //اتاكد اللي يضيف هو ادمن
        if (!g.getCreatedByUserId().equals(adminId)) {
            throw new ApiException("sorry, only the group admin can add members");
        }
        //استدعي اليوزر
        User member = userRepository.findUserById(memberId);
        if (member == null) {
            throw new ApiException("sorry, user not found");
        }
        //اتاكد انه مو موجود
        if (g.getMembersIds().contains(memberId)) {
            throw new ApiException("sorry, user is already a member in this group");
        }

        // اجيب القطه داخل القروب
        List<Contribution> contributions = contributionRepository.findContributionsByGroupId(groupId);
        //احسب المطلوب
        double totalMonthlyRequired = contributions.stream().mapToDouble(Contribution::getMonthlyAmount).sum();

        // فحص الميزانية اذا المبلغ اكبر من الميزانيه نرفض الاضافه
        if (totalMonthlyRequired > 0 && member.getMonthlyBudget() < totalMonthlyRequired) {
            throw new ApiException("WARNING: " + member.getName() + " budget is " + member.getMonthlyBudget() + " which is less than required " + totalMonthlyRequired);
        }

        // فحص ٣٠% من الراتب، شرط للتاكيد
        if (totalMonthlyRequired > member.getMonthlyIncome() * 0.3) {
            throw new ApiException("WARNING: contribution exceeds 30% of income for " + member.getName());
        }

        // إضافة العضو
        g.getMembersIds().add(memberId);
        groupRepository.save(g);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "success");
        response.put("groupName", g.getName());
        response.put("groupId", groupId);
        response.put("message", "Member added successfully");
        response.put("memberId", memberId);
        return response;
    }

    // 8 الأدمن يحذف عضو - يتوزع المبلغ تلقائياً على الباقين
    public void removeMemberFromGroup(Integer adminId, Integer groupId, Integer memberId) {
        Group group = groupRepository.findGroupById(groupId);
        if(group == null){
            throw new ApiException("sorry, group not found");
        }
        //اتاكد اللي يحذف هو الادمن
        if(!group.getCreatedByUserId().equals(adminId)){
            throw new ApiException("sorry, only admin can remove members");
        }
        //اتاكد من وجود العضو
        if(!group.getMembersIds().contains(memberId)){
            throw new ApiException("sorry, member not found in group");
        }
        //امنع حذف الادمن نفسه
        if(memberId.equals(adminId)){
            throw new ApiException("sorry, admin can not remove himself");
        }
        //حذف العضو
        group.getMembersIds().remove(memberId);
        groupRepository.save(group);
        //اجيب كل القطة
        List<Contribution> contributions = contributionRepository.findContributionsByGroupId(groupId);
        //لوب للقطه
        for(Contribution contribution : contributions) {
           //احسب كم باقي بالقروب
            int memberCount = group.getMembersIds().size();
            if (memberCount == 0) {
                continue;
            }
            //احسب عدد الاشهر
            long months = java.time.temporal.ChronoUnit.MONTHS.between(contribution.getStartDate(), contribution.getEndDate()) + 1;
            //احسب المبلغ الجديد عشان تتعدل القطه بعد خروج العضو
            double newAmount = contribution.getTotalAmount() / (memberCount * months);
            //تحديث
            contribution.setMonthlyAmount(newAmount);
           //احفظها
            contributionRepository.save(contribution);
            //اجيب كل البيمنت المرتبطه بهذي القطة
            List<Payment> payments = paymentRepository.findPaymentsByContributionId(contribution.getId());
            for (Payment payment : payments) {
                //احدث الدفعات الغير مدفوعه
                if (payment.getStatus().equals("UNPAID")) {
                    //اتاكد هل اليوزر باقي بالقروب
                    if (group.getMembersIds().contains(payment.getUserId())) {
                        payment.setAmount(newAmount);
                        paymentRepository.save(payment);
                    }
                }
            }
        }
    }

    public List<Group> getGroupsByAdminId(Integer adminId) {
        User user = userRepository.findUserById(adminId);
        if (user == null) {
            throw new ApiException("sorry, user not found");
        }
        return groupRepository.findGroupsByCreatedByUserId(adminId);
    }

    //بريف قبل ما اليوزر يدخل القروب
    public Map<String, Object> previewGroup(Integer groupId, Integer userId) {
        //استدعي القروب واليوزر
        Group group = groupRepository.findGroupById(groupId);
        User user = userRepository.findUserById(userId);
        //اذا احد منهم غير موجود بتعطيني رسالة
        if (group == null || user == null) {
            throw new ApiException("not found");
        }
        //اجيب كل القطه
        List<Contribution> contributions = contributionRepository.findContributionsByGroupId(groupId);
       //احسب كم لازم يدفع شهريا في القروب
        double totalMonthly = contributions.stream().mapToDouble(Contribution::getMonthlyAmount).sum();
        //الرد
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("Group Name: ", group.getName());
        res.put("Members Count: ", group.getMembersIds().size());
        res.put("Monthly Required: ", totalMonthly);
        res.put("can Join? ", user.getMonthlyBudget() >= totalMonthly);

        return res;
    }


}