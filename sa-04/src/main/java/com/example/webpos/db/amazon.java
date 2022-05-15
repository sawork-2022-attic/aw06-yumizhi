package com.example.webpos.db;

import com.example.webpos.model.Product;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class amazon implements PosDB {


    private List<Product> products = null;
    private List<Product> products1 = null;

    @Override
    public List<Product> getProducts() {
        try {
            if (products == null) {
                products = parseAmazon("all_beauty", 30);
                products1 = parseAmazon("software", 30);
                products.addAll(products1);
            }
        } catch (IOException e) {
            products = new ArrayList<>();
        }
        return products;
    }

    @Override
    public Product getProduct(String productId) {
        for (Product p : getProducts()) {
            if (p.getId().equals(productId)) {
                return p;
            }
        }
        return null;
    }

    public static List<Product> parseAmazon(String keyword, int limit) throws IOException {
        List<Product> list = new ArrayList<>();


        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/amazondata", "root", "201213");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return list;
        }

        if (connection == null)
            System.out.print("connection is null!\n");

        try {
            Statement stmt = connection.createStatement();
            if (!stmt.execute(String.format("SELECT * FROM amazondata" + ".%s LIMIT %d, %d;", keyword, 0, limit))) {
                return list;
            }
            ResultSet resultSet = stmt.getResultSet();
            while (resultSet.next()) {
                String id = resultSet.getString("asin");
                String img = resultSet.getString("imageURLHighRes");
                String price = resultSet.getString("price");
                String title = resultSet.getString("title");

                Product product = new Product(id, title, Double.parseDouble(price.substring(1)), img);

                list.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return list;
        }

        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

}
