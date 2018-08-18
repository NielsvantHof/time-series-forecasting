package nl.nielsvanthof.forecasting;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import java.io.FileNotFoundException;

public class Main extends Application {
    private static SesForecast sf;
    private static DesForecast df;

    public static void main(String [] args) throws FileNotFoundException {
        sf = new SesForecast("resources/SwordForecasting.txt");
        sf.foreCast();

        System.out.println();
        System.out.println();

        df = new DesForecast("resources/SwordForecasting.txt");
        df.foreCast();

        launch(args);
    }

    // Drawing plot method
    @Override
    public void start(Stage primaryStage) {
        // Set window title
        primaryStage.setTitle("Demand for Anduril, Flame of the West forecast using SES and DES");

        // Create the axis
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();

        // Set axis labels
        xAxis.setLabel("Months");
        yAxis.setLabel("Demand");

        // Create the chart
        final LineChart<Number,Number> lineChart = new LineChart<>(xAxis,yAxis);

        // Set plot title
        lineChart.setTitle("Demand for Anduril, Flame of the West");

        // Define three series: for values from dataset, for values calculated using SES, for values calculated using DES
        XYChart.Series dataSet = new XYChart.Series();
        XYChart.Series sesValues = new XYChart.Series();
        XYChart.Series desValues = new XYChart.Series();

        // Set plot names
        dataSet.setName("Dataset");
        sesValues.setName("SES Forecast");
        desValues.setName("DES Forecast");

        // Draw values from data set
        sf.drawValues(dataSet);

        // Draw values, forecast using SES
        sf.drawForecastValues(sesValues);

        // Draw values, forecast using DES
        df.drawForecastValues(desValues);

        // Create scene for the chart
        Scene scene  = new Scene(lineChart,800,600);

        // Add series to the chart
        lineChart.getData().add(dataSet);
        lineChart.getData().add(sesValues);
        lineChart.getData().add(desValues);

        // Display the result
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
