package com.example.taykotoproject.controller;

import com.example.taykotoproject.config.PaypalPaymentIntent;
import com.example.taykotoproject.config.PaypalPaymentMethod;
import com.example.taykotoproject.model.*;
import com.example.taykotoproject.service.*;
import com.example.taykotoproject.utils.Utils;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
public class DetailController {

    @Autowired
    private CustomerServiceImpl customerService;

    @Autowired
    private DealServiceImpl dealService;

    @Autowired
    private InfoServicesImpl infoService;

    @Autowired
    private UsersServiceImpl usersService;

    @Autowired
    private VehicleServiceImpl vehicleService;

    @Autowired
    private VehicleGalleryServiceImpl galleryService;

    @Autowired
    private EmailService emailService;

    @GetMapping(path = "/car/{id}")
    public String detailCar(@PathVariable Long id,

                                        Model model,

                                        Deal deal) {

        Optional<Vehicle> vehicle = vehicleService.findById(id);

        Optional<InfoService> info = infoService.findByVehicleId(id);

        List<VehicleGallery> gallery = galleryService.findAllById(id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        Users user = usersService.findByUsername(username);

        if (user == null){
            return "notlogin";
        } else {
            if (info.isPresent() && vehicle.isPresent()) {

                Customer customer = customerService.getOne(user.getCustomerId());

                model.addAttribute("vehicle", vehicle.get());

                model.addAttribute("info", info.get());

                model.addAttribute("gallery", gallery);

                model.addAttribute("user", user);

                model.addAttribute("customer", customer);

                model.addAttribute("deal", deal);

                return "car-details";

            } else {

                return "redirect:/";

            }

        }
    }

    @PostMapping(path = "/order/{id}")
    public String submitOrder(@PathVariable Long id,

                              @Valid Deal deal,

                              @Valid Customer customer,

                              Model model) {

        LocalDate localDate = LocalDate.now();

        Optional<Vehicle> vehicle = vehicleService.findById(id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        Users user = usersService.findByUsername(username);

        Customer customer1 = customerService.getOne(user.getCustomerId());

        customer.setCustomerId(customer1.getCustomerId());

        customerService.save(customer);

        Vehicle vehiclefind = vehicleService.findById(id).get();

        deal.setCustomerId(user.getCustomerId());

        deal.setVehicle(vehiclefind);

        deal.setStatus("Waiting");

        deal.setPrice(vehicle.get().getPrice());

        deal.setOrderDate(Date.valueOf(localDate));

        dealService.save(deal);

        model.addAttribute("deal", deal);

        return "payment";
    }

    @PostMapping(path = "cancel/deal/{id}")
    public String cancel(@PathVariable Long id){
        Optional<Deal> deal = dealService.findById(id);
        dealService.delete(deal.get().getDealId());
        return "redirect:/cars";

    }

    public static final String URL_PAYPAL_SUCCESS = "success";
    public static final String URL_PAYPAL_CANCEL = "cancel";
    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private PaypalService paypalService;

    @PostMapping("/pay/deal/{id}")
    public String pay(HttpServletRequest request, @RequestParam("deposit") double deposit,
                      @PathVariable Long id){
        Deal deal = dealService.getOne(id);
        deal.setStatus("Approved");
        deal.setDepositDate(Date.valueOf(LocalDate.now()));
        dealService.save(deal);

        Customer customer = customerService.getOne(deal.getCustomerId());
        emailService.sendMail(customer.getCustomerEmail(),"Thông báo xác nhận đơn hàng",
        "Đơn hàng của " +customer.getCustomerName()+" đã được xác nhận");

        String cancelUrl = Utils.getBaseURL(request) + "/" + URL_PAYPAL_CANCEL;
        String successUrl = Utils.getBaseURL(request) + "/" + URL_PAYPAL_SUCCESS;
        try {

            Payment payment = paypalService.createPayment(
                    deposit,
                    "USD",
                    PaypalPaymentMethod.paypal,
                    PaypalPaymentIntent.sale,
                    "payment description",
                    cancelUrl,
                    successUrl);
            for(Links links : payment.getLinks()){
                if(links.getRel().equals("approval_url")){

                    return "redirect:" + links.getHref();
                }
            }
        } catch (PayPalRESTException e) {
            log.error(e.getMessage());
        }

        return "redirect:/";
    }
    @GetMapping(URL_PAYPAL_CANCEL)
    public String cancelPay(){
        return "redirect:/";
    }
    @GetMapping(URL_PAYPAL_SUCCESS)
    public String successPay(@RequestParam("paymentId") String paymentId,
                             @RequestParam("PayerID") String payerId){
        try {
            Payment payment = paypalService.executePayment(paymentId, payerId);
            if(payment.getState().equals("approved")){
                return "redirect:/";
            }
        } catch (PayPalRESTException e) {
            log.error(e.getMessage());
        }
        return "redirect:/";
    }
}
