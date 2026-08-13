package com.bookstore.service;

import com.bookstore.domain.User;
import com.bookstore.dto.request.ContactRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.Year;

/**
 * EmailService — sends transactional emails using Spring Mail / JavaMailSender.
 *
 * <p>All send methods are {@link Async} so they never block the calling thread.
 * If no SMTP credentials are configured the service logs a debug message and
 * returns immediately — registration is never affected.
 *
 * <p>To enable email, set the following in application-dev.yml (or as env vars):
 * <pre>
 *   spring.mail.username: YOUR_MAILTRAP_USERNAME
 *   spring.mail.password: YOUR_MAILTRAP_PASSWORD
 * </pre>
 * For local testing, use Mailtrap (https://mailtrap.io).
 * For production, use Gmail App Password or a transactional email provider.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    // ── Welcome Email ─────────────────────────────────────────────────────────

    /**
     * Sends a welcome email to a newly registered user.
     * Fire-and-forget — failures are logged but never propagated.
     *
     * @param user the newly created user
     */
    @Async
    public void sendWelcomeEmail(User user) {
        if (mailUsername == null || mailUsername.isBlank()) {
            log.debug("Email not configured — skipping welcome email for '{}'", user.getEmail());
            return;
        }
        try {
            String to      = user.getEmail();
            String subject = "Welcome to BookStore — Happy Reading! 📚";
            String body    = buildWelcomeHtml(user.getFirstName());
            sendHtmlEmail(mailUsername, to, subject, body);
            log.info("Welcome email sent to '{}'", to);
        } catch (Exception ex) {
            // Email failures must never break registration
            log.warn("Failed to send welcome email to '{}': {}", user.getEmail(), ex.getMessage());
        }
    }

    // ── Contact Email ─────────────────────────────────────────────────────────

    /**
     * Forwards a Contact Us form submission to the site owner's inbox.
     * Fire-and-forget — runs on a background thread so the HTTP response is immediate.
     *
     * @param req the validated contact form payload
     */
    public void sendContactEmail(ContactRequest req) {
        if (mailUsername == null || mailUsername.isBlank()) {
            log.debug("Email not configured — skipping contact email from '{}'", req.getEmail());
            return;
        }
        try {
            String to      = "teambookstore26@gmail.com";
            String subject = "[BookStore Contact] " +
                    (req.getSubject() != null && !req.getSubject().isBlank()
                            ? req.getSubject().trim()
                            : "(no subject)");
            String body    = buildContactHtml(req);
            sendHtmlEmail(mailUsername, to, subject, body);
            log.info("Contact email forwarded from '{}' to '{}'", req.getEmail(), to);
        } catch (Exception ex) {
            log.warn("Failed to send contact email from '{}': {}", req.getEmail(), ex.getMessage());
            // Re-throw so the controller can return a 500 to the client instead of false-success
            throw new RuntimeException("Failed to deliver contact message. Please try again later.", ex);
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void sendHtmlEmail(String from, String to, String subject, String htmlBody)
            throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        mailSender.send(message);
    }

    private String buildContactHtml(ContactRequest req) {
        int year = Year.now().getValue();
        String safeSubject = (req.getSubject() != null && !req.getSubject().isBlank())
                ? escapeHtml(req.getSubject().trim()) : "<em style=\"color:#9e9e9e\">No subject</em>";
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                  <title>BookStore — Contact Message</title>
                </head>
                <body style="margin:0;padding:0;background:#0f0f0f;font-family:'Segoe UI',Helvetica,Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#0f0f0f;padding:40px 0;">
                    <tr>
                      <td align="center">
                        <table width="600" cellpadding="0" cellspacing="0"
                               style="background:#1e1e1e;border-radius:12px;overflow:hidden;
                                      border:1px solid rgba(255,255,255,0.08);">

                          <!-- Header -->
                          <tr>
                            <td style="background:linear-gradient(135deg,#1565c0 0%%,#0d47a1 100%%);
                                        padding:28px 40px;text-align:center;">
                              <h1 style="margin:0;color:#ffffff;font-size:24px;font-weight:700;">
                                📚 BookStore — New Contact Message
                              </h1>
                            </td>
                          </tr>

                          <!-- Body -->
                          <tr>
                            <td style="padding:32px 40px;">

                              <!-- Meta -->
                              <table width="100%%" cellpadding="0" cellspacing="0" style="margin-bottom:24px;">
                                <tr>
                                  <td style="padding:10px 14px;background:#252525;border-radius:8px;
                                              border-left:3px solid #2979ff;margin-bottom:8px;">
                                    <p style="margin:0;color:#9e9e9e;font-size:12px;text-transform:uppercase;
                                               letter-spacing:0.5px;">From</p>
                                    <p style="margin:4px 0 0;color:#e0e0e0;font-size:15px;font-weight:600;">
                                      %s &lt;%s&gt;
                                    </p>
                                  </td>
                                </tr>
                                <tr><td style="height:8px;"></td></tr>
                                <tr>
                                  <td style="padding:10px 14px;background:#252525;border-radius:8px;
                                              border-left:3px solid #2979ff;">
                                    <p style="margin:0;color:#9e9e9e;font-size:12px;text-transform:uppercase;
                                               letter-spacing:0.5px;">Subject</p>
                                    <p style="margin:4px 0 0;color:#e0e0e0;font-size:15px;">%s</p>
                                  </td>
                                </tr>
                              </table>

                              <!-- Message -->
                              <p style="margin:0 0 8px;color:#9e9e9e;font-size:12px;
                                         text-transform:uppercase;letter-spacing:0.5px;">Message</p>
                              <div style="background:#252525;border-radius:8px;padding:16px 20px;
                                           border:1px solid rgba(255,255,255,0.06);">
                                <p style="margin:0;color:#e0e0e0;font-size:15px;line-height:1.75;
                                           white-space:pre-wrap;">%s</p>
                              </div>

                            </td>
                          </tr>

                          <!-- Footer -->
                          <tr>
                            <td style="background:#161616;padding:16px 40px;text-align:center;
                                        border-top:1px solid rgba(255,255,255,0.06);">
                              <p style="margin:0;color:#424242;font-size:12px;">
                                Sent from the BookStore Contact Us form &nbsp;·&nbsp; © %d Ramkumar K.
                              </p>
                            </td>
                          </tr>

                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(
                        escapeHtml(req.getName().trim()),
                        escapeHtml(req.getEmail().trim()),
                        safeSubject,
                        escapeHtml(req.getMessage().trim()),
                        year);
    }

    /** Minimal HTML escaping to prevent injection in email bodies. */
    private String escapeHtml(String input) {
        if (input == null) return "";
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String buildWelcomeHtml(String firstName) {
        int year = Year.now().getValue();
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                  <title>Welcome to BookStore</title>
                </head>
                <body style="margin:0;padding:0;background:#0f0f0f;font-family:'Segoe UI',Helvetica,Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#0f0f0f;padding:40px 0;">
                    <tr>
                      <td align="center">
                        <table width="600" cellpadding="0" cellspacing="0"
                               style="background:#1e1e1e;border-radius:12px;overflow:hidden;
                                      border:1px solid rgba(255,255,255,0.08);">

                          <!-- Header -->
                          <tr>
                            <td style="background:linear-gradient(135deg,#1565c0 0%%,#0d47a1 100%%);
                                        padding:32px 40px;text-align:center;">
                              <h1 style="margin:0;color:#ffffff;font-size:28px;font-weight:700;
                                         letter-spacing:-0.5px;">
                                📚 BookStore
                              </h1>
                              <p style="margin:8px 0 0;color:rgba(255,255,255,0.75);font-size:14px;">
                                Your destination for great books
                              </p>
                            </td>
                          </tr>

                          <!-- Body -->
                          <tr>
                            <td style="padding:40px 40px 32px;">
                              <h2 style="margin:0 0 16px;color:#e0e0e0;font-size:22px;font-weight:600;">
                                Welcome, %s! 🎉
                              </h2>
                              <p style="margin:0 0 16px;color:#9e9e9e;font-size:15px;line-height:1.7;">
                                Thank you for joining <strong style="color:#e0e0e0;">BookStore</strong>.
                                Your account has been created and you're all set to start exploring thousands
                                of books across genres, languages, and formats.
                              </p>
                              <p style="margin:0 0 24px;color:#9e9e9e;font-size:15px;line-height:1.7;">
                                Here's what you can do with your new account:
                              </p>

                              <!-- Feature list -->
                              <table width="100%%" cellpadding="0" cellspacing="0" style="margin-bottom:24px;">
                                <tr>
                                  <td style="padding:10px 12px;background:#252525;border-radius:8px;
                                              border-left:3px solid #2979ff;">
                                    <p style="margin:0;color:#e0e0e0;font-size:14px;">
                                      🔖 <strong>Wishlist</strong> — Save books you want to read later
                                    </p>
                                  </td>
                                </tr>
                                <tr><td style="height:8px;"></td></tr>
                                <tr>
                                  <td style="padding:10px 12px;background:#252525;border-radius:8px;
                                              border-left:3px solid #2979ff;">
                                    <p style="margin:0;color:#e0e0e0;font-size:14px;">
                                      ⭐ <strong>Reviews</strong> — Share your thoughts on books you've read
                                    </p>
                                  </td>
                                </tr>
                                <tr><td style="height:8px;"></td></tr>
                                <tr>
                                  <td style="padding:10px 12px;background:#252525;border-radius:8px;
                                              border-left:3px solid #2979ff;">
                                    <p style="margin:0;color:#e0e0e0;font-size:14px;">
                                      ✍️ <strong>My Writers</strong> — Follow your favourite authors
                                    </p>
                                  </td>
                                </tr>
                              </table>

                              <!-- CTA -->
                              <div style="text-align:center;margin:32px 0;">
                                <a href="%s"
                                   style="display:inline-block;background:#2979ff;color:#ffffff;
                                          text-decoration:none;padding:14px 32px;border-radius:8px;
                                          font-weight:600;font-size:15px;letter-spacing:0.2px;">
                                  Start Exploring BookStore →
                                </a>
                              </div>

                              <p style="margin:0;color:#616161;font-size:13px;line-height:1.65;
                                         border-top:1px solid rgba(255,255,255,0.08);padding-top:20px;">
                                If you did not create this account, you can safely ignore this email.
                              </p>
                            </td>
                          </tr>

                          <!-- Footer -->
                          <tr>
                            <td style="background:#161616;padding:20px 40px;text-align:center;
                                        border-top:1px solid rgba(255,255,255,0.06);">
                              <p style="margin:0;color:#424242;font-size:12px;">
                                © %d BookStore — A portfolio project by Ramkumar K.<br/>
                                All book data and content are for demonstration purposes only.
                              </p>
                            </td>
                          </tr>

                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(firstName, frontendUrl, year);
    }
}
