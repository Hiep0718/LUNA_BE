package iuh.fit.se.services.impl;

import iuh.fit.se.entities.Orders;
import iuh.fit.se.entities.OrderDetails;
import iuh.fit.se.services.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import org.springframework.scheduling.annotation.Async;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String mailFrom;

    @Value("${app.mail.from-name}")
    private String mailFromName;

    @Autowired
    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    @Async
    public void sendOrderConfirmationEmail(Orders order) {
        try {
            log.info("Starting to send email for order: {}", order.getId()); // Log để debug
            // Tiêu đề email hấp dẫn hơn
            String subject = String.format("🎉 Đặt hàng thành công! Mã đơn #%s - LUNA Shop", order.getId());
            String htmlContent = buildOrderEmailContent(order);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailFrom, mailFromName);
            helper.setTo(order.getUser().getEmail());
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Order confirmation email sent to: {}", order.getUser().getEmail());

        } catch (MessagingException e) {
            log.error("Failed to send email: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage(), e);
        }
    }

    private String buildOrderEmailContent(Orders order) {
        StringBuilder html = new StringBuilder();
        DecimalFormat df = new DecimalFormat("#,###"); // Format tiền tệ đẹp hơn: 10,000 thay vì 10000

        // COLORS CONFIG
        String primaryColor = "#7c3aed"; // Màu tím chủ đạo
        String bgColor = "#f3f4f6";      // Màu nền xám nhạt
        String textColor = "#1f2937";    // Màu chữ đen xám

        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0' />");
        html.append("<style>");
        // CSS Reset & Base Styles
        html.append("body { margin: 0; padding: 0; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: " + bgColor + "; font-size: 14px; line-height: 1.6; color: " + textColor + "; }");
        html.append(".email-container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        html.append(".email-card { background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.05); }");

        // Header Style
        html.append(".header { background-color: " + primaryColor + "; padding: 30px 20px; text-align: center; color: white; }");
        html.append(".header h1 { margin: 0; font-size: 24px; font-weight: 700; letter-spacing: 1px; }");
        html.append(".header-icon { font-size: 40px; margin-bottom: 10px; display: block; }");

        // Content Style
        html.append(".content { padding: 30px; }");
        html.append(".greeting { font-size: 18px; margin-bottom: 20px; }");
        html.append(".order-id-badge { background-color: #f3e8ff; color: " + primaryColor + "; padding: 5px 10px; border-radius: 4px; font-weight: bold; font-family: monospace; }");

        // Info Grid
        html.append(".info-grid { display: table; width: 100%; margin-bottom: 25px; border-spacing: 0; }");
        html.append(".info-column { display: table-cell; width: 50%; vertical-align: top; padding-right: 10px; }");
        html.append(".info-label { font-size: 12px; text-transform: uppercase; color: #6b7280; margin-bottom: 5px; letter-spacing: 0.5px; }");
        html.append(".info-value { font-weight: 600; font-size: 15px; }");

        // Table Style
        html.append(".table-container { margin: 25px 0; width: 100%; border-collapse: collapse; }");
        html.append(".table-header th { text-align: left; padding: 12px; font-size: 12px; text-transform: uppercase; color: #6b7280; border-bottom: 2px solid #e5e7eb; }");
        html.append(".table-row td { padding: 15px 12px; border-bottom: 1px solid #f3f4f6; vertical-align: middle; }");
        html.append(".product-name { font-weight: 600; color: " + textColor + "; display: block; }");
        html.append(".product-qty { font-size: 12px; color: #6b7280; }");

        // Summary Section
        html.append(".summary-section { width: 100%; border-top: 2px solid #e5e7eb; padding-top: 15px; margin-top: 10px; }");
        html.append(".summary-row { display: flex; justify-content: space-between; margin-bottom: 8px; }");
        html.append(".summary-row.total { font-size: 18px; font-weight: 800; color: " + primaryColor + "; margin-top: 10px; border-top: 1px dashed #e5e7eb; padding-top: 10px; }");

        // Button
        html.append(".btn-container { text-align: center; margin: 30px 0 10px; }");
        html.append(".btn { background-color: " + primaryColor + "; color: white; padding: 14px 28px; text-decoration: none; border-radius: 50px; font-weight: bold; font-size: 16px; display: inline-block; transition: background 0.3s; }");
        html.append(".btn:hover { background-color: #6d28d9; }");

        // Footer
        html.append(".footer { text-align: center; margin-top: 30px; font-size: 12px; color: #9ca3af; }");
        html.append(".footer a { color: " + primaryColor + "; text-decoration: none; }");

        // Responsive Mobile
        html.append("@media only screen and (max-width: 600px) {");
        html.append(".email-container { width: 100% !important; padding: 10px !important; }");
        html.append(".content { padding: 20px !important; }");
        html.append(".header { padding: 20px !important; }");
        html.append("}");

        html.append("</style>");
        html.append("</head>");

        html.append("<body>");
        html.append("<div class='email-container'>");

        // --- CARD START ---
        html.append("<div class='email-card'>");

        // HEADER
        html.append("<div class='header'>");
        html.append("<span class='header-icon'>📦</span>");
        html.append("<h1>Cảm ơn bạn đã đặt hàng!</h1>");
        html.append("</div>");

        // BODY CONTENT
        html.append("<div class='content'>");

        // Greeting
        html.append("<p class='greeting'>Xin chào <strong>").append(order.getUser().getFullName()).append("</strong>, 👋</p>");
        html.append("<p>Đơn hàng của bạn tại LUNA Shop đã được tiếp nhận và đang trong quá trình xử lý.</p>");

        // Info Grid (Mã đơn & Ngày đặt)
        html.append("<div class='info-grid'>");
        html.append("<div class='info-column'>");
        html.append("<div class='info-label'>Mã đơn hàng</div>");
        html.append("<div class='info-value'><span class='order-id-badge'>#").append(order.getId()).append("</span></div>");
        html.append("</div>");
        html.append("<div class='info-column' style='text-align: right;'>");
        html.append("<div class='info-label'>Ngày đặt hàng</div>");
        html.append("<div class='info-value'>").append(formatDate(order.getOrderDate())).append("</div>");
        html.append("</div>");
        html.append("</div>");

        // Address Section
        html.append("<div style='background-color: #f9fafb; padding: 15px; border-radius: 8px; margin-bottom: 20px;'>");
        html.append("<div class='info-label'>Địa chỉ giao hàng</div>");
        html.append("<div class='info-value'>").append(order.getAddress().getStreet())
                .append(", ").append(order.getAddress().getDistrict())
                .append(", ").append(order.getAddress().getProvince()).append("</div>");
        html.append("<div style='margin-top: 5px; color: #6b7280; font-size: 13px;'>SĐT: ").append(order.getUser().getPhone()).append("</div>");
        html.append("</div>");

        // PRODUCT TABLE
        html.append("<table class='table-container'>");
        html.append("<tr class='table-header'><th>Sản phẩm</th><th style='text-align: right;'>Thành tiền</th></tr>");

        for (OrderDetails detail : order.getOrderDetails()) {
            double itemTotal = detail.getPrice() * detail.getQuantity();
            html.append("<tr class='table-row'>");
            html.append("<td>");
            html.append("<span class='product-name'>").append(detail.getProduct().getName()).append("</span>");
            html.append("<span class='product-qty'>x").append(detail.getQuantity()).append("</span>");
            html.append("</td>");
            html.append("<td style='text-align: right; font-weight: 500;'>").append(df.format(itemTotal)).append(" đ</td>");
            html.append("</tr>");
        }
        html.append("</table>");

        // SUMMARY (Tổng tiền) - Sử dụng Table thay vì Div để căn chỉnh tốt hơn trong email
        html.append("<table width='100%'>");
        html.append("<tr><td style='padding: 5px 0; color: #6b7280;'>Tạm tính</td><td style='text-align: right; font-weight: 500;'>").append(df.format(order.getTotal() - order.getShippingFee() - order.getTax())).append(" đ</td></tr>");
        html.append("<tr><td style='padding: 5px 0; color: #6b7280;'>Phí vận chuyển</td><td style='text-align: right; font-weight: 500;'>").append(df.format(order.getShippingFee())).append(" đ</td></tr>");
        if(order.getTax() > 0) {
            html.append("<tr><td style='padding: 5px 0; color: #6b7280;'>Thuế</td><td style='text-align: right; font-weight: 500;'>").append(df.format(order.getTax())).append(" đ</td></tr>");
        }
        html.append("<tr><td style='padding-top: 10px; border-top: 1px dashed #e5e7eb; font-weight: 700; color: "+primaryColor+"; font-size: 16px;'>Tổng thanh toán</td><td style='padding-top: 10px; border-top: 1px dashed #e5e7eb; text-align: right; font-weight: 700; color: "+primaryColor+"; font-size: 18px;'>").append(df.format(order.getTotal())).append(" đ</td></tr>");
        html.append("</table>");

        // BUTTON CTA
        html.append("<div class='btn-container'>");
        html.append("<a href='https://luna-shop.com/orders/").append(order.getId()).append("' class='btn'>Theo Dõi Đơn Hàng</a>");
        html.append("</div>");

        html.append("</div>"); // End Content
        html.append("</div>"); // End Card
        // --- CARD END ---

        // FOOTER
        html.append("<div class='footer'>");
        html.append("<p>&copy; 2025 LUNA Shop. All rights reserved.</p>");
        html.append("<p>Đây là email tự động, vui lòng không trả lời email này.</p>");
        html.append("<p><a href='#'>Trung tâm trợ giúp</a> | <a href='#'>Chính sách bảo mật</a></p>");
        html.append("</div>");

        html.append("</div>"); // End Container
        html.append("</body></html>");

        return html.toString();
    }

    // Helper để format ngày tháng đơn giản
    private String formatDate(Object date) {
        if (date == null) return "";
        // Giả sử date là LocalDateTime, nếu là Date thường thì format khác
        return date.toString().replace("T", " ").substring(0, 16);
    }
}