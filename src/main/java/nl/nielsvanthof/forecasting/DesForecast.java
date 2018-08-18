package nl.nielsvanthof.forecasting;

import javafx.scene.chart.XYChart;
import javafx.util.Pair;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class DesForecast {
    private final List<Pair<Integer, Double>> values = new ArrayList<>();
    private final double step = 0.01;
    private double level0;
    private double trend0;

    DesForecast(String dataFileName) throws FileNotFoundException {
        Scanner s = new Scanner(new File(dataFileName));

        while (s.hasNext()) {
            String str = s.nextLine();
            String[] data = str.split("\t");

            values.add(new Pair<>(Integer.parseInt(data[0]), Double.parseDouble(data[1])));
        }

        s.close();

        Pair<Double, Double> levelTrend = getLevelTrend();
        level0 = levelTrend.getKey();
        trend0 = levelTrend.getValue();

        System.out.println("Level = " + level0 + ", Trend = " + trend0);
    }

    private double getError(double alpha, double beta) {
        double sqrError = 0;
        double level = level0;
        double trend = trend0;

        // Loop through the data set, updating level, trend and increasing error
        for (Pair<Integer, Double> value : values) {
            double sumLevelTrend = level + trend;
            sqrError += (value.getValue() - sumLevelTrend) * (value.getValue() - sumLevelTrend);

            level += trend + alpha * (value.getValue() - sumLevelTrend);
            trend += beta * alpha * (value.getValue() - sumLevelTrend);
        }

        return Math.sqrt(sqrError / (values.size() - 2));
    }

    private double getBasicError(double level, double trend) {
        double sqrError = 0;

        // Calculate square of the error of first n values
        for(int i = 0; i < 18; i++) {
            sqrError += Math.pow(values.get(i).getValue() - level - trend * (i + 1), 2);
        }

        return sqrError;
    }

    // Get initial values for level and trend
    private Pair<Double, Double> getLevelTrend() {
        double bestLevel = 100;
        double bestTrend = 0;
        double bestError = getBasicError(100, 0);

        // Loop through all possible combination of level and trend, where level is from 100 - 300, trend is from 0 - 1
        for (double level = 100 + step; level <= 300; level += step) {
            for (double trend = step; trend <= 1; trend += step) {
                if (getBasicError(level, trend) < bestError) {
                    bestError = getBasicError(level, trend);
                    bestLevel = level;
                    bestTrend = trend;
                }
            }
        }

        return new Pair<>(bestLevel, bestTrend);
    }

    // Get the best alpha and beta values
    private Pair<Double, Double> getAlphaBeta() {
        double bestAlpha = 0;
        double bestBeta = 0;
        double bestError = getError(0, 0);

        // Try different values for alpha and beta with given step
        for(double alpha = step; alpha <= 1; alpha += step) {
            for(double beta = step; beta <= 1; beta += step) {

                // If those values are better than previously saved, save those values
                if(getError(alpha, beta) < bestError) {
                    bestAlpha = alpha;
                    bestBeta = beta;
                    bestError = getError(alpha, beta);
                }
            }
        }

        return new Pair<>(bestAlpha, bestBeta);
    }

    // Get forecast for next months
    private double getForecast(double alpha, double beta, int t) {
        double level = level0;
        double trend = trend0;

        // Get forecast for the last month with saved value
        for (Pair<Integer, Double> value : values) {
            double sumLevelTrend = level + trend;
            level += trend + alpha * (value.getValue() - sumLevelTrend);
            trend += beta * alpha * (value.getValue() - sumLevelTrend);
        }

        // Add trend multiplied by number of months after the last one which has given value
        return level + t * trend;
    }

    // Output calculated forecast using DES method
    void foreCast() {
        Pair<Double, Double> alphaBeta = getAlphaBeta();
        System.out.println("Double exponential smoothing");
        System.out.println("Alpha: " + alphaBeta.getKey());
        System.out.println("Beta : " + alphaBeta.getValue());
        System.out.println("Error: " + getError(alphaBeta.getKey(), alphaBeta.getValue()));
        System.out.println("Forecast: ");

        for (int i = 0; i < 12; i++) {
            System.out.println((values.size() + i + 1) + "\t" + getForecast(alphaBeta.getKey(), alphaBeta.getValue(), i + 1));
        }
    }

    // Draw a plot of forecast values
    void drawForecastValues(XYChart.Series series) {
        Pair<Double, Double> alphaBeta = getAlphaBeta();

        // Put a point on a plot for every forecast value
        for (int i = 0; i < 12; i++) {
            series.getData().add(new XYChart.Data(values.size() + i + 1, getForecast(alphaBeta.getKey(), alphaBeta.getValue(), i + 1)));
        }
    }
}
