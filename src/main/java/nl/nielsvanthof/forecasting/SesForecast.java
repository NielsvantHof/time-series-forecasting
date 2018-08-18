package nl.nielsvanthof.forecasting;

import javafx.scene.chart.XYChart;
import javafx.util.Pair;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class SesForecast {
    private final List<Pair<Integer, Double>> values = new ArrayList<>();
    private double level0;

    SesForecast(String dataFileName) throws FileNotFoundException {
        Scanner s = new Scanner(new File(dataFileName));

        while (s.hasNext()) {
            String str = s.nextLine();
            String [] data = str.split("\t");

            values.add(new Pair<>(Integer.parseInt(data[0]), Double.parseDouble(data[1])));
        }

        s.close();

        level0 = getAvg();
    }

    // Output calculated forecast using SES method
    void foreCast() {
        double alpha = getAlpha();
        System.out.println("Simple exponential smoothing");
        System.out.println("Alpha: " + alpha);
        System.out.println("Error: " + getError(alpha));
        System.out.println("Forecast: ");

        double forecast = getForecast(alpha);

        for(int i = 0; i < 12; i++) {
            System.out.println((values.size() + i + 1) + "\t" + forecast);
        }
    }

    // Draw a plot of given values
    void drawValues(XYChart.Series series) {
        // Put a point on a plot for every value in the data set
        for (Pair<Integer, Double> value : values) {
            series.getData().add(new XYChart.Data(value.getKey(), value.getValue()));
        }
    }

    // Draw a plot of forecast values
    void drawForecastValues(XYChart.Series series) {
        double forecast = getForecast(getAlpha());

        // Put a point on a plot for every forecast value
        for (int i = 0; i < 12; i++) {
            series.getData().add(new XYChart.Data(values.size() + i + 1, forecast));
        }
    }

    // Calculate error for method of least squares
    private double getError(double alpha) {
        double sqrError = 0;
        double level = level0;

        // Loop through the data set, updating level and increasing error
        for (Pair<Integer, Double> value : values) {
            sqrError += (value.getValue() - level) * (value.getValue() - level);
            level += alpha * (value.getValue() - level);
        }

        return Math.sqrt(sqrError / (values.size() - 1));
    }

    // Get the best alpha value
    private double getAlpha() {
        double bestAlpha = 0;
        double bestError = getError(0);

        // Try different values for alpha
        double step = 0.01;

        for(double alpha = step; alpha <= 1; alpha += step) {
            // If that value is better than previously saved, save that value
            if (getError(alpha) < bestError) {
                bestAlpha = alpha;
                bestError = getError(alpha);
            }
        }

        return bestAlpha;
    }

    // Get forecast for next months
    private double getForecast(double alpha) {
        double level = level0;

        // Calculate forecast level
        for (Pair<Integer, Double> value : values) {
            level += alpha * (value.getValue() - level);
        }

        return level;
    }

    // Get average of first n values from data set
    private double getAvg() {
        double avg = 0;

        for (int i = 0; i < 12; i++) {
            avg += values.get(i).getValue();
        }

        return avg / 12;
    }
}