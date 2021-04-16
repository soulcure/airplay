package com.coocaa.movie.web.product;

import java.io.Serializable;

public class PayOrder implements Serializable {
    public String product_id;
    public String movie_id;
    public String node_type;
    public String title;
    public String price;
    public String count;
    public String discount_price;
    public String auth_type;
    public String discount_info;
    public String third_id;
    public int option;
}
