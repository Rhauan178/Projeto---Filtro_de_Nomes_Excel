package org.example;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.SimpleTokenizer;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        // Caminho do arquivo (ajuste se precisar)
        File arquivoExcel = new File("ArquivoExcel.xlsx");
        File arquivoModeloIA = new File("en-ner-person.bin"); //Arquivo da IA

        // lista para guardar os nomes antes de imprimir
        List<String> listaDePessoas = new ArrayList<>();

        try {
            // Carrega a IA e a faz "iniciar"
            InputStream modelIn = new FileInputStream(arquivoModeloIA);
            TokenNameFinderModel model = new TokenNameFinderModel(modelIn);
            NameFinderME nameFinder = new NameFinderME(model);

            SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;

            FileInputStream fis = new FileInputStream(arquivoExcel);
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);

            System.out.println("---Iniciando análise com a IA---");

            for (Row row : sheet) {
                Cell cell = row.getCell(0);

                if (cell != null && cell.getCellType() == CellType.STRING) {
                    String texto = cell.getStringCellValue().trim();

                    // pula caso o texto seja muito curto
                    if (texto.length() < 3) continue;
                    //Quebra o texto
                    String[] tokens = tokenizer.tokenize(texto);

                    opennlp.tools.util.Span[] nomesEncontrados = nameFinder.find(tokens);

                    if (nomesEncontrados.length > 0) {
                        // Se achou um nome adiciona uma linha
                        listaDePessoas.add(texto);
                    } else {

                        if (pareceNomeHumano(tokens)) {
                            listaDePessoas.add(texto);
                        }

                    }
                    nameFinder.clearAdaptiveData();
                }

            }
            Collections.sort(listaDePessoas);

            System.out.println("\n--- Nomes de Pessoas Identificadas (" + listaDePessoas.size() + ") ---");
            for (String p : listaDePessoas) {
                System.out.println(p);
            }
            workbook.close();
            fis.close();
            modelIn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
        public static boolean pareceNomeHumano(String[] tokens) {
            if (tokens.length < 2) return false;
            return Character.isUpperCase(tokens[0].charAt(0)) &&
                    Character.isUpperCase(tokens[1].charAt(0));
        }
    }
