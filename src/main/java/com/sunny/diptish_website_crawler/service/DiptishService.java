package com.sunny.diptish_website_crawler.service;

import com.opencsv.bean.CsvToBeanBuilder;
import com.sunny.diptish_website_crawler.model.Doctor;
import com.sunny.diptish_website_crawler.model.FileRequest;
import jakarta.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Service
public class DiptishService {

    private static final Logger logger = LogManager.getLogger(DiptishService.class);
    @Autowired
    RestClient restClient;
    private BufferedWriter writer;
    private int counter = 0;
    @Value("${diptish.local-file-path}")
    private String LOCAL_FILE_PATH;
    @Value("${diptish.local-file-name}")
    private String fileName;
    @Value("${diptish.wait-time}")
    private long WAIT_TIME;
    public DiptishService(){

    }

    public String getProfileLinksFile() {
        String response = restClient.get()
                .uri("https://medicine.yale.edu/neurology/people/")
                .retrieve()
                .body(String.class);

        try{
            Document document = Jsoup.parse(response);
            Elements elements = document.select("div.categorized-list__inner-list");
            Elements subElements = elements.select("li.link-items-list__item");
            createFile(fileName);
            writer.write("First Name,Last Name,Profile Link");
            writer.newLine();
            subElements.forEach(entry -> {
                try {
                    Elements profile = entry.select("a");
                    String profileLink = "https://medicine.yale.edu".concat(profile.attr("href"));
                    String profileName = profile.text();
                    String[] name = profileName.split(", ");
                    writer.write(name[1]+","+name[0]+","+profileLink);
                    writer.newLine();
                } catch (Exception e) {
                    System.out.println("Error happened --> " + e.getMessage());
                    logger.info("Error happened --> {}", e.getMessage());
                    throw new RuntimeException(e);
                }
            });
            writer.close();
            System.out.println("File " + fileName + ".csv created successfully!");
            logger.info("File {}.csv created successfully!",fileName);
        }
        catch (Exception e){
            System.out.println("Error happened --> " + e.getMessage());
            logger.info("Error happened --> {}", e.getMessage());
            return "Error happened during file creation. Could not create file " + fileName + ".csv";
        }
        return "File " + fileName + ".csv created successfully!";
    }

    public void extractEmailAddressBlocking (String[] name, String profileLink) throws IOException {
        String response = restClient.get().uri(profileLink)
                .retrieve()
                .onStatus(HttpStatusCode::is2xxSuccessful, (request, serverResponse) -> {
                    System.out.println("Received success response from server");
                    logger.info("Received success response from server");
                })
                .onStatus(HttpStatusCode::is4xxClientError, (request, serverResponse) -> {
                    System.out.println("Received HTTP 4XX error.");
                    logger.info("Received HTTP 4XX error.");
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, serverResponse) -> {
                    System.out.println("Received HTTP 5XX error.");
                    logger.info("Received HTTP 5XX error.");
                })
                .body(String.class);
        try {
            if(null!=response){
                Document document = Jsoup.parse(response);
                Elements elements = document.select("script[data-schema=\"ProfilePage\"]");
                if(!elements.isEmpty()){
                    String profileJSON = elements.get(0).data();
                    String emailAddress = "";
                    JSONParser parser = new JSONParser();
                    JSONObject json = (JSONObject) parser.parse(profileJSON);
                    JSONObject mainEntity = (JSONObject) json.get("mainEntity");
                    emailAddress = (mainEntity.containsKey("email") ? mainEntity.get("email").toString() : null);
                    if(null!=emailAddress) {
                        System.out.println("Writing record number " + (++counter) +" to file --> " + name[0]+", "+name[1]+", "+emailAddress);
                        logger.info("Writing record number {} to file --> {} {} {}", ++counter, name[0], name[1], emailAddress);
                        writer.write(name[0]+","+name[1]+","+emailAddress);
                        writer.newLine();
                    }
                    else {
                        System.out.println("Email Address not found so not writing record to file --> "+ name[0]+" "+name[1]);
                        logger.info("Email Address not found so not writing record to file --> {} {}",name[0], name[1]);
                    }
                }
            } else {
                System.out.println("Null response from profile page link.");
                logger.info("Null response from profile page link.");
            }
        } catch (Exception e) {
            System.out.println("Error happened --> " + e.getMessage());
            logger.error("Error happened --> {}",e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public String createProcessedFile(FileRequest fileRequest) throws IOException {
        //Read the File
        String csvFilePath = LOCAL_FILE_PATH + fileName + ".csv";
        try (FileReader fileReader = new FileReader(csvFilePath)) {
            List<Doctor> doctors = new CsvToBeanBuilder<Doctor>(fileReader)
                    .withType(Doctor.class)
                    .build()
                    .parse();

            if(!doctors.isEmpty()){
                createFile(fileRequest.getFileName());
                writer.write("First Name,Last Name,Email Address");
                writer.newLine();
                doctors.stream()
                        .filter(doctor -> doctor.getLastName().toLowerCase().startsWith(fileRequest.getLetter().toString().toLowerCase()))
                        .forEach(doctor -> {
                            try{
                                String[] name = {doctor.getFirstName(),doctor.getLastName()};
                                extractEmailAddressBlocking(name,doctor.getProfileLink());
                                Thread.sleep(WAIT_TIME);
                            }
                            catch (Exception e){
                                System.out.println("Error happened --> " + e.getMessage());
                                logger.error("Error happened --> {}",e.getMessage());
                            }
                        });
                counter = 0;
                writer.close();
                System.out.println("Successfully created file " + fileRequest.getFileName() + ".csv");
                logger.info("Successfully created file {}.csv",fileRequest.getFileName());
            } else {
                return "File " + fileName + ".csv is Empty";
            }
        }
        catch (Exception e){
            System.out.println("Error happened while reading file --> " + e.getMessage());
            logger.error("Error happened while reading file --> {}",e.getMessage());
            return "Could not create file " + fileRequest.getFileName() + ".csv";
        }
        return "Successfully created file " + fileRequest.getFileName() + ".csv";
    }

    public void createFile(String fileName) throws IOException {
        this.writer = new BufferedWriter(new FileWriter(LOCAL_FILE_PATH + fileName + ".csv"));
    }

    @PreDestroy
    public void finishProcessing() throws IOException {
        System.out.println("Saving data and closing file.");
        logger.info("Saving data and closing file.");
        writer.close();
    }
}
