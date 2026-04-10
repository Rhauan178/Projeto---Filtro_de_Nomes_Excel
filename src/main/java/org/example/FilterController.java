package org.example;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.SimpleTokenizer;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Set;
import java.util.TreeSet;

@Controller
public class FilterController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/filtrar")
    public String filtrar(@RequestParam("file") MultipartFile file, Model model) {
        // Usamos TreeSet para manter a ordem alfabética e evitar duplicados
        Set<String> nomesFiltrados = new TreeSet<>();

        // Arquivos de IA que você já baixou
        File arquivoModeloPessoa = new File("en-ner-person.bin");
        File arquivoModeloOrg = new File("en-ner-organization.bin");

        try (InputStream fisExcel = file.getInputStream();
             InputStream modelInPessoa = new FileInputStream(arquivoModeloPessoa);
             InputStream modelInOrg = new FileInputStream(arquivoModeloOrg);
             Workbook workbook = new XSSFWorkbook(fisExcel)) {

            // Inicializando a IA
            NameFinderME pessoaFinder = new NameFinderME(new TokenNameFinderModel(modelInPessoa));
            NameFinderME orgFinder = new NameFinderME(new TokenNameFinderModel(modelInOrg));
            SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                Cell cell = row.getCell(0);
                if (cell != null && cell.getCellType() == CellType.STRING) {
                    String texto = cell.getStringCellValue().trim();
                    if (texto.length() < 3) continue;

                    String[] tokens = tokenizer.tokenize(texto);

                    // Filtro inteligente: descarta se for empresa, aceita se for pessoa
                    if (orgFinder.find(tokens).length > 0) continue;

                    if (pessoaFinder.find(tokens).length > 0) {
                        nomesFiltrados.add(texto);
                    }
                }
            }

            model.addAttribute("nomes", nomesFiltrados);
            return "resultado";

        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao processar: " + e.getMessage());
            return "index";
        }
    }
}