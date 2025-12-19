package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.OrderLineItemDao;
import org.yearup.models.OrderLineItem;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Component
public class MySqlOrderLineItemDao extends MySqlDaoBase implements OrderLineItemDao {

    // pass datasource up to base dao so we can open db connections
    public MySqlOrderLineItemDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void create(OrderLineItem item) {

        // insert a single line item for an order
        // each row represents one product within an order
        String sql = """
                INSERT INTO order_line_items
                (order_id, product_id, quantity, price)
                VALUES (?, ?, ?, ?)
                """;

        // try with resources ensures the connection closes automatically
        try (Connection connection = getConnection()) {

            // prepare insert statement
            PreparedStatement statement = connection.prepareStatement(sql);

            // bind order id so this line item belongs to the correct order
            statement.setInt(1, item.getOrderId());

            // bind which product this line item refers to
            statement.setInt(2, item.getProductId());

            // bind how many units were purchased
            statement.setInt(3, item.getQuantity());

            // bind price at time of purchase (important for price history)
            statement.setBigDecimal(4, item.getPrice());

            // execute insert
            statement.executeUpdate();
        } catch (SQLException e) {
            // bubble up any db errors
            throw new RuntimeException(e);
        }
    }
}