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
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
@Service
@RequiredArgsConstructor
public class ContributionService {
    private final ContributionRepository contributionRepository;
    private final GroupRepository groupRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    public List<Contribution> getAllContributions() {
        return contributionRepository.findAll();
    }

    public void addContribution(Contribution contribution) {
        Group group = groupRepository.findGroupById(contribution.getGroupId());
        if(group == null){
            throw new ApiException("sorry, group id not found");
        }
        if(contribution.getEndDate().isBefore(contribution.getStartDate())){
            throw new ApiException("sorry, end date must be after start date");
        }
        if(group.getMembersIds() == null || group.getMembersIds().isEmpty()){
            throw new ApiException("sorry, group has no members");
        }

        int memberCount = group.getMembersIds().size();
        long months = java.time.temporal.ChronoUnit.MONTHS.between(
                contribution.getStartDate(), contribution.getEndDate()) + 1;
        double monthlyAmount = contribution.getTotalAmount() / (memberCount * months);

        List<String> warnings = new ArrayList<>();
        for (Integer memberId : group.getMembersIds()) {
            User member = userRepository.findUserById(memberId);
            if (member == null) continue;

            if (member.getMonthlyBudget() < monthlyAmount) {
                warnings.add(member.getName()
                        + " (budget: " + member.getMonthlyBudget() + ")");
            } else if (monthlyAmount > member.getMonthlyIncome() * 0.3) {
                warnings.add(member.getName()
                        + " (contribution exceeds 30% of income: "
                        + member.getMonthlyIncome() + ")");
            }
        }

        if (!warnings.isEmpty()) {
            throw new ApiException("WARNING: these members cannot join. Monthly amount: "
                    + monthlyAmount + " SAR — " + warnings);
        }

        contribution.setMonthlyAmount(monthlyAmount);
        contributionRepository.save(contribution);
        generatePaymentsForContribution(contribution, group);
    }

    public void updateContribution(Integer id, Contribution contribution) {
        //اتاكد من القطة
        Contribution c = contributionRepository.findContributionById(id);
        if (c == null) {
            throw new ApiException("sorry, contribution not found");
        }
        //اتاكد من القروب
        Group group = groupRepository.findGroupById(contribution.getGroupId());
        if (group == null) {
            throw new ApiException("sorry, group id not found");
        }
        //اتاكد من التاريخ لانه كان يطلع لي ايرور ضروري بداية التاريخ قبل النهاية
        if (contribution.getEndDate().isBefore(contribution.getStartDate())) {
            throw new ApiException("sorry, end date must be after start date");
        }
        c.setTitle(contribution.getTitle());
        c.setTotalAmount(contribution.getTotalAmount());
        c.setDurationType(contribution.getDurationType());
        c.setDurationValue(contribution.getDurationValue());
        c.setMonthlyAmount(contribution.getMonthlyAmount());
        c.setStartDate(contribution.getStartDate());
        c.setEndDate(contribution.getEndDate());
        c.setStatus(contribution.getStatus());
        c.setGroupId(contribution.getGroupId());
        c.setParticipantsIds(contribution.getParticipantsIds());

        contributionRepository.save(c);
    }

    public void deleteContribution(Integer id) {
        //اتاكد من القطة
        Contribution c = contributionRepository.findContributionById(id);
        if (c == null) {
            throw new ApiException("sorry, contribution not found");
        }
        //يحذف لو كان موجود
        contributionRepository.delete(c);
    }

    // ----------------------------------------------------------END CRUD---------------------------------------------------------------------

    // get contributions by group id
    public List<Contribution> getContributionsByGroupId(Integer groupId) {
        //اتاكد من القروب
        Group group = groupRepository.findGroupById(groupId);
        if (group == null) {
            throw new ApiException("sorry, group id not found");
        }
        //يرجع كل القطة التابعة لقروب
        List<Contribution> contributions = contributionRepository.findContributionsByGroupId(groupId);
       //لو ما كان موجود بيرجع رسالة مو لست فاضية
        if (contributions.isEmpty()) {
            throw new ApiException("no contributions found for this group");
        }
        return contributions;
    }

    // complete contribution
    public void completeContribution(Integer id) {
        Contribution c = contributionRepository.findContributionById(id);
        if (c == null) {
            throw new ApiException("sorry, contribution not found");
        }
        //اذا الحالة مو اكتف يرفض العملية
        if (!c.getStatus().equals("ACTIVE")) {
            throw new ApiException("sorry, only ACTIVE contributions can be completed");
        }
        c.setStatus("COMPLETED");
        contributionRepository.save(c);
    }

    //مثيود للاستدعاء
    private void generatePaymentsForContribution(Contribution contribution, Group group) {
        //يبدا من تاريخ البداية
        LocalDate currentDate = contribution.getStartDate();
        //احسب عدد اعضاء القروب عشان اقسم المبلغ عليهم
        int memberCount = group.getMembersIds().size();
       //كم كل شخص يدفع = التوتل ٪ عدد الاعضاء ٪ عدد الاشهر
        double amountPerMember = contribution.getTotalAmount() / memberCount / contribution.getDurationValue();
        //لوب تشتغل طول ما التاريخ ماجا النهاية
        while(!currentDate.isAfter(contribution.getEndDate())){
            //للوضوح احول التاريخ الى سترنق
            String month = currentDate.getMonth().toString() + "_" + currentDate.getYear();
          //لوب لاعضاء القروب
            for(Integer memberId : group.getMembersIds()){
                User user = userRepository.findUserById(memberId);
                //
                if(user == null){
                    continue;
                }
                //للتحقق وتمنع الدبلكيت لليوزر
                boolean exists = paymentRepository.existsByUserIdAndContributionIdAndMonth
                        (memberId, contribution.getId(), month);
                if (exists) {
                    continue;
                }
                //اوبجكت للبيمنت
                Payment payment = new Payment();
                //ربط البيمنت باليوزر
                payment.setUserId(memberId);
                //ربط بالقطة
                payment.setContributionId(contribution.getId());
                //احدد الشهر
                payment.setMonth(month);
                // قيمة الدفع لكل شخص
                payment.setAmount(amountPerMember);
                //اتوماتكلي بيكون غير مدفوع
                payment.setStatus("UNPAID");
                //ضروري يتاكد من الادمن
                payment.setVerified(false);
                //حساب الغرامة
                payment.setLatePenalty(0.0);
                //تاريخ الاستحقاق لدفعة
                payment.setPaymentDate(currentDate);
                paymentRepository.save(payment);
            }
            //الانتقال للشهر اللي بعده ونكرر العملية نفسها
            currentDate = currentDate.plusMonths(1);
        }
    }
    }