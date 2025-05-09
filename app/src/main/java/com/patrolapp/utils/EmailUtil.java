package com.patrolapp.utils;

import android.content.Context;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailUtil {

    /**
     * Utility method to send simple HTML email
     * @param session
     * @param toEmail
     * @param subject
     * @param body
     */
    public static void sendEmail(Session session, String toEmail, String subject, String body){ // no attach example
        try
        {
            MimeMessage msg = new MimeMessage(session);
            //set message headers
            msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
            msg.addHeader("format", "flowed");
            msg.addHeader("Content-Transfer-Encoding", "8bit");

            msg.setFrom(new InternetAddress("no_reply@example.com", "NoReply-JD"));
            msg.setReplyTo(InternetAddress.parse("no_reply@example.com", false));
            msg.setSubject(subject, "UTF-8");
            msg.setText(body, "UTF-8");
            msg.setSentDate(new Date());

            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
            System.out.println("Message is ready");
            Transport.send(msg);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Utility method to send attach txt HTML email
     * @param context
     * @param session
     * @param fromEmail
     * @param toEmail
     * @param subject
     * @param body
     * @param filename
     */
    public static void sendAttachmentEmail(Context context, Session session, String fromEmail, String toEmail, String subject, String body, String filename){
        try{
            Thread.currentThread().setContextClassLoader(javax.mail.Message.class.getClassLoader());

            MimeMessage msg = new MimeMessage(session);
            msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
            msg.addHeader("format", "flowed");
            msg.addHeader("Content-Transfer-Encoding", "8bit");

            msg.setFrom(new InternetAddress(fromEmail, "Maria Temi"));
            msg.setReplyTo(InternetAddress.parse("no_reply@mail.com", false));

            msg.setSubject(subject, "UTF-8");

            msg.setSentDate(new Date());
            //msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            BodyPart messageBodyPart = new MimeBodyPart();

            messageBodyPart.setText(body);

            Multipart multipart = new MimeMultipart();

            multipart.addBodyPart(messageBodyPart);

            messageBodyPart = new MimeBodyPart();
            String filePath = context.getFilesDir() + "/" + filename; // default internal storage

            DataSource source = new FileDataSource(filePath);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(filename);
            multipart.addBodyPart(messageBodyPart);

            msg.setContent(multipart);
            Transport.send(msg);

        }catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
