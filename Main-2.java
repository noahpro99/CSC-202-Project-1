package com.company;

//Imports
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.*;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.Session;
import javax.mail.Transport;

public class Main {

    public static FileTime getCreateTime(File file) throws IOException {
//        Gets the path of the file and then returns the .creationTime();
        Path path = Paths.get(file.getPath());
        BasicFileAttributes attr;
        try {
            attr = Files.readAttributes(path, BasicFileAttributes.class);
            return attr.creationTime();
        } catch (IOException e) {
            System.out.println("oops error! " + e.getMessage());
        }


        return null;
    }

    public static Long formattedFileSize(Long filesize){
//        Reformats the filesize long or the unit
        if (filesize > 1000000000){
            filesize = filesize / 1000000000;
        } else if (filesize > 1000000){
            filesize = filesize / 1000000;
        } else if (filesize > 1000){
            filesize = filesize / 1000;
        }
        return filesize;
    }
    public static String fileSizeUnit(Long filesize){
//        Returns the unit for the filesize
        if (filesize > 1000000000){
            return "Gigabytes";
        } else if (filesize > 1000000){
            return "Megabytes";
        } else if (filesize > 1000){
            return "Kilobytes";
        }else {
            return "Bytes";
        }
    }

    public static void sendEmail(String contents, String file) {

//        Modified Send email function from the web. It was tricky to get it to work with google.
        final String username = "noahpro@gmail.com";
        final String password = "yilr gooi pqpm rnfb";

        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); //TLS

        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));

            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(username));

            message.setSubject("Data output");

            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(contents);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            messageBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(file);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(file);
            multipart.addBodyPart(messageBodyPart);

            message.setContent(multipart);

            Transport.send(message);
            System.out.println("Sent message successfully....");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public static void traverseFiles(String inputDir, String outputDir) throws IOException {
//        Create data structures
//        Queue for traversing the files
        Queue<File> queue = new LinkedList<>();
//        one more queue to add the file sizes as it is traversing
        Queue<Long> filesizes = new LinkedList<>();
//        hashtable to connect the files to their lengths. I just really wanted to use one
        Hashtable<Long, File> files = new Hashtable<Long, File>();

//        init the file writer
        FileWriter fileWriter = new FileWriter(outputDir);
        PrintWriter printWriter = new PrintWriter(fileWriter);

//        Traverse the files
        queue.add(new File(inputDir));

        while (!queue.isEmpty()) {

            File current = queue.poll();

            File[] fileDirList = current.listFiles();

            if (fileDirList != null) {

                for (File fd : fileDirList) {
                    if (fd.isDirectory()) {
                        queue.add(fd);
                    } else {
                        files.put(fd.length(), fd);
                        filesizes.add(fd.length());
                    }

                }
            }
        }
//        Finish traversing

//        create an array which we can use to sort by file size. the hash table was used so I didn't loose which one went to which
        Long[] filesizesArray = new Long[filesizes.size()];
        for (int i=0;i<filesizesArray.length;i++){
            filesizesArray[i] = filesizes.poll();
        }
//        Sort the files
        Arrays.sort(filesizesArray, Collections.reverseOrder());

//        Display all of the files sorted and with formatting to the output and file
        System.out.println("------Files found sorted by size--------");
        for (int i=0;i<filesizesArray.length;i++){
            System.out.println("File Name: " + files.get(filesizesArray[i]));
            System.out.println("File Creation time is: "+ getCreateTime(files.get(filesizesArray[i])));
            System.out.println("File size is: "+ formattedFileSize(filesizesArray[i]) +" "+ fileSizeUnit(filesizesArray[i]));
            System.out.println("-------------------");

            printWriter.printf("\nFile Name: " + files.get(filesizesArray[i]));
            printWriter.printf("\nFile Creation time is: "+ getCreateTime(files.get(filesizesArray[i])));
            printWriter.printf("\nFile size is: "+ formattedFileSize(filesizesArray[i]) +" "+ fileSizeUnit(filesizesArray[i]));
            printWriter.printf("\n------------------");

        }

        printWriter.close();
    }

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);

//        Ask for directory to traverse
        System.out.println("Which directory would you like to traverse for files? ");
        String inputDir = sc.nextLine();

//        prompt for output file location
        System.out.println("Please specify a file for the output to be written to? (Please enter a file directory)");
        System.out.println("Note -> A new file will be created if the file isn't found.");
        String outputDir = sc.nextLine();

//        Check all of the files and output their data and write it to the file location previously inputted
        traverseFiles(inputDir, outputDir);

//        Send email with file attachment
        System.out.println("Where would you like this file data emailed to?");
        String toEmailAddress = sc.nextLine();
        sendEmail("Files sorted by size are in the attachment.", outputDir);

    }

}
