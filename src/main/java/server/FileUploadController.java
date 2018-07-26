package server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import server.storage.FileSystemStorageService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.stream.Collectors;

@Controller
public class FileUploadController {

    private final FileSystemStorageService storageService;

    @Autowired
    public FileUploadController(FileSystemStorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model) throws IOException {
        model.addAttribute("files", storageService.loadAll().map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                        "serveFile", path.getFileName().toString()).build().toString())
                .collect(Collectors.toList()));

        return "uploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Resource file = storageService.loadAsResource(filename);
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + file.getFilename() + "\"").body(file);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   Model model) {
        try {
            storageService.store(file);
            InputStream inputSteam = file.getInputStream();
            Scanner scanner = new Scanner(inputSteam);

            int words = 0;
            int sentences =0;
            int letters = 0;
            while (scanner.hasNext()) {
                String word = scanner.next();
                words++;
                if (word.endsWith(".") || word.endsWith("?") || word.endsWith("!")) {
                    sentences++;
                }
                letters += word.length();
            }
            if (sentences==0) sentences++;
            double syllables = letters/3;
            double score = 0.39*(words/sentences) + 11.8*(syllables/words);
            String level;
            if (score < 30){
                level = "College graduate.";
            } else if (score <50){
                level = "College student.";
            } else if (score < 60) {
                level = "11th grade.";
            } else if (score < 70){
                level = "9th grade.";
            } else if (score <80){
                level = "7th grade.";
            } else if (score < 90){
                level = "6th grade";
            } else {
                level = "'yousa dum dum Ani.' ~~~ Jar-Jar Binks";
            }
            model.addAttribute("words", words);
            model.addAttribute("level", level);

            return "word-count";
        } catch (IOException e) {

        }

        return "redirect:/";
    }
}


