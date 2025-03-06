package com.example;

import org.springframework.web.bind.annotation.*;

import java.io.FileWriter;
import java.io.IOException;

@RestController
@RequestMapping("/submit")
public class UserInputController {

    @PostMapping
    public String handleInput(@RequestParam String userInput) {
        try (FileWriter writer = new FileWriter("output.txt", true)) {
            writer.write(userInput + "\n");
            return "Text has been written to output.txt";
        } catch (IOException e) {
            e.printStackTrace();
            return "Error occurred while writing to the file.";
        }
    }
}

