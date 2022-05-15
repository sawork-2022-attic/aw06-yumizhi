package com.example.batch.service;

import com.example.batch.config.BatchConfig;
import com.example.batch.model.Product;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;

import java.util.List;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ProductWriter implements ItemWriter<Product>, StepExecutionListener {
    private Connection connection;
    @Override
    public void beforeStep(StepExecution stepExecution) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/amazondata","root", "201213");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void write(List<? extends Product> list) throws Exception {
//        list.stream().forEach(System.out::println);
//        System.out.println("chunk written");
        Statement stmt = connection.createStatement();
        stmt.execute(String.format("CREATE TABLE IF NOT EXISTS %s(main_cat varchar(40), title varchar(100), asin char(10) not null primary key, category varchar(40), price varchar(10), imageURLHighRes varchar(120))default charset=utf8;", BatchConfig.tableName));

        for (Product product : list) {
            List<String> category = product.getCategory();
            List<String> url = product.getImageURLHighRes();
            String price = product.getPrice();
            try {
                if(price == null || price.isEmpty() || url == null || url.isEmpty())
                    continue;

                stmt.execute(String.format("INSERT INTO %s(main_cat, title, asin, category, price, imageURLHighRes) VALUES ('%s', '%s', '%s', '%s', '%s', '%s')",
                        BatchConfig.tableName, product.getMain_cat(), product.getTitle(), product.getAsin(),
                        (category == null || category.isEmpty()) ? "software" : category.get(0),
                        (price == null || price.isEmpty()) ? "$0" : price,
                        (url == null || url.isEmpty()) ? "" : url.get(0)));
            } catch (SQLException e) {}
        }
        stmt.close();

    }
}
