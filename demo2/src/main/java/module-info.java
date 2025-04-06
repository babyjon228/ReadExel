module com.example.exceljava {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.poi.ooxml;
    requires org.apache.poi.poi;


    opens com.example.demo2 to javafx.fxml;
    exports com.example.demo2;
}