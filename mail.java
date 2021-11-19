package org.webdriver.seleniumUI.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import javax.mail.*;
import java.io.IOException;
import java.util.Properties;

public class Mail {

    private  String host;
    private  String emailId;
    private  String emailPwd;
    private  String folder;
    private static Store store;
    private  String port;
    @Getter @Setter
    private String title;

    public Mail(String host, String emailId, String emailPwd, String folder, String port) {
        this.host = host;
        this.emailId = emailId;
        this.emailPwd = emailPwd;
        this.folder = folder;
        this.port = port;
    }

    public Mail(String emailPwd, String folder) {
        this.emailPwd = emailPwd;
        this.folder = folder;
    }

    // cisco = mail.cisco.com
    private void connectToMail() throws MessagingException {
        Properties properties = new Properties();
        properties.put("mail.imap.host", host);
        properties.put("mail.imap.port", port);
        properties.put("mail.imap.ssl.trust", "*");
        properties.put("mail.imap.starttls.enable", "true");
        Session emailSession = Session.getInstance(properties);
        store = emailSession.getStore("imaps");
        store.connect(host, emailId, emailPwd);
    }

    @SneakyThrows ({IOException.class, MessagingException.class})
    public String readInbox(Boolean delete)  {
        connectToMail();
        Folder emailFolder = store.getFolder(folder);
        emailFolder.open(Folder.READ_WRITE);
        Message[] messages = emailFolder.getMessages();

        String emailMessage = "";
        for (Message message : messages) {
            emailMessage = getMessage(message);
            title = message.getSubject();
            if (delete) {
                message.setFlag(Flags.Flag.DELETED, true);
            }
        }
        emailFolder.close(true);
        store.close();
        return emailMessage;
    }

    private String getMessage(Part p) throws MessagingException, IOException {
        if (p.isMimeType("text/*")) {
            String s = (String) p.getContent();
            boolean textIsHtml = p.isMimeType("text/html");
            return s;
        }
        if (p.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart) p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    if (text == null)
                        text = getMessage(bp);
                    continue;
                } else if (bp.isMimeType("text/html")) {
                    String s = getMessage(bp);
                    if (s != null)
                        return s;
                } else {
                    return getMessage(bp);
                }
            }
            return text;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = getMessage(mp.getBodyPart(i));
                if (s != null)
                    return s;
            }
        }
        return null;
    }

        private String getText(Part part) throws
            MessagingException, IOException {
        StringBuilder textBuilder = new StringBuilder();
        if (part.isMimeType("text/plain")) {
            textBuilder.append((String) part.getContent());
            return (String) part.getContent();
        }
        Multipart multipart = (Multipart) part.getContent();
        for (int i = 0; i < multipart.getCount(); i++) {
            String text = getText(multipart.getBodyPart(i));
            if (text != null)
                textBuilder.append(text);
            return text;
        }
        String text = textBuilder.toString();
        return text;
    }

    public Mail gmail(){
        return new Mail("imap.gmail.com",
                "delphi.cisco@gmail.com",
                this.emailPwd,
                this.folder,
                "993");
    }
}
