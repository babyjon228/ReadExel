package com.example.demo2;
import com.example.demo2.MonthS;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.*;
import java.util.stream.Collectors;

public class HelloApplication extends Application {

    private BarChart<String, Number> chart;
    private List<com.example.demo2.MonthS> monthlyProfits = new ArrayList<>();
    private ComboBox<Integer> yearComboBox;

    @Override
    public void start(Stage stage) throws IOException {
        Button button = new Button("Загрузить excel файл");
        button.setOnAction(event -> loadFile(stage));
        yearComboBox = new ComboBox<>();
        yearComboBox.setDisable(true);
        yearComboBox.setOnAction(e -> charUpd());
        Label yearLabel = new Label("Год:");
        HBox yearBox = new HBox(10, yearLabel, yearComboBox);
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Месяц");
        xAxis.setTickLabelRotation(90);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Доход");
        chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Прибыль по месяцам");
        VBox root = new VBox(10,button,yearBox, chart);
        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    private void loadFile(Stage stage){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            parseExcel(file);
            updateYearComboBox();
            charUpd();
        }
    }



    private void parseExcel(File file) {
        monthlyProfits.clear();
        try (Workbook workbook = new XSSFWorkbook(file)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                Cell dateCell = row.getCell(5);
                if (dateCell == null) continue;
                LocalDate date;
                date = dateCell.getLocalDateTimeCellValue().toLocalDate();
                double total = row.getCell(4).getNumericCellValue();
                monthlyProfits.add(new MonthS(
                        date.getYear(),
                        date.getMonthValue(),
                        total
                ));
            }
        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
        }
    }

    private void updateYearComboBox() {
        Set<Integer> years = monthlyProfits.stream()
                .map(com.example.demo2.MonthS::getYear)
                .collect(Collectors.toSet());
        yearComboBox.setItems(FXCollections.observableArrayList(years));
        yearComboBox.setDisable(years.isEmpty());
        if (!years.isEmpty()) {
            yearComboBox.getSelectionModel().selectFirst();
        }
    }

    private void charUpd(){
        chart.getData().clear();
        Integer selectedYear = yearComboBox.getValue();
        Map<Integer, Double> profitByMonth = monthlyProfits.stream()
                .filter(mp -> mp.getYear() == selectedYear)
                .collect(Collectors.groupingBy(
                        com.example.demo2.MonthS::getMonth,
                        Collectors.summingDouble(com.example.demo2.MonthS::getProfit)
                ));
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        profitByMonth.forEach((month, profit) ->
                series.getData().add(new XYChart.Data<>(
                        Month.of(month).toString(),
                        profit
                ))
        );
        chart.getData().add(series);
    }
    public static void main(String[] args) {
        launch();
    }
}