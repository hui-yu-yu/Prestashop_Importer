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
     * 用來封裝來自 3_product_new_price.csv 的價格資訊
     */
    public static class ProductPriceInfo {
        String core;
        String name;
        String price1; // 8051/M0/M23/Audio → 1-99；M4→ 1-999；MPU→ 1-99
        String price2; // 8051/M0/M23/Audio → 100-499；M4→ 1000-1999；MPU→ 100-499
        String price3; // 8051/M0/M23/Audio → 500-999；M4→ 2000-4999；MPU→ 500-999
        String price4; // 僅 8051/M0/M23/Audio (1000-1999)
        String price5; // 僅 8051/M0/M23/Audio (2000-4999)
    }

    /**
     * 用來封裝來自 2_product_in_direct.csv 的資料，
     * 包含商品 ID 與原始 Base Price（假設 Base Price 位於第 F 欄，即第 6 欄）
     */
    public static class ProductDirectInfo {
        String id;
        String basePrice;
    }

    public static void main(String[] args) {
        // 設定 CSV 資料夾路徑 (放在專案根目錄下的 csv 資料夾)
        String folder = "csv" + File.separator;
        String priceUpdateFile = folder + "1_price_update.csv";
        String productInDirectFile = folder + "2_product_in_direct.csv";
        String productNewPriceFile = folder + "3_product_new_price.csv";
        String outputFile = folder + "output.csv";

        // 1. 讀取 1_price_update.csv，取得需更新的商品名稱集合
        Set<String> updateNames = readPriceUpdateCSV(priceUpdateFile);

        // 2. 讀取 2_product_in_direct.csv，建立商品名稱對應 ProductDirectInfo 的 Map
        Map<String, ProductDirectInfo> directInfoMap = readProductInDirectCSV(productInDirectFile);

        // 3. 讀取 3_product_new_price.csv，建立商品名稱對應 ProductPriceInfo 的 Map
        Map<String, ProductPriceInfo> priceInfoMap = readProductNewPriceCSV(productNewPriceFile);

        // 4. 整合資料並輸出新的 CSV 檔案
        writeOutputCSV(outputFile, updateNames, directInfoMap, priceInfoMap);

        System.out.println("輸出 CSV 檔案產生於：" + outputFile);
    }

    /**
     * 讀取 1_price_update.csv，回傳需要更新的商品名稱集合
     * 假設檔案有標題，且商品名稱在第二欄（index 1）
     */
    private static Set<String> readPriceUpdateCSV(String filePath) {
        Set<String> updateNames = new HashSet<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            // 跳過標題列
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
     * 讀取 2_product_in_direct.csv，建立商品名稱對應 ProductDirectInfo 的 Map
     * 假設檔案有標題：
     *   - ID 在第一欄 (index 0)
     *   - 名稱在第二欄 (index 1)
     *   - Base Price 在第六欄 (index 5)
     */
    private static Map<String, ProductDirectInfo> readProductInDirectCSV(String filePath) {
        Map<String, ProductDirectInfo> map = new HashMap<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            // 跳過標題列
            reader.readNext();
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                if (nextLine.length > 1) {
                    ProductDirectInfo info = new ProductDirectInfo();
                    info.id = nextLine[0].trim();
                    String name = nextLine[1].trim();
                    // 若存在 Base Price 資料 (第六欄)，則讀取；否則設定為空字串
                    if (nextLine.length > 5) {
                        info.basePrice = nextLine[5].trim();
                    } else {
                        info.basePrice = "";
                    }
                    map.put(name, info);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 讀取 3_product_new_price.csv，建立商品名稱對應 ProductPriceInfo 的 Map
     * 假設檔案有標題，欄位定義如下：
     *   - Core：第1欄 (index 0)
     *   - (未使用)：第2欄 (index 1)
     *   - 名稱：第3欄 (index 2)
     *   - 第一階梯價格：第4欄 (index 3)
     *   - 第二階梯價格：第5欄 (index 4)
     *   - 第三階梯價格：第6欄 (index 5)
     *   - 第四階梯價格 (僅 8051/M0/M23/Audio)：第7欄 (index 6)
     *   - 第五階梯價格 (僅 8051/M0/M23/Audio)：第8欄 (index 7)
     */
    private static Map<String, ProductPriceInfo> readProductNewPriceCSV(String filePath) {
        Map<String, ProductPriceInfo> map = new HashMap<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            // 跳過標題列
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
                    map.put(info.name, info);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 整合資料後，輸出新的 CSV 檔案
     * 輸出欄位依序為：
     *   - 商品 ID
     *   - 商品名稱
     *   - 新底價 (第一階梯價格)
     *   - Short Description (HTML 表格)
     *   - 原始 Base Price (來自 2_product_in_direct.csv 的欄位F)
     */

    /*
    private static void writeOutputCSV(String filePath, Set<String> updateNames,

                                       Map<String, ProductDirectInfo> directInfoMap,
                                       Map<String, ProductPriceInfo> priceInfoMap) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            String[] header = {"ID", "Name", "New Base Price", "Short Description", "Original Base Price"};
            writer.writeNext(header);

            for (String name : updateNames) {
                ProductDirectInfo directInfo = directInfoMap.get(name);
                ProductPriceInfo priceInfo = priceInfoMap.get(name);

                if (directInfo == null) {
                    System.out.println("找不到商品 ID 與原始價格資訊，商品名稱：" + name);
                    continue;
                }
                if (priceInfo == null) {
                    System.out.println("找不到價格資訊，商品名稱：" + name);
                    continue;
                }

                // 根據商品的 Core 產生 HTML 表格 (Short Description)
                String shortDesc = generateHtmlTable(priceInfo);

                // 以第一階梯價格作為新底價
                String newBasePrice = priceInfo.price1;
                String originalBasePrice = directInfo.basePrice;

                String[] record = {directInfo.id, name, newBasePrice, shortDesc, originalBasePrice};
                writer.writeNext(record);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
     */

    private static void writeOutputCSV(String filePath, Set<String> updateNames,
                                       Map<String, ProductDirectInfo> directInfoMap,
                                       Map<String, ProductPriceInfo> priceInfoMap) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // 新版的 header 擴增為 10 個欄位，其中六個 Short Description 欄位
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
                    System.out.println("找不到商品 ID 與原始價格資訊，商品名稱：" + name);
                    continue;
                }
                if (priceInfo == null) {
                    System.out.println("找不到價格資訊，商品名稱：" + name);
                    continue;
                }

                // 產生 HTML 表格 (Short Description)
                String shortDesc = generateHtmlTable(priceInfo);
                String newBasePrice = priceInfo.price1;
                String originalBasePrice = directInfo.basePrice;

                // 新版的 record 將 shortDesc 複製至六個欄位中
                String[] record = {
                        directInfo.id,
                        name,
                        newBasePrice,
                        shortDesc, // Short Description-EN
                        shortDesc, // Short Description-TW
                        shortDesc, // Short Description-DE
                        shortDesc, // Short Description-KR
                        shortDesc, // Short Description-JA
                        shortDesc, // Short Description-CN
                        originalBasePrice
                };
                writer.writeNext(record);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 根據商品的 Core 與價格資訊產生 HTML 表格 (Short Description)
     *
     * 不同 Core 處理方式：
     *  - 8051、M0M23、Audio：六個階梯
     *       1-99    → price1
     *       100-499 → price2
     *       500-999 → price3
     *       1000-1999 → price4
     *       2000-4999 → price5
     *       5000+   → Contact (超連結)
     *
     *  - M4：四個階梯
     *       1-999    → price1
     *       1000-1999 → price2
     *       2000-4999 → price3
     *       5000+    → Contact
     *
     *  - MPU：四個階梯
     *       1-99    → price1
     *       100-499 → price2
     *       500-999 → price3
     *       1000+   → Contact
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
