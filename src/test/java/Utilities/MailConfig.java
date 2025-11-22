package Utilities;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.FileNotFoundException;
import java.util.Properties;

import static Base.Setup.props;
import static Listeners.Redipae_Listeners.attachmentflag;
import static Listeners.Redipae_Listeners.fileName;
import static Utilities.Constants.body;
import static Utilities.Utils.generatedateandtime;
import static Utilities.Utils.screenshotName;

/**
 * MailConfig Class - Handles sending email reports with test results
 * 
 * PURPOSE: This class sends automated email notifications after test execution.
 * It attaches the test report (HTML) and optionally screenshots if tests failed.
 * 
 * WHY THIS CLASS EXISTS:
 * - Automates test result distribution to stakeholders
 * - Sends reports without manual intervention
 * - Attaches test reports and failure screenshots
 * - Keeps team informed of test execution status
 * 
 * HOW IT WORKS:
 * 1. Configures SMTP email server settings (Gmail, etc.)
 * 2. Creates email session with authentication
 * 3. Builds email message with recipients, subject, body
 * 4. Attaches test report HTML file
 * 5. Conditionally attaches screenshot if test failed
 * 6. Sends email via SMTP
 * 
 * EMAIL SERVER SUPPORT:
 * - Gmail (smtp.gmail.com) - configured in this code
 * - Other SMTP servers can be configured via Config.properties
 * 
 * SECURITY:
 * - Uses SSL/TLS encryption for secure email transmission
 * - Requires sender email and password (app password for Gmail)
 * - Credentials stored in Config.properties (should be secured)
 * 
 * LIBRARY USED: JavaMail API (javax.mail)
 */
public class MailConfig {

    /**
     * Sends email with test execution report and optional screenshots
     * 
     * WHAT THIS METHOD DOES:
     * 1. Configures SMTP email server settings
     * 2. Creates authenticated email session
     * 3. Builds email message with:
     *    - From address (sender)
     *    - To addresses (recipients)
     *    - Subject line
     *    - Email body text
     *    - Test report HTML attachment
     *    - Screenshot attachment (if test failed)
     * 4. Sends email via SMTP
     * 
     * WHY THIS METHOD:
     * - Centralizes email sending logic
     * - Reusable across test suites
     * - Handles all email configuration in one place
     * 
     * WHEN IT'S CALLED:
     * - After all tests complete (@AfterSuite in Setup.java)
     * - Only if EmailMode = "true" in Config.properties
     * 
     * @throws MessagingException if email sending fails (server error, auth failure, etc.)
     * @throws FileNotFoundException if report file or screenshot not found
     */
    public static void sendMailReport() throws MessagingException, FileNotFoundException {
        // ========== STEP 1: CONFIGURE SMTP SERVER SETTINGS ==========
        // Properties object stores email server configuration
        Properties properties = new Properties();
        
        // SMTP Host: Email server address (e.g., "smtp.gmail.com")
        // WHY: Tells JavaMail which email server to connect to
        properties.put("mail.smtp.host",props.get("Host"));
        
        // SSL Socket Factory Port: Port for SSL connection (465 = Gmail SSL port)
        // WHY: Gmail requires SSL on port 465 for secure connection
        properties.put("mail.smtp.socketFactory.port", "465");
        
        // SSL Socket Factory Class: Uses SSL for encrypted connection
        // WHY: Encrypts email transmission to prevent interception
        properties.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
        
        // SMTP Authentication: Enable login required
        // WHY: Email servers require authentication to prevent spam
        properties.put("mail.smtp.auth", "true");
        
        // SMTP Port: Port number for email server (587 = Gmail TLS port)
        // WHY: Different ports for different connection types (SSL vs TLS)
        properties.put("mail.smtp.port", props.get("Port"));
        
        // STARTTLS: Enable TLS encryption upgrade
        // WHY: Upgrades plain connection to encrypted TLS connection
        // NOTE: Duplicate entries below (can be removed, but harmless)
        properties.put("mail.smtp.starttls.enable", "true");
        
        // SSL Protocol: Use TLS version 1.2
        // WHY: TLS 1.2 is secure and widely supported (TLS 1.0/1.1 are deprecated)
        // NOTE: Duplicate entry (can be removed)
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
        
        // SSL Ciphers: Specific encryption cipher suite
        // WHY: Ensures strong encryption algorithm is used
        // NOTE: Duplicate entry (can be removed)
        properties.put("mail.smtp.ssl.ciphers", "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");
        
        // Duplicate entries (can be removed but don't cause issues)
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
        properties.put("mail.smtp.ssl.ciphers", "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");
        properties.put("mail.smtp.starttls.enable", "true");
        // ========== STEP 2: CREATE AUTHENTICATED EMAIL SESSION ==========
        // Session: Represents connection to email server
        // WHY: Needed to send emails - manages connection and authentication
        Session session = Session.getDefaultInstance(properties,
                // Authenticator: Provides credentials for email server login
                // WHY: Email servers require username/password to send emails
                new javax.mail.Authenticator() {
                    /**
                     * Provides email server login credentials
                     * 
                     * WHAT THIS DOES:
                     * - Returns sender email and password for authentication
                     * - Called automatically by JavaMail when server requests credentials
                     * 
                     * WHY ANONYMOUS CLASS:
                     * - Simple way to provide credentials without creating separate class
                     * - Credentials come from Config.properties
                     * 
                     * SECURITY NOTE:
                     * - For Gmail, use "App Password" not regular password
                     * - Regular password won't work with 2FA enabled
                     * - Generate App Password: Google Account → Security → App Passwords
                     * 
                     * @return PasswordAuthentication with sender email and password
                     */
                    protected PasswordAuthentication getPasswordAuthentication() {
                        // Get sender email and password from Config.properties
                        // WHY: Keeps credentials configurable, not hardcoded
                        return new PasswordAuthentication(
                                props.get("Sendermail"),      // Sender email address
                                props.get("Senderpassword")   // Sender password (App Password for Gmail)
                        );

                    }
                });
        
        // Disable debug mode (set to true to see detailed SMTP communication)
        // WHY: Reduces console output, set to true only for troubleshooting
        session.setDebug(false);
        try {
            // ========== STEP 3: CREATE EMAIL MESSAGE ==========
            // MimeMessage: Represents the email message
            // WHY: Container for all email content (to, from, subject, body, attachments)
            Message message = new MimeMessage(session);
            
            // Set sender email address (FROM field)
            // WHY: Recipients see who sent the email
            message.setFrom(new InternetAddress(props.get("Sendermail")));
            
            // ========== STEP 4: SET RECIPIENTS ==========
            // Create array of recipient email addresses
            // WHY: Can send to multiple recipients at once
            // NOTE: Currently supports 2 recipients (can be extended)
            String[] recipients = {
                    props.get("Reciptents1"),  // First recipient from Config.properties
                    props.get("Reciptents2"),  // Second recipient from Config.properties
            };
            
            // Convert String array to InternetAddress array
            // WHY: JavaMail requires InternetAddress objects, not plain strings
            InternetAddress[] recipientAddresses = new InternetAddress[recipients.length];
            for (int i = 0; i < recipients.length; i++) {
                recipientAddresses[i] = new InternetAddress(recipients[i]);
            }
            
            // Set recipients (TO field)
            // WHY: Specifies who receives the email
            message.setRecipients(Message.RecipientType.TO,recipientAddresses);
            
            // Set email subject line
            // WHY: Recipients see subject in inbox, helps identify email
            message.setSubject( props.get("Subject"));
            // ========== STEP 5: CREATE EMAIL BODY ==========
            // BodyPart 1: Email text body (the message content)
            // WHY: Contains the email message text (from Constants.body)
            BodyPart messageBodyPart1 = new MimeBodyPart();
            messageBodyPart1.setText(body);  // Body text from Constants.java

            // ========== STEP 6: ATTACH TEST REPORT ==========
            // BodyPart 2: Test report HTML file attachment
            // WHY: Recipients can open detailed test report in browser
            MimeBodyPart messageBodyPart2 = new MimeBodyPart();
            
            // Create file data source pointing to test report HTML file
            // WHY: JavaMail needs DataSource to read file for attachment
            DataSource source = new FileDataSource(props.get("TestReportspath")+fileName);
            
            // Set data handler to read file content
            // WHY: DataHandler manages file reading and attachment
            messageBodyPart2.setDataHandler(new DataHandler(source));
            
            // Set attachment filename (what recipients see when downloading)
            // WHY: Descriptive name helps identify attachment
            // Format: "Automation Test Report_2024-01-15_10:30:45_AM.html"
            messageBodyPart2.setFileName("Automation Test Report"+generatedateandtime()+".html");


            // ========== STEP 7: COMBINE ALL PARTS INTO MULTIPART MESSAGE ==========
            // Multipart: Container for multiple parts (body + attachments)
            // WHY: Email can have text body + multiple file attachments
            Multipart multipart = new MimeMultipart();
            
            // Add attachments first, then body
            // WHY: Order doesn't matter, but attachments typically listed first
            multipart.addBodyPart(messageBodyPart2);  // Test report attachment
            multipart.addBodyPart(messageBodyPart1);  // Email body text
            
            // ========== STEP 8: CONDITIONALLY ATTACH SCREENSHOT ==========
            // BodyPart 3: Screenshot attachment (only if test failed)
            // WHY: Helps debug failures - visual evidence of what went wrong
            MimeBodyPart messageBodyPart3= null;
            
            // Check if screenshot was captured (attachmentflag = true if test failed)
            // WHY: Only attach screenshot if there was a failure (saves email size)
            if(attachmentflag){
                // Create screenshot attachment
                messageBodyPart3 = new MimeBodyPart();
                
                // Create file data source pointing to screenshot file
                DataSource source2 = new FileDataSource(props.get("Screenshotpath") + screenshotName);
                
                // Set data handler to read screenshot file
                messageBodyPart3.setDataHandler(new DataHandler(source2));
                
                // Set screenshot filename
                // Format: "Screenshot_2024-01-15_10:30:45_AM.jpg"
                messageBodyPart3.setFileName("Screenshot "+generatedateandtime()+".jpg");
                
                // Add screenshot to email
                multipart.addBodyPart(messageBodyPart3);
            }
            
            // Set email content to multipart (body + attachments)
            // WHY: Tells JavaMail this email has multiple parts
            message.setContent(multipart);
            
            // ========== STEP 9: SEND EMAIL ==========
            // Transport.send(): Actually sends the email via SMTP
            // WHY: This is where email leaves your code and goes to email server
            // NOTE: This is a blocking call - waits for email to be sent
            Transport.send(message);
        } catch (MessagingException e) {
            // Wrap MessagingException in RuntimeException
            // WHY: Makes it easier to handle in calling code
            // MessagingException can occur due to:
            // - Invalid email addresses
            // - Authentication failure (wrong password)
            // - SMTP server connection issues
            // - Network problems
            throw new RuntimeException(e);
        }

    }

}
