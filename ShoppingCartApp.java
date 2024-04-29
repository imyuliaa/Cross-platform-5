import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private double price;

    public Product(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}

class ShoppingCart implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Product> items;

    public ShoppingCart() {
        items = new ArrayList<>();
    }

    public void addItem(Product product) {
        items.add(product);
    }

    public void removeItem(int index) {
        items.remove(index);
    }

    public void editItem(int index, String newName, double newPrice) {
        Product product = items.get(index);
        product.setName(newName);
        product.setPrice(newPrice);
    }

    public List<Product> getItems() {
        return items;
    }

    public double calculateTotal() {
        double total = 0;
        for (Product item : items) {
            total += item.getPrice();
        }
        return total;
    }

    public void printInvoice() {
        StringBuilder invoice = new StringBuilder("Розрахунок:\n");
        for (Product item : items) {
            invoice.append(item.getName()).append(" - $").append(item.getPrice()).append("\n");
        }
        invoice.append("Всього: $").append(calculateTotal());
        System.out.println(invoice.toString());
    }

    public void saveToFile(String filename) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(this);
            System.out.println("Data saved to " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ShoppingCart loadFromFile(String filename) {
        ShoppingCart cart = null;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            cart = (ShoppingCart) in.readObject();
            System.out.println("Data loaded from " + filename);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return cart;
    }
}

public class ShoppingCartApp extends JFrame {
    private ShoppingCart cart;
    private JList<String> productList;
    private DefaultListModel<String> listModel;
    private JLabel totalLabel; // Added label for total cost

    public ShoppingCartApp() {
        setTitle("Онлайн кошик покупок");
        setSize(600, 350); // Increased height to accommodate total label
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cart = ShoppingCart.loadFromFile("shopping_cart.ser");
        if (cart == null) {
            cart = new ShoppingCart();
        }

        initUI();

        updateProductList();
        updateTotalLabel(); // Update total label initially
    }

    private void initUI() {
        // Colors
        Color bgColor = new Color(230, 230, 230);
        Color buttonColor = new Color(100, 160, 240);

        getContentPane().setBackground(bgColor);

        listModel = new DefaultListModel<>();
        productList = new JList<>(listModel);
        productList.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(productList);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3));
        buttonPanel.setBackground(bgColor);
        add(buttonPanel, BorderLayout.SOUTH);

        JButton addButton = new JButton("Додати продукт");
        JButton editButton = new JButton("Змінити продукт");
        JButton removeButton = new JButton("Вилучити продукт");

        addButton.setBackground(buttonColor);
        editButton.setBackground(buttonColor);
        removeButton.setBackground(buttonColor);

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(removeButton);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = JOptionPane.showInputDialog("Введіть назву продукту:");
                double price = Double.parseDouble(JOptionPane.showInputDialog("Введіть ціну продукту:"));
                Product product = new Product(name, price);
                cart.addItem(product);
                listModel.addElement(product.getName() + " - $" + product.getPrice());
                cart.saveToFile("shopping_cart.ser");
                updateTotalLabel(); // Update total label after adding product
            }
        });

        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = productList.getSelectedIndex();
                if (selectedIndex != -1) {
                    String newName = JOptionPane.showInputDialog("Введіть нове ім'я:");
                    double newPrice = Double.parseDouble(JOptionPane.showInputDialog("Введіть нову ціну:"));
                    cart.editItem(selectedIndex, newName, newPrice);
                    listModel.setElementAt(newName + " - $" + newPrice, selectedIndex);
                    cart.saveToFile("shopping_cart.ser");
                    updateTotalLabel(); // Update total label after editing product
                }
            }
        });

        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = productList.getSelectedIndex();
                if (selectedIndex != -1) {
                    cart.removeItem(selectedIndex);
                    listModel.remove(selectedIndex);
                    cart.saveToFile("shopping_cart.ser");
                    updateTotalLabel(); // Update total label after removing product
                }
            }
        });

        // Total Label
        totalLabel = new JLabel();
        totalLabel.setHorizontalAlignment(SwingConstants.CENTER);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalLabel.setText("Всього: $0.00");
        add(totalLabel, BorderLayout.NORTH);

        JButton invoiceButton = new JButton("Розрахунок"); // Added invoice button
        invoiceButton.setBackground(buttonColor);
        buttonPanel.add(invoiceButton);

        invoiceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showInvoice(); // Method to show invoice
            }
        });
    }

    private void updateProductList() {
        listModel.clear();
        for (Product item : cart.getItems()) {
            listModel.addElement(item.getName() + " - $" + item.getPrice());
        }
    }

    private void updateTotalLabel() {
        totalLabel.setText("Всього: $" + String.format("%.2f", cart.calculateTotal()));
    }

    private void showInvoice() {    JFrame invoiceFrame = new JFrame("Чек про оплату");
        invoiceFrame.setSize(300, 400);    invoiceFrame.setLocationRelativeTo(null);
        invoiceFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JTextArea invoiceTextArea = new JTextArea();    invoiceTextArea.setEditable(false);
        Random random = new Random();
        int invoiceNumber = random.nextInt(100000); // Генеруємо випадковий номер чеку
        StringBuilder invoiceText = new StringBuilder("---------------\n");    invoiceText.append("Номер чеку: ").append(invoiceNumber).append("\n");
        invoiceText.append("---------------\n");
        for (Product item : cart.getItems()) {        invoiceText.append(item.getName()).append(" - $").append(item.getPrice()).append("\n");
        }    invoiceText.append("---------------\n");
        invoiceText.append("Загальна сума: $").append(String.format("%.2f", cart.calculateTotal())).append("\n");    invoiceText.append("Дякуємо за покупки!\n");
        invoiceText.append("---------------\n");
        invoiceTextArea.setText(invoiceText.toString());
        JScrollPane scrollPane = new JScrollPane(invoiceTextArea);    invoiceFrame.add(scrollPane, BorderLayout.CENTER);
        invoiceFrame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ShoppingCartApp().setVisible(true);
            }
        });
    }
}
