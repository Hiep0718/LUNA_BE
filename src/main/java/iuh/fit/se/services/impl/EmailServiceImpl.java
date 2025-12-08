package iuh.fit.se.services.impl;

import iuh.fit.se.entities.Orders;
import iuh.fit.se.entities.OrderDetails;
import iuh.fit.se.services.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;

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
    public void sendOrderConfirmationEmail(Orders order) {
        try {
            String htmlContent = buildOrderEmailContent(order);

            // Send HTML email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailFrom, mailFromName);
            helper.setTo(order.getUser().getEmail());
            helper.setSubject("Xác Nhận Đơn Hàng #" + order.getId() + " - LUNA Shop");
            helper.setText(htmlContent, true); // true = isHtml

            mailSender.send(message);
            log.info("Order confirmation email sent successfully to: {}", order.getUser().getEmail());

        } catch (MessagingException e) {
            log.error("Failed to send order confirmation email to: {} - Error: {}",
                    order.getUser().getEmail(), e.getMessage(), e);
            // Không throw exception để không ảnh hưởng đến việc tạo đơn hàng
        } catch (Exception e) {
            log.error("Unexpected error while sending email: {}", e.getMessage(), e);
        }
    }

    private String buildOrderEmailContent(Orders order) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"vi\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>Xác Nhận Đơn Hàng</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: Arial, sans-serif; color: #333; }\n");
        html.append("        .container { max-width: 600px; margin: 0 auto; padding: 20px; }\n");
        html.append("        .header { background-color: #7c3aed; color: white; padding: 20px; text-align: center; border-radius: 5px; }\n");
        html.append("        .header h1 { margin: 0; font-size: 24px; }\n");
        html.append("        .content { background-color: #f9fafb; padding: 20px; margin: 20px 0; border-radius: 5px; }\n");
        html.append("        .section { margin: 20px 0; }\n");
        html.append("        .section-title { font-size: 18px; font-weight: bold; color: #7c3aed; margin-bottom: 10px; }\n");
        html.append("        .order-info { display: table; width: 100%; margin: 10px 0; }\n");
        html.append("        .info-row { display: table-row; }\n");
        html.append("        .info-label { display: table-cell; width: 40%; font-weight: bold; padding: 8px; border-bottom: 1px solid #e5e7eb; }\n");
        html.append("        .info-value { display: table-cell; width: 60%; padding: 8px; border-bottom: 1px solid #e5e7eb; }\n");
        html.append("        .items-table { width: 100%; border-collapse: collapse; margin: 10px 0; }\n");
        html.append("        .items-table th { background-color: #7c3aed; color: white; padding: 12px; text-align: left; }\n");
        html.append("        .items-table td { padding: 12px; border-bottom: 1px solid #e5e7eb; }\n");
        html.append("        .items-table tr:nth-child(even) { background-color: #f3f4f6; }\n");
        html.append("        .total-row { font-size: 18px; font-weight: bold; color: #7c3aed; padding: 12px; text-align: right; }\n");
        html.append("        .footer { text-align: center; color: #6b7280; font-size: 14px; margin-top: 20px; border-top: 1px solid #e5e7eb; padding-top: 20px; }\n");
        html.append("        .button { display: inline-block; background-color: #7c3aed; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; margin: 10px 0; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");

        // Header
        html.append("    <div class=\"container\">\n");
        html.append("        <div class=\"header\">\n");
        html.append("            <h1>✓ Đơn Hàng Được Xác Nhận</h1>\n");
        html.append("        </div>\n");

        // Main content
        html.append("        <div class=\"content\">\n");
        html.append("            <p>Xin chào <strong>").append(order.getUser().getFullName()).append("</strong>,</p>\n");
        html.append("            <p>Cảm ơn bạn đã đặt hàng tại LUNA Shop! Đơn hàng của bạn đã được xác nhận và đang được chuẩn bị gửi đi.</p>\n");

        // Order details
        html.append("            <div class=\"section\">\n");
        html.append("                <div class=\"section-title\">Thông Tin Đơn Hàng</div>\n");
        html.append("                <div class=\"order-info\">\n");
        html.append("                    <div class=\"info-row\">\n");
        html.append("                        <div class=\"info-label\">Mã Đơn Hàng:</div>\n");
        html.append("                        <div class=\"info-value\">#").append(order.getId()).append("</div>\n");
        html.append("                    </div>\n");
        html.append("                    <div class=\"info-row\">\n");
        html.append("                        <div class=\"info-label\">Ngày Đặt:</div>\n");
        html.append("                        <div class=\"info-value\">").append(formatDate(order.getOrderDate())).append("</div>\n");
        html.append("                    </div>\n");
        html.append("                    <div class=\"info-row\">\n");
        html.append("                        <div class=\"info-label\">Trạng Thái:</div>\n");
        html.append("                        <div class=\"info-value\"><strong>").append(getStatusLabel(order.getStatus())).append("</strong></div>\n");
        html.append("                    </div>\n");
        html.append("                </div>\n");
        html.append("            </div>\n");

        // Shipping address
        html.append("            <div class=\"section\">\n");
        html.append("                <div class=\"section-title\">Địa Chỉ Giao Hàng</div>\n");
        html.append("                <p>\n");
        html.append(order.getUser().getFullName()).append("<br>\n");
        html.append(order.getUser().getPhone()).append("<br>\n");
        html.append(order.getAddress().getStreet()).append(", ").append(order.getAddress().getDistrict()).append(", ").append(order.getAddress().getProvince()).append("\n");
        html.append("                </p>\n");
        html.append("            </div>\n");

        // Order items
        html.append("            <div class=\"section\">\n");
        html.append("                <div class=\"section-title\">Chi Tiết Sản Phẩm</div>\n");
        html.append("                <table class=\"items-table\">\n");
        html.append("                    <thead>\n");
        html.append("                        <tr>\n");
        html.append("                            <th>Sản Phẩm</th>\n");
        html.append("                            <th>Đơn Giá</th>\n");
        html.append("                            <th>Số Lượng</th>\n");
        html.append("                            <th>Tổng Cộng</th>\n");
        html.append("                        </tr>\n");
        html.append("                    </thead>\n");
        html.append("                    <tbody>\n");

        // Loop through order details
        for (OrderDetails detail : order.getOrderDetails()) {
            double itemTotal = detail.getPrice() * detail.getQuantity();
            html.append("                        <tr>\n");
            html.append("                            <td>").append(detail.getProduct().getName()).append("</td>\n");
            html.append("                            <td>").append(formatPrice(detail.getPrice())).append("</td>\n");
            html.append("                            <td>").append(detail.getQuantity()).append("</td>\n");
            html.append("                            <td>").append(formatPrice(itemTotal)).append("</td>\n");
            html.append("                        </tr>\n");
        }

        html.append("                    </tbody>\n");
        html.append("                </table>\n");
        html.append("            </div>\n");

        // Summary
        html.append("            <div class=\"section\">\n");
        html.append("                <div class=\"order-info\">\n");
        html.append("                    <div class=\"info-row\">\n");
        html.append("                        <div class=\"info-label\">Tạm Tính:</div>\n");
        html.append("                        <div class=\"info-value\">").append(formatPrice(order.getTotal() - order.getShippingFee() - order.getTax())).append("</div>\n");
        html.append("                    </div>\n");
        html.append("                    <div class=\"info-row\">\n");
        html.append("                        <div class=\"info-label\">Phí Vận Chuyển:</div>\n");
        html.append("                        <div class=\"info-value\">").append(formatPrice(order.getShippingFee())).append("</div>\n");
        html.append("                    </div>\n");
        html.append("                    <div class=\"info-row\">\n");
        html.append("                        <div class=\"info-label\">Thuế:</div>\n");
        html.append("                        <div class=\"info-value\">").append(formatPrice(order.getTax())).append("</div>\n");
        html.append("                    </div>\n");
        html.append("                </div>\n");
        html.append("                <div class=\"total-row\">\n");
        html.append("                    Tổng Cộng: ").append(formatPrice(order.getTotal())).append("\n");
        html.append("                </div>\n");
        html.append("            </div>\n");

        // Call to action
        html.append("            <div style=\"text-align: center;\">\n");
        html.append("                <a href=\"https://luna-shop.com/orders/").append(order.getId()).append("\" class=\"button\">Xem Chi Tiết Đơn Hàng</a>\n");
        html.append("            </div>\n");

        html.append("        </div>\n");

        // Footer
        html.append("        <div class=\"footer\">\n");
        html.append("            <p>LUNA Shop - Cửa hàng mua sắm trực tuyến</p>\n");
        html.append("            <p>Nếu có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi qua support@lunashop.com</p>\n");
        html.append("        </div>\n");
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>\n");

        return html.toString();
    }

    private String formatPrice(double price) {
        return String.format("%.0f đ", price);
    }

    private String formatDate(Object date) {
        if (date == null) return "N/A";
        return date.toString().substring(0, 10);
    }

    private String getStatusLabel(String status) {
        return switch (status) {
            case "PENDING" -> "Chờ xác nhận";
            case "CONFIRMED" -> "Đã xác nhận";
            case "SHIPPED" -> "Đang giao";
            case "DELIVERED" -> "Đã giao";
            case "CANCELLED" -> "Đã hủy";
            default -> status;
        };
    }
}
