# Prestashop Importer

This project is used to read multiple CSV files, integrate product information and price update data, and then generate a new CSV file for uploading to an e-commerce website (such as Prestashop or other platforms).

## Purpose

- **CSV File Reading:**
    - **`1_price_update.csv`:** Contains the names of products that require a price update.
    - **`2_product_in_direct.csv`:** Contains product IDs, names, and the original Base Price.
    - **`3_product_new_price.csv`:** Contains product cores and tiered pricing information.

- **Data Processing:**
  The program generates an HTML-formatted short description based on the product core, then integrates it with the other data to produce a new CSV file. The output CSV includes the following columns:
    - Product ID
    - Product Name
    - New Base Price (using the first tier price as an example)
    - Six identical Short Description columns (named Short Description-EN, Short Description-TW, Short Description-DE, Short Description-KR, Short Description-JA, Short Description-CN)
    - Original Base Price

## Usage

1. **CSV File Placement:**
    - Place the following CSV files in a folder named `csv` located in the root directory of the project:
        - `1_price_update.csv`
        - `2_product_in_direct.csv`
        - `3_product_new_price.csv`

2. **Running the Project:**
    - This is a Maven project. You can run the project directly in IntelliJ IDEA by executing `Main.java` (or via the command line using `mvn compile exec:java`).
    - Upon execution, the program will read the CSV files and generate an `output.csv` file in the `csv` folder.

3. **Notes:**
    - CSV檔案在Fish手上請來信索取哈哈哈
