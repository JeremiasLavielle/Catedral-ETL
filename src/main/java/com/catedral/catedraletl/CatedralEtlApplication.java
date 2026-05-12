package com.catedral.catedraletl;

import com.catedral.catedraletl.parsing.LpgParserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class CatedralEtlApplication {

    public CatedralEtlApplication() throws IOException {
    }

    public static void main(String[] args) throws IOException {SpringApplication.run(CatedralEtlApplication.class, args);


    }




}
