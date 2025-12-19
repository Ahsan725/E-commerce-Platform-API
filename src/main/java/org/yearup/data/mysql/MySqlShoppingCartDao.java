package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.sql.*;

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao
{
    // used to hydrate cart items with full product details
    private final ProductDao productDao;

    // datasource goes to base dao, productDao is needed when mapping rows
    public MySqlShoppingCartDao(DataSource dataSource, ProductDao productDao)
    {
        super(dataSource);
        this.productDao = productDao;
    }

    @Override
    public ShoppingCart getByUserId(int userId)
    {
        // start with an empty cart and fill it from db rows
        ShoppingCart cart = new ShoppingCart();

        // pull all cart rows for this user
        String sql = """
            SELECT product_id, quantity
            FROM shopping_cart
            WHERE user_id = ?;
        """;

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);

            // bind the user id so we only get this user’s cart
            statement.setInt(1, userId);

            ResultSet row = statement.executeQuery();

            // convert each db row into a ShoppingCartItem
            while (row.next())
            {
                ShoppingCartItem item = mapRow(row);
                cart.add(item);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        return cart;
    }

    @Override
    public void addProduct(int userId, int productId)
    {
        // add a new product to the cart with quantity defaulted to 1
        String sql = """
            INSERT INTO shopping_cart (user_id, product_id, quantity)
            VALUES (?, ?, 1);
        """;

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);

            // bind user + product
            statement.setInt(1, userId);
            statement.setInt(2, productId);

            // execute insert
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateQuantity(int userId, int productId, int quantity)
    {
        // overwrite quantity for an existing cart row
        String sql = """
            UPDATE shopping_cart
            SET quantity = ?
            WHERE user_id = ? AND product_id = ?;
        """;

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);

            // bind new quantity
            statement.setInt(1, quantity);

            // identify which cart row to update
            statement.setInt(2, userId);
            statement.setInt(3, productId);

            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean exists(int userId, int productId)
    {
        String sql = """
            SELECT 1
            FROM shopping_cart
            WHERE user_id = ? AND product_id = ?;
        """;

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);

            // bind user + product
            statement.setInt(1, userId);
            statement.setInt(2, productId);

            ResultSet row = statement.executeQuery();

            // if at least one row exists, the item is in the cart
            return row.next();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getQuantity(int userId, int productId)
    {
        // fetch the current quantity for a specific cart item
        String sql = """
            SELECT quantity
            FROM shopping_cart
            WHERE user_id = ? AND product_id = ?;
        """;

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);

            // bind identifiers
            statement.setInt(1, userId);
            statement.setInt(2, productId);

            ResultSet row = statement.executeQuery();

            // return quantity if row exists
            if (row.next())
            {
                return row.getInt("quantity");
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        // fallback if item isn’t found
        return 0;
    }

    @Override
    public void clearCart(int userId)
    {
        // remove all cart rows for this user
        String sql = "DELETE FROM shopping_cart WHERE user_id = ?;";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);

            // bind user id
            statement.setInt(1, userId);

            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    private ShoppingCartItem mapRow(ResultSet row) throws SQLException
    {
        // extract raw values from the cart table
        int productId = row.getInt("product_id");
        int quantity = row.getInt("quantity");

        // build cart item object
        ShoppingCartItem item = new ShoppingCartItem();

        // this gives name, price, description
        item.setProduct(productDao.getById(productId));

        // set quantity from cart table
        item.setQuantity(quantity);

        return item;
    }
}