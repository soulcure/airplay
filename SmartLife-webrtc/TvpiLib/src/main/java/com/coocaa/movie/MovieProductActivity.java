package com.coocaa.movie;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.coocaa.movie.product.p.IProductPresenter;
import com.coocaa.movie.product.p.ProductPresenterImpl;
import com.coocaa.movie.product.v.ProductListMainView;
import com.coocaa.movie.util.UtilMovie;

public class MovieProductActivity extends AppCompatActivity {

    IProductPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UtilMovie.instence(this);

        ProductListMainView productListMainView = new ProductListMainView(this);
        setContentView(productListMainView);
        presenter = new ProductPresenterImpl(productListMainView, this);
        presenter.prepareData();
    }
}