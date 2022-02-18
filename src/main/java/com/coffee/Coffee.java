package com.coffee;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Coffee {

    // The amount paid by users. Keys are users and values are amounts
    private static Map<String, Integer> amounts = new HashMap<>();

    // The price information for all products. Keys are users and values are a map with keys size and values prices
    private static Map<String, Map<String, Integer>> products = new HashMap<>();

    // The amount ordered by users. Keys are users and values are amounts
    private static Map<String, Integer> ordered = new HashMap<>();

    // The list of users
    private static List<String> users = new ArrayList<>();

    // Add a user to the list
    private static void addUserIfNotExists(String user) {
        for (String userSearched : users) {
            if (userSearched.equals(user)) {
                return;
            }
        }
        users.add(user);
    }

    // Process a payment. Sum the amounts in cents
    private static void processPayment(JSONObject payment) {
        String user = payment.getString("user");
        addUserIfNotExists(user);
        if (amounts.get(user) != null) {
            amounts.put(user, amounts.get(user) + (payment.getInt("amount") * 100));
        } else {
            amounts.put(user, payment.getInt("amount") * 100);
        }
    }

    // Process a product. Create a map entry with price information for a product.
    private static void processProduct(JSONObject product) {

        List<String> sizes = new ArrayList<>();
        sizes.add("small");
        sizes.add("medium");
        sizes.add("large");
        sizes.add("huge");
        sizes.add("mega");
        sizes.add("ultra");

        products.put(product.getString("drink_name"), new HashMap<>());
        JSONObject prices = product.getJSONObject("prices");
        for (int i = 0; i < prices.length(); i++) {
            for (String size : sizes) {
                if (prices.has(size)) {
                    Number price = prices.getNumber(size);
                    Double price_double = Double.parseDouble(price.toString()) * 100;
                    int price_integer = price_double.intValue();
                    products.get(product.getString("drink_name")).put(size, price_integer);
                }
            }
        }
    }

    // Process an order. Sum the amount ordered in cents.
    private static void processOrders(JSONObject order) {
        String user = order.getString("user");
        String size = order.getString("size");
        String product = order.getString("drink");

        addUserIfNotExists(user);

        int price = products.get(product).get(size);
        ordered.merge(user, price, Integer::sum);
    }

    /** The main method of the program.
     *
     * Process payments, then products and finally orders. Finally write the information for the amounts paid by every
     * user and the amount still owed for every user.
     */
    public static void main(String[] args) {
        System.out.println("Coffee!");

        System.out.println("");
        JSONArray orders = null;
        try {
            orders = new JSONArray(Files.readString(Path.of("src/main/resources/orders.json")));
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
            System.exit(1);
        }

        JSONArray products = null;
        try {
            products = new JSONArray((Files.readString(Path.of("src/main/resources/products.json"))));
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
            System.exit(1);
        }

        JSONArray payments = null;
        try {
            payments = new JSONArray(Files.readString((Path.of("src/main/resources/payments.json"))));
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
            System.exit(1);
        }

        for (int i = 0; i < payments.length(); i++) {
            processPayment(payments.getJSONObject(i));
        }

        for (int i = 0; i < products.length(); i++) {
            processProduct(products.getJSONObject(i));
        }

        for (int i = 0; i < orders.length(); i++) {
            processOrders(orders.getJSONObject(i));
        }

        for (Map.Entry<String, Integer> amount : amounts.entrySet()) {
            System.out.println(amount.getKey() + " has paid " + " : " + amount.getValue());
        }

        for (String user : users) {
            int owing = (ordered.get(user) - amounts.get(user));
            if (owing < 0) {
                // User has paid more than he/she actually owes
                owing = 0;
            }
            System.out.println(user + " owes : " + owing);
        }
    }
}
