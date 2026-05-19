package com.example.qismaplus.Service;

import com.example.qismaplus.API.ApiException;
import com.example.qismaplus.External.NotificationService;
import com.example.qismaplus.Model.Contribution;
import com.example.qismaplus.Model.Group;
import com.example.qismaplus.Model.Payment;
import com.example.qismaplus.Model.User;
import com.example.qismaplus.Repository.ContributionRepository;
import com.example.qismaplus.Repository.GroupRepository;
import com.example.qismaplus.Repository.PaymentRepository;
import com.example.qismaplus.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;


@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final ContributionRepository contributionRepository;
    private final GroupRepository groupRepository;
    private final NotificationService notificationService;

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public void deletePayment(Integer id) {
        Payment p = paymentRepository.findPaymentById(id);
        if (p == null) {
            throw new ApiException("sorry, payment not found");
        }
        paymentRepository.delete(p);
    }

    // ---------------------------------------------------------------END CRUD---------------------------------------------------------------------
    //3. تعتبر add  اليوزر يدفع ويحسب غرامة لو تاخر + يخصم من الميزانية
    public void makePayment(Integer userId, Integer paymentId) {
        // استدعاء الدفعه
        Payment p = paymentRepository.findPaymentById(paymentId);
        if (p == null) {
            throw new ApiException("payment not found");
        }
        // التحقق ان الدفعه تخص اليوزر
        if (!p.getUserId().equals(userId)) {
            throw new ApiException("this payment does not belong to you");
        }
        // منع تكرار الدفع
        if (p.getStatus().equals("PAID")) {
            throw new ApiException("already paid");
        }
        // استدعاء اليوزر
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("user not found");
        }
        LocalDate today = LocalDate.now();
        // حساب الغرامة اذا تاخر اكثر من يومين
        if (today.isAfter(p.getPaymentDate().plusDays(2))) {
            double penalty = p.getAmount() * 0.01;

            p.setLatePenalty(penalty);
            //تحديث الغرامة
            p.setAmount(p.getAmount() + penalty);
        }

        //  التحقق من الميزانيةلو ماعنده فلوس كافيه راح يرفض الدفع
        if (user.getMonthlyBudget() < p.getAmount()) {
            throw new ApiException("insufficient budget");
        }

        // خصم الفلوس  من الميزانية
        user.setMonthlyBudget(user.getMonthlyBudget() - p.getAmount());
        userRepository.save(user);

        // تتحول لحالة معلقة لين الادمن يعتمدها
        p.setStatus("PENDING");
        paymentRepository.save(p);

        // تنرسل رسالة لليوزر تم الدفع والحالة معلقه
        notificationService.sendPaymentPending(user.getPhoneNumber(), user.getEmail(), user.getName(), p.getAmount());
        //تنرسل رسالة للادمن
        User admin = userRepository.findUserById(groupRepository.findGroupById(contributionRepository.findContributionById(p.getContributionId()).getGroupId()).getCreatedByUserId());
        notificationService.sendAdminPaymentAlert(admin.getPhoneNumber(), admin.getEmail(), admin.getName(), user.getName(), p.getAmount());
    }
    //دفع عن عضو اخر في القروب
    public void payForMember(Integer payerId, Integer paymentId) {
        //استدعي البيمنت
        Payment payment = paymentRepository.findPaymentById(paymentId);
        if (payment == null) {
            throw new ApiException("payment not found");
        }
        //استدعي القروب المربوط باليمنت
        Group group = groupRepository.findGroupById(contributionRepository.findContributionById(payment.getContributionId()).getGroupId());
        //اتحقق الدفع انه عضو بالقروب
        if (!group.getMembersIds().contains(payerId)) {
            throw new ApiException("you are not a group member");
        }
        //اذا تم الدفع بيرمي اكبسشن
        if (payment.getStatus().equals("PAID")) {
            throw new ApiException("already paid");
        }
        //اذا دفع بتتحول الحاله معلقه
        payment.setStatus("PENDING");
        paymentRepository.save(payment);
        //اجيب اليوزراللي دفع
        User payer = userRepository.findUserById(payerId);
        //المدفوع عنه
        User receiver = userRepository.findUserById(payment.getUserId());
        //ارسل للي دفع
        notificationService.sendGroupPaymentPaid(payer.getPhoneNumber(), payer.getEmail(), payer.getName(), payment.getAmount(), payer.getName());
        //المدفوع عنه بتجيه رسالة
        notificationService.sendPaymentPending(receiver.getPhoneNumber(), receiver.getEmail(), receiver.getName(), payment.getAmount());
    }

    //تاكيد الدفع للادمن
    public void verifyPayment(Integer adminId, Integer paymentId) {
        //استدعي البيمنت
        Payment p = paymentRepository.findPaymentById(paymentId);
        if (p == null) {
            throw new ApiException("payment not found");
        }
        //استدعي القطة
        Contribution contribution = contributionRepository.findContributionById(p.getContributionId());
        //استدعي القروب
        Group group = groupRepository.findGroupById(contribution.getGroupId());
        //اذا ماكان الشخص هو الادمن بيرفض العملية
        if (!group.getCreatedByUserId().equals(adminId)) {
            throw new ApiException("only admin can verify");
        }
        //الدفع لازم تكون معلقة اصلا عشان الادمن يوافق عليها
        if (!p.getStatus().equals("PENDING")) {
            throw new ApiException("must be PENDING");
        }
        p.setStatus("PAID");
        p.setVerified(true);
        paymentRepository.save(p);
        //استدعي اليوزر
        User user = userRepository.findUserById(p.getUserId());
        //ارسل رسالة لليوزر
        notificationService.sendPaymentVerified(user.getPhoneNumber(), user.getEmail(), user.getName(), p.getAmount());
    }

    //اجيب البيمنت الخاصه باليوزر
    public List<Payment> getPaymentsByUserId(Integer userId) {
        //استدعها من الديتا
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("sorry, user not found");
        }
        //اجيب كل الدفعات المرتبطه باليوزر
        List<Payment> payments = paymentRepository.findPaymentsByUserId(userId);
        //اذا مافيه بيمنت بيرجع رسالة
        if (payments.isEmpty()) {
            throw new ApiException("no payments found for this user");
        }
        return payments;
    }
    //اجيب البيمنت الخاصة بالقطة
    public List<Payment> getPaymentsByContributionId(Integer contributionId) {
        //استدعي القطة
        Contribution contribution = contributionRepository.findContributionById(contributionId);
        if (contribution == null) {
            throw new ApiException("sorry, contribution not found");
        }
        //اجيب كل البيمنت المرتبطة بالقطة
        List<Payment> payments = paymentRepository.findPaymentsByContributionId(contributionId);
        if (payments.isEmpty()) {
            throw new ApiException("no payments found for this contribution");
        }
        return payments;
    }

    //اجيب البيمنت الغير مدفوعه
    public List<Payment> getUnpaidPaymentsByUserId(Integer userId) {
        //استدعيها
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("sorry, user not found");
        }
        //اجيب المدفوعات غير المدفوعه
        List<Payment> payments = paymentRepository.findUnpaidPaymentsByUserId(userId);
        if (payments.isEmpty()) {
            throw new ApiException("no unpaid payments found for this user");
        }
        return payments;
    }

    //  5. نسبة الالتزام بالدفع لمستخدم في contribution معينة
    public Map<String, Object> getCommitmentRate(Integer userId, Integer contributionId) {
        //استدعي اليوزر
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("sorry, user not found");
        }
        //استدعي البيمنت
        Contribution contribution = contributionRepository.findContributionById(contributionId);
        if (contribution == null) {
            throw new ApiException("sorry, contribution not found");
        }
        //اجيب كل الدفعات تبع القطة
        List<Payment> allPayments = paymentRepository.findPaymentsByContributionId(contributionId);
        //احسبهم لليوزر
        long userTotal = allPayments.stream().filter(p -> p.getUserId().equals(userId)).count();
        //نحسب المدفوعات فعليا
        int paidCount = paymentRepository.countPaidPaymentsByUserIdAndContributionId(userId, contributionId);
        //اذا اليوزر غير مشارك بالقطه بوقف العملية
        if (userTotal == 0) {
            throw new ApiException("no payments found for this user");
        }
        //هنا احسب نسبة الالتزام اللي دفعه اليوزر على التوتل مضروب ب١٠٠
        double rate = ((double) paidCount / userTotal) * 100;

        Map<String, Object> response = new LinkedHashMap<>();
        //اسمه
        response.put("user", user.getName());
        //اسم القطة
        response.put("Contribution Title", contribution.getTitle());
        //عدد المفوعات المدفوعة
        response.put("Paid Payments", paidCount);
        //توتل المطلوب
        response.put("Total Payments", userTotal);
        //نسبة الالتزام
        response.put("Commitment Rate", String.format("%.1f", rate) + "%");

        return response;
    }
    //6. يعرض الاشخاص المتأخره بالدفع داخل القروب
    public List<Payment> getLatePaymentsByGroup(Integer groupId){
        //استدعي القروب
        Group group = groupRepository.findGroupById(groupId);
        if(group == null){
            throw new ApiException("group not found");
        }
        List<Contribution> contributions = contributionRepository.findContributionsByGroupId(groupId);
        //اجهز لست نخزن البيمنت المتاخره
        List<Payment> latePayments = new ArrayList<>();
        //امر على كل القطه
        for(Contribution contribution : contributions){
            //اجيب البيمنت الخاصه بالقطه
            List<Payment> payments = paymentRepository.findPaymentsByContributionId(contribution.getId());
            //لوب عشان اتاكد من كل دفعه
            for(Payment payment : payments){
                //شرط للتاخير
                if(payment.getStatus().equals("UNPAID") && payment.getPaymentDate().isBefore(LocalDate.now())){
                    //يضيف الدفعه المتاخره الى اللست
                    latePayments.add(payment);
                }
            }
        }
        //اذا مافيه بيرجع رسالة
        if(latePayments.isEmpty()){
            throw new ApiException("no late payments");
        }
        //يرجع لست
        return latePayments;
    }


    public Map<String, Object> getPaymentSummary(Integer userId) {
        List<Payment> payments = paymentRepository.findPaymentsByUserId(userId);

        double paid = payments.stream()
                .filter(p -> p.getStatus().equals("PAID"))
                .mapToDouble(Payment::getAmount)
                .sum();

        double unpaid = payments.stream()
                .filter(p -> p.getStatus().equals("UNPAID"))
                .mapToDouble(Payment::getAmount)
                .sum();

        long late = payments.stream()
                .filter(p -> p.getLatePenalty() > 0)
                .count();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("Total Paid", paid);
        map.put("Total Unpaid", unpaid);
        map.put("Late Payments", late);

        return map;
    }
    //ستريك
    public Map<String, Object> getPaymentStreak(Integer userId) {
        //استدعي اليوزر
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("user not found");
        }
        //اجيب كل الدفعات مرتبه عشان احسب الستريك
        List<Payment> payments = paymentRepository.findByUserIdOrderByPaymentDateAsc(userId);
        if (payments.isEmpty()) {
            throw new ApiException("no payments found");
        }
        //اسوي ٢ لست للحساب
        Set<YearMonth> successfulMonths = new HashSet<>();
        Set<YearMonth> badMonths = new HashSet<>();
        //امر على المدفوعات
        for (Payment p : payments) {
            //احول التاريخ الى شهر وسنه للحساب الصح
            YearMonth month = YearMonth.from(p.getPaymentDate());
            //يعتبر شهر ناجح
            if (p.getStatus().equals("PAID")) {
                successfulMonths.add(month);
            } else {
                //شهر غير ناجح
                badMonths.add(month);
            }
        }

        if (successfulMonths.isEmpty()) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("user", user.getName());
            empty.put("streak", 0);
            empty.put("message", "No successful payments yet");
            return empty;
        }
        //اذا لقى شهر ناجح احدث النقطة التي تبدا منها الستريك
        YearMonth current = successfulMonths.stream().max(YearMonth::compareTo).orElse(YearMonth.now());
        //احسب
        int streak = 0;
        //الشهر لازم يكون ناجح ومافيه مشاكل
        while (successfulMonths.contains(current) && !badMonths.contains(current)) {
            streak++;
            //للفحص
            current = current.minusMonths(1);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("user", user.getName());
        response.put("streak", streak);
        response.put("message", streak > 0
                ? " " + streak + " consecutive month(s) of successful payments!"
                //اذا صفر بتطلع له هالمسج
                : "No active streak");

        return response;
    }

}
