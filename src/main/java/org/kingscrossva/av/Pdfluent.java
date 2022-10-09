package org.kingscrossva.av;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

//import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class Pdfluent {

    public static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
    public static void main(String args[]) throws IOException {

        for (String path : args) {
            processFile(path);
        }

    }

    public static void processFile(String path) throws IOException {
        //File file = new File("/home/john/Downloads/May 29 Phone Guide.pdf");
        File file = new File(path);
        PDDocument document = PDDocument.load(file);

        // PDFTextStripper pdfStripper = new PDFTextStripper();
        PdfLayoutTextStripper pdfStripper = new PdfLayoutTextStripper();

        // PdfLayoutTextStripper2 pdfStripper = new PdfLayoutTextStripper2();
        // pdfStripper.setSortByPosition(true);
        // pdfStripper.fixedCharWidth = 5;


        String text = pdfStripper.getText(document);
        //System.out.println(text);
        document.close();

        state = ST_PRELUDE;

        try (BufferedReader br = new BufferedReader(new StringReader(text))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                line = line.replaceAll("\\s+", " ");
                line = line.replace("Son gs", "Songs").replace("T o", "To")
                    .replace("Y ou", "You").replace("F or", "For")
                    .replace("W e", "We").replace("T riune", "Triune")
                    .replace("W orship", "Worship").replace("V erse", "Verse")
                    .replace("de F e", "de Fe").replace("Y es", "Yes")
                    .replace(" ,", ",").replace("TODA Y", "TODAY");
                if (line.contains("to give online")) {
                    line = "https://kingscrossva.org/giving";
                }
                processLine(line);
            }
        }
    }

    private static final int MAX_LINES = 4;
    private static final int ST_PRELUDE = 0;
    private static final int ST_PRELUDE_END = 10;
    private static final int ST_BEFORE_BLOCK = 20;
    private static final int ST_BLOCK = 30;
    private static final int ST_POSTLUDE = 1000;
    private static int state = ST_PRELUDE;
    private static List<String> block = new ArrayList<>();
    private static List<String> chorus = new ArrayList<>();

    public static void processLine(String line) {
        // System.out.println(line);
        // if (Math.max(3, 5) > 2) return;
        String lower = line.toLowerCase();

        if (state == ST_PRELUDE) {
            if (lower.contains("scriptural call") || lower.contains("llamado")) {
                //System.out.println("ST_PRELUDE_END");
                state = ST_PRELUDE_END;
            }
        }
        else if (state == ST_PRELUDE_END) {
            if (isEmpty(line)) {
                //System.out.println("ST_BEFORE_BLOCK 1");
                state = ST_BEFORE_BLOCK;
            }
        }
        else if (state == ST_BEFORE_BLOCK) {
            if (!isEmpty(line)) {
                //System.out.println("ST_BLOCK");
                state = ST_BLOCK;
                block.add(line);
            }
        }
        else if (state == ST_BLOCK) {
            if (isEmpty(line) || block.size() + 1 >= MAX_LINES) {
                if (isEmpty(line)) {
                    if (block.size() == 1 && block.get(0).trim().equalsIgnoreCase("Chorus: O the blood of Jesus washes me")) {
                        return; // really weird case
                    }
                }
                else {
                    block.add(line);
                }

                //System.out.println("ST_BEFORE_BLOCK");
                state = ST_BEFORE_BLOCK;
                if (!block.isEmpty()) {
                    if (block.get(0).trim().equalsIgnoreCase("chorus")) {
                        printout(chorus);
                    }
                    else {
                        //System.out.println("DOG " + block.get(0))
                        if (block.get(0).trim().toLowerCase().startsWith("chorus")) {
                            chorus.clear();
                            block.forEach(s -> chorus.add(s));
                        }
                        printout(block);
                        if (block.get(0).toLowerCase().contains("amen! thanks be to god. alleluia!")) {
                            state = ST_POSTLUDE;
                        }
                    }
                    block.clear();
                }
            }
            else {
                block.add(line);
            }
        }
        else if (state == ST_POSTLUDE) {

        }

    }

    public static void printout(List<String> list) {
        boolean foundCcli = false;
        if (!list.isEmpty()) {
            for (String line : list) {
                if (line.contains("CCLI")) {
                    foundCcli = true;
                }
                else if (foundCcli) {
                    System.out.println("--");
                }
                System.out.println(line);
            }
            System.out.println("--");
        }
    }

}
