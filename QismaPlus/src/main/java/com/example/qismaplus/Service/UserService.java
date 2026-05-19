package com.example.qismaplus.Service;

import com.example.qismaplus.API.ApiException;
import com.example.qismaplus.Model.Group;
import com.example.qismaplus.Model.Payment;
import com.example.qismaplus.Model.User;
import com.example.qismaplus.Repository.ExpenseRepository;
import com.example.qismaplus.Repository.GroupRepository;
import com.example.qismaplus.Repository.PaymentRepository;
import com.example.qismaplus.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final ExpenseRepository expenseRepository;
    private  final GroupRepository groupRepository;

    // GET
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    // ADD USER
    public void addUser(User user){

        if(userRepository.findUserByEmail(user.getEmail()) != null){
            throw new ApiException("email already exists");
        }
        user.setRole("USER");
        userRepository.save(user);
    }

    // UPDATE
    public void updateUser(Integer id, User user){

        User oldUser = userRepository.findUserById(id);

        if(oldUser == null){
            throw new ApiException("user not found");
        }

        oldUser.setName(user.getName());
        oldUser.setEmail(user.getEmail());
        oldUser.setPassword(user.getPassword());
        oldUser.setMonthlyBudget(user.getMonthlyBudget());
        oldUser.setMonthlyIncome(user.getMonthlyIncome());

        userRepository.save(oldUser);
    }
    // DELETE
    public void deleteUser(Integer id){
        //استدعي اليوزر
        User user = userRepository.findUserById(id);
        if(user == null){
            throw new ApiException("user not found");
        }
        //اتحقق اذا اليوزر هو ادمن لاحد القروبات
        List<Group> adminGroups = groupRepository.findGroupsByCreatedByUserId(id);
        //اذا عنده قروبات ما ينحذف
        if(!adminGroups.isEmpty()){
            throw new ApiException("sorry, user is admin of a group and cannot be deleted");
        }
        //اتاكد هل هو عضو في القروب
        List<Group> groups = groupRepository.findAll();
        //اتحقق اذا موجود
        boolean isMember = groups.stream().anyMatch(g -> g.getMembersIds() != null && g.getMembersIds().contains(id));
        //اذا موجود مقدر احذفه عشان النظام
        if(isMember){
            throw new ApiException("sorry, user is member in a group and cannot be deleted");
        }
        //اتحقق اذا عنده دفعات
        List<Payment> payments = paymentRepository.findPaymentsByUserId(id);
        //اذا عنده ماينحذف
        if(!payments.isEmpty()){
            throw new ApiException("sorry, user has payments and cannot be deleted");
        }

        userRepository.delete(user);
    }

    //=========================-==================================================END CRUD=====================================-===========-===================================

  // ١. خطورة اليوزر وتقرير بسيط
    public Map<String, Object> detectUserRisk(Integer userId) {
        //استدعيها من الديتا
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("user not found");
        }
        //احسب المصاريف
        Double expenses = expenseRepository.sumExpensesByUserId(userId);
        //اذا ماعنده مصاريف تنحسب صفر بدالnull
        expenses = (expenses == null) ? 0.0 : expenses;
        //اجيب كل البيمنت حقت اليوزر
        List<Payment> payments = paymentRepository.findPaymentsByUserId(userId);
        //احسل البيمنت الغير مدفوعه والمعلقه
        double debt = payments.stream()
                .filter(p -> p.getStatus().equals("UNPAID") || p.getStatus().equals("PENDING"))
                //يجمع الديون
                .mapToDouble(Payment::getAmount).sum();
        //حساب نسبة الخطورة
        double riskRatio = (expenses + debt) / user.getMonthlyIncome();
        //تحديد مستواها
        String level;
        if (riskRatio >= 1) {
            level = "High Risk";
        } else if (riskRatio >= 0.7) {
            level = "Medium Risk";
        } else {
            level = "Low Risk";
        }
        // ارسل رساله إذا كان High Risk
        if (level.equals("High Risk")) {
            String message = "Warning " + user.getName() +
                    ", your financial risk level is HIGH. " +
                    "Please review your expenses and unpaid payments.";

            whatsAppService.sendMessage(user.getPhoneNumber(), message);

            emailService.sendEmail(user.getEmail(), "High Financial Risk Warning", message);
        }
        //الرد النهائي
        Map<String, Object> response = new LinkedHashMap<>();
        //اسم اليوزر
        response.put("User" , user.getName());
        //مصاريفه
        response.put("expenses", expenses);
        //ديونه
        response.put("Debt", debt);
        //الراتب
        response.put("Monthly Income", user.getMonthlyIncome());
        //نسبة
        response.put("Risk Ratio", riskRatio);
        //المستوى
        response.put("Risk Level", level);
        return response;
    }
    
    //داشبورد لليوزر
    public Map<String, Object> getDashboard(Integer userId) {
        //استدعيها
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }
        //يجمع كل مصاريفه من expenses
        Double expenses = expenseRepository.sumExpensesByUserId(userId);
        //اذا ماعنده مصاريف بنحسب ٠
        expenses = (expenses == null) ? 0.0 : expenses;
        //اجيب كل القروبات
        List<Group> groups = groupRepository.findAll();
        //احسب عدد القروبات اللي هو فيها
        long memberGroups = groups.stream()
                .filter(g -> g.getMembersIds() != null && g.getMembersIds().contains(userId)).count();
        //احسب كم قروب هو ادمن فيها
        long adminGroups = groups.stream()
                .filter(g -> g.getCreatedByUserId().equals(userId))
                .count();
//استخدمت لنكد عشان يحافظ على ترتيبها بالجيسون
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("Name", user.getName());
        response.put("Expenses", expenses);
        response.put("Member Groups", memberGroups);
        response.put("Admin Groups", adminGroups);

        return response;
    }
    //  نصيحة مالية بناءًا على استخدام اليوزر fake ai
    public Map<String, Object> getSmartSavingTip(Integer userId) {

        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("user not found");
        }
        //احسب المصاريف
        Double expenses = expenseRepository.sumExpensesByUserId(userId);
        expenses = (expenses == null) ? 0.0 : expenses;
        //احسب الدخل
        double salary = user.getMonthlyIncome();
        //احسب نسبة الصرف
        double ratio = (salary == 0) ? 0 : (expenses / salary) * 100;
        String status;
        String tip;

        if (ratio >= 80) {
            status = "CRITICAL";
            tip = "You are spending more than 80% of your income. Try reducing unnecessary expenses and set a strict monthly budget.";
        }
        else if (ratio >= 50) {
            status = "WARNING";
            tip = "You are spending more than half of your income. Consider tracking your daily expenses more carefully.";
        }
        else {
            status = "GOOD";
            tip = "Great job! Your spending is under control. Keep maintaining your saving habits.";
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("user", user.getName());
        response.put("salary", salary);
        response.put("expenses", expenses);
        response.put("spending Ratio", String.format("%.1f", ratio) + "%");
        response.put("status", status);
        response.put("tip", tip);

        return response;
    }

}
