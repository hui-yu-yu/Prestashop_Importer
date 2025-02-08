package org.example;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.util.Map;
import java.util.Set;

/**
 * DiscountCSVGenerator is responsible for generating a combined discount CSV file.
 *
 * The CSV file includes the following columns:
 *   A: ID                  (from ProductDirectInfo)
 *   B: Name                (product name)
 *   C: Starting at Tier 1  (fixed value "100")
 *   D: Price Reduction Tier 1 (from ProductPriceInfo.discount1)
 *   E: Starting at Tier 2  (fixed value "500")
 *   F: Price Reduction Tier 2 (from ProductPriceInfo.discount2)
 *   G: Type                (fixed value "percentage")
 *   H: Tax                 (fixed value "0")
 *
 * Note: This example currently only processes products whose core is "MPU".
 */
public class DiscountCSVGenerator {

    /**
     * Generates a combined discount CSV file.
     *
     * @param filePath         The output file path.
     * @param updateNames      A set of product names that need updating.
     * @param directInfoMap    A map of product names to ProductDirectInfo (containing ID and base price).
     * @param priceInfoMap     A map of product names to ProductPriceInfo (containing pricing and discount info).
     */
    public static void generateCombinedDiscountCSV(String filePath, Set<String> updateNames,
                                                   Map<String, Main.ProductDirectInfo> directInfoMap,
                                                   Map<String, Main.ProductPriceInfo> priceInfoMap) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // 設定 CSV 的欄位標題
            String[] header = {
                    "ID",
                    "Name",
                    "Starting at Tier 1",
                    "Price Reduction Tier 1",
                    "Starting at Tier 2",
                    "Price Reduction Tier 2",
                    "Type",
                    "Tax"
            };
            writer.writeNext(header);

            for (String name : updateNames) {
                Main.ProductDirectInfo directInfo = directInfoMap.get(name);
                Main.ProductPriceInfo priceInfo = priceInfoMap.get(name);

                if (directInfo == null) {
                    System.out.println("Cannot find product ID for: " + name);
                    continue;
                }
                if (priceInfo == null) {
                    System.out.println("Cannot find price info for: " + name);
                    continue;
                }
                // 確認 Core，這次只處理 Core= MPU || M4 的型號
                //if (!priceInfo.core.equalsIgnoreCase("MPU")) continue;
                if (!(priceInfo.core.equalsIgnoreCase("MPU") || priceInfo.core.equalsIgnoreCase("M4"))) continue;


                String startingAtTier1 = "100";
                String priceReductionTier1 = priceInfo.discount1; // excel 中 discount1 需為 value
                String startingAtTier2 = "500";
                String priceReductionTier2 = priceInfo.discount2; // excel 中 discount2 也需為 value
                String type = "percentage";
                String tax = "0";

                String[] record = {
                        directInfo.id,
                        name,
                        startingAtTier1,
                        priceReductionTier1,
                        startingAtTier2,
                        priceReductionTier2,
                        type,
                        tax
                };

                writer.writeNext(record);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
