package com.shortlisting.stocks;

import com.shortlisting.stocks.pojo.Stock;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) {
        Main main = new Main();
        try {

            // Read all properties in a map
            Map<String, String> properties = getPropertiesInMap("config.properties");

            // Scan the directory for all the csv files present
            List<String> fileNames = getAllFiles(properties.get("directoryToScan"));

            // For each csv file, print the specified column
//            fileNames.forEach(f -> {
//                try {
//                    main.printCsv(f);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            });

            // For each file, fetch the map of stocks
            List<Map<String, Double>> stocksListWithHoldings = new ArrayList<>();
            Map<String, Double> stocksFromTheFile;
            for (String fileName : fileNames) {
                double cutoffHoldindsPercent = Double.parseDouble(properties.get("individualCutoffHoldingsPercent"));
                stocksFromTheFile = main.getStocksFromTheFile(fileName, Integer.parseInt(properties.get("columnIndexForStockName")), Integer.parseInt(properties.get("columnIndexForStockHoldings")), cutoffHoldindsPercent);
                if (!stocksFromTheFile.isEmpty()) {
                    stocksListWithHoldings.add(stocksFromTheFile);
                }
            }

            // Add all the stocks in one common map and sum up their holdings and add count
            Map<String, Stock> combinedStocksMap = new HashMap<>();
            for (Map<String, Double> stocksListWithHolding : stocksListWithHoldings) {
                for (Map.Entry<String, Double> stockNameAndValueEntrySet : stocksListWithHolding.entrySet()) {
                    String stockName = stockNameAndValueEntrySet.getKey();
                    if (combinedStocksMap.containsKey(stockName)) {
                        Stock stock = combinedStocksMap.get(stockName);
                        stock.setCount(stock.getCount() + 1);
                        stock.setHolding(stock.getHolding() + stockNameAndValueEntrySet.getValue());
                        combinedStocksMap.put(stockName, stock);
                    } else {
                        combinedStocksMap.put(stockName, new Stock(stockName, stockNameAndValueEntrySet.getValue(), 1));
                    }
                }
            }

            // Calculate the average weight of the stocks;
            for (Map.Entry<String, Stock> stockEntry : combinedStocksMap.entrySet()) {
                Stock stock = stockEntry.getValue();
                if (stock.getCount() > 1) {
                    stock.setHolding(stock.getHolding() / stock.getCount());
                }
            }

            // Sort the map by count and then holdings
//            List<Map.Entry<String, Stock>> list = new LinkedList<>(combinedStocksMap.entrySet());

            List<Stock> stocks = new ArrayList<>(combinedStocksMap.values());
            Collections.sort(stocks, new Comparator<Stock>() {
                @Override
                public int compare(Stock stock1, Stock stock2) {

                    int compare = Integer.valueOf(stock2.getCount()).compareTo(Integer.valueOf(stock1.getCount()));
                    if (compare != 0) {
                        return compare;
                    }
                    return Double.valueOf(stock2.getHolding()).compareTo(Double.valueOf(stock1.getHolding()));
                }
            });

            printDivider("=", 90);
            System.out.format("%5s | %-50s | %10s | %10s\n", "#", "Stock", "Holding", "Count");
            printDivider("=", 90);
            int totalStocksListed = Integer.parseInt(properties.get("topNStocks"));
            int count = 1;
            for (Stock stock : stocks) {
                double averageCutOffHoldingsPercent = Double.parseDouble(properties.get("averageCutOffHoldingsPercent"));
                if ( stock.getHolding() >= averageCutOffHoldingsPercent ) {
                    System.out.format("%5d | %-50s | %10.2f | %10d\n", count, stock.getName(), stock.getHolding(), stock.getCount());
                }
                if ( count >= totalStocksListed ) {
                    break;
                }
                count ++;
            }
            printDivider("=", 90);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printDivider(String pattern, int limit) {
        System.out.println(Stream.generate(() -> pattern).limit(limit).collect(Collectors.joining()));
    }

    private static Map<String, String> getPropertiesInMap(String propertyFileName) throws IOException {
        Map<String, String> propertyMap = new LinkedHashMap<>();
        InputStream stream = Main.class.getClassLoader().getResourceAsStream(propertyFileName);
        Properties properties = new Properties();
        properties.load(stream);

        Enumeration keys = properties.propertyNames();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            propertyMap.put(key, properties.getProperty(key));
        }

        stream.close();
        return propertyMap;
    }

    private static List<String> getAllFiles(String relativeFolderPath) {

//        System.out.println("Scanning the directory: " + relativeFolderPath);

        ClassLoader classLoader = Main.class.getClassLoader();
        File file = new File(classLoader.getResource(relativeFolderPath).getFile());
        String absolutePath = file.getAbsolutePath();

        try (Stream<Path> walk = Files.walk(Paths.get(absolutePath))) {

            List<String> fileNameList = walk.map(Path::toString)
                    .filter(f -> f.endsWith(".csv"))
                    .collect(Collectors.toList());

            fileNameList.forEach(System.out::println);

            return fileNameList;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    public void printCsv(String absoluteFileName) throws IOException {
        Reader in = new FileReader(absoluteFileName);
        Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);

        for (CSVRecord record : records) {
            System.out.print("Stock: " + record.get(0));
            System.out.println(" --------------- % Holdings: " + record.get(4));
        }
    }

    public Map<String, Double> getStocksFromTheFile(String absoluteFileName, int stockNameIndex, int holdingsIndex, double cutoffHoldingPercentage) throws IOException {
        Map<String, Double> stocksMap = new LinkedHashMap<>();
        Reader in = new FileReader(absoluteFileName);
        Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
        for (CSVRecord record : records) {
//            System.out.print("Stock: " + record.get(stockNameIndex));
            Double holdingPercentage = Double.valueOf(record.get(holdingsIndex));
//            System.out.println(" --------------- % Holdings: " + holdingPercentage);
            if (holdingPercentage >= cutoffHoldingPercentage) {
                stocksMap.put(record.get(stockNameIndex), holdingPercentage);
            }
        }
        return stocksMap;
    }
}
