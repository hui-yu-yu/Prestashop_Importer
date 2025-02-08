package org.example;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Main {

    /**
     * Encapsulates price information from 3_product_new_price.csv.
     * Now includes discount1 and discount2.
     */
    public static class ProductPriceInfo {
        String core;
        String name;
        String price1; // For MPU: 1-99
        String price2; // For MPU: 100-499
        String price3; // For MPU: 500-999
        String price4; // For 8051/M0/M23/Audio
        String price5; // For 8051/M0/M23/Audio
        String discount1; // Price reduction value, formatted to two decimals, without "%"
        String discount2; // Price reduction value, formatted to two decimals, without "%"
    }

    /**
     * Encapsulates data from 2_product_in_direct.csv,
     * including product ID and the original Base Price.
     */
    public static class ProductDirectInfo {
        String id;
        String basePrice;
    }

    public static void main(String[] args) {
        // Set the CSV folder path (placed in the project root's "csv" folder)
        String folder = "csv" + File.separator;
        String priceUpdateFile = folder + "1_price_update.csv";
        String productInDirectFile = folder + "2_product_in_direct.csv";
        String productNewPriceFile = folder + "3_product_new_price.csv";

        // Read CSV files to obtain the update names and mapping info
        Set<String> updateNames = readPriceUpdateCSV(priceUpdateFile);
        Map<String, ProductDirectInfo> directInfoMap = readProductInDirectCSV(productInDirectFile);
        Map<String, ProductPriceInfo> priceInfoMap = readProductNewPriceCSV(productNewPriceFile);

        // Generate the Base Price modification CSV (if needed)
        String outputFile = folder + "output.csv";
        writeOutputCSV(outputFile, updateNames, directInfoMap, priceInfoMap);
        System.out.println("Original CSV generated at: " + outputFile);

        // Generate the combined discount CSV by calling DiscountCSVGenerator (if needed)
        String combinedDiscountOutputFile = folder + "combined_discount_output.csv";
        DiscountCSVGenerator.generateCombinedDiscountCSV(combinedDiscountOutputFile, updateNames, directInfoMap, priceInfoMap);
        System.out.println("Combined discount CSV generated at: " + combinedDiscountOutputFile);
    }

    /**
     * Helper method to format discount values.
     * Removes any '%' symbol and rounds the number to two decimal places.
     */
    private static String formatDiscount(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        // Remove any '%' symbol
        s = s.replace("%", "");
        try {
            double value = Double.parseDouble(s);
            return String.format("%.2f", value);
        } catch (NumberFormatException e) {
            return "";
        }
    }

    /**
     * Reads 1_price_update.csv and returns a set of product names that need updating.
     * Assumes the product name is in the second column (index 1).
     */
    private static Set<String> readPriceUpdateCSV(String filePath) {
        Set<String> updateNames = new HashSet<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            // Skip the header row
            reader.readNext();
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                if (nextLine.length > 1) {
                    updateNames.add(nextLine[1].trim());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return updateNames;
    }

    /**
     * Reads 2_product_in_direct.csv and creates a map of product names to ProductDirectInfo.
     * Assumes:
     * - ID is in the first column (index 0)
     * - Name is in the second column (index 1)
     * - Base Price is in the sixth column (index 5)
     */
    private static Map<String, ProductDirectInfo> readProductInDirectCSV(String filePath) {
        Map<String, ProductDirectInfo> map = new HashMap<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            // Skip the header row
            reader.readNext();
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                if (nextLine.length > 1) {
                    ProductDirectInfo info = new ProductDirectInfo();
                    info.id = nextLine[0].trim();
                    String name = nextLine[1].trim();
                    info.basePrice = nextLine.length > 5 ? nextLine[5].trim() : "";
                    map.put(name, info);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * Reads 3_product_new_price.csv and creates a map of product names to ProductPriceInfo.
     * Assumes:
     * - Core is in the first column (index 0)
     * - Name is in the third column (index 2)
     * - Price tiers start from the fourth column (index 3)
     * - discount1 from column P (index 15) and discount2 from column Q (index 16)
     */
    private static Map<String, ProductPriceInfo> readProductNewPriceCSV(String filePath) {
        Map<String, ProductPriceInfo> map = new HashMap<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            // Skip the header row
            reader.readNext();
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                if (nextLine.length >= 4) {
                    ProductPriceInfo info = new ProductPriceInfo();
                    info.core = nextLine[0].trim();
                    info.name = nextLine[2].trim();
                    info.price1 = nextLine[3].trim();
                    info.price2 = nextLine.length > 4 ? nextLine[4].trim() : "";
                    info.price3 = nextLine.length > 5 ? nextLine[5].trim() : "";
                    info.price4 = nextLine.length > 6 ? nextLine[6].trim() : "";
                    info.price5 = nextLine.length > 7 ? nextLine[7].trim() : "";
                    info.discount1 = nextLine.length > 15 ? formatDiscount(nextLine[15].trim()) : "";
                    info.discount2 = nextLine.length > 16 ? formatDiscount(nextLine[16].trim()) : "";
                    map.put(info.name, info);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * Exports a new CSV file with the following columns:
     * - ID
     * - Name
     * - New Base Price (using the first tier price)
     * - Six Short Description columns (copies of the generated HTML table)
     * - Original Base Price (from 2_product_in_direct.csv)
     */
    private static void writeOutputCSV(String filePath, Set<String> updateNames,
                                       Map<String, ProductDirectInfo> directInfoMap,
                                       Map<String, ProductPriceInfo> priceInfoMap) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            String[] header = {
                    "ID",
                    "Name",
                    "New Base Price",
                    "Short Description-EN",
                    "Short Description-TW",
                    "Short Description-DE",
                    "Short Description-KR",
                    "Short Description-JA",
                    "Short Description-CN",
                    "Original Base Price"
            };
            writer.writeNext(header);

            for (String name : updateNames) {
                ProductDirectInfo directInfo = directInfoMap.get(name);
                ProductPriceInfo priceInfo = priceInfoMap.get(name);

                if (directInfo == null) {
                    System.out.println("Cannot find product ID for: " + name);
                    continue;
                }
                if (priceInfo == null) {
                    System.out.println("Cannot find price info for: " + name);
                    continue;
                }

                String shortDesc = generateHtmlTable(priceInfo);
                String newBasePrice = priceInfo.price1;
                String originalBasePrice = directInfo.basePrice;

                String[] record = {
                        directInfo.id,
                        name,
                        newBasePrice,
                        shortDesc,
                        shortDesc,
                        shortDesc,
                        shortDesc,
                        shortDesc,
                        shortDesc,
                        originalBasePrice
                };
                writer.writeNext(record);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates an HTML table (short description) based on the product's core and pricing info.
     */
    private static String generateHtmlTable(ProductPriceInfo info) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table border=\"1\" cellpadding=\"5\" style=\"text-align: center; border-collapse: collapse; border: 1px #E8E8E8 solid;\">");
        sb.append("<tbody>");
        sb.append("<tr><td><strong>Quantity</strong></td><td><strong>Unit Price (USD)</strong></td></tr>");

        String contactLink = "<a href=\"mailto:eSupport@nuvoton.com\" target=\"_blank\">Contact</a>";

        if (info.core.equalsIgnoreCase("8051") ||
                info.core.equalsIgnoreCase("M0M23") ||
                info.core.equalsIgnoreCase("Audio")) {
            sb.append("<tr><td>1-99</td><td>").append(info.price1).append("</td></tr>");
            sb.append("<tr><td>100-499</td><td>").append(info.price2).append("</td></tr>");
            sb.append("<tr><td>500-999</td><td>").append(info.price3).append("</td></tr>");
            sb.append("<tr><td>1000-1999</td><td>").append(info.price4).append("</td></tr>");
            sb.append("<tr><td>2000-4999</td><td>").append(info.price5).append("</td></tr>");
            sb.append("<tr><td>5000+</td><td>").append(contactLink).append("</td></tr>");
        } else if (info.core.equalsIgnoreCase("M4")) {
            sb.append("<tr><td>1-999</td><td>").append(info.price1).append("</td></tr>");
            sb.append("<tr><td>1000-1999</td><td>").append(info.price2).append("</td></tr>");
            sb.append("<tr><td>2000-4999</td><td>").append(info.price3).append("</td></tr>");
            sb.append("<tr><td>5000+</td><td>").append(contactLink).append("</td></tr>");
        } else if (info.core.equalsIgnoreCase("MPU")) {
            sb.append("<tr><td>1-99</td><td>").append(info.price1).append("</td></tr>");
            sb.append("<tr><td>100-499</td><td>").append(info.price2).append("</td></tr>");
            sb.append("<tr><td>500-999</td><td>").append(info.price3).append("</td></tr>");
            sb.append("<tr><td>1000+</td><td>").append(contactLink).append("</td></tr>");
        } else {
            sb.append("<tr><td colspan=\"2\">No pricing information available</td></tr>");
        }

        sb.append("</tbody>");
        sb.append("</table>");
        return sb.toString();
    }
}
