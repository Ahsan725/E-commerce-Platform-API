package org.yearup.data;

import org.yearup.models.Order;

public interface OrderDao {
    Order create(Order order); //I think this is the only methid I will need
}